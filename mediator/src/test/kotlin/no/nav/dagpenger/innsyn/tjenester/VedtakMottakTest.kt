package no.nav.dagpenger.innsyn.tjenester

import org.junit.jupiter.api.Test

internal class VedtakMottakTest {
    @Test
    fun ` `() {
    }
}

private val vedtakJson = """{
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
    "VEDTAKSTATUSKODE": "IVERK",
    "VEDTAKTYPEKODE": "E",
    "UTFALLKODE": "JA",
    "RETTIGHETKODE": "DAGO",
    "PERSON_ID": 4124685,
    "FRA_DATO": "2018-03-05 00:00:00",
    "TIL_DATO": null
  }
}
""".trimIndent()
