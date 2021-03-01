package no.nav.dagpenger.innsyn

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(env: Map<String, String>) : RapidsConnection.StatusListener {
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(env)
    ).withKtorModule {
        innsynApi(mediator = mediator) // AuthFactory.jwkProvider, AuthFactory.issuer, AuthFactory.clientId)
    }.build()
    private val mediator: Mediator
        get() = Mediator(rapidsConnection)
    private val dings: StatusDings = StatusDings(rapidsConnection)

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
}
