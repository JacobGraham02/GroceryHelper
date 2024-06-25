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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.FragmentAddGroceryStoreBinding
import com.jacobdamiangraham.groceryhelper.factory.PromptBuilderFactory
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryStoreCallback
import com.jacobdamiangraham.groceryhelper.model.DialogInformation
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
        setupStoreNameButton()

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechRecognizerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                    val voiceInputText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)

                    if (voiceInputText != null) {
                        val currentFocusedField = voiceInputFields[currentVoiceInputFieldIndex]
                        currentFocusedField.setText(voiceInputText)
                        currentVoiceInputFieldIndex++
                        if (currentVoiceInputFieldIndex < voiceInputFields.size) {
                            startVoiceInput()
                        }
                    }
                }
        }
    }

    private fun setupStoreNameButton() {
        binding.addNewGroceryStoreButton.setOnClickListener {
            val newStoreName: String = binding.addStoreName.text.toString()

            val dialogInfo = DialogInformation(
                title = getString(R.string.confirm_add_grocery_store),
                message = getString(R.string.confirmation_add_grocery_store)
            )
            val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
                getString(R.string.prompt_confirmation)
            )
            alertDialogGenerator.configure(
                AlertDialog.Builder(requireContext()),
                dialogInfo,
                positiveButtonAction = {
                    addGroceryStoreToFirebase(newStoreName)
                }
            ).show()
        }
    }

    private fun addGroceryStoreToFirebase(newStoreName: String) {
        firebaseStorage.getGroceryStoreNames {
                stores ->
            val groceryStoreExists = stores.contains(newStoreName)
            if (groceryStoreExists) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.store_exists),
                    Toast.LENGTH_SHORT).show()
                return@getGroceryStoreNames
            } else {
                firebaseStorage.addGroceryStoreToUser(
                    newStoreName,
                    object : IAddGroceryStoreCallback {
                        override fun onAddStoreSuccess(successMessage: String) {
                            Toast.makeText(
                                requireContext(),
                                successMessage,
                                Toast.LENGTH_SHORT).show()
                        }

                        override fun onAddStoreFailure(failureMessage: String) {
                            Toast.makeText(
                                requireContext(),
                                failureMessage,
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                )
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

        val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getPromptMessage())
        }

        speechRecognizerLauncher.launch(intent)
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