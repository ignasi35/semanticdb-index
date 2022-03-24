package com.marimonclos.semantic

import scala.annotation.tailrec

case class Node(name: String)
// directed Edge
case class Edge(from: Node, to: Node){
  def reverse = Edge(to, from)
}
object Edge {
  def apply(from: String, to: String): Edge = Edge(Node(from), Node(to))
  def apply(edge: (String,String)): Edge = Edge(Node(edge._1), Node(edge._2))
}

// I don't care about unconnected nodes. Anything that's not in an edge can be ignored ATM.
case class DirectedGraph(edges: Set[Edge]){
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

  /**
   * A collection of edges dependant on `sut`
   */
  def graphTo(sut: String): Set[Edge] = {
    val d = dependantsOf(sut)
    edges.filter(e => d.contains(e.from.name))
  }

  def reverse: DirectedGraph = DirectedGraph(edges.map( _.reverse))
}
