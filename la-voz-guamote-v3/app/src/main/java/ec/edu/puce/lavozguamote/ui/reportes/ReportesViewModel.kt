package ec.edu.puce.lavozguamote.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportesViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _envioExitoso = MutableStateFlow(false)
    val envioExitoso: StateFlow<Boolean> = _envioExitoso.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _misReportes = MutableStateFlow<List<Reporte>>(emptyList())
    val misReportes: StateFlow<List<Reporte>> = _misReportes.asStateFlow()

    fun enviarReporte(
        tipo: String,
        titulo: String,
        descripcion: String,
        ubicacion: String?,
        latitud: Double?,
        longitud: Double?,
        imagenUrl: String? = null,
        audioUrl: String? = null,
        esAnonimo: Boolean
    ) {
        _isLoading.value = true
        _envioExitoso.value = false
        _error.value = null

        viewModelScope.launch {
            try {
                // Simular envío al servidor
                delay(1500)
                
                // TODO: Implementar llamada real a la API
                // val response = apiService.crearReporte(CrearReporteRequest(...))
                
                _envioExitoso.value = true
            } catch (e: Exception) {
                _error.value = "Error al enviar el reporte: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarMisReportes() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                delay(1000)
                
                // TODO: Implementar llamada real a la API
                // val response = apiService.getMisReportes()
                
                _misReportes.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Error al cargar reportes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class Reporte(
    val id: Int,
    val tipo: String,
    val titulo: String,
    val descripcion: String,
    val ubicacion: String?,
    val imagenUrl: String?,
    val estado: String,
    val fechaReporte: String
)
