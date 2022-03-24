package com.marimonclos.semantic.samples.generified

case class ID(id:String)
case class Age(in:Int)
case class ZipCode(code: String)
case class Address(street:String, number: Int, zip: Option[ZipCode])

case class PersonInfo(name: String, age:Age, address: List[Address])

case class User(id: ID, person: Option[PersonInfo])
