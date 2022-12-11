package com.tkachenko.audionotesvk.audioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import com.tkachenko.audionotesvk.InvalidFileNameException
import com.tkachenko.audionotesvk.MyApplication
import com.tkachenko.audionotesvk.R
import com.tkachenko.audionotesvk.utils.Constants
import top.oply.opuslib.OpusEvent
import top.oply.opuslib.OpusService
import java.io.*


private const val TAG = "AudioNoteRecorder"

class AudioNoteRecorder {
    private var fileName = ""

    fun recordStart() {
        Log.d(TAG, "record start")

        setUniqueFile()
        OpusService.record(MyApplication.applicationContext, "${Constants.DIR}/$fileName.opus")

        val opusReceiver = OpusReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(OpusEvent.ACTION_OPUS_UI_RECEIVER)
        MyApplication.applicationContext.registerReceiver(opusReceiver, intentFilter)

    }

    fun recordStop() {
        Log.d(TAG, "record stop")
        OpusService.stopRecording(MyApplication.applicationContext)
    }

    fun saveAudioFile(newFilename: String) {
        renameRecordedAudioFile(newFilename)
    }

    fun noSaveFile(): Boolean {
        return deleteRecordedAudioFile()
    }

    fun getDefaultFileName(): String {
        return fileName
    }

    private fun setUniqueFile() {
        fileName = "Новая запись 1"

        var fileWav = File(Constants.DIR, "$fileName.${Constants.EXT_OPUS}")
        var numbFile = 2

        while (fileWav.exists()) {
            fileName = "Новая запись $numbFile"
            fileWav = File(Constants.DIR, "$fileName.${Constants.EXT_OPUS}")
            numbFile++
        }
    }

    private fun deleteRecordedAudioFile(): Boolean {
        val sourceFile = File(Constants.DIR, "$fileName.${Constants.EXT_OPUS}")
        return if (sourceFile.exists()) {
            sourceFile.delete()
        } else false
    }

    private fun renameRecordedAudioFile(newFilename: String) {
        if (newFilename.isEmpty()) {
            throw InvalidFileNameException(MyApplication.applicationContext.getString(R.string.invalidate_file_name_empty))
        } else if (checkFileExist(newFilename)) {
            throw InvalidFileNameException(MyApplication.applicationContext.getString(R.string.invalidate_file_name_exist))
        }

        val sourceFile = File(Constants.DIR, "$fileName.${Constants.EXT_OPUS}")
        val destFile = File(Constants.DIR, "$newFilename.${Constants.EXT_OPUS}")

        if (sourceFile.renameTo(destFile)) {
            fileName = newFilename
        } else {
            throw InvalidFileNameException(MyApplication.applicationContext.getString(R.string.invalidate_file_name_error))
        }
    }

    private fun checkFileExist(newFileName: String): Boolean {
        if (newFileName == fileName) return false

        val file = File(Constants.DIR, "$newFileName.${Constants.EXT_OPUS}")
        return file.exists()
    }

    inner class OpusReceiver: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val bundle: Bundle = p1?.extras!!
            val type = bundle.getInt(OpusEvent.EVENT_TYPE, 0)
            when (type) {
                OpusEvent.RECORD_FAILED -> Log.d("AudioNoteRecorder", "failed")
                OpusEvent.RECORD_STARTED -> Log.d("AudioNoteRecorder", "started")
                OpusEvent.RECORD_PROGRESS_UPDATE -> Log.d("AudioNoteRecorder", "updated")
                OpusEvent.RECORD_FINISHED -> Log.d("AudioNoteRecorder", "finished")
            }
        }
    }
}