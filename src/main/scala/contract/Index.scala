package contract

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class Index(name: String)

object Index {
  implicit val decoder: Decoder[Index] = deriveDecoder[Index]
  implicit val encoder: Encoder[Index] = deriveEncoder[Index]
}
