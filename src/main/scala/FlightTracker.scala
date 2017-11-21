import java.util.Calendar

import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.github.mauricio.async.db.Connection
import org.json4s.native.Serialization

import scalaj.http.{Http, HttpResponse}
import scala.concurrent.duration._
import scala.concurrent.Await

class FlightTracker(settings: ApplicationSettings) {

    implicit val formats = Serialization.formats(org.json4s.NoTypeHints)

    /**
      * Método principal do FlightTracker, responsável por rodar o processo
      * de monitoramento de preços em background
      */
    def run(): Unit = {
        //rodar para sempre
        while (true) {
            //faz a requisição la no qpx
            val response : TripsSearchResponse = this.sendQpxRequest()

            this.registerTrackEvent(response.trips.requestId)

            //toda a estrutura de cidades, aeroportos, companhias aéreas, etc
            //deve ser salva na base antes dos vôos, pois nos vôos há referências
            //para essas paradas
            this.storeFlightStructure(response.trips.data)

            //salva as opções de vôo disponíveis
            for (flight <- response.trips.tripOption) {
                this.storeFlightOption(response.trips.requestId, flight)
            }

            //registra que essa consulta foi efetivada
            this.updateTrackEvent(response.trips.requestId)

            //dorme o tempo necessário até a próxima consulta
            Thread.sleep(settings.qpx.delayBetweenRequests)
        }
    }

    /**
      * Registra na base de dados uma busca realizada
      * @param requestId Identificador da busca realizada
      */
    private def registerTrackEvent(requestId: String): Unit = {
        val configuration = URLParser.parse(settings.dbConnectionString)
        val connection: Connection = new PostgreSQLConnection(configuration)

        Await.result(connection.connect, Duration.Inf)

        val sql: String = """INSERT INTO request (id, begun_at)
                             VALUES (?, ?)"""

        Await.result(connection.sendPreparedStatement(sql, Array(
            requestId, Calendar.getInstance()
        )), Duration.Inf)

        connection.disconnect
    }

    /**
      * Atualiza a data de finalização de uma busca realizada
      * @param requestId Identificador da busca realizada
      */
    private def updateTrackEvent(requestId: String): Unit = {
        val configuration = URLParser.parse(settings.dbConnectionString)
        val connection: Connection = new PostgreSQLConnection(configuration)

        Await.result(connection.connect, Duration.Inf)

        val sql: String = """UPDATE request
                                SET finished_at = ?
                              WHERE id = ?"""

        Await.result(connection.sendPreparedStatement(sql, Array(
            Calendar.getInstance(), requestId
        )), Duration.Inf)

        connection.disconnect
    }

    /**
      * Insere na base as opções de vôo na base de dados
      * @param requestId Identificador da busca realizada
      * @param flight Opção de vôo
      */
    private def storeFlightOption(requestId: String, flight: TripOption): Unit = {
        val configuration = URLParser.parse(settings.dbConnectionString)
        val connection: Connection = new PostgreSQLConnection(configuration)

        Await.result(connection.connect, Duration.Inf)

        val sql: String = """INSERT INTO trip_option
                               (request_id, trip_id, sale_total, slice, pricing)
                             VALUES
                               (?, ?, ?, ?, ?)"""

        val sliceJson: String = Serialization.write(flight.slice)
        val pricingJson: String = Serialization.write(flight.pricing)

        Await.result(connection.sendPreparedStatement(sql, Array(
            requestId, flight.id, flight.saleTotal, sliceJson, pricingJson
        )), Duration.Inf)

        connection.disconnect
    }

    /**
      * Faz upsert da estrutura do vôos disponíveis
      * @param data Estrutura dos vôos
      */
    private def storeFlightStructure(data: FlightData): Unit = {
        val configuration = URLParser.parse(settings.dbConnectionString)
        val connection: Connection = new PostgreSQLConnection(configuration)

        Await.result(connection.connect, Duration.Inf)

        for (city <- data.city) {
            this.upsertCity(connection, city)
        }

        for (airport <- data.airport) {
            this.upsertAirport(connection, airport)
        }

        for (aircraft <- data.aircraft) {
            this.upsertAircraft(connection, aircraft)
        }

        for (tax <- data.tax) {
            this.upsertTaxKind(connection, tax)
        }

        for (carrier <- data.carrier) {
            this.upsertCompany(connection, carrier)
        }

        connection.disconnect
    }

    private def upsertCity(connection: Connection, city: CityData): Unit = {
        val sql: String = """INSERT INTO city (code, name)
                             VALUES (?, ?)
                             ON CONFLICT DO NOTHING"""

        Await.result(connection.sendPreparedStatement(sql,
            Array(city.code, city.name)
        ), Duration.Inf)
    }

    private def upsertAirport(connection: Connection, airport: AirportData): Unit = {
        val sql: String = """INSERT INTO airport (code, name, city)
                             VALUES (?, ?, ?)
                             ON CONFLICT DO NOTHING"""

        Await.result(connection.sendPreparedStatement(sql,
            Array(airport.code, airport.name, airport.city)
        ), Duration.Inf)
    }

    private def upsertAircraft(connection: Connection, aircraft: AircraftData): Unit = {
        val sql: String = """INSERT INTO aircraft (code, name)
                             VALUES (?, ?)
                             ON CONFLICT DO NOTHING"""

        Await.result(connection.sendPreparedStatement(sql,
            Array(aircraft.code, aircraft.name)
        ), Duration.Inf)
    }

    private def upsertTaxKind(connection: Connection, tax: TaxData): Unit = {
        val sql: String = """INSERT INTO tax_kind (code, name)
                             VALUES (?, ?)
                             ON CONFLICT DO NOTHING"""

        Await.result(connection.sendPreparedStatement(sql,
            Array(tax.id, tax.name)
        ), Duration.Inf)
    }

    private def upsertCompany(connection: Connection, carrier: CarrierData): Unit = {
        val sql: String = """INSERT INTO company (code, name)
                             VALUES (?, ?)
                             ON CONFLICT DO NOTHING"""

        Await.result(connection.sendPreparedStatement(sql,
            Array(carrier.code, carrier.name)
        ), Duration.Inf)
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
