package no.nav.dagpenger.innsyn.tjenester

import no.nav.dagpenger.innsyn.Configuration

object Lenker {
    fun påbegyntNySøknadsdialogIngress(søknadId: String) = Configuration.nySøknadsdialogIngress + "/$søknadId"
    fun påbegyntGammelSøknadsdialogIngress(søknadId: String) = Configuration.gammelSøknadsdialogIngress + "/soknad/$søknadId"
    fun ettersendelseNySøknadsdialog(søknadId: String): String = påbegyntNySøknadsdialogIngress(søknadId) + "/kvittering"
    fun ettersendelseGammelSøknadsdialog(søknadId: String): String = Configuration.gammelSøknadsdialogIngress + "/startettersending/$søknadId"
}
