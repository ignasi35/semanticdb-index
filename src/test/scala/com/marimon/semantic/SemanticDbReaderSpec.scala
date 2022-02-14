package com.marimon.semantic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.meta.internal.semanticdb.TextDocuments

/**
 * 
 */
class SemanticDbReaderSpec extends AnyFlatSpec with Matchers {

  private val baseFolder = "/Users/imarimon/git/github/ignasi35/semanticdb-index/target/scala-2.13/meta/META-INF/semanticdb/src/main/scala/com/marimon/semantic/samples"
  private val foobarbaz = baseFolder+"/foobarbaz"
  private val returntypes = baseFolder+"/returntypes"
  private val alphabet = baseFolder+"/alphabet"

  private val documentsfoobarbaz: Array[TextDocuments] = MainSemanticPoc.loadDocuments(foobarbaz)
  private val documentsreturntypes: Array[TextDocuments] = MainSemanticPoc.loadDocuments(returntypes)
  private val documentsalphabet: Array[TextDocuments] = MainSemanticPoc.loadDocuments(alphabet)

  behavior of "SemanticDbReader"

  it should "locate upstream containers" in {
    new SemanticDbReader(documentsfoobarbaz).findContainers(
      "com/marimon/semantic/samples/foobarbaz/Bar#"
    ) should contain theSameElementsAs Set(
      // Bar is used in Baz.B
      "com/marimon/semantic/samples/foobarbaz/Baz#b.",
      // ... whic is in Baz
      "com/marimon/semantic/samples/foobarbaz/Baz#",
      // ... which is the return type in foo
      "com/marimon/semantic/samples/foobarbaz/Hello#foo().",
      // ... which is in Hello
      "com/marimon/semantic/samples/foobarbaz/Hello#"
    )
  }

  it should "locate explicit return type of a def" in {
    new SemanticDbReader(documentsreturntypes).findUsage(
      "com/marimon/semantic/samples/returntypes/ReturnTypeInDef#"
    )should contain theSameElementsAs Set(
      "com/marimon/semantic/samples/returntypes/UsageTrait#",
      "com/marimon/semantic/samples/returntypes/UsageTrait#aDef().",
//      "com/marimon/semantic/samples/UsageObject",  // the object container is not reported?
      "com/marimon/semantic/samples/returntypes/UsageObject.aDef().",
      "com/marimon/semantic/samples/returntypes/UsageClass#",
      "com/marimon/semantic/samples/returntypes/UsageClass#aDef().",
    )
  }

  it should "locate explicit type of a val" in {
    new SemanticDbReader(documentsreturntypes).findUsage(
      "com/marimon/semantic/samples/returntypes/ReturnTypeInVal#"
    )should contain theSameElementsAs Set(
      "com/marimon/semantic/samples/returntypes/UsageTrait#",
      "com/marimon/semantic/samples/returntypes/UsageTrait#x.",
//      "com/marimon/semantic/samples/UsageObject",  // the object container is not reported?
      "com/marimon/semantic/samples/returntypes/UsageObject.x.",
      "com/marimon/semantic/samples/returntypes/UsageClass#",
      "com/marimon/semantic/samples/returntypes/UsageClass#x.",
    )
  }

  it should "locate direct usages" in {
    new SemanticDbReader(documentsalphabet).findUsage(
      "com/marimon/semantic/samples/alphabet/KI#"
    )should contain theSameElementsAs Set(
      "com/marimon/semantic/samples/alphabet/DirectUsage.directKi().(ki)",
      "com/marimon/semantic/samples/alphabet/DirectUsage.directKi().",
    )
  }

  it should "locate potential usages (PECS!)" ignore {}
  it should "locate potential return types on a def or type of a val (PECS!)" ignore {}
  it should "locate downstream contained" ignore {}

}
