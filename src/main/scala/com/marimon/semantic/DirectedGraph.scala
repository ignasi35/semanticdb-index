package com.marimon.semantic

case class Node(name: String)
case class Edge(from: Node, to: Node) // directed Edge
object Edge {
  def apply(from: String, to: String): Edge = Edge(Node(from), Node(to))
  def apply(edge: (String,String)): Edge = Edge(Node(edge._1), Node(edge._2))
}

// I don't care about unconnected nodes. Anything that's not in an edge can be ignored ATM.
case class DirectedGraph(edges: Set[Edge]){
  // TODO: reimplement using memoization, colorings, etc. (make it efficient)
  def dependenciesOf(sut: String): Set[String] = {
    val directDependencies: Set[String] = edges.filter(_.from.name == sut).map(_.to.name)
    directDependencies ++ directDependencies.flatMap(dependenciesOf)
  }
  // TODO: reimplement using memoization, colorings, etc. (make it efficient)
  def dependantsOf(sut: String): Set[String] = {
    val directDependants: Set[String] = edges.filter(_.to.name == sut).map(_.from.name)
    directDependants ++ directDependants.flatMap(dependantsOf)
  }
}
