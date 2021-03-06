package ok.work.etoroapi.client

import ok.work.etoroapi.client.browser.EtoroMetadataService
import ok.work.etoroapi.model.TradingMode
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.*
import javax.annotation.PostConstruct

@Component
class UserContext {

    lateinit var exchangeToken: String
    lateinit var requestId: String
    lateinit var demogcid: String
    lateinit var realgcid: String
    private lateinit var userdata: JSONObject

    private val client: HttpClient = HttpClient.newHttpClient()

    @Autowired
    lateinit var metadataService: EtoroMetadataService

    @PostConstruct
    fun setupAuthorizationContext() {
        println("setupAuthorizationContext");

        requestId = UUID.randomUUID().toString().toLowerCase()
        val token = System.getenv("TOKEN")
        if (token != null) {
            exchangeToken = token
        } else {
            println("inner token:" + metadataService.getMetadata().token)
            exchangeToken = metadataService.getMetadata().token
        }
        getAccountData(TradingMode.REAL)
    }

    fun getAccountData(mode: TradingMode) {
        val req = prepareRequest("api/logindata/v1.1/logindata?" +
                "client_request_id=${requestId}&conditionIncludeDisplayableInstruments=false&conditionIncludeMarkets=false&conditionIncludeMetadata=false&conditionIncludeMirrorValidation=false",
                exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()
        var responseBody = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
        println(responseBody)
        val response = JSONObject(responseBody)
                .getJSONObject("AggregatedResult")
                .getJSONObject("ApiResponses")
        userdata = response.getJSONObject("CurrentUserData").getJSONObject("Content").getJSONArray("users").getJSONObject(0)
        realgcid = userdata.getInt("realCID").toString()
        demogcid = userdata.getInt("demoCID").toString()
    }
}
