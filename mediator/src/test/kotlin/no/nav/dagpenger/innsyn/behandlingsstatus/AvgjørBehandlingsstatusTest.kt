package no.nav.dagpenger.innsyn.behandlingsstatus

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.innsyn.db.PersonRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AvgjørBehandlingsstatusTest {
    private val personRepository = mockk<PersonRepository>(relaxed = true)
    private val avgjørBehandlingsstatus = AvgjørBehandlingsstatus(personRepository)

    @Test
    fun `verifiser at det blir delegert riktig videre`() {
        val expectedFom = LocalDate.now()
        val expectedTom = LocalDate.now()
        val dummyFnr = "123"

        val fnrParameter = slot<String>()
        val fom = slot<LocalDate>()
        val tom = slot<LocalDate>()

        avgjørBehandlingsstatus.hentStatus(dummyFnr, expectedFom, expectedTom)

        verify {
            personRepository.hentSøknaderFor(capture(fnrParameter), capture(fom), capture(tom))
        }
        assertEquals(dummyFnr, fnrParameter.captured)
        assertEquals(expectedFom, fom.captured)
        assertEquals(expectedTom, tom.captured)

        fnrParameter.clear()
        fom.clear()
        tom.clear()

        verify {
            personRepository.hentVedtakFor(capture(fnrParameter), capture(fom), capture(tom))
        }
        assertEquals(dummyFnr, fnrParameter.captured)
        assertEquals(expectedFom, fom.captured)
        assertEquals(expectedTom, tom.captured)

        confirmVerified(personRepository)
    }
}
