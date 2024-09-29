package com.rameshvoltella.pdfeditorpro.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.io.File
import java.util.Locale
import android.speech.tts.UtteranceProgressListener
class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    init {
        // Initialize Text-to-Speech engine
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language
            tts?.language = Locale.US
        }
    }

    fun convertTextToSpeech(text: String, outputFile: File, onComplete: (Boolean) -> Unit) {
        tts?.let { ttsEngine ->
            // Set a progress listener to handle the TTS completion
            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // No-op: This is when the TTS starts
                }

                override fun onDone(utteranceId: String?) {
                    // Called when the TTS is done
                    onComplete(true)
                }

                override fun onError(utteranceId: String?) {
                    // Called when there is an error
                    onComplete(false)
                }
            })

            // Synthesize speech to the specified file
            val params = hashMapOf<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "ttsOutput"
            ttsEngine.synthesizeToFile(text, null, outputFile, "ttsOutput")
        }
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
