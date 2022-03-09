package no.nav.dagpenger.innsyn.common

import io.ktor.http.HttpStatusCode
import no.nav.dagpenger.innsyn.objectmother.MultiSourceResultObjectMother
import no.nav.dagpenger.innsyn.tjenester.ettersending.MinimalEttersendingDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MultiSourceResultTest {

    @Test
    fun `Skal summere to vellykkede resultater til et nytt resultatobjekt`() {
        val expectedSuccessfulSource1 = KildeType.HENVENDELSE
        val expectedSuccessfulSource2 = KildeType.DB

        val source1 = MultiSourceResultObjectMother.giveMeSuccessfulResult(expectedSuccessfulSource1)
        val source2 = MultiSourceResultObjectMother.giveMeSuccessfulResult(expectedSuccessfulSource2)

        val sum = source1 + source2

        assertEquals(sum.results().size, source1.results().size + source2.results().size)
        assertTrue(sum.results().containsAll(source1.results()))
        assertTrue(sum.results().containsAll(source2.results()))

        assertEquals(sum.failedSources().size, source1.failedSources().size + source2.failedSources().size)

        assertEquals(sum.successFullSources().size, 2)
        assertTrue(sum.successFullSources().containsAll(listOf(expectedSuccessfulSource1, expectedSuccessfulSource2)))

        assertFalse(sum.hasErrors())
        assertEquals(sum.determineHttpCode(), HttpStatusCode.OK)
    }

    @Test
    fun `Skal summere et vellykket og et feilende resultat til et nytt resultatobjekt`() {
        val expectedSuccessfulSource = KildeType.HENVENDELSE
        val expectedFailedSource = KildeType.DB

        val successfulSource = MultiSourceResultObjectMother.giveMeSuccessfulResult(expectedSuccessfulSource)
        val failedSource = MultiSourceResultObjectMother.giveMeFailedResult(expectedFailedSource)

        val sum = successfulSource + failedSource

        assertNotNull(sum)
        assertEquals(sum.results().size, (successfulSource.results().size + failedSource.results().size))
        assertTrue(sum.results().containsAll(successfulSource.results()))
        assertTrue(sum.results().containsAll(failedSource.results()))

        assertEquals(sum.failedSources().size, (successfulSource.failedSources().size + failedSource.failedSources().size))

        assertEquals(sum.successFullSources().size, 1)
        assertTrue(sum.successFullSources().contains(expectedSuccessfulSource))

        assertTrue(sum.hasErrors())
        assertEquals(sum.determineHttpCode(), HttpStatusCode.PartialContent)
    }

    @Test
    fun `Skal summere to feilede resultater til et nytt resultatobjekt`() {
        val expectedFailedSource1 = KildeType.HENVENDELSE
        val expectedFailedSource2 = KildeType.DB

        val failedSource1 = MultiSourceResultObjectMother.giveMeFailedResult(expectedFailedSource1)
        val failedSource2 = MultiSourceResultObjectMother.giveMeFailedResult(expectedFailedSource2)

        val tilsammen = failedSource1 + failedSource2

        assertNotNull(tilsammen)
        assertEquals(tilsammen.results().size, (failedSource1.results().size + failedSource2.results().size))
        assertTrue(tilsammen.results().isEmpty())

        assertEquals(tilsammen.failedSources().size, (failedSource1.failedSources().size + failedSource2.failedSources().size))

        assertEquals(tilsammen.successFullSources().size, 0)

        assertEquals(tilsammen.hasErrors(), true)
        assertTrue(tilsammen.failedSources().containsAll(listOf(expectedFailedSource1, expectedFailedSource2)))
        assertEquals(tilsammen.determineHttpCode(), HttpStatusCode.ServiceUnavailable)
    }

    @Test
    fun `Skal kunne addere et tomt resultatobjekt til et annet resultat uten at resultatet endrer seg`() {
        val expectedSource = KildeType.HENVENDELSE

        val validResult = MultiSourceResultObjectMother.giveMeSuccessfulResult(expectedSource)
        val emptyResult = MultiSourceResult.createEmptyResult<MinimalEttersendingDto>()

        val sum = validResult + emptyResult

        assertNotNull(sum)
        assertEquals(sum.results().size, validResult.results().size)
        assertTrue(sum.results().containsAll(validResult.results()))

        assertEquals(sum.failedSources().size, validResult.failedSources().size)

        assertEquals(sum.successFullSources().size, validResult.successFullSources().size)
        assertTrue(sum.successFullSources().contains(expectedSource))

        assertEquals(sum.hasErrors(), false)
        assertEquals(sum.determineHttpCode(), HttpStatusCode.OK)
    }
}
