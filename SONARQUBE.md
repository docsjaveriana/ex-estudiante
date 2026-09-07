# Cómo ejecutar SonarQube en este proyecto

El servidor de SonarQube **ya está montado y funcionando**. No hay que instalar nada:

**<https://sonar.desarrolloweb.click/>**

Esta guía explica cómo analizar el proyecto contra ese servidor y cómo leer los resultados.

---

## Resumen rápido

Desde la raíz del proyecto:

```bash
./mvnw clean verify sonar:sonar -Dsonar.token=XXXX
```

Resultados en <https://sonar.desarrolloweb.click/dashboard?id=estudiante>.

El servidor y la clave del proyecto ya están en el `pom.xml`, así que **lo único que hay que
pasar es el token**.

---

## Requisitos

| Requisito | Comprobación |
|-----------|--------------|
| Java 21 | `java -version` |
| Conexión a internet | `curl -s https://sonar.desarrolloweb.click/api/server/version` |
| Acceso a la base de datos | los tests levantan el contexto de Spring y necesitan PostgreSQL |

No hace falta Docker ni instalar SonarQube: el servidor es compartido.

Comprueba que responde antes de empezar:

```bash
curl -s https://sonar.desarrolloweb.click/api/system/status
```

Debe devolver:

```json
{"id":"...","version":"26.9.0.129388","status":"UP"}
```

---

## Paso 1 — Entrar al servidor

Abre <https://sonar.desarrolloweb.click/> en el navegador e inicia sesión con las
credenciales que te haya dado el administrador del curso.

El proyecto ya está creado con la clave **`estudiante`**:

<https://sonar.desarrolloweb.click/dashboard?id=estudiante>

---

## Paso 2 — El token de análisis

El escáner no usa usuario y contraseña, usa un token. El proyecto ya tiene uno:

```
sqp_37d8624420ac7e979477e8968c1b7a4f1e7a7ca1
```

Guárdalo en una variable para no repetirlo en cada comando:

```bash
export SONAR_TOKEN=sqp_37d8624420ac7e979477e8968c1b7a4f1e7a7ca1
```

Compruébalo:

```bash
curl -s -u $SONAR_TOKEN: https://sonar.desarrolloweb.click/api/authentication/validate
# {"valid":true}
```

### Si necesitas generar otro

En la interfaz: avatar arriba a la derecha → *My Account* → pestaña *Security* → escribe un
nombre y pulsa *Generate*. El token **solo se muestra una vez**.

> El prefijo indica el tipo: `sqp_` es un token de proyecto (sirve para analizar ese
> proyecto concreto) y `squ_` es un token de usuario (hereda tus permisos).

---

## Paso 3 — Ejecutar el análisis

```bash
./mvnw clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN
```

Qué hace cada parte:

| Fase | Qué hace |
|------|----------|
| `clean` | borra `target/` para partir de cero |
| `verify` | compila, ejecuta los 27 tests y genera el informe de cobertura de JaCoCo |
| `sonar:sonar` | envía el código y la cobertura al servidor |

> **Importante:** `verify` y `sonar:sonar` van **siempre juntos**. Si ejecutas solo
> `./mvnw sonar:sonar`, el informe de JaCoCo no existe y SonarQube reportará **0% de
> cobertura**.

El análisis tarda alrededor de un minuto. Termina con:

```
[INFO] ANALYSIS SUCCESSFUL, you can find the results at: https://sonar.desarrolloweb.click/dashboard?id=estudiante
[INFO] BUILD SUCCESS
```

---

## Paso 4 — Ver los resultados

<https://sonar.desarrolloweb.click/dashboard?id=estudiante>

El servidor tarda unos segundos en procesar el informe después de recibirlo; si el panel
sale vacío, recarga.

### Qué mirar

| Indicador | Qué significa |
|-----------|---------------|
| **Quality Gate** | `Passed` / `Failed`. Es el veredicto global |
| **Bugs** | errores que romperán algo en ejecución |
| **Vulnerabilities** | fallos de seguridad |
| **Security Hotspots** | código sensible que hay que revisar a mano |
| **Code Smells** | deuda técnica: no rompe nada, ensucia el código |
| **Coverage** | % de líneas cubiertas por los tests |
| **Duplications** | % de código duplicado |

Para ver el detalle de cada problema: pestaña **Issues**. Al pinchar en uno, SonarQube
muestra la línea exacta, la regla que se incumple y una explicación con ejemplos.

### Consultar por línea de comandos

```bash
H=https://sonar.desarrolloweb.click

# Métricas
curl -s -u $SONAR_TOKEN: \
  "$H/api/measures/component?component=estudiante&metricKeys=bugs,vulnerabilities,code_smells,coverage"

# Quality Gate
curl -s -u $SONAR_TOKEN: \
  "$H/api/qualitygates/project_status?projectKey=estudiante"
```

> Un token de proyecto (`sqp_`) tiene permisos limitados: algunas rutas de la API responden
> `Insufficient privileges`. Las dos de arriba sí funcionan.

---

## Paso 5 — Corregir y volver a analizar

1. Arregla lo que reporte SonarQube.
2. Vuelve a lanzar el mismo comando del paso 3.
3. Los problemas resueltos desaparecen solos del panel.

---

## Probar la detección: archivo con defectos a propósito

Para comprobar que el análisis funciona de verdad, el proyecto incluye
[`ReporteClaseService.java`](src/main/java/co/edu/javeriana/estudiante/service/ReporteClaseService.java),
escrito **deliberadamente mal**. Compila y no rompe la aplicación.

SonarQube detecta **9 problemas**: 3 de seguridad, 1 de fiabilidad y 5 de mantenibilidad.
Cubren las tres dimensiones que mide la herramienta:

| Dimensión | Métrica | Nota del proyecto |
|-----------|---------|-------------------|
| Seguridad | 3 vulnerabilidades | Security Rating **E** |
| Fiabilidad | 1 bug | Reliability Rating **B** |
| Mantenibilidad | 5 code smells | Maintainability Rating **A** |

### Vulnerabilidades (3)

| Regla | Línea | Qué señala |
|-------|-------|------------|
| `java:S2077` | 46 | **Inyección SQL.** La consulta se arma concatenando el parámetro: un valor como `2026-1' OR '1'='1` cambia la sentencia |
| `java:S4790` | 52 | **Hash débil.** MD5 está roto para cualquier uso criptográfico; usar SHA-256 o superior |
| `java:S2245` | 29 | **PRNG no criptográfico.** `java.util.Random` es predecible; para valores con relevancia de seguridad va `SecureRandom` |

### Bug de fiabilidad (1)

| Regla | Línea | Qué señala |
|-------|-------|------------|
| `java:S2184` | 110 | **División entera asignada a un `double`.** `totalCreditos / clases.size()` opera entre enteros, así que el decimal se pierde *antes* de convertirse a `double`: con clases de 3, 4 y 4 créditos devuelve `3.0` en vez de `3.67`. Se arregla convirtiendo un operando: `(double) totalCreditos / clases.size()` |

Este es el tipo de defecto que un test tampoco detecta si solo se comprueba que el método
"devuelve algo": hay que comprobar el valor.

### Code smells (5)

| Regla | Línea | Qué señala |
|-------|-------|------------|
| `java:S106`  | 76 | `System.out.println` en vez de un logger |
| `java:S1481` | 89 | Variable local declarada y nunca usada (`separador`) |
| `java:S1854` | 89 | Asignación cuyo valor no se lee nunca |
| `java:S1155` | 90 | `size() == 0` en vez de `isEmpty()` |
| `java:S125`  | 100 | Bloque de código comentado |

Con este archivo el **Quality Gate está en `Failed`**, por dos condiciones sobre código nuevo:

| Condición | Valor | Umbral |
|-----------|-------|--------|
| `new_violations` | 9 | 0 |
| `new_coverage` | ~10% | 80% |

La cobertura global baja de 92,8% a **59,1%** porque la clase no tiene tests.

### Ejercicio

Ve corrigiendo los problemas de uno en uno y relanza el análisis; verás cómo bajan las
cifras y suben las notas hasta que el Quality Gate vuelve a `Passed`. Orden sugerido:

1. **La inyección SQL** (`S2077`) — es la única que un atacante puede explotar de verdad.
2. **El bug de la división** (`S2184`) — devuelve un resultado incorrecto en silencio.
3. El resto, que son deuda técnica sin consecuencias inmediatas.

> El archivo también tiene una contraseña escrita en el código
> (`REPORTE_PASSWORD`). El perfil de calidad por defecto **no** la marca, así que sirve
> para enseñar que el análisis estático no lo detecta todo: hay defectos reales que solo
> encuentra una revisión humana.

### Quitar el archivo

Cuando termine la demostración, borra
`src/main/java/co/edu/javeriana/estudiante/service/ReporteClaseService.java` y vuelve a
lanzar el análisis. El proyecto regresa a 0 problemas, 0 vulnerabilidades, 92,8% de
cobertura y Quality Gate `Passed`.

---

## Configuración: dónde está definida

Todo vive en el [`pom.xml`](pom.xml), no hace falta `sonar-project.properties`.

```xml
<properties>
    <sonar.projectKey>estudiante</sonar.projectKey>
    <sonar.projectName>estudiante</sonar.projectName>
    <sonar.host.url>https://sonar.desarrolloweb.click</sonar.host.url>
    <sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
</properties>
```

Más dos plugins:

- **`jacoco-maven-plugin`** — mide la cobertura. `prepare-agent` instrumenta los tests y
  `report` (en fase `verify`) genera `target/site/jacoco/jacoco.xml`.
- **`sonar-maven-plugin`** — aporta el objetivo `sonar:sonar`.

El token **no** está en el `pom.xml` a propósito: es una credencial y no debe versionarse.

Cualquiera de estas propiedades se puede sobrescribir por línea de comandos:

```bash
./mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey=estudiante \
  -Dsonar.projectName='estudiante' \
  -Dsonar.host.url=https://sonar.desarrolloweb.click \
  -Dsonar.token=XXXXX
```

---

## Problemas frecuentes

| Síntoma | Causa y solución |
|---------|------------------|
| Cobertura al **0%** | Lanzaste `sonar:sonar` sin `verify`. Ejecuta los dos juntos |
| `Not authorized` / `401` | Token caducado, mal copiado o de otro servidor. Genera uno nuevo |
| `Insufficient privileges` al consultar la API | Normal con un token `sqp_`. Usa la interfaz web o un token de usuario |
| `You're not authorized to analyze this project` | El token no tiene permiso sobre la clave `estudiante`. Pídeselo al administrador |
| `Project not found` | La clave del `pom.xml` no coincide con la del servidor |
| El panel sale vacío tras un análisis correcto | El servidor aún procesa el informe. Recarga a los pocos segundos |
| Los tests fallan con `Unable to determine Dialect` | No hay conexión con PostgreSQL. Revisa `application.properties` |
| `UnknownHostException: sonar.desarrolloweb.click` | Sin conexión o DNS caído. Comprueba el endpoint de estado con `curl` |

---

## Anexo — Levantar un SonarQube propio en local

Solo si necesitas trabajar sin conexión al servidor compartido. Requiere Docker.

La primera vez, crear el contenedor:

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
```

Las siguientes veces ya existe, basta con arrancarlo:

```bash
docker start sonarqube
```

Tarda entre 40 y 90 segundos en estar listo:

```bash
until curl -s http://localhost:9000/api/system/status | grep -q '"status":"UP"'; do
  echo "esperando a SonarQube..."; sleep 5
done; echo "SonarQube listo"
```

Entra en <http://localhost:9000> con `admin`/`admin` (te pedirá cambiar la contraseña),
genera un token y analiza apuntando al servidor local:

```bash
./mvnw clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TU_TOKEN_LOCAL
```

Para ver el arranque en directo: `docker logs -f sonarqube`. Para pararlo conservando los
datos: `docker stop sonarqube`.

> El contenedor no tiene volumen persistente: si lo eliminas, se pierde el historial de
> análisis. Para conservarlo hay que montar volúmenes en `/opt/sonarqube/data` y
> `/opt/sonarqube/extensions`.
