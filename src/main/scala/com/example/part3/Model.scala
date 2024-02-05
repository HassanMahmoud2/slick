package com.example.part3

import java.time.LocalDate

case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
case class Actor(id: Long, name: String)
case class MovieActorMapping(id: Long, movieId: Long, actorId: Long)
case class StreamingProviderMapping(id: Long, movieId: Long, provider: StreamingService.Provider)

object StreamingService extends Enumeration {
  type Provider = Value
  val Netflix = Value("Netflix")
  val Disney = Value("Disney")
  val Prime = Value("Prime")
}

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
  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") {
    def id = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Length(20))

    //mapping function to a case class
    override def * = (id, name) <> (Actor.tupled, Actor.unapply)
  }
    //API entry point
    lazy val actorTable = TableQuery[ActorTable]

  class MovieActorMappingTable(tag: Tag) extends Table[MovieActorMapping](tag, Some("movies"), "MovieActorMapping") {
    def id = column[Long]("movie_actor_id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie_id")
    def actorId = column[Long]("actor_id")

    //mapping function to a case class
    override def * = (id, movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }
  //API entry point
  lazy val movieActorMapping = TableQuery[MovieActorMappingTable]

  class StreamingProviderMappingTable(tag: Tag) extends Table[StreamingProviderMapping](tag, Some("movies"), "StreamingProviderMapping") {
    implicit val providerMapping = MappedColumnType.base[StreamingService.Provider, String] (
      provider => provider.toString,
      string => StreamingService.withName(string)
    )
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie_id")
    def provider = column[StreamingService.Provider]("streaming_provider")

    //mapping function to a case class
    override def * = (id, movieId, provider) <> (StreamingProviderMapping.tupled, StreamingProviderMapping.unapply)
  }
  //API entry point
  lazy val streamingProviderMapping = TableQuery[StreamingProviderMappingTable]

  val tables = List(movieTable,actorTable,movieActorMapping,streamingProviderMapping)
  val ddl = tables.map(_.schema).reduce(_++_)
}
