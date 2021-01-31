package com.tinkoff.gifapp.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.tinkoff.gifapp.data.GifAPI
import com.tinkoff.gifapp.data.GifDTO
import com.tinkoff.gifapp.data.GifResp
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GifRepoTest {

    private val section = Section.LATEST
    private val api = mock<GifAPI>()

    private lateinit var repo: GifRepo

    @Before
    fun setup() {
        repo = GifRepo(section, api)
    }

    @Test
    fun currentGifWithEmptyGifs() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.just(GifResp(listOf(
                mockDTO(0),
                mockDTO(1),
                mockDTO(2),
                mockDTO(3),
                mockDTO(4)
            )))
        )

        repo.getCurrentGif().test()
            .assertValue {
                it.id == "id0" && it.description == "description0" && it.gifURL == "gifUrl0"
            }
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun currentGifWithError() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.error(
                Throwable("test error message")
            )
        )

        repo.getCurrentGif().test()
            .assertError {
                it.message == "test error message"
            }
    }

    @Test
    fun newGifWithEmptyGifs() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.just(GifResp(listOf(
                mockDTO(0),
                mockDTO(1),
                mockDTO(2),
                mockDTO(3),
                mockDTO(4)
            )))
        )

        repo.getNewGif().test()
            .assertValue {
                it.id == "id0" && it.description == "description0" && it.gifURL == "gifUrl0"
            }
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun newGifWithNonEmptyGifs() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.just(GifResp(listOf(
                mockDTO(0),
                mockDTO(1),
                mockDTO(2),
                mockDTO(3),
                mockDTO(4)
            )))
        )

        repo.getNewGif().subscribe()
        repo.getNewGif().test()
            .assertValue {
                it.id == "id1" && it.description == "description1" && it.gifURL == "gifUrl1"
            }
            .assertComplete()
            .assertNoErrors()

        repo.getNewGif().test()
            .assertValue {
                it.id == "id2" && it.description == "description2" && it.gifURL == "gifUrl2"
            }
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun newGifWithError() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.error(
                Throwable("test error message")
            )
        )

        repo.getNewGif().test()
            .assertError {
                it.message == "test error message"
            }
    }

    @Test
    fun newGifWithNonEmptyGifsAndLast() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.just(GifResp(listOf(mockDTO(0))))
        ).thenReturn(
            Single.just(GifResp(listOf(mockDTO(1))))
        )

        val testObserver0 = repo.getNewGif().test()
        verify(api).getGif(section.value, 0, true)
        testObserver0
            .assertValue {
                it.id == "id0" && it.description == "description0" && it.gifURL == "gifUrl0"
            }
            .assertComplete()
            .assertNoErrors()

        val testObserver1 = repo.getNewGif().test()
        verify(api).getGif(section.value, 1, true)
        testObserver1
            .assertValue {
                it.id == "id1" && it.description == "description1" && it.gifURL == "gifUrl1"
            }
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun previousGifWithEmptyGifs() {
        repo.getPreviousGif().test()
            .assertError {
                it.message == "can't show previous"
            }
    }

    @Test
    fun previousGifWithNonEmptyGifsAndFirstCurrentItem() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.just(GifResp(listOf(
                mockDTO(0),
                mockDTO(1),
                mockDTO(2),
                mockDTO(3),
                mockDTO(4)
            )))
        )

        repo.getCurrentGif().subscribe()

        repo.getPreviousGif().test()
            .assertError {
                it.message == "can't show previous"
            }
    }

    @Test
    fun previousGifWithEmptyGifsAndFirstCurrentItem() {
        whenever(api.getGif(any(), any(), any())).thenReturn(
            Single.just(GifResp(listOf(
                mockDTO(0),
                mockDTO(1),
                mockDTO(2)
            )))
        )

        repo.getCurrentGif().subscribe()
        repo.getNewGif().subscribe()

        repo.getPreviousGif().test()
            .assertValue {
                it.id == "id0" && it.description == "description0" && it.gifURL == "gifUrl0"
            }
            .assertComplete()
            .assertNoErrors()
    }

        private fun mockDTO(index: Int = 0) = GifDTO(
        "id$index", "description$index", "gifUrl$index"
    )
}