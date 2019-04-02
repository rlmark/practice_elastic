import cats.effect.Sync
import com.sksamuel.elastic4s.http.{ElasticClient, Response}
import contract.Index
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import Index._
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.indexes.CreateIndexRequest
import com.sksamuel.elastic4s.mappings.FieldType._
import io.circe.generic.auto._
import io.circe.syntax._


class ElasticRoutes[F[_]: Sync ](elasticClient: ElasticClient) extends Http4sDsl[F] {

//  def request(index: String): CreateIndexRequest = createIndex(index).mappings (
//    mapping("test").fields (
//      textField("name")
//    )
//  )

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "test" => Ok("Hello World")
  }

}
