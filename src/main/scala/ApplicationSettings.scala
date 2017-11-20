import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

object ApplicationSettings {
    implicit val jsonDefaultFormats = DefaultFormats

    def fromJson(json: String): ApplicationSettings = JsonMethods.parse(json).extract[ApplicationSettings]
}

//root
case class ApplicationSettings(
    qpx: QpxSettings,
    dbConnectionString: String
)

//root.qpx
case class QpxSettings(
    endpoint: String,
    delayBetweenRequests: Int,
    flight: FlightSettings
)

//root.qpx.flight
case class FlightSettings(
    departureAirport: String,
    arrivalAirport: String,
    departureDate: String,
    returnDate: String
)