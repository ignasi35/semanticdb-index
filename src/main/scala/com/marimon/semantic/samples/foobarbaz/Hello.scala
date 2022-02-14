package com.marimon.semantic.samples.foobarbaz

case class Rocket(i:Int, s:String)
case class Bar(i:Int, s:String)
case class Baz(b: Bar, j:Int, t:String)
class Hello {
  def foo(i:Int) = Baz(Bar(1, "a"), 2, "b")
}
