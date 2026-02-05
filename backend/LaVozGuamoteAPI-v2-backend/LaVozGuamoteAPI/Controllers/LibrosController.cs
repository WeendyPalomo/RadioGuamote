using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using LaVozGuamoteAPI.Data;
using LaVozGuamoteAPI.Models;
using LaVozGuamoteAPI.DTOs;
using LaVozGuamoteAPI.Services;

namespace LaVozGuamoteAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class LibrosController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly ILibroService _libroService;

    public LibrosController(AppDbContext context, ILibroService libroService)
    {
        _context = context;
        _libroService = libroService;
    }

    private async Task<bool> IsAdmin()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim)) return false;
        var userId = int.Parse(userIdClaim);
        var user = await _context.Usuarios.FindAsync(userId);
        return user?.EsAdmin ?? false;
    }

    [HttpGet]
    public async Task<ActionResult<PaginatedResponse<LibroDto>>> GetAll(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10,
        [FromQuery] int? categoriaId = null)
    {
        int? userId = null;
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (!string.IsNullOrEmpty(userIdClaim))
            userId = int.Parse(userIdClaim);

        var baseUrl = $"{Request.Scheme}://{Request.Host}";

        var query = _context.Libros
            .Include(l => l.Categoria)
            .Where(l => l.EsActivo)
            .AsQueryable();

        if (categoriaId.HasValue)
            query = query.Where(l => l.IdCategoria == categoriaId.Value);

        var total = await query.CountAsync();

        // Libros comprados por el usuario
        var userBooks = userId.HasValue ?
            await _context.ComprasLibros
                .Where(c => c.IdUsuario == userId.Value && c.EstadoPago == "APROBADO")
                .Select(c => c.IdLibro)
                .ToListAsync() :
            new List<int>();

        // Libros activados por admin para el usuario
        var activatedBooks = userId.HasValue ?
            await _context.UsuariosLibros
                .Where(ul => ul.IdUsuario == userId.Value)
                .Select(ul => ul.IdLibro)
                .ToListAsync() :
            new List<int>();

        var allUserBooks = userBooks.Union(activatedBooks).ToList();

        var libros = await query
            .OrderByDescending(l => l.FechaPublicacion)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var items = libros.Select(l => {
            // VERIFICACION ESTRICTA: tiene acceso SOLO si:
            // 1. EsGratis = true (libro marcado como gratis por admin)
            // 2. El usuario tiene el libro (comprado o activado por admin)
            // NO se usa Precio == 0 como condicion de acceso
            var tieneAcceso = l.EsGratis || allUserBooks.Contains(l.IdLibro);
            
            return new LibroDto(
                l.IdLibro,
                l.Titulo,
                l.Autor,
                l.Descripcion,
                // URL de imagen
                l.Imagen != null ? $"{baseUrl}/api/libros/{l.IdLibro}/imagen" : l.ImagenPortada,
                // PDF solo si tiene acceso
                tieneAcceso ? l.UrlPdf : null,
                l.Categoria?.Nombre ?? "",
                "#1565C0",
                l.Precio,
                l.PrecioOferta,
                l.EsGratis,
                l.Rating,
                l.Descargas,
                // Campo "comprado" - indica si tiene acceso
                tieneAcceso
            );
        }).ToList();

        return Ok(new PaginatedResponse<LibroDto>(
            items, total, page, pageSize, (int)Math.Ceiling(total / (double)pageSize)
        ));
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<LibroDto>>> GetById(int id)
    {
        var baseUrl = $"{Request.Scheme}://{Request.Host}";

        int? userId = null;
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (!string.IsNullOrEmpty(userIdClaim))
            userId = int.Parse(userIdClaim);

        var libro = await _context.Libros
            .Include(l => l.Categoria)
            .FirstOrDefaultAsync(l => l.IdLibro == id && l.EsActivo);

        if (libro == null)
            return NotFound(new ApiResponse<LibroDto>(false, "Libro no encontrado", null));

        // VERIFICACION ESTRICTA: tiene acceso SOLO si:
        // 1. EsGratis = true
        // 2. El usuario tiene el libro (comprado o activado)
        // NO se usa Precio == 0
        var tieneAcceso = libro.EsGratis || (userId.HasValue &&
            (await _context.ComprasLibros.AnyAsync(c =>
                c.IdLibro == id && c.IdUsuario == userId.Value && c.EstadoPago == "APROBADO") ||
             await _context.UsuariosLibros.AnyAsync(ul =>
                ul.IdLibro == id && ul.IdUsuario == userId.Value)));

        var dto = new LibroDto(
            libro.IdLibro,
            libro.Titulo,
            libro.Autor,
            libro.Descripcion,
            libro.Imagen != null ? $"{baseUrl}/api/libros/{libro.IdLibro}/imagen" : libro.ImagenPortada,
            tieneAcceso ? libro.UrlPdf : null,
            libro.Categoria?.Nombre ?? "",
            "#1565C0",
            libro.Precio,
            libro.PrecioOferta,
            libro.EsGratis,
            libro.Rating,
            libro.Descargas,
            tieneAcceso
        );

        return Ok(new ApiResponse<LibroDto>(true, null, dto));
    }

    // GET /api/libros/{id}/imagen - Obtener imagen binaria
    [HttpGet("{id}/imagen")]
    public async Task<IActionResult> GetImagen(int id)
    {
        var libro = await _context.Libros
            .Where(l => l.IdLibro == id)
            .Select(l => new { l.Imagen, l.ImagenContentType })
            .FirstOrDefaultAsync();

        if (libro?.Imagen == null)
            return NotFound();

        var contentType = libro.ImagenContentType ?? "image/jpeg";
        return File(libro.Imagen, contentType);
    }

    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ApiResponse<LibroDto>>> Create([FromBody] LibroCreateDtoWithImage dto)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);

        var libro = new Libro
        {
            Titulo = dto.Titulo,
            Autor = dto.Autor,
            Descripcion = dto.Descripcion,
            UrlPdf = dto.PdfUrl ?? "",
            IdCategoria = dto.IdCategoria,
            Precio = dto.Precio,
            PrecioOferta = dto.PrecioOferta,
            EsGratis = dto.EsGratis,
            IdUsuarioCreador = userId
        };

        // Si viene imagen en Base64
        if (!string.IsNullOrEmpty(dto.ImagenBase64))
        {
            try
            {
                var base64Data = dto.ImagenBase64;
                var contentType = "image/jpeg";

                if (base64Data.Contains(","))
                {
                    var parts = base64Data.Split(',');
                    var header = parts[0];
                    base64Data = parts[1];

                    if (header.Contains("image/png")) contentType = "image/png";
                    else if (header.Contains("image/gif")) contentType = "image/gif";
                    else if (header.Contains("image/webp")) contentType = "image/webp";
                }

                libro.Imagen = Convert.FromBase64String(base64Data);
                libro.ImagenContentType = contentType;
            }
            catch (Exception)
            {
                return BadRequest(new ApiResponse<LibroDto>(false, "Imagen Base64 invalida", null));
            }
        }
        else if (!string.IsNullOrEmpty(dto.PortadaUrl))
        {
            libro.ImagenPortada = dto.PortadaUrl;
        }

        _context.Libros.Add(libro);
        await _context.SaveChangesAsync();

        await _context.Entry(libro).Reference(l => l.Categoria).LoadAsync();

        var result = new LibroDto(
            libro.IdLibro,
            libro.Titulo,
            libro.Autor,
            libro.Descripcion,
            libro.Imagen != null ? $"{baseUrl}/api/libros/{libro.IdLibro}/imagen" : libro.ImagenPortada,
            null,
            libro.Categoria?.Nombre ?? "",
            "#1565C0",
            libro.Precio,
            libro.PrecioOferta,
            libro.EsGratis,
            0,
            0,
            false
        );

        return Ok(new ApiResponse<LibroDto>(true, "Libro creado", result));
    }

    [HttpPut("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<LibroDto>>> Update(int id, [FromBody] LibroCreateDtoWithImage dto)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        var libro = await _context.Libros.FindAsync(id);

        if (libro == null)
            return NotFound(new ApiResponse<LibroDto>(false, "Libro no encontrado", null));

        libro.Titulo = dto.Titulo;
        libro.Autor = dto.Autor;
        libro.Descripcion = dto.Descripcion;
        libro.UrlPdf = dto.PdfUrl ?? libro.UrlPdf;
        libro.IdCategoria = dto.IdCategoria;
        libro.Precio = dto.Precio;
        libro.PrecioOferta = dto.PrecioOferta;
        libro.EsGratis = dto.EsGratis;

        // Si viene nueva imagen en Base64
        if (!string.IsNullOrEmpty(dto.ImagenBase64))
        {
            try
            {
                var base64Data = dto.ImagenBase64;
                var contentType = "image/jpeg";

                if (base64Data.Contains(","))
                {
                    var parts = base64Data.Split(',');
                    var header = parts[0];
                    base64Data = parts[1];

                    if (header.Contains("image/png")) contentType = "image/png";
                    else if (header.Contains("image/gif")) contentType = "image/gif";
                    else if (header.Contains("image/webp")) contentType = "image/webp";
                }

                libro.Imagen = Convert.FromBase64String(base64Data);
                libro.ImagenContentType = contentType;
                libro.ImagenPortada = null;
            }
            catch (Exception)
            {
                return BadRequest(new ApiResponse<LibroDto>(false, "Imagen Base64 invalida", null));
            }
        }
        else if (!string.IsNullOrEmpty(dto.PortadaUrl))
        {
            libro.ImagenPortada = dto.PortadaUrl;
        }

        await _context.SaveChangesAsync();
        await _context.Entry(libro).Reference(l => l.Categoria).LoadAsync();

        var result = new LibroDto(
            libro.IdLibro,
            libro.Titulo,
            libro.Autor,
            libro.Descripcion,
            libro.Imagen != null ? $"{baseUrl}/api/libros/{libro.IdLibro}/imagen" : libro.ImagenPortada,
            null,
            libro.Categoria?.Nombre ?? "",
            "#1565C0",
            libro.Precio,
            libro.PrecioOferta,
            libro.EsGratis,
            libro.Rating,
            libro.Descargas,
            false
        );

        return Ok(new ApiResponse<LibroDto>(true, "Libro actualizado", result));
    }

    [HttpPost("{id}/purchase")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Purchase(int id)
    {
        var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);
        var success = await _libroService.PurchaseAsync(id, userId);

        if (!success)
            return NotFound(new ApiResponse<object>(false, "Libro no encontrado", null));

        return Ok(new ApiResponse<object>(true, "Compra registrada exitosamente", null));
    }

    [HttpGet("mis-libros")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<List<LibroDto>>>> GetMyBooks()
    {
        var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);
        var libros = await _libroService.GetUserBooksAsync(userId);
        return Ok(new ApiResponse<List<LibroDto>>(true, null, libros));
    }

    [HttpDelete("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Delete(int id)
    {
        if (!await IsAdmin())
            return Forbid();

        var libro = await _context.Libros.FindAsync(id);
        if (libro == null)
            return NotFound(new ApiResponse<object>(false, "Libro no encontrado", null));

        libro.EsActivo = false;
        await _context.SaveChangesAsync();

        return Ok(new ApiResponse<object>(true, "Libro eliminado", null));
    }
}
