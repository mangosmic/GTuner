package com.example.gtuner

import android.annotation.SuppressLint
import android.media.*
import android.util.Log
import com.robinhood.spark.SparkAdapter
import java.io.*
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecordHelper(val pcm_file_path : String) {
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private val recordingInProgress = AtomicBoolean(false)
//    private val pcm_file_path: String = "${externalCacheDir?.absolutePath}/recording.pcm"

    @SuppressLint("MissingPermission")
    private fun startRecording() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.RECORD_AUDIO
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
        recorder = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT, SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE
        )
        recorder!!.startRecording()
        recordingInProgress.set(true)
        recordingThread = Thread(RecordingRunnable(), "Recording Thread")
        recordingThread!!.start()
    }

    private fun stopRecording() {
        if (null == recorder) {
            return
        }
        recordingInProgress.set(false)
        recorder!!.stop()
        recorder!!.release()
        recorder = null
        recordingThread = null
    }

    @Throws(IOException::class)
    private fun PlayAudioFileViaAudioTrack() {
        // previously worked with manually typed string
        var file = File(pcm_file_path)
        var byteData = ByteArray(file!!.length().toInt())
        var `in`: FileInputStream? = null
        try {
            `in` = FileInputStream(file)
            `in`.read(byteData)
            `in`.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val intSize = AudioTrack.getMinBufferSize(
            44100, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val at = AudioTrack(
            AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM
        )
        if (at != null) {
            at.play()
            // Write the byte array to the track
            at.write(byteData, 0, byteData.size)
            at.stop()
            at.release()
        } else Log.d("TCAudio", "audio track is not initialised ")

//        val floats : FloatArray? = byteToFloat(byteData)
//
//        val sparkView = findViewById<View>(R.id.sparkview) as SparkView
//        sparkView.setLineColor(getColor(R.color.purple_200));
//        sparkView.adapter = floats?.let { MyAdapter(it) }

    }

    fun byteToFloat(bytes: ByteArray): FloatArray? {
        val arr_size = bytes.size / 2
        val floaters = FloatArray(arr_size)
        val bb = ByteBuffer.wrap(bytes)
        for (i in 0 until arr_size) {
            floaters[i] = bb.short.toFloat()
        }
        return floaters
    }

    private inner class RecordingRunnable : Runnable {
        override fun run() {
            val file = File(pcm_file_path)
            val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
            try {
                FileOutputStream(file).use { outStream ->
                    while (recordingInProgress.get()) {
                        val result =
                            recorder!!.read(buffer, BUFFER_SIZE)
                        if (result < 0) {
                            throw RuntimeException(
                                "Reading of audio buffer failed: " +
                                        getBufferReadFailureReason(result)
                            )
                        }
                        outStream.write(
                            buffer.array(),
                            0,
                            BUFFER_SIZE
                        )
                        buffer.clear()
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Writing of recorded audio failed", e)
            }
        }

        private fun getBufferReadFailureReason(errorCode: Int): String {
            return when (errorCode) {
                AudioRecord.ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
                AudioRecord.ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
                AudioRecord.ERROR_DEAD_OBJECT -> "ERROR_DEAD_OBJECT"
                AudioRecord.ERROR -> "ERROR"
                else -> "Unknown ($errorCode)"
            }
        }
    }

    companion object {
        private const val SAMPLING_RATE_IN_HZ = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        /**
         * Factor by that the minimum buffer size is multiplied. The bigger the factor is the less
         * likely it is that samples will be dropped, but more memory will be used. The minimum buffer
         * size is determined by [AudioRecord.getMinBufferSize] and depends on the
         * recording settings.
         */
        private const val BUFFER_SIZE_FACTOR = 2

        /**
         * Size of the buffer where the audio data is stored by Android
         */
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG, AUDIO_FORMAT
        ) * BUFFER_SIZE_FACTOR
    }
}

//class MyAdapter(private val yData: FloatArray) : SparkAdapter() {
//    override fun getCount(): Int {
//        return yData.size
//    }
//
//    override fun getItem(index: Int): Any {
//        return yData[index]
//    }
//
//    override fun getY(index: Int): Float {
//        return yData[index]
//    }
//}