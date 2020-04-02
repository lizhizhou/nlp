import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.util.FileManager
import java.io.InputStream

import org.apache.spark.sql.SparkSession

class JenaGraphX()
{


}

object JenaGraphX {
  def unitTest(spark: SparkSession) {
    val model: Model = ModelFactory.createDefaultModel
    // use the FileManager to find the input file
    val inputFileName = "vc-db-1.rdf"
    val in: InputStream = FileManager.get.open(inputFileName)
    if (in == null) throw new IllegalArgumentException("File: " + inputFileName + " not found")

    // read the RDF/XML file
    model.read(in, null)
    // write it to standard out
    model.write(System.out)
  }
}