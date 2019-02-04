import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout

import concurrent.duration._
import com.bot4s.telegram.api.declarative.{Commands, Messages}
import com.bot4s.telegram.api.{Polling, RequestHandler, TelegramBot}
import com.bot4s.telegram.clients.SttpClient
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}
import thesaurus._

import scala.concurrent.Future

class WordBot(token: String) extends TelegramBot with Messages with Commands with Polling {
  LoggerConfig.factory = PrintLoggerFactory()
  // set log level, e.g. to TRACE
  LoggerConfig.level = LogLevel.TRACE

  implicit val backend = OkHttpFutureBackend()
  val client: RequestHandler = new SttpClient(token)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val thesaurus = system.actorOf(ThesaurusApi.props)
  implicit val timeout = Timeout(3 seconds)

  import akka.pattern.ask

  onCommand("/syn") { msg =>
    val request = msg.text.flatMap{ s =>
      val spaceIndex = s.indexOf(" ")
      if (spaceIndex > 0) Some(s.substring(spaceIndex + 1))
      else None
    }

    val responce = request.map { s =>
      (thesaurus ? WordLookup(s)).mapTo[Either[String, ThesaurusWord]].map(_.map(word => word.synonyms.mkString(", ")))
    }.getOrElse(Future.successful(Left("No word provided")))

    responce.foreach {
      case Left(x) => reply(s"An error occurred: $x")(msg)
      case Right(x) => reply(s"Synonyms: $x")(msg)
    }
  }
}
