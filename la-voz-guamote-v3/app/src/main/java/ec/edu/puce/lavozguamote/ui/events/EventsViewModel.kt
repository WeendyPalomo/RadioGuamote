package ec.edu.puce.lavozguamote.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ec.edu.puce.lavozguamote.data.api.ApiService
import ec.edu.puce.lavozguamote.data.models.CategoriaEvento
import ec.edu.puce.lavozguamote.data.models.Evento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos

    private val _categorias = MutableStateFlow<List<CategoriaEvento>>(emptyList())
    val categorias: StateFlow<List<CategoriaEvento>> = _categorias

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadEventos(categoriaId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getEventos(categoria = categoriaId)
                if (response.isSuccessful) {
                    _eventos.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Error al cargar eventos"
                }
            } catch (e: Exception) {
                _error.value = "Sin conexión al servidor"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(evento: Evento) {
        viewModelScope.launch {
            try {
                if (evento.userLiked) {
                    apiService.unlikeEvento(evento.idEvento)
                } else {
                    apiService.likeEvento(evento.idEvento)
                }
                loadEventos()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
