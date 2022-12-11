package com.tkachenko.audionotesvk.audioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.tkachenko.audionotesvk.MyApplication
import com.tkachenko.audionotesvk.utils.Constants
import top.oply.opuslib.OpusEvent
import top.oply.opuslib.OpusService
import java.io.File

private const val TAG = "AudioNotePlayer"

class AudioNotePlayer {
    private val currentPositionMutableLiveData: MutableLiveData<String> = MutableLiveData()
    val currentPositionLiveData = currentPositionMutableLiveData
    private val maxProgressMutableLiveData: MutableLiveData<String> = MutableLiveData()
    val maxProgressLiveData = maxProgressMutableLiveData
    private val playFinishedMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val playFinishedLiveData = playFinishedMutableLiveData

    fun playStart(title: String) {
        OpusService.play(
            MyApplication.applicationContext,
            "${Constants.DIR}/$title.${Constants.EXT_OPUS}"
        )

        val opusReceiver = OpusReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(OpusEvent.ACTION_OPUS_UI_RECEIVER)
        MyApplication.applicationContext.registerReceiver(opusReceiver, intentFilter)
    }

    fun playStop() {
        OpusService.stopPlaying(MyApplication.applicationContext)
    }

    fun deleteAudioFile(fileName: String): Boolean {
        val sourceFile = File(Constants.DIR, "$fileName.${Constants.EXT_OPUS}")
        return if (sourceFile.exists()) {
            sourceFile.delete()
        } else false
    }

    inner class OpusReceiver: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val bundle: Bundle = p1?.extras!!
            when (bundle.getInt(OpusEvent.EVENT_TYPE, 0)) {
                OpusEvent.PLAYING_FAILED -> Log.d("AudioNoteRecorder", "failed")
                OpusEvent.PLAYING_STARTED -> Log.d("AudioNoteRecorder", "started")
                OpusEvent.PLAY_PROGRESS_UPDATE -> {
                    val position = bundle.get(OpusEvent.EVENT_PLAY_PROGRESS_POSITION).toString()
                    val maxPos = bundle.get(OpusEvent.EVENT_PLAY_DURATION).toString()
                    Log.d("AudioNotePlayer", position)
                    currentPositionMutableLiveData.postValue(position)
                    maxProgressMutableLiveData.postValue(maxPos)
                }
                OpusEvent.PLAYING_FINISHED -> {
                    playFinishedMutableLiveData.postValue(true)
                }
            }
        }
    }
}