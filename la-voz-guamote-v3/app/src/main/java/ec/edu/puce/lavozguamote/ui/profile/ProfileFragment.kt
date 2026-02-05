package ec.edu.puce.lavozguamote.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.FragmentProfileBinding
import ec.edu.puce.lavozguamote.ui.auth.AuthActivity
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDonacionesSection()
        setupConfiguraciones()
        setupObservers()
        loadLogoPuce()
    }

    private fun loadLogoPuce() {
        // Cargar logo de la PUCE
        binding?.ivLogoPuce?.let { imageView ->
            Glide.with(this)
                .load("https://universidadesecuador.com/assets/images/universidades-privadas/puce.jpg")
                .into(imageView)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLoginStatus()
    }

    private fun setupDonacionesSection() {
        binding?.apply {
            tvDonacionBanco.text = "Banco Guayaquil"
            tvDonacionNombre.text = "Fundación Acción Integral Guamote"
            tvDonacionRuc.text = "RUC: 0691709523001"
            tvDonacionCuenta.text = "Cuenta Corriente: 0006913857"
            tvDonacionTelefono.text = "Teléfono: 0995224384"
            tvDonacionEmail.text = "fundacionaig@faig.ec"

            btnWhatsappDonacion.setOnClickListener {
                val mensaje = "Hola, quiero realizar una donación a La Voz de Guamote"
                val url = "https://wa.me/593995224384?text=${Uri.encode(mensaje)}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            btnEmailDonacion.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:fundacionaig@faig.ec")
                    putExtra(Intent.EXTRA_SUBJECT, "Donación - La Voz de Guamote")
                }
                startActivity(intent)
            }

            btnLlamarDonacion.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0995224384"))
                startActivity(intent)
            }

            btnCopiarCuenta.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Cuenta", "0006913857")
                clipboard.setPrimaryClip(clip)
                Snackbar.make(root, "Número de cuenta copiado", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupConfiguraciones() {
        binding?.apply {
            btnEditarPerfil.setOnClickListener {
                Snackbar.make(root, "Función próximamente", Snackbar.LENGTH_SHORT).show()
            }

            // Botón Mis Libros - Navegar a la sección de libros
            btnMisLibros.setOnClickListener {
                showMisLibrosDialog()
            }

            // ELIMINADO: btnMisDonaciones

            switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setNotificaciones(isChecked)
            }

            btnCerrarSesion.setOnClickListener {
                showLogoutDialog()
            }

            btnIniciarSesion.setOnClickListener {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
            }

            btnRegistrarse.setOnClickListener {
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.putExtra("mode", "register")
                startActivity(intent)
            }

            // Botón de Administrador
            btnAdmin.setOnClickListener {
                findNavController().navigate(R.id.adminPanelFragment)
            }
        }
    }

    private fun showMisLibrosDialog() {
        // Cargar los libros del usuario
        viewLifecycleOwner.lifecycleScope.launch {
            binding?.progressBar?.visibility = View.VISIBLE
            
            try {
                // Cargar libros y esperar
                viewModel.loadMisLibros()
                
                // Pequeña espera para que se carguen los datos
                kotlinx.coroutines.delay(500)
                
                binding?.progressBar?.visibility = View.GONE
                
                val libros = viewModel.misLibros.value
                
                if (libros.isEmpty()) {
                    context?.let { ctx ->
                        MaterialAlertDialogBuilder(ctx)
                            .setTitle("Mis Libros")
                            .setMessage("No tienes libros activados.\n\nPuedes adquirir libros en la sección de Libros o contactar al administrador para activar uno.")
                            .setPositiveButton("Ir a Libros") { _, _ ->
                                findNavController().navigate(R.id.booksFragment)
                            }
                            .setNegativeButton("Cerrar", null)
                            .show()
                    }
                } else {
                    // Mostrar lista de libros
                    val librosTitulos = libros.map { libro ->
                        "${libro.titulo}\n   Autor: ${libro.autor ?: "No especificado"}"
                    }.toTypedArray()
                    
                    context?.let { ctx ->
                        MaterialAlertDialogBuilder(ctx)
                            .setTitle("Mis Libros (${libros.size})")
                            .setItems(librosTitulos) { _, which ->
                                val libroSeleccionado = libros[which]
                                // Abrir el libro si tiene PDF
                                if (!libroSeleccionado.pdfUrl.isNullOrEmpty()) {
                                    abrirPdf(libroSeleccionado.pdfUrl!!)
                                } else {
                                    Snackbar.make(
                                        binding?.root ?: return@setItems,
                                        "Este libro no tiene PDF disponible",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setPositiveButton("Cerrar", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                binding?.progressBar?.visibility = View.GONE
                binding?.root?.let {
                    Snackbar.make(it, "Error al cargar libros: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun abrirPdf(url: String) {
        // Convertir URL de Google Drive si es necesario
        val finalUrl = convertGoogleDriveUrl(url)
        
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            startActivity(browserIntent)
        } catch (e: Exception) {
            binding?.root?.let {
                Snackbar.make(it, "No se pudo abrir el PDF", Snackbar.LENGTH_SHORT).show()
            }
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
        
        return url
    }

    private fun showLogoutDialog() {
        context?.let { ctx ->
            MaterialAlertDialogBuilder(ctx)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    viewModel.logout()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.usuario.collect { usuario ->
                if (usuario != null) {
                    binding?.layoutLoggedIn?.visibility = View.VISIBLE
                    binding?.layoutNotLoggedIn?.visibility = View.GONE

                    binding?.tvNombreUsuario?.text = usuario.nombreCompleto
                    binding?.tvEmailUsuario?.text = usuario.email

                    if (!usuario.fotoPerfil.isNullOrEmpty()) {
                        binding?.ivFotoPerfil?.let { imageView ->
                            Glide.with(this@ProfileFragment)
                                .load(usuario.fotoPerfil)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(imageView)
                        }
                    }
                } else {
                    binding?.layoutLoggedIn?.visibility = View.GONE
                    binding?.layoutNotLoggedIn?.visibility = View.VISIBLE
                }
            }
        }

        // Observar si es administrador
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isAdmin.collect { isAdmin ->
                binding?.btnAdmin?.visibility = if (isAdmin) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding?.progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
