using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using LaVozGuamoteAPI.Data;
using LaVozGuamoteAPI.Models;

namespace LaVozGuamoteAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AnunciosController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IWebHostEnvironment _env;

    public AnunciosController(AppDbContext context, IWebHostEnvironment env)
    {
        _context = context;
        _env = env;
    }

    private async Task<bool> IsAdmin()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim)) return false;
        var userId = int.Parse(userIdClaim);
        var user = await _context.Usuarios.FindAsync(userId);
        return user?.EsAdmin ?? false;
    }

    // GET /api/anuncios
    [HttpGet]
    public async Task<ActionResult<ApiResponse<List<AnuncioDto>>>> GetAll()
    {
        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        
        var anuncios = await _context.Anuncios
            .Where(a => a.Activo && (a.FechaExpiracion == null || a.FechaExpiracion > DateTime.UtcNow))
            .OrderByDescending(a => a.FechaCreacion)
            .Select(a => new AnuncioDto
            {
                IdAnuncio = a.IdAnuncio,
                Titulo = a.Titulo,
                Descripcion = a.Descripcion,
                // Si tiene imagen binaria, generar URL para obtenerla; si no, usar ImagenUrl
                ImagenUrl = a.Imagen != null 
                    ? $"{baseUrl}/api/anuncios/{a.IdAnuncio}/imagen" 
                    : a.ImagenUrl,
                EnlaceUrl = a.EnlaceUrl,
                Activo = a.Activo,
                FechaCreacion = a.FechaCreacion
            })
            .ToListAsync();

        return Ok(new ApiResponse<List<AnuncioDto>>(true, null, anuncios));
    }

    // GET /api/anuncios/{id}
    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<AnuncioDto>>> GetById(int id)
    {
        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        var anuncio = await _context.Anuncios.FindAsync(id);
        
        if (anuncio == null)
            return NotFound(new ApiResponse<AnuncioDto>(false, "Anuncio no encontrado", null));

        var dto = new AnuncioDto
        {
            IdAnuncio = anuncio.IdAnuncio,
            Titulo = anuncio.Titulo,
            Descripcion = anuncio.Descripcion,
            ImagenUrl = anuncio.Imagen != null 
                ? $"{baseUrl}/api/anuncios/{anuncio.IdAnuncio}/imagen" 
                : anuncio.ImagenUrl,
            EnlaceUrl = anuncio.EnlaceUrl,
            Activo = anuncio.Activo,
            FechaCreacion = anuncio.FechaCreacion
        };

        return Ok(new ApiResponse<AnuncioDto>(true, null, dto));
    }

    // GET /api/anuncios/{id}/imagen - Obtener imagen binaria
    [HttpGet("{id}/imagen")]
    public async Task<IActionResult> GetImagen(int id)
    {
        var anuncio = await _context.Anuncios
            .Where(a => a.IdAnuncio == id)
            .Select(a => new { a.Imagen, a.ImagenContentType })
            .FirstOrDefaultAsync();

        if (anuncio?.Imagen == null)
            return NotFound();

        var contentType = anuncio.ImagenContentType ?? "image/jpeg";
        return File(anuncio.Imagen, contentType);
    }

    // POST /api/anuncios - Crear con imagen Base64
    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ApiResponse<AnuncioDto>>> Create([FromBody] AnuncioCreateDto dto)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";

        var anuncio = new Anuncio
        {
            Titulo = dto.Titulo,
            Descripcion = dto.Descripcion,
            EnlaceUrl = dto.EnlaceUrl,
            Activo = dto.Activo ?? true
        };

        // Si viene imagen en Base64, convertir a bytes
        if (!string.IsNullOrEmpty(dto.ImagenBase64))
        {
            try
            {
                // Detectar y remover el prefijo data:image/xxx;base64, si existe
                var base64Data = dto.ImagenBase64;
                var contentType = "image/jpeg"; // default
                
                if (base64Data.Contains(","))
                {
                    var parts = base64Data.Split(',');
                    var header = parts[0];
                    base64Data = parts[1];
                    
                    // Extraer content type del header
                    if (header.Contains("image/png"))
                        contentType = "image/png";
                    else if (header.Contains("image/gif"))
                        contentType = "image/gif";
                    else if (header.Contains("image/webp"))
                        contentType = "image/webp";
                }
                
                anuncio.Imagen = Convert.FromBase64String(base64Data);
                anuncio.ImagenContentType = contentType;
            }
            catch (Exception)
            {
                return BadRequest(new ApiResponse<AnuncioDto>(false, "Imagen Base64 inválida", null));
            }
        }
        else if (!string.IsNullOrEmpty(dto.ImagenUrl))
        {
            // Si no hay Base64 pero hay URL, guardar la URL
            anuncio.ImagenUrl = dto.ImagenUrl;
        }

        _context.Anuncios.Add(anuncio);
        await _context.SaveChangesAsync();

        var result = new AnuncioDto
        {
            IdAnuncio = anuncio.IdAnuncio,
            Titulo = anuncio.Titulo,
            Descripcion = anuncio.Descripcion,
            ImagenUrl = anuncio.Imagen != null 
                ? $"{baseUrl}/api/anuncios/{anuncio.IdAnuncio}/imagen" 
                : anuncio.ImagenUrl,
            EnlaceUrl = anuncio.EnlaceUrl,
            Activo = anuncio.Activo,
            FechaCreacion = anuncio.FechaCreacion
        };

        return Ok(new ApiResponse<AnuncioDto>(true, "Anuncio creado", result));
    }

    // POST /api/anuncios/upload - Crear con imagen como archivo (multipart/form-data)
    [HttpPost("upload")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<AnuncioDto>>> CreateWithFile(
        [FromForm] string titulo,
        [FromForm] string? descripcion,
        [FromForm] string? enlaceUrl,
        [FromForm] bool? activo,
        [FromForm] IFormFile? imagen)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";

        var anuncio = new Anuncio
        {
            Titulo = titulo,
            Descripcion = descripcion,
            EnlaceUrl = enlaceUrl,
            Activo = activo ?? true
        };

        // Si viene archivo de imagen
        if (imagen != null && imagen.Length > 0)
        {
            using var ms = new MemoryStream();
            await imagen.CopyToAsync(ms);
            anuncio.Imagen = ms.ToArray();
            anuncio.ImagenContentType = imagen.ContentType;
        }

        _context.Anuncios.Add(anuncio);
        await _context.SaveChangesAsync();

        var result = new AnuncioDto
        {
            IdAnuncio = anuncio.IdAnuncio,
            Titulo = anuncio.Titulo,
            Descripcion = anuncio.Descripcion,
            ImagenUrl = anuncio.Imagen != null 
                ? $"{baseUrl}/api/anuncios/{anuncio.IdAnuncio}/imagen" 
                : null,
            EnlaceUrl = anuncio.EnlaceUrl,
            Activo = anuncio.Activo,
            FechaCreacion = anuncio.FechaCreacion
        };

        return Ok(new ApiResponse<AnuncioDto>(true, "Anuncio creado con imagen", result));
    }

    // PUT /api/anuncios/{id}
    [HttpPut("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<AnuncioDto>>> Update(int id, [FromBody] AnuncioCreateDto dto)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        var anuncio = await _context.Anuncios.FindAsync(id);
        
        if (anuncio == null)
            return NotFound(new ApiResponse<AnuncioDto>(false, "Anuncio no encontrado", null));

        anuncio.Titulo = dto.Titulo;
        anuncio.Descripcion = dto.Descripcion;
        anuncio.EnlaceUrl = dto.EnlaceUrl;
        anuncio.Activo = dto.Activo ?? anuncio.Activo;

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
                    
                    if (header.Contains("image/png"))
                        contentType = "image/png";
                    else if (header.Contains("image/gif"))
                        contentType = "image/gif";
                    else if (header.Contains("image/webp"))
                        contentType = "image/webp";
                }
                
                anuncio.Imagen = Convert.FromBase64String(base64Data);
                anuncio.ImagenContentType = contentType;
                anuncio.ImagenUrl = null; // Limpiar URL si ahora usa binario
            }
            catch (Exception)
            {
                return BadRequest(new ApiResponse<AnuncioDto>(false, "Imagen Base64 inválida", null));
            }
        }
        else if (!string.IsNullOrEmpty(dto.ImagenUrl))
        {
            anuncio.ImagenUrl = dto.ImagenUrl;
        }

        await _context.SaveChangesAsync();

        var result = new AnuncioDto
        {
            IdAnuncio = anuncio.IdAnuncio,
            Titulo = anuncio.Titulo,
            Descripcion = anuncio.Descripcion,
            ImagenUrl = anuncio.Imagen != null 
                ? $"{baseUrl}/api/anuncios/{anuncio.IdAnuncio}/imagen" 
                : anuncio.ImagenUrl,
            EnlaceUrl = anuncio.EnlaceUrl,
            Activo = anuncio.Activo,
            FechaCreacion = anuncio.FechaCreacion
        };

        return Ok(new ApiResponse<AnuncioDto>(true, "Anuncio actualizado", result));
    }

    // PUT /api/anuncios/{id}/upload - Actualizar con archivo
    [HttpPut("{id}/upload")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<AnuncioDto>>> UpdateWithFile(
        int id,
        [FromForm] string titulo,
        [FromForm] string? descripcion,
        [FromForm] string? enlaceUrl,
        [FromForm] bool? activo,
        [FromForm] IFormFile? imagen)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        var anuncio = await _context.Anuncios.FindAsync(id);
        
        if (anuncio == null)
            return NotFound(new ApiResponse<AnuncioDto>(false, "Anuncio no encontrado", null));

        anuncio.Titulo = titulo;
        anuncio.Descripcion = descripcion;
        anuncio.EnlaceUrl = enlaceUrl;
        anuncio.Activo = activo ?? anuncio.Activo;

        // Si viene nuevo archivo de imagen
        if (imagen != null && imagen.Length > 0)
        {
            using var ms = new MemoryStream();
            await imagen.CopyToAsync(ms);
            anuncio.Imagen = ms.ToArray();
            anuncio.ImagenContentType = imagen.ContentType;
            anuncio.ImagenUrl = null;
        }

        await _context.SaveChangesAsync();

        var result = new AnuncioDto
        {
            IdAnuncio = anuncio.IdAnuncio,
            Titulo = anuncio.Titulo,
            Descripcion = anuncio.Descripcion,
            ImagenUrl = anuncio.Imagen != null 
                ? $"{baseUrl}/api/anuncios/{anuncio.IdAnuncio}/imagen" 
                : anuncio.ImagenUrl,
            EnlaceUrl = anuncio.EnlaceUrl,
            Activo = anuncio.Activo,
            FechaCreacion = anuncio.FechaCreacion
        };

        return Ok(new ApiResponse<AnuncioDto>(true, "Anuncio actualizado", result));
    }

    // DELETE /api/anuncios/{id}
    [HttpDelete("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Delete(int id)
    {
        if (!await IsAdmin())
            return Forbid();

        var anuncio = await _context.Anuncios.FindAsync(id);
        if (anuncio == null)
            return NotFound(new ApiResponse<object>(false, "Anuncio no encontrado", null));

        _context.Anuncios.Remove(anuncio);
        await _context.SaveChangesAsync();

        return Ok(new ApiResponse<object>(true, "Anuncio eliminado", null));
    }
}

// DTOs
public class AnuncioDto
{
    public int IdAnuncio { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public string? ImagenUrl { get; set; }
    public string? EnlaceUrl { get; set; }
    public bool Activo { get; set; }
    public DateTime FechaCreacion { get; set; }
}

public class AnuncioCreateDto
{
    public string Titulo { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public string? ImagenUrl { get; set; }
    public string? ImagenBase64 { get; set; } // NUEVO: Para recibir imagen como Base64
    public string? EnlaceUrl { get; set; }
    public bool? Activo { get; set; }
}

// Response wrapper
public class ApiResponse<T>
{
    public bool Success { get; set; }
    public string? Message { get; set; }
    public T? Data { get; set; }

    public ApiResponse(bool success, string? message, T? data)
    {
        Success = success;
        Message = message;
        Data = data;
    }
}
