using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using LaVozGuamoteAPI.Data;
using LaVozGuamoteAPI.Models;

namespace LaVozGuamoteAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PodcastsController : ControllerBase
{
    private readonly AppDbContext _context;

    public PodcastsController(AppDbContext context) => _context = context;

    private async Task<bool> IsAdmin()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim)) return false;
        var userId = int.Parse(userIdClaim);
        var user = await _context.Usuarios.FindAsync(userId);
        return user?.EsAdmin ?? false;
    }

    // GET /api/podcasts
    [HttpGet]
    public async Task<ActionResult<ApiResponse<List<PodcastDto>>>> GetAll()
    {
        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        
        var podcasts = await _context.Podcasts
            .Where(p => p.EsActivo)
            .OrderByDescending(p => p.FechaPublicacion)
            .Select(p => new PodcastDto
            {
                IdPodcast = p.IdPodcast,
                Titulo = p.Titulo,
                Descripcion = p.Descripcion,
                ImagenUrl = p.Imagen != null 
                    ? $"{baseUrl}/api/podcasts/{p.IdPodcast}/imagen" 
                    : p.ImagenUrl,
                SpotifyUrl = p.SpotifyUrl,
                SpotifyEmbedUrl = p.SpotifyEmbedUrl,
                Duracion = p.Duracion,
                FechaPublicacion = p.FechaPublicacion
            })
            .ToListAsync();

        return Ok(new ApiResponse<List<PodcastDto>>(true, null, podcasts));
    }

    // GET /api/podcasts/{id}
    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<PodcastDto>>> GetById(int id)
    {
        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        var podcast = await _context.Podcasts.FindAsync(id);
        
        if (podcast == null)
            return NotFound(new ApiResponse<PodcastDto>(false, "Podcast no encontrado", null));

        var dto = new PodcastDto
        {
            IdPodcast = podcast.IdPodcast,
            Titulo = podcast.Titulo,
            Descripcion = podcast.Descripcion,
            ImagenUrl = podcast.Imagen != null 
                ? $"{baseUrl}/api/podcasts/{podcast.IdPodcast}/imagen" 
                : podcast.ImagenUrl,
            SpotifyUrl = podcast.SpotifyUrl,
            SpotifyEmbedUrl = podcast.SpotifyEmbedUrl,
            Duracion = podcast.Duracion,
            FechaPublicacion = podcast.FechaPublicacion
        };

        return Ok(new ApiResponse<PodcastDto>(true, null, dto));
    }

    // GET /api/podcasts/{id}/imagen - Obtener imagen binaria
    [HttpGet("{id}/imagen")]
    public async Task<IActionResult> GetImagen(int id)
    {
        var podcast = await _context.Podcasts
            .Where(p => p.IdPodcast == id)
            .Select(p => new { p.Imagen, p.ImagenContentType })
            .FirstOrDefaultAsync();

        if (podcast?.Imagen == null)
            return NotFound();

        var contentType = podcast.ImagenContentType ?? "image/jpeg";
        return File(podcast.Imagen, contentType);
    }

    // POST /api/podcasts
    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ApiResponse<PodcastDto>>> Create([FromBody] PodcastCreateDto dto)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";

        var podcast = new Podcast
        {
            Titulo = dto.Titulo,
            Descripcion = dto.Descripcion,
            SpotifyUrl = dto.SpotifyUrl ?? "", // NOT NULL en la BD
            Duracion = dto.Duracion,
            EsActivo = true
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
                
                podcast.Imagen = Convert.FromBase64String(base64Data);
                podcast.ImagenContentType = contentType;
            }
            catch (Exception)
            {
                return BadRequest(new ApiResponse<PodcastDto>(false, "Imagen Base64 inválida", null));
            }
        }
        else if (!string.IsNullOrEmpty(dto.ImagenUrl))
        {
            podcast.ImagenUrl = dto.ImagenUrl;
        }

        _context.Podcasts.Add(podcast);
        await _context.SaveChangesAsync();

        var result = new PodcastDto
        {
            IdPodcast = podcast.IdPodcast,
            Titulo = podcast.Titulo,
            Descripcion = podcast.Descripcion,
            ImagenUrl = podcast.Imagen != null 
                ? $"{baseUrl}/api/podcasts/{podcast.IdPodcast}/imagen" 
                : podcast.ImagenUrl,
            SpotifyUrl = podcast.SpotifyUrl,
            Duracion = podcast.Duracion,
            FechaPublicacion = podcast.FechaPublicacion
        };

        return Ok(new ApiResponse<PodcastDto>(true, "Podcast creado", result));
    }

    // PUT /api/podcasts/{id}
    [HttpPut("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<PodcastDto>>> Update(int id, [FromBody] PodcastCreateDto dto)
    {
        if (!await IsAdmin())
            return Forbid();

        var baseUrl = $"{Request.Scheme}://{Request.Host}";
        var podcast = await _context.Podcasts.FindAsync(id);
        
        if (podcast == null)
            return NotFound(new ApiResponse<PodcastDto>(false, "Podcast no encontrado", null));

        podcast.Titulo = dto.Titulo;
        podcast.Descripcion = dto.Descripcion;
        podcast.SpotifyUrl = dto.SpotifyUrl ?? podcast.SpotifyUrl;
        podcast.Duracion = dto.Duracion;

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
                
                podcast.Imagen = Convert.FromBase64String(base64Data);
                podcast.ImagenContentType = contentType;
                podcast.ImagenUrl = null;
            }
            catch (Exception)
            {
                return BadRequest(new ApiResponse<PodcastDto>(false, "Imagen Base64 inválida", null));
            }
        }
        else if (!string.IsNullOrEmpty(dto.ImagenUrl))
        {
            podcast.ImagenUrl = dto.ImagenUrl;
        }

        await _context.SaveChangesAsync();

        var result = new PodcastDto
        {
            IdPodcast = podcast.IdPodcast,
            Titulo = podcast.Titulo,
            Descripcion = podcast.Descripcion,
            ImagenUrl = podcast.Imagen != null 
                ? $"{baseUrl}/api/podcasts/{podcast.IdPodcast}/imagen" 
                : podcast.ImagenUrl,
            SpotifyUrl = podcast.SpotifyUrl,
            Duracion = podcast.Duracion,
            FechaPublicacion = podcast.FechaPublicacion
        };

        return Ok(new ApiResponse<PodcastDto>(true, "Podcast actualizado", result));
    }

    // DELETE /api/podcasts/{id}
    [HttpDelete("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Delete(int id)
    {
        if (!await IsAdmin())
            return Forbid();

        var podcast = await _context.Podcasts.FindAsync(id);
        if (podcast == null)
            return NotFound(new ApiResponse<object>(false, "Podcast no encontrado", null));

        _context.Podcasts.Remove(podcast);
        await _context.SaveChangesAsync();

        return Ok(new ApiResponse<object>(true, "Podcast eliminado", null));
    }
}

// DTOs
public class PodcastDto
{
    public int IdPodcast { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public string? ImagenUrl { get; set; }
    public string? SpotifyUrl { get; set; }
    public string? SpotifyEmbedUrl { get; set; }
    public string? Duracion { get; set; }
    public DateTime FechaPublicacion { get; set; }
}

public class PodcastCreateDto
{
    public string Titulo { get; set; } = string.Empty;
    public string? Descripcion { get; set; }
    public string? ImagenUrl { get; set; }
    public string? ImagenBase64 { get; set; }
    public string? SpotifyUrl { get; set; }
    public string? Duracion { get; set; }
}
