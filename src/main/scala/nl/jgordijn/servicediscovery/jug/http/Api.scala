package nl.jgordijn.servicediscovery.jug.http

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Api {
  case class Registration(host: String, port: Int)
}

class Api extends Serialization {
  import Api._

  val route: Route = pathPrefix("service") {
    get {
      complete("OK")
    } ~
      path(Segments) { serviceName ⇒
        post {
          entity(as[Registration]) { r ⇒
            complete(r)
          }
        } ~
          delete {
            complete("OK")
          }
      }
  }

}