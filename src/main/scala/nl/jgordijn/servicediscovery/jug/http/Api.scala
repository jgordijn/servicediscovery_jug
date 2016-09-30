package nl.jgordijn.servicediscovery.jug
package http

import akka.actor.ActorRef
import akka.cluster.ddata.ORSetKey
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Api {
  case class Registration(host: String, port: Int)
}

class Api(serviceDiscoveryActor: ActorRef)(implicit executionContext: ExecutionContext) extends Serialization {
  import Api._
  implicit val timeout: Timeout = 3.seconds
  val DataKey = ORSetKey[Service]("data")

  val route: Route = pathPrefix("services") {
    path(Segment) { name ⇒
      get {
        onSuccess(serviceDiscoveryActor ? ServiceDiscoveryActor.Get(name)) {
          case ServiceDiscoveryActor.Result(s) ⇒ complete(s)
          case ServiceDiscoveryActor.NoResult  ⇒ complete(StatusCodes.NotFound, "Unknown service")
        }
      } ~
        post {
          entity(as[Registration]) { r ⇒
            onSuccess(serviceDiscoveryActor ? ServiceDiscoveryActor.Register(name, r.host, r.port)) { _ ⇒
              complete(StatusCodes.NoContent)
            }
          }
        } ~
        delete {
          entity(as[Registration]) { r ⇒
            onSuccess(serviceDiscoveryActor ? ServiceDiscoveryActor.Deregister(name, r.host, r.port)) { _ ⇒
              complete(StatusCodes.NoContent)
            }
          }
        }
    }
  }
}
