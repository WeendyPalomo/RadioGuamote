package ec.edu.puce.lavozguamote.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// =============================================
// USUARIO
// =============================================
@Parcelize
data class Usuario(
    @SerializedName("idUsuario") val idUsuario: Int = 0,
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellido") val apellido: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("fotoPerfil") val fotoPerfil: String? = null,
    @SerializedName("rol") val rol: String? = null
) : Parcelable {
    val nombreCompleto: String get() = "$nombre $apellido"
}

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("telefono") val telefono: String? = null
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("usuario") val usuario: Usuario? = null
)

// =============================================
// EVENTOS
// =============================================
@Parcelize
data class Evento(
    @SerializedName("idEvento") val idEvento: Int = 0,
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fechaEvento") val fechaEvento: String = "",
    @SerializedName("lugar") val direccion: String? = "",
    @SerializedName("imagenUrl") val imagenPrincipal: String? = null,
    @SerializedName("nombreEncargado") val nombreEncargado: String? = "",
    @SerializedName("telefonoEncargado") val telefonoEncargado: String? = null,
    @SerializedName("categoria") val categoriaName: String? = null,
    @SerializedName("categoriaColor") val categoriaColor: String? = null,
    @SerializedName("precio") val precio: Double = 0.0,
    @SerializedName("likes") val likesCount: Int = 0,
    @SerializedName("comentarios") val comentariosCount: Int = 0
) : Parcelable {
    val horaInicio: String get() = ""
    val horaFin: String? get() = null
    val esDestacado: Boolean get() = false
    val userLiked: Boolean get() = false
    val categoria: CategoriaEvento? get() = categoriaName?.let { CategoriaEvento(0, it, categoriaColor) }
}

@Parcelize
data class CategoriaEvento(
    @SerializedName("id_categoria") val idCategoria: Int = 0,
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("color") val color: String? = null
) : Parcelable

@Parcelize
data class ComentarioEvento(
    @SerializedName("id_comentario") val idComentario: Int = 0,
    @SerializedName("usuario") val usuario: Usuario? = null,
    @SerializedName("comentario") val comentario: String = "",
    @SerializedName("fecha_comentario") val fechaComentario: String = ""
) : Parcelable

// =============================================
// NOTICIAS
// =============================================
@Parcelize
data class Noticia(
    @SerializedName("idNoticia") val idNoticia: Int = 0,
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("subtitulo") val subtitulo: String? = null,
    @SerializedName("contenido") val contenido: String = "",
    @SerializedName("resumen") val resumen: String? = null,
    @SerializedName("imagenUrl") val imagenPrincipal: String? = null,
    @SerializedName("galeriaImagenes") val galeriaImagenes: List<String>? = null,
    @SerializedName("autor") val autor: String? = null,
    @SerializedName("categoria") val categoriaName: String? = null,
    @SerializedName("categoriaColor") val categoriaColor: String? = null,
    @SerializedName("fechaPublicacion") val fechaPublicacion: String = "",
    @SerializedName("esDestacada") val esDestacada: Boolean = false,
    @SerializedName("vistas") val vistas: Int = 0,
    @SerializedName("esUrgente") val esNoticiaUrgente: Boolean = false
) : Parcelable {
    val imagenes: List<ImagenNoticia>? get() = galeriaImagenes?.map { ImagenNoticia(0, it, null) }
    val categoria: CategoriaNoticia? get() = categoriaName?.let { CategoriaNoticia(0, it, categoriaColor) }
}

@Parcelize
data class ImagenNoticia(
    @SerializedName("id_imagen") val idImagen: Int = 0,
    @SerializedName("url_imagen") val urlImagen: String = "",
    @SerializedName("titulo_imagen") val tituloImagen: String? = null
) : Parcelable

@Parcelize
data class CategoriaNoticia(
    @SerializedName("id_categoria") val idCategoria: Int = 0,
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("color") val color: String? = null
) : Parcelable

// =============================================
// LIBROS
// =============================================
@Parcelize
data class Libro(
    @SerializedName("idLibro") val idLibro: Int = 0,
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("autor") val autor: String? = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("portadaUrl") val imagenPortada: String? = null,
    @SerializedName("pdfUrl") val pdfUrl: String? = null,
    @SerializedName("categoria") val categoriaName: String? = null,
    @SerializedName("precio") val precio: Double = 0.0,
    @SerializedName("precioOferta") val precioOferta: Double? = null,
    @SerializedName("esGratis") val esGratis: Boolean = false,
    @SerializedName("rating") val valoracionPromedio: Double = 0.0,
    @SerializedName("numeroVentas") val numeroVentas: Int = 0,
    @SerializedName("comprado") val comprado: Boolean = false
) : Parcelable {
    val precioFinal: Double get() = precioOferta ?: precio
    val tieneDescuento: Boolean get() = precioOferta != null && precioOferta < precio
    val categoria: CategoriaLibro? get() = categoriaName?.let { CategoriaLibro(0, it) }
}

@Parcelize
data class CategoriaLibro(
    @SerializedName("id_categoria") val idCategoria: Int = 0,
    @SerializedName("nombre") val nombre: String = ""
) : Parcelable

data class CompraLibroRequest(
    @SerializedName("id_libro") val idLibro: Int,
    @SerializedName("metodo_pago") val metodoPago: String,
    @SerializedName("referencia_pago") val referenciaPago: String
)

// =============================================
// DONACIONES
// =============================================
data class DonacionRequest(
    @SerializedName("nombre_donante") val nombreDonante: String? = null,
    @SerializedName("monto") val monto: Double,
    @SerializedName("metodo_pago") val metodoPago: String,
    @SerializedName("referencia_pago") val referenciaPago: String,
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("es_anonimo") val esAnonimo: Boolean = false
)

@Parcelize
data class Donacion(
    @SerializedName("id_donacion") val idDonacion: Int = 0,
    @SerializedName("nombre_donante") val nombreDonante: String? = null,
    @SerializedName("monto") val monto: Double = 0.0,
    @SerializedName("fecha_donacion") val fechaDonacion: String = "",
    @SerializedName("estado") val estado: String = ""
) : Parcelable

// =============================================
// RESPUESTAS API
// =============================================
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

data class PaginatedResponse<T>(
    @SerializedName("items") val data: List<T> = emptyList(),
    @SerializedName("totalItems") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1
)

// =============================================
// RADIO CONFIG
// =============================================
data class RadioConfig(
    @SerializedName("streamingUrl") val streamingUrl: String = "",
    @SerializedName("nombreRadio") val nombreRadio: String = "",
    @SerializedName("slogan") val slogan: String? = null,
    @SerializedName("logoUrl") val logoUrl: String? = null,
    @SerializedName("enVivo") val enVivo: Boolean = true
)

// =============================================
// PODCASTS
// =============================================
@Parcelize
data class Podcast(
    @SerializedName("idPodcast") val idPodcast: Int = 0,
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("imagenUrl") val imagenUrl: String? = null,
    @SerializedName("spotifyUrl") val spotifyUrl: String = "",
    @SerializedName("spotifyEmbedUrl") val spotifyEmbedUrl: String? = null,
    @SerializedName("duracion") val duracion: String? = null,
    @SerializedName("fechaPublicacion") val fechaPublicacion: String = ""
) : Parcelable

// =============================================
// ANUNCIOS
// =============================================
@Parcelize
data class Anuncio(
    @SerializedName("idAnuncio") val idAnuncio: Int = 0,
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("imagenUrl") val imagenUrl: String? = null,
    @SerializedName("enlaceUrl") val enlaceUrl: String? = null,
    @SerializedName("activo") val activo: Boolean = true,
    @SerializedName("fechaCreacion") val fechaCreacion: String = "",
    @SerializedName("fechaExpiracion") val fechaExpiracion: String? = null
) : Parcelable

// =============================================
// REQUESTS CON IMAGEN BASE64
// =============================================

// Request para Anuncios
data class AnuncioRequest(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("imagenUrl") val imagenUrl: String? = null,
    @SerializedName("imagenBase64") val imagenBase64: String? = null,
    @SerializedName("enlaceUrl") val enlaceUrl: String?,
    @SerializedName("activo") val activo: Boolean = true
)

// Request para Podcasts (con imagen Base64)
data class PodcastRequest(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("imagenUrl") val imagenUrl: String? = null,
    @SerializedName("imagenBase64") val imagenBase64: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("spotifyUrl") val spotifyUrl: String?,
    @SerializedName("duracion") val duracion: String?
)

// Request para Libros (con imagen Base64)
data class LibroRequest(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("autor") val autor: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("portadaUrl") val portadaUrl: String? = null,
    @SerializedName("imagenBase64") val imagenBase64: String? = null,
    @SerializedName("pdfUrl") val pdfUrl: String?,
    @SerializedName("precio") val precio: Double = 0.0,
    @SerializedName("esGratis") val esGratis: Boolean = true
)

data class UploadResponse(
    @SerializedName("url") val url: String,
    @SerializedName("filename") val filename: String?
)

// =============================================
// MODELOS PARA GESTION DE USUARIOS Y LIBROS
// =============================================
data class UserSearchResult(
    @SerializedName("idUsuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String
)

data class UserWithActivation(
    @SerializedName("idUsuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("fechaActivacion") val fechaActivacion: String?
)
