import io.thekraken.grok.api.exception.GrokException
import io.thekraken.grok.api.Grok

class GrokPattern {
  
}

object GrokPattern {
   def unitTest()
   {
        //bigdl.unittest(); return
    /** Create a new grok instance */
    val grok = Grok.create("patterns/patterns");

    /** Grok pattern to compile, here httpd logs */
    grok.compile("%{COMBINEDAPACHELOG}");

    /** Line of log to match */
    val log =
      "112.169.19.192 - - [06/Mar/2013:01:36:30 +0900] \"GET / HTTP/1.1\" 200 44346 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\""

    val gm = grok.`match`(log);
    gm.captures();

    /** Get the output */
    println(gm.toJson());

    return
  }
}