package com.marimon.semantic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class GraphSpec extends AnyFlatSpec with Matchers {
  behavior of "Graph"

  it should "provide dependencies of node A" in {
    val edges = Set(
      ("A" -> "B"),
      ("A" -> "C"),
      ("A" -> "D"),
      ("J" -> "K"),
    ).map(d => Edge(d))

    DirectedGraph(edges).dependenciesOf("A") must contain theSameElementsAs Set("B", "C", "D")
  }
  it should "provide deep dependencies of node A" in {
    val edges = Set(
      ("A" -> "B"),
      ("A" -> "C"),
      ("A" -> "D"),
      ("B" -> "E"),
      ("C" -> "E"),
      ("D" -> "E"),
      ("J" -> "K"),
      ("K" -> "L"),
    ).map(d => Edge(d))

    DirectedGraph(edges).dependenciesOf("A") must contain theSameElementsAs Set("B", "C", "D", "E" )
  }

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

  it should "provide dependants of node  when there's a loop" in {
    val edges = Set(
      ("A" -> "B"),
      ("B" -> "C"),
      ("C" -> "D"),
      ("D" -> "B"),
      ("B" -> "H"),
    ).map(d => Edge(d))

    DirectedGraph(edges).dependantsOf("H") must contain theSameElementsAs Set("A", "B", "C", "D")
  }

}
