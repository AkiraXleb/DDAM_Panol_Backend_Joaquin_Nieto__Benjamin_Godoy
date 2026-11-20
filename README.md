# MPAZ Pañol – Backend (microservicios)

Backend de la app Android **MPAZ Pañol** (DSY1105, Evaluación Parcial 3). Tres microservicios Spring Boot con una base PostgreSQL.

| Servicio | Puerto local | Qué hace |
|---|---|---|
| `equipos-service` | 8081 | Catálogo de equipos y stock (`/api/equipos`) |
| `solicitudes-service` | 8082 | Solicitudes de los docentes, regla de 3 horas, aceptar/rechazar/entregar/devolver (`/api/solicitudes`). Llama a equipos para descontar/reponer stock. |
| `usuarios-service` | 8083 | Login y administración de usuarios (`/api/usuarios`). El correo debe ser `prefijo.nombre@escuelamarcelapaz.cl` y el prefijo (`prof`, `insp`, `ti`, `admin`) define el cargo. |
| `img-service` | 8084 | Imágenes de los equipos (`/api/imagenes/equipos/{id}`), guardadas en Postgres (máx. 3 MB, solo `image/*`). |

Cada servicio tiene su propia base (`equiposdb`, `solicitudesdb`, `usuariosdb`, `imgdb`) dentro del mismo Postgres.

## Cómo ejecutar
1. Copiar `.env.example` como `.env` y poner una clave en `DB_PASSWORD`.
2. `docker compose up -d --build`
3. Probar: `http://localhost:8081/api/equipos` (viene con 11 equipos de ejemplo).

Pruebas unitarias (JUnit 5 + Mockito), en cada carpeta de servicio:
```
./mvnw test        (Windows: mvnw.cmd test)
```

## Endpoints
**Usuarios**
- `POST /api/usuarios/login` body `{"email": "...", "password": "..."}` (401 si son incorrectos)
- `PATCH /api/equipos/{id}/mantenimiento` body `{"cantidad": N}`: positivo envía N unidades a mantenimiento, negativo las devuelve al stock (409 si no alcanzan)
- `PUT /api/imagenes/equipos/{id}` (multipart, campo `archivo`) · `GET` (404 si no tiene) · `DELETE`
- `GET /api/usuarios` · `POST /api/usuarios` · `DELETE /api/usuarios/{id}` (no se puede eliminar a un Super Admin)

**Equipos**
- `GET /api/equipos?categoria=` · `GET /api/equipos/{id}` · `POST /api/equipos` · `PUT /api/equipos/{id}`
- `DELETE /api/equipos/{id}` (409 si hay unidades prestadas o reservadas)
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
img-service/          entities · repository · services · controller · test
usuarios-service/     entities · repository · services · controller · config (usuarios de prueba) · test
db/init.sql           crea las bases solicitudesdb, usuariosdb e imgdb
docker-compose.yml
```

## Despliegue en AWS

Corren en una instancia **AWS EC2** (AWS Academy Learner Lab, us-east-1) con Docker Compose y reinicio automático.

| Servicio | URL |
|---|---|
| equipos-service | http://32.197.25.36:8081/api/equipos |
| solicitudes-service | http://32.197.25.36:8082/api/solicitudes |
| usuarios-service | http://32.197.25.36:8083/api/usuarios |
| img-service | http://32.197.25.36:8084/api/imagenes/equipos/{id} |

> Solo disponible mientras el Learner Lab esté iniciado.

## Aplicación Android

Cliente móvil: https://github.com/AkiraXleb/DDAM_Panol_Joaquin_Nieto__Benjamin_Godoy
