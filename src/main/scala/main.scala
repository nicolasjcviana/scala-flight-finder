object Main {

    def main(args: Array[String]): Unit = {
        //val json: String = scala.io.Source.fromFile("C:\\__balaio\\putamerda.json").getLines.mkString
        //val res: TripsSearchResponse = TripsSearchResponse.fromJson(json)

        //carregas as configurações da aplicação
        val settingsJson = scala.io.Source.fromFile("settings.json").getLines.mkString
        val settings = ApplicationSettings.fromJson(settingsJson)

        //cria um flight tracker
        val tracker: FlightTracker = new FlightTracker(settings)
        //e bota pra moer
        tracker.run()
    }

}