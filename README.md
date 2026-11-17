# MPAZ Pañol – Backend (microservicios)

Backend de la app Android **MPAZ Pañol** (DSY1105, Evaluación Parcial 3). Dos microservicios Spring Boot con una base PostgreSQL.

| Servicio | Puerto local | Qué hace |
|---|---|---|
| `equipos-service` | 8081 | Catálogo de equipos y stock (`/api/equipos`) |
| `solicitudes-service` | 8082 | Solicitudes de los docentes, regla de 3 horas, aceptar/rechazar/entregar/devolver (`/api/solicitudes`). Llama a equipos para descontar/reponer stock. |

Cada servicio tiene su propia base (`equiposdb`, `solicitudesdb`) dentro del mismo Postgres.

## Cómo ejecutar
1. Copiar `.env.example` como `.env` y poner una clave en `DB_PASSWORD`.
2. `docker compose up -d --build`
3. Probar: `http://localhost:8081/api/equipos` (viene con 11 equipos de ejemplo).

Pruebas unitarias (JUnit 5 + Mockito), en cada carpeta de servicio:
```
./mvnw test        (Windows: mvnw.cmd test)
```

## Endpoints
**Equipos**
- `GET /api/equipos?categoria=` · `GET /api/equipos/{id}` · `POST /api/equipos` · `PUT /api/equipos/{id}`
- `PATCH /api/equipos/{id}/stock` body `{"cantidad": -2}` (negativo descuenta, positivo repone)

**Solicitudes**
- `GET /api/solicitudes?usuarioId=1&estado=Pendiente` (filtros opcionales)
- `GET /api/solicitudes/{id}`
- `POST /api/solicitudes` (201). Ejemplo (fechas en milisegundos, inicio ≥ ahora + 3 h):
```json
{ "usuarioId": 1, "usuarioNombre": "Patricia", "sala": "Sala 4",
  "fechaInicio": 1893456000000, "fechaDevolucion": 1893459600000,
  "asignatura": "Redes", "observaciones": "",
  "detalles": [ { "equipoId": 1, "equipoNombre": "Proyector", "cantidad": 2 } ] }
```
- `PATCH /api/solicitudes/{id}/estado` body `{"estado":"Aceptada"}` o `{"estado":"Rechazada","motivo":"..."}`
- `PATCH /api/solicitudes/{id}/notificada`

Flujo de estados: Pendiente → Aceptada / Rechazada / Cancelada; Aceptada → Entregada; Entregada → Devuelta.
Aceptar descuenta stock; devolver lo repone.

## Seguridad
`.env` y archivos `.pem` están en `.gitignore`: nunca se suben al repositorio.

## Estructura
```
equipos-service/      entities · repository · services · controller · config (datos iniciales) · test
solicitudes-service/  entities · repository · services · client (llamada a equipos) · controller · test
db/init.sql           crea la segunda base de datos
docker-compose.yml
```
