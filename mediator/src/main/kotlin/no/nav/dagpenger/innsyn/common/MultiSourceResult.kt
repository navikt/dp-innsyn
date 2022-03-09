package no.nav.dagpenger.innsyn.common

import io.ktor.http.HttpStatusCode

data class MultiSourceResult<R, S>(
    private val results: List<R>,
    private val successFullSources: List<S>,
    private val failedSources: List<S> = emptyList()
) {

    companion object {
        fun <R, S> createSuccessfulResult(results: List<R>, successfulSource: S) = MultiSourceResult(
            results,
            listOf(successfulSource)
        )

        fun <R, S> createErrorResult(failedSource: S) = MultiSourceResult(
            emptyList<R>(),
            emptyList<S>(),
            listOf(failedSource)
        )

        fun <R> createEmptyResult(): MultiSourceResult<R, KildeType> = MultiSourceResult(
            emptyList(),
            emptyList(),
            emptyList()
        )
    }

    operator fun plus(other: MultiSourceResult<R, S>): MultiSourceResult<R, S> =
        MultiSourceResult(
            this.results + other.results,
            this.successFullSources + other.successFullSources,
            this.failedSources + other.failedSources
        )

    fun results() = mutableListOf<R>().apply { addAll(results) }
    fun successFullSources() = mutableListOf<S>().apply { addAll(successFullSources) }

    fun hasErrors() = failedSources.isNotEmpty()
    fun failedSources() = mutableListOf<S>().apply { addAll(failedSources) }

    fun determineHttpCode(): HttpStatusCode {
        return when {
            hasPartialResult() -> HttpStatusCode.PartialContent
            allSourcesFailed() -> HttpStatusCode.ServiceUnavailable
            else -> HttpStatusCode.OK
        }
    }

    private fun hasPartialResult(): Boolean = successFullSources.isNotEmpty() && failedSources.isNotEmpty()
    private fun allSourcesFailed(): Boolean = successFullSources.isEmpty() && failedSources.isNotEmpty()
}

enum class KildeType {
    HENVENDELSE,
    DB
}
