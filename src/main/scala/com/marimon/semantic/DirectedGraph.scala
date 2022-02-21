package com.marimon.semantic

import scala.annotation.tailrec

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

  def dependantsOf(sut: String): Set[String] = {
    @tailrec
    def dep0(newSuts:Set[String], currentDependants: Set[String]) : Set[String] = {
      val dependantsOfNewSuts = edges.filter(edge => newSuts.contains(edge.to.name)).map(_.from.name)
      val newFounds = dependantsOfNewSuts.diff(currentDependants)
      newFounds.size match {
        case 0 => currentDependants ++ newFounds
        case _ => dep0(newFounds, currentDependants ++ newFounds)
      }
    }
    dep0(Set(sut),  Set.empty)

  }
}
