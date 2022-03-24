package com.marimonclos.semantic.samples.returntypes

trait ReturnTypeInDef
trait ReturnTypeInVal

trait UsageTrait {
  def aDef(): ReturnTypeInDef
  val x: ReturnTypeInVal
}
object UsageObject {
  def aDef(): ReturnTypeInDef = ???
  val x: ReturnTypeInVal = ???
}
class UsageClass {
  def aDef(): ReturnTypeInDef = ???
  val x: ReturnTypeInVal = ???
}
