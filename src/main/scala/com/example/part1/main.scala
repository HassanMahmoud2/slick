package com.example.part1
import com.example.part1.MyExecutionContext.exec
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

  def main(args: Array[String]): Unit = {
    demoInsertMovie()
    demoReadAllMovies()
    demoUpdate()
    demoReadAllMovies()
    demoDelete()
    demoReadAllMovies()
  }
}
