import org.scalatest._
import org.scalacheck._

import hex.RandomResponse

import scala.collection.immutable.SortedSet

/**
 * Created by kuba on 27/12/14.
 */
class RandomResponseSpec extends FlatSpec with Matchers {
  object Responder extends RandomResponse[String]
  "RandomResponse" should "return exact value if there is only one" in {
    Responder.response(SortedSet(1.0 → "test")) should contain ("test")
  }

  "RandomResponse" should "result in None when empty source is provided" in {
    Responder.response(SortedSet[(Double, String)]()) should be (None)
  }
}
