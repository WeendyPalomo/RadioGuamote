package ec.edu.puce.lavozguamote.ui.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.FragmentEventsBinding
import ec.edu.puce.lavozguamote.ui.adapters.EventosAdapter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventsViewModel by viewModels()
    private lateinit var eventosAdapter: EventosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()

        viewModel.loadEventos()
    }

    private fun setupRecyclerView() {
        eventosAdapter = EventosAdapter(
            onItemClick = { evento ->
                // Navegar al detalle
            },
            onLikeClick = { evento ->
                viewModel.toggleLike(evento)
                Toast.makeText(context, "¡Like!", Toast.LENGTH_SHORT).show()
            },
            onShareClick = { evento ->
                shareEvento(evento)
            }
        )

        binding.rvEventos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventosAdapter
            setHasFixedSize(true)
        }
    }

    private fun shareEvento(evento: ec.edu.puce.lavozguamote.data.models.Evento) {
        val shareText = """
            🎉 ${evento.titulo}
            
            📅 ${evento.fechaEvento}
            📍 ${evento.direccion ?: "Por confirmar"}
            💰 ${if (evento.precio > 0) "$${String.format("%.2f", evento.precio)}" else "GRATIS"}
            
            ${evento.descripcion ?: ""}
            
            📻 La Voz de Guamote - La voz del pueblo
        """.trimIndent()
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, evento.titulo)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir evento"))
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadEventos()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventos.collect { eventos ->
                eventosAdapter.submitList(eventos)
                binding.tvEmptyState.visibility =
                    if (eventos.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.swipeRefresh.isRefreshing = loading
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
