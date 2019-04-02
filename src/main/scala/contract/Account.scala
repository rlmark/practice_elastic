package contract

import cats.data.ValidatedNel
import io.circe
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._


/*
* {"account_number":1,"balance":39225,"firstname":"Amber","lastname":"Duke","age":32,"gender":"M",
* "address":"880 Holmes Lane","employer":"Pyrami","email":"amberduke@pyrami.com","city":"Brogan","state":"IL"}
* */
case class Account(
                    account_number: Int,
                    balance: Int,
                    firstname: String,
                    lastname: String,
                    age: Int,
                    gender: String,
                    address: String,
                    employer: String,
                    email: String,
                    city: String,
                    state: String
                  )

object Account {
  implicit val encoder: Encoder[Account] = deriveEncoder[Account]
  implicit val decoder: Decoder[Account] = deriveDecoder[Account]
  val fieldDecoder: String => ValidatedNel[circe.Error, Map[String, String]]
    = (string: String) => io.circe.parser.decodeAccumulating[Map[String, String]](string)
}
