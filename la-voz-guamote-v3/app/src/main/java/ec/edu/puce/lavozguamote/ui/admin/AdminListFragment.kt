package ec.edu.puce.lavozguamote.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.data.local.ApiResult
import ec.edu.puce.lavozguamote.data.local.DataManager
import ec.edu.puce.lavozguamote.data.models.Anuncio
import ec.edu.puce.lavozguamote.data.models.Libro
import ec.edu.puce.lavozguamote.data.models.Podcast
import ec.edu.puce.lavozguamote.databinding.FragmentAdminListBinding
import ec.edu.puce.lavozguamote.databinding.ItemAdminBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AdminListFragment : Fragment() {

    private var _binding: FragmentAdminListBinding? = null
    private val binding get() = _binding

    @Inject
    lateinit var dataManager: DataManager

    private var contentType: String = "anuncios"
    private lateinit var adapter: AdminItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminListBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentType = arguments?.getString("type") ?: "anuncios"
        val title = arguments?.getString("title") ?: "Gestionar"

        binding?.toolbar?.title = title
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupToolbar() {
        binding?.toolbar?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminItemAdapter(
            contentType = contentType,
            onEditClick = { item -> navigateToEdit(item) },
            onDeleteClick = { item -> showDeleteDialog(item) },
            onActivateUsersClick = { item -> navigateToActivateUsers(item) }
        )

        binding?.rvItems?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminListFragment.adapter
        }
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Verificar binding antes de continuar
                if (_binding == null) return@launch
                
                val items = when (contentType) {
                    "anuncios" -> dataManager.getAnuncios().map { it.toAdminItem() }
                    "podcasts" -> dataManager.getPodcasts().map { it.toAdminItem() }
                    "libros" -> dataManager.getLibros().map { it.toAdminItemWithExtra() }
                    else -> emptyList()
                }
                
                // Verificar binding después de llamadas asíncronas
                if (_binding == null) return@launch
                
                adapter.submitList(items)
                updateEmptyState(items.isEmpty())
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    updateEmptyState(true)
                }
            }
        }
    }

    private fun setupFab() {
        binding?.fabAdd?.setOnClickListener {
            navigateToEdit(null)
        }
    }

    private fun navigateToEdit(item: AdminItem?) {
        val bundle = Bundle().apply {
            putString("type", contentType)
            putInt("itemId", item?.id ?: -1)
            putString("title", if (item == null) "Nuevo" else "Editar")
        }
        findNavController().navigate(R.id.action_adminList_to_adminEdit, bundle)
    }

    private fun navigateToActivateUsers(item: AdminItem) {
        val bundle = Bundle().apply {
            putInt("bookId", item.id)
            putString("bookTitle", item.titulo)
            putString("bookAuthor", item.extraInfo ?: "")
            putString("bookImageUrl", item.imagenUrl)
        }
        findNavController().navigate(R.id.adminActivateBookFragment, bundle)
    }

    private fun showDeleteDialog(item: AdminItem) {
        context?.let { ctx ->
            MaterialAlertDialogBuilder(ctx)
                .setTitle("Eliminar")
                .setMessage("¿Estás seguro de eliminar \"${item.titulo}\"?")
                .setPositiveButton("Eliminar") { _, _ ->
                    deleteItem(item)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun deleteItem(item: AdminItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = when (contentType) {
                "anuncios" -> dataManager.deleteAnuncio(item.id)
                "podcasts" -> dataManager.deletePodcast(item.id)
                "libros" -> dataManager.deleteLibro(item.id)
                else -> ApiResult.Error("Tipo no válido")
            }
            
            if (_binding == null) return@launch
            
            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                    loadData()
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        _binding?.let { b ->
            b.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            b.rvItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Modelo unificado para mostrar en lista
data class AdminItem(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val imagenUrl: String?,
    val fecha: String,
    val extraInfo: String? = null // Para autor de libros
)

// Extensiones para convertir modelos a AdminItem
fun Anuncio.toAdminItem() = AdminItem(
    id = idAnuncio,
    titulo = titulo,
    descripcion = descripcion ?: "",
    imagenUrl = imagenUrl,
    fecha = fechaCreacion.formatDate()
)

fun Podcast.toAdminItem() = AdminItem(
    id = idPodcast,
    titulo = titulo,
    descripcion = "${duracion ?: ""} - ${descripcion ?: ""}".trim(' ', '-'),
    imagenUrl = imagenUrl,
    fecha = fechaPublicacion.formatDate()
)

fun Libro.toAdminItem() = AdminItem(
    id = idLibro,
    titulo = titulo,
    descripcion = "Por ${autor ?: "Anónimo"} - ${descripcion ?: ""}".trim(' ', '-'),
    imagenUrl = imagenPortada,
    fecha = ""
)

fun Libro.toAdminItemWithExtra() = AdminItem(
    id = idLibro,
    titulo = titulo,
    descripcion = "Por ${autor ?: "Anónimo"} - ${descripcion ?: ""}".trim(' ', '-'),
    imagenUrl = imagenPortada,
    fecha = "",
    extraInfo = autor
)

private fun String.formatDate(): String {
    if (this.isEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es"))
        val date = inputFormat.parse(this)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        this.take(10)
    }
}

// Adapter
class AdminItemAdapter(
    private val contentType: String = "",
    private val onEditClick: (AdminItem) -> Unit,
    private val onDeleteClick: (AdminItem) -> Unit,
    private val onActivateUsersClick: ((AdminItem) -> Unit)? = null
) : RecyclerView.Adapter<AdminItemAdapter.ViewHolder>() {

    private var items: List<AdminItem> = emptyList()

    fun submitList(newItems: List<AdminItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminItem) {
            binding.tvTitulo.text = item.titulo
            binding.tvDescripcion.text = item.descripcion
            binding.tvFecha.text = item.fecha

            if (!item.imagenUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(item.imagenUrl)
                    .placeholder(R.drawable.placeholder_news)
                    .into(binding.ivImagen)
            } else {
                binding.ivImagen.setImageResource(R.drawable.placeholder_news)
            }

            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }

            // Mostrar botón de activar usuarios solo para libros
            if (contentType == "libros" && onActivateUsersClick != null) {
                binding.btnActivateUsers.visibility = View.VISIBLE
                binding.btnActivateUsers.setOnClickListener { onActivateUsersClick.invoke(item) }
            } else {
                binding.btnActivateUsers.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
