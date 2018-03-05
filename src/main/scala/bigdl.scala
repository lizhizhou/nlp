import java.nio.ByteOrder

import com.intel.analytics.bigdl.nn._
import com.intel.analytics.bigdl.numeric.NumericFloat

class bigdl {

}

object bigdl {

  def unittest = {
    val modelPath = "./syntaxnet/models/output_graph.pb"
    val binPath = "./syntaxnet/models/lm.binary"
    val inputs = Seq("input_node","input_lengths")
    val outputs = Seq("output_node")

    // For tensorflow freezed graph or graph without Variables
    val model = Module.loadTF(modelPath, inputs, outputs, ByteOrder.LITTLE_ENDIAN)

  }
}