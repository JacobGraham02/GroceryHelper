package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.FragmentAddGroceryItemBinding
import com.jacobdamiangraham.groceryhelper.enums.AddGroceryItemInputType
import com.jacobdamiangraham.groceryhelper.factory.PromptBuilderFactory
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryStoreCallback
import com.jacobdamiangraham.groceryhelper.model.DialogInformation
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.utils.CustomEditText
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class AddGroceryItemFragment: Fragment() {

    private var _binding: FragmentAddGroceryItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddGroceryItemViewModel

    private val firebaseStorage: FirebaseStorage = FirebaseStorage()

    private var storeNames = mutableListOf<String>()

    private lateinit var storeNamesSpinnerAdapter: ArrayAdapter<String>

    private val voiceInputFields: List<EditText> by lazy {
        listOf(binding.addItemName, binding.addItemQuantity, binding.addItemCost)
    }

    private var currentVoiceInputFieldIndex: Int = 0

    private lateinit var speechRecognizerLauncher: ActivityResultLauncher<Intent>

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AddGroceryItemViewModel::class.java]
        _binding = FragmentAddGroceryItemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        textToSpeech = TextToSpeech(context) {
            textToSpeechStatus ->
                if (textToSpeechStatus != TextToSpeech.ERROR) {
                    textToSpeech.language = Locale.getDefault()
                }
        }

        val groceryItemArgs: AddGroceryItemFragmentArgs by navArgs()

        val groceryItemValueConditions = mapOf(
            0 to { item: Any? -> item != null && item.toString() != getString(R.string.invalid) },  // Name
            1 to { item: Any? -> item != null && item != 1 },                                       // Quantity
            2 to { item: Any? -> item != null && item != 0.00 },                                    // Cost
        )

        val arrayListBindingElements = arrayListOf(
            binding.addItemName,
            binding.addItemQuantity,
            binding.addItemCost)

        val arrayListGroceryItemArgs = arrayListOf(
            groceryItemArgs.groceryItemName,
            groceryItemArgs.groceryItemQuantity,
            groceryItemArgs.groceryItemCost)

        val groceryItemCategory = groceryItemArgs.groceryItemCategory
        val groceryItemStore = groceryItemArgs.groceryItemStore
        val groceryItemId = groceryItemArgs.groceryItemId

        val arrayListGroceryItemCategory = ArrayList(resources.getStringArray(R.array.categories).toList())

        for (index in arrayListBindingElements.indices) {
            val editText: EditText = arrayListBindingElements[index]
            val itemValue = arrayListGroceryItemArgs[index]

            groceryItemValueConditions[index]?.let { condition ->
                if (condition(itemValue)) {
                    editText.setText(itemValue.toString())
                }
            }
        }

        setupAddItemButton(groceryItemId)
        setupCategorySpinner()
        setupStoreNameSpinner()
        setupStoreNameButton()
        setupVoiceInputButton()
        loadStoreNamesIntoSpinner(groceryItemStore)

        if (groceryItemArgs.groceryItemName != getString(R.string.invalid)) {
            binding.addItemButton.text = getString(R.string.grocery_list_modify_item, groceryItemArgs.groceryItemName)
            binding.addItemPageLabel.text = getString(R.string.grocery_list_title_modify, groceryItemArgs.groceryItemName)
        }

        if (arrayListGroceryItemCategory.contains(groceryItemCategory)) {
            val categoryAdapter =
                binding.addGroceryItemCategorySpinner.adapter as ArrayAdapter<String>
            val categoryPosition = categoryAdapter.getPosition(groceryItemCategory)
            binding.addGroceryItemCategorySpinner.setSelection(categoryPosition)
        }

        binding.addItemName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(AddGroceryItemInputType.NAME, s.toString())
            }
        })

        binding.addItemCost.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(AddGroceryItemInputType.COST, s.toString())
            }
        })

        binding.addItemQuantity.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(AddGroceryItemInputType.QUANTITY, s.toString())
            }
        })

        binding.addItemQuantity.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(AddGroceryItemInputType.STORE, s.toString())
            }
        })


        binding.addNewStore.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(v)
            }
            true
        }
        return root
    }

    private fun speakPrompt(prompt: String) {
        textToSpeech.speak(prompt, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun setupStoreNameButton() {
        binding.addNewStoreConfirmButton.setOnClickListener {
            val newStoreName: String = binding.addNewStore.text.toString()
            addStoreNameToSpinner(newStoreName)

            firebaseStorage.getGroceryStoreNames {
                stores ->
                val groceryStoreExists = stores.contains(newStoreName)
                if (groceryStoreExists) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.store_exists),
                        Toast.LENGTH_SHORT).show()
                    return@getGroceryStoreNames
                }
            }
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
                })
        }
    }

    private fun setupVoiceInputButton() {
        binding.voiceInputButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        lifecycleScope.launch(Dispatchers.Main) {
            val promptMessage = getPromptMessage()
            val ttsCompleted = speakPromptAndWait(promptMessage)

            if (ttsCompleted) {
                launchSpeechRecognizer()
            }
        }
    }

    private suspend fun speakPromptAndWait(promptMessage: String): Boolean {
        val utteranceId = "AddGroceryItemTts"

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        textToSpeech.speak(promptMessage, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        return suspendCancellableCoroutine { continuation ->

            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    continuation.resume(true)
                }

                override fun onError(utteranceId: String?) {
                    continuation.resume(false)
                }
            })
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechRecognizerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val voiceInputText =
                    result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)

                if (voiceInputText != null) {
                    val currentFocusedField = voiceInputFields[currentVoiceInputFieldIndex]

                    if ((currentFocusedField.id == R.id.addItemQuantity) || (currentFocusedField.id == R.id.addItemCost)) {
                        val lowercaseVoiceInputText = voiceInputText.lowercase()
                        val quantityWordToNum = convertWordToNumber(lowercaseVoiceInputText)
                        currentFocusedField.text = SpannableStringBuilder("$quantityWordToNum")
                    } else {
                        currentFocusedField.setText(voiceInputText)
                    }

                    currentVoiceInputFieldIndex++

                    if (currentVoiceInputFieldIndex < voiceInputFields.size) {
                        startVoiceInput()
                    }
                }
            }
        }
    }

    private fun convertWordToNumber(text: String): Int? {
        val numberWords = mapOf(
            "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
            "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9
        )
        return numberWords.get(text)
    }

    private fun getPromptMessage(): String {
        return when (currentVoiceInputFieldIndex) {
            0 -> getString(R.string.tts_item_name)
            1 -> getString(R.string.tts_item_quantity)
            2 -> getString(R.string.tts_item_cost)
            else -> getString(R.string.blank_string)
        }
    }

    private fun closeKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun validate(addGroceryItemInputType: AddGroceryItemInputType, value: String) {
        when (addGroceryItemInputType) {
            AddGroceryItemInputType.NAME -> {
                if (ValidationUtil.isValidGroceryItemString(value)) {
                    binding.addItemName.setBackgroundResource(R.drawable.edit_text_valid)
                    binding.addItemNameLabel.text = getString(R.string.valid_name)
                    binding.addItemNameLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.addItemName.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.addItemName.setBackgroundResource(R.drawable.edit_text_invalid)
                    binding.addItemNameLabel.text = getString(R.string.invalid_name)
                    binding.addItemNameLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.addItemName.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
            AddGroceryItemInputType.QUANTITY -> {
                val quantity = value.toInt()
                if (ValidationUtil.isValidQuantity(quantity)) {
                    binding.addItemQuantity.setBackgroundResource(R.drawable.edit_text_valid)
                    binding.addItemQuantityLabel.text = getString(R.string.valid_quantity)
                    binding.addItemQuantityLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.addItemQuantity.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.addItemQuantity.setBackgroundResource(R.drawable.edit_text_invalid)
                    binding.addItemQuantityLabel.text = getString(R.string.invalid_quantity)
                    binding.addItemQuantityLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.addItemQuantity.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
            AddGroceryItemInputType.COST -> {
                val cost = value.toFloat()
                if (ValidationUtil.isValidCost(cost)) {
                    binding.addItemCost.setBackgroundResource(R.drawable.edit_text_valid)
                    binding.addItemCostLabel.text = getString(R.string.valid_cost)
                    binding.addItemCostLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.addItemCost.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.addItemCost.setBackgroundResource(R.drawable.edit_text_invalid)
                    binding.addItemCostLabel.text = getString(R.string.invalid_cost)
                    binding.addItemCostLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.addItemCost.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }

            AddGroceryItemInputType.STORE -> {
                if (ValidationUtil.isValidGroceryItemString(value)) {
                    binding.addNewStore.setBackgroundResource(R.drawable.edit_text_valid)
                    binding.addNewStoreLabel.text = getString(R.string.valid_store)
                    binding.addNewStoreLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.addNewStore.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.addNewStore.setBackgroundResource(R.drawable.edit_text_invalid)
                    binding.addNewStoreLabel.text = getString(R.string.invalid_store)
                    binding.addNewStoreLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.addNewStore.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
        }
    }

    private fun setupAddItemButton(groceryItemId: String) {
        binding.addItemButton.setOnClickListener {
            val groceryItemName = binding.addItemName.text?.toString() ?: getString(R.string.blank_string)
            val groceryItemQuantity = binding.addItemQuantity.text?.toString()?.toIntOrNull() ?: 0
            val groceryItemCost = binding.addItemCost.text?.toString()?.toFloatOrNull() ?: 0.0f
            val groceryItemStore = binding.addGroceryStoreNameSpinner.selectedItem?.toString() ?: ""
            val groceryItemCategory = binding.addGroceryItemCategorySpinner.selectedItem?.toString() ?: ""

            val validGroceryItemId = if (groceryItemId.isBlank() || !isValidUUID(groceryItemId)) {
                UUID.randomUUID().toString()
            } else {
                groceryItemId
            }

            if (!(ValidationUtil.validateGroceryItemInputs(
                    groceryItemName,
                    groceryItemQuantity,
                    groceryItemCategory,
                    groceryItemStore,
                    groceryItemCost))) {

                Toast.makeText(
                    context,
                    getString(R.string.invalid_data),
                    Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val newGroceryItem = GroceryItem(
                groceryItemName,
                validGroceryItemId,
                groceryItemCategory,
                groceryItemStore,
                groceryItemQuantity,
                groceryItemCost)

            val dialogInfo = DialogInformation(
                title = getString(R.string.confirm_add_grocery_item),
                message = getString(R.string.confirmation_add_grocery_item)
            )
            val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
                getString(R.string.prompt_confirmation)
            )
            alertDialogGenerator.configure(
                AlertDialog.Builder(requireContext()),
                dialogInfo,
                positiveButtonAction = {
                    addGroceryItemToFirebase(newGroceryItem)
                }
            ).show()
        }
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun addGroceryItemToFirebase(groceryItem: GroceryItem) {
        try {
            firebaseStorage.addGroceryItemToFirebase(
                groceryItem,
                object : IAddGroceryItemCallback {
                    override fun onAddSuccess(successMessage: String) {
                        Toast.makeText(
                            context,
                            successMessage,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        clearInputFields()
                    }

                    override fun onAddFailure(failureMessage: String) {
                        Toast.makeText(
                            context,
                            failureMessage,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
        } catch (e: Exception) {
            throw Error(getString(R.string.error_adding_item))
        }
    }

    private fun clearInputFields() {
        binding.addItemName.text = null
        binding.addItemQuantity.text = null
        binding.addItemCost.text = null
        binding.addGroceryItemCategorySpinner.setSelection(0)
        binding.addGroceryStoreNameSpinner.setSelection(0)
        binding.addNewStore.text = null
    }

    private fun setupCategorySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addGroceryItemCategorySpinner.adapter = adapter
        }
    }

    private fun setupStoreNameSpinner() {
        storeNamesSpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            storeNames
        )
        storeNamesSpinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_item
        )
        binding.addGroceryStoreNameSpinner.adapter = storeNamesSpinnerAdapter
    }

    private fun loadStoreNamesIntoSpinner(storeName: String) {
        firebaseStorage.getGroceryStoreNames {
            stores ->
                storeNames.clear()
                storeNames.addAll(stores)
                storeNamesSpinnerAdapter.notifyDataSetChanged()
                setSelectedStoreName(storeName)
        }
    }

    private fun setSelectedStoreName(storeName: String) {
        val normalizedStoreName = storeName.trim().lowercase(Locale.ROOT)
        val normalizedStoreNames = storeNames.map { it.trim().lowercase(Locale.ROOT) }
        val storePositionInSpinner = normalizedStoreNames.indexOf(normalizedStoreName)

        if (storePositionInSpinner != -1) {
            binding.addGroceryStoreNameSpinner.setSelection(storePositionInSpinner)
        }
    }

    private fun addStoreNameToSpinner(newStoreName: String) {
        if (!storeNames.contains(newStoreName)) {
            storeNames.add(newStoreName)
            storeNamesSpinnerAdapter.notifyDataSetChanged()
            binding.addGroceryStoreNameSpinner.setSelection(storeNames.size-1)
        }
    }

    override fun onDestroyView() {
        viewModel.liveDataGroceryItem.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
        textToSpeech.stop()
        textToSpeech.shutdown()
        currentVoiceInputFieldIndex = 0
        _binding = null
    }
}