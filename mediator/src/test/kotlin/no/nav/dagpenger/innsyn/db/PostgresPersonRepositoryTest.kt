import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import org.junit.jupiter.api.Test

class PostgresPersonRepositoryTest {

    @Test
    fun `skal lagreogfinne person`() {
        PostgresPersonRepository().person("123")
    }
}
