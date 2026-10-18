# Ciudad interactiva ampliada con OpenGL (11 × 11)

Ampliación del proyecto *CiudadConduccion* (mapa original de 7 × 7). Java 17, Maven, LWJGL 3.3.3, GLFW y OpenGL 3.3 Core. Toda la geometría se genera con código: no hay imágenes ni modelos externos.

## Ejecutar (versión final)

Abre una terminal en la carpeta donde está `pom.xml` (JDK 17 o superior y Maven; la primera vez se descargan las dependencias):

```sh
mvn compile exec:exec
```

Es equivalente a `mvn compile exec:exec -DmainClass=com.graphics.clase5`. Si algo falla en la demostración, `clase4` es una versión completa con **todos los requisitos obligatorios** y sin las mejoras opcionales:

```sh
mvn compile exec:exec -DmainClass=com.graphics.clase4
```

Pruebas automáticas (no abren ventana): `mvn test`. En macOS el perfil Maven añade `-XstartOnFirstThread`; si ejecutas desde un IDE, añádelo a las opciones de la JVM. Requiere sesión gráfica y OpenGL 3.3.

## Diseño de la ciudad

- **Mapa 11 × 11** con celdas de 10 × 10 (mismo tamaño que antes): 121 celdas, ciudad de 110 × 110 unidades. `MAPA` usa 0 = calle, 1 = edificio, 2 = parque; las calles ocupan las filas y columnas pares, y las manzanas las impares.
- **Ampliación:** las filas/columnas 0 a 6 son el mapa original (intacto). Se añadieron las filas y columnas 7 a 10: distrito este, distrito sur y barrio nuevo. La antigua calle del borde (fila/columna 6) pasó a ser una avenida interior y aparecieron nuevas calles de borde.
- **25 manzanas: 18 edificios y 7 parques** (4 parques nuevos). Altura de edificios entre 5 y 13 y color variable según su posición.
- **Cuatro sectores** con tono de acera distinto: Casco Antiguo (original), Distrito Este, Distrito Sur y Barrio Nuevo.
- **Red vial única y conectada:** una prueba automática recorre las calles y comprueba que el inicio y los cinco destinos son alcanzables.
- **Coherencia del escenario:** suelo, `LIMITE` (55), colisiones, cámaras, escala del minimapa, posición inicial y destinos salen del tamaño de `MAPA`; no quedan números fijos del mapa antiguo.

## Controles

| Tecla | Acción |
|---|---|
| W / S o ↑ / ↓ | Acelerar / frenar y marcha atrás |
| A / D o ← / → | Girar |
| Espacio | Freno |
| C | Cambiar cámara (seguimiento / aérea) |
| N | Día / noche |
| F | Encender / apagar faros |
| M | Mostrar / ocultar minimapa |
| R | Reiniciar (auto, velocidad, entregas y tráfico) |
| H | Mostrar / ocultar ayuda |
| P | Pausa |
| ESC | Salir |

## Funciones implementadas

**Organización por responsabilidad** (cada clase hereda de la anterior):

| Clase | Responsabilidad |
|---|---|
| `clase1` | Ciudad, mapa, cámaras, renderizador y shaders base |
| `clase2` | Conducción, tiempo entre cuadros, colisiones con la ciudad |
| `clase3` | Iluminación: día/noche, farolas, faros |
| `clase4` | Decoración, semáforos, entregas, minimapa (requisitos obligatorios) |
| `clase5` | Mejoras opcionales: tráfico autónomo e interfaz |
| `VehiculoAutonomo`, `FuentePixeles` | Lógica del tráfico y fuente de texto, sin OpenGL |

- **Iluminación:** luz ambiental y direccional; difusa calculada con las normales; de noche, cielo oscuro y ambiente tenue. **34 farolas** generadas desde `MAPA` (25 de ellas en los sectores nuevos: una por manzana más otra en las manzanas que dan a los bordes sur y este), con luz que decae con la distancia y se anula a 30 unidades. Dos focos delanteros en cono con alcance limitado (tecla F). La bombilla brillante (emisión) está separada de la luz que recibe el entorno.
- **Decoración:** árboles y bancos en los parques, ventanas (de noche, unas encendidas y otras apagadas), pasos peatonales y semáforos.
- **Semáforos:** ciclo de 12 s (rojo 5, verde 5, amarillo 2) controlado por un reloj propio, desfasado entre manzanas vecinas. Son solo visuales.
- **Entregas:** 5 destinos repartidos por los sectores nuevos; se completan frenando (< 1 u/s) dentro de 3 unidades de la marca dorada. Al terminar aparece el resultado y **R** permite repetir.
- **Minimapa:** vista superior con toda la ciudad, posición y orientación del auto, destino activo (pulsa) y el tráfico; norte arriba (rótulo N). Conserva su esquina y forma cuadrada al redimensionar y usa `scissor`, así que no toca la vista principal.
- **Título de la ventana:** velocidad, ambiente, faros, entregas, tiempo y sector primero; los controles, al final.
- **Tráfico autónomo (opcional):** 4 vehículos con rutas cerradas por el carril derecho, giros suaves, velocidad propia y frenada ante vehículos delante. No atraviesan edificios ni salen del mapa (probado con 10 minutos simulados). El jugador no puede atravesarlos.
- **Interfaz (opcional):** panel con velocidad (cifras y barra), estado de faros y ambiente, entrega, tiempo y nombre del sector; ayuda de controles (H), pausa (P) y mensaje final. El texto usa una fuente de píxeles propia dibujada con rectángulos (sin archivos de fuente).

## Limitaciones

- No hay sombras: las farolas iluminan fachadas aunque haya otro edificio por medio.
- Los semáforos no detienen a nadie (alcance pedido); los vehículos autónomos no se ceden el paso en los cruces y solo guardan distancia con los que van en su mismo sentido.
- Colisiones con círculos y rectángulos, sin física de choque.
- La fuente solo tiene mayúsculas, dígitos y algunos signos, sin tildes.
- Las capturas y el comportamiento se comprobaron con un entorno de pruebas propio (ver *Recursos externos*); conviene ejecutar el juego real antes de entregar.

## Recursos externos

- Proyecto base *CiudadConduccion* (7 × 7) entregado en clase: estructura de clases, renderizador de cajas y shaders de partida.
- Bibliotecas: LWJGL 3.3.3 (GLFW y OpenGL), JUnit 3.8.1 (tests).
- Fuente de píxeles 5 × 7 y rutas de tráfico: diseño propio.
- Asistente de IA (Claude, de Anthropic) como apoyo al desarrollo. **Declara su uso según las normas de tu curso.**
