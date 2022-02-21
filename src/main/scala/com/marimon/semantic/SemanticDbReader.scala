package com.marimon.semantic

import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream
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

class SemanticDbReader(documents: Array[TextDocuments]) {

  private val textDocuments = documents.flatMap(_.documents)

  private def typeName(t:Type): List[String] = {
    t match {
      case Type.Empty => Nil
      case tr:TypeRef =>
        tr.typeArguments.toList match {
          case Nil => List(tr.symbol)
          case l => l.flatMap(typeName)
        }
      case at:AnnotatedType => typeName(at.tpe)
      // untested
      case st:SingleType => List(st.symbol)
      case rt:RepeatedType => typeName(rt.tpe)
      case et:ExistentialType => typeName(et.tpe)
      case bnt:ByNameType => typeName(bnt.tpe)
      case st:StructuralType => typeName(st.tpe)
      case wt:WithType => wt.types.toList.flatMap(typeName)
      case _:ConstantType => Nil
      case tt:ThisType => List(tt.symbol)
      case ut:UniversalType => typeName(ut.tpe)
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
    (1 to 20).map(i => s"local$i").toSet
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

  private val edges: Set[Edge] = textDocuments.flatMap{ doc=>
    doc.symbols.flatMap{ symbol =>
      symbol.signature match {
        case ms: MethodSignature => {
          val methodName = symbol.symbol
          val displayName = symbol.displayName
          // Sometimes the signature has a methodName of `local<number>`. I'm filtering
          // them all. Usually not too relevant. There's also `local27+1` or `local27+2` or ...
          if(ignored.contains(displayName) || ignored.contains(methodName) || methodName.matches("local[0-9+]*"))
            Seq.empty
          else {
            val returnTypeDep: List[(String, String)] = typeName(ms.returnType).map(returnType => (methodName -> returnType))

            val parametersDep: Seq[(String, String)] = ms.parameterLists.flatMap{ scope =>
              val symlinks = scope.symlinks
              symlinks.map(link => (methodName-> link))
            }

            parametersDep :++ returnTypeDep
          }
        }
        case cs: ClassSignature =>
          //  In the case of Hello, cs.declarations contains:
          //    "com/marimon/semantic/samples/Hello#foo()."
          val symlinks = cs.declarations.head.symlinks
          symlinks.map{ symlink => ( symlink -> symlink.split("#"))}
            .collect{
              case (symlink, Array(className, methodName)) if !ignoredAsFullName.contains(methodName) =>
                ( s"$className#" -> symlink)
            }
        case vs: ValueSignature => // method params also enter this branch
          val valueName = symbol.symbol
          if(ignored.exists(valueName.contains)) {
            Seq.empty
          } else {
            typeName(vs.tpe).map(valueType =>
              (valueName -> valueType)
            )
          }
        // case ts: TypeSignature => ???
        case _ => Seq.empty
      }
    }
  }.toSet.map(Edge.apply)

  private var codeGraph: DirectedGraph = DirectedGraph(edges)

  def findUsage(sut: String) :Set[String] = {
    println(s"""Searching usages of $sut in a graph with ${codeGraph.edges.size} edges""")
    codeGraph.dependantsOf(sut)
  }

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
