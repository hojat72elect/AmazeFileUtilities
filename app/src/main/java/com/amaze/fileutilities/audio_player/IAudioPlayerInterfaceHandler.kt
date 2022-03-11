/*
 * Copyright (C) 2021-2022 Team Amaze - Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com>. All Rights reserved.
 *
 * This file is part of Amaze File Utilities.
 *
 * 'Amaze File Utilities' is a registered trademark of Team Amaze. All other product
 * and company names mentioned are trademarks or registered trademarks of their respective owners.
 */

package com.amaze.fileutilities.audio_player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.amaze.fileutilities.R
import com.amaze.fileutilities.utilis.getFileFromUri
import com.amaze.fileutilities.utilis.hideFade
import com.amaze.fileutilities.utilis.showFade
import com.google.android.material.slider.Slider
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import linc.com.amplituda.exceptions.io.FileNotFoundException
import java.lang.ref.WeakReference
import kotlin.math.ceil

interface IAudioPlayerInterfaceHandler : OnPlaybackInfoUpdate, LifecycleOwner {
    fun getSeekbar(): Slider?
    fun getWaveformSeekbar(): WaveformSeekBar?
    fun getTimeElapsedTextView(): TextView?
    fun getTrackLengthTextView(): TextView?
    fun getTitleTextView(): TextView?
    fun getAlbumTextView(): TextView?
    fun getArtistTextView(): TextView?
    fun getPlayButton(): ImageView?
    fun getPrevButton(): ImageView?
    fun getNextButton(): ImageView?
    fun getShuffleButton(): ImageView?
    fun getRepeatButton(): ImageView?
    fun getContextWeakRef(): WeakReference<Context>
    fun getAudioPlayerHandlerViewModel(): AudioPlayerInterfaceHandlerViewModel

    override fun onPositionUpdate(progressHandler: AudioProgressHandler) {
        progressHandler.audioPlaybackInfo.duration.toFloat()
        getSeekbar()?.let {
            seekbar ->
            seekbar.valueTo = progressHandler.audioPlaybackInfo.duration.toFloat()
                .coerceAtLeast(0.1f)
            seekbar.value = progressHandler.audioPlaybackInfo.currentPosition.toFloat()
                .coerceAtMost(seekbar.valueTo)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWaveformSeekbar()?.let {
                waveformSeekBar ->
                waveformSeekBar.maxProgress = progressHandler.audioPlaybackInfo.duration
                    .toFloat()
                waveformSeekBar.progress = progressHandler
                    .audioPlaybackInfo.currentPosition.toFloat()
                    .coerceAtMost(waveformSeekBar.maxProgress)
            }
        }
        getTimeElapsedTextView()?.text = AudioUtils.getReadableDurationString(
            progressHandler
                .audioPlaybackInfo.currentPosition
        ) ?: ""
        getTrackLengthTextView()?.text = AudioUtils.getReadableDurationString(
            progressHandler
                .audioPlaybackInfo.duration
        ) ?: ""
        invalidateActionButtons(progressHandler)
    }

    override fun onPlaybackStateChanged(
        progressHandler: AudioProgressHandler,
        renderWaveform: Boolean
    ) {
        invalidateActionButtons(progressHandler)
        // invalidate wavebar
        if (renderWaveform) {
            loadWaveFormSeekbar(progressHandler)
        }
    }

    override fun setupActionButtons(audioServiceRef: WeakReference<ServiceOperationCallback>) {
        val audioService = audioServiceRef.get()
        getTitleTextView()?.text = audioService?.getAudioPlaybackInfo()?.title
        getAlbumTextView()?.text = audioService?.getAudioPlaybackInfo()?.albumName
        getArtistTextView()?.text = audioService?.getAudioPlaybackInfo()?.artistName

        audioService?.let {
            setShuffleButton(it.getShuffle())
            setRepeatButton(it.getRepeat())
        }

        audioService?.let {
            getPlayButton()?.setOnClickListener {
                audioService.invokePlayPausePlayer()
                invalidateActionButtons(audioService.getAudioProgressHandlerCallback())
            }
            getPrevButton()?.setOnClickListener {
                getContextWeakRef()
                    .get()?.startService(retrievePlaybackAction(AudioPlayerService.ACTION_PREVIOUS))
            }
            getNextButton()?.setOnClickListener {
                getContextWeakRef()
                    .get()?.startService(retrievePlaybackAction(AudioPlayerService.ACTION_NEXT))
            }
            getShuffleButton()?.setOnClickListener {
                setShuffleButton(audioService.cycleShuffle())
            }
            getRepeatButton()?.setOnClickListener {
                setRepeatButton(audioService.cycleRepeat())
            }
        }
        setupSeekBars(audioService)
    }

    private fun setShuffleButton(doShuffle: Boolean) {
        getContextWeakRef().get()?.let {
            val gray = it.resources.getColor(R.color.grey_color)
            getShuffleButton()?.setImageDrawable(
                it.resources
                    .getDrawable(R.drawable.ic_round_shuffle_24)
            )
            if (!doShuffle) {
                getShuffleButton()?.setColorFilter(gray)
            } else {
                getShuffleButton()?.setColorFilter(null)
            }
        }
    }

    private fun setRepeatButton(repeatMode: Int) {
        getContextWeakRef().get()?.let {
            when (repeatMode) {
                AudioPlayerService.REPEAT_NONE -> {
                    val gray = it.resources.getColor(R.color.grey_color)
                    getRepeatButton()?.setImageDrawable(
                        it.resources
                            .getDrawable(R.drawable.ic_round_repeat_24)
                    )
                    getRepeatButton()?.setColorFilter(gray)
                }
                AudioPlayerService.REPEAT_ALL -> {
                    getRepeatButton()?.setImageDrawable(
                        it.resources
                            .getDrawable(R.drawable.ic_round_repeat_24)
                    )
                    getRepeatButton()?.setColorFilter(null)
                }
                AudioPlayerService.REPEAT_SINGLE -> {
                    getRepeatButton()?.setImageDrawable(
                        it.resources
                            .getDrawable(R.drawable.ic_round_repeat_one_24)
                    )
                    getRepeatButton()?.setColorFilter(null)
                }
                else -> {
                    getRepeatButton()?.setImageDrawable(
                        it.resources
                            .getDrawable(R.drawable.ic_round_repeat_24)
                    )
                    getRepeatButton()?.setColorFilter(null)
                }
            }
        }
    }

    private fun invalidateActionButtons(progressHandler: AudioProgressHandler?) {
        getAudioPlayerHandlerViewModel().isPlaying =
            progressHandler?.audioPlaybackInfo?.isPlaying ?: false
        progressHandler?.audioPlaybackInfo?.let {
            info ->
            if (progressHandler.isCancelled || !info.isPlaying) {
                getPlayButton()?.setImageResource(R.drawable.ic_round_play_circle_32)
            } else {
                getPlayButton()?.setImageResource(R.drawable.ic_round_pause_circle_32)
            }
            setShuffleButton(progressHandler.doShuffle)
            setRepeatButton(progressHandler.repeatMode)
            if (progressHandler.isCancelled) {
                setSeekbarProgress(0)
            }
            getTitleTextView()?.text = info.title
            getAlbumTextView()?.text = info.albumName
            getArtistTextView()?.text = info.artistName
        }
    }

    private fun setSeekbarProgress(progress: Int) {
        getSeekbar()?.value = progress.toFloat().coerceAtMost(
            getSeekbar()?.valueTo ?: 0f
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWaveformSeekbar()?.visibility = View.VISIBLE
            getSeekbar()?.visibility = View.GONE
            getWaveformSeekbar()?.progress = progress.toFloat()
        } else {
            getSeekbar()?.visibility = View.VISIBLE
            getWaveformSeekbar()?.visibility = View.GONE
        }
    }

    private fun retrievePlaybackAction(action: String): Intent? {
        getContextWeakRef().get()?.let {
            val serviceName = ComponentName(it, AudioPlayerService::class.java)
            val intent = Intent(action)
            intent.component = serviceName
            return intent
        }
        return null
    }

    private fun loadWaveFormSeekbar(progressHandler: AudioProgressHandler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            !getAudioPlayerHandlerViewModel().forceShowSeekbar
        ) {
            scheduleWaveformSeekbarVisibility()
            getContextWeakRef().get()?.let {
                context ->
                val file = progressHandler.audioPlaybackInfo
                    .audioModel.getUri().getFileFromUri(context)
                if (file != null) {
                    try {
                        // TODO: hack to get valid wavebar path
                        /*if (!file.path.startsWith("/storage")) {
                            file = File("storage/" + file.path)
                        }*/
                        getWaveformSeekbar()?.setSampleFrom(file)
                    } catch (fe: FileNotFoundException) {
                        fe.printStackTrace()
                        getAudioPlayerHandlerViewModel().forceShowSeekbar = true
                    }
                } else {
                    getAudioPlayerHandlerViewModel().forceShowSeekbar = true
                }
            }
        }
    }

    private fun setupSeekBars(audioService: ServiceOperationCallback?) {
        val valueTo = audioService?.getAudioPlaybackInfo()?.duration?.toFloat()
        getSeekbar()?.valueTo = valueTo?.coerceAtLeast(0.1f) ?: 0f
        getWaveformSeekbar()?.maxProgress = audioService
            ?.getAudioPlaybackInfo()?.duration?.toFloat() ?: 0f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            !getAudioPlayerHandlerViewModel().forceShowSeekbar
        ) {
            scheduleWaveformSeekbarVisibility()
            getContextWeakRef().get().let {
                context ->
                if (context != null) {
                    val file = audioService?.getAudioProgressHandlerCallback()?.audioPlaybackInfo
                        ?.audioModel?.getUri()?.getFileFromUri(context)
                    if (file != null) {
                        try {
                            getWaveformSeekbar()?.setSampleFrom(file)
                        } catch (fe: FileNotFoundException) {
                            fe.printStackTrace()
                            getAudioPlayerHandlerViewModel().forceShowSeekbar = true
                            setupSeekBars(audioService)
                            return
                        }
                    } else {
                        getAudioPlayerHandlerViewModel().forceShowSeekbar = true
                        setupSeekBars(audioService)
                        return
                    }
                } else {
                    getAudioPlayerHandlerViewModel().forceShowSeekbar = true
                    setupSeekBars(audioService)
                    return
                }
            }

            getWaveformSeekbar()?.onProgressChanged = object : SeekBarOnProgressChanged {
                override fun onProgressChanged(
                    waveformSeekBar: WaveformSeekBar,
                    progress: Float,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
//                            mediaController.transportControls.seekTo(progress.toLong())
//                            audioService.seekPlayer(progress.toLong())
                        audioService?.invokeSeekPlayer(progress.toLong())
                        val x: Int = ceil(progress / 1000f).toInt()

                        if (x == 0 && audioService?.getAudioProgressHandlerCallback()
                            ?.isCancelled == true
                        ) {
                            waveformSeekBar.progress = 0f
                        }
                    }
                }
            }
        } else {
            getSeekbar()?.visibility = View.VISIBLE
            getWaveformSeekbar()?.visibility = View.GONE
        }
        getSeekbar()?.addOnChangeListener(
            Slider.OnChangeListener { slider, value, fromUser ->
                if (fromUser) {
//                    mediaController.transportControls.seekTo(progress.toLong())
//                    audioService.seekPlayer(progress.toLong())
                    audioService?.invokeSeekPlayer(value.toLong())
                    val x: Int = ceil(value / 1000f).toInt()

                    if (x == 0 && audioService?.getAudioProgressHandlerCallback()
                        ?.isCancelled == true
                    ) {
                        slider.value = 0f
                    }
                }
            }
        )
    }

    private fun scheduleWaveformSeekbarVisibility() {
        getWaveformSeekbar()?.hideFade(300)
        getSeekbar()?.showFade(200)
        object : CountDownTimer(5000, 5000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if (!getAudioPlayerHandlerViewModel().forceShowSeekbar) {
                    getSeekbar()?.cancelPendingInputEvents()
                    getWaveformSeekbar()?.showFade(300)
                    getSeekbar()?.hideFade(200)
                }
            }
        }.start()
    }
}