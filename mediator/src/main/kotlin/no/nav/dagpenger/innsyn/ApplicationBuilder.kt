package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.JournalførtMottak
import no.nav.dagpenger.innsyn.tjenester.OppgaveMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakAvsluttetMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(env: Map<String, String>) : RapidsConnection.StatusListener {
    private val personRepository = PostgresPersonRepository()
    private val personMediator = PersonMediator(personRepository)
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(env)
    ).withKtorModule {
        innsynApi(AuthFactory.jwkProvider, AuthFactory.issuer, AuthFactory.clientId, personRepository)
    }.build().apply {
        SøknadMottak(this, personMediator)
        JournalførtMottak(this, personMediator)
        EttersendingMottak(this, personMediator)
        VedtakMottak(this, personMediator)
        OppgaveMottak(this, personMediator)
        VedtakAvsluttetMottak(this)
    }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
    }
}
