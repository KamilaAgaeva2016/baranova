package com.tinkoff.gifapp

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tinkoff.gifapp.data.GifAPI
import com.tinkoff.gifapp.databinding.ActivityMainBinding
import com.tinkoff.gifapp.domain.GifItem
import com.tinkoff.gifapp.domain.GifRepo
import com.tinkoff.gifapp.domain.Section
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val gifApi: GifAPI = GifAPI.create()

    private var gifHotRepo: GifRepo = GifRepo(Section.HOT, gifApi)
    private var gifLatestRepo: GifRepo = GifRepo(Section.LATEST, gifApi)
    private var gifTopRepo: GifRepo = GifRepo(Section.TOP, gifApi)

    private var currentSection: Section = Section.LATEST

    private var request: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainTopButton.setOnClickListener {
            startWorkWithSection(Section.TOP)
        }

        binding.mainHotButton.setOnClickListener {
            startWorkWithSection(Section.HOT)
        }

        binding.mainLatestButton.setOnClickListener {
            startWorkWithSection(Section.LATEST)
        }

        binding.forwardButton.setOnClickListener {
            request?.dispose()
            request = getNewGifDisposable(
                when (currentSection) {
                    Section.TOP -> gifTopRepo
                    Section.LATEST -> gifLatestRepo
                    Section.HOT -> gifHotRepo
                }
            )
            checkPreviousGifs()
        }

        binding.backButton.setOnClickListener {
            request?.dispose()
            request = getPreviousGifDisposable(
                when (currentSection) {
                    Section.TOP -> gifTopRepo
                    Section.LATEST -> gifLatestRepo
                    Section.HOT -> gifHotRepo
                }
            )
            checkPreviousGifs()
        }

        binding.reloadButton.setOnClickListener {
            request?.dispose()
            request = getNewGifDisposable(
                when (currentSection) {
                    Section.TOP -> gifTopRepo
                    Section.LATEST -> gifLatestRepo
                    Section.HOT -> gifHotRepo
                }
            )
            checkPreviousGifs()
        }

        binding.mainLatestButton.performClick()
    }

    /**
     * Начало работы с секцией в случае переключения на одну из них (Последнее, Горячее, Лучшее)
     */
    private fun startWorkWithSection(section: Section) {
        request?.dispose()
        currentSection = section
        changeCurrentSectionButtonColor()
        val repo = when (section) {
            Section.TOP -> gifTopRepo
            Section.LATEST -> gifLatestRepo
            Section.HOT -> gifHotRepo
        }
        request = getCurrentGifDisposable(repo)

        binding.backButton.isEnabled = repo.isPreviousAvailable()
    }

    /**
     * Функция проверки возможности перехода (в случае наличия предыдущих гифок) назад для каждой сессии
     */
    private fun checkPreviousGifs() {
        when (currentSection) {
            Section.TOP -> binding.backButton.isEnabled = gifTopRepo.isPreviousAvailable()
            Section.LATEST -> binding.backButton.isEnabled = gifLatestRepo.isPreviousAvailable()
            Section.HOT -> binding.backButton.isEnabled = gifHotRepo.isPreviousAvailable()
        }
    }

    /**
     * Подсвечивание кнопки с выбранной секцией (Последнее, Горячее, Лучшее)
     */
    private fun changeCurrentSectionButtonColor() {
        when (currentSection) {
            Section.TOP -> {
                binding.mainHotButton.setBackgroundColor(Color.TRANSPARENT)
                binding.mainLatestButton.setBackgroundColor(Color.TRANSPARENT)
                binding.mainTopButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.teal_200
                    )
                )
            }
            Section.LATEST -> {
                binding.mainHotButton.setBackgroundColor(Color.TRANSPARENT)
                binding.mainTopButton.setBackgroundColor(Color.TRANSPARENT)
                binding.mainLatestButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.teal_200
                    )
                )
            }
            Section.HOT -> {
                binding.mainLatestButton.setBackgroundColor(Color.TRANSPARENT)
                binding.mainTopButton.setBackgroundColor(Color.TRANSPARENT)
                binding.mainHotButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.teal_200
                    )
                )
            }
        }
    }

    /**
     * Получение из запроса гифки и отображение в UI
     */
    private fun getGifItemDisposable(item: Single<GifItem>): Disposable {
        return item
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                showLoading()
            }
            .subscribe(
                { gifItem ->
                    showGifItem(gifItem)
                },
                {
                    it.printStackTrace()
                    showError()
                }
            )
    }

    /**
     * Отображение предыдущей гифки из сохранненых
     */
    private fun getPreviousGifDisposable(gifRepo: GifRepo): Disposable {
        return getGifItemDisposable(gifRepo.getPreviousGif())
    }

    /**
     * Отображение следующей гифки
     */
    private fun getNewGifDisposable(gifRepo: GifRepo): Disposable {
        return getGifItemDisposable(gifRepo.getNewGif())
    }

    /**
     * Отображение текушей гифки
     */
    private fun getCurrentGifDisposable(gifRepo: GifRepo): Disposable {
        return getGifItemDisposable(gifRepo.getCurrentGif())
    }

    /**
     * Отображение гифки и ее описания в UI
     */
    private fun showGifItem(item: GifItem) {
        binding.errorState.isVisible = false
        binding.dataState.isVisible = true
        binding.descTextView.text = item.description
        loadGif(item.gifURL)
        binding.forwardButton.isEnabled = true
    }

    /**
     * Загрузка гифки в UI
     */
    private fun loadGif(url: String) {
        Glide
            .with(this)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.isVisible = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.isVisible = false
                    return false
                }
            })
            .error(R.drawable.ic_warning)
            .into(binding.imageView)
    }

    /**
     * Отображение состояния ошибки
     */
    private fun showError() {
        binding.dataState.isVisible = false
        binding.progressBar.isVisible = false

        binding.errorState.isVisible = true

    }

    /**
     * Отображение состояния загрузки
     */
    private fun showLoading() {
        binding.errorState.isVisible = false

        binding.dataState.isVisible = true

        binding.imageView.setImageDrawable(null)
        binding.progressBar.isVisible = true
    }

    override fun onDestroy() {
        request?.dispose()
        super.onDestroy()
    }
}