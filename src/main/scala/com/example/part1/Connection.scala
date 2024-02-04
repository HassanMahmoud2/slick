package com.example.part1
import slick.jdbc.PostgresProfile.api._

object Connection {
  //this function will look for postgres element in application.conf
  val db = Database.forConfig("postgres")

}
