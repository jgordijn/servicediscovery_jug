package nl.jgordijn.servicediscovery.jug.http

trait Serialization extends spray.json.DefaultJsonProtocol {
  implicit val registrationFormat = jsonFormat2(Api.Registration)
}