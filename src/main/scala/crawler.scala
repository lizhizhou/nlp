import java.net.{HttpURLConnection, SocketTimeoutException, URL}

import org.apache.spark.sql.functions._

import scala.collection.JavaConversions._
import java.io._
import java.util.concurrent._
import java.util.HashSet
import java.util.regex.Pattern

import scala.collection.mutable.HashMap
import scala.collection.mutable.Stack
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.apache.log4j.Logger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

//https://blog.csdn.net/liangyihuai/article/details/54698469
/**
  *
  * @param startPage
  * @param outputPath 所爬取小说的存储路径，默认为当前目录下的crawl.txt文件.
  * @param filter url的过滤条件, 默认为true
  */
class crawler(startPage: String, filter: (String => Boolean) = (url: String) => true, maxLink:Int=200) extends Serializable {
  @transient lazy val log = Logger.getLogger(this.getClass)
  /**
    * 获取链接的正则表达式
    */
  private val linkRegex = """ (src|href)="([^"]+)"|(src|href)='([^']+)' """.trim.r

  /**
    * 地址最大长度
    */
  private val maxLinklength = 200

  /**
    * 文件类型正则
    */
  private val htmlTypeRegex = ".*text\\/html.*"

  /**
    * 存储符合条件的链接
    */
  private val crawledPool = new HashSet[String]

  /**
    * 连接超时时间
    */
  private val CONN_TIME_OUT = 10*1000

  /**
    * 读取超时时间超时时间
    */
  private val READ_TIME_OUT = 15*1000

  def crawl(outputPath: String = "./crawl.txt"):Unit ={
    //爬取原始html页面
    val linksAndContent = doCrawlPages(startPage)
    //解析和提取有价值的内容
    linksAndContent.foreach(entry => {linksAndContent += (entry._1 -> extractTitleAndContent(entry._2))})
    //保存数据到文件系统中
    storeContent(linksAndContent, outputPath)
  }

  def crawl():HashMap[String,String] = {
    //爬取原始html页面
    val linksAndContent = doCrawlPages(startPage)
    //解析和提取有价值的内容
    linksAndContent.foreach(entry => {linksAndContent += (entry._1 -> extractTitleAndContent(entry._2))})
    linksAndContent
  }

  /**
    * 这个函数负责主要的爬取任务。它调用线程池中的一条线程来爬取一个页面，返回所爬取的页面内容和对应的url。
    * 该方法没有采用递归的方式来爬取网页内容，取而代之的是自定义一个栈结构，从而避免了大量的link造成的栈溢出和速度慢的问题。
    * 主线程需要等待其他爬取工作线程结束之后再进行下一步动作，又因为这里的爬取工作线程的数量是不确定的，这里的解决方法是
    * 让主线程循环等待，直到所有的爬取工作线程结束（正常完成任务或者超时）.
    *
    * @param pageUrl
    * @return  存储链接和对应的页面数据（HTML）. key：url， value：原始的html页面数据。
    */
  private def doCrawlPages(pageUrl: String):HashMap[String,String] ={
    //创建线程池
    val threadPool: ThreadPoolExecutor = new ThreadPoolExecutor(10, 200, 3, TimeUnit.SECONDS,
      new LinkedBlockingDeque[Runnable](),
      new ThreadPoolExecutor.CallerRunsPolicy())
    //设置线程池相关属性
    threadPool.allowCoreThreadTimeOut(true)
    threadPool.setKeepAliveTime(6, TimeUnit.SECONDS)
    //存储该函数的返回值
    val result = new HashMap[String, String]()
    //用于存储每个页面符合条件的url，该栈共享于多个线程
    val LinksStack = Stack[String]()
    LinksStack.push(pageUrl)
    try{
      do{//线程池中还有任务在进行
        while(!LinksStack.isEmpty){//link栈不空
        val link = LinksStack.pop()
          if(crawledPool.size() < maxLink) {
            val future = new FutureTask[(Int, String, Map[String, String])](
                new Callable[(Int, String, Map[String, String])]{
                  @Override
                  def call() = {
                    getPageFromRemote(link)
                  }
                })
            threadPool.execute(future)

            val pageContent = future.get(this.READ_TIME_OUT, TimeUnit.SECONDS)._2
            val tempLinks = parseCrawlLinks(link, pageContent)

            tempLinks.filter(_.length() < maxLinklength)
              .filter(!crawledPool.contains(_)).foreach(LinksStack.push(_))
            result += (link -> pageContent)
          }
        }
        Thread.sleep(200)
      }while(threadPool.getActiveCount != 0)
    }finally {
      threadPool.shutdown()
    }
    result
  }


  /**
    * 连接将要爬取得网站，并下载该url的内容
    *
    * @param url
    * @return ResponseCode， page内容， headers
    */
  def getPageFromRemote(url: String):(Int, String, Map[String, String]) = {
    val uri = new URL(url);

    var conn:HttpURLConnection = null
    var status:Int = 0
    var data:String = ""
    var headers:Map[String,String] = null
    try{
      conn = uri.openConnection().asInstanceOf[HttpURLConnection];
      conn.setConnectTimeout(CONN_TIME_OUT)
      conn.setReadTimeout(this.READ_TIME_OUT)
      val stream = conn.getInputStream()
      val bufferedReader = new BufferedReader(new InputStreamReader(stream, "utf-8"))

      val strBuf = new StringBuilder()
      var line = bufferedReader.readLine()
      while (line != null) {
        strBuf.append(line)
        line = bufferedReader.readLine()
      }
      data = strBuf.toString()
      status = conn.getResponseCode()
      //根据status code判断页面是否被重定向了，从而进一步处理。这里略掉此步骤。

      headers = conn.getHeaderFields().toMap.map {
        head => (head._1, head._2.mkString(","))
      }
    }catch{
      case e:SocketTimeoutException => log.error(e.getStackTrace)
      case e2:Exception => log.error(e2.getStackTrace)
    }finally {
      if(conn != null) conn.disconnect
      crawledPool.add(url)
    }
    if (headers == null || !isTextPage(headers)) data="";
    return (status, data, headers)
  }

  /**
    * 从HTML文件中提取符合条件的URL
    *
    * @param parentUrl
    * @param html
    * @return
    */
  private def parseCrawlLinks(parentUrl: String, html: String) = {
    val baseHost = getHostBase(parentUrl)
    val links = fetchLinks(html).map {
      link =>
        link match {
          case link if link.startsWith("/") => baseHost + link
          case link if link.startsWith("http:") || link.startsWith("https:") => link
          case _ =>
            val index = parentUrl.lastIndexOf("/")
            parentUrl.substring(0, index) + "/" + link
        }
    }.filter {
      link => !link.matches(".*(\\.ico|\\.jpeg|\\.jpg|\\.png|\\.gif)$")  //remove pic
    }
    .filter {
      link => !crawledPool.contains(link) && this.filter(link)
    }.map(link => link.replaceAll("#.*", ""))
    log.info("find " + links.size + " links at page " + parentUrl)
    links
  }

  /**
    * 通过正则表达式从页面中提取出所有的url，包含不符合条件的
    *
    * @param html
    * @return
    */
  private def fetchLinks(html: String):Set[String] = {
    val list = for (m <- linkRegex.findAllIn(html).matchData if (m.group(1) != null || m.group(3) != null)) yield {
      if (m.group(1) != null) m.group(2) else m.group(4)
    }

    list.filter {
      link => !link.startsWith("#") && !link.startsWith("javascript:") && link != "" && !link.startsWith("mailto:")
    }.toSet
  }

  /**
    * 根据第一个url得到该网站的基本url
    *
    * @param url
    * @return
    */
  private def getHostBase(url: String) = {
    val uri = new URL(url)
    val portPart = if (uri.getPort() == -1 || uri.getPort() == 80) "" else ":" + uri.getPort()
    uri.getProtocol() + "://" + uri.getHost() + portPart
  }

  /**
    * 判断所爬取的网页是不是文本类型
    *
    * @param headers
    * @return
    */
  private def isTextPage(headers: Map[String, String]) = {
    val contentType = if (headers contains "Content-Type") headers("Content-Type") else null
    contentType match {
      case null => false
      case contentType if contentType isEmpty => false
      case contentType if Pattern.compile(htmlTypeRegex).matcher(contentType).find => true
      case _ => false
    }

  }

  /**
    * 从原始的html文件中提取出自己想要的内容。所以需要修改这个函数来适应不同的网站页面。
    * toDo Use the HTML/XML parser
    *
    * @param html
    * @return
    */

  private def extractTitleAndContent(html:String):String ={
    if(html.isEmpty) return ""
    val browser = JsoupBrowser()
    val htmldom = browser.parseString(html)
    log.trace(html)
    val title = (htmldom >> texts("title")).mkString(" ")
    val content = (htmldom >> texts("p")).mkString(" ")
    //map (_.replaceAll("<br />|&nbsp;+|\t+", ""))
    s"${title}\n${content}\n\n"
  }

  /**
    * 保存所爬取得数据到文件系统中。
    *
    */
  private def storeContent(linksAndContent:HashMap[String, String], outputPath:String):Unit = {
    val writer = new BufferedWriter(new FileWriter(new File(outputPath)))
    val values = linksAndContent.valuesIterator
    while(values.hasNext){
      writer.write(values.next())
    }
    writer.close()
  }
}

object crawler{
  def crawler_url(link:String) = {
    val data = new crawler(link,
      filter = (url:String) => url.contains(link)).crawl()
    data.map{case (k,v) => v }.mkString(" ")
  }

  def encode(path: String): String =
    URLEncoder.encode(path, StandardCharsets.UTF_8.toString).toLowerCase

  def crawler_baidu_url(query:String) = {
    val data = new crawler("http://www.baidu.com/s?wd="+crawler.encode(query),
      filter = (url:String) => url.contains("http://www.baidu.com")).crawl()
    data.map{case (k,v) => v }.mkString(" ")
  }

  val crawler_site = udf((link: String) => crawler_url(link))
  val crawler_baidu = udf((link: String) => crawler_baidu_url(link))

  def main(args:Array[String]): Unit ={
//    new crawler("http://lizhizhou.github.io/", //http://www.example.com/,
//      filter = (url:String) => url.contains("http://lizhizhou.github.io/")).crawl("crawl.txt")
    new crawler("http://www.baidu.com/s?wd="+crawler.encode("孙智勇"),
            filter = (url:String) =>url.contains("http://www.baidu.com")).crawl("crawl.txt")

  }
}