package ec.edu.puce.lavozguamote.ui.contactos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.databinding.FragmentContactosBinding

@AndroidEntryPoint
class ContactosFragment : Fragment() {

    private var _binding: FragmentContactosBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val EMAIL = "lavozdeguamote@faig.ec"
        private const val CELULAR1 = "0989432206"
        private const val CELULAR2 = "0991280714"
        private const val GOOGLE_MAPS_URL = "https://maps.google.com/?q=Radio+La+Voz+de+Guamote"
        private const val WHATSAPP_NUMBER = "593989432206"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Abrir Google Maps
        binding.cardMapa.setOnClickListener {
            openGoogleMaps()
        }

        // Enviar email
        binding.layoutEmail.setOnClickListener {
            sendEmail()
        }

        // Llamar
        binding.layoutCelular1.setOnClickListener {
            makeCall(CELULAR1)
        }

        // Botón WhatsApp
        binding.btnWhatsapp.setOnClickListener {
            openWhatsApp()
        }

        // Botón Llamar
        binding.btnLlamar.setOnClickListener {
            makeCall(CELULAR1)
        }
    }

    private fun openGoogleMaps() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_MAPS_URL))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, "Contacto desde la App")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun makeCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openWhatsApp() {
        try {
            val url = "https://wa.me/$WHATSAPP_NUMBER?text=Hola,%20contacto%20desde%20la%20app%20La%20Voz%20de%20Guamote"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
