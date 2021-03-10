import no.nav.dagpenger.innsyn.modell.Vedlegg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class VedleggTest {

    @Test
    fun `skal kunne vise når fristen for å sende inn vedlegg er`() {
        assertEquals(LocalDate.now().plusDays(14), Vedlegg("id").frist)
    }
}
