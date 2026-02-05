using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using LaVozGuamoteAPI.Data;
using LaVozGuamoteAPI.DTOs;
using LaVozGuamoteAPI.Models;

namespace LaVozGuamoteAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class AdminController : ControllerBase
{
    private readonly AppDbContext _context;

    public AdminController(AppDbContext context) => _context = context;

    private async Task<bool> IsAdmin()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim)) return false;
        var userId = int.Parse(userIdClaim);
        var user = await _context.Usuarios.FindAsync(userId);
        return user?.EsAdmin ?? false;
    }

    private int GetCurrentUserId()
    {
        var claim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        return string.IsNullOrEmpty(claim) ? 0 : int.Parse(claim);
    }

    // GET /api/admin/users/search?q=nombre
    [HttpGet("users/search")]
    public async Task<ActionResult<ApiResponse<List<UsuarioBusquedaDto>>>> SearchUsers([FromQuery] string q)
    {
        if (!await IsAdmin())
            return Forbid();

        if (string.IsNullOrEmpty(q) || q.Length < 2)
            return BadRequest(new ApiResponse<List<UsuarioBusquedaDto>>(false, "Mínimo 2 caracteres", null));

        var usuarios = await _context.Usuarios
            .Where(u => u.EsActivo && 
                (u.Nombre.Contains(q) || u.Apellido.Contains(q) || u.Email.Contains(q)))
            .Take(20)
            .Select(u => new UsuarioBusquedaDto(
                u.IdUsuario, u.Nombre, u.Apellido, u.Email,
                $"{u.Nombre} {u.Apellido}", u.FotoPerfil))
            .ToListAsync();

        return Ok(new ApiResponse<List<UsuarioBusquedaDto>>(true, null, usuarios));
    }

    // POST /api/admin/users/{userId}/books/{bookId}/activate
    [HttpPost("users/{userId}/books/{bookId}/activate")]
    public async Task<ActionResult<ApiResponse<object>>> ActivateBook(int userId, int bookId)
    {
        if (!await IsAdmin())
            return Forbid();

        var usuario = await _context.Usuarios.FindAsync(userId);
        if (usuario == null)
            return NotFound(new ApiResponse<object>(false, "Usuario no encontrado", null));

        var libro = await _context.Libros.FindAsync(bookId);
        if (libro == null)
            return NotFound(new ApiResponse<object>(false, "Libro no encontrado", null));

        var existe = await _context.UsuariosLibros
            .AnyAsync(ul => ul.IdUsuario == userId && ul.IdLibro == bookId);

        if (existe)
            return BadRequest(new ApiResponse<object>(false, 
                $"{usuario.Nombre} ya tiene el libro \"{libro.Titulo}\"", null));

        _context.UsuariosLibros.Add(new UsuarioLibro
        {
            IdUsuario = userId,
            IdLibro = bookId,
            ActivadoPor = GetCurrentUserId()
        });
        await _context.SaveChangesAsync();

        return Ok(new ApiResponse<object>(true, 
            $"Libro \"{libro.Titulo}\" activado para {usuario.Nombre} {usuario.Apellido}", null));
    }

    // DELETE /api/admin/users/{userId}/books/{bookId}/activate
    [HttpDelete("users/{userId}/books/{bookId}/activate")]
    public async Task<ActionResult<ApiResponse<object>>> DeactivateBook(int userId, int bookId)
    {
        if (!await IsAdmin())
            return Forbid();

        var activacion = await _context.UsuariosLibros
            .FirstOrDefaultAsync(ul => ul.IdUsuario == userId && ul.IdLibro == bookId);

        if (activacion == null)
            return NotFound(new ApiResponse<object>(false, "No tiene este libro activado", null));

        _context.UsuariosLibros.Remove(activacion);
        await _context.SaveChangesAsync();

        return Ok(new ApiResponse<object>(true, "Libro desactivado", null));
    }

    // GET /api/admin/books/{bookId}/users
    [HttpGet("books/{bookId}/users")]
    public async Task<ActionResult<ApiResponse<List<UsuarioConActivacionDto>>>> GetBookUsers(int bookId)
    {
        if (!await IsAdmin())
            return Forbid();

        var usuarios = await _context.UsuariosLibros
            .Where(ul => ul.IdLibro == bookId)
            .Include(ul => ul.Usuario)
            .OrderByDescending(ul => ul.FechaActivacion)
            .Select(ul => new UsuarioConActivacionDto(
                ul.Usuario!.IdUsuario,
                ul.Usuario.Nombre,
                ul.Usuario.Apellido,
                ul.Usuario.Email,
                $"{ul.Usuario.Nombre} {ul.Usuario.Apellido}",
                ul.FechaActivacion.ToString("dd/MM/yyyy HH:mm")))
            .ToListAsync();

        return Ok(new ApiResponse<List<UsuarioConActivacionDto>>(true, null, usuarios));
    }

    // GET /api/admin/stats
    [HttpGet("stats")]
    public async Task<ActionResult<ApiResponse<AdminStatsDto>>> GetStats()
    {
        if (!await IsAdmin())
            return Forbid();

        var stats = new AdminStatsDto(
            await _context.Anuncios.CountAsync(a => a.Activo),
            await _context.Podcasts.CountAsync(p => p.EsActivo),
            await _context.Libros.CountAsync(l => l.EsActivo));

        return Ok(new ApiResponse<AdminStatsDto>(true, null, stats));
    }
}

// DTOs para Admin
public record UsuarioBusquedaDto(int IdUsuario, string Nombre, string Apellido, string Email, string NombreCompleto, string? FotoPerfil);
public record UsuarioConActivacionDto(int IdUsuario, string Nombre, string Apellido, string Email, string NombreCompleto, string FechaActivacion);
public record AdminStatsDto(int Anuncios, int Podcasts, int Libros);
