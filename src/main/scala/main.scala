import scalaj.http.{Http, HttpResponse}

object Main {
    def main(args: Array[String]): Unit = {
        val response: HttpResponse[String] = Http("https://www.googleapis.com/qpxExpress/v1/trips/search?key=AIzaSyAtsyWv_klFnBusLbCZMtX4uDW-QjRoXrc")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .postData("{\n  \"request\": {\n    \"passengers\": {\n      \"adultCount\": 1\n    },\n    \"slice\": [\n      {\n        \"origin\": \"GRU\",\n        \"destination\": \"FRA\",\n        \"date\": \"2018-07-01\"\n      },\n      {\n        \"origin\": \"FRA\",\n        \"destination\": \"GRU\",\n        \"date\": \"2018-07-15\"\n      }\n    ]\n  }\n}")
            .timeout(60 * 1000, 60 * 1000)
            .asString

        //println(response.body.length)
        val res: TripsSearchResponse = TripsSearchResponse.fromJson(response.body)

        println(res.trips.requestId)
    }
}