package lambda

import scala.util.{Try, Success, Failure}

trait LambdaReductor {
	import LambdaREPL._

  def reduce(input: String): Try[String] =
    parseInput(parser.parse, input) { expr =>
      val bound = bind(expr)
      if (bind.messages.isEmpty)
        Success(pretty(eval(bound)))
      else {
        val res = (for (m <- bind.messages) yield m.pos.longString + m.msg).mkString
        bind.messages.clear()
        Failure(new Exception(res))
      }
    }

  def parseInput[T](p: String => parser.ParseResult[T], input: String)(eval: T => Try[String]): Try[String] = {
    import parser.{ Success => Succ, NoSuccess }
    p(input) match {
      case Succ(res, _) => eval(res)
      case NoSuccess(err, _) => Failure(new Exception(err))
    }
  }
}