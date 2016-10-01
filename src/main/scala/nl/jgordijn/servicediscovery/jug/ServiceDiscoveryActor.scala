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
  case object Updated
}

class ServiceDiscoveryActor extends Actor {
  import ServiceDiscoveryActor._

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)

  val ServicesKey: ORSetKey[Service] = ORSetKey[Service]("services")

  def receive = {
    case Register(s, h, p) ⇒
      replicator ! Replicator.Update(ServicesKey, ORSet.empty[Service], Replicator.WriteLocal, Some(sender())) { set ⇒
        set + Service(s, h, p)
      }
    case Deregister(s, h, p) ⇒
      replicator ! Replicator.Update(ServicesKey, ORSet.empty[Service], Replicator.WriteLocal, Some(sender())) { set ⇒
        set - Service(s, h, p)
      }
    case Replicator.UpdateSuccess(key, Some(sndr: ActorRef)) ⇒
      sndr ! Updated
    case e: Replicator.ModifyFailure[ORSet[Service]] =>
      // happens when the update function throws an exception
    case e: Replicator.UpdateTimeout[ORSet[Service]] =>
      // happens when write consistence > local and nodes don't respond

    case Get(name) ⇒
      replicator ! Replicator.Get(ServicesKey, Replicator.ReadLocal, Some((name, sender())))
    case result @ Replicator.GetSuccess(key, Some((name, sndr: ActorRef))) ⇒
      sndr ! Result(result.get(ServicesKey).elements.filter(_.name == name))
    case Replicator.NotFound(_, Some((_, sndr: ActorRef))) ⇒
      // Will only happen when no services are registered
      sndr ! Result(Set.empty)
  }
}

