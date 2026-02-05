-- ============================================
-- SQL PARA LA VOZ DE GUAMOTE - SQL SERVER
-- Ejecutar en SQL Server Management Studio
-- ============================================

USE LaVozGuamoteDB;
GO

-- Tabla UsuariosLibros (Activación de libros por admin)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'UsuariosLibros')
BEGIN
    CREATE TABLE UsuariosLibros (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_usuario INT NOT NULL,
        id_libro INT NOT NULL,
        fecha_activacion DATETIME DEFAULT GETDATE(),
        activado_por INT NULL,
        notas NVARCHAR(255) NULL,
        CONSTRAINT UQ_UsuarioLibro UNIQUE (id_usuario, id_libro),
        FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE,
        FOREIGN KEY (id_libro) REFERENCES Libros(id_libro) ON DELETE CASCADE
    );
END
GO

-- Índices para UsuariosLibros
CREATE NONCLUSTERED INDEX IX_UsuariosLibros_Usuario ON UsuariosLibros(id_usuario);
CREATE NONCLUSTERED INDEX IX_UsuariosLibros_Libro ON UsuariosLibros(id_libro);
GO

-- Tabla Anuncios
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Anuncios')
BEGIN
    CREATE TABLE Anuncios (
        id_anuncio INT IDENTITY(1,1) PRIMARY KEY,
        titulo NVARCHAR(200) NOT NULL,
        descripcion NVARCHAR(MAX) NULL,
        imagen_url NVARCHAR(500) NULL,
        enlace_url NVARCHAR(500) NULL,
        activo BIT DEFAULT 1,
        fecha_creacion DATETIME DEFAULT GETDATE(),
        fecha_expiracion DATETIME NULL
    );
END
GO

CREATE NONCLUSTERED INDEX IX_Anuncios_Activo ON Anuncios(activo);
CREATE NONCLUSTERED INDEX IX_Anuncios_Fecha ON Anuncios(fecha_creacion);
GO

-- Tabla Podcasts
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Podcasts')
BEGIN
    CREATE TABLE Podcasts (
        id_podcast INT IDENTITY(1,1) PRIMARY KEY,
        titulo NVARCHAR(200) NOT NULL,
        descripcion NVARCHAR(MAX) NULL,
        imagen_url NVARCHAR(500) NULL,
        audio_url NVARCHAR(500) NULL,
        spotify_url NVARCHAR(500) NULL,
        duracion NVARCHAR(20) NULL,
        fecha_publicacion DATETIME DEFAULT GETDATE(),
        es_activo BIT DEFAULT 1
    );
END
GO

CREATE NONCLUSTERED INDEX IX_Podcasts_Activo ON Podcasts(es_activo);
CREATE NONCLUSTERED INDEX IX_Podcasts_Fecha ON Podcasts(fecha_publicacion);
GO

-- ============================================
-- DATOS DE PRUEBA (opcional)
-- ============================================

-- Insertar anuncios de ejemplo
INSERT INTO Anuncios (titulo, descripcion, imagen_url, activo) VALUES
('Feria de Guamote 2026', 'Gran feria anual con música, comida y artesanías', NULL, 1),
('Curso de Kichwa', 'Aprende el idioma de nuestros ancestros', NULL, 1);
GO

-- Insertar podcasts de ejemplo
INSERT INTO Podcasts (titulo, descripcion, duracion, es_activo) VALUES
('Historia de Guamote', 'Conoce los orígenes de nuestro cantón', '45:30', 1),
('Entrevista al Alcalde', 'Proyectos 2026 para la comunidad', '32:15', 1);
GO

-- ============================================
-- CONSULTAS ÚTILES
-- ============================================

-- Ver usuarios con libros activados
-- SELECT u.nombre, u.apellido, l.titulo, ul.fecha_activacion
-- FROM UsuariosLibros ul
-- JOIN Usuarios u ON ul.id_usuario = u.id_usuario
-- JOIN Libros l ON ul.id_libro = l.id_libro;

-- Hacer un usuario administrador
-- UPDATE Usuarios SET es_admin = 1 WHERE id_usuario = 1;

PRINT '=============================================';
PRINT 'Tablas adicionales creadas exitosamente';
PRINT '=============================================';
GO
