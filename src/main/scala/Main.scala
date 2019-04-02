import java.io.{File, FileOutputStream}

import cats.data.OptionT
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import cats.effect.IO
import com.sksamuel.elastic4s.http._

import scala.io.{BufferedSource, Source}


object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    program[IO]
  }

  def program[F[_]: ConcurrentEffect: Timer]: F[ExitCode] = {
    makeElasticClient("localhost", 9200).use(esClient =>
      makeHttpServer(esClient)
    )
  }

  def makeElasticClient[F[_]: Sync](host: String, port: Int): Resource[F, ElasticClient] =
  Resource.make(
    Sync[F].delay(ElasticClient(ElasticProperties(s"http://$host:$port")))
  )(
    client => Sync[F].delay(client.close)
  )



  def makeHttpServer[F[_]: ConcurrentEffect : Timer](elasticClient: ElasticClient): F[ExitCode] = {
    val routes: HttpRoutes[F] = new ElasticRoutes(elasticClient).service
    val httpApp = Router("/" -> routes).orNotFound
    val optServer = BlazeServerBuilder[F]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .last

    OptionT(optServer).getOrElse(ExitCode.Error)
  }
}
