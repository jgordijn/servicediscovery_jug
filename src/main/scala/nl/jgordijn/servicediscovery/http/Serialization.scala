package nl.jgordijn.servicediscovery
package http

import nl.jgordijn.servicediscovery.http.Api.Registration
import spray.json.RootJsonFormat

trait Serialization extends spray.json.DefaultJsonProtocol {
  implicit val registrationFormat: RootJsonFormat[Registration] = jsonFormat2(Api.Registration)
  implicit val serviceFormat: RootJsonFormat[Service] = jsonFormat3(Service)
}