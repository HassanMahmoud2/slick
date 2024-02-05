package com.example.part3
import com.example.part3.MyExecutionContext.exec
import play.api.libs.json.Json
import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object MyExecutionContext {
  val executor = Executors.newFixedThreadPool(4)
  implicit val exec: ExecutionContext =  ExecutionContext.fromExecutorService(executor)
}

object main {
  val persuitOfHappiness = Movie(1L, "persuit_of_happiness", LocalDate.now(), 60)
  val jawan = Movie(12L, "jawan", LocalDate.now(), 60)

  val willSmith = Actor(1L, "will_smith")
  val tomCruise = Actor(2L, "tom_cruise")
  val shahroukhan = Actor(3L, "shahroukhan")
  val providers = List(
    StreamingProviderMapping(1L, 12L, StreamingService.Netflix),
    StreamingProviderMapping(2L, 1L, StreamingService.Prime)
  )
  val jawanLocations = MovieLocations(1L, 12L, List("India", "London", "Italy"))
  val jawanProperties = MovieProperties(1L, 12L, Map(
    "Genre" -> "Sci-Fi",
    "Features" -> "fire works"
  ))

  val willSmithDetails = ActorDetails(1L, 1L, Json.parse(
    """
      |{
      |"born": 1963,
      |"type": "super star"
      |}
      |""".stripMargin
  ))
  def demoInsertMovie(): Unit = {
    val queryDescription = SlickTables.movieTable += persuitOfHappiness
    val futureId: Future[Int] = Connection.db.run(queryDescription)
    futureId.onComplete {
      case Success(newMoviewId) => println(s"Query was successful, new id is $newMoviewId")
      case Failure(ex) => println(s"Query failed, reason $ex")
    }
    Thread.sleep(3000)
  }

  def demoReadAllMovies(): Unit = {
    val futureMovies: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result)
    futureMovies.onComplete {
      case Success(movies) => println(s"The movies are: ${movies.mkString(", ")}")
      case Failure(ex) => println(s"The read function failed due to $ex")
    }
    Thread.sleep(3000)
  }

  def demoUpdate(): Unit = {
    val queryDescriptor = SlickTables.movieTable.filter(_.id === 3L).update(persuitOfHappiness.copy(id = 3L, name = "Ghosted"))
    val futureId: Future[Int] = Connection.db.run(queryDescriptor)
    futureId.onComplete {
      case Success(movieId) => println(s"The updated movie is: ${movieId}")
      case Failure(ex) => println(s"The update function failed due to $ex")
    }
    Thread.sleep(3000)
  }

  def demoDelete(): Unit = {
    val futureId: Future[Int] = Connection.db.run(SlickTables.movieTable.filter(_.id === 7L).delete)
    futureId.onComplete {
      case Success(movieId) => println(s"The movie deleted is : ${movieId}")
      case Failure(ex) => println(s"The read function failed due to $ex")
    }
    Thread.sleep(3000)
  }

  def readMoviesByPlainQuery(): Future[Vector[Movie]] = {
    implicit val getResultMovie: GetResult[Movie] =
      GetResult(positionedResult => Movie(
        positionedResult.<<,
        positionedResult.<<,
        LocalDate.parse(positionedResult.nextString()),
        positionedResult.<<
      ))
    val query = sql"""select * from movies."Movie"""".as[Movie]
    Connection.db.run(query)
  }

  /*********************************/

  def demoInsertActor(): Unit = {
    val queryDescription = SlickTables.actorTable ++= Seq(willSmith, tomCruise)
    val futureId = Connection.db.run(queryDescription)
    futureId.onComplete {
      case Success(_) => println("Query of inserting actor was successful")
      case Failure(ex) => println(s"Query failed, reason $ex")
    }
    Thread.sleep(3000)
  }

  def multipleQueriesSingleTransaction(): Future[Unit] = {
    val insertMovie = SlickTables.movieTable += jawan
    val insertActor = SlickTables.actorTable += shahroukhan
    val finalQuery = DBIO.seq(insertMovie, insertActor)
    Connection.db.run(finalQuery.transactionally)
  }

  def findAllActorsByMovie(movieId: Long): Future[Seq[Actor]] = {
    val joinQuery = SlickTables.movieActorMapping
      .filter(_.movieId === movieId)
      .join(SlickTables.actorTable)
      .on(_.actorId === _.id)
      .map(_._2)
    Connection.db.run(joinQuery.result)
  }

  /**************************/
  def addStreamingProviders(): Unit = {
    val insertQuery = SlickTables.streamingProviderMapping ++= providers
    Connection.db.run(insertQuery).onComplete {
      case Success(_) => println("inserted streaming providers successfully")
      case Failure(ex) => println(s"inserting streaming providers error: $ex")
    }
    Thread.sleep(3000)
  }
  def findProvidersForMovie(movieId: Long): Unit = {
    val findQuery = SlickTables.streamingProviderMapping.filter(_.movieId === movieId)
    Connection.db.run(findQuery.result).onComplete {
      case Success(movies) => println(s"the providers for $movieId are: ${movies.map(_.provider)}")
      case Failure(ex) => println(s"findProvidersForMovie error: $ex")
    }
    Thread.sleep(3000)
  }

  def insertLocationsJawan(): Unit = {
    val query = SpecialTables.movieLocationsTable += jawanLocations
    Connection.db.run(query).onComplete {
      case Success(_) => println("inserting locations query is done successfully")
      case Failure(ex) => println(s"insertLocationsJawan error: $ex")
    }
    Thread.sleep(3000)
  }

  def insertPropertiesJawan(): Unit = {
    val query = SpecialTables.moviePropertiesTable += jawanProperties
    Connection.db.run(query).onComplete {
      case Success(_) => println("inserting properties query is done successfully")
      case Failure(ex) => println(s"insertPropertiesJawan error: $ex")
    }
    Thread.sleep(3000)
  }

  def insertWillSmithDetails(): Unit = {
    val query = SpecialTables.actorDetailsTable += willSmithDetails
    Connection.db.run(query).onComplete {
      case Success(_) => println("inserting details for will smith query is done successfully")
      case Failure(ex) => println(s"insertWillSmithDetails error: $ex")
    }
    Thread.sleep(3000)
  }
  def main(args: Array[String]): Unit = {
    insertWillSmithDetails
  }
}
