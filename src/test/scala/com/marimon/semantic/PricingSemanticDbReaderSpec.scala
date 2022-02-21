package com.marimon.semantic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.meta.internal.semanticdb.TextDocuments

/**
 * 
 */
class PricingSemanticDbReaderSpec extends AnyFlatSpec with Matchers {

  private lazy val parentFolder = "/Users/imarimon/git/github/hopper-org/ImagoMundi/local-repos/clones/hopper-org"
  private lazy val folder = parentFolder + "/Helpers"
  private lazy val documents: Array[TextDocuments] = SemanticDbReader.loadRecursively(folder)

  behavior of "SemanticDbReader"

  it should "locate upstream containers" in {
    var reader = new SemanticDbReader(documents)

    val usages = reader.findUsage(
      "com/hopper/common/model/api/Paris.Pricing#"
    )




    usages should contain theSameElementsAs Set(
      // Bar is used in Baz.B
      "com/marimon/semantic/samples/Baz#b.",
      // ... whic is in Baz
      "com/marimon/semantic/samples/Baz#",
      // ... which is the return type in foo
      "com/marimon/semantic/samples/Hello#foo().",
      // ... which is in Hello
      "com/marimon/semantic/samples/Hello#"
    )
  }

}
