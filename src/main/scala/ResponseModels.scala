import java.time.ZonedDateTime

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

object TripsSearchResponse {
    implicit val jsonDefaultFormats = DefaultFormats

    def fromJson(json: String): TripsSearchResponse = {
        return JsonMethods.parse(json).extract[TripsSearchResponse]
    }
}

//main response object
case class TripsSearchResponse(trips: TripOptions)

//root
case class TripOptions(
    requestId: String,
    data: FlightData,
    tripOption: Array[TripOption]
)

//root.data
case class FlightData(
    airport: Array[AirportData],
    city: Array[CityData],
    aircraft: Array[AircraftData],
    tax: Array[TaxData],
    carrier: Array[CarrierData]
)

//root.data.airport[n]
case class AirportData(
    code: String,
    city: String,
    name: String
)

//root.data.city[n]
case class CityData(
    code: String,
    name: String
)

//root.data.aircraft[n]
case class AircraftData(
    code: String,
    name: String
)

//root.data.tax[n]
case class TaxData(
    id: String,
    name: String
)

//root.data.carrier[n]
case class CarrierData(
    code: String,
    name: String
)

//root.tripOption[n]
case class TripOption(
    id: String,
    saleTotal: String,
    slice: Array[SliceInfo],
    pricing: Array[PricingInfo]
)

//root.tripOption[n].slice[n]
case class SliceInfo(
    duration: Int,
    segment: Array[SegmentInfo]
)

//root.tripOption[n].slice[n].segment[n]
case class SegmentInfo(
    duration: Int,
    flight: SegmentFlightInfo,
    cabin: String,
    bookingCode: String,
    bookingCodeCount: Int,
    marriedSegmentGroup: String,
    leg: Array[LegInfo],
    connectionDuration: Int
)

//root.tripOption[n].slice[n].segment[n].flight
case class SegmentFlightInfo(
    carrier: String,
    number: String
)

//root.tripOption[n].slice[n].segment[n].leg[n]
case class LegInfo(
    aircraft: String,
    arrivalTime: ZonedDateTime,
    departureTime: ZonedDateTime,
    origin: String,
    destination: String,
    originTerminal: String,
    destinationTerminal: String,
    duration: Int,
    mileage: Int,
    meal: String
)

//root.tripOption[n].pricing[n]
case class PricingInfo(
    saleFareTotal: String,
    saleTaxTotal: String,
    saleTotal: String,
    tax: Array[TaxInfo],
    fareCalculation: String,
    refundable: Boolean
)

//root.tripOption[n].pricing[n].tax[n]
case class TaxInfo(
    id: String,
    chargeType: String,
    code: String,
    country: String,
    salePrice: String
)