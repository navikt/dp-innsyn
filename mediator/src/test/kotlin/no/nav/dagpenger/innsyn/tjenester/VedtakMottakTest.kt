package no.nav.dagpenger.innsyn.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Vedtaksmelding
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VedtakMottakTest {
    private val testRapid = TestRapid()
    private val personMediator = mockk<PersonMediator>(relaxed = true)

    @BeforeEach
    internal fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `vi kan motta vedtak (v1)`() {
        VedtakMottak(testRapid, personMediator)
        testRapid.sendTestMessage(vedtakJsonV1)
        verify { personMediator.håndter(any<Vedtak>(), any<Vedtaksmelding>()) }
        confirmVerified(personMediator)
    }

    @Test
    fun `vi kan motta vedtak (v2)`() {
        VedtakMottak(testRapid, personMediator)
        testRapid.sendTestMessage(vedtakJsonV2)
        verify { personMediator.håndter(any<Vedtak>(), any<Vedtaksmelding>()) }
        confirmVerified(personMediator)
    }
}

//language=JSON
private val vedtakJsonV1 =
    """
    {
      "table": "SIAMO.VEDTAK",
      "op_type": "I",
      "op_ts": "2020-04-07 14:31:08.840468",
      "current_ts": "2020-04-07T14:53:03.656001",
      "pos": "00000000000000013022",
      "tokens": {
        "FODSELSNR": "***********"
      },
      "after": {
        "VEDTAK_ID": 29501880,
        "SAK_ID": 123,
        "VEDTAKSTATUSKODE": "IVERK",
        "VEDTAKTYPEKODE": "O",
        "UTFALLKODE": "JA",
        "RETTIGHETKODE": "DAGO",
        "PERSON_ID": 4124685,
        "FRA_DATO": "2018-03-05 00:00:00",
        "TIL_DATO": null
      }
    }
    """.trimIndent()

//language=JSON
private val vedtakJsonV2 =
    """
    {
        "table": "SIAMO.VEDTAK",
        "op_type": "I",
        "op_ts": "2021-11-12 08:31:33.092337",
        "current_ts": "2021-11-12 08:57:55.082000",
        "pos": "00000000000000010892",
        "FODSELSNR": "***********",
        "after": {
            "VEDTAK_ID": 29501880,
            "SAK_ID": 123,
            "VEDTAKSTATUSKODE": "IVERK",
            "VEDTAKTYPEKODE": "O",
            "UTFALLKODE": "JA",
            "RETTIGHETKODE": "DAGO",
            "PERSON_ID": 4124685,
            "FRA_DATO": "2018-03-05 00:00:00",
            "TIL_DATO": null
        }
    }
    """.trimIndent()
