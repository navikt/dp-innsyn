package no.nav.dagpenger.innsyn

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import no.nav.dagpenger.innsyn.helpers.JwtStub
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InnsynApiTest {

    private val testIssuer = "test-issuer"
    private val jwtStub = JwtStub(testIssuer)
    private val clientId = "id"

    @Test
    fun `test at bruker ikke har søknad`() {
        withTestApplication({
            innsynApi(
                InMemoryPersonRepository(),
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId
            )
        }) {
            autentisert("/soknad")
        }.apply {

            assertEquals(200, response.status()?.value)
        }
    }

    private fun TestApplicationEngine.autentisert(
        endepunkt: String,
        token: String = jwtStub.createTokenFor("test@nav.no", "id"),
        httpMethod: HttpMethod = HttpMethod.Get,
        body: String? = null
    ) = handleRequest(httpMethod, endepunkt) {
        addHeader(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString()
        )
        addHeader(HttpHeaders.Authorization, "Bearer $token")
        body?.also { setBody(it) }
    }
}
