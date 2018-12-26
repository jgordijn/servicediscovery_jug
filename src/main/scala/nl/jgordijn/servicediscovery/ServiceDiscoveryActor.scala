package nl.jgordijn.servicediscovery

import akka.actor.{ Actor, ActorRef }
import akka.cluster.Cluster
import akka.cluster.ddata._

object ServiceDiscoveryActor {

  case class Service(name: String, host: String, port: Int)

  case class Get(name: String)
  case class Register(name: String, host: String, port: Int)
  case class Deregister(name: String, host: String, port: Int)
  case class Result(services: Set[Service])
  case object NoResult
  case object Updated

}

class ServiceDiscoveryActor extends Actor {

  import ServiceDiscoveryActor._

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)

  val ServicesKey: ORSetKey[Service] = ORSetKey[Service]("services")

  def receive = {
    case Get(name) ⇒
      replicator ! Replicator.Get(ServicesKey, Replicator.ReadLocal, Some((name, sender())))
    case result @ Replicator.GetSuccess(key, Some((name, sndr: ActorRef))) ⇒
      sndr ! Result(result.get(ServicesKey).elements.filter(_.name == name))
    case Replicator.NotFound(_, Some((_, sndr: ActorRef))) ⇒
      // Will only happen when no services are registered
      sndr ! Result(Set.empty)

    case Register(s, h, p) ⇒
      replicator ! Replicator.Update(ServicesKey, ORSet.empty[Service], Replicator.WriteLocal, Some(sender())) { set ⇒
        set + Service(s, h, p)
      }
    case Replicator.UpdateSuccess(key, Some(sndr: ActorRef)) ⇒
      sndr ! Updated
    case Replicator.ModifyFailure(ServicesKey, errorMessage, throwable, requestContext) ⇒
    // happens when the update function throws an exception
    case Replicator.UpdateTimeout(ServicesKey, requestContext) ⇒
    // happens when write consistence > local and nodes don't respond
  }

}

