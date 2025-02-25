# Backend Challenge: API REST en Spring Boot con WebFlux

Este proyecto implementa una API REST en **Spring Boot con WebFlux** utilizando **Java 21** que cumple con varios requisitos funcionales y técnicos. A continuación se describe cada uno de los requisitos y cómo se ha abordado su implementación.

## Funcionalidades principales

### 1. Cálculo con porcentaje dinámico
- Se implementó un **endpoint REST POST /calculate** en CalculatorController que recibe dos números `first` y `second`, y devuelve el resultado de la suma de ambos números con un **porcentaje adicional** obtenido de un servicio externo.
- En este caso, el servicio externo es consumido mediente PorcentageClient usando WebClient de WebFlux, como tal servicio externo no existe se hacen 3 reintentos, en caso afirmativo se almacena dicho porcentaje en una cache de memoria durancte 30 minutos. Como falla en todos sus reintentos, escribe en log cada uno de ellos y termina devolviendo un valor **mock** que retorna un valor fijo de porcentaje (10%).
- La fórmula de cálculo es: `(first + second) + (porcentaje de servicio externo)`.

### 2. Caché del porcentaje
- El porcentaje obtenido del servicio externo se almacena en **memoria caché**. Este valor es válido por **30 minutos** (Definido como constante en CacheItem).
- Se utiliza el último valor almacenado en caché si existe y no esta expirado, en caso contrario se consume la API EXTERNA.
- Si no hay un valor almacenado en caché, se consume ls API Externa, si no consigue un valor en sus 3 reintentos, se retonar el valor por Default. (Por lo que este servicio nunca devuelve una Exception por falta de dato)

### 3. Reintentos ante fallos del servicio externo
- Se implementó una lógica de **reintentos** para el servicio externo con un máximo de **3 intentos**. Si el servicio sigue fallando, se utiliza el valor por Default.

### 4. Historial de llamadas
- Se creó un **endpoint REST GET /request-history** para consultar un historial de todas las request realizadas a los endpoints de la API.
- El historial incluye detalles como:
  - Fecha y hora de la llamada.
  - Endpoint invocado.
  - Parámetros recibidos.
  - Respuesta (o error retornado).
- La consulta del historial soporta **paginación** y se registra de forma **asíncrona** utilizando **r2dbc-postgresql** para no afectar el rendimiento de los endpoints principales. Se creo un Reopository que extiende de ReactiveCrudRepository para tratar asincronamente las transactions a la DB. Como no se encontro la pagination en estos Repository se hace mediante LIMIT y OFFSET en una query. Se uso @Table para mapear la entidad a la tabla creada y Lombok para crear los metodos getters y setters, constructors y builder.
- Se implemento un interceptor WebFilter para almanacenar todas las request que recibe nuestra API.

### 5. Control de tasas (Rate Limiting)
- La API está configurada para permitir un máximo de **3 solicitudes por minuto (RPM)** por cliente. Esto se logro haciendo uso de un Interceptor aplicando **WebFilter** y el **uso de Redis en un docker externo** al de la API REST.
- Si se excede este umbral, se responde con el error **HTTP 429 (Too Many Requests)**.

### 6. Manejo de errores HTTP
- Se implementa un manejo adecuado de errores HTTP, con mensajes descriptivos para los códigos de error **4XX** y **5XX** en un **ControllerAdvice**. Este captura Exceptions de PostSQl, Redis, Genericas, de WebClient, etc capturandolas y devolviendo un response {"details": "valor_details", "error": "valor_error"}


## Requerimientos técnicos

### 1. Base de datos
- La API utiliza **PostgreSQL** para almacenar el historial de request e implementa r2dbc para mantener la asincronizadad.

### 2. Despliegue
- El servicio se ejecuta en un **contenedor Docker**.
- El archivo `docker-compose.yml` se proporciona para levantar tanto la API como la base de datos y Redis fácilmente.
- - openjdk:21-jdk (challenge-api). Este contiene Java21 y el .jar compilado de nuestra API REST.
- - postgres:13 (tenpo-challenge-db-1). Este contiene PostgreSQL 13 con un schema llamado register. La API se encarga de construir las estructuras correspondientes mediante el sql.init.mode allways.
- - redis:alpine (tenpo-challenge-redis-1). Este contiene un Redis.

### 3. Documentación
- Se generó documentación para la API utilizando **Open API (Swagger)** en su version mas reciente (2.8.5).
- Instrucciones claras en el repositorio sobre cómo ejecutar el proyecto y utilizar los endpoints.

## Estructura del proyecto

### Dependencias clave en el `pom.xml`:

- **Spring Boot WebFlux**: para la creación de la API REST.
- **R2DBC PostgreSQL**: para la conexión reactiva con la base de datos PostgreSQL.
- **Spring Data Redis Reactive**: para almacenar y gestionar los valores en caché de forma reactiva.
- **Springdoc OpenAPI**: para generar la documentación de la API con Swagger.

### Docker y Docker Compose
El proyecto está configurado para ejecutarse tanto la API como la base de datos PostgreSQL en contenedores Docker. Utiliza el siguiente archivo `docker-compose.yml`: