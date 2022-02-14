package com.marimon.semantic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.meta.internal.semanticdb.TextDocuments

/**
 * 
 */
class PricingSemanticDbReaderSpec extends AnyFlatSpec with Matchers {

  val folder = "/Users/imarimon/git/github/hopper-org/ImagoMundi/local-repos/clone/"
  private val documents: Array[TextDocuments] = MainSemanticPoc.loadDocuments(folder)

  behavior of "SemanticDbReader"

  it should "locate upstream containers" in {
    new SemanticDbReader(documents).findContainers(
      "com/hopper/common/model/api/Paris#Pricing"
    ) should contain theSameElementsAs Set(
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
  it should "locate usages" in {}
  it should "locate downstream contained" in {}

}
