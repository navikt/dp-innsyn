package no.nav.dagpenger.innsyn

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import no.nav.dagpenger.innsyn.aktivdagpenger.DpDatadelingAktivDagpengerTjeneste
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.JournalførtMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.RapidApplication

internal class ApplicationBuilder(
    configuration: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val httpClient =
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 3_000
                connectTimeoutMillis = 2_000
                socketTimeoutMillis = 3_000
            }
            install(ContentNegotiation) {
                jackson {
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    registerModule(JavaTimeModule())
                }
            }
        }
    private val personRepository = PostgresPersonRepository()
    private val aktivDagpengerTjeneste =
        DpDatadelingAktivDagpengerTjeneste(
            httpClient = httpClient,
            oauth2Client = Configuration.azureAdOauth2Client,
            perioderUrl = Configuration.dpDatadelingPerioderUrl,
            scope = Configuration.dpDatadelingScope,
        )

    private val personMediator = PersonMediator(personRepository)
    private val rapidsConnection =
        RapidApplication
            .create(
                configuration,
                builder = {
                    withKtorModule {
                        innsynApi(
                            AuthFactory.jwkProvider,
                            AuthFactory.issuer,
                            AuthFactory.clientId,
                            personRepository,
                            aktivDagpengerTjeneste,
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

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        httpClient.close()
    }
}
