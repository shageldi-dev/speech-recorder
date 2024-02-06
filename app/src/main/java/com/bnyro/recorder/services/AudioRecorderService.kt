package com.bnyro.recorder.services

import android.media.MediaRecorder
import android.widget.Toast
import com.bnyro.recorder.App
import com.bnyro.recorder.R
import com.bnyro.recorder.db.AppDatabase
import com.bnyro.recorder.db.RecordEntity
import com.bnyro.recorder.enums.AudioChannels
import com.bnyro.recorder.enums.AudioDeviceSource
import com.bnyro.recorder.obj.AudioFormat
import com.bnyro.recorder.util.PlayerHelper
import com.bnyro.recorder.util.Preferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AudioRecorderService : RecorderService() {
    override val notificationTitle: String
        get() = getString(R.string.recording_audio)

    @OptIn(DelicateCoroutinesApi::class)
    override fun start() {
        val audioFormat = AudioFormat.getCurrent()
        val db = AppDatabase.getDatabase(this)
        recorder = PlayerHelper.newRecorder(this).apply {
            Preferences.prefs.getInt(
                Preferences.audioDeviceSourceKey,
                AudioDeviceSource.DEFAULT.value
            ).let {
                setAudioSource(it)
            }
            if (audioFormat.codec != MediaRecorder.AudioEncoder.OPUS) {
                Preferences.prefs.getInt(Preferences.audioSampleRateKey, -1).takeIf { it > 0 }
                    ?.let {
                        setAudioSamplingRate(it)
                        setAudioEncodingBitRate(it * 32 * 2)
                    }
                Preferences.prefs.getInt(Preferences.audioBitrateKey, -1).takeIf { it > 0 }?.let {
                    setAudioEncodingBitRate(it)
                }
            }

            Preferences.prefs.getInt(Preferences.audioChannelsKey, AudioChannels.MONO.value).let {
                setAudioChannels(it)
            }



            setOutputFormat(audioFormat.format)
            setAudioEncoder(audioFormat.codec)



            outputFile = (application as App).fileRepository.getOutputFile(
                audioFormat.extension,
                "${
                    Preferences.prefs.getString(
                        Preferences.username,
                        ""
                    )
                }_${
                    Preferences.prefs.getString(
                        Preferences.step,
                        ""
                    )
                }_${Preferences.prefs.getString(Preferences.words, "")}_"
            )
            if (outputFile == null) {
                Toast.makeText(
                    this@AudioRecorderService,
                    R.string.cant_access_selected_folder,
                    Toast.LENGTH_LONG
                ).show()
                onDestroy()
                return
            }
//            Preferences.edit {
//                putString(Preferences.currentFile, "${outputFile?.uri?.path}")
//            }
            GlobalScope.launch {
                db.recordDao().insertRecord(
                    RecordEntity(
                        filename = "${outputFile?.name}",
                        fileDirectory = Preferences.prefs.getString(Preferences.targetFolderKey, "")
                            .toString(),
                        words = "${Preferences.prefs.getString(Preferences.words, "")}".split("*"),
                        filesize = outputFile?.length()?:0
                    )
                )
            }


            fileDescriptor = contentResolver.openFileDescriptor(outputFile!!.uri, "w")
            setOutputFile(fileDescriptor?.fileDescriptor)

            runCatching {
                prepare()
            }

            start()
        }

        super.start()
    }

    override fun getCurrentAmplitude() = recorder?.maxAmplitude
}
