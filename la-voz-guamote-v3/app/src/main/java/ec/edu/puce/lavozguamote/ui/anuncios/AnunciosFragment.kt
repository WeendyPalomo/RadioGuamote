package ec.edu.puce.lavozguamote.ui.anuncios

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.data.local.DataManager
import ec.edu.puce.lavozguamote.data.models.Anuncio
import ec.edu.puce.lavozguamote.databinding.FragmentAnunciosBinding
import ec.edu.puce.lavozguamote.databinding.ItemAnuncioBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AnunciosFragment : Fragment() {

    private var _binding: FragmentAnunciosBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dataManager: DataManager

    private lateinit var adapter: AnunciosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnunciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadAnuncios()
    }

    private fun setupRecyclerView() {
        adapter = AnunciosAdapter { anuncio ->
            // Al hacer click, abrir enlace si existe
            anuncio.enlaceUrl?.let { url ->
                if (url.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se puede abrir el enlace", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.rvAnuncios.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@AnunciosFragment.adapter
        }
    }

    private fun loadAnuncios() {
        // Mostrar estado de carga
        binding.tvEmptyState.visibility = View.GONE
        binding.rvAnuncios.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val anuncios = dataManager.getAnuncios()
                
                adapter.submitList(anuncios)
                
                // Mostrar estado vacío si no hay anuncios
                if (anuncios.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvAnuncios.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvAnuncios.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar anuncios", Toast.LENGTH_SHORT).show()
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvAnuncios.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar anuncios al volver a la pantalla
        loadAnuncios()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Adapter para mostrar anuncios
class AnunciosAdapter(
    private val onAnuncioClick: (Anuncio) -> Unit
) : RecyclerView.Adapter<AnunciosAdapter.AnuncioViewHolder>() {

    private var anuncios: List<Anuncio> = emptyList()

    fun submitList(newAnuncios: List<Anuncio>) {
        anuncios = newAnuncios
        notifyDataSetChanged()
    }

    inner class AnuncioViewHolder(private val binding: ItemAnuncioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(anuncio: Anuncio) {
            // Cargar imagen del anuncio
            if (!anuncio.imagenUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(anuncio.imagenUrl)
                    .placeholder(R.drawable.placeholder_anuncio)
                    .error(R.drawable.placeholder_anuncio)
                    .into(binding.ivAnuncio)
            } else {
                binding.ivAnuncio.setImageResource(R.drawable.placeholder_anuncio)
            }

            binding.root.setOnClickListener {
                onAnuncioClick(anuncio)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnuncioViewHolder {
        val binding = ItemAnuncioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AnuncioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnuncioViewHolder, position: Int) {
        holder.bind(anuncios[position])
    }

    override fun getItemCount() = anuncios.size
}
