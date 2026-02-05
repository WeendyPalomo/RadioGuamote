package ec.edu.puce.lavozguamote.ui.books

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.data.models.Libro
import ec.edu.puce.lavozguamote.databinding.FragmentBooksBinding
import ec.edu.puce.lavozguamote.ui.adapters.LibrosAdapter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BooksFragment : Fragment() {

    private var _binding: FragmentBooksBinding? = null
    private val binding get() = _binding

    private val viewModel: BooksViewModel by viewModels()
    private lateinit var librosAdapter: LibrosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()

        viewModel.loadLibros()
    }

    private fun setupRecyclerView() {
        librosAdapter = LibrosAdapter(
            onItemClick = { libro ->
                handleLibroClick(libro)
            },
            onComprarClick = { libro ->
                // Boton Solicitar - siempre envia a WhatsApp
                enviarWhatsApp(libro)
            }
        )

        binding?.rvLibros?.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = librosAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleLibroClick(libro: Libro) {
        // VERIFICACION ESTRICTA: Solo puede leer si esGratis=true O comprado=true
        // NO se usa precio == 0.0 como condicion
        val tieneAcceso = libro.esGratis || libro.comprado
        
        if (tieneAcceso) {
            // Tiene acceso, intentar abrir PDF
            if (!libro.pdfUrl.isNullOrEmpty()) {
                abrirPdf(libro.pdfUrl!!)
            } else {
                Snackbar.make(
                    binding?.root ?: return,
                    "Verificando acceso...",
                    Snackbar.LENGTH_SHORT
                ).show()
                
                viewModel.loadLibros()
                
                viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(1500)
                    val libroActualizado = viewModel.libros.value.find { it.idLibro == libro.idLibro }
                    // Verificar acceso de nuevo antes de abrir
                    val tieneAccesoActualizado = libroActualizado?.esGratis == true || libroActualizado?.comprado == true
                    if (libroActualizado != null && tieneAccesoActualizado && !libroActualizado.pdfUrl.isNullOrEmpty()) {
                        abrirPdf(libroActualizado.pdfUrl!!)
                    } else {
                        Snackbar.make(
                            binding?.root ?: return@launch,
                            "No hay PDF disponible para este libro",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            // NO tiene acceso - enviar a WhatsApp para solicitar
            enviarWhatsApp(libro)
        }
    }

    private fun abrirPdf(url: String) {
        val finalUrl = convertGoogleDriveUrl(url)
        
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            startActivity(browserIntent)
        } catch (e: Exception) {
            Snackbar.make(
                binding?.root ?: return,
                "No se pudo abrir el PDF",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    private fun convertGoogleDriveUrl(url: String): String {
        val pattern1 = Regex("drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)")
        val match1 = pattern1.find(url)
        if (match1 != null) {
            val fileId = match1.groupValues[1]
            return "https://drive.google.com/file/d/$fileId/preview"
        }
        
        val pattern2 = Regex("drive\\.google\\.com/open\\?id=([a-zA-Z0-9_-]+)")
        val match2 = pattern2.find(url)
        if (match2 != null) {
            val fileId = match2.groupValues[1]
            return "https://drive.google.com/file/d/$fileId/preview"
        }
        
        val pattern3 = Regex("drive\\.google\\.com/uc\\?.*id=([a-zA-Z0-9_-]+)")
        val match3 = pattern3.find(url)
        if (match3 != null) {
            val fileId = match3.groupValues[1]
            return "https://drive.google.com/file/d/$fileId/preview"
        }
        
        return url
    }

    private fun setupSwipeRefresh() {
        binding?.swipeRefresh?.setColorSchemeResources(R.color.primary)
        binding?.swipeRefresh?.setOnRefreshListener {
            viewModel.loadLibros()
        }
    }

    private fun enviarWhatsApp(libro: Libro) {
        val mensaje = "Hola, estoy interesado en el libro \"${libro.titulo}\". Me gustaria obtener mas informacion."

        try {
            val url = "https://wa.me/593989432206?text=${Uri.encode(mensaje)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            binding?.root?.let {
                Snackbar.make(it, "No se pudo abrir WhatsApp", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.libros.collect { libros ->
                librosAdapter.submitList(libros)
                binding?.tvEmptyState?.visibility =
                    if (libros.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding?.swipeRefresh?.isRefreshing = loading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mensaje.collect { mensaje ->
                mensaje?.let {
                    binding?.root?.let { root ->
                        Snackbar.make(root, it, Snackbar.LENGTH_LONG).show()
                    }
                    viewModel.clearMensaje()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLibros()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
