package com.marimonclos.semantic.samples.alphabet

trait A
trait BA extends A { val unused: String }
trait CA extends A { val unused: String }

trait I
trait JI extends I
trait KI extends I

trait Alphabet extends KI with CA { val unused: String }

object DirectUsage {
  def directCa(ca: CA) = ???
  def directKi(ki: KI) = ???
}
object PotentialUsage {
  def producerExtends(a: A) = ???
}
object PotentialReturn {
  def consumerSuper(): Alphabet = ???
}

class OtherClass() {
  def usage(i: I): String = ???
}
class MainClass() {
  def mainUsage() = new OtherClass().usage(null)
}
