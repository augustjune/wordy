import com.typesafe.config.ConfigFactory

object Run extends App {
  val config = ConfigFactory.parseResources("credentials.conf")
  val token = config.getString("telegram.token")
  val bot = new WordBot(token)

  bot.run()
  println("Successfully started")
}
