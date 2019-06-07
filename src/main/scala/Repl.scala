import org.apache.spark.sql.SparkSession

import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.ILoop;

object Repl {
  def start(spark: SparkSession, initialCommands:String = "") = {
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
        }
      }
    }
    repl.process(new Settings {
      usejavacp.value = true
      deprecation.value = true
    })
  }

}