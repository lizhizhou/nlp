import org.apache.log4j.Logger
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{ILoop, NamedParam};

object Repl {
  @transient lazy val log = Logger.getLogger(this.getClass)
  def start(Commands:String, args: NamedParam*) = {
    val initialCommands =
      """
         import java.lang.Math._
         import org.apache.spark.sql._
      """.stripMargin
    val repl = new ILoop {
      override def printWelcome() {
        echo("\n" +
        """
                          ,--,
                   ,--.,---.'|  ,-.----.
                 ,--.'||   | :  \    /  \
             ,--,:  : |:   : |  |   :    \
          ,`--.'`|  ' :|   ' :  |   |  .\ :
          |   :  :  | |;   ; '  .   :  |: |
          :   |   \ | :'   | |__|   |   \ :
          |   : '  '; ||   | :.'|   : .   /
          '   ' ;.    ;'   :    ;   | |`-'
          |   | | \   ||   |  ./|   | ;
          '   : |  ; .';   : ;  :   ' |
          |   | '`--'  |   ,/   :   : :
          '   : |      '---'    |   | :
          ;   |.'               `---'.|
          '---'                   `---`
        """)
      }

      override def prompt: String = "nlp>"

      override def createInterpreter(): Unit = {
        super.createInterpreter()

        intp.beQuietDuring {
          intp.interpret(initialCommands)
          intp.interpret(Commands)
          args.toList.foreach(p => {
            log.trace(p)
            if (p.tpe == "org.apache.spark.sql.Dataset") intp.bind(p.name,"org.apache.spark.sql.DataFrame",p.value)
            else intp.bind(p)
          })
        }
      }
    }
    repl.process(new Settings {
      usejavacp.value = true
      deprecation.value = true
    })
  }

}
