# Backend Challenge: API REST en Spring Boot con WebFlux

Este proyecto implementa una API REST en **Spring Boot** utilizando **Java 21** que cumple con varios requisitos funcionales y técnicos. A continuación se describe cada uno de los requisitos y cómo se ha abordado su implementación.

## Funcionalidades principales

### 1. Cálculo con porcentaje dinámico
- Se implementó un **endpoint REST** que recibe dos números `num1` y `num2`, y devuelve el resultado de la suma de ambos números con un **porcentaje adicional** obtenido de un servicio externo.
- En este caso, el servicio externo es un **mock** que retorna un valor fijo de porcentaje (por ejemplo, un 10%).
- La fórmula de cálculo es: `(num1 + num2) + (porcentaje de servicio externo)`.

### 2. Caché del porcentaje
- El porcentaje obtenido del servicio externo se almacena en **memoria (caché)** utilizando **Redis**. Este valor es válido por **30 minutos**.
- Si el servicio externo falla, se utiliza el último valor almacenado en caché.
- Si no hay un valor almacenado en caché, se responde con un **error HTTP 503**.

### 3. Reintentos ante fallos del servicio externo
- Se implementó una lógica de **reintentos** para el servicio externo con un máximo de **3 intentos**. Si el servicio sigue fallando, se utiliza el valor almacenado en caché (si existe) o se devuelve un error.

### 4. Historial de llamadas
- Se creó un **endpoint** para consultar un historial de todas las llamadas realizadas a los endpoints de la API.
- El historial incluye detalles como:
  - Fecha y hora de la llamada.
  - Endpoint invocado.
  - Parámetros recibidos.
  - Respuesta (o error retornado).
- La consulta del historial soporta **paginación** y se registra de forma **asíncrona** para no afectar el rendimiento de los endpoints principales.

### 5. Control de tasas (Rate Limiting)
- La API está configurada para permitir un máximo de **3 solicitudes por minuto (RPM)** por cliente.
- Si se excede este umbral, se responde con el error **HTTP 429 (Too Many Requests)**.

### 6. Manejo de errores HTTP
- Se implementa un manejo adecuado de errores HTTP, con mensajes descriptivos para los códigos de error **4XX** y **5XX**.

## Requerimientos técnicos

### 1. Base de datos
- La API utiliza **PostgreSQL** para almacenar el historial de llamadas.
- La base de datos se ejecuta en un **contenedor Docker** y se configura mediante **Docker Compose**.

### 2. Despliegue
- El servicio se ejecuta en un **contenedor Docker**.
- La imagen se publica en un repositorio público de **Docker Hub**.
- El archivo `docker-compose.yml` se proporciona para levantar tanto la API como la base de datos fácilmente.

### 3. Documentación
- Se generó documentación para la API utilizando **Swagger**.
- Instrucciones claras en el repositorio sobre cómo ejecutar el proyecto y utilizar los endpoints.

## Estructura del proyecto

### Dependencias clave en el `pom.xml`:

- **Spring Boot WebFlux**: para la creación de la API REST.
- **R2DBC PostgreSQL**: para la conexión reactiva con la base de datos PostgreSQL.
- **Spring Data Redis Reactive**: para almacenar y gestionar los valores en caché de forma reactiva.
- **Springdoc OpenAPI**: para generar la documentación de la API con Swagger.

### Docker y Docker Compose
El proyecto está configurado para ejecutarse tanto la API como la base de datos PostgreSQL en contenedores Docker. Utiliza el siguiente archivo `docker-compose.yml`: