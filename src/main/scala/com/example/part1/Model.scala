package com.example.part1

import java.time.LocalDate

case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)

object SlickTables {
  import slick.jdbc.PostgresProfile.api._
  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {
    def id = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Length(20))

    def releaseDate = column[LocalDate]("release_date")

    def lengthInMin = column[Int]("length_in_min", O.Length(3))

    //mapping function to a case class
    override def * = (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)
  }
    //API entry point
    lazy val movieTable = TableQuery[MovieTable]
}
