import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.github.mauricio.async.db.{QueryResult, Connection}
import scalaj.http.{Http, HttpResponse}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class FlightTracker(settings: ApplicationSettings) {

    /**
      * Método principal do FlightTracker, responsável por rodar o processo
      * de monitoramento de preços em background
      */
    def run(): Unit = {
        //rodar para sempre
        while (true) {
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

            //dorme o tempo necessário até a próxima consulta
            Thread.sleep(settings.qpx.delayBetweenRequests)
        }
    }

    /**
      * Insere na base as opções de vôo na base de dados
      * @param flight Opção de vôo
      */
    private def storeFlightOption(flight: TripOption): Unit = {
        //TODO: Implementar isso (salvar no pg)
    }

    /**
      * Faz upsert da estrutura do vôos disponíveis
      * @param data Estrutura dos vôos
      */
    private def storeFlightStructure(data: FlightData): Unit = {
        //TODO: Implementar isso (salvar no pg)
    }

    /**
      * Envia uma requisição para a QPX API
      * @return Objeto do tipo TripsSearchResponse contendo a resposta da QPX API
      */
    private def sendQpxRequest(): TripsSearchResponse = {
        val response: HttpResponse[String] = Http(settings.qpx.endpoint)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .postData(this.buildQpxPostData())
            .timeout(60 * 1000, 60 * 1000)
            .asString

        TripsSearchResponse.fromJson(response.body)
    }

    /**
      * Monta um JSON para ser usado como corpo da requisição que será enviada para a QPX API
      * @return String contendo um JSON
      */
    private def buildQpxPostData(): String = {
        val slice = new Array[RequestSliceInfo](2)
        //vôo de ida
        slice(0) = RequestSliceInfo(
            settings.qpx.flight.departureAirport,
            settings.qpx.flight.arrivalAirport,
            settings.qpx.flight.departureDate)

        //vôo de volta
        slice(1) = RequestSliceInfo(
            settings.qpx.flight.arrivalAirport,
            settings.qpx.flight.departureAirport,
            settings.qpx.flight.returnDate)

        //monta a request usando as respectivas case classes
        val request: QpxRequest = QpxRequest(
            QpxRequestInfo(
                //modelo solicitando passagem para uma pessoa apenas
                RequestPassengersInfo(1),
                //array de pedaços do vôo desejado
                slice
            )
        )

        //usa o método estático do objeto QpxRequest para converter a case class em JSON
        QpxRequest.toJson(request)
    }

}
