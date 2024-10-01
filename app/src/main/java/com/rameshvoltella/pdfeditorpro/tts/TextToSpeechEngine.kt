

package com.rameshvoltella.pdfeditorpro.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.io.File

import java.util.Locale

class TextToSpeechEngine private constructor() {
    private var tts: TextToSpeech? = null

    private var defaultPitch = 1f
    private var defaultSpeed = 0.6f
    private var defLanguage = Locale.getDefault()
    private var onStartListener: (() -> Unit)? = null
    private var onDoneListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var onHighlightListener: ((Int, Int) -> Unit)? = null
    private var message: String? = null


    companion object {
        private var instance: TextToSpeechEngine? = null
        fun getInstance(): TextToSpeechEngine {
            if (instance == null) {
                instance = TextToSpeechEngine()
            }
            return instance!!
        }
    }

    fun initTTS(context: Context, message: String,isRecord: Boolean) {
        tts = TextToSpeech(context) {
            if (it == TextToSpeech.SUCCESS) {
                tts!!.language = defLanguage
                tts!!.setPitch(defaultPitch)
                tts!!.setSpeechRate(defaultSpeed)
                tts!!.setListener(
                    onStart = {
                        onStartListener?.invoke()
                    },
                    onError = { e ->
                        e?.let { error ->
                            onErrorListener?.invoke(error)
                        }
                    },
                    onRange = { start, end ->
                        if (this@TextToSpeechEngine.message != null)
                            onHighlightListener?.invoke(start, end)
                    },
                    onDone = {
                        onDoneListener?.invoke()
                    }
                )
                if(!isRecord) {
                    speak(message)
                }else
                {
                    saveAsAudio(context,message)
                }
            } else {
                Log.d("AAA", "initTTS: $it")
                onErrorListener?.invoke(getErrorText(it))
            }
        }
    }

    private fun speak(message: String): TextToSpeechEngine {
        tts!!.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
        )
        /*
       tts!!.playSilentUtterance(2000, QUEUE_ADD, null);*/
        /*val delayInMillis = 5000 // 500 milliseconds delay

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f) // Adjust volume if needed
            putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0.0f) // Adjust pan if needed
//            putFloat(TextToSpeech.Engine.KEY_PARAM_RATE, 1.0f) // Adjust speech rate if needed
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC) // Adjust audio stream if needed
        }
        tts!!.setSpeechRate(1.0f) // Adjust speech rate as needed
            // Queue the TTS message with delay between words
            tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, params, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)

            // Add a delay for each word (not recommended for long messages)
            val words = message.split("\\s+".toRegex())
            var totalDelay: Long = 70000
            words.forEach {
                tts!!.playSilentUtterance(delayInMillis.toLong(), TextToSpeech.QUEUE_ADD, null)
                totalDelay += delayInMillis.toLong()
            }

            // Add a delay for the entire message to allow previous words to be spoken before proceeding
            tts!!.playSilentUtterance(totalDelay, TextToSpeech.QUEUE_ADD, null)

*/
        return this
    }

    fun setPitchAndSpeed(pitch: Float, speed: Float) {
        defaultPitch = pitch
        defaultSpeed = speed
    }

    fun resetPitchAndSpeed() {
        defaultPitch = 1f
        defaultSpeed = 0.6f
    }

    fun setLanguage(local: Locale): TextToSpeechEngine {
        this.defLanguage = local
        return this
    }

    fun setHighlightedMessage(message: String) {
        this.message = message
    }

    fun setOnStartListener(onStartListener: (() -> Unit)): TextToSpeechEngine {
        this.onStartListener = onStartListener
        return this
    }

    fun setOnCompletionListener(onDoneListener: () -> Unit): TextToSpeechEngine {
        this.onDoneListener = onDoneListener
        return this
    }

    fun setOnErrorListener(onErrorListener: (String) -> Unit): TextToSpeechEngine {
        this.onErrorListener = onErrorListener
        return this
    }

    fun setOnHighlightListener(onHighlightListener: (Int, Int) -> Unit): TextToSpeechEngine {
        this.onHighlightListener = onHighlightListener
        return this
    }


    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        instance = null
    }

    fun stop() {
        tts?.stop()
        tts?.shutdown()

    }

    fun saveAsAudio(context: Context,text:String): TextToSpeechEngine {


        val file = File(
            context.filesDir.absolutePath
                    + "/pdfAudio/audio.wav"
        )
        if( !File(
                context.filesDir.absolutePath
                        + "/pdfAudio/").exists())
        {
            File(
                context.filesDir.absolutePath
                        + "/pdfAudio/").mkdirs()
        }
        tts?.synthesizeToFile(text, null, file, "TTS_ID")
        return this
    }
//    File(
//    filesDir.absolutePath
//    + "/lessonNotes/"
//    + intent?.extras?.getString(Constants.BUNDLE_MAIN_TYPES)
//    + "-"
//    + fileBaseDirectory
//    )

}