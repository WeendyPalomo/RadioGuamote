namespace LaVozGuamoteAPI.DTOs;

// ==================== AUTH DTOs ====================
public record LoginRequest(string Email, string Password);

public record RegisterRequest(
    string Nombre,
    string Apellido,
    string Email,
    string Password,
    string? Telefono
);

public record AuthResponse(
    bool Success,
    string? Token,
    string? Message,
    UsuarioDto? Usuario
);

public record UsuarioDto(
    int IdUsuario,
    string Nombre,
    string Apellido,
    string Email,
    string Cedula,
    string? Telefono,
    string Rol,
    string? FotoPerfil
);

// ==================== EVENTO DTOs ====================
public record EventoDto(
    int IdEvento,
    string Titulo,
    string? Descripcion,
    DateTime FechaEvento,
    string? Lugar,
    string? ImagenUrl,
    decimal Precio,
    string Categoria,
    string CategoriaColor,
    string? NombreEncargado,
    string? TelefonoEncargado,
    int Likes,
    int Comentarios
);

public record EventoCreateDto(
    string Titulo,
    string? Descripcion,
    DateTime FechaEvento,
    string? Lugar,
    string? ImagenUrl,
    decimal Precio,
    int IdCategoria,
    string? NombreEncargado,
    string? TelefonoEncargado
);

// ==================== NOTICIA DTOs ====================
public record NoticiaDto(
    int IdNoticia,
    string Titulo,
    string? Subtitulo,
    string? Contenido,
    string? Resumen,
    string? ImagenUrl,
    List<string>? GaleriaImagenes,
    string Categoria,
    string CategoriaColor,
    string? Autor,
    int Vistas,
    bool EsUrgente,
    bool EsDestacada,
    DateTime FechaPublicacion
);

public record NoticiaCreateDto(
    string Titulo,
    string? Subtitulo,
    string? Contenido,
    string? Resumen,
    string? ImagenUrl,
    List<string>? GaleriaImagenes,
    int IdCategoria,
    string? Autor,
    bool EsUrgente,
    bool EsDestacada
);

// ==================== LIBRO DTOs ====================
public record LibroDto(
    int IdLibro,
    string Titulo,
    string? Autor,
    string? Descripcion,
    string? PortadaUrl,
    string? PdfUrl,
    string Categoria,
    string CategoriaColor,
    decimal Precio,
    decimal? PrecioOferta,
    bool EsGratis,
    decimal Rating,
    int NumeroVentas,
    bool Comprado
);

public record LibroCreateDto(
    string Titulo,
    string? Autor,
    string? Descripcion,
    string? PortadaUrl,
    string? PdfUrl,
    int IdCategoria,
    decimal Precio,
    decimal? PrecioOferta,
    bool EsGratis
);

// DTO para crear/actualizar libro con imagen Base64 (usado por LibrosController)
public class LibroCreateDtoWithImage
{
    public string Titulo { get; set; } = string.Empty;
    public string? Autor { get; set; }
    public string? Descripcion { get; set; }
    public string? PortadaUrl { get; set; }
    public string? ImagenBase64 { get; set; }
    public string? PdfUrl { get; set; }
    public int? IdCategoria { get; set; }
    public decimal Precio { get; set; } = 0;
    public decimal? PrecioOferta { get; set; }
    public bool EsGratis { get; set; } = false;
}

// ==================== DONACION DTOs ====================
public record DonacionDto(
    int IdDonacion,
    string? NombreDonante,
    decimal Monto,
    string MetodoPago,
    string? Mensaje,
    string Estado,
    DateTime FechaDonacion
);

public record DonacionCreateDto(
    decimal Monto,
    string MetodoPago,
    string? NumeroComprobante,
    string? Mensaje,
    string? NombreDonante,
    string? EmailDonante
);

// ==================== GENERAL ====================
public record ApiResponse<T>(bool Success, string? Message, T? Data);

public record PaginatedResponse<T>(
    List<T> Items,
    int TotalItems,
    int Page,
    int PageSize,
    int TotalPages
);

public record CategoriaDto(int IdCategoria, string Nombre, string? Color, string? Icono);

public record RadioConfigDto(string StreamingUrl, string NombreRadio, string? Slogan, string? LogoUrl, bool EnVivo);
