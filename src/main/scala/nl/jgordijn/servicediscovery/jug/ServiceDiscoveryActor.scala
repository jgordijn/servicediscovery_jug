package nl.jgordijn.servicediscovery.jug

import akka.actor.Actor
import akka.cluster.ddata.DistributedData
import akka.cluster.Cluster
import akka.cluster.ddata.ORSetKey
import akka.cluster.ddata.Replicator
import akka.cluster.ddata.ORSet
import akka.actor.ActorRef

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

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)
  val DataKey = ORSetKey[Service]("data")

  def receive = {
    case Register(s, h, p) ⇒
      replicator ! Replicator.Update(DataKey, ORSet.empty[Service], Replicator.writeLocal, Some(sender())) { set ⇒
        val service = Service(s, h, p)
        if (set.contains(service)) set else set + service
      }
    case Deregister(s, h, p) ⇒
      replicator ! Replicator.Update(DataKey, ORSet.empty[Service], Replicator.writeLocal, Some(sender())) { set ⇒
        val service = Service(s, h, p)
        if (!set.contains(service)) set else set - service
      }
    case Get(name) ⇒
      replicator ! Replicator.Get(DataKey, Replicator.readLocal, Some((sender(), name)))
    case g @ Replicator.GetSuccess(key, Some((sndr: ActorRef, serviceName))) ⇒
      val services = g.get(DataKey).elements.filter(_.name == serviceName)
      sndr ! Result(services)
    case g @ Replicator.NotFound(key, Some((sndr: ActorRef, serviceName))) ⇒
      sndr ! NoResult
    case g @ Replicator.GetFailure(key, Some((sndr: ActorRef, serviceName))) ⇒
      // Can only happen when consistency level > local
      sndr ! NoResult
    case Replicator.UpdateSuccess(key, Some(sndr: ActorRef)) ⇒
      sndr ! Updated

    case x ⇒ println(s">>>>>>>> $x")
  }
}

