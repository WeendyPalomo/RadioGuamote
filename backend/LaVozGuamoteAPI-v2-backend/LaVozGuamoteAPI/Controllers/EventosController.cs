using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;
using LaVozGuamoteAPI.DTOs;
using LaVozGuamoteAPI.Services;

namespace LaVozGuamoteAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class EventosController : ControllerBase
{
    private readonly IEventoService _eventoService;

    public EventosController(IEventoService eventoService) => _eventoService = eventoService;

    [HttpGet]
    public async Task<ActionResult<PaginatedResponse<EventoDto>>> GetAll(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10,
        [FromQuery] int? categoriaId = null)
    {
        var result = await _eventoService.GetAllAsync(page, pageSize, categoriaId);
        return Ok(result);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<EventoDto>>> GetById(int id)
    {
        var evento = await _eventoService.GetByIdAsync(id);
        if (evento == null)
            return NotFound(new ApiResponse<EventoDto>(false, "Evento no encontrado", null));
        return Ok(new ApiResponse<EventoDto>(true, null, evento));
    }

    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ApiResponse<EventoDto>>> Create([FromBody] EventoCreateDto dto)
    {
        var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);
        var evento = await _eventoService.CreateAsync(dto, userId);
        return CreatedAtAction(nameof(GetById), new { id = evento.IdEvento }, 
            new ApiResponse<EventoDto>(true, "Evento creado", evento));
    }

    [HttpPut("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Update(int id, [FromBody] EventoCreateDto dto)
    {
        var success = await _eventoService.UpdateAsync(id, dto);
        if (!success)
            return NotFound(new ApiResponse<object>(false, "Evento no encontrado", null));
        return Ok(new ApiResponse<object>(true, "Evento actualizado", null));
    }

    [HttpDelete("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Delete(int id)
    {
        var success = await _eventoService.DeleteAsync(id);
        if (!success)
            return NotFound(new ApiResponse<object>(false, "Evento no encontrado", null));
        return Ok(new ApiResponse<object>(true, "Evento eliminado", null));
    }

    [HttpPost("{id}/like")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Like(int id)
    {
        var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);
        var success = await _eventoService.LikeAsync(id, userId);
        if (!success)
            return NotFound(new ApiResponse<object>(false, "Evento no encontrado", null));
        return Ok(new ApiResponse<object>(true, "Like actualizado", null));
    }
}
