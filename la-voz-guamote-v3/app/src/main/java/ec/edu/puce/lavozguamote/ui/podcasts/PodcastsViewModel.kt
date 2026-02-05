package ec.edu.puce.lavozguamote.ui.podcasts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ec.edu.puce.lavozguamote.data.api.ApiService
import ec.edu.puce.lavozguamote.data.models.Podcast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PodcastsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _podcasts = MutableStateFlow<List<Podcast>>(emptyList())
    val podcasts: StateFlow<List<Podcast>> = _podcasts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPodcasts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getPodcasts()
                if (response.isSuccessful) {
                    _podcasts.value = response.body()?.data ?: emptyList()
                } else {
                    _podcasts.value = emptyList()
                    _error.value = "Error al cargar podcasts"
                }
            } catch (e: Exception) {
                _podcasts.value = emptyList()
                _error.value = "Error de conexión"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
