package ec.edu.puce.lavozguamote.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ec.edu.puce.lavozguamote.data.api.ApiService
import ec.edu.puce.lavozguamote.data.local.DataManager
import ec.edu.puce.lavozguamote.data.models.Libro
import ec.edu.puce.lavozguamote.data.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dataManager: DataManager,
    private val apiService: ApiService
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _misLibros = MutableStateFlow<List<Libro>>(emptyList())
    val misLibros: StateFlow<List<Libro>> = _misLibros.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            if (dataManager.isLoggedIn()) {
                val user = dataManager.getCurrentUser()
                _usuario.value = user
                _isAdmin.value = dataManager.isAdmin()
            } else {
                _usuario.value = null
                _isAdmin.value = false
            }
            _isLoading.value = false
        }
    }

    fun loadMisLibros() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getMisLibrosComprados()
                if (response.isSuccessful && response.body()?.success == true) {
                    _misLibros.value = response.body()?.data ?: emptyList()
                } else {
                    _misLibros.value = emptyList()
                    _mensaje.value = response.body()?.message ?: "Error al cargar libros"
                }
            } catch (e: Exception) {
                _misLibros.value = emptyList()
                _mensaje.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataManager.logout()
            _usuario.value = null
            _isAdmin.value = false
            _misLibros.value = emptyList()
        }
    }

    fun setNotificaciones(enabled: Boolean) {
        // TODO: Guardar preferencia de notificaciones
    }

    fun clearMensaje() {
        _mensaje.value = null
    }
}
