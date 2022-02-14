package com.marimon.semantic

import java.io.File
import java.io.FileInputStream
import java.io.FilenameFilter
import scala.meta.internal.semanticdb.AnnotatedType
import scala.meta.internal.semanticdb.ClassSignature
import scala.meta.internal.semanticdb.MethodSignature
import scala.meta.internal.semanticdb.SymbolInformation
import scala.meta.internal.semanticdb.TextDocuments
import scala.meta.internal.semanticdb.Type
import scala.meta.internal.semanticdb.Type.NonEmpty
import scala.meta.internal.semanticdb.TypeRef
import scala.meta.internal.semanticdb.TypeSignature
import scala.meta.internal.semanticdb.ValueSignature

class SemanticDbReader(documents: Array[TextDocuments]) {
  private val textDocuments = documents.flatMap(_.documents)

  private def typeName(t:Type): Option[String] = {
    t match {
      case Type.Empty => None
      case tr:TypeRef => Some(tr.symbol)
      case at:AnnotatedType => typeName(at.tpe)
      // other return types
    }
  }

  private val ignored = Set("equals", "hashCode", "toString", "getClass", "notify",
    "notifyAll", "wait", "copy", "clone", "finalize", "productArity", "productElement",
    "writeReplace", "unapply", "canEqual", "productPrefix", "productIterator", "productElementName",
    "apply", "copy$default$1", "copy$default$2", "copy$default$3", "copy$default$4", "copy$default$5",
    "local1", "local2", "local3", "local4", "local5",
    "<init>", "`<init>`"
  )
  private val ignoredAsFullName = ignored.map(_ + "().")

  private val edges: Set[Edge] = textDocuments.flatMap{ doc=>
    doc.symbols.flatMap{ symbol =>
      symbol.signature match {
        case ms: MethodSignature => {
          if(ignored.contains(symbol.displayName))
            Seq.empty
          else {
            val methodName = symbol.symbol
            val returnTypeDep: Option[(String, String)] = typeName(ms.returnType).map(returnType => (methodName -> returnType))

            val parametersDep: Seq[(String, String)] = ms.parameterLists.flatMap{ scope =>
              val symlinks = scope.symlinks
              symlinks.map(link => (methodName-> link))
            }

            if(returnTypeDep.isDefined)
              parametersDep :+ returnTypeDep.get
            else
              parametersDep
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
            val valueType = typeName(vs.tpe).get
            Seq((valueName -> valueType))
          }
        //        case ts: TypeSignature => ???
        case _ => Seq.empty
      }
    }
  }.toSet.map(Edge.apply)

  private val codeGraph =  DirectedGraph(edges)

  def findContainers(sut: String) :Set[String] = {
    codeGraph.dependantsOf(sut)
  }
  def findUsage(sut: String) :Set[String] = {
    println(codeGraph.edges)
    codeGraph.dependantsOf(sut)
  }
}

object MainSemanticPoc extends App {

  def loadDocuments(folder:String): Array[TextDocuments] = {
    val files: Array[File] = new File(folder).listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {
        name.endsWith(".semanticdb")
      }
    }
    )
    files.map(file => {
      println(s"Parsing ${file.getAbsolutePath}")
      TextDocuments.parseFrom(new FileInputStream(file))
    }
    )
  }

    loadDocuments(new File("./target").getAbsolutePath).take(1)(0).documents.foreach {
    doc =>
      doc.symbols
        .filter {
          symbol =>
            symbol.kind.isMethod && symbol.symbol.contains("foo()")
        }.map {
        symbol =>
          val m = symbol.signature match {
            case ms: MethodSignature => ms
          }
          (symbol, m)
      }.foreach {
        case (symbol, methodSignature) =>
          println(
            s"""
               |symbol (method): ${symbol.symbol}
               |return type: ${methodSignature.returnType}
               |""".stripMargin
          )
        //|${methodSignature.returnType.asNonEmpty.get.asInstanceOf[TypeRef].symbol}
      }
  }
  //  textDocument.documents.foreach(_.symbols.foreach(println))
  //  textDocument.documents.foreach(_.occurrences.foreach(println))
  //  println(" ............  ")

}
