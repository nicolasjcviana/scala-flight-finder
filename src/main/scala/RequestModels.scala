import org.json4s.native.Serialization

object QpxRequest {
    implicit val formats = Serialization.formats(org.json4s.NoTypeHints)

    def toJson(request: QpxRequest): String = Serialization.write(request)
}

//root
case class QpxRequest(
    request: QpxRequestInfo
)

//root.request
case class QpxRequestInfo(
    passengers: RequestPassengersInfo,
    slice: Array[RequestSliceInfo]
)

//root.request.passengers
case class RequestPassengersInfo(
    adultCount: Int
)

//root.request.slice[n]
case class RequestSliceInfo(
    origin: String,
    destination: String,
    date: String
)