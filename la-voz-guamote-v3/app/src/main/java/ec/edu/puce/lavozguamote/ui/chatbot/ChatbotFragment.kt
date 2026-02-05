package ec.edu.puce.lavozguamote.ui.chatbot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.FragmentChatbotBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class ChatbotFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatbotViewModel by viewModels()
    private lateinit var adapter: ChatMessagesAdapter

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private var currentLanguage = "es"

    companion object {
        private const val PERMISSION_REQUEST_AUDIO = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        setupVoiceRecognition()
        setupTextToSpeech()
        observeViewModel()
        
        // Mensaje de bienvenida
        viewModel.addWelcomeMessage(getString(R.string.chat_welcome))
    }

    private fun setupRecyclerView() {
        adapter = ChatMessagesAdapter()
        
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = this@ChatbotFragment.adapter
        }
    }

    private fun setupClickListeners() {
        // Botón atrás
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Botón de enviar
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Enter para enviar
        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        // Botón de voz (mantener presionado)
        binding.btnVoice.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startVoiceRecognition()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopVoiceRecognition()
                    true
                }
                else -> false
            }
        }

        // Selector de idioma
        binding.btnLanguage.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessage(text, currentLanguage)
            binding.etMessage.text?.clear()
            scrollToBottom()
        }
    }

    private fun setupVoiceRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    binding.tvStatus.text = getString(R.string.chat_listening)
                    binding.btnVoice.setBackgroundResource(R.drawable.bg_voice_button_active)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    binding.tvStatus.text = getString(R.string.chat_thinking)
                }

                override fun onError(error: Int) {
                    binding.tvStatus.text = getString(R.string.radio_live)
                    binding.btnVoice.setBackgroundResource(R.drawable.bg_voice_button)
                    isListening = false
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        binding.etMessage.setText(text)
                        sendMessage()
                    }
                    binding.tvStatus.text = getString(R.string.radio_live)
                    binding.btnVoice.setBackgroundResource(R.drawable.bg_voice_button)
                    isListening = false
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun startVoiceRecognition() {
        if (!checkAudioPermission()) {
            requestAudioPermission()
            return
        }

        val locale = when (currentLanguage) {
            "qu" -> Locale("es", "EC")
            "en" -> Locale.ENGLISH
            else -> Locale("es", "EC")
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Toast.makeText(context, "Error al iniciar reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopVoiceRecognition() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            binding.btnVoice.setBackgroundResource(R.drawable.bg_voice_button)
            binding.tvStatus.text = getString(R.string.radio_live)
        }
    }

    private fun setupTextToSpeech() {
        tts = TextToSpeech(requireContext(), this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = when (currentLanguage) {
                "en" -> Locale.ENGLISH
                else -> Locale("es", "EC")
            }
            val result = tts?.setLanguage(locale)
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun speakResponse(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "response")
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.lang_spanish),
            getString(R.string.lang_kichwa),
            getString(R.string.lang_english)
        )
        val codes = arrayOf("es", "qu", "en")
        val currentIndex = codes.indexOf(currentLanguage)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                currentLanguage = codes[which]
                updateLanguageUI()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateLanguageUI() {
        val locale = when (currentLanguage) {
            "en" -> Locale.ENGLISH
            else -> Locale("es", "EC")
        }
        tts?.setLanguage(locale)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                adapter.submitList(messages.toList())
                scrollToBottom()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.layoutTyping.visibility = if (isLoading) View.VISIBLE else View.GONE
                if (isLoading) scrollToBottom()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastBotResponse.collectLatest { response ->
                if (response != null && viewModel.shouldSpeak) {
                    speakResponse(response)
                }
            }
        }
    }

    private fun scrollToBottom() {
        binding.rvMessages.post {
            val itemCount = adapter.itemCount
            if (itemCount > 0) {
                binding.rvMessages.smoothScrollToPosition(itemCount - 1)
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_AUDIO
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        _binding = null
    }
}
