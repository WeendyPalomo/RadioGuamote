package ec.edu.puce.lavozguamote.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.FragmentHomeBinding
import ec.edu.puce.lavozguamote.ui.MainActivity

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding

    companion object {
        private const val FACEBOOK_URL = "https://www.facebook.com/lavozdeguamote"
        private const val TIKTOK_URL = "https://www.tiktok.com/@lavozdeguamote"
        private const val INSTAGRAM_URL = "https://www.instagram.com/lavozdeguamote_/"
        private const val YOUTUBE_URL = "https://www.youtube.com/channel/UCc1y6KJ8ltYBIPNTedezlAQ"
        
        // Logo de La Voz de Guamote
        private const val LOGO_URL = "https://jesuitas.ec/wp-content/uploads/2024/02/LVG-PRODUCCIONES-01.png"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadLogo()
        setupRadioPlayer()
        setupSocialButtons()
    }

    override fun onResume() {
        super.onResume()
        updatePlayButtonState()
    }

    private fun loadLogo() {
        // Cargar logo desde URL
        binding?.ivLogoGrande?.let { imageView ->
            Glide.with(this)
                .load(LOGO_URL)
                .placeholder(R.drawable.ic_logo_guamote)
                .error(R.drawable.ic_logo_guamote)
                .into(imageView)
        }
    }

    private fun setupRadioPlayer() {
        binding?.btnPlayPause?.setOnClickListener {
            toggleRadio()
        }
        updatePlayButtonState()
    }

    private fun toggleRadio() {
        val activity = requireActivity() as MainActivity
        
        if (activity.isRadioPlaying()) {
            activity.pauseRadioFromHome()
        } else {
            activity.startRadioFromHome()
        }
        updatePlayButtonState()
    }

    private fun updatePlayButtonState() {
        val activity = requireActivity() as? MainActivity
        val isPlaying = activity?.isRadioPlaying() ?: false
        
        binding?.btnPlayPause?.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun setupSocialButtons() {
        binding?.btnFacebook?.setOnClickListener {
            openUrl(FACEBOOK_URL)
        }

        binding?.btnTiktok?.setOnClickListener {
            openUrl(TIKTOK_URL)
        }

        binding?.btnInstagram?.setOnClickListener {
            openUrl(INSTAGRAM_URL)
        }

        binding?.btnYoutube?.setOnClickListener {
            openUrl(YOUTUBE_URL)
        }
    }

    private fun openUrl(url: String) {
        try {
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
