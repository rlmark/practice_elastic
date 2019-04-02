package contract

import org.scalatest.{FlatSpec, Matchers}
import io.circe.syntax._
import io.circe.parser.decode

class AccountSpec extends FlatSpec with Matchers {
  import Account._

  val testAccount = Account(
    account_number = 1, balance = 39225, firstname ="Amber", lastname = "Duke", age =32, gender = "M",
    address = "880 Holmes Lane", employer = "Pyrami", email = "amberduke@pyrami.com", city ="Brogan", state ="IL"
  )
  val jsonString = """{"account_number":1,"balance":39225,"firstname":"Amber","lastname":"Duke","age":32,"gender":"M","address":"880 Holmes Lane","employer":"Pyrami","email":"amberduke@pyrami.com","city":"Brogan","state":"IL"}"""

  "Account" should "encode to Json" in {
    testAccount.asJson.noSpaces shouldBe jsonString
  }
  it should "decode to Account" in {
    decode[Account](jsonString) shouldBe Right(testAccount)
  }
}
