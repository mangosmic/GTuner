package com.example.gtuner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm


class MainActivity : Activity() {
    /**
     * Signals whether a recording is in progress (true) or not (false).
     */


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            111
        )

        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)
        val pdh = PitchDetectionHandler { res, e ->
            val pitchInHz = res.pitch
            runOnUiThread { processPitch(pitchInHz) }
        }
        val pitchProcessor: AudioProcessor =
            PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050.0f, 1024, pdh)
        dispatcher.addAudioProcessor(pitchProcessor)

        val audioThread = Thread(dispatcher, "Audio Thread")
        audioThread.start()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    fun processPitch(pitchInHz: Float) {
        val pitchText = findViewById<View>(R.id.pitchText) as TextView
        val noteText = findViewById<View>(R.id.noteText) as TextView
        pitchText.text = pitchInHz.toString()
        if (pitchInHz >= 110 && pitchInHz < 123.47) {
            //A
            noteText.text = "A"
        } else if (pitchInHz >= 123.47 && pitchInHz < 130.81) {
            //B
            noteText.text = "B"
        } else if (pitchInHz >= 130.81 && pitchInHz < 146.83) {
            //C
            noteText.text = "C"
        } else if (pitchInHz >= 146.83 && pitchInHz < 164.81) {
            //D
            noteText.text = "D"
        } else if (pitchInHz >= 164.81 && pitchInHz <= 174.61) {
            //E
            noteText.text = "E"
        } else if (pitchInHz >= 174.61 && pitchInHz < 185) {
            //F
            noteText.text = "F"
        } else if (pitchInHz >= 185 && pitchInHz < 196) {
            //G
            noteText.text = "G"
        }
    }

}