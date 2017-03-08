package nl.jgordijn.servicediscovery
package http

import akka.actor.ActorRef
import akka.cluster.ddata.ORSetKey
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Api {
  case class Registration(host: String, port: Int)
}

class Api(serviceDiscoveryActor: ActorRef)(implicit executionContext: ExecutionContext) extends CirceSupport {
  import Api._
  implicit val timeout: Timeout = 3.seconds

  val route: Route = pathPrefix("services") {
    path(Segment) { name ⇒
      get {
        onSuccess(serviceDiscoveryActor ? ServiceDiscoveryActor.Get(name)) {
          case ServiceDiscoveryActor.Result(s) ⇒ complete(s)
        }
      } ~
        post {
          entity(as[Registration]) { r ⇒
            onSuccess(serviceDiscoveryActor ? ServiceDiscoveryActor.Register(name, r.host, r.port)) { _ ⇒
              complete(StatusCodes.NoContent)
            }
          }
        }
    }
  }
}
