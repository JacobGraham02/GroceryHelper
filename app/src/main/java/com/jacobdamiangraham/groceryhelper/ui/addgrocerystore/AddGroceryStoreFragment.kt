package com.jacobdamiangraham.groceryhelper.ui.addgrocerystore

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.FragmentAddGroceryStoreBinding
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.utils.CustomEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class AddGroceryStoreFragment: Fragment() {

    private var _binding: FragmentAddGroceryStoreBinding? = null

    private val binding get() = _binding!!

    private val firebaseStorage: FirebaseStorage = FirebaseStorage()

    private val voiceInputFields: List<EditText> by lazy {
        listOf(binding.addStoreName)
    }

    private lateinit var speechRecognizerLauncher: ActivityResultLauncher<Intent>

    private var currentVoiceInputFieldIndex: Int = 0

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGroceryStoreBinding.inflate(inflater, container, false)
        val root: View = binding.root

        textToSpeech = TextToSpeech(context) {
            textToSpeechStatus ->
                if (textToSpeechStatus != TextToSpeech.ERROR) {
                    textToSpeech.language = Locale.getDefault()
                }
        }

        setupVoiceInputButton()

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechRecognizerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                    val voiceInputText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)

                    if (voiceInputText != null) {
                        if (currentVoiceInputFieldIndex < voiceInputFields.size) {
                            startVoiceInput()
                        }
                    }
                }
        }
    }

    private fun startVoiceInput() {
        lifecycleScope.launch(Dispatchers.Main) {
            val currentFocusedField = voiceInputFields[currentVoiceInputFieldIndex]
            val promptMessage = (currentFocusedField as? CustomEditText)?.getTtsPrompt() ?: "Please provide the input."
            val ttsCompleted = speakPromptAndWait(promptMessage)

            if (ttsCompleted) {
                launchSpeechRecognizer()
            }
        }
    }

    private fun setupVoiceInputButton() {
        binding.voiceInputButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun getPromptMessage(): String {
        val customEditText = binding.addStoreName as CustomEditText
        if (customEditText.getTtsPrompt() == null) {
            return ""
        } else {
            return customEditText.getTtsPrompt()!!
        }
    }

    private fun launchSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.tts_not_available),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
    }


    private suspend fun speakPromptAndWait(promptMessage: String): Boolean {
        val utteranceId = "AddGroceryStoreTts"

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        if (!isAdded) {
            return false
        }

        textToSpeech.speak(promptMessage, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        return suspendCancellableCoroutine {
            cancellableContinuation ->
                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(uterranceId: String?) {
                        if (isAdded) {
                            cancellableContinuation.resume(true)
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        if (isAdded) {
                            cancellableContinuation.resume(false)
                        }
                    }
                })
        }
    }
}