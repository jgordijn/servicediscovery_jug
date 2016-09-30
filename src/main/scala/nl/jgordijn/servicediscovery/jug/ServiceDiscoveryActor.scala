package nl.jgordijn.servicediscovery.jug

import akka.actor.{ Actor, ActorRef, ActorSystem }
import akka.cluster.ddata._
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.util.Timeout

import scala.concurrent.{ ExecutionContext, Future }

object ServiceDiscoveryActor {
  case class Get(name: String)
  case class Register(name: String, host: String, port: Int)
  case class Deregister(name: String, host: String, port: Int)
  case class Result(services: Set[Service])
  case object NoResult
  case object Updated
}

class ServiceDiscoveryActor extends Actor {
  import ServiceDiscoveryActor._

  var service = Set.empty[Service]

  def receive = {
    case Register(s, h, p) ⇒
      service = service + Service(s, h, p)
      sender() ! Updated
    case Deregister(s, h, p) ⇒
      service = service - Service(s, h, p)
      sender() ! Updated
    case Get(name) ⇒
      sender() ! Result(service.filter(_.name == name))
  }
}

