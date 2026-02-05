package ec.edu.puce.lavozguamote.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.data.local.ApiResult
import ec.edu.puce.lavozguamote.data.local.DataManager
import ec.edu.puce.lavozguamote.databinding.FragmentAdminEditBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminEditFragment : Fragment() {

    private var _binding: FragmentAdminEditBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dataManager: DataManager

    private var contentType: String = "anuncios"
    private var itemId: Int = -1
    private var selectedImageUri: Uri? = null
    private var currentImageBase64: String? = null
    
    // Para libros: guardar el estado de esGratis
    private var currentEsGratis: Boolean = false

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                showImagePreview(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentType = arguments?.getString("type") ?: "anuncios"
        itemId = arguments?.getInt("itemId") ?: -1
        val title = arguments?.getString("title") ?: "Nuevo"

        setupToolbar(title)
        setupFieldsVisibility()
        setupListeners()

        if (itemId != -1) {
            loadItemData()
        }
    }

    private fun setupToolbar(title: String) {
        val typeName = when (contentType) {
            "anuncios" -> "Anuncio"
            "podcasts" -> "Podcast"
            "libros" -> "Libro"
            else -> ""
        }
        binding.toolbar.title = "$title $typeName"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupFieldsVisibility() {
        // Ocultar todos los campos especificos primero
        binding.tilEnlace.visibility = View.GONE
        binding.tilSpotifyUrl.visibility = View.GONE
        binding.tilDuracion.visibility = View.GONE
        binding.tilAutor.visibility = View.GONE
        binding.tilPdfUrl.visibility = View.GONE
        binding.cardLibroGratis.visibility = View.GONE

        // Mostrar campos segun el tipo
        when (contentType) {
            "anuncios" -> {
                binding.tilEnlace.visibility = View.VISIBLE
            }
            "podcasts" -> {
                binding.tilSpotifyUrl.visibility = View.VISIBLE
                binding.tilDuracion.visibility = View.VISIBLE
            }
            "libros" -> {
                binding.tilAutor.visibility = View.VISIBLE
                binding.tilPdfUrl.visibility = View.VISIBLE
                binding.cardLibroGratis.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnRemoveImage.setOnClickListener {
            removeImage()
        }

        binding.btnSave.setOnClickListener {
            saveItem()
        }
        
        // Listener para el checkbox de libro gratis
        binding.cbLibroGratis.setOnCheckedChangeListener { _, isChecked ->
            currentEsGratis = isChecked
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun showImagePreview(uri: Uri) {
        binding.ivPreview.visibility = View.VISIBLE
        binding.tvNoImage.visibility = View.GONE
        binding.btnRemoveImage.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .into(binding.ivPreview)
    }

    private fun showImagePreview(url: String) {
        binding.ivPreview.visibility = View.VISIBLE
        binding.tvNoImage.visibility = View.GONE
        binding.btnRemoveImage.visibility = View.VISIBLE

        Glide.with(this)
            .load(url)
            .into(binding.ivPreview)
    }

    private fun removeImage() {
        selectedImageUri = null
        currentImageBase64 = null
        binding.ivPreview.visibility = View.GONE
        binding.tvNoImage.visibility = View.VISIBLE
        binding.btnRemoveImage.visibility = View.GONE
    }

    private fun loadItemData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                when (contentType) {
                    "anuncios" -> loadAnuncio()
                    "podcasts" -> loadPodcast()
                    "libros" -> loadLibro()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadAnuncio() {
        val anuncios = dataManager.getAnuncios()
        val anuncio = anuncios.find { it.idAnuncio == itemId }
        anuncio?.let {
            binding.etTitulo.setText(it.titulo)
            binding.etDescripcion.setText(it.descripcion)
            binding.etEnlace.setText(it.enlaceUrl)
            it.imagenUrl?.let { url ->
                showImagePreview(url)
            }
        }
    }

    private suspend fun loadPodcast() {
        val podcasts = dataManager.getPodcasts()
        val podcast = podcasts.find { it.idPodcast == itemId }
        podcast?.let {
            binding.etTitulo.setText(it.titulo)
            binding.etDescripcion.setText(it.descripcion)
            binding.etSpotifyUrl.setText(it.spotifyUrl)
            binding.etDuracion.setText(it.duracion)
            it.imagenUrl?.let { url ->
                showImagePreview(url)
            }
        }
    }

    private suspend fun loadLibro() {
        val libros = dataManager.getLibros()
        val libro = libros.find { it.idLibro == itemId }
        libro?.let {
            binding.etTitulo.setText(it.titulo)
            binding.etDescripcion.setText(it.descripcion)
            binding.etAutor.setText(it.autor)
            binding.etPdfUrl.setText(it.pdfUrl)
            
            // Cargar estado de esGratis
            currentEsGratis = it.esGratis
            binding.cbLibroGratis.isChecked = it.esGratis
            
            it.imagenPortada?.let { url ->
                showImagePreview(url)
            }
        }
    }

    private fun saveItem() {
        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (titulo.isEmpty()) {
            binding.tilTitulo.error = "El titulo es requerido"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Convertir imagen si se selecciono una nueva
                val imagenBase64 = selectedImageUri?.let {
                    dataManager.uriToBase64(it)
                }

                val result = when (contentType) {
                    "anuncios" -> saveAnuncio(titulo, descripcion, imagenBase64)
                    "podcasts" -> savePodcast(titulo, descripcion, imagenBase64)
                    "libros" -> saveLibro(titulo, descripcion, imagenBase64)
                    else -> ApiResult.Error("Tipo no valido")
                }

                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true

                when (result) {
                    is ApiResult.Success -> {
                        Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is ApiResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveAnuncio(titulo: String, descripcion: String, imagenBase64: String?): ApiResult<*> {
        val enlaceUrl = binding.etEnlace.text.toString().trim().ifEmpty { null }

        return if (itemId == -1) {
            dataManager.createAnuncio(titulo, descripcion, imagenBase64, enlaceUrl)
        } else {
            dataManager.updateAnuncio(itemId, titulo, descripcion, imagenBase64, enlaceUrl)
        }
    }

    private suspend fun savePodcast(titulo: String, descripcion: String, imagenBase64: String?): ApiResult<*> {
        val spotifyUrl = binding.etSpotifyUrl.text.toString().trim().ifEmpty { null }
        val duracion = binding.etDuracion.text.toString().trim().ifEmpty { null }

        return if (itemId == -1) {
            dataManager.createPodcast(titulo, descripcion, imagenBase64, spotifyUrl, duracion)
        } else {
            dataManager.updatePodcast(itemId, titulo, descripcion, imagenBase64, spotifyUrl, duracion)
        }
    }

    private suspend fun saveLibro(titulo: String, descripcion: String, imagenBase64: String?): ApiResult<*> {
        val autor = binding.etAutor.text.toString().trim().ifEmpty { null }
        val pdfUrl = binding.etPdfUrl.text.toString().trim().ifEmpty { null }
        
        // Obtener el valor del checkbox
        val esGratis = binding.cbLibroGratis.isChecked

        return if (itemId == -1) {
            dataManager.createLibro(titulo, autor, descripcion, imagenBase64, pdfUrl, esGratis)
        } else {
            dataManager.updateLibro(itemId, titulo, autor, descripcion, imagenBase64, pdfUrl, esGratis)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
