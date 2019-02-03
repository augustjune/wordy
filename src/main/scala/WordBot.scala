import com.bot4s.telegram.api.declarative.{Commands, Messages}
import com.bot4s.telegram.api.{Polling, RequestHandler, TelegramBot}
import com.bot4s.telegram.clients.SttpClient
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

class WordBot(token: String) extends TelegramBot with Messages with Commands with Polling {
  LoggerConfig.factory = PrintLoggerFactory()
  // set log level, e.g. to TRACE
  LoggerConfig.level = LogLevel.TRACE

  implicit val backend = OkHttpFutureBackend()
  val client: RequestHandler = new SttpClient(token)

  onMessage { msg =>
    reply(msg.text.getOrElse("You sent me no words :("))(msg)
  }
}
