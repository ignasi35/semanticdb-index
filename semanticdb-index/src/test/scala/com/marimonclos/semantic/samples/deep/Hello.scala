package com.marimonclos.semantic.samples.deep

case class ID(id: String)

case class Address(street: String, number: Int)
case class PersonInfo(id: ID, name: String, address: List[Address])

trait User {
  val person: Option[PersonInfo]
}
trait Teacher {
  val person: Option[PersonInfo]
}

case class Classroom(id: ID, teacher: Teacher, users: Seq[User])
