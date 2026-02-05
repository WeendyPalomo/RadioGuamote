package ec.edu.puce.lavozguamote.data.local

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import ec.edu.puce.lavozguamote.data.api.ApiService
import ec.edu.puce.lavozguamote.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    private val gson = Gson()
    
    private val userPrefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val cachePrefs: SharedPreferences = context.getSharedPreferences("cache_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_AUTH_TOKEN = "auth_token"
        
        // Cache keys
        private const val KEY_CACHE_ANUNCIOS = "cache_anuncios"
        private const val KEY_CACHE_PODCASTS = "cache_podcasts"
        private const val KEY_CACHE_LIBROS = "cache_libros"
        
        // Tamano maximo de imagen (800px)
        private const val MAX_IMAGE_SIZE = 800
        // Calidad de compresion JPEG (0-100)
        private const val JPEG_QUALITY = 80
    }

    // ========== UTILIDADES DE IMAGEN ==========
    
    /**
     * Convierte un Uri de imagen a Base64, comprimiendo si es necesario
     */
    suspend fun uriToBase64(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                // Decodificar la imagen
                val originalBitmap = BitmapFactory.decodeStream(stream)
                
                // Redimensionar si es muy grande
                val resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SIZE)
                
                // Comprimir a JPEG
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
                
                // Convertir a Base64
                val bytes = outputStream.toByteArray()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                
                // Limpiar
                if (resizedBitmap != originalBitmap) {
                    resizedBitmap.recycle()
                }
                originalBitmap.recycle()
                
                // Devolver con prefijo data URI
                "data:image/jpeg;base64,$base64"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Redimensiona un bitmap manteniendo la proporcion
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // ========== AUTENTICACION ==========
    
    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()!!
                val usuario = authResponse.usuario
                
                userPrefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_AUTH_TOKEN, authResponse.token)
                    .putString(KEY_CURRENT_USER, gson.toJson(usuario))
                    .apply()
                
                LoginResult.Success(usuario!!)
            } else {
                LoginResult.Error(response.body()?.message ?: "Credenciales incorrectas")
            }
        } catch (e: Exception) {
            LoginResult.Error("Error de conexion: ${e.message}")
        }
    }

    suspend fun register(nombre: String, apellido: String, email: String, password: String, telefono: String?): RegisterResult = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(nombre, apellido, email, password, telefono)
            val response = apiService.register(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()!!
                val usuario = authResponse.usuario
                
                userPrefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_AUTH_TOKEN, authResponse.token)
                    .putString(KEY_CURRENT_USER, gson.toJson(usuario))
                    .apply()
                
                RegisterResult.Success(usuario!!)
            } else {
                RegisterResult.Error(response.body()?.message ?: "Error al registrar")
            }
        } catch (e: Exception) {
            RegisterResult.Error("Error de conexion: ${e.message}")
        }
    }

    fun logout() {
        userPrefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_CURRENT_USER)
            .apply()
    }

    fun isLoggedIn(): Boolean = userPrefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getCurrentUser(): Usuario? {
        val json = userPrefs.getString(KEY_CURRENT_USER, null) ?: return null
        return try {
            gson.fromJson(json, Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getAuthToken(): String? = userPrefs.getString(KEY_AUTH_TOKEN, null)

    fun isAdmin(): Boolean {
        val user = getCurrentUser()
        return user?.rol?.equals("admin", ignoreCase = true) == true ||
               user?.rol?.equals("administrador", ignoreCase = true) == true
    }

    // ========== ANUNCIOS ==========
    
    suspend fun getAnuncios(): List<Anuncio> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAnuncios()
            if (response.isSuccessful && response.body()?.success == true) {
                val anuncios = response.body()?.data ?: emptyList()
                cachePrefs.edit().putString(KEY_CACHE_ANUNCIOS, gson.toJson(anuncios)).apply()
                anuncios
            } else {
                getCachedAnuncios()
            }
        } catch (e: Exception) {
            getCachedAnuncios()
        }
    }

    private fun getCachedAnuncios(): List<Anuncio> {
        val json = cachePrefs.getString(KEY_CACHE_ANUNCIOS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Anuncio>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createAnuncio(titulo: String, descripcion: String?, imagenBase64: String?, enlaceUrl: String?): ApiResult<Anuncio> = withContext(Dispatchers.IO) {
        try {
            val request = AnuncioRequest(
                titulo = titulo,
                descripcion = descripcion,
                imagenBase64 = imagenBase64,
                imagenUrl = null,
                enlaceUrl = enlaceUrl
            )
            val response = apiService.createAnuncio(request)
            if (response.isSuccessful && response.body()?.success == true) {
                ApiResult.Success(response.body()?.data!!)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al crear anuncio")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    suspend fun updateAnuncio(id: Int, titulo: String, descripcion: String?, imagenBase64: String?, enlaceUrl: String?): ApiResult<Anuncio> = withContext(Dispatchers.IO) {
        try {
            val request = AnuncioRequest(
                titulo = titulo,
                descripcion = descripcion,
                imagenBase64 = imagenBase64,
                imagenUrl = null,
                enlaceUrl = enlaceUrl
            )
            val response = apiService.updateAnuncio(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                ApiResult.Success(response.body()?.data!!)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al actualizar")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    suspend fun deleteAnuncio(id: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteAnuncio(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al eliminar")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    // ========== PODCASTS ==========
    
    suspend fun getPodcasts(): List<Podcast> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPodcasts()
            if (response.isSuccessful && response.body()?.success == true) {
                val podcasts = response.body()?.data ?: emptyList()
                cachePrefs.edit().putString(KEY_CACHE_PODCASTS, gson.toJson(podcasts)).apply()
                podcasts
            } else {
                getCachedPodcasts()
            }
        } catch (e: Exception) {
            getCachedPodcasts()
        }
    }

    private fun getCachedPodcasts(): List<Podcast> {
        val json = cachePrefs.getString(KEY_CACHE_PODCASTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Podcast>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createPodcast(titulo: String, descripcion: String?, imagenBase64: String?, spotifyUrl: String?, duracion: String?): ApiResult<Podcast> = withContext(Dispatchers.IO) {
        try {
            val request = PodcastRequest(
                titulo = titulo,
                descripcion = descripcion,
                imagenBase64 = imagenBase64,
                imagenUrl = null,
                spotifyUrl = spotifyUrl,
                duracion = duracion
            )
            val response = apiService.createPodcast(request)
            if (response.isSuccessful && response.body()?.success == true) {
                ApiResult.Success(response.body()?.data!!)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al crear podcast")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    suspend fun updatePodcast(id: Int, titulo: String, descripcion: String?, imagenBase64: String?, spotifyUrl: String?, duracion: String?): ApiResult<Podcast> = withContext(Dispatchers.IO) {
        try {
            val request = PodcastRequest(
                titulo = titulo,
                descripcion = descripcion,
                imagenBase64 = imagenBase64,
                imagenUrl = null,
                spotifyUrl = spotifyUrl,
                duracion = duracion
            )
            val response = apiService.updatePodcast(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                ApiResult.Success(response.body()?.data!!)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al actualizar")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    suspend fun deletePodcast(id: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deletePodcast(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al eliminar")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    // ========== LIBROS ==========
    
    suspend fun getLibros(): List<Libro> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLibros()
            if (response.isSuccessful) {
                val libros = response.body()?.data ?: emptyList()
                cachePrefs.edit().putString(KEY_CACHE_LIBROS, gson.toJson(libros)).apply()
                libros
            } else {
                getCachedLibros()
            }
        } catch (e: Exception) {
            getCachedLibros()
        }
    }

    private fun getCachedLibros(): List<Libro> {
        val json = cachePrefs.getString(KEY_CACHE_LIBROS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Libro>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // CORREGIDO: esGratis ahora es un parametro, no esta hardcodeado
    suspend fun createLibro(titulo: String, autor: String?, descripcion: String?, imagenBase64: String?, pdfUrl: String?, esGratis: Boolean = false): ApiResult<Libro> = withContext(Dispatchers.IO) {
        try {
            val request = LibroRequest(
                titulo = titulo,
                autor = autor,
                descripcion = descripcion,
                imagenBase64 = imagenBase64,
                portadaUrl = null,
                pdfUrl = pdfUrl,
                esGratis = esGratis  // Usar el parametro
            )
            val response = apiService.createLibro(request)
            if (response.isSuccessful && response.body()?.success == true) {
                ApiResult.Success(response.body()?.data!!)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al crear libro")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    // CORREGIDO: esGratis ahora es un parametro, no esta hardcodeado
    suspend fun updateLibro(id: Int, titulo: String, autor: String?, descripcion: String?, imagenBase64: String?, pdfUrl: String?, esGratis: Boolean = false): ApiResult<Libro> = withContext(Dispatchers.IO) {
        try {
            val request = LibroRequest(
                titulo = titulo,
                autor = autor,
                descripcion = descripcion,
                imagenBase64 = imagenBase64,
                portadaUrl = null,
                pdfUrl = pdfUrl,
                esGratis = esGratis  // Usar el parametro
            )
            val response = apiService.updateLibro(id, request)
            if (response.isSuccessful && response.body()?.success == true) {
                ApiResult.Success(response.body()?.data!!)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al actualizar")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    suspend fun deleteLibro(id: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteLibro(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.body()?.message ?: "Error al eliminar")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error de conexion: ${e.message}")
        }
    }

    // Contar items para estadisticas
    suspend fun getAnunciosCount(): Int = getAnuncios().size
    suspend fun getPodcastsCount(): Int = getPodcasts().size
    suspend fun getLibrosCount(): Int = getLibros().size
}

// ========== RESULT CLASSES ==========

sealed class LoginResult {
    data class Success(val usuario: Usuario) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class RegisterResult {
    data class Success(val usuario: Usuario) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}
