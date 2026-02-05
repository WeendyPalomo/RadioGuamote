using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using LaVozGuamoteAPI.DTOs;

namespace LaVozGuamoteAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class UploadController : ControllerBase
{
    private readonly IWebHostEnvironment _env;
    private readonly IConfiguration _config;
    private readonly string[] _allowedExtensions = { ".jpg", ".jpeg", ".png", ".gif", ".webp" };
    private readonly long _maxFileSize = 10 * 1024 * 1024; // 10MB

    public UploadController(IWebHostEnvironment env, IConfiguration config)
    {
        _env = env;
        _config = config;
    }

    // POST /api/upload/image?type=anuncios
    [HttpPost("image")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<UploadResultDto>>> UploadImage(
        IFormFile image,
        [FromQuery] string type = "general")
    {
        if (image == null || image.Length == 0)
            return BadRequest(new ApiResponse<UploadResultDto>(false, "No se proporcionó ninguna imagen", null));

        if (image.Length > _maxFileSize)
            return BadRequest(new ApiResponse<UploadResultDto>(false, "El archivo es muy grande. Máximo 10MB.", null));

        var extension = Path.GetExtension(image.FileName).ToLowerInvariant();
        if (!_allowedExtensions.Contains(extension))
            return BadRequest(new ApiResponse<UploadResultDto>(false, "Tipo no permitido. Solo: JPG, PNG, GIF, WEBP", null));

        var validTypes = new[] { "anuncios", "podcasts", "libros", "perfiles", "general" };
        if (!validTypes.Contains(type.ToLower()))
            type = "general";

        try
        {
            var uploadsPath = Path.Combine(_env.WebRootPath ?? "wwwroot", "uploads", type);
            if (!Directory.Exists(uploadsPath))
                Directory.CreateDirectory(uploadsPath);

            var fileName = $"{Guid.NewGuid()}{extension}";
            var filePath = Path.Combine(uploadsPath, fileName);

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await image.CopyToAsync(stream);
            }

            var baseUrl = _config["BaseUrl"] ?? $"{Request.Scheme}://{Request.Host}";
            var imageUrl = $"{baseUrl}/uploads/{type}/{fileName}";

            return Ok(new ApiResponse<UploadResultDto>(true, "Imagen subida correctamente", 
                new UploadResultDto(imageUrl, fileName)));
        }
        catch (Exception ex)
        {
            return StatusCode(500, new ApiResponse<UploadResultDto>(false, $"Error: {ex.Message}", null));
        }
    }

    // DELETE /api/upload/image/{fileName}?type=anuncios
    [HttpDelete("image/{fileName}")]
    [Authorize]
    public ActionResult<ApiResponse<object>> DeleteImage(string fileName, [FromQuery] string type = "general")
    {
        var validTypes = new[] { "anuncios", "podcasts", "libros", "perfiles", "general" };
        if (!validTypes.Contains(type.ToLower()))
            type = "general";

        var filePath = Path.Combine(_env.WebRootPath ?? "wwwroot", "uploads", type, fileName);

        if (!System.IO.File.Exists(filePath))
            return NotFound(new ApiResponse<object>(false, "Imagen no encontrada", null));

        try
        {
            System.IO.File.Delete(filePath);
            return Ok(new ApiResponse<object>(true, "Imagen eliminada", null));
        }
        catch (Exception ex)
        {
            return StatusCode(500, new ApiResponse<object>(false, $"Error: {ex.Message}", null));
        }
    }
}

public record UploadResultDto(string Url, string FileName);
