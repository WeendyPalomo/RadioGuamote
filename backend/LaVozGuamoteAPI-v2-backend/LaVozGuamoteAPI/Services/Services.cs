using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using LaVozGuamoteAPI.Data;
using LaVozGuamoteAPI.Models;
using LaVozGuamoteAPI.DTOs;

namespace LaVozGuamoteAPI.Services;

// ==================== AUTH SERVICE ====================
public interface IAuthService
{
    Task<AuthResponse> LoginAsync(LoginRequest request);
    Task<AuthResponse> RegisterAsync(RegisterRequest request);
    Task<UsuarioDto?> GetUserByIdAsync(int id);
}

public class AuthService : IAuthService
{
    private readonly AppDbContext _context;
    private readonly IConfiguration _config;

    public AuthService(AppDbContext context, IConfiguration config)
    {
        _context = context;
        _config = config;
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        var user = await _context.Usuarios
            .FirstOrDefaultAsync(u => u.Email == request.Email && u.EsActivo);

        if (user == null || !BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
            return new AuthResponse(false, null, "Email o contraseña incorrectos", null);

        var token = GenerateJwtToken(user);
        var userDto = MapToDto(user);

        return new AuthResponse(true, token, "Login exitoso", userDto);
    }

    public async Task<AuthResponse> RegisterAsync(RegisterRequest request)
    {
        if (await _context.Usuarios.AnyAsync(u => u.Email == request.Email))
            return new AuthResponse(false, null, "El email ya está registrado", null);

        var user = new Usuario
        {
            Nombre = request.Nombre,
            Apellido = request.Apellido,
            Email = request.Email,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
            Telefono = request.Telefono,
            EsAdmin = false,
            EsActivo = true
        };

        _context.Usuarios.Add(user);
        await _context.SaveChangesAsync();

        var token = GenerateJwtToken(user);
        var userDto = MapToDto(user);

        return new AuthResponse(true, token, "Registro exitoso", userDto);
    }

    public async Task<UsuarioDto?> GetUserByIdAsync(int id)
    {
        var user = await _context.Usuarios.FindAsync(id);
        return user == null ? null : MapToDto(user);
    }

    private string GenerateJwtToken(Usuario user)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(
            _config["Jwt:Key"] ?? "LaVozDeGuamote2024SecretKey123456"));
        
        var claims = new[]
        {
            new Claim(ClaimTypes.NameIdentifier, user.IdUsuario.ToString()),
            new Claim(ClaimTypes.Email, user.Email),
            new Claim(ClaimTypes.Name, $"{user.Nombre} {user.Apellido}"),
            new Claim(ClaimTypes.Role, user.EsAdmin ? "Admin" : "Usuario")
        };

        var token = new JwtSecurityToken(
            issuer: _config["Jwt:Issuer"] ?? "LaVozGuamote",
            audience: _config["Jwt:Audience"] ?? "LaVozGuamoteApp",
            claims: claims,
            expires: DateTime.UtcNow.AddMinutes(
                int.Parse(_config["Jwt:ExpireMinutes"] ?? "10080")),
            signingCredentials: new SigningCredentials(key, SecurityAlgorithms.HmacSha256)
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    private static UsuarioDto MapToDto(Usuario u) => new(
        u.IdUsuario, u.Nombre, u.Apellido, u.Email, 
        "", u.Telefono, u.EsAdmin ? "Admin" : "Usuario", u.FotoPerfil
    );
}

// ==================== EVENTO SERVICE ====================
public interface IEventoService
{
    Task<PaginatedResponse<EventoDto>> GetAllAsync(int page, int pageSize, int? categoriaId);
    Task<EventoDto?> GetByIdAsync(int id);
    Task<EventoDto> CreateAsync(EventoCreateDto dto, int userId);
    Task<bool> UpdateAsync(int id, EventoCreateDto dto);
    Task<bool> DeleteAsync(int id);
    Task<bool> LikeAsync(int id, int userId);
}

public class EventoService : IEventoService
{
    private readonly AppDbContext _context;

    public EventoService(AppDbContext context) => _context = context;

    public async Task<PaginatedResponse<EventoDto>> GetAllAsync(int page, int pageSize, int? categoriaId)
    {
        var query = _context.Eventos
            .Include(e => e.Categoria)
            .Where(e => e.EsActivo)
            .AsQueryable();

        if (categoriaId.HasValue)
            query = query.Where(e => e.IdCategoria == categoriaId.Value);

        var total = await query.CountAsync();
        
        var items = await query
            .OrderByDescending(e => e.FechaEvento)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(e => new EventoDto(
                e.IdEvento,
                e.Titulo,
                e.Descripcion,
                e.FechaEvento,
                e.Direccion,
                e.ImagenPrincipal,
                e.Precio,
                e.Categoria != null ? e.Categoria.Nombre : "",
                e.Categoria != null ? e.Categoria.Color : "#1565C0",
                e.NombreEncargado,
                e.TelefonoEncargado,
                _context.LikesEventos.Count(l => l.IdEvento == e.IdEvento),
                _context.ComentariosEventos.Count(c => c.IdEvento == e.IdEvento && c.EsActivo)
            ))
            .ToListAsync();

        return new PaginatedResponse<EventoDto>(
            items, total, page, pageSize, (int)Math.Ceiling(total / (double)pageSize)
        );
    }

    public async Task<EventoDto?> GetByIdAsync(int id)
    {
        var evento = await _context.Eventos
            .Include(e => e.Categoria)
            .FirstOrDefaultAsync(e => e.IdEvento == id && e.EsActivo);
        
        if (evento == null) return null;

        var likes = await _context.LikesEventos.CountAsync(l => l.IdEvento == id);
        var comentarios = await _context.ComentariosEventos.CountAsync(c => c.IdEvento == id && c.EsActivo);

        return new EventoDto(
            evento.IdEvento,
            evento.Titulo,
            evento.Descripcion,
            evento.FechaEvento,
            evento.Direccion,
            evento.ImagenPrincipal,
            evento.Precio,
            evento.Categoria?.Nombre ?? "",
            evento.Categoria?.Color ?? "#1565C0",
            evento.NombreEncargado,
            evento.TelefonoEncargado,
            likes,
            comentarios
        );
    }

    public async Task<EventoDto> CreateAsync(EventoCreateDto dto, int userId)
    {
        var evento = new Evento
        {
            Titulo = dto.Titulo,
            Descripcion = dto.Descripcion,
            FechaEvento = dto.FechaEvento,
            HoraInicio = TimeSpan.FromHours(8),
            Direccion = dto.Lugar,
            ImagenPrincipal = dto.ImagenUrl,
            Precio = dto.Precio,
            IdCategoria = dto.IdCategoria,
            NombreEncargado = dto.NombreEncargado,
            TelefonoEncargado = dto.TelefonoEncargado,
            IdUsuarioCreador = userId
        };

        _context.Eventos.Add(evento);
        await _context.SaveChangesAsync();

        await _context.Entry(evento).Reference(e => e.Categoria).LoadAsync();
        
        return new EventoDto(
            evento.IdEvento, evento.Titulo, evento.Descripcion, evento.FechaEvento,
            evento.Direccion, evento.ImagenPrincipal, evento.Precio,
            evento.Categoria?.Nombre ?? "", evento.Categoria?.Color ?? "#1565C0",
            evento.NombreEncargado, evento.TelefonoEncargado, 0, 0
        );
    }

    public async Task<bool> UpdateAsync(int id, EventoCreateDto dto)
    {
        var evento = await _context.Eventos.FindAsync(id);
        if (evento == null) return false;

        evento.Titulo = dto.Titulo;
        evento.Descripcion = dto.Descripcion;
        evento.FechaEvento = dto.FechaEvento;
        evento.Direccion = dto.Lugar;
        evento.ImagenPrincipal = dto.ImagenUrl;
        evento.Precio = dto.Precio;
        evento.IdCategoria = dto.IdCategoria;
        evento.NombreEncargado = dto.NombreEncargado;
        evento.TelefonoEncargado = dto.TelefonoEncargado;

        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<bool> DeleteAsync(int id)
    {
        var evento = await _context.Eventos.FindAsync(id);
        if (evento == null) return false;

        evento.EsActivo = false;
        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<bool> LikeAsync(int id, int userId)
    {
        var evento = await _context.Eventos.FindAsync(id);
        if (evento == null) return false;

        var existingLike = await _context.LikesEventos
            .FirstOrDefaultAsync(l => l.IdEvento == id && l.IdUsuario == userId);

        if (existingLike != null)
        {
            _context.LikesEventos.Remove(existingLike);
        }
        else
        {
            _context.LikesEventos.Add(new LikeEvento
            {
                IdEvento = id,
                IdUsuario = userId
            });
        }

        await _context.SaveChangesAsync();
        return true;
    }
}

// ==================== NOTICIA SERVICE ====================
public interface INoticiaService
{
    Task<PaginatedResponse<NoticiaDto>> GetAllAsync(int page, int pageSize, int? categoriaId);
    Task<List<NoticiaDto>> GetLatestAsync(int count);
    Task<NoticiaDto?> GetByIdAsync(int id);
    Task<NoticiaDto> CreateAsync(NoticiaCreateDto dto, int userId);
    Task<bool> UpdateAsync(int id, NoticiaCreateDto dto);
    Task<bool> DeleteAsync(int id);
}

public class NoticiaService : INoticiaService
{
    private readonly AppDbContext _context;

    public NoticiaService(AppDbContext context) => _context = context;

    public async Task<PaginatedResponse<NoticiaDto>> GetAllAsync(int page, int pageSize, int? categoriaId)
    {
        var query = _context.Noticias
            .Include(n => n.Categoria)
            .Where(n => n.EsActivo)
            .AsQueryable();

        if (categoriaId.HasValue)
            query = query.Where(n => n.IdCategoria == categoriaId.Value);

        var total = await query.CountAsync();
        
        var noticias = await query
            .OrderByDescending(n => n.FechaPublicacion)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var items = new List<NoticiaDto>();
        foreach (var n in noticias)
        {
            var imagenes = await _context.ImagenesNoticias
                .Where(i => i.IdNoticia == n.IdNoticia)
                .OrderBy(i => i.Orden)
                .Select(i => i.UrlImagen)
                .ToListAsync();

            items.Add(new NoticiaDto(
                n.IdNoticia, n.Titulo, n.Subtitulo, n.Contenido, n.Resumen,
                n.ImagenPrincipal, imagenes.Any() ? imagenes : null,
                n.Categoria?.Nombre ?? "", n.Categoria?.Color ?? "#1565C0",
                n.Autor, n.Vistas, n.EsUrgente, n.EsDestacada, n.FechaPublicacion
            ));
        }

        return new PaginatedResponse<NoticiaDto>(
            items, total, page, pageSize, (int)Math.Ceiling(total / (double)pageSize)
        );
    }

    public async Task<List<NoticiaDto>> GetLatestAsync(int count)
    {
        var noticias = await _context.Noticias
            .Include(n => n.Categoria)
            .Where(n => n.EsActivo)
            .OrderByDescending(n => n.FechaPublicacion)
            .Take(count)
            .ToListAsync();

        return noticias.Select(n => new NoticiaDto(
            n.IdNoticia, n.Titulo, n.Subtitulo, n.Contenido, n.Resumen,
            n.ImagenPrincipal, null,
            n.Categoria?.Nombre ?? "", n.Categoria?.Color ?? "#1565C0",
            n.Autor, n.Vistas, n.EsUrgente, n.EsDestacada, n.FechaPublicacion
        )).ToList();
    }

    public async Task<NoticiaDto?> GetByIdAsync(int id)
    {
        var noticia = await _context.Noticias
            .Include(n => n.Categoria)
            .FirstOrDefaultAsync(n => n.IdNoticia == id && n.EsActivo);

        if (noticia == null) return null;

        noticia.Vistas++;
        await _context.SaveChangesAsync();

        var imagenes = await _context.ImagenesNoticias
            .Where(i => i.IdNoticia == id)
            .OrderBy(i => i.Orden)
            .Select(i => i.UrlImagen)
            .ToListAsync();

        return new NoticiaDto(
            noticia.IdNoticia, noticia.Titulo, noticia.Subtitulo, noticia.Contenido,
            noticia.Resumen, noticia.ImagenPrincipal, imagenes.Any() ? imagenes : null,
            noticia.Categoria?.Nombre ?? "", noticia.Categoria?.Color ?? "#1565C0",
            noticia.Autor, noticia.Vistas, noticia.EsUrgente, noticia.EsDestacada,
            noticia.FechaPublicacion
        );
    }

    public async Task<NoticiaDto> CreateAsync(NoticiaCreateDto dto, int userId)
    {
        var noticia = new Noticia
        {
            Titulo = dto.Titulo,
            Subtitulo = dto.Subtitulo,
            Contenido = dto.Contenido,
            Resumen = dto.Resumen,
            ImagenPrincipal = dto.ImagenUrl,
            IdCategoria = dto.IdCategoria,
            Autor = dto.Autor,
            EsUrgente = dto.EsUrgente,
            EsDestacada = dto.EsDestacada,
            IdUsuarioCreador = userId
        };

        _context.Noticias.Add(noticia);
        await _context.SaveChangesAsync();

        if (dto.GaleriaImagenes != null)
        {
            int orden = 0;
            foreach (var url in dto.GaleriaImagenes)
            {
                _context.ImagenesNoticias.Add(new ImagenNoticia
                {
                    IdNoticia = noticia.IdNoticia,
                    UrlImagen = url,
                    Orden = orden++
                });
            }
            await _context.SaveChangesAsync();
        }

        await _context.Entry(noticia).Reference(n => n.Categoria).LoadAsync();
        
        return new NoticiaDto(
            noticia.IdNoticia, noticia.Titulo, noticia.Subtitulo, noticia.Contenido,
            noticia.Resumen, noticia.ImagenPrincipal, dto.GaleriaImagenes,
            noticia.Categoria?.Nombre ?? "", noticia.Categoria?.Color ?? "#1565C0",
            noticia.Autor, 0, noticia.EsUrgente, noticia.EsDestacada, noticia.FechaPublicacion
        );
    }

    public async Task<bool> UpdateAsync(int id, NoticiaCreateDto dto)
    {
        var noticia = await _context.Noticias.FindAsync(id);
        if (noticia == null) return false;

        noticia.Titulo = dto.Titulo;
        noticia.Subtitulo = dto.Subtitulo;
        noticia.Contenido = dto.Contenido;
        noticia.Resumen = dto.Resumen;
        noticia.ImagenPrincipal = dto.ImagenUrl;
        noticia.IdCategoria = dto.IdCategoria;
        noticia.Autor = dto.Autor;
        noticia.EsUrgente = dto.EsUrgente;
        noticia.EsDestacada = dto.EsDestacada;

        await _context.SaveChangesAsync();
        return true;
    }

    public async Task<bool> DeleteAsync(int id)
    {
        var noticia = await _context.Noticias.FindAsync(id);
        if (noticia == null) return false;

        noticia.EsActivo = false;
        await _context.SaveChangesAsync();
        return true;
    }
}

// ==================== LIBRO SERVICE ====================
public interface ILibroService
{
    Task<PaginatedResponse<LibroDto>> GetAllAsync(int page, int pageSize, int? categoriaId, int? userId);
    Task<LibroDto?> GetByIdAsync(int id, int? userId);
    Task<LibroDto> CreateAsync(LibroCreateDto dto, int userId);
    Task<bool> PurchaseAsync(int libroId, int userId);
    Task<List<LibroDto>> GetUserBooksAsync(int userId);
}

public class LibroService : ILibroService
{
    private readonly AppDbContext _context;

    public LibroService(AppDbContext context) => _context = context;

    public async Task<PaginatedResponse<LibroDto>> GetAllAsync(int page, int pageSize, int? categoriaId, int? userId)
    {
        var query = _context.Libros
            .Include(l => l.Categoria)
            .Where(l => l.EsActivo)
            .AsQueryable();

        if (categoriaId.HasValue)
            query = query.Where(l => l.IdCategoria == categoriaId.Value);

        var total = await query.CountAsync();
        
        var userBooks = userId.HasValue ? 
            await _context.ComprasLibros
                .Where(c => c.IdUsuario == userId.Value && c.EstadoPago == "APROBADO")
                .Select(c => c.IdLibro)
                .ToListAsync() : 
            new List<int>();

        var libros = await query
            .OrderByDescending(l => l.FechaPublicacion)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        var items = libros.Select(l => new LibroDto(
            l.IdLibro, l.Titulo, l.Autor, l.Descripcion, l.ImagenPortada,
            userBooks.Contains(l.IdLibro) || l.Precio == 0 ? l.UrlPdf : null,
            l.Categoria?.Nombre ?? "", "#1565C0",
            l.Precio, l.PrecioOferta, l.Precio == 0,
            l.Rating, l.Descargas, userBooks.Contains(l.IdLibro) || l.Precio == 0
        )).ToList();

        return new PaginatedResponse<LibroDto>(
            items, total, page, pageSize, (int)Math.Ceiling(total / (double)pageSize)
        );
    }

    public async Task<LibroDto?> GetByIdAsync(int id, int? userId)
    {
        var libro = await _context.Libros
            .Include(l => l.Categoria)
            .FirstOrDefaultAsync(l => l.IdLibro == id && l.EsActivo);

        if (libro == null) return null;

        var comprado = libro.Precio == 0 || (userId.HasValue && 
            await _context.ComprasLibros.AnyAsync(c => 
                c.IdLibro == id && c.IdUsuario == userId.Value && c.EstadoPago == "APROBADO"));

        return new LibroDto(
            libro.IdLibro, libro.Titulo, libro.Autor, libro.Descripcion,
            libro.ImagenPortada, comprado ? libro.UrlPdf : null,
            libro.Categoria?.Nombre ?? "", "#1565C0",
            libro.Precio, libro.PrecioOferta, libro.Precio == 0,
            libro.Rating, libro.Descargas, comprado
        );
    }

    public async Task<LibroDto> CreateAsync(LibroCreateDto dto, int userId)
    {
        var libro = new Libro
        {
            Titulo = dto.Titulo,
            Autor = dto.Autor,
            Descripcion = dto.Descripcion,
            ImagenPortada = dto.PortadaUrl,
            UrlPdf = dto.PdfUrl,
            IdCategoria = dto.IdCategoria,
            Precio = dto.Precio,
            PrecioOferta = dto.PrecioOferta,
            IdUsuarioCreador = userId
        };

        _context.Libros.Add(libro);
        await _context.SaveChangesAsync();

        await _context.Entry(libro).Reference(l => l.Categoria).LoadAsync();
        
        return new LibroDto(
            libro.IdLibro, libro.Titulo, libro.Autor, libro.Descripcion,
            libro.ImagenPortada, null, libro.Categoria?.Nombre ?? "", "#1565C0",
            libro.Precio, libro.PrecioOferta, libro.Precio == 0, 0, 0, false
        );
    }

    public async Task<bool> PurchaseAsync(int libroId, int userId)
    {
        var libro = await _context.Libros.FindAsync(libroId);
        if (libro == null) return false;

        var exists = await _context.ComprasLibros
            .AnyAsync(c => c.IdLibro == libroId && c.IdUsuario == userId);
        if (exists) return true;

        var purchase = new CompraLibro
        {
            IdLibro = libroId,
            IdUsuario = userId,
            PrecioCompra = libro.PrecioOferta ?? libro.Precio,
            EstadoPago = "PENDIENTE"
        };

        _context.ComprasLibros.Add(purchase);
        await _context.SaveChangesAsync();

        return true;
    }

    public async Task<List<LibroDto>> GetUserBooksAsync(int userId)
    {
        var compras = await _context.ComprasLibros
            .Include(c => c.Libro)
            .ThenInclude(l => l!.Categoria)
            .Where(c => c.IdUsuario == userId && c.EstadoPago == "APROBADO")
            .ToListAsync();

        return compras.Select(c => new LibroDto(
            c.Libro!.IdLibro, c.Libro.Titulo, c.Libro.Autor, c.Libro.Descripcion,
            c.Libro.ImagenPortada, c.Libro.UrlPdf,
            c.Libro.Categoria?.Nombre ?? "", "#1565C0",
            c.Libro.Precio, c.Libro.PrecioOferta, c.Libro.Precio == 0,
            c.Libro.Rating, c.Libro.Descargas, true
        )).ToList();
    }
}
