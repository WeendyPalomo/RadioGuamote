package ec.edu.puce.lavozguamote.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ec.edu.puce.lavozguamote.data.api.ApiService
import ec.edu.puce.lavozguamote.data.models.CategoriaNoticia
import ec.edu.puce.lavozguamote.data.models.Noticia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _noticias = MutableStateFlow<List<Noticia>>(emptyList())
    val noticias: StateFlow<List<Noticia>> = _noticias

    private val _categorias = MutableStateFlow<List<CategoriaNoticia>>(emptyList())
    val categorias: StateFlow<List<CategoriaNoticia>> = _categorias

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadNoticias(categoriaId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getNoticias(categoria = categoriaId)
                if (response.isSuccessful) {
                    _noticias.value = response.body()?.data ?: emptyList()
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
