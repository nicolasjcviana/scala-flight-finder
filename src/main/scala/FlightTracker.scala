import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.github.mauricio.async.db.{QueryResult, Connection}
import scalaj.http.{Http, HttpResponse}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class FlightTracker(settings: ApplicationSettings) {

    def run(): Unit = {
        //rodar para sempre
        /*while (true) {
            //faz a requisição la no qpx
            val response : TripsSearchResponse = this.sendQpxRequest()

            //toda a estrutura de cidades, aeroportos, companhias aéreas, etc
            //deve ser salva na base antes dos vôos, pois nos vôos há referências
            //para essas paradas
            this.storeFlightStructure(response.trips.data)

            //salva as opções de vôo disponíveis
            for (flight <- response.trips.tripOption) {
                this.storeFlightOption(flight)
            }
        }*/

        val configuration = URLParser.parse(settings.dbConnectionString)
        val connection: Connection = new PostgreSQLConnection(configuration)

        Await.result(connection.connect, Duration.Inf)

        val future: Future[QueryResult] = connection.sendQuery("SELECT 'Guilherme' as nene, 20 as age")

        val result = Await.result(future, Duration.Inf)

        println(result.rows.get(0)("nene"))
        println(result.rows.get(0)("age"))

        connection.disconnect
    }

    private def storeFlightOption(flight: TripOption): Unit = {
        //TODO: Implementar isso (salvar no pg)
    }

    private def storeFlightStructure(data: FlightData): Unit = {
        //TODO: Implementar isso (salvar no pg)
    }

    private def sendQpxRequest(): TripsSearchResponse = {
        val response: HttpResponse[String] = Http(settings.qpx.endpoint)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .postData(this.buildQpxPostData())
            .timeout(60 * 1000, 60 * 1000)
            .asString

        TripsSearchResponse.fromJson(response.body)
    }

    private def buildQpxPostData(): String = {
        val slice = new Array[RequestSliceInfo](2)
        //vôo de ida
        slice(0) = RequestSliceInfo(settings.qpx.flight.departureAirport, settings.qpx.flight.arrivalAirport, settings.qpx.flight.departureDate)
        //vôo de volta
        slice(1) = RequestSliceInfo(settings.qpx.flight.arrivalAirport, settings.qpx.flight.departureAirport, settings.qpx.flight.returnDate)

        val request: QpxRequest = QpxRequest(
            QpxRequestInfo(
                RequestPassengersInfo(1),
                slice
            )
        )

        QpxRequest.toJson(request)
    }

}
