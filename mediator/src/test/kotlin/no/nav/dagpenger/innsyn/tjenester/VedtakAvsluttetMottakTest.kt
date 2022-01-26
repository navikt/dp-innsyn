package no.nav.dagpenger.innsyn.tjenester

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VedtakAvsluttetMottakTest {
    private val testRapid = TestRapid()

    @BeforeEach
    internal fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `vi kan motta vedtak (v1)`() {
        VedtakAvsluttetMottak(testRapid)
        testRapid.sendTestMessage(vedtakJsonV1)
    }

    @Test
    fun `vi kan motta vedtak (v2)`() {
        VedtakAvsluttetMottak(testRapid)
        testRapid.sendTestMessage(vedtakJsonV2)
    }
}

private val vedtakJsonV1 = """{
  "table": "SIAMO.VEDTAK",
  "op_type": "U",
  "op_ts": "2020-04-07 14:31:08.840468",
  "current_ts": "2020-04-07T14:53:03.656001",
  "pos": "00000000000000013022",
  "tokens": {
    "FODSELSNR": "***********"
  },
  "before": {
    "VEDTAK_ID": 29501880,
    "SAK_ID": 123,
    "VEDTAKSTATUSKODE": "IVERK",
    "VEDTAKTYPEKODE": "O",
    "UTFALLKODE": "JA",
    "RETTIGHETKODE": "DAGO",
    "PERSON_ID": 4124685,
    "FRA_DATO": "2018-03-05 00:00:00",
    "TIL_DATO": null
  },
  "after": {
    "VEDTAK_ID": 29501880,
    "SAK_ID": 123,
    "VEDTAKSTATUSKODE": "AVSLU",
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
private val vedtakJsonV2 = """{
    "table": "SIAMO.VEDTAK",
  "op_type": "U",
  "op_ts": "2020-04-07 14:31:08.840468",
  "current_ts": "2020-04-07T14:53:03.656001",
  "pos": "00000000000000013022",
   "FODSELSNR": "***********",
  "before": {
    "VEDTAK_ID": 29501880,
    "SAK_ID": 123,
    "VEDTAKSTATUSKODE": "IVERK",
    "VEDTAKTYPEKODE": "O",
    "UTFALLKODE": "JA",
    "RETTIGHETKODE": "DAGO",
    "PERSON_ID": 4124685,
    "FRA_DATO": "2018-03-05 00:00:00",
    "TIL_DATO": null
  },
  "after": {
    "VEDTAK_ID": 29501880,
    "SAK_ID": 123,
    "VEDTAKSTATUSKODE": "AVSLU",
    "VEDTAKTYPEKODE": "O",
    "UTFALLKODE": "JA",
    "RETTIGHETKODE": "DAGO",
    "PERSON_ID": 4124685,
    "FRA_DATO": "2018-03-05 00:00:00",
    "TIL_DATO": null
  }
}
}
""".trimIndent()
