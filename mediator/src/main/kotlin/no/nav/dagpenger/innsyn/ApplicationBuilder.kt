package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(env: Map<String, String>) : RapidsConnection.StatusListener {
    private val personRepository = PostgresPersonRepository()
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(env)
    ).withKtorModule {
        innsynApi(personRepository) // AuthFactory.jwkProvider, AuthFactory.issuer, AuthFactory.clientId)
    }.build().apply {
        Søknadsmottak(this, personRepository)
    }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
}
