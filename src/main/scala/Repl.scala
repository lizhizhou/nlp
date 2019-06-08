import org.apache.spark.sql.SparkSession

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{ILoop, NamedParam};

object Repl {
  def start(spark: SparkSession, Commands:String, args: NamedParam*) = {
    val initialCommands =
      """
         import java.lang.Math._
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

      override def prompt: String = "nlp> "

      override def createInterpreter(): Unit = {
        super.createInterpreter()

        intp.beQuietDuring {
          intp.bind("spark", spark)
          intp.interpret(initialCommands)
          intp.interpret(Commands)
          args.toList.foreach(p => intp.bind(p))
        }
      }
    }
    repl.process(new Settings {
      usejavacp.value = true
      deprecation.value = true
    })
  }

}
