package nl.jgordijn.servicediscovery

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.settings.ServerSettings
import nl.jgordijn.servicediscovery.http.Api

import scala.concurrent.ExecutionContext


object Main extends App {
  implicit val system = ActorSystem("servicediscovery")
  import system.dispatcher
  val services = system.actorOf(Props(new ServiceDiscoveryActor), "services")
  val hostname = "localhost"
  val port = system.settings.config.getInt("port")

  new WebServer(services).startServer(hostname, port, ServerSettings(system), system)
}


class WebServer(services: ActorRef)(implicit executionContext: ExecutionContext) extends HttpApp {

  override val route = new Api(services).route
}