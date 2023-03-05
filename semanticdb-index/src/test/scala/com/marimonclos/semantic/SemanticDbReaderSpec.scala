package com.marimonclos.semantic

import java.io.File
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.meta.internal.semanticdb.TextDocuments

class SemanticDbReaderSpec extends AnyFlatSpec with Matchers {

  private val baseFolder = new File(
    ".",
    "semanticdb-index/target/scala-2.13/test-meta/META-INF/semanticdb/semanticdb-index/src/test/scala/com/marimonclos/semantic/samples"
  ).getAbsolutePath

  private val foobarbaz = baseFolder + "/foobarbaz"
  private val returntypes = baseFolder + "/returntypes"
  private val alphabet = baseFolder + "/alphabet"
  private val deep = baseFolder + "/deep"
  private val generified = baseFolder + "/generified"

  private val documentsfoobarbaz: Array[TextDocuments] =
    SemanticDbReader.load(foobarbaz)
  private val documentsreturntypes: Array[TextDocuments] =
    SemanticDbReader.load(returntypes)
  private val documentsalphabet: Array[TextDocuments] =
    SemanticDbReader.load(alphabet)
  private val documentsdeep: Array[TextDocuments] = SemanticDbReader.load(deep)
  private val documentsGenerified: Array[TextDocuments] =
    SemanticDbReader.load(generified)

  behavior of "SemanticDbReader.findSignatureDependencies"

  it should "locate upstream containers" in {
    new SemanticDbReader(documentsfoobarbaz).findSignatureDependencies(
      "com/marimonclos/semantic/samples/foobarbaz/Bar#"
    ) should contain theSameElementsAs Set(
      // Bar is used in Baz.B
      "com/marimonclos/semantic/samples/foobarbaz/Baz#b.",
      // ... which is in Baz
      "com/marimonclos/semantic/samples/foobarbaz/Baz#",
      // ... which is the return type in foo
      "com/marimonclos/semantic/samples/foobarbaz/Hello#foo().",
      // ... which is in Hello
      "com/marimonclos/semantic/samples/foobarbaz/Hello#"
    )
  }

  it should "locate explicit return type of a def" in {
    new SemanticDbReader(documentsreturntypes).findSignatureDependencies(
      "com/marimonclos/semantic/samples/returntypes/ReturnTypeInDef#"
    ) should contain theSameElementsAs Set(
      "com/marimonclos/semantic/samples/returntypes/UsageTrait#",
      "com/marimonclos/semantic/samples/returntypes/UsageTrait#aDef().",
      "com/marimonclos/semantic/samples/returntypes/UsageObject.",
      "com/marimonclos/semantic/samples/returntypes/UsageObject.aDef().",
      "com/marimonclos/semantic/samples/returntypes/UsageClass#",
      "com/marimonclos/semantic/samples/returntypes/UsageClass#aDef()."
    )
  }

  it should "locate explicit type of a val" in {
    new SemanticDbReader(documentsreturntypes).findSignatureDependencies(
      "com/marimonclos/semantic/samples/returntypes/ReturnTypeInVal#"
    ) should contain theSameElementsAs Set(
      "com/marimonclos/semantic/samples/returntypes/UsageTrait#x.",
      "com/marimonclos/semantic/samples/returntypes/UsageTrait#",
      "com/marimonclos/semantic/samples/returntypes/UsageObject.x.",
      "com/marimonclos/semantic/samples/returntypes/UsageObject.",
      "com/marimonclos/semantic/samples/returntypes/UsageClass#x.",
      "com/marimonclos/semantic/samples/returntypes/UsageClass#"
    )
  }

  it should "locate direct usages" in {
    new SemanticDbReader(documentsalphabet).findSignatureDependencies(
      "com/marimonclos/semantic/samples/alphabet/KI#"
    ) should contain theSameElementsAs Set(
      "com/marimonclos/semantic/samples/alphabet/DirectUsage.directKi().(ki)",
      "com/marimonclos/semantic/samples/alphabet/DirectUsage.directKi().",
      "com/marimonclos/semantic/samples/alphabet/DirectUsage."
    )
  }

  // TODO: I'm not entirely sure what to detect here. Should I also detect usages of sub-classes of A?
  it should "locate potential usages (PECS!) - trait&subclass" ignore {
    new SemanticDbReader(documentsalphabet).findSignatureDependencies(
      "com/marimonclos/semantic/samples/alphabet/A#"
    ) should contain theSameElementsAs Set(
      "com/marimonclos/semantic/samples/alphabet/PotentialUsage.producerExtends().(a)",
      "com/marimonclos/semantic/samples/alphabet/PotentialUsage.producerExtends().",
      "com/marimonclos/semantic/samples/alphabet/PotentialUsage.",
      "com/marimonclos/semantic/samples/alphabet/DirectUsage.directCa().(ca)",
      "com/marimonclos/semantic/samples/alphabet/DirectUsage.directCa().",
      "com/marimonclos/semantic/samples/alphabet/DirectUsage."
    )
  }

  // TODO: implement
  it should "locate potential return types on a def or type of a val (PECS!)" ignore {}

  it should "follow the dependencies on generic types (Option[Bar], List[Foo],...)" in {
    new SemanticDbReader(documentsGenerified).findSignatureDependencies(
      "com/marimonclos/semantic/samples/generified/ZipCode#"
    ) should contain theSameElementsAs Set(
      "com/marimonclos/semantic/samples/generified/Address#",
      "com/marimonclos/semantic/samples/generified/Address#zip.",
      "com/marimonclos/semantic/samples/generified/PersonInfo#",
      "com/marimonclos/semantic/samples/generified/PersonInfo#address.",
      "com/marimonclos/semantic/samples/generified/User#",
      "com/marimonclos/semantic/samples/generified/User#person."
    )
  }

  // TODO: re-assess this idea. I think I need a different graph of invocations, pattern matchings and other cases.
  // If I want to detect when some code in a class X invokes a method on object Y, as in:
  //
  //  class X(){
  //    val y = Y.doStuff()
  //  }
  //
  // I need to improve the semanticDb reader. There is a dependency between X and Y but it's hidden on that static
  // invocation.
  it should "ignore invocations and instantiations" ignore {
    new SemanticDbReader(documentsalphabet).findSignatureDependencies(
      "com/marimonclos/semantic/samples/alphabet/I#"
    ) should contain theSameElementsAs Set(
      "com/marimonclos/semantic/samples/alphabet/OtherClass#usage().(i)",
      "com/marimonclos/semantic/samples/alphabet/OtherClass#usage().",
      "com/marimonclos/semantic/samples/alphabet/OtherClass#"
    )
  }

  // -------------------------------------------------------------------------------------

  behavior of "SemanticDbReader.findDownstreamContained"

  // TODO: implement
  it should "locate downstream contained" ignore {}

  // -------------------------------------------------------------------------------------
  behavior of "SemanticDbReader.findGraphTo"
  it should "provide the whole graph of usages" in {
    new SemanticDbReader(documentsalphabet).findSignatureGraphTo(
      "com/marimonclos/semantic/samples/alphabet/I#"
    ) should contain theSameElementsAs Set(
      Edge(
        from = Node(
          "com/marimonclos/semantic/samples/alphabet/OtherClass#usage().(i)"
        ),
        to = Node("com/marimonclos/semantic/samples/alphabet/I#")
      ),
      Edge(
        from =
          Node("com/marimonclos/semantic/samples/alphabet/OtherClass#usage()."),
        to = Node(
          "com/marimonclos/semantic/samples/alphabet/OtherClass#usage().(i)"
        )
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/alphabet/OtherClass#"),
        to =
          Node("com/marimonclos/semantic/samples/alphabet/OtherClass#usage().")
      )
    )
  }

  // -------------------------------------------------------------------------------------
  behavior of "SemanticDbReader.findSimplifiedGraphTo"

  it should "provide the simplified graph to a SUT (collapse methods and fields)) " in {
    new SemanticDbReader(documentsalphabet).findTypeGraphTo(
      "com/marimonclos/semantic/samples/alphabet/I#"
    ) should contain theSameElementsAs Set(
      Edge(
        from = Node("com/marimonclos/semantic/samples/alphabet/OtherClass#"),
        to = Node("com/marimonclos/semantic/samples/alphabet/I#")
      )
    )
  }

  it should "provide the simplified graph to a SUT (collapse methods and fields) with multiple levels) " in {
    new SemanticDbReader(documentsdeep).findTypeGraphTo(
      "com/marimonclos/semantic/samples/deep/ID#"
    ) should contain theSameElementsAs Set(
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/Classroom#"),
        to = Node("com/marimonclos/semantic/samples/deep/ID#")
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/Classroom#"),
        to = Node("com/marimonclos/semantic/samples/deep/Teacher#")
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/Classroom#"),
        to = Node("com/marimonclos/semantic/samples/deep/User#")
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/PersonInfo#"),
        to = Node("com/marimonclos/semantic/samples/deep/ID#")
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/Teacher#"),
        to = Node("com/marimonclos/semantic/samples/deep/PersonInfo#")
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/User#"),
        to = Node("com/marimonclos/semantic/samples/deep/PersonInfo#")
      )
    )
  }

  // -------------------------------------------------------------------------------------
  it should "provide the graph of Types from a SUT (anything needed to build that SUT)" in {
    new SemanticDbReader(documentsdeep).findTypeGraphFrom(
      "com/marimonclos/semantic/samples/deep/User#"
    ) should contain theSameElementsAs Set(
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/User#"),
        to = Node("com/marimonclos/semantic/samples/deep/PersonInfo#")
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/PersonInfo#"),
        to = Node("com/marimonclos/semantic/samples/deep/ID#")
      ),
      Edge(
        from = Node("com/marimonclos/semantic/samples/deep/PersonInfo#"),
        to = Node("com/marimonclos/semantic/samples/deep/Address#")
      )
    )
  }

  // -------------------------------------------------------------------------------------
  behavior of "SemanticDbReader.findDependenciesWithMetadata"

  // TODO: implement
  it should "find dependencies with the source metadata (originating file)" ignore {}

}
