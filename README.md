# Estudiante

API REST para administrar las **clases** de un **usuario**. Se usa en el curso como
ejemplo de análisis con SonarQube.

- **Producción:** <http://desarrolloweb.click/api/estudiante>
- **Swagger UI:** <http://desarrolloweb.click/api/estudiante/swagger-ui.html>
- **OpenAPI (JSON):** <http://desarrolloweb.click/api/estudiante/v3/api-docs>
- **SonarQube:** <https://sonar.desarrolloweb.click/dashboard?id=estudiante>

Stack: Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL 16, springdoc-openapi. Se
empaqueta como **WAR** y corre en **Tomcat 11**.

## Endpoints

Todas las rutas cuelgan de `/api/estudiante`. Usuarios y clases tienen CRUD completo.

### Usuarios

| Operación | Método | Ruta | Respuesta |
|---|---|---|---|
| Crear | `POST` | `/api/estudiante/usuarios` | `201 Created` + header `Location` |
| Listar (por nombre) | `GET` | `/api/estudiante/usuarios` | `200 OK` |
| Consultar | `GET` | `/api/estudiante/usuarios/{id}` | `200 OK` |
| Reemplazar | `PUT` | `/api/estudiante/usuarios/{id}` | `200 OK` |
| Eliminar | `DELETE` | `/api/estudiante/usuarios/{id}` | `204 No Content`; `409` si tiene clases |

Cuerpo: `{"nombre": "Ana Pérez", "correo": "ana@javeriana.edu.co"}`. El correo es único
sin distinguir mayúsculas (se guarda en minúsculas).

### Clases

| Operación | Método | Ruta | Respuesta |
|---|---|---|---|
| Crear | `POST` | `/api/estudiante/clases` | `201 Created` + header `Location` |
| Listar | `GET` | `/api/estudiante/clases` | `200 OK` |
| Consultar | `GET` | `/api/estudiante/clases/{id}` | `200 OK` |
| Reemplazar | `PUT` | `/api/estudiante/clases/{id}` | `200 OK` |
| Eliminar | `DELETE` | `/api/estudiante/clases/{id}` | `204 No Content` |
| Clases de un usuario | `GET` | `/api/estudiante/usuarios/{usuarioId}/clases` | `200 OK` |

### Cuerpo de una clase

```json
{
  "codigo": "ISIS-1101",
  "nombre": "Algoritmos",
  "creditos": 3,
  "semestre": "2026-1",
  "usuarioId": 1
}
```

| Campo | Reglas |
|---|---|
| `codigo` | Obligatorio y único |
| `nombre` | Obligatorio |
| `creditos` | Obligatorio, mayor que 0 |
| `semestre` | Opcional |
| `usuarioId` | Obligatorio; el usuario debe existir |

`PUT` reemplaza la clase completa: hay que enviar todos los campos. No existe `PATCH`.

### Errores

| Código | Cuándo |
|---|---|
| `400 Bad Request` | Faltan campos obligatorios o `creditos <= 0` |
| `404 Not Found` | La clase o el usuario no existen |
| `409 Conflict` | Ya existe otra clase con el mismo `codigo` |

Todos los errores tienen el mismo formato. `campos` trae el error de cada campo en los
fallos de validación y viene en `null` en los demás errores:

```json
{
  "timestamp": "2026-10-05T12:00:00-05:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Los datos enviados no son válidos",
  "campos": { "creditos": "Los créditos deben ser mayores que cero" }
}
```

### Ejemplo

```bash
curl -X POST http://desarrolloweb.click/api/estudiante/clases \
  -H 'Content-Type: application/json' \
  -d '{"codigo":"ISIS-1101","nombre":"Algoritmos","creditos":3,"usuarioId":1}'
```

## Ejecución local

Requisitos: JDK 21. Maven no hace falta, porque el proyecto trae el wrapper `./mvnw`.

La conexión a la base se configura con variables de entorno:

| Variable | Por defecto |
|---|---|
| `DB_HOST` | servidor del curso |
| `DB_PORT` | `2345` |
| `DB_NAME` | `estudiante` |
| `DB_USER` | `estudiante` |
| `DB_PASSWORD` | *(sin valor, obligatoria)* |

```bash
DB_PASSWORD='...' ./mvnw spring-boot:run
```

Para usar un PostgreSQL local, exporta solo las variables que cambian:

```bash
DB_HOST=localhost DB_PORT=5432 DB_USER=$(whoami) DB_PASSWORD='...' ./mvnw spring-boot:run
```

En local la API queda en <http://localhost:8080/api/estudiante/clases> y Swagger en
<http://localhost:8080/api/estudiante/swagger-ui.html>.

El esquema lo crea Hibernate al arrancar (`ddl-auto=update`). No hay migraciones.

### Con Docker

```bash
DB_PASSWORD='...' docker compose up --build
```

## Tests

```bash
./mvnw test                          # toda la suite
./mvnw test -Dtest=ClaseControllerTest   # una clase de test
./mvnw clean verify                  # tests + informe de cobertura JaCoCo
```

Los tests **no necesitan PostgreSQL**. Los que levantan el contexto de Spring usan H2 en
memoria (`src/test/resources/application.properties`). Ese archivo reemplaza por completo
al principal, así que las rutas de `springdoc` están repetidas en los dos y tienen que
coincidir.

## Calidad: SonarQube

```bash
./mvnw clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN
```

`verify` es obligatorio: sin él no se genera el informe de JaCoCo y Sonar reporta 0% de
cobertura. La guía completa está en [SONARQUBE.md](SONARQUBE.md).

`service/ReporteClaseService.java` está **escrito mal a propósito** para la demostración:
tiene vulnerabilidades, bugs y code smells. Mientras exista, el Quality Gate estará en
`Failed`. No es código de referencia.

## Despliegue

Cada push a `main` ejecuta [.github/workflows/deploy.yaml](.github/workflows/deploy.yaml):

1. **Tests + SonarQube**: corre también en los pull requests. El Quality Gate no frena el
   pipeline.
2. **Imagen**: construye el [Dockerfile](Dockerfile) (Maven → Tomcat 11, desplegado como
   `ROOT.war`) y la publica en `ghcr.io/docsjaveriana/ex-estudiante` con los tags
   `:latest` y el SHA corto del commit.
3. **Kubernetes**: un runner self-hosted en el nodo aplica
   [k8s/dev/deployment.yaml](k8s/dev/deployment.yaml) en microk8s y reinicia el deployment.

Manifiesto de Kubernetes: namespace `estudiante`, ConfigMap con los datos de conexión,
Deployment, Service e Ingress. El Ingress publica `desarrolloweb.click/api/estudiante`
(también con `www.`) y reenvía la ruta sin reescribirla.

### Configuración inicial (una sola vez)

- **Secret de GitHub `SONAR_TOKEN`**: token del proyecto en SonarQube. Si no existe, el
  pipeline corre los tests sin análisis.
- **Runner self-hosted** registrado en el repositorio u organización, con acceso a
  `microk8s`.
- **Secret de la base en el cluster**: se crea en el nodo con el script
  `k8s/dev/create-secrets.sh`. El script **no se versiona** porque contiene la
  contraseña:

  ```bash
  #!/bin/bash
  set -e
  NAMESPACE="estudiante"
  microk8s kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | microk8s kubectl apply -f -
  microk8s kubectl create secret generic estudiante-secret \
    --from-literal=DB_PASSWORD="$DB_PASSWORD" \
    -n "$NAMESPACE" --dry-run=client -o yaml | microk8s kubectl apply -f -
  ```

- **Acceso a la imagen**: el Deployment usa `imagePullSecrets: ghcr-secret`. Hay que
  crearlo en el namespace `estudiante` o hacer público el paquete en GHCR.

Después de cambiar la contraseña de la base, se actualiza el secret y se reinicia el
deployment, porque el pod solo lee las variables de entorno al arrancar:

```bash
microk8s kubectl rollout restart deployment/estudiante -n estudiante
```

## Estructura

```
src/main/java/co/edu/javeriana/estudiante/
├── config/        OpenApiConfig (metadatos de Swagger)
├── controller/    ClaseController, UsuarioController, UsuarioClaseController
├── dto/           ClaseRequest, ClaseResponse, ErrorResponse
├── exception/     excepciones propias + @RestControllerAdvice
├── model/         Usuario, Clase (@ManyToOne LAZY)
├── repository/    Spring Data JPA
└── service/       ClaseService (reglas de negocio), ClaseMapper
```

Las capas son estrictas: `controller → service → repository`. Los controladores reciben
y devuelven DTO, nunca entidades JPA.
