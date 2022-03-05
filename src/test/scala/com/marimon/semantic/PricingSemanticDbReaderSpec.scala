package com.marimon.semantic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.meta.internal.semanticdb.TextDocuments

/**
 * 
 */
class PricingSemanticDbReaderSpec extends AnyFlatSpec with Matchers {

  private lazy val parentFolder = "/Users/imarimon/git/github/hopper-org/ImagoMundi/local-repos/clones/hopper-org/"
  private lazy val folder = parentFolder + "Helpers"
  private lazy val documents: Array[TextDocuments] = SemanticDbReader.loadRecursively(folder)

  behavior of "SemanticDbReader"

  it should "locate upstream containers" in {
    val reader = new SemanticDbReader(documents)

    /**
     * contains all deps including all dependency steps (class to method to argument to class to...)
     */
    val usages = reader.findTypeGraphTo(
      "com/hopper/common/model/api/Paris.Pricing#"
    )

    /**
     * val sample: Nothing = null {
A -> B;
B -> C;
}
     */
    println(
      s"""
         |digraph sample {
         |${usages.map{edge => s"  \"${edge.from.name}\" -> \"${edge.to.name}\" ;"}.mkString("\n")}
         |}
         |""".stripMargin)

    //    val (usages, sourcePaths) = reader.findUsageWithMetadata(
//      "com/hopper/common/model/api/Paris.Pricing#"
//    )
//    val projectNames = sourcePaths.map{ sourcePath =>
//      sourcePath.toString.replace(parentFolder, "").split("/").head
//    }
//    println(
//      s"""
//         |Found ${usages.size} usages in projects:
//         |${projectNames.toSeq.sorted.mkString("\n - ")}
//         |""".stripMargin)

    usages should contain theSameElementsAs usages
  }

}
