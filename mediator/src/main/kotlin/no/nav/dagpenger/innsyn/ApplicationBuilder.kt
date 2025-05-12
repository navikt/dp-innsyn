package no.nav.dagpenger.innsyn

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.JournalførtMottak
import no.nav.dagpenger.innsyn.tjenester.PåbegyntOppslag
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.RapidApplication

internal class ApplicationBuilder(
    configuration: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val personRepository = PostgresPersonRepository()
    private val påbegyntOppslag =
        PåbegyntOppslag(
            Configuration.dpSoknadUrl,
            Configuration.dpSoknadAudience,
        )
    private val personMediator = PersonMediator(personRepository)
    private val rapidsConnection =
        RapidApplication
            .create(
                configuration,
                builder = {
                    withKtorModule {
                        innsynApi(
                            personRepository,
                            påbegyntOppslag,
                        )
                    }
                },
            ).apply {
                SøknadMottak(this, personMediator)
                JournalførtMottak(this, personMediator)
                EttersendingMottak(this, personMediator)
                VedtakMottak(this, personMediator)
            }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
    }
}
