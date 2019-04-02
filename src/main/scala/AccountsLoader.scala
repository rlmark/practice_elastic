import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.{IO, Resource, Sync}
import cats.instances.vector._
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.sksamuel.elastic4s.ElasticApi.indexInto
import com.sksamuel.elastic4s.bulk.BulkRequest
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.bulk.BulkResponse
import com.sksamuel.elastic4s.http.{bulk => _, _}
import com.sksamuel.elastic4s.indexes.IndexRequest
import contract.Account
import io.circe

import scala.collection.immutable
import scala.concurrent.{Future, Promise}
import scala.io.{BufferedSource, Source}

class AccountsLoader[F[_] : Sync : Functor : Executor](elasticClient: ElasticClient) {
  def load(fileName: String, indexName: String, esType: String): F[Response[BulkResponse]] = {
    val sourceLines: F[Vector[String]] = fileResource(fileName).use { bufferedSource =>
      Sync[F].delay(bufferedSource.getLines().toVector)
    }

    val ioAccounts: F[Vector[ValidatedNel[circe.Error, Map[String, String]]]] =
      sourceLines.map((lines: Vector[String]) => parseLines(lines))

    val successfulAccounts: F[Response[BulkResponse]] =
      ioAccounts.flatMap ((accounts: Vector[ValidatedNel[circe.Error, Map[String, String]]]) =>
        executeClientValidated(accounts, indexName, esType)
      )

    successfulAccounts
  }

  def fileResource(fileName: String): Resource[F, BufferedSource] = {
    Resource.make(Sync[F].delay(Source.fromResource(fileName)))(bufferedSource => Sync[F].delay(bufferedSource.close()))
  }

  def parseLines(lines: Vector[String]): Vector[ValidatedNel[circe.Error, Map[String, String]]] = {
    lines.zipWithIndex.collect { case (line, index) if index % 2 == 1 =>
      Account.fieldDecoder(line)
    }
  }

  // Version without Validated
  def executeClient(accounts: Vector[Map[String, String]], indexName: String, esType: String): F[Response[BulkResponse]] = {
    val bulkRequests: Seq[IndexRequest] = accounts.map(fieldsMap =>
      indexInto(indexName, esType) fields fieldsMap
    )

    elasticClient.execute[BulkRequest, BulkResponse, F] {
      bulk(
        bulkRequests
      )
    }
  }

  // Maybe it's best to work on the Validated result
  def executeClientValidated(
                              accounts: Vector[ValidatedNel[circe.Error, Map[String, String]]],
                              indexName: String,
                              esType: String
                            ): F[Response[BulkResponse]] = {
    // Think about this... should it actually return F[ValidatedNel[circe.Error, Response[BulkResponse]]]
    val requests: immutable.Seq[Vector[IndexRequest]] = accounts.traverse[List, IndexRequest] { nel: ValidatedNel[circe.Error, Map[String, String]] =>
      val formRequests: Validated[NonEmptyList[circe.Error], IndexRequest] = nel.map(fieldsMap => indexInto(indexName, esType) fields fieldsMap)
      formRequests.toList // Sadness... removes the richness of Validated typeclass
    }

    elasticClient.execute[BulkRequest, BulkResponse, F] {
      bulk(
        requests.flatten
      )
    }
  }
}

// Elastic4S defines it's own Functor and Executor instances, so we need to create instances for cats effect IO
object Elastic4STranslation {

  implicit val esFunctor: Functor[IO] = new Functor[IO] {
    override def map[A, B](fa: IO[A])(f: A => B): IO[B] = fa.map(f)
  }

  // Is there a better way to do this?
  implicit val esExecutor: Executor[IO] = new Executor[IO] {
    override def exec(client: HttpClient, request: ElasticRequest): IO[HttpResponse] = {
      val promise = Promise[HttpResponse]()
      val callback: Either[Throwable, HttpResponse] => Unit = {
        case Left(t) => promise.tryFailure(t)
        case Right(r) => promise.trySuccess(r)
      }
      client.send(request, callback)
      val t: Future[HttpResponse] = promise.future

      IO.fromFuture(IO(t))
    }
  }
}

object TestApp extends App {

  import Elastic4STranslation._

  val testClient = ElasticClient(ElasticProperties.apply(s"http://localhost:9200"))
  val loader = new AccountsLoader[IO](testClient)
  val bulkResponseIo: IO[Response[BulkResponse]] = loader.load(
    "accounts.json",
    "exampleindex",
    "_doc")
  println(bulkResponseIo.unsafeRunSync())


 //smaller test so we know client execution works
  val responseIo: IO[Response[BulkResponse]] = loader.executeClientValidated(Vector(Valid(Map("hello" -> "world"))), "exampleindex", "_doc")
  val result: Response[BulkResponse] = responseIo.unsafeRunSync()
  println(result)
}
