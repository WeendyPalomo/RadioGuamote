using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LaVozGuamoteAPI.Models;

// ==================== USUARIO ====================
[Table("Usuarios")]
public class Usuario
{
    [Key]
    [Column("id_usuario")]
    public int IdUsuario { get; set; }
    
    [Required, MaxLength(100)]
    [Column("nombre")]
    public string Nombre { get; set; } = string.Empty;
    
    [Required, MaxLength(100)]
    [Column("apellido")]
    public string Apellido { get; set; } = string.Empty;
    
    [Required, MaxLength(150)]
    [Column("email")]
    public string Email { get; set; } = string.Empty;
    
    [Required]
    [Column("password_hash")]
    public string PasswordHash { get; set; } = string.Empty;
    
    [MaxLength(20)]
    [Column("telefono")]
    public string? Telefono { get; set; }
    
    [Column("foto_perfil")]
    public string? FotoPerfil { get; set; }
    
    [Column("fecha_registro")]
    public DateTime FechaRegistro { get; set; } = DateTime.UtcNow;
    
    [Column("es_activo")]
    public bool EsActivo { get; set; } = true;
    
    [Column("es_admin")]
    public bool EsAdmin { get; set; } = false;
    
    [NotMapped]
    public string NombreCompleto => $"{Nombre} {Apellido}";
}

// ==================== CATEGORIAS EVENTOS ====================
[Table("CategoriasEventos")]
public class CategoriaEvento
{
    [Key]
    [Column("id_categoria")]
    public int IdCategoria { get; set; }
    
    [Required, MaxLength(100)]
    [Column("nombre")]
    public string Nombre { get; set; } = string.Empty;
    
    [Column("descripcion")]
    public string? Descripcion { get; set; }
    
    [Column("icono")]
    public string? Icono { get; set; }
    
    [MaxLength(7)]
    [Column("color")]
    public string Color { get; set; } = "#1565C0";
}

// ==================== EVENTO ====================
[Table("Eventos")]
public class Evento
{
    [Key]
    [Column("id_evento")]
    public int IdEvento { get; set; }
    
    [Required, MaxLength(200)]
    [Column("titulo")]
    public string Titulo { get; set; } = string.Empty;
    
    [Column("descripcion")]
    public string? Descripcion { get; set; }
    
    [Column("fecha_evento")]
    public DateTime FechaEvento { get; set; }
    
    [Column("hora_inicio")]
    public TimeSpan HoraInicio { get; set; }
    
    [Column("hora_fin")]
    public TimeSpan? HoraFin { get; set; }
    
    [MaxLength(300)]
    [Column("direccion")]
    public string? Direccion { get; set; }
    
    [Column("imagen_principal")]
    public string? ImagenPrincipal { get; set; }
    
    [MaxLength(150)]
    [Column("nombre_encargado")]
    public string? NombreEncargado { get; set; }
    
    [MaxLength(20)]
    [Column("telefono_encargado")]
    public string? TelefonoEncargado { get; set; }
    
    [Column("id_categoria")]
    public int? IdCategoria { get; set; }
    
    [ForeignKey("IdCategoria")]
    public CategoriaEvento? Categoria { get; set; }
    
    [Column("id_usuario_creador")]
    public int IdUsuarioCreador { get; set; }
    
    [Column("precio")]
    public decimal Precio { get; set; } = 0;
    
    [Column("es_activo")]
    public bool EsActivo { get; set; } = true;
    
    [Column("es_destacado")]
    public bool EsDestacado { get; set; } = false;
    
    [Column("fecha_creacion")]
    public DateTime FechaCreacion { get; set; } = DateTime.UtcNow;
}

// ==================== LIKES EVENTOS ====================
[Table("LikesEventos")]
public class LikeEvento
{
    [Key]
    [Column("id_like")]
    public int IdLike { get; set; }
    
    [Column("id_evento")]
    public int IdEvento { get; set; }
    
    [Column("id_usuario")]
    public int IdUsuario { get; set; }
    
    [Column("fecha_like")]
    public DateTime FechaLike { get; set; } = DateTime.UtcNow;
}

// ==================== COMENTARIOS EVENTOS ====================
[Table("ComentariosEventos")]
public class ComentarioEvento
{
    [Key]
    [Column("id_comentario")]
    public int IdComentario { get; set; }
    
    [Column("id_evento")]
    public int IdEvento { get; set; }
    
    [Column("id_usuario")]
    public int IdUsuario { get; set; }
    
    [Column("comentario")]
    public string Comentario { get; set; } = string.Empty;
    
    [Column("fecha_comentario")]
    public DateTime FechaComentario { get; set; } = DateTime.UtcNow;
    
    [Column("es_activo")]
    public bool EsActivo { get; set; } = true;
    
    [Column("likes_count")]
    public int LikesCount { get; set; } = 0;
}

// ==================== CATEGORIAS NOTICIAS ====================
[Table("CategoriasNoticias")]
public class CategoriaNoticia
{
    [Key]
    [Column("id_categoria")]
    public int IdCategoria { get; set; }
    
    [Required, MaxLength(100)]
    [Column("nombre")]
    public string Nombre { get; set; } = string.Empty;
    
    [Column("descripcion")]
    public string? Descripcion { get; set; }
    
    [Column("icono")]
    public string? Icono { get; set; }
    
    [MaxLength(7)]
    [Column("color")]
    public string Color { get; set; } = "#1565C0";
}

// ==================== NOTICIA ====================
[Table("Noticias")]
public class Noticia
{
    [Key]
    [Column("id_noticia")]
    public int IdNoticia { get; set; }
    
    [Required, MaxLength(250)]
    [Column("titulo")]
    public string Titulo { get; set; } = string.Empty;
    
    [MaxLength(300)]
    [Column("subtitulo")]
    public string? Subtitulo { get; set; }
    
    [Column("contenido")]
    public string? Contenido { get; set; }
    
    [MaxLength(500)]
    [Column("resumen")]
    public string? Resumen { get; set; }
    
    [Column("imagen_principal")]
    public string? ImagenPrincipal { get; set; }
    
    [Column("id_categoria")]
    public int? IdCategoria { get; set; }
    
    [ForeignKey("IdCategoria")]
    public CategoriaNoticia? Categoria { get; set; }
    
    [MaxLength(150)]
    [Column("autor")]
    public string? Autor { get; set; }
    
    [Column("id_usuario_creador")]
    public int IdUsuarioCreador { get; set; }
    
    [Column("vistas")]
    public int Vistas { get; set; } = 0;
    
    [Column("es_noticia_urgente")]
    public bool EsUrgente { get; set; } = false;
    
    [Column("es_destacada")]
    public bool EsDestacada { get; set; } = false;
    
    [Column("es_activo")]
    public bool EsActivo { get; set; } = true;
    
    [Column("fecha_publicacion")]
    public DateTime FechaPublicacion { get; set; } = DateTime.UtcNow;
}

// ==================== IMAGENES NOTICIAS ====================
[Table("ImagenesNoticias")]
public class ImagenNoticia
{
    [Key]
    [Column("id_imagen")]
    public int IdImagen { get; set; }
    
    [Column("id_noticia")]
    public int IdNoticia { get; set; }
    
    [Column("url_imagen")]
    public string UrlImagen { get; set; } = string.Empty;
    
    [Column("titulo_imagen")]
    public string? TituloImagen { get; set; }
    
    [Column("orden")]
    public int Orden { get; set; } = 0;
}

// ==================== CATEGORIAS LIBROS ====================
[Table("CategoriasLibros")]
public class CategoriaLibro
{
    [Key]
    [Column("id_categoria")]
    public int IdCategoria { get; set; }
    
    [Required, MaxLength(100)]
    [Column("nombre")]
    public string Nombre { get; set; } = string.Empty;
    
    [Column("descripcion")]
    public string? Descripcion { get; set; }
    
    [Column("icono")]
    public string? Icono { get; set; }
}

// ==================== LIBRO (CON IMAGEN BINARIA) ====================
[Table("Libros")]
public class Libro
{
    [Key]
    [Column("id_libro")]
    public int IdLibro { get; set; }
    
    [Required, MaxLength(250)]
    [Column("titulo")]
    public string Titulo { get; set; } = string.Empty;
    
    [MaxLength(200)]
    [Column("autor")]
    public string? Autor { get; set; }
    
    [Column("descripcion")]
    public string? Descripcion { get; set; }
    
    [Column("imagen_portada")]
    public string? ImagenPortada { get; set; }
    
    // Campo para imagen binaria
    [Column("imagen")]
    public byte[]? Imagen { get; set; }
    
    // Tipo de contenido de la imagen
    [MaxLength(50)]
    [Column("imagen_content_type")]
    public string? ImagenContentType { get; set; }
    
    [Column("url_pdf")]
    public string? UrlPdf { get; set; }
    
    [Column("id_categoria")]
    public int? IdCategoria { get; set; }
    
    [ForeignKey("IdCategoria")]
    public CategoriaLibro? Categoria { get; set; }
    
    [Column("id_usuario_creador")]
    public int IdUsuarioCreador { get; set; }
    
    [Column("precio")]
    public decimal Precio { get; set; } = 0;
    
    [Column("precio_oferta")]
    public decimal? PrecioOferta { get; set; }
    
    // ========== CORREGIDO: Mapeo exacto al nombre de columna en BD ==========
    [Column("EsGratis")]  // <-- Cambiado de "es_gratis" a "EsGratis" para coincidir con la BD
    public bool EsGratis { get; set; } = false;
    
    [Column("valoracion_promedio")]
    public decimal Rating { get; set; } = 0;
    
    [Column("descargas")]
    public int Descargas { get; set; } = 0;
    
    [Column("es_activo")]
    public bool EsActivo { get; set; } = true;
    
    [Column("es_destacado")]
    public bool EsDestacado { get; set; } = false;
    
    [Column("fecha_publicacion")]
    public DateTime FechaPublicacion { get; set; } = DateTime.UtcNow;
}

// ==================== COMPRAS LIBROS ====================
[Table("ComprasLibros")]
public class CompraLibro
{
    [Key]
    [Column("id_compra")]
    public int IdCompra { get; set; }
    
    [Column("id_libro")]
    public int IdLibro { get; set; }
    
    [ForeignKey("IdLibro")]
    public Libro? Libro { get; set; }
    
    [Column("id_usuario")]
    public int IdUsuario { get; set; }
    
    [ForeignKey("IdUsuario")]
    public Usuario? Usuario { get; set; }
    
    [Column("precio_compra")]
    public decimal PrecioCompra { get; set; }
    
    [Column("fecha_compra")]
    public DateTime FechaCompra { get; set; } = DateTime.UtcNow;
    
    [Column("estado_pago")]
    public string EstadoPago { get; set; } = "PENDIENTE";
}

// ==================== DONACION ====================
[Table("Donaciones")]
public class Donacion
{
    [Key]
    [Column("id_donacion")]
    public int IdDonacion { get; set; }
    
    [Column("id_usuario")]
    public int? IdUsuario { get; set; }
    
    [ForeignKey("IdUsuario")]
    public Usuario? Usuario { get; set; }
    
    [MaxLength(150)]
    [Column("nombre_donante")]
    public string? NombreDonante { get; set; }
    
    [MaxLength(150)]
    [Column("email_donante")]
    public string? EmailDonante { get; set; }
    
    [Column("monto")]
    public decimal Monto { get; set; }
    
    [MaxLength(50)]
    [Column("metodo_pago")]
    public string MetodoPago { get; set; } = "Transferencia";
    
    [MaxLength(100)]
    [Column("referencia_pago")]
    public string? ReferenciaPago { get; set; }
    
    [Column("comprobante")]
    public string? Comprobante { get; set; }
    
    [Column("mensaje")]
    public string? Mensaje { get; set; }
    
    [MaxLength(20)]
    [Column("estado")]
    public string Estado { get; set; } = "PENDIENTE";
    
    [Column("fecha_donacion")]
    public DateTime FechaDonacion { get; set; } = DateTime.UtcNow;
}

// ==================== CONFIGURACION RADIO ====================
[Table("ConfiguracionRadio")]
public class ConfiguracionRadio
{
    [Key]
    [Column("id_config")]
    public int IdConfig { get; set; }
    
    [MaxLength(100)]
    [Column("nombre_radio")]
    public string NombreRadio { get; set; } = "La Voz de Guamote";
    
    [MaxLength(200)]
    [Column("slogan")]
    public string? Slogan { get; set; }
    
    [Column("url_streaming")]
    public string UrlStreaming { get; set; } = "https://usa3.lhdserver.es:8093/stream";
    
    [Column("url_logo")]
    public string? UrlLogo { get; set; }
    
    [Column("telefono_contacto")]
    public string? TelefonoContacto { get; set; }
    
    [Column("email_contacto")]
    public string? EmailContacto { get; set; }
}

// ==================== USUARIOS LIBROS (Activacion por Admin) ====================
[Table("UsuariosLibros")]
public class UsuarioLibro
{
    [Key]
    [Column("id")]
    public int Id { get; set; }
    
    [Column("id_usuario")]
    public int IdUsuario { get; set; }
    
    [ForeignKey("IdUsuario")]
    public Usuario? Usuario { get; set; }
    
    [Column("id_libro")]
    public int IdLibro { get; set; }
    
    [ForeignKey("IdLibro")]
    public Libro? Libro { get; set; }
    
    [Column("fecha_activacion")]
    public DateTime FechaActivacion { get; set; } = DateTime.UtcNow;
    
    [Column("activado_por")]
    public int? ActivadoPor { get; set; }
    
    [Column("notas")]
    public string? Notas { get; set; }
}

// ==================== ANUNCIOS (CON IMAGEN BINARIA) ====================
[Table("Anuncios")]
public class Anuncio
{
    [Key]
    [Column("id_anuncio")]
    public int IdAnuncio { get; set; }
    
    [Required, MaxLength(200)]
    [Column("titulo")]
    public string Titulo { get; set; } = string.Empty;
    
    [Column("descripcion")]
    public string? Descripcion { get; set; }
    
    [Column("imagen_url")]
    public string? ImagenUrl { get; set; }
    
    // Campo para imagen binaria
    [Column("imagen")]
    public byte[]? Imagen { get; set; }
    
    // Tipo de contenido de la imagen (ej: image/jpeg, image/png)
    [MaxLength(50)]
    [Column("imagen_content_type")]
    public string? ImagenContentType { get; set; }
    
    [Column("enlace_url")]
    public string? EnlaceUrl { get; set; }
    
    [Column("activo")]
    public bool Activo { get; set; } = true;
    
    [Column("fecha_creacion")]
    public DateTime FechaCreacion { get; set; } = DateTime.UtcNow;
    
    [Column("fecha_expiracion")]
    public DateTime? FechaExpiracion { get; set; }
}

// ==================== PODCASTS (CON IMAGEN BINARIA) ====================
[Table("Podcasts")]
public class Podcast
{
    [Key]
    [Column("id_podcast")]
    public int IdPodcast { get; set; }
    
    [Required, MaxLength(200)]
    [Column("titulo")]
    public string Titulo { get; set; } = string.Empty;
    
    [Column("descripcion")]
    public string? Descripcion { get; set; }
    
    [MaxLength(500)]
    [Column("imagen_url")]
    public string? ImagenUrl { get; set; }
    
    // Campo para imagen binaria
    [Column("imagen")]
    public byte[]? Imagen { get; set; }
    
    // Tipo de contenido de la imagen
    [MaxLength(50)]
    [Column("imagen_content_type")]
    public string? ImagenContentType { get; set; }
    
    [MaxLength(500)]
    [Column("spotify_url")]
    public string SpotifyUrl { get; set; } = string.Empty;
    
    [MaxLength(500)]
    [Column("spotify_embed_url")]
    public string? SpotifyEmbedUrl { get; set; }
    
    [MaxLength(50)]
    [Column("duracion")]
    public string? Duracion { get; set; }
    
    [Column("fecha_publicacion")]
    public DateTime FechaPublicacion { get; set; } = DateTime.UtcNow;
    
    [Column("es_activo")]
    public bool EsActivo { get; set; } = true;
    
    [Column("orden")]
    public int? Orden { get; set; }
}
