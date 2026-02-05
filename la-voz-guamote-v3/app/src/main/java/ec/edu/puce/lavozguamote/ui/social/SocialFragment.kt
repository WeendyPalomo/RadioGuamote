package ec.edu.puce.lavozguamote.ui.social

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ec.edu.puce.lavozguamote.databinding.FragmentSocialBinding

class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val URL_YOUTUBE = "https://www.youtube.com/channel/UCc1y6KJ8ltYBIPNTedezlAQ"
        const val URL_FACEBOOK = "https://www.facebook.com/lavozdeguamote"
        const val URL_TIKTOK = "https://www.tiktok.com/@lavozdeguamote?lang=fr"
        const val URL_INSTAGRAM = "https://www.instagram.com/lavozdeguamote_/"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSocialButtons()
    }

    private fun setupSocialButtons() {
        binding.btnYoutube.setOnClickListener {
            openUrl(URL_YOUTUBE)
        }

        binding.btnFacebook.setOnClickListener {
            openUrl(URL_FACEBOOK)
        }

        binding.btnTiktok.setOnClickListener {
            openUrl(URL_TIKTOK)
        }

        binding.btnInstagram.setOnClickListener {
            openUrl(URL_INSTAGRAM)
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
