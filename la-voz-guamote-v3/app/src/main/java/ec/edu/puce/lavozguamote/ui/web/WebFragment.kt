package ec.edu.puce.lavozguamote.ui.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.databinding.FragmentWebBinding

@AndroidEntryPoint
class WebFragment : Fragment() {

    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding

    private val webUrl = "https://lavozdeguamote.org/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        setupRetryButton()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding?.webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Verificar que el binding sigue activo
                    _binding?.let { b ->
                        b.progressBar.visibility = View.GONE
                        b.errorView.visibility = View.GONE
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    // Verificar que el binding sigue activo
                    _binding?.let { b ->
                        b.progressBar.visibility = View.GONE
                        b.errorView.visibility = View.VISIBLE
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    // Verificar que el binding sigue activo antes de acceder
                    _binding?.let { b ->
                        if (newProgress < 100) {
                            b.progressBar.visibility = View.VISIBLE
                        } else {
                            b.progressBar.visibility = View.GONE
                        }
                    }
                }
            }

            loadUrl(webUrl)
        }
    }

    private fun setupRetryButton() {
        binding?.btnRetry?.setOnClickListener {
            binding?.errorView?.visibility = View.GONE
            binding?.progressBar?.visibility = View.VISIBLE
            binding?.webView?.loadUrl(webUrl)
        }
    }

    override fun onDestroyView() {
        // Detener el WebView antes de destruir el binding
        _binding?.webView?.apply {
            stopLoading()
            webChromeClient = null
        }
        super.onDestroyView()
        _binding = null
    }
}
