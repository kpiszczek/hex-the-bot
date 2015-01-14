package hex

import scala.collection.immutable.SortedSet
import java.util.Random

trait RandomResponse[A] {
  val random = new Random(System.currentTimeMillis)
  def response(source: SortedSet[(Double, A)]): Option[A] =  {
    val total = sum(source)
    val interval = random.nextDouble * total
    source.foldLeft((None: Option[A], total)){ 
      case ((a: Some[A], c), (w, _)) ⇒ (a, c - w)
      case ((None, c), (_, text)) if c <= 0.0 ⇒ (Option(text), c)
      case ((result, currentTotal), (weight, text)) if interval >= (currentTotal - weight) ⇒ (Option(text), 0.0)
      case ((result, currentTotal), (weight, text)) ⇒ (result, currentTotal - weight)
    }._1
  }

  def sum(source: SortedSet[(Double, A)]) = source.foldLeft(0.0){
    case (acc, (a, _)) => acc + a
  }
}