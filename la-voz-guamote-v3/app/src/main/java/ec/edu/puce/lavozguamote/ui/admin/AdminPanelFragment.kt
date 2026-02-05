package ec.edu.puce.lavozguamote.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.data.local.DataManager
import ec.edu.puce.lavozguamote.databinding.FragmentAdminBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding

    @Inject
    lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Verificar que es admin
        if (!dataManager.isAdmin()) {
            findNavController().popBackStack()
            return
        }

        setupToolbar()
        setupClickListeners()
        loadStatistics()
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
    }

    private fun setupToolbar() {
        binding?.toolbar?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        
        // Mostrar nombre del admin
        dataManager.getCurrentUser()?.let { user ->
            binding?.tvAdminName?.text = user.nombreCompleto
        }
    }

    private fun setupClickListeners() {
        // Gestionar Anuncios
        binding?.cardAnuncios?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "anuncios")
                putString("title", "Gestionar Anuncios")
            }
            findNavController().navigate(R.id.action_adminPanel_to_adminList, bundle)
        }

        // Gestionar Podcasts
        binding?.cardPodcasts?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "podcasts")
                putString("title", "Gestionar Podcasts")
            }
            findNavController().navigate(R.id.action_adminPanel_to_adminList, bundle)
        }

        // Gestionar Libros
        binding?.cardLibros?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("type", "libros")
                putString("title", "Gestionar Libros")
            }
            findNavController().navigate(R.id.action_adminPanel_to_adminList, bundle)
        }

        // Cerrar sesión
        binding?.btnLogout?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadStatistics() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Verificar que el binding no sea null antes de actualizar la UI
                if (_binding == null) return@launch
                
                val anunciosCount = dataManager.getAnunciosCount()
                val podcastsCount = dataManager.getPodcastsCount()
                val librosCount = dataManager.getLibrosCount()
                
                // Verificar de nuevo después de las llamadas asíncronas
                _binding?.let { b ->
                    b.tvCountAnuncios.text = anunciosCount.toString()
                    b.tvCountPodcasts.text = podcastsCount.toString()
                    b.tvCountLibros.text = librosCount.toString()
                }
            } catch (e: Exception) {
                _binding?.let { b ->
                    b.tvCountAnuncios.text = "0"
                    b.tvCountPodcasts.text = "0"
                    b.tvCountLibros.text = "0"
                }
            }
        }
    }

    private fun showLogoutDialog() {
        context?.let { ctx ->
            MaterialAlertDialogBuilder(ctx)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    dataManager.logout()
                    findNavController().navigate(R.id.homeFragment)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
