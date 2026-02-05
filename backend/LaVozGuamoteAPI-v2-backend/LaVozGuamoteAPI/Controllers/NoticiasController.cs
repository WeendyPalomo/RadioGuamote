using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;
using LaVozGuamoteAPI.DTOs;
using LaVozGuamoteAPI.Services;

namespace LaVozGuamoteAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class NoticiasController : ControllerBase
{
    private readonly INoticiaService _noticiaService;

    public NoticiasController(INoticiaService noticiaService) => _noticiaService = noticiaService;

    [HttpGet]
    public async Task<ActionResult<PaginatedResponse<NoticiaDto>>> GetAll(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10,
        [FromQuery] int? categoriaId = null)
    {
        var result = await _noticiaService.GetAllAsync(page, pageSize, categoriaId);
        return Ok(result);
    }

    [HttpGet("latest")]
    public async Task<ActionResult<ApiResponse<List<NoticiaDto>>>> GetLatest([FromQuery] int count = 5)
    {
        var noticias = await _noticiaService.GetLatestAsync(count);
        return Ok(new ApiResponse<List<NoticiaDto>>(true, null, noticias));
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponse<NoticiaDto>>> GetById(int id)
    {
        var noticia = await _noticiaService.GetByIdAsync(id);
        if (noticia == null)
            return NotFound(new ApiResponse<NoticiaDto>(false, "Noticia no encontrada", null));
        return Ok(new ApiResponse<NoticiaDto>(true, null, noticia));
    }

    [HttpPost]
    [Authorize]
    public async Task<ActionResult<ApiResponse<NoticiaDto>>> Create([FromBody] NoticiaCreateDto dto)
    {
        var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)!.Value);
        var noticia = await _noticiaService.CreateAsync(dto, userId);
        return CreatedAtAction(nameof(GetById), new { id = noticia.IdNoticia },
            new ApiResponse<NoticiaDto>(true, "Noticia creada", noticia));
    }

    [HttpPut("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Update(int id, [FromBody] NoticiaCreateDto dto)
    {
        var success = await _noticiaService.UpdateAsync(id, dto);
        if (!success)
            return NotFound(new ApiResponse<object>(false, "Noticia no encontrada", null));
        return Ok(new ApiResponse<object>(true, "Noticia actualizada", null));
    }

    [HttpDelete("{id}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<object>>> Delete(int id)
    {
        var success = await _noticiaService.DeleteAsync(id);
        if (!success)
            return NotFound(new ApiResponse<object>(false, "Noticia no encontrada", null));
        return Ok(new ApiResponse<object>(true, "Noticia eliminada", null));
    }
}
