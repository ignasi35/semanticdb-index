package com.marimonclos.experiments

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

object Programmatically extends App {

  // see https://docs.scala-lang.org/overviews/reflection/symbols-trees-types.html#typechecking-with-toolboxes
  // https://stackoverflow.com/questions/57964879/how-to-compile-and-run-scala-code-programmatically

  val toolbox = currentMirror.mkToolBox()
  val params = """Array("Apple", "Banana", "Orange")"""
  val source =
    s"""
       |private object HelloWorld {
       |  println("hellooooo")
       |  def main(args: Array[String]): Unit = {
       |    println(args.toList)
       |  }
       |}
       |
       |HelloWorld.main($params)
       |""".stripMargin
  val tree: toolbox.u.Tree = toolbox.parse(source)

  object traverser extends toolbox.u.Traverser {
    override def traverse(tree: toolbox.u.Tree): Unit = tree match {

//        case app @ toolbox.u.Super(tree, typeName) =>
//          println(s"type: $typeName")
//          super.traverse(tree)
//        case app @ toolbox.u.Apply(fun, args) =>
//          println(app.getClass)
//          super.traverse(fun)
//          super.traverseTrees(args)
//        case x =>
//          println(x.getClass)
//          super.traverse(tree)

      case sut @ toolbox.u.Alternative(trees)           => ???
      case sut @ toolbox.u.Annotated(tree1, tree2)      => ???
      case sut @ toolbox.u.AppliedTypeTree(tree, trees) => ???
      case sut @ toolbox.u.Apply(fun, args) =>
        traverse(fun)
        traverseTrees(args)
      case sut @ toolbox.u.Assign(tree1, tree2) => ???
      case sut @ toolbox.u.Bind(name, tree)     => ???
      case sut @ toolbox.u.Block(stats, expr) =>
        println("Block{")
        traverseTrees(stats)
        traverse(expr)
        println("}")
      case sut @ toolbox.u.CaseDef(tree1, tree2, tree3) => ???
      case sut @ toolbox.u.ClassDef(modifiers, typeName, typeDefs, template) =>
        ???
      case sut @ toolbox.u.CompoundTypeTree(template) => ???
      case sut @ toolbox.u.DefDef(
            modifiers,
            name,
            typeParams,
            arguments,
            tree_tpt,
            tree_rhs
          ) =>
        println(s"DefDef($name) ")
        traverseModifiers(modifiers)
//          traverseTrees(modifiers)
      case sut @ toolbox.u.ExistentialTypeTree(tree, memberDefs) => ???
      case sut @ toolbox.u.Function(valDefs, tree)               => ???
      case sut @ toolbox.u.Ident(name) =>
        println(s"Ident($name)")
      case sut @ toolbox.u.If(tree1, tree2, tree3)            => ???
      case sut @ toolbox.u.Import(tree, importSelectors)      => ???
      case sut @ toolbox.u.ImportSelector(name1, x, name2, j) => ???
      case sut @ toolbox.u.LabelDef(termName, idents, tree)   => ???
      case sut @ toolbox.u.Literal(constant) =>
        println(s"Literal($constant)")
      case sut @ toolbox.u.Match(tree, caseDefs) => ???
      case sut @ toolbox.u.Modifiers(flags, privateWithin, annotations) =>
        println(s" ----------  Modifiers($flags, $privateWithin, trees)")
        traverseTrees(annotations)
      case sut @ toolbox.u.ModuleDef(modifiers, moduleName, implementation) =>
        println(s"Module($modifiers, $moduleName, trees)")
        traverseModifiers(modifiers)
        traverse(implementation)
      case sut @ toolbox.u.NamedArg(tree1, tree2)     => ???
      case sut @ toolbox.u.New(tree)                  => ???
      case sut @ toolbox.u.PackageDef(refTree, trees) => ???
      case sut @ toolbox.u.RefTree(qualifier, name) =>
        println(s"RefTree($qualifier, $name)")
        traverse(qualifier)
      case sut @ toolbox.u.Return(tree)                       => ???
      case sut @ toolbox.u.Select(tree, name)                 => ???
      case sut @ toolbox.u.SelectFromTypeTree(tree, typeName) => ???
      case sut @ toolbox.u.SingletonTypeTree(tree)            => ???
      case sut @ toolbox.u.Star(tree)                         => ???
      case sut @ toolbox.u.Super(tree, typeName)              => ???
      case sut @ toolbox.u.Template(parentTypes, selfType, body) =>
        traverseTrees(parentTypes)
        traverseTrees(body)
      case sut @ toolbox.u.This(typeName)                               => ???
      case sut @ toolbox.u.Throw(tree)                                  => ???
      case sut @ toolbox.u.Try(tree1, caseDefs, tree2)                  => ???
      case sut @ toolbox.u.TypeApply(tree, trees)                       => ???
      case sut @ toolbox.u.TypeBoundsTree(tree1, tree2)                 => ???
      case sut @ toolbox.u.TypeDef(modifiers, typeName, typeDefs, tree) => ???
      case sut @ toolbox.u.Typed(tree1, tree2)                          => ???
      case sut @ toolbox.u.UnApply(tree, trees)                         => ???
      case sut @ toolbox.u.ValDef(modifiers, termName, tree1, tree2)    => ???
      //        case sut @ toolbox.u.TypeTree): Boolean

    }
  }

  traverser.traverse(tree)

  tree match {
    case block: toolbox.u.Block => println(block)
  }

  val binary = toolbox.compile(tree)
  binary()

}
