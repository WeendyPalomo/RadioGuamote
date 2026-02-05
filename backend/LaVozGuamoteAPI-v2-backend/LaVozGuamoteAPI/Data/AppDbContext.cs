using Microsoft.EntityFrameworkCore;
using LaVozGuamoteAPI.Models;

namespace LaVozGuamoteAPI.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<Usuario> Usuarios => Set<Usuario>();
    public DbSet<CategoriaEvento> CategoriasEventos => Set<CategoriaEvento>();
    public DbSet<Evento> Eventos => Set<Evento>();
    public DbSet<LikeEvento> LikesEventos => Set<LikeEvento>();
    public DbSet<ComentarioEvento> ComentariosEventos => Set<ComentarioEvento>();
    public DbSet<CategoriaNoticia> CategoriasNoticias => Set<CategoriaNoticia>();
    public DbSet<Noticia> Noticias => Set<Noticia>();
    public DbSet<ImagenNoticia> ImagenesNoticias => Set<ImagenNoticia>();
    public DbSet<CategoriaLibro> CategoriasLibros => Set<CategoriaLibro>();
    public DbSet<Libro> Libros => Set<Libro>();
    public DbSet<CompraLibro> ComprasLibros => Set<CompraLibro>();
    public DbSet<UsuarioLibro> UsuariosLibros => Set<UsuarioLibro>();
    public DbSet<Donacion> Donaciones => Set<Donacion>();
    public DbSet<ConfiguracionRadio> ConfiguracionRadio => Set<ConfiguracionRadio>();
    public DbSet<Anuncio> Anuncios => Set<Anuncio>();
    public DbSet<Podcast> Podcasts => Set<Podcast>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Índices únicos
        modelBuilder.Entity<Usuario>()
            .HasIndex(u => u.Email)
            .IsUnique();
        
        // Índice único para UsuariosLibros
        modelBuilder.Entity<UsuarioLibro>()
            .HasIndex(ul => new { ul.IdUsuario, ul.IdLibro })
            .IsUnique();
    }
}
