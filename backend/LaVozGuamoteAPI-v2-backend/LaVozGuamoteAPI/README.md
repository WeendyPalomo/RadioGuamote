# 🔧 La Voz de Guamote - Backend API

API REST desarrollada con **ASP.NET Core 8** y **SQL Server**.

---

## 📋 REQUISITOS

- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0)
- [SQL Server](https://www.microsoft.com/sql-server) (Express, Developer o superior)
- Visual Studio 2022 o VS Code

---

## 🚀 INSTALACIÓN RÁPIDA

### 1. Configurar la Base de Datos

Edita `appsettings.json` con tu conexión a SQL Server:

```json
"ConnectionStrings": {
    "DefaultConnection": "Server=TU_SERVIDOR;Database=LaVozGuamoteDB;Trusted_Connection=True;TrustServerCertificate=True;"
}
```

**Ejemplos de conexión:**

```
// SQL Server Local
Server=localhost;Database=LaVozGuamoteDB;Trusted_Connection=True;TrustServerCertificate=True;

// SQL Server con usuario/contraseña
Server=localhost;Database=LaVozGuamoteDB;User Id=sa;Password=TuPassword;TrustServerCertificate=True;

// SQL Server remoto
Server=tu-servidor.database.windows.net;Database=LaVozGuamoteDB;User Id=admin;Password=TuPassword;
```

### 2. Ejecutar la API

```bash
cd LaVozGuamoteAPI
dotnet restore
dotnet run
```

La API estará disponible en:
- **HTTP:** http://localhost:5000
- **HTTPS:** https://localhost:5001
- **Swagger:** https://localhost:5001/swagger

---

## 📡 ENDPOINTS DISPONIBLES

### Autenticación
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/register` | Registrar usuario |
| GET | `/api/auth/profile` | Obtener perfil (auth) |

### Eventos
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/eventos` | Listar eventos |
| GET | `/api/eventos/{id}` | Obtener evento |
| POST | `/api/eventos` | Crear evento (admin) |
| PUT | `/api/eventos/{id}` | Actualizar evento (admin) |
| DELETE | `/api/eventos/{id}` | Eliminar evento (admin) |
| POST | `/api/eventos/{id}/like` | Dar like |

### Noticias
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/noticias` | Listar noticias |
| GET | `/api/noticias/latest?count=5` | Últimas noticias |
| GET | `/api/noticias/{id}` | Obtener noticia |
| POST | `/api/noticias` | Crear noticia (admin) |
| PUT | `/api/noticias/{id}` | Actualizar noticia (admin) |
| DELETE | `/api/noticias/{id}` | Eliminar noticia (admin) |

### Libros
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/libros` | Listar libros |
| GET | `/api/libros/{id}` | Obtener libro |
| POST | `/api/libros` | Crear libro (admin) |
| POST | `/api/libros/{id}/purchase` | Comprar libro (auth) |
| GET | `/api/libros/mis-libros` | Mis libros (auth) |

### Donaciones
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/donaciones` | Registrar donación |
| GET | `/api/donaciones` | Listar donaciones (admin) |
| PUT | `/api/donaciones/{id}/verificar` | Verificar donación (admin) |

### Otros
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/categorias` | Listar categorías |
| GET | `/api/radio/config` | Configuración de radio |

### Upload de Imágenes (NUEVO)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/upload/image?type=anuncios` | Subir imagen (auth) |
| DELETE | `/api/upload/image/{fileName}?type=anuncios` | Eliminar imagen (auth) |

**Tipos válidos:** `anuncios`, `podcasts`, `libros`, `perfiles`, `general`

### Anuncios (NUEVO)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/anuncios` | Listar anuncios |
| GET | `/api/anuncios/{id}` | Obtener anuncio |
| POST | `/api/anuncios` | Crear anuncio (admin) |
| PUT | `/api/anuncios/{id}` | Actualizar anuncio (admin) |
| DELETE | `/api/anuncios/{id}` | Eliminar anuncio (admin) |

### Podcasts (NUEVO)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/podcasts` | Listar podcasts |
| GET | `/api/podcasts/{id}` | Obtener podcast |
| POST | `/api/podcasts` | Crear podcast (admin) |
| PUT | `/api/podcasts/{id}` | Actualizar podcast (admin) |
| DELETE | `/api/podcasts/{id}` | Eliminar podcast (admin) |

### Admin - Activación de Libros (NUEVO)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/admin/users/search?q=nombre` | Buscar usuarios (admin) |
| POST | `/api/admin/users/{userId}/books/{bookId}/activate` | Activar libro (admin) |
| DELETE | `/api/admin/users/{userId}/books/{bookId}/activate` | Desactivar libro (admin) |
| GET | `/api/admin/books/{bookId}/users` | Usuarios con libro (admin) |
| GET | `/api/admin/stats` | Estadísticas (admin) |

---

## 🔐 AUTENTICACIÓN

La API usa **JWT (JSON Web Tokens)**. Para endpoints protegidos:

```
Authorization: Bearer TU_TOKEN_JWT
```

### Usuario Admin por defecto:
- **Email:** admin@lavozguamote.ec
- **Password:** Admin123!

---

## 🌐 DEPLOY EN PRODUCCIÓN

### Opción 1: Azure App Service
```bash
dotnet publish -c Release
# Subir a Azure
```

### Opción 2: Docker
```dockerfile
FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY publish/ .
ENTRYPOINT ["dotnet", "LaVozGuamoteAPI.dll"]
```

### Opción 3: IIS (Windows Server)
1. Publicar: `dotnet publish -c Release -o ./publish`
2. Copiar carpeta `publish` al servidor
3. Configurar IIS con el módulo ASP.NET Core

---

## 📱 CONFIGURAR EN LA APP ANDROID

En `app/build.gradle`, cambia la URL:

```gradle
buildConfigField "String", "BASE_URL", '"https://TU-SERVIDOR.com/api/"'
```

---

## 📞 INFORMACIÓN DE DONACIONES

```
Banco: Banco Guayaquil
Nombre: Fundación Acción Integral Guamote
RUC: 0691709523001
Cuenta Corriente: 0006913857
Teléfono: 0995224384
Email: fundacionaig@faig.ec
```

---

## 📄 LICENCIA

© 2024 La Voz de Guamote - PUCE
