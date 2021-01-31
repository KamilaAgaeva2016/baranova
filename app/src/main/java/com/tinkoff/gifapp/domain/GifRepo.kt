package com.tinkoff.gifapp.domain

import com.tinkoff.gifapp.data.GifAPI
import io.reactivex.Single

/**
 * Класс-репозиторий для хранения гифок кокнретной секции
 */
class GifRepo(
    private val section: Section,
    private val gifApi: GifAPI
) {
    /**
     * Список уже загруженных гифок
     */
    private val gifs = arrayListOf<GifItem>()
    /**
     * Индекс текущей показываемой гифки
     */
    private var currentIndex : Int = -1
    /**
     * Номер последней загруженной страницы
     */
    private var currentPageNumber: Int = -1

    /**
     * Запрос на получение новых гифок
     *  @param pageNumber - номер страницы
     */
    private fun getNewGifsRequest(pageNumber: Int): Single<List<GifItem>> {
        return gifApi.getGif(section.value, pageNumber)
            .map { resp ->
                resp.result?.map { dto ->
                    GifItem(
                        dto.id ?: "",
                        dto.description ?: "",
                        dto.gifURL ?: ""
                    )
                }
            }
    }

    /**
     * Проверка возможности перехода на предыдущую гифку
     */
    fun isPreviousAvailable(): Boolean {
        return currentIndex > 0
    }

    /**
     * Получение предыдущей гифки
     */
    fun getPreviousGif(): Single<GifItem> {
        return if (gifs.isNotEmpty() && currentIndex != 0) {
            currentIndex -= 1
            Single.just(gifs[currentIndex])
        } else {
            Single.error(IllegalStateException("can't show previous"))
        }
    }

    /**
     * Получение текущей гифки
     */
    fun getCurrentGif(): Single<GifItem> {
        return if (gifs.isEmpty()) {
            getNewGif()
        } else {
            Single.just(gifs[currentIndex])
        }
    }

    /**
     * Получение следующей гифки
     */
    fun getNewGif(): Single<GifItem> {
        return if (gifs.isEmpty() || currentIndex == gifs.size - 1) {
            currentPageNumber += 1
            getNewGifsRequest(currentPageNumber)
                .map { items ->
                    gifs.addAll(items)
                    currentIndex +=1
                    gifs[currentIndex]
                }
        } else {
            currentIndex += 1
            Single.just(gifs[currentIndex])
        }
    }
}