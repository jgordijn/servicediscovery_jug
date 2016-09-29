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

class ReplicatorInterface(system: ActorSystem) {
  val replicator = DistributedData(system).replicator
  implicit val node = Cluster(system)
  import akka.pattern.ask

  def get[A <: ReplicatedData](key: Key[A], consistency: ReadConsistency)(implicit timeout: Timeout, executionContext: ExecutionContext): Future[GetResponse[A]] = {
    (replicator ? Replicator.Get(key, consistency)).mapTo[GetResponse[A]]
  }
  def update[A <: ReplicatedData](key: Key[A], initial: A, writeConsistency: WriteConsistency)(modify: A ⇒ A)(implicit timeout: Timeout, executionContext: ExecutionContext): Future[UpdateResponse[A]] = {
    (replicator ? Replicator.Update(key, initial, writeConsistency)(modify)).mapTo[UpdateResponse[A]]
  }
}

class ServiceDiscovery(system: ActorSystem) {
  val replicatorInterface = new ReplicatorInterface(system)
  implicit val node = Cluster(system)

  val DataKey = ORSetKey[Service]("data")

  def register(name: String, host: String, port: Int)(implicit timeout: Timeout, executionContext: ExecutionContext): Future[UpdateResponse[ORSet[Service]]] = replicatorInterface.update(DataKey, ORSet.empty[Service], Replicator.WriteLocal) { set ⇒
    val service = Service(name, host, port)
    if (set.contains(service)) set else set + service
  }

  def deregister(name: String, host: String, port: Int)(implicit timeout: Timeout, executionContext: ExecutionContext): Future[UpdateResponse[ORSet[Service]]] = replicatorInterface.update(DataKey, ORSet.empty[Service], Replicator.WriteLocal) { set ⇒
    val service = Service(name, host, port)
    if (!set.contains(service)) set else set - service
  }

  def get(name: String)(implicit timeout: Timeout, executionContext: ExecutionContext): Future[Set[Service]] = replicatorInterface.get(DataKey, Replicator.readLocal).map {
    case g @ Replicator.GetSuccess(key, _) ⇒
      val services = g.get(DataKey).elements.filter(_.name == name)
      services
    case g @ Replicator.NotFound(key, _) ⇒
      Set.empty
    case g @ Replicator.GetFailure(key, _) ⇒
      // Can only happen when consistency level > local
      Set.empty
  }
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

  }
}

