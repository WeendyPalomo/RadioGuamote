package ec.edu.puce.lavozguamote.data.api

import ec.edu.puce.lavozguamote.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // AUTENTICACIÓN
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ApiResponse<Usuario>>

    // EVENTOS
    @GET("api/eventos")
    suspend fun getEventos(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("categoriaId") categoria: Int? = null
    ): Response<PaginatedResponse<Evento>>

    @GET("api/eventos/{id}")
    suspend fun getEvento(@Path("id") id: Int): Response<ApiResponse<Evento>>

    @POST("api/eventos/{id}/like")
    suspend fun likeEvento(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @DELETE("api/eventos/{id}/like")
    suspend fun unlikeEvento(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @GET("api/categorias/eventos")
    suspend fun getCategoriasEventos(): Response<ApiResponse<List<CategoriaEvento>>>

    // ============================================
    // ANUNCIOS - CORREGIDO: usa ApiResponse en lugar de PaginatedResponse
    // ============================================
    @GET("api/anuncios")
    suspend fun getAnuncios(): Response<ApiResponse<List<Anuncio>>>

    @GET("api/anuncios/{id}")
    suspend fun getAnuncio(@Path("id") id: Int): Response<ApiResponse<Anuncio>>

    @POST("api/anuncios")
    suspend fun createAnuncio(@Body anuncio: AnuncioRequest): Response<ApiResponse<Anuncio>>

    @PUT("api/anuncios/{id}")
    suspend fun updateAnuncio(@Path("id") id: Int, @Body anuncio: AnuncioRequest): Response<ApiResponse<Anuncio>>

    @DELETE("api/anuncios/{id}")
    suspend fun deleteAnuncio(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // UPLOAD
    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Query("type") type: String = "general"
    ): Response<ApiResponse<UploadResponse>>

    // NOTICIAS
    @GET("api/noticias")
    suspend fun getNoticias(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("categoriaId") categoria: Int? = null
    ): Response<PaginatedResponse<Noticia>>

    @GET("api/noticias/latest")
    suspend fun getUltimasNoticias(@Query("count") count: Int = 5): Response<ApiResponse<List<Noticia>>>

    @GET("api/noticias/{id}")
    suspend fun getNoticia(@Path("id") id: Int): Response<ApiResponse<Noticia>>

    @GET("api/categorias/noticias")
    suspend fun getCategoriasNoticias(): Response<ApiResponse<List<CategoriaNoticia>>>

    // LIBROS
    @GET("api/libros")
    suspend fun getLibros(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("categoriaId") categoria: Int? = null
    ): Response<PaginatedResponse<Libro>>

    @GET("api/libros/{id}")
    suspend fun getLibro(@Path("id") id: Int): Response<ApiResponse<Libro>>

    @POST("api/libros/{id}/purchase")
    suspend fun comprarLibro(
        @Path("id") id: Int,
        @Body request: CompraLibroRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/libros/mis-libros")
    suspend fun getMisLibrosComprados(): Response<ApiResponse<List<Libro>>>

    @GET("api/libros/{id}/pdf")
    suspend fun getLibroPdf(@Path("id") id: Int): Response<ApiResponse<String>>

    @GET("api/categorias/libros")
    suspend fun getCategoriasLibros(): Response<ApiResponse<List<CategoriaLibro>>>

    // ADMIN - LIBROS
    @POST("api/libros")
    suspend fun createLibro(@Body libro: LibroRequest): Response<ApiResponse<Libro>>

    @PUT("api/libros/{id}")
    suspend fun updateLibro(@Path("id") id: Int, @Body libro: LibroRequest): Response<ApiResponse<Libro>>

    @DELETE("api/libros/{id}")
    suspend fun deleteLibro(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // DONACIONES
    @POST("api/donaciones")
    suspend fun registrarDonacion(@Body request: DonacionRequest): Response<ApiResponse<Unit>>

    // RADIO
    @GET("api/radio/config")
    suspend fun getRadioConfig(): Response<ApiResponse<RadioConfig>>

    // ============================================
    // PODCASTS - CORREGIDO: usa ApiResponse en lugar de PaginatedResponse
    // ============================================
    @GET("api/podcasts")
    suspend fun getPodcasts(): Response<ApiResponse<List<Podcast>>>

    @GET("api/podcasts/{id}")
    suspend fun getPodcast(@Path("id") id: Int): Response<ApiResponse<Podcast>>

    @POST("api/podcasts")
    suspend fun createPodcast(@Body podcast: PodcastRequest): Response<ApiResponse<Podcast>>

    @PUT("api/podcasts/{id}")
    suspend fun updatePodcast(@Path("id") id: Int, @Body podcast: PodcastRequest): Response<ApiResponse<Podcast>>

    @DELETE("api/podcasts/{id}")
    suspend fun deletePodcast(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ============================================
    // ADMIN - GESTIÓN DE USUARIOS Y LIBROS
    // ============================================

    // Buscar usuarios
    @GET("api/admin/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<ApiResponse<List<UserSearchResult>>>

    // Activar libro para usuario
    @POST("api/admin/users/{userId}/books/{bookId}/activate")
    suspend fun activateBookForUser(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Unit>>

    // Desactivar libro para usuario
    @DELETE("api/admin/users/{userId}/books/{bookId}/activate")
    suspend fun deactivateBookForUser(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Unit>>

    // Obtener usuarios con libro activado
    @GET("api/admin/books/{bookId}/users")
    suspend fun getBookUsers(@Path("bookId") bookId: Int): Response<ApiResponse<List<UserWithActivation>>>

    // Obtener libros activados de un usuario
    @GET("api/admin/users/{userId}/books")
    suspend fun getUserBooks(@Path("userId") userId: Int): Response<ApiResponse<List<Libro>>>
}
