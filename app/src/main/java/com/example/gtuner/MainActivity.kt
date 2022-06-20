package com.example.gtuner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import kotlin.math.pow
import kotlin.properties.Delegates

var pitchInHz : Float = 0.0f
var currentUserNote : Float = 0.0f

class MainActivity : Activity() {

    @SuppressLint("SetTextI18n")
    fun tes(){
        runOnUiThread {
            val alertText = findViewById<TextView>(R.id.alert_text)
            val topArrow = findViewById<ImageView>(R.id.topArrow)
            val downArrow = findViewById<ImageView>(R.id.downArrow)
            val okImage = findViewById<ImageView>(R.id.okImage)

            alertText.text = "TOO LOW!"
            topArrow.visibility = View.VISIBLE
            downArrow.visibility = View.INVISIBLE
            okImage.visibility = View.INVISIBLE
        }

    }

    @SuppressLint("SetTextI18n")
    fun tes2(){

        runOnUiThread {
            val alertText = findViewById<TextView>(R.id.alert_text)
            val topArrow = findViewById<ImageView>(R.id.topArrow)
            val downArrow = findViewById<ImageView>(R.id.downArrow)
            val okImage = findViewById<ImageView>(R.id.okImage)

            alertText.text = "TOO HIGH!"
            downArrow.visibility = View.VISIBLE
            topArrow.visibility = View.INVISIBLE
            okImage.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    fun tes3(){

        runOnUiThread {
            val alertText = findViewById<TextView>(R.id.alert_text)
            val topArrow = findViewById<ImageView>(R.id.topArrow)
            val downArrow = findViewById<ImageView>(R.id.downArrow)
            val okImage = findViewById<ImageView>(R.id.okImage)

            alertText.text = "PERFECT!"
            downArrow.visibility = View.INVISIBLE
            topArrow.visibility = View.INVISIBLE
            okImage.visibility = View.VISIBLE
        }
    }

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

        val pitchText = findViewById<View>(R.id.pitchText) as TextView
        val noteText = findViewById<View>(R.id.noteText) as TextView
        noteText.setTypeface(null, Typeface.BOLD)
        pitchText.setTypeface(null, Typeface.ITALIC)


        // code example given by authors for starting DSP thread
        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)
        val pdh = PitchDetectionHandler { res, e ->
            // get pitch value calculated by DSP library
            pitchInHz = res.pitch
            // run this code (function) whenever pitchInHz changes value
            runOnUiThread { processPitch(pitchInHz) }
        }
        val pitchProcessor: AudioProcessor =
            PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050.0f, 1024, pdh)
        dispatcher.addAudioProcessor(pitchProcessor)

        // create thread with DSP functionality
        val audioThread = Thread(dispatcher, "Audio Thread")
        // start this thread
        audioThread.start()

        val infoTask = NoteInfoTask(this)
        infoTask.execute()

//        val key1:Button = findViewById(R.id.key1)
//        val buttonText:String = key1.getText().toString()
//        val tuning_text:TextView = findViewById(R.id.tuningText)
//        key1!!.setOnClickListener{
//            tuning_text.setText("This should be $buttonText sound")
//        }

        val key1:Button = findViewById(R.id.key1)
        val key2:Button = findViewById(R.id.key2)
        val key3:Button = findViewById(R.id.key3)
        val key4:Button = findViewById(R.id.key4)
        val key5:Button = findViewById(R.id.key5)
        val key6:Button = findViewById(R.id.key6)
        //Spinner Mode
        val mode :Array<String> = arrayOf("E", "Dropped D", "Open C")
        val e: Spinner = findViewById(R.id.Mode_list)
        val arrayAdapter =
            ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,mode)
        e.adapter = arrayAdapter
        e.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected_mode = parent?.getItemAtPosition(position).toString()

                if (selected_mode == "Dropped D"){
                    key1.text = "D"
                    key2.text = "A"
                    key3.text = "D"
                    key4.text = "G"
                    key5.text = "H"
                    key6.text = "E"
                }

                if (selected_mode == "E"){
                    key1.text = "E"
                    key2.text = "A"
                    key3.text = "D"
                    key4.text = "G"
                    key5.text = "H"
                    key6.text = "E"
                }

                if (selected_mode == "Open C"){
                    key1.text = "C"
                    key2.text = "G"
                    key3.text = "C"
                    key4.text = "G"
                    key5.text = "C"
                    key6.text = "E"
                }

                Toast.makeText(this@MainActivity,"You selected "+mode[position],Toast.LENGTH_LONG).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        //map of frequency
        val soundsMap = mapOf("C" to 121.0f, "D" to 147.0f, "E" to 165.0f, "F" to 175.0f, "G" to 196.0f, "A" to 220.0f, "B" to 247.0f )
        val alertText:TextView = findViewById(R.id.alert_text)


        key1.setOnClickListener{
            //get sound (text) from clicked button and assign to sound value (soundsMap)
            currentUserNote = soundsMap[key1.text.toString()]!!
        }
        key2.setOnClickListener{ currentUserNote = soundsMap[key2.text.toString()]!! }
        key3.setOnClickListener{ currentUserNote = soundsMap[key3.text.toString()]!! }
        key4.setOnClickListener{ currentUserNote = soundsMap[key4.text.toString()]!! }
        key5.setOnClickListener{ currentUserNote = soundsMap[key5.text.toString()]!! }
        key6.setOnClickListener{ currentUserNote = soundsMap[key6.text.toString()]!! }

    }

    private fun processPitch(pitchInHz: Float) {
        val pitchText = findViewById<View>(R.id.pitchText) as TextView
        val noteText = findViewById<View>(R.id.noteText) as TextView
        var hz_info_text = ""
        if (pitchInHz != -1.0f) {
            hz_info_text = "${pitchInHz.toString()} Hz"
        } else {
            "Unknown Note".also { noteText.text = it }
            hz_info_text = "Unknown frequency"
        }
        pitchText.text = hz_info_text
        noteText.text = calcNearestTone(pitchInHz)

    }

    private fun calcNearestTone(freq: Float) : String{
        val sound :Array<String> = arrayOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
        val ratio:Double = 2.0.pow((1.0/12.0))
        val base_frequency = 55.0
        val get_note = {i: Int -> ratio.pow(i) * base_frequency} // get note frequency, i is index of note in regards to A(55 Hz) --> get_note(0) gives 55Hz

        if (freq < get_note(0)) return "Unknown Note"
        if (freq >= get_note(0) && freq < (get_note(1) - get_note(0)) / 2 ){
            return sound[0]
        }

        var tone:String = "X"
        var index:Int = 1
        var curNoteFreq = 0.0
        while (curNoteFreq < 1000.0){
            curNoteFreq = get_note(index)
            val past_freq_diff = (curNoteFreq - get_note(index - 1)) / 2
            val future_freq_diff = (get_note(index + 1) - curNoteFreq) / 2
            if (freq >= curNoteFreq - past_freq_diff && freq < curNoteFreq + future_freq_diff){
                tone = sound[index % 12]
                break // note found, exit while loop
            }
            index++
        }
        return if (tone != "X"){
            tone
        } else {
            "Unknown Note"
        }
    }

    class NoteInfoTask(val mainActivity: MainActivity) : AsyncTask<Void?, Void?, Void?>() {
        protected override fun doInBackground(vararg params: Void?): Void? {
            while (true){
                if (pitchInHz < currentUserNote - 8) {
                    mainActivity.tes()

                } else if (pitchInHz > currentUserNote + 8) {
                    mainActivity.tes2()
                } else {
                    mainActivity.tes3()
                }
                Thread.sleep(100)
            }
            return null
        }
    }
}
