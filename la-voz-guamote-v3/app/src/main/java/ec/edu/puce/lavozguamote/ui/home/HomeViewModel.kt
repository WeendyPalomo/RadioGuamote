package ec.edu.puce.lavozguamote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ec.edu.puce.lavozguamote.data.api.ApiService
import ec.edu.puce.lavozguamote.data.models.Noticia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _ultimasNoticias = MutableStateFlow<List<Noticia>>(emptyList())
    val ultimasNoticias: StateFlow<List<Noticia>> = _ultimasNoticias

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUltimasNoticias() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getUltimasNoticias(5)
                if (response.isSuccessful && response.body()?.success == true) {
                    _ultimasNoticias.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Error al cargar noticias"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión al servidor"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
