package no.nav.dagpenger.innsyn

import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InnsynApiTest {
    @Test
    fun `test at bruker ikke har søknad`() {
        withTestApplication({ innsynApi(InMemoryPersonRepository()) }) {
            handleRequest(HttpMethod.Get, "/søknad/123").apply {
                assertEquals(200, response.status()?.value)
            }
        }
    }
}
