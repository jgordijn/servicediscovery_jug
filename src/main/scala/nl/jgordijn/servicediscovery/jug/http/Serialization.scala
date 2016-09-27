package nl.jgordijn.servicediscovery.jug
package http

trait Serialization extends spray.json.DefaultJsonProtocol {
  implicit val registrationFormat = jsonFormat2(Api.Registration)
  implicit val serviceFormat = jsonFormat3(Service)
}