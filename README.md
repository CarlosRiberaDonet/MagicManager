# Magic Investor 🃏

Aplicación web full stack para la búsqueda, análisis y seguimiento de inversiones en cartas de Magic: The Gathering. Permite consultar precios actualizados desde Cardmarket, gestionar una colección personal y hacer seguimiento de cartas de interés.

---

## Tabla de contenidos

- [Descripción](#descripción)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Base de datos](#base-de-datos)
- [API REST](#api-rest)
- [Seguridad](#seguridad)
- [Frontend](#frontend)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Funcionalidades](#funcionalidades)

---

## Descripción

Magic Investor es una herramienta orientada a inversores y coleccionistas de Magic: The Gathering. Integra datos de **Scryfall** (catálogo de cartas) y **Cardmarket** (precios de mercado) para ofrecer una experiencia completa de búsqueda, filtrado y seguimiento de valor de cartas.

El sistema permite:
- Buscar cartas con múltiples filtros simultáneos
- Consultar precios históricos y tendencias de mercado
- Gestionar una colección personal con seguimiento de inversión
- Mantener una watchlist de cartas de interés
- Actualización automática diaria de precios

---

## Tecnologías

### Backend
| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.x | Framework REST |
| Spring Security | 3.x | Autenticación y autorización |
| JJWT | 0.12.6 | Generación y validación de tokens JWT |
| MySQL | 8.x | Base de datos relacional |
| JDBC | — | Acceso a datos (patrón DAO manual) |
| Jackson | 2.x | Parseo de JSON en streaming |
| Lombok | — | Reducción de boilerplate |
| Maven | — | Gestión de dependencias |

### Frontend
| Tecnología | Uso |
|---|---|
| HTML5 / CSS3 | Estructura y estilos |
| JavaScript ES6+ | Lógica e interactividad |
| Módulos ES6 | Organización del código |
| noUiSlider | Slider de rango de precio |
| Google Fonts (Cinzel + Crimson Pro) | Tipografía premium |

### APIs externas
| API | Uso |
|---|---|
| Scryfall Bulk Data | Catálogo completo de cartas |
| Scryfall Sets API | Información de ediciones |
| Cardmarket Price Guide | Precios diarios de mercado |

---

## Arquitectura

El proyecto sigue una arquitectura en capas clásica de Spring Boot:

```
┌─────────────────────────────────────────┐
│              Frontend (JS)              │
│  index.html · cardDetail.html           │
│  collection.html · navbar.html          │
└──────────────────┬──────────────────────┘
                   │ HTTP/REST
┌──────────────────▼──────────────────────┐
│           Controller Layer              │
│  CardController · UserController        │
│  AuthController · ExpansionController   │
│  AdminController                        │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│            Service Layer                │
│  CardService · UserService              │
│  ScryfallService · CardmarketImport     │
│  ExpansionService                       │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│              DAO Layer                  │
│  ScryfallCardDAO · UserDAO              │
│  ExpansionDAO · CardPriceDAO            │
└──────────────────┬──────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────┐
│               MySQL 8                   │
│  scryfall_card · card_price             │
│  scryfall_set · user                    │
│  user_collection · user_watchlist       │
└─────────────────────────────────────────┘
```

### Decisiones de diseño

- **DAO manual con JDBC** en lugar de JPA para queries complejas y dinámicas con múltiples filtros opcionales. Permite construcción de queries con `StringBuilder` e índices optimizados.
- **JWT stateless** para autenticación sin sesiones en servidor, ideal para APIs REST.
- **Parseo en streaming con Jackson** para procesar los JSONs de Scryfall (~500MB) sin cargarlos en memoria.
- **Queries dinámicas con `WHERE 1=1`** para combinar filtros opcionales de forma limpia y segura con `PreparedStatement`.

---

## Base de datos

### Diagrama de tablas principales

```
scryfall_card          card_price             scryfall_set
─────────────          ──────────             ────────────
id (PK)                id (PK)                id (PK)
scryfall_id            cardmarket_id (FK)     code
cardmarket_id          avg                    name
name                   low                    released_at
printed_name           trend                  icon_svg_uri
lang                   avg1/7/30
image_url              avg_foil/low_foil
rarity                 trend_foil
set_name               avg1/7/30_foil
set_code               updated_at
collector_number
cardmarket_url
price
price_foil
type_line
released_at

user                   user_collection        user_watchlist
────                   ───────────────        ──────────────
id (PK)                id (PK)                id (PK)
email (UNIQUE)         user_id (FK)           user_id (FK)
password (BCrypt)      card_id (FK)           card_id (FK)
role                   purchase_price         added_at
                       quantity
                       added_at
```

### Índices relevantes en `scryfall_card`

```sql
INDEX idx_name (name)
INDEX idx_printed_name (printed_name)
INDEX idx_rarity (rarity)
INDEX idx_lang (lang)
INDEX idx_set_name (set_name)
INDEX idx_set_code (set_code)
INDEX idx_type_line (type_line)
INDEX idx_price (price)
INDEX idx_cardmarket (cardmarket_id)
```

---

## API REST

### Autenticación — `/auth`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | `/auth/register` | Registrar nuevo usuario | No |
| POST | `/auth/login` | Login — devuelve JWT | No |

### Cartas — `/cards`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| GET | `/cards/search` | Búsqueda con filtros | No |
| GET | `/cards/id` | Detalle de carta por ID | No |

**Parámetros de búsqueda:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `name` | String | Nombre (parcial, opcional) |
| `setCode` | String | Código de edición |
| `rarity` | String | common / uncommon / rare / mythic |
| `lang` | String | Código de idioma (en, es, fr...) |
| `typeLine` | String | Tipo de carta |
| `minPrice` | Double | Precio mínimo |
| `maxPrice` | Double | Precio máximo |
| `orderBy` | String | price\_asc / price\_desc / name\_asc / name\_desc |
| `hideNA` | Boolean | Ocultar cartas sin precio |
| `page` | int | Número de página |
| `size` | int | Resultados por página |

### Usuario — `/user` *(requiere JWT)*

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/user/collection` | IDs de cartas en colección |
| GET | `/user/collection/contains?cardId=` | Cantidad de una carta en colección |
| POST | `/user/collection/add` | Añadir carta a colección |
| DELETE | `/user/collection/del` | Eliminar carta de colección |
| GET | `/user/mycollection` | Colección completa con detalles |
| GET | `/user/watchlist/contains?cardId=` | Comprobar si carta está en watchlist |
| POST | `/user/watchlist/add` | Añadir carta a watchlist |
| DELETE | `/user/watchlist/del` | Eliminar carta de watchlist |

### Ediciones — `/sets`

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| GET | `/sets/scryfall` | Lista de ediciones disponibles | No |

### Administración — `/admin` *(requiere rol ADMIN)*

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/admin/update` | Forzar actualización completa de BD |

---

## Seguridad

### Autenticación JWT

El sistema utiliza **JSON Web Tokens** para autenticación stateless:

1. El cliente envía credenciales a `POST /auth/login`
2. El servidor valida con BCrypt y genera un JWT firmado con HMAC-SHA384
3. El JWT contiene: `userId`, `email`, `role`, `iat`, `exp` (24h)
4. El cliente incluye el token en cada petición: `Authorization: Bearer <token>`
5. `JwtAuthFilter` intercepta, valida y carga el contexto de seguridad

### Roles y permisos

| Rol | Acceso |
|---|---|
| Sin autenticar | Búsqueda de cartas, login, registro |
| USER | Gestión de colección y watchlist |
| ADMIN | Todo lo anterior + actualización de BD |

### Seguridad de contraseñas

Las contraseñas se almacenan hasheadas con **BCrypt** (factor de coste 10). Nunca se almacenan ni transmiten en texto plano.

---

## Frontend

### Estructura de archivos

```
/
├── index.html              # Página principal — búsqueda
├── cardDetail.html         # Detalle de carta
├── collection.html         # Colección del usuario
├── navbar.html             # Navbar compartido (carga dinámica)
├── css/
│   ├── style.css           # Estilos globales
│   ├── navbar.css          # Estilos del navbar y modal
│   ├── cardDetail.css      # Estilos del detalle de carta
│   └── collection.css      # Estilos de la colección
└── js/
    ├── app.js              # Lógica principal — búsqueda y paginación
    ├── api.js              # Llamadas al backend (cartas y ediciones)
    ├── apiUser.js          # Llamadas al backend (usuario)
    ├── auth.js             # Autenticación, modal, menú usuario
    ├── navbar.js           # Carga dinámica del navbar
    ├── cardsRenderer.js    # Renderizado de cartas en grid
    ├── pagination.js       # Lógica de paginación
    ├── userActions.js      # Acciones de colección y watchlist
    ├── cardDetail.js       # Lógica de la página de detalle
    └── collection.js       # Lógica de la página de colección
```

### Características del frontend

- **Módulos ES6** — separación clara de responsabilidades
- **Navbar dinámico** — cargado con `fetch()` y compartido en todas las páginas
- **Autenticación JWT** en `localStorage` con menú de usuario desplegable
- **Filtros en tiempo real** — set, rareza, idioma, tipo, rango de precio con noUiSlider
- **Vista lista / cuadrícula** en la página de colección
- **Responsive** — adaptado a tablet y móvil con media queries
- **Diseño premium** — paleta oscura con acentos dorados, tipografía Cinzel

---

## Instalación y ejecución

### Requisitos

- Java 21+
- Maven 3.8+
- MySQL 8+
- Node.js (opcional, para servir el frontend en local)

### Backend

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/magic-investor.git

# 2. Configurar la BD en application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/magic_investor
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

# 3. Crear las tablas en MySQL (ver /docs/schema.sql)

# 4. Arrancar
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8081`.

### Frontend

Sirve los archivos estáticos con cualquier servidor local. Con VS Code + Live Server apunta a `index.html`.

### Primera carga de datos

Con el servidor arrancado, ejecuta en orden desde Postman o curl:

```bash
# 1. Descargar e importar ediciones de Scryfall
POST http://localhost:8081/scryfall/editions

# 2. Descargar e importar cartas de Scryfall (~500MB, proceso largo)
POST http://localhost:8081/scryfall/cards

# 3. Descargar precios de Cardmarket
POST http://localhost:8081/prices/import

# 4. Importar precios a la BD
POST http://localhost:8081/prices/update

# 5. Actualizar precios en scryfall_card
POST http://localhost:8081/scryfall/update-prices
```

> ⚠️ Los endpoints de administración requieren token JWT con rol ADMIN en el header `Authorization`.

### Actualización automática

El sistema incluye un `SchedulerTask` que ejecuta el proceso completo de actualización automáticamente cada día a las **6:00 AM**:

```java
@Scheduled(cron = "0 0 6 * * *")
public void updateBBDD()
```

---

## Funcionalidades

### Implementadas ✅

- Búsqueda de cartas con 7 filtros combinables
- Paginación de resultados
- Detalle completo de carta con precios históricos
- Enlace directo a Cardmarket
- Registro e inicio de sesión con JWT
- Colección personal — añadir, eliminar, ver cantidad
- Watchlist — seguimiento de cartas de interés
- Estadísticas de colección (valor actual, invertido, ganancia)
- Vista lista y cuadrícula de la colección
- Actualización automática diaria de precios
- Diseño responsive para móvil y tablet

### En desarrollo 🚧

- Scraper de precios para cartas sin datos de Cardmarket
- Gráfica histórica de precios
- Panel de administración web
- Notificaciones de variación de precio
- Funcionalidades premium con autenticación de pago

---

*Proyecto desarrollado como aplicación full stack con Java Spring Boot y JavaScript vanilla. Diseñado para su comercialización como herramienta SaaS para inversores de Magic: The Gathering.*

