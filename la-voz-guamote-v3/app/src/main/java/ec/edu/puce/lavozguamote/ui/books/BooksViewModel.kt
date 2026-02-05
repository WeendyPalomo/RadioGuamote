package ec.edu.puce.lavozguamote.ui.books

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ec.edu.puce.lavozguamote.data.api.ApiService
import ec.edu.puce.lavozguamote.data.models.CompraLibroRequest
import ec.edu.puce.lavozguamote.data.models.Libro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _libros = MutableStateFlow<List<Libro>>(emptyList())
    val libros: StateFlow<List<Libro>> = _libros

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _pdfUrl = MutableStateFlow<String?>(null)
    val pdfUrl: StateFlow<String?> = _pdfUrl

    fun loadLibros() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getLibros()
                if (response.isSuccessful) {
                    _libros.value = response.body()?.data ?: emptyList()
                } else {
                    _mensaje.value = "Error al cargar libros"
                }
            } catch (e: Exception) {
                _mensaje.value = "Sin conexión al servidor"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registrarCompra(idLibro: Int, metodoPago: String, referencia: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CompraLibroRequest(idLibro, metodoPago, referencia)
                val response = apiService.comprarLibro(idLibro, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _mensaje.value = "¡Compra registrada! Recibirás confirmación cuando sea verificada."
                    loadLibros()
                } else {
                    _mensaje.value = response.body()?.message ?: "Error al registrar compra"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun abrirLibro(libro: Libro) {
        // El libro ya tiene el pdfUrl si el usuario tiene acceso (comprado = true)
        // El backend devuelve pdfUrl solo si el usuario tiene permiso
        
        if (libro.comprado && !libro.pdfUrl.isNullOrEmpty()) {
            // El usuario tiene acceso, abrir el PDF
            _pdfUrl.value = libro.pdfUrl
            
            // También intentar abrir directamente
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(libro.pdfUrl), "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Si no hay app de PDF, abrir en navegador
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(libro.pdfUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                } catch (e2: Exception) {
                    _mensaje.value = "No se pudo abrir el PDF. URL: ${libro.pdfUrl}"
                }
            }
        } else if (libro.esGratis || libro.precio == 0.0) {
            // Es gratis pero no tiene URL
            _mensaje.value = "Este libro no tiene archivo PDF disponible"
        } else {
            // No tiene acceso
            _mensaje.value = "No tienes acceso a este libro. Contacta al administrador."
        }
    }
    
    fun clearMensaje() {
        _mensaje.value = null
    }
    
    fun clearPdfUrl() {
        _pdfUrl.value = null
    }
}
