package com.rameshvoltella.pdfeditorpro.tts



import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference
import java.util.Locale

class TextToSpeechHelper private constructor(private val activity: WeakReference<Activity>) :
    LifecycleEventObserver {
    private val context
        get() = activity.get()!!.applicationContext


    private var message: String? = null

    private var tts: TextToSpeechEngine? = null

    private var onStart: (() -> Unit)? = null

    private var onDoneListener: (() -> Unit)? = null

    private var onErrorListener: ((String) -> Unit)? = null

    private var onHighlightListener: ((Pair<Int, Int>) -> Unit)? = null

    private var customActionForDestroy: (() -> Unit)? = null

    private var onLifeCycleChanged: (() -> Unit)? = null



    init {
        initTTS()
    }


    fun registerLifecycle(owner: LifecycleOwner): TextToSpeechHelper {
        owner.lifecycle.addObserver(this)
        return this
    }

    private fun initTTS() = activity.get()?.run {
        tts = TextToSpeechEngine.getInstance()
            .setOnCompletionListener { onDoneListener?.invoke() }
            .setOnErrorListener { onErrorListener?.invoke(it) }
            .setOnStartListener { onStart?.invoke() }
    }

    fun speak(message: String): TextToSpeechHelper {
        if (tts == null)
            initTTS()
        this.message = message

        tts?.initTTS(
            context,
            message,false
        )
        return this
    }
    fun saveAsAudio(message: String): TextToSpeechHelper
    {
        if (tts == null)
            initTTS()
        this.message = message

        tts?.initTTS(
            context,
            message,true
        )
        return this
    }




    fun destroy(
        action: (() -> Unit) = {}
    ) {
        tts?.destroy()
        tts = null
        action.invoke()
        INSTANCE = null
    }

    fun stop(): TextToSpeechHelper
    {
        tts?.stop()
        return this
    }


    fun onStart(onStartListener: () -> Unit): TextToSpeechHelper {
        this.onStart = onStartListener
        return this
    }

    fun onDone(onCompleteListener: () -> Unit): TextToSpeechHelper {
        this.onDoneListener = onCompleteListener
        return this
    }

    fun onError(onErrorListener: (String) -> Unit): TextToSpeechHelper {
        this.onErrorListener = onErrorListener
        return this
    }



    fun onLifeCycleChanged(onLifeCycleChanged: () -> Unit): TextToSpeechHelper {
        this.onLifeCycleChanged = onLifeCycleChanged
        return this
    }


    fun setCustomActionForDestroy(action: () -> Unit): TextToSpeechHelper {
        customActionForDestroy = action
        return this
    }

    fun setLanguage(locale: Locale): TextToSpeechHelper {
        tts?.setLanguage(locale)
        return this
    }

    fun setPitchAndSpeed(
        pitch: Float = DEF_SPEECH_AND_PITCH, speed: Float = DEF_SPEECH_AND_PITCH
    ): TextToSpeechHelper {
        tts?.setPitchAndSpeed(pitch, speed)
        return this
    }

    fun resetPitchAndSpeed(): TextToSpeechHelper {
        tts?.resetPitchAndSpeed()
        return this
    }

    companion object {
        private var INSTANCE: TextToSpeechHelper? = null
        fun getInstance(activity: Activity): TextToSpeechHelper {
            synchronized(TextToSpeechHelper::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = TextToSpeechHelper(WeakReference(activity))
                }
                return INSTANCE!!
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY
            || event == Lifecycle.Event.ON_STOP
            || event == Lifecycle.Event.ON_PAUSE
        ) {
            destroy {
                customActionForDestroy?.invoke()
            }
            onLifeCycleChanged?.invoke()

        }
    }


}