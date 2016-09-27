package nl.jgordijn.servicediscovery.jug

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import http.Api

object Main extends App {
  implicit val system = ActorSystem("servicediscovery_jug")
  implicit val materializer = ActorMaterializer()

  //val services = system.actorOf(Services.props, "services")

  val routes = new Api().route
  val hostname = "localhost"
  val port = system.settings.config.getInt("port")

  val bindingFuture = Http().bindAndHandle(routes, hostname, port)
  import system.dispatcher
  bindingFuture.onSuccess {
    case binding ⇒ println(s"Bound: ${binding.localAddress.getPort}")
  }
}
