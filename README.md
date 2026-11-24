# Logística de los Reyes Magos — Java sin Spring Boot

API REST construida con **Java 17 o superior**, el servidor HTTP incluido en Java (`HttpServer`), JDBC y base de datos H2. No utiliza Spring ni otro framework web. Se puede abrir y trabajar desde Visual Studio Code.

## 1. Requisitos
- JDK 17 o superior.
- Apache Maven 3.8 o superior (solo administra las dos librerías externas: H2 y Jackson).
- Visual Studio Code y la extensión **Extension Pack for Java**.
- Postman.

> Visual Studio Code es el editor. Java ejecuta la aplicación y Maven descarga las dependencias. No necesitas instalar Spring Boot.

## 2. Ejecutar desde VS Code
1. Descomprime el proyecto y abre en VS Code la carpeta que contiene `pom.xml`.
2. Abre Terminal → New Terminal.
3. Comprueba que Java y Maven estén instalados:
   ```bash
   java -version
   mvn -version
   ```
4. Ejecuta:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass="com.elkin.reyes.Main"
   ```
   Si el comando `exec:java` no está disponible en tu Maven, usa:
   ```bash
   mvn clean package
   java -cp "target/classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.elkin.reyes.Main
   ```
5. Debe aparecer `Reyes Magos API activa en http://localhost:8080`.
6. En Postman prueba `GET http://localhost:8080/`.

En Ubuntu/Zorin, si falta Maven: `sudo apt update && sudo apt install maven`.

## 3. Base de datos
Se utiliza H2 en modo archivo. Se crea automáticamente en `./data/reyesdb`; los datos permanecen aunque cierres la aplicación. No tienes que crear tablas manualmente.

Opcional: cambia la conexión con la variable de entorno `DB_URL`. Para usar otra base de datos habría que cambiar el driver JDBC y las sentencias de `Database.java`.

## 4. Tokens de prueba
- Jefe de caravana: `dev-jefe-caravana`
- Ayudante: `dev-ayudante`

En Postman, para las rutas protegidas, agrega `Authorization: Bearer dev-jefe-caravana`. El token de ayudante debe devolver 403 en rutas reservadas al jefe. Para probar 401, omite el encabezado.

En un entorno real cambia estos valores mediante variables de entorno:
- `TOKEN_JEFE_CARAVANA`
- `TOKEN_AYUDANTE`
- `PORT` (por defecto 8080)
- `DB_URL` (opcional; por defecto utiliza la base H2 en `./data/reyesdb`)
- `SOAP_ESTRELLA_URL` (por defecto `http://localhost:8081/soap/estrella`)

La aplicación lee estas variables del entorno del sistema; no carga automáticamente un archivo `.env`. En Linux/Zorin, puedes definirlas en la terminal antes de iniciar la API:

```bash
export TOKEN_JEFE_CARAVANA="dev-jefe-caravana"
export TOKEN_AYUDANTE="dev-ayudante"
export PORT="8080"
export DB_URL="jdbc:h2:file:./data/reyesdb"
export SOAP_ESTRELLA_URL="http://localhost:8081/soap/estrella"
```

Después ejecuta el comando de inicio indicado en la sección 2, en esa misma terminal. Si no defines alguna variable, se utilizará su valor predeterminado.

## 5. Endpoints
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/caravanas` | Jefe |
| GET | `/api/caravanas?estado=DETENIDA` | Público |
| GET | `/api/caravanas/{id}` | Público |
| PUT | `/api/caravanas/{id}` | Jefe; solo DETENIDA |
| PATCH | `/api/caravanas/{id}/estado` | Jefe |
| POST | `/api/caravanas/{id}/paradas` | Jefe |
| GET | `/api/caravanas/{id}/paradas` | Público |
| PATCH | `/api/paradas/{id}/estado` | Público |
| DELETE | `/api/paradas/{id}` | Jefe; solo PLANIFICADA |
| GET | `/api/caravanas/{id}/itinerario` | Público (extra) |

### JSON de ejemplo
Crear caravana:
```json
{"rey":"MELCHOR","ofrenda":"ORO","origen":"Persia","camellos":6}
```

Crear parada:
```json
{"ciudad":"Bagdad","orden":1,"diasEstancia":1}
```

Cambiar estado de caravana:
```json
{"estado":"EN_CAMINO"}
```

Cambiar estado de parada:
```json
{"estado":"CUMPLIDA"}
```

## 6. Reglas implementadas
- Caravana nueva en `DETENIDA`; transiciones de estado permitidas según la consigna.
- Antes de `EN_CAMINO` consulta SOAP; `NUBLADA` devuelve 422 y Fault/timeout devuelve 503 sin cambiar el estado.
- No se permite `EN_BELEN` con paradas `PLANIFICADA`.
- Las paradas se cumplen en orden.
- No se agregan paradas si la caravana está `EN_BELEN` u `OFRENDA_ENTREGADA`.
- Errores con `codigo`, `mensaje`, `detalles`, `timestamp` y `ruta`.
- SOAP se consume en una clase adaptadora independiente; timeout de 3 segundos y hasta 2 intentos.

## 7. Servicio SOAP
El endpoint predeterminado es `http://localhost:8081/soap/estrella`. El docente debe proporcionar el endpoint/WSDL del mock. Configura `SOAP_ESTRELLA_URL` con esa dirección antes de probar la respuesta `VISIBLE` o `NUBLADA`. Si el servicio no está levantado, la API debe responder 503 y conservar la caravana en `DETENIDA`. Para provocar el Fault, la consigna indica enviar el origen `Herodes` al mock.

## 8. Arquitectura
`ApiHandler` recibe la petición HTTP; `SecurityMiddleware` valida el Bearer token; `RouteDispatcher` resuelve las rutas; `CaravanaController` delega en `CaravanaService`, donde viven las reglas de negocio; `CaravanaRepository` y `ParadaRepository` ejecutan SQL JDBC; `Database` crea las tablas H2; `EstrellaAdapter` encapsula la llamada SOAP; y `GlobalExceptionHandler` unifica los errores. Ver [diagrama PNG](docs/arquitectura.png) o abrir el archivo editable [arquitectura.drawio](docs/arquitectura.drawio) en https://app.diagrams.net/. El middleware, el enrutador, los controladores y el manejador global de errores están separados en clases independientes para respetar la arquitectura en capas.

## 9. Microservicios
El sistema podría dividirse en dos microservicios: **Caravanas**, encargado de los reyes, ofrendas, camellos y estados; e **Itinerarios**, responsable de las paradas, ciudades, orden y días de estancia. Cada servicio administraría sus propios datos y expondría una API para comunicarse con el otro. La navegación de la Estrella seguiría siendo un servicio externo consumido mediante SOAP. Si el servicio de itinerarios cae el 24 de diciembre a las 23:00, la API de caravanas debería continuar disponible para consultar y gestionar las caravanas, pero las operaciones que dependan de las paradas quedarían temporalmente indisponibles. Se respondería con un error controlado, sin perder datos ni modificar estados incorrectamente, y se permitiría reintentar cuando el servicio se recupere.

## 10. Verificación y consideraciones
El proyecto se compiló y ejecutó correctamente en el entorno local mediante Maven, y la API respondió a las pruebas realizadas desde Postman. La colección incluye 17 solicitudes para cubrir los casos 200/201, 400, 401, 403, 404, 409, 422 y 503. Los IDs de caravana y paradas se guardan automáticamente en variables de colección al ejecutar las solicitudes de creación. Los casos 422 de SOAP requieren el mock configurado en `NUBLADA` o una caravana `EN_CAMINO` con paradas pendientes. Los tokens incluidos son de demostración y no constituyen un sistema de autenticación JWT para producción. Antes de la evaluación, configura y prueba el endpoint SOAP/WSDL indicado por el docente.

## 11. Casos de Postman y orden de prueba
1. Ejecuta `01 - Crear caravana (201)`; la colección guarda automáticamente `caravanaId`.
2. Ejecuta `02 - Listar caravanas (200)` y `02B - Consultar caravana por ID (200)`.
3. Ejecuta `03B` para probar PUT. Luego `07 - Agregar parada 1 (201)` y `08 - Agregar parada 2 (201)`; se guardan sus IDs.
4. Ejecuta `09` para demostrar el conflicto 409 por incumplir el orden y `09B` para probar DELETE de la parada 2.
5. `10B` requiere SOAP configurado para `NUBLADA`; `10C` requiere una caravana `EN_CAMINO` con paradas `PLANIFICADA`; ambos demuestran 422.
6. `11` demuestra 503 cuando SOAP no responde. Para probar el Fault específico, configura el origen `Herodes` y utiliza el mock del docente.

Para repetir la creación desde cero, detén la API y elimina la carpeta `data/` (se regenerará al iniciar); esto borra los datos locales de prueba.
