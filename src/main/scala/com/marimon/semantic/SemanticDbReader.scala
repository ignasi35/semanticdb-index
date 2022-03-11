package com.marimon.semantic

import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream
import scala.collection.mutable
import scala.meta.internal.semanticdb.AnnotatedType
import scala.meta.internal.semanticdb.ByNameType
import scala.meta.internal.semanticdb.ClassSignature
import scala.meta.internal.semanticdb.ConstantType
import scala.meta.internal.semanticdb.ExistentialType
import scala.meta.internal.semanticdb.MethodSignature
import scala.meta.internal.semanticdb.RepeatedType
import scala.meta.internal.semanticdb.SingleType
import scala.meta.internal.semanticdb.StructuralType
import scala.meta.internal.semanticdb.TextDocuments
import scala.meta.internal.semanticdb.ThisType
import scala.meta.internal.semanticdb.Type
import scala.meta.internal.semanticdb.TypeRef
import scala.meta.internal.semanticdb.UniversalType
import scala.meta.internal.semanticdb.ValueSignature
import scala.meta.internal.semanticdb.WithType

class SemanticDbReader(val documents: Array[TextDocuments]) {

  private val textDocuments = documents.flatMap(_.documents)

  private def typeName(t: Type): List[String] = {
    t match {
      case Type.Empty => Nil
      case tr: TypeRef =>
        tr.typeArguments.toList match {
          case Nil => List(tr.symbol)
          case l => l.flatMap(typeName)
        }
      case at: AnnotatedType => typeName(at.tpe)
      // untested
      case st: SingleType => List(st.symbol)
      case rt: RepeatedType => typeName(rt.tpe)
      case et: ExistentialType => typeName(et.tpe)
      case bnt: ByNameType => typeName(bnt.tpe)
      case st: StructuralType => typeName(st.tpe)
      case wt: WithType => wt.types.toList.flatMap(typeName)
      case _: ConstantType => Nil
      case tt: ThisType => List(tt.symbol)
      case ut: UniversalType => typeName(ut.tpe)
      // other return types
      //  IntersectionType(_), SuperType(_, _), UnionType(_)
      case _ => ???
    }
  }

  private val ignored = Set("equals", "hashCode", "toString", "getClass", "notify",
    "notifyAll", "wait", "copy", "clone", "finalize", "productArity", "productElement",
    "writeReplace", "unapply", "canEqual", "productPrefix", "productIterator", "productElementName",
    "isEqualTo",
    "apply", "<init>", "`<init>`"
  ) ++ (
    (0 to 20).map(i => s"local$i").toSet
    ) ++ (
    (1 to 25).flatMap(i => Set(
      s"copy$$default$$$i",
      s"`<init>$$default$$$i`",
      s"<init>$$default$$$i",
      s"apply$$default$$$i",
      s"create$$default$$$i",
    )
    )
    )

  private val ignoredAsFullName = ignored.map(_ + "().")

  private lazy val sourcesReference = mutable.Map.empty[String, Path]

  private lazy val edges: Set[Edge] = textDocuments.flatMap { doc =>
    val dependenciesInDoc: Seq[(String, String)] = doc.symbols.flatMap { symbol =>
      symbol.signature match {
        case ms: MethodSignature => {
          val methodName = symbol.symbol
          val displayName = symbol.displayName
          if (ignored.contains(displayName) || ignored.contains(methodName) || methodName.matches("local[0-9+]*")) {
            // Sometimes the signature has a methodName of `local<number>` and other reserved names. I'm filtering
            // them all. Usually not too relevant. There's also `local27+1` or `local27+2` or ...
            Seq.empty
          } else {
            val returnTypeDep: List[(String, String)] =
              typeName(ms.returnType)
                .map(returnType => (methodName -> returnType))

            val parametersDep: Seq[(String, String)] =
              ms
                .parameterLists
                .flatMap { scope =>
                  val symlinks = scope.symlinks
                  symlinks.map(link => (methodName -> link))
                }
            // A methods depends on the types of the arguments and the type of the return value
            parametersDep :++ returnTypeDep
          }
        }
        case cs: ClassSignature =>
          //  In the case of `class Hello`, cs.declarations contains:
          //    ".../samples/Hello#foo()."
          val symlinks = cs.declarations.head.symlinks
          symlinks.headOption.toSeq.flatMap { head =>
            // When it's a class
            if (head.contains(("#"))) {
              symlinks.map { symlink => (symlink -> symlink.split("#")) }
                .collect {
                  case (symlink, Array(className, methodName)) if !ignoredAsFullName.contains(methodName) =>
                    (s"$className#" -> symlink)
                }
            } else {
              // When it's an object
              symlinks.map { symlink => (symlink -> symlink.split("\\.", 2)) }
                .collect {
                  case (symlink, Array(className, methodName)) if !ignoredAsFullName.contains(methodName) =>
                    (s"$className." -> symlink)
                }
            }
          }
        case vs: ValueSignature => // method params also enter this branch
          val valueName = symbol.symbol
          if (ignored.exists(valueName.contains)) {
            Seq.empty
          } else {
            typeName(vs.tpe).map(valueType =>
              (valueName -> valueType)
            )
          }
        // case ts: TypeSignature => ???
        case _ => Seq.empty
      }
    }.filter(!_._2.startsWith("scala/")) // I don't care about "scala/Nothing", "scala/Unit", Int,...
    dependenciesInDoc
      .map(_._1)
      .toSet
      .foreach(k => sourcesReference.update(k, Paths.get(doc.uri)))
    dependenciesInDoc
  }.toSet.map(Edge.apply)

  /** A graph of how type signatures (types, fields and methods) depend on each other */
  private lazy val signatureGraph: DirectedGraph = DirectedGraph(edges)
  private lazy val reverseSignatureGraph = signatureGraph.reverse

  // -----------------------------------------------------------------------------------------------------
  // -----------------------------------------------------------------------------------------------------
  // -----------------------------------------------------------------------------------------------------

  /**
   * Builds a list of methods, types and fields whose signature depends on `sut`
   */
  def findSignatureDependencies(sut: String): Set[String] = {
    signatureGraph.dependantsOf(sut)
  }

  /**
   * Builds a list of methods, types and fields whose signature depends on `sut` with extra metadata
   */
  def findSignatureDependenciesWithMetadata(sut: String): (Set[String], Set[Path]) = {
    val usages = findSignatureDependencies(sut)
    val sourcePaths = usages.map(sourcesReference)
    (usages, sourcePaths)
  }

  /**
   * @return the complete signaturesGraph to the `sut` provided
   */
  def findSignatureGraphTo(sut: String): Set[Edge] = Ops.findSignatureGraph(sut, signatureGraph)

  /**
   * @return a collapsed signaturesGraph (only contains types) to the `sut` provided
   */
  def findTypeGraphTo(sut: String): Set[Edge] = Ops.findTypeGraph(sut, signatureGraph)
  def findTypeGraphFrom(sut: String): Set[Edge] = Ops.findTypeGraph(sut, reverseSignatureGraph).map(_.reverse)

}

object SemanticDbReader {

  import scala.jdk.CollectionConverters._

  private def load(paths: stream.Stream[Path]): Array[TextDocuments] = {
    val files =
      paths
        .iterator()
        .asScala
        .map(_.toFile)
        .filter(_.getName.endsWith(".semanticdb"))
        .toArray
    files.map(file => {
      TextDocuments.parseFrom(new FileInputStream(file))
    }
    )
  }

  def loadRecursively(folder:String): Array[TextDocuments] = load(Files.walk(Paths.get(folder)))

  def load(folder:String): Array[TextDocuments] = load(Files.list(Paths.get(folder)))

}


private[semantic] object Ops {
  def findSignatureGraph(sut: String, graph: DirectedGraph): Set[Edge] = graph.graphTo(sut)

  /**
   * @return a collapsed signaturesGraph (only contains types) to the `sut` provided
   */
  private[semantic] def findTypeGraph(sut: String, graph: DirectedGraph): Set[Edge] = {
    def isTypeName(sut: String): Boolean = sut.endsWith("#")

    def isType(sut: Node): Boolean = isTypeName(sut.name)

    require(isTypeName(sut))

    val fullGraph = graph.graphTo(sut)
    val indexedGraph: Map[Node, Set[Edge]] = fullGraph.groupBy(_.from)


    val typeEdges: Set[Edge] = fullGraph.filter(edge => isType(edge.from))

    /** Given a starting node point and a subgraph, returns a projection with
     * the edges from the starting node to the closest types. */
    def collapse(start: Node, intermediate: Node, indexGraph: Map[Node, Set[Edge]]): Set[Edge] = {
      indexGraph.get(intermediate).toList.flatten match {
        case Nil => Set.empty[Edge]
        case steps =>
          steps.foldLeft(Set.empty[Edge]) { case (acc, step) =>
            if (isType(step.to)) acc + Edge(start, step.to)
            else acc ++ collapse(start, step.to, indexGraph)
          }
      }
    }

    typeEdges.map(_.from).map { start =>
      collapse(start, start, indexedGraph)
    }.foldLeft(Set.empty[Edge]) { case (acc, more) => acc ++ more }

  }
}
