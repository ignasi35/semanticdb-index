package com.marimon.semantic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class GraphSpec extends AnyFlatSpec with Matchers {
  behavior of "Graph"

  it should "provide dependants of node A" in {
    val edges = Set(
      ("A" -> "B"),
      ("A" -> "C"),
      ("A" -> "D"),
      ("B" -> "E"),
      ("C" -> "E"),
      ("D" -> "F"),
      ("J" -> "K"),
      ("K" -> "L"),
    ).map(d => Edge(d))

    DirectedGraph(edges).dependantsOf("F") must contain theSameElementsAs Set("A", "D")
  }

  it should "provide dependants of node when there's a loop" in {
    val edges = Set(
      ("A" -> "B"),
      ("B" -> "C"),
      ("C" -> "D"),
      ("D" -> "B"),
      ("B" -> "H"),
    ).map(d => Edge(d))

    DirectedGraph(edges).dependantsOf("H") must contain theSameElementsAs Set("A", "B", "C", "D")
  }

  it should "be reversible" in {
    val edges: Set[Edge] = Set(
      ("A" -> "B"),
      ("B" -> "C"),
      ("C" -> "D"),
      ("B" -> "H"),
      ("D" -> "H"),
    ).map(d => Edge(d))


    val reversedEdges: Set[Edge] = Set(
      ("B" -> "A"),
      ("C" -> "B"),
      ("D" -> "C"),
      ("H" -> "B"),
      ("H" -> "D"),
    ).map(d => Edge(d))

    DirectedGraph(edges).reverse must be(DirectedGraph(reversedEdges))
  }

}
