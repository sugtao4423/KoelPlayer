package sugtao4423.koel4j

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Koel4j(private var host: String, private val token: String = "") {

    init {
        if (host.endsWith("/")) {
            host = host.removeSuffix("/")
        }
    }

    companion object {
        const val USER_AGENT = "Android KoelPlayer"
    }

    fun auth(email: String, password: String): String? {
        val endpoint = KoelEndpoints.authentication
        val body = mapOf(
            "email" to email,
            "password" to password
        )
        val json = postKoelApi(endpoint, body)
        return json?.getString("token")
    }

    private fun postKoelApi(endpoint: String, body: Map<String, String> = mapOf()): JSONObject? {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "Authorization" to "Bearer $token",
            "User-Agent" to USER_AGENT
        )
        val json = JSONObject(body).toString()
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().let {
            headers.map { header ->
                it.addHeader(header.key, header.value)
            }
            it.url(host + endpoint)
            it.post(requestBody)
            it.build()
        }
        return try {
            val response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let {
                    return JSONObject(it.string())
                }
            }
            null
        } catch (e: IOException) {
            null
        }
    }

}