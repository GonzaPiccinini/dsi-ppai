# Bolsín

Sistema de seguimiento de bolsines desarrollado con Spring Boot siguiendo el patrón de arquitectura Frontera-Control-Entidad (BCE) del análisis RUP/PUD.

## Stack tecnológico

- **Java 26**
- **Spring Boot 4.1.1**
- **Spring MVC** (`spring-boot-starter-webmvc`)
- **Spring Data JPA** + **Hibernate**
- **Thymeleaf** (motor de plantillas)
- **MySQL 8** (vía Docker) — entorno de desarrollo y producción
- **H2** (base en memoria) — solo para los tests unitarios
- **JUnit 5** (`spring-boot-starter-*-test`)
- **Maven** (build tool)

## Requisitos previos

Antes de levantar el proyecto, asegurate de tener instalado:

- [JDK 26](https://jdk.java.net/26/) (o la versión configurada en `pom.xml`)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (o Docker Engine en Linux) — necesario para levantar la base de datos MySQL
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community o Ultimate) — o cualquier IDE con soporte para Maven
- Git

No es necesario instalar Maven por separado: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`).

## Clonar el proyecto

```bash
git clone <url-del-repositorio>
cd bolsin
```

## Configurar variables de entorno

Antes de levantar nada, creá un archivo `.env` en la **raíz del proyecto** (mismo nivel que `docker-compose.dev.yaml` y `pom.xml`) con este contenido:

```
DB_NAME=
DB_USER=
DB_PASSWORD=
```

> Este archivo está en `.gitignore` y **no se sube al repo** — cada dev lo crea localmente la primera vez que clona el proyecto.

### ⚠️ Importante: el `.env` solo lo lee Docker Compose automáticamente

Docker Compose carga este archivo solo, sin configuración adicional. **Tu aplicación Spring Boot no lee el `.env` por sí sola** — son dos procesos separados. Esto significa que además tenés que configurar las mismas variables en tu IDE para que la app Java se conecte con los mismos valores:

**En IntelliJ:**
1. `Run > Edit Configurations`
2. Seleccioná tu configuración de tipo **Spring Boot** (no "Application")
3. Clic en **"Modify options"** → activá **"Environment variables"**
4. Completá con los mismos valores del `.env`
5. Apply → OK

## Levantar la base de datos (MySQL 8 con Docker)

El proyecto usa Docker Compose para levantar una instancia de MySQL 8 en un contenedor, usando las variables definidas en tu `.env`.

**1.** Verificá que Docker Desktop esté corriendo.

**2.** Desde la raíz del proyecto (con el `.env` ya creado), ejecutá:

```bash
docker compose -f docker-compose.dev.yaml up -d
```

**3.** Esperá unos 10-15 segundos la primera vez (MySQL tarda en inicializar completamente).

**4.** Verificá que el contenedor esté corriendo:

```bash
docker ps
```

Deberías ver un contenedor con el nombre `mysql8-bolsin` (o el que corresponda) en estado `Up`.

### Detener la base de datos

Cuando termines de trabajar:

```bash
docker compose -f docker-compose.dev.yaml down
```

Esto detiene el contenedor pero **conserva los datos** (gracias al volumen configurado). Si querés borrar también los datos:

```bash
docker compose -f docker-compose.dev.yaml down -v
```

## Configuración de `application.properties`

El archivo `src/main/resources/application.properties` ya está preparado para leer las variables de entorno (con defaults de respaldo por si no están seteadas):

```properties
spring.application.name=bolsin

spring.datasource.url=jdbc:mysql://localhost:${DB_PORT:33060}/bolsin
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> **Nota**: no hace falta especificar `hibernate.dialect` manualmente — Hibernate 6+ lo detecta automáticamente a partir de la conexión JDBC.

## Ejecutar los tests unitarios

Los tests **no necesitan Docker ni MySQL**: usan una base de datos **H2 en memoria** que se
configura automáticamente al correr la suite.

```bash
./mvnw test
```

La configuración específica de tests está en `src/test/resources/application.properties`
(base H2 en modo MySQL, `ddl-auto=create-drop`) y la dependencia `com.h2database:h2` está
declarada con `<scope>test</scope>` en el `pom.xml`. Nada de esto afecta la configuración de
`src/main/resources/application.properties`, que sigue apuntando a MySQL.

## Ejecutar la aplicación

### Opción A — Desde IntelliJ

1. Abrí el proyecto en IntelliJ (`File > Open`, seleccionando la carpeta que contiene el `pom.xml`)
2. Esperá a que IntelliJ indexe y descargue las dependencias de Maven
3. Con el contenedor de MySQL corriendo, buscá la clase `BolsinApplication.java` (tiene la anotación `@SpringBootApplication`)
4. Clic derecho → **Run 'BolsinApplication'**

### Opción B — Desde la terminal

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

## Verificar que todo funciona

Una vez que la consola muestre:

```
Started BolsinApplication in X seconds
```

Abrí el navegador en:

```
http://localhost:8080
```

## Proceso de release

El versionado y la publicación de imágenes están automatizados en GitLab CI
(`.gitlab-ci.yml`). Se sigue **GitFlow**: las releases salen de una rama
`release/X.Y.Z` que se mergea a `master`.

### Requisitos (una sola vez)

En **Settings → CI/CD → Variables** del proyecto:

| Variable | Qué es | Flags |
|---|---|---|
| `DOCKERHUB_USER` | Usuario de Docker Hub (`dondarrion91`) | Masked |
| `DOCKERHUB_TOKEN` | Access token de Docker Hub con permiso *Read & Write* | Masked |
| `DOCKERHUB_IMAGE` | *(opcional)* repo destino. Default: `dondarrion91/bolsin` | — |
| `BACKPORT_TOKEN` | Project Access Token (rol Developer, scopes `api` + `write_repository`). Lo usa el job que abre la MR `master → develop` | Masked, Protected |

> No marques `DOCKERHUB_*` como *Protected* salvo que también declares
> `release/*` como Protected Branch, porque el job de Release Candidate corre
> en el contexto de la rama de origen de la MR.

### Paso a paso

**1. Crear la rama de release desde `develop`**

```bash
git checkout develop && git pull
git checkout -b release/1.0.3
```

El nombre tiene que ser exactamente `release/` + tres números (`MAJOR.MINOR.PATCH`).

**2. Preparar el CHANGELOG**

Movés lo que está bajo `## [Unreleased]` a una sección nueva con la versión y la
fecha, y dejás `[Unreleased]` vacío. Commiteás.

**3. Push y abrir la Merge Request `release/1.0.3 → master`**

```bash
git push -u origin release/1.0.3
```

Abrís la MR en GitLab apuntando a `master`. Esto dispara la pipeline de MR:

| Job | Stage | Qué hace |
|---|---|---|
| `validate-branch-name` | validate | Verifica la convención GitFlow del nombre de rama |
| `validate-changelog` | validate | Exige una entrada nueva en `CHANGELOG.md` (se saltea en MRs de backport) |
| `test-coverage` | test | `mvn verify` + JaCoCo (falla si la cobertura baja del 80%). Publica el `.jar` como artifact |
| `build-release-candidate` | release | Buildea la imagen con el `Dockerfile` y la sube a Docker Hub como **Release Candidate**: `dondarrion91/bolsin:1.0.3-rc.<n>` (inmutable) y `:1.0.3-rc` (puntero al último RC) |
| `tag-release-candidate` | tag | Crea el tag de Git `v1.0.3-rc.<n>` y un Release en GitLab |

El RC solo se genera si `validate` y `test` pasaron en verde.

**4. Mergear la MR a `master`**

Usá **"Merge commit"** (no squash ni fast-forward) para que el mensaje conserve
`Merge branch 'release/1.0.3' into 'master'` — de ahí se saca la versión.

El merge dispara la pipeline de `master`:

| Job | Stage | Qué hace |
|---|---|---|
| `promote-release` | promote | Hace `docker pull` de `:1.0.3-rc` (la imagen exacta ya testeada), la re-taggea y publica la **release oficial**: `dondarrion91/bolsin:1.0.3` y `:latest`. No rebuildea |
| `create-gitlab-release` | tag | Crea el tag de Git `v1.0.3` y el Release oficial en GitLab |
| `backport-to-develop` | backport | Crea la rama `backport/v1.0.3-to-develop` y abre una MR `→ develop` para reintegrar lo liberado |

**5. Mergear la MR de backport a `develop`**

La revisás (puede tener conflictos si `develop` avanzó) y la mergeás. Con eso
`develop` vuelve a estar al día con lo que se liberó.

### Resultado

Después de todo el flujo, para la versión `1.0.3` quedan:

- **Docker Hub**: `dondarrion91/bolsin:1.0.3`, `:1.0.3-rc.<n>`, `:1.0.3-rc`, `:latest`
- **Git / GitLab**: tags `v1.0.3` y `v1.0.3-rc.<n>` + sus Releases
- **`develop`**: actualizado vía la MR de backport

### Desplegar en el droplet (manual, por SSH)

La CI **no** toca el servidor. Una vez publicada la imagen oficial, en el droplet:

```bash
docker pull dondarrion91/bolsin:1.0.3
docker rm -f bolsin-app
docker run -d --name bolsin-app --network bolsin-net \
  --restart unless-stopped \
  -e DB_HOST=mysql8-bolsin -e DB_PORT=3306 \
  -e DB_USER=user -e DB_PASSWORD=<db_pass> \
  -p 8080:8080 dondarrion91/bolsin:1.0.3
```

Nginx (nativo en el droplet) hace de reverse proxy del `:8080` al `:80`.

## Estructura del proyecto

El proyecto sigue el patrón **Frontera-Control-Entidad (BCE)**, propio del modelo de análisis RUP:

```
src/main/java/com/dsigrupo7/bolsin/
├── boundary/     # Clases <<boundary>>: controllers, interacción con actores externos
├── control/      # Clases <<control>>: lógica y orquestación de cada caso de uso
├── entity/       # Clases <<entity>>: entidades JPA persistentes del dominio
└── BolsinApplication.java
```

- **Frontera**: recibe la interacción del actor (usuario/navegador) y delega en el control correspondiente.
- **Control**: coordina el flujo del caso de uso, aplica validaciones y accede a las entidades.
- **Entidad**: representa los conceptos del dominio persistidos en la base de datos.

## Problemas comunes

| Error | Causa probable | Solución |
|---|---|---|
| `Failed to configure a DataSource` | No hay conexión a base de datos configurada | Verificá `application.properties` y que el contenedor de MySQL esté corriendo |
| `Cannot load driver class: com.mysql.cj.jdbc.Driver` | Falta la dependencia o no se resolvió en Maven | Ejecutá `./mvnw clean install -U` |
| `Unable to resolve name [MySQL8Dialect]` | Dialecto obsoleto en Hibernate 6+ | Borrá la línea `hibernate.dialect` de `application.properties` |
| `Server is not specified` (Docker en IntelliJ) | No hay conexión configurada al daemon de Docker | Levantá el contenedor por terminal con `docker compose up -d`, o configurá la conexión en Settings > Docker |

## Contacto / Dudas

Ante cualquier problema al levantar el entorno, consultar con el equipo antes de modificar configuraciones compartidas (`docker-compose.dev.yaml`, `pom.xml`).