package ec.edu.puce.lavozguamote.ui.podcasts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.data.models.Podcast
import ec.edu.puce.lavozguamote.databinding.FragmentPodcastsBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PodcastsFragment : Fragment() {

    private var _binding: FragmentPodcastsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PodcastsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPodcastsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupObservers()

        viewModel.loadPodcasts()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPodcasts()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.podcasts.collect { podcasts ->
                displayPodcasts(podcasts)
                binding.tvEmptyState.visibility =
                    if (podcasts.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.swipeRefresh.isRefreshing = loading
            }
        }
    }

    private fun displayPodcasts(podcasts: List<Podcast>) {
        binding.podcastsContainer.removeAllViews()
        
        for (podcast in podcasts) {
            val cardView = createPodcastCard(podcast)
            binding.podcastsContainer.addView(cardView)
        }
    }

    private fun createPodcastCard(podcast: Podcast): View {
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
            cardElevation = 4f
            radius = 16f
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Header con imagen y título
        val header = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 32, 32, 16)
        }

        val imageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        
        if (!podcast.imagenUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(podcast.imagenUrl)
                .placeholder(R.drawable.ic_radio_logo)
                .centerCrop()
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_radio_logo)
        }

        val textContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(24, 0, 0, 0)
        }

        val titleView = TextView(requireContext()).apply {
            text = podcast.titulo
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_primary, null))
        }

        val dateView = TextView(requireContext()).apply {
            text = podcast.fechaPublicacion
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_secondary, null))
        }

        val descView = TextView(requireContext()).apply {
            text = podcast.descripcion ?: ""
            textSize = 14f
            maxLines = 3
            setTextColor(resources.getColor(R.color.text_secondary, null))
        }

        textContainer.addView(titleView)
        textContainer.addView(dateView)
        textContainer.addView(descView)

        header.addView(imageView)
        header.addView(textContainer)

        // WebView para reproductor de Spotify embebido
        val webView = WebView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            ).apply {
                setMargins(32, 0, 32, 32)
            }
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            
            // Cargar el reproductor embebido de Spotify
            val embedUrl = podcast.spotifyEmbedUrl ?: convertToEmbedUrl(podcast.spotifyUrl)
            if (embedUrl.isNotEmpty()) {
                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body { margin: 0; padding: 0; background: transparent; }
                            iframe { border-radius: 12px; width: 100%; height: 152px; }
                        </style>
                    </head>
                    <body>
                        <iframe src="$embedUrl" 
                            frameBorder="0" 
                            allowfullscreen="" 
                            allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture">
                        </iframe>
                    </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        }

        // Botón para abrir en Spotify
        val openSpotifyBtn = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Abrir en Spotify"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 0, 32, 32)
            }
            setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(podcast.spotifyUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    // Si no tiene Spotify, abrir en navegador
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(podcast.spotifyUrl))
                    startActivity(webIntent)
                }
            }
        }

        container.addView(header)
        container.addView(webView)
        container.addView(openSpotifyBtn)
        card.addView(container)

        return card
    }

    private fun convertToEmbedUrl(spotifyUrl: String): String {
        // Convierte URL de Spotify a URL de embed
        // https://open.spotify.com/episode/xxx -> https://open.spotify.com/embed/episode/xxx
        // https://open.spotify.com/show/xxx -> https://open.spotify.com/embed/show/xxx
        return spotifyUrl
            .replace("open.spotify.com/episode/", "open.spotify.com/embed/episode/")
            .replace("open.spotify.com/show/", "open.spotify.com/embed/show/")
            .replace("open.spotify.com/playlist/", "open.spotify.com/embed/playlist/")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
