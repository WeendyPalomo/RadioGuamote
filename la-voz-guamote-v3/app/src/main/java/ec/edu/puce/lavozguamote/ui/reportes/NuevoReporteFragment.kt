package ec.edu.puce.lavozguamote.ui.reportes

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.FragmentNuevoReporteBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NuevoReporteFragment : Fragment() {

    private var _binding: FragmentNuevoReporteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportesViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedTipo = "AVISO"
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevoReporteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        setupChipGroup()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupChipGroup() {
        binding.chipGroupTipo.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                selectedTipo = when (chip?.id) {
                    R.id.chipDenuncia -> "DENUNCIA"
                    R.id.chipSaludo -> "SALUDO"
                    R.id.chipAviso -> "AVISO"
                    R.id.chipEmergencia -> "EMERGENCIA"
                    else -> "AVISO"
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnUseGps.setOnClickListener {
            requestLocationPermission()
        }

        binding.cardAddPhoto.setOnClickListener {
            Toast.makeText(context, "Función de cámara próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.cardAddAudio.setOnClickListener {
            Toast.makeText(context, "Función de audio próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnEnviar.setOnClickListener {
            enviarReporte()
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    private fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLatitude = it.latitude
                        currentLongitude = it.longitude
                        binding.etUbicacion.setText("Lat: ${it.latitude}, Lng: ${it.longitude}")
                        Toast.makeText(context, "Ubicación obtenida", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarReporte() {
        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val ubicacion = binding.etUbicacion.text.toString().trim()
        val esAnonimo = binding.switchAnonimo.isChecked

        // Validaciones
        if (titulo.isEmpty()) {
            binding.tilTitulo.error = "El título es requerido"
            return
        }
        binding.tilTitulo.error = null

        if (descripcion.isEmpty()) {
            binding.tilDescripcion.error = "La descripción es requerida"
            return
        }
        binding.tilDescripcion.error = null

        viewModel.enviarReporte(
            tipo = selectedTipo,
            titulo = titulo,
            descripcion = descripcion,
            ubicacion = ubicacion.ifEmpty { null },
            latitud = currentLatitude,
            longitud = currentLongitude,
            esAnonimo = esAnonimo
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.envioExitoso.collectLatest { exitoso ->
                if (exitoso) {
                    Toast.makeText(context, getString(R.string.report_success), Toast.LENGTH_LONG).show()
                    limpiarFormulario()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.btnEnviar.isEnabled = !isLoading
                binding.btnEnviar.text = if (isLoading) getString(R.string.loading) else getString(R.string.report_send)
            }
        }
    }

    private fun limpiarFormulario() {
        binding.etTitulo.text?.clear()
        binding.etDescripcion.text?.clear()
        binding.etUbicacion.text?.clear()
        binding.switchAnonimo.isChecked = false
        binding.chipAviso.isChecked = true
        currentLatitude = null
        currentLongitude = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
