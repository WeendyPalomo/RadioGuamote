using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using LaVozGuamoteAPI.Data;
using LaVozGuamoteAPI.DTOs;
using LaVozGuamoteAPI.Models;

namespace LaVozGuamoteAPI.Controllers;

// ==================== DONACIONES ====================
[ApiController]
[Route("api/[controller]")]
public class DonacionesController : ControllerBase
{
    private readonly AppDbContext _context;

    public DonacionesController(AppDbContext context) => _context = context;

    [HttpPost]
    public async Task<ActionResult<ApiResponse<DonacionDto>>> Create([FromBody] DonacionCreateDto dto)
    {
        int? userId = null;
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (!string.IsNullOrEmpty(userIdClaim))
            userId = int.Parse(userIdClaim);

        var donacion = new Donacion
        {
            IdUsuario = userId,
            NombreDonante = dto.NombreDonante,
            EmailDonante = dto.EmailDonante,
            Monto = dto.Monto,
            MetodoPago = dto.MetodoPago,
            ReferenciaPago = dto.NumeroComprobante,
            Mensaje = dto.Mensaje,
            Estado = "PENDIENTE"
        };

        _context.Donaciones.Add(donacion);
        await _context.SaveChangesAsync();

        var result = new DonacionDto(
            donacion.IdDonacion,
            donacion.NombreDonante,
            donacion.Monto,
            donacion.MetodoPago,
            donacion.Mensaje,
            donacion.Estado,
            donacion.FechaDonacion
        );

        return Ok(new ApiResponse<DonacionDto>(true, "Donación registrada. ¡Gracias por tu apoyo!", result));
    }

    [HttpGet]
    [Authorize]
    public async Task<ActionResult<PaginatedResponse<DonacionDto>>> GetAll(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10)
    {
        var total = await _context.Donaciones.CountAsync();
        
        var items = await _context.Donaciones
            .OrderByDescending(d => d.FechaDonacion)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(d => new DonacionDto(
                d.IdDonacion, d.NombreDonante, d.Monto, d.MetodoPago,
                d.Mensaje, d.Estado, d.FechaDonacion
            ))
            .ToListAsync();

        return Ok(new PaginatedResponse<DonacionDto>(
            items, total, page, pageSize, (int)Math.Ceiling(total / (double)pageSize)
        ));
    }

    [HttpPut("{id}/verificar")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Verificar(int id)
    {
        var donacion = await _context.Donaciones.FindAsync(id);
        if (donacion == null)
            return NotFound(new ApiResponse<object>(false, "Donación no encontrada", null));

        donacion.Estado = "VERIFICADO";
        await _context.SaveChangesAsync();

        return Ok(new ApiResponse<object>(true, "Donación verificada", null));
    }
}

// ==================== CATEGORIAS EVENTOS ====================
[ApiController]
[Route("api/categorias/eventos")]
public class CategoriasEventosController : ControllerBase
{
    private readonly AppDbContext _context;

    public CategoriasEventosController(AppDbContext context) => _context = context;

    [HttpGet]
    public async Task<ActionResult<ApiResponse<List<CategoriaDto>>>> GetAll()
    {
        var categorias = await _context.CategoriasEventos
            .Select(c => new CategoriaDto(c.IdCategoria, c.Nombre, c.Color, c.Icono))
            .ToListAsync();

        return Ok(new ApiResponse<List<CategoriaDto>>(true, null, categorias));
    }
}

// ==================== CATEGORIAS NOTICIAS ====================
[ApiController]
[Route("api/categorias/noticias")]
public class CategoriasNoticiasController : ControllerBase
{
    private readonly AppDbContext _context;

    public CategoriasNoticiasController(AppDbContext context) => _context = context;

    [HttpGet]
    public async Task<ActionResult<ApiResponse<List<CategoriaDto>>>> GetAll()
    {
        var categorias = await _context.CategoriasNoticias
            .Select(c => new CategoriaDto(c.IdCategoria, c.Nombre, c.Color, c.Icono))
            .ToListAsync();

        return Ok(new ApiResponse<List<CategoriaDto>>(true, null, categorias));
    }
}

// ==================== CATEGORIAS LIBROS ====================
[ApiController]
[Route("api/categorias/libros")]
public class CategoriasLibrosController : ControllerBase
{
    private readonly AppDbContext _context;

    public CategoriasLibrosController(AppDbContext context) => _context = context;

    [HttpGet]
    public async Task<ActionResult<ApiResponse<List<CategoriaDto>>>> GetAll()
    {
        var categorias = await _context.CategoriasLibros
            .Select(c => new CategoriaDto(c.IdCategoria, c.Nombre, null, c.Icono))
            .ToListAsync();

        return Ok(new ApiResponse<List<CategoriaDto>>(true, null, categorias));
    }
}

// ==================== RADIO ====================
[ApiController]
[Route("api/[controller]")]
public class RadioController : ControllerBase
{
    private readonly AppDbContext _context;

    public RadioController(AppDbContext context) => _context = context;

    [HttpGet("config")]
    public async Task<ActionResult<ApiResponse<RadioConfigDto>>> GetConfig()
    {
        var config = await _context.ConfiguracionRadio.FirstOrDefaultAsync();
        
        if (config == null)
        {
            return Ok(new ApiResponse<RadioConfigDto>(true, null, new RadioConfigDto(
                "https://usa3.lhdserver.es:8093/stream",
                "La Voz de Guamote",
                "La voz del pueblo",
                null,
                true
            )));
        }

        return Ok(new ApiResponse<RadioConfigDto>(true, null, new RadioConfigDto(
            config.UrlStreaming,
            config.NombreRadio,
            config.Slogan,
            config.UrlLogo,
            true
        )));
    }
}
