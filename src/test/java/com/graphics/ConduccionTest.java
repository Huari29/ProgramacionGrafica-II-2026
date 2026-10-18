package com.graphics; // Permite probar los métodos protegidos del juego desde el mismo paquete.

import java.util.ArrayDeque; // Cola para el recorrido en anchura de la red vial.
import junit.framework.TestCase; // Proporciona las comprobaciones de JUnit usadas por Maven.

/** Comprueba las reglas de la ciudad ampliada sin abrir una ventana OpenGL (ningún test llama a GLFW ni a GL). */
public class ConduccionTest extends TestCase {

    /** El mapa cumple las dimensiones y el contenido mínimos del enunciado. */
    public void testMapaAmpliado() {
        assertTrue("al menos 11 filas", clase1.FILAS >= 11); // Mínimo de filas.
        assertTrue("al menos 11 columnas", clase1.COLUMNAS >= 11); // Mínimo de columnas.
        assertEquals("el mapa debe ser cuadrado", clase1.FILAS, clase1.COLUMNAS); // LIMITE supone un mapa cuadrado.
        assertEquals("la celda conserva su tamaño", 10f, clase1.CELDA, 0f); // Tamaño de celda original.
        assertEquals("el límite sale del mapa", clase1.FILAS * clase1.CELDA / 2, clase1.LIMITE, 0f); // Nada de números fijos.
        int edificios = 0; // Contador de manzanas con edificio.
        int parques = 0; // Contador de parques.
        for (int[] fila : clase1.MAPA) { // Recorre las filas.
            assertEquals("filas de igual longitud", clase1.COLUMNAS, fila.length); // Matriz rectangular.
            for (int celda : fila) { // Recorre las celdas.
                if (celda == clase1.EDIFICIO) { // Manzana con edificio.
                    edificios++; // La cuenta.
                } else if (celda == clase1.PARQUE) { // Parque.
                    parques++; // Lo cuenta.
                }
            }
        }
        assertTrue("al menos 12 manzanas con edificios (hay " + edificios + ")", edificios >= 12); // Requisito.
        assertTrue("al menos 4 parques (hay " + parques + ")", parques >= 4); // Requisito.
    }

    /** Todas las calles forman una única red conectada y contienen el inicio y todos los destinos. */
    public void testRedVialConectadaYDestinosAccesibles() {
        int n = clase1.FILAS; // Tamaño del mapa.
        boolean[][] visitada = new boolean[n][n]; // Celdas ya alcanzadas.
        int totalCalles = 0; // Calles existentes.
        int[] semilla = null; // Primera calle encontrada.
        for (int f = 0; f < n; f++) { // Recorre filas.
            for (int c = 0; c < n; c++) { // Recorre columnas.
                if (clase1.MAPA[f][c] == clase1.CALLE) { // Calle.
                    totalCalles++; // La cuenta.
                    if (semilla == null) { // Guarda la primera.
                        semilla = new int[] {f, c}; // Punto de partida del recorrido.
                    }
                }
            }
        }
        ArrayDeque<int[]> cola = new ArrayDeque<>(); // Recorrido en anchura.
        cola.add(semilla); // Empieza en la primera calle.
        visitada[semilla[0]][semilla[1]] = true; // La marca.
        int alcanzadas = 0; // Calles alcanzadas desde la semilla.
        int[][] vecinos = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}; // Cuatro direcciones.
        while (!cola.isEmpty()) { // Mientras queden celdas por explorar.
            int[] p = cola.poll(); // Toma una.
            alcanzadas++; // La cuenta.
            for (int[] d : vecinos) { // Mira a sus vecinas.
                int f = p[0] + d[0]; // Fila vecina.
                int c = p[1] + d[1]; // Columna vecina.
                if (f >= 0 && c >= 0 && f < n && c < n && !visitada[f][c] && clase1.MAPA[f][c] == clase1.CALLE) { // Calle no visitada.
                    visitada[f][c] = true; // La marca.
                    cola.add(new int[] {f, c}); // La explorará.
                }
            }
        }
        assertEquals("todas las calles deben estar conectadas", totalCalles, alcanzadas); // Una sola red vial.
        for (int[] d : clase4.DESTINOS_CELDAS) { // Cada destino de entrega.
            assertEquals("el destino debe estar sobre una calle", clase1.CALLE, clase1.MAPA[d[0]][d[1]]); // Accesible por calle.
            assertTrue("el destino debe ser alcanzable", visitada[d[0]][d[1]]); // Pertenece a la red conectada.
        }
        clase4 juego = new clase4(); // Estado del juego.
        assertTrue("el inicio es transitable", juego.puedeCircular(clase2.INICIO_X, clase2.INICIO_Z)); // Empieza sobre la calle.
        for (float[] d : clase4.DESTINOS) { // Destinos en coordenadas del mundo.
            assertTrue("el auto puede situarse en el destino", juego.puedeCircular(d[0], d[1])); // Sin chocar al llegar.
        }
    }

    /** Hay suficientes farolas, también en los sectores añadidos, y no sobrepasan la capacidad del shader. */
    public void testFarolas() {
        assertTrue("capacidad del shader", clase3.LUCES.length <= clase3.MAX_LUCES); // Cabe en uLuces.
        int enZonaNueva = 0; // Farolas en los sectores añadidos.
        for (float[] luz : clase3.LUCES) { // Cada farola.
            assertTrue("farola dentro del mapa", Math.abs(luz[0]) < clase1.LIMITE && Math.abs(luz[2]) < clase1.LIMITE); // No fuera del suelo.
            int columna = (int) Math.floor((luz[0] + clase1.LIMITE) / clase1.CELDA); // Columna de la farola.
            int fila = (int) Math.floor((luz[2] + clase1.LIMITE) / clase1.CELDA); // Fila de la farola.
            if (columna >= clase1.PRIMERA_NUEVA || fila >= clase1.PRIMERA_NUEVA) { // Zona añadida.
                enZonaNueva++; // La cuenta.
            }
        }
        assertTrue("al menos nueve farolas en la zona nueva (hay " + enZonaNueva + ")", enZonaNueva >= 9); // Requisito.
    }

    /** Las colisiones se adaptaron a la ciudad ampliada: edificios, parques, aceras y bordes nuevos. */
    public void testObstaculosYLimites() {
        clase2 juego = new clase2(); // Crea un vehículo con su radio de colisión inicial.
        assertFalse(juego.puedeCircular(-40, -40)); // Edificio del sector original (fila 1, columna 1).
        assertFalse(juego.puedeCircular(-40, -20)); // Parque original.
        assertFalse(juego.puedeCircular(20, 20)); // Edificio de la ampliación (fila 7, columna 7).
        assertFalse(juego.puedeCircular(40, 0)); // Parque de la ampliación este (fila 5, columna 9).
        assertFalse(juego.puedeCircular(-44, -40)); // Invade la acera de una manzana.
        assertTrue(juego.puedeCircular(-47, -40)); // Calle con separación suficiente de la acera.
        assertTrue(juego.puedeCircular(50, 50)); // Esquina sureste nueva: antes quedaba fuera del mapa.
        assertFalse(juego.puedeCircular(54, 0)); // Demasiado cerca del nuevo borde este (55).
        assertFalse(juego.puedeCircular(0, 56)); // Fuera del nuevo borde sur.
        assertFalse(juego.puedeCircular(-56, 0)); // Fuera del borde oeste.
        assertFalse(juego.puedeCircular(0, -56)); // Fuera del borde norte.
        for (float x = -50; x <= 50; x += 0.25f) { // Recorre toda la calle norte.
            assertTrue(juego.puedeCircular(x, -50)); // Debe ser transitable de extremo a extremo.
            assertTrue(juego.puedeCircular(x, 50)); // Igual la calle sur nueva.
        }
    }

    /** El reinicio restaura posición, orientación y velocidad, y vuelve a empezar las entregas. */
    public void testReinicio() {
        clase5 juego = new clase5(); // Versión final sin inicializar OpenGL.
        juego.autoX = 12; // Simula un desplazamiento.
        juego.autoZ = 4; // Simula un desplazamiento.
        juego.velocidad = 10; // Simula que avanza.
        juego.angulo = 2; // Simula un giro.
        juego.entregas = 3; // Simula progreso.
        juego.tiempo = 40; // Simula tiempo transcurrido.
        juego.trafico.get(0).x = 0; // Desplaza un vehículo autónomo.
        juego.reiniciar(); // Mismo método que ejecuta la tecla R.
        assertEquals(clase2.INICIO_X, juego.autoX, 0f); // X inicial.
        assertEquals(clase2.INICIO_Z, juego.autoZ, 0f); // Z inicial.
        assertEquals(0f, juego.velocidad, 0f); // Detenido.
        assertEquals(0f, juego.angulo, 0f); // Mirando al norte.
        assertEquals(0, juego.entregas); // Entregas a cero.
        assertEquals(0f, juego.tiempo, 0f); // Cronómetro a cero.
        assertEquals(new clase5().trafico.get(0).x, juego.trafico.get(0).x, 0f); // El tráfico vuelve a su origen.
    }

    /** Se pueden completar todas las entregas, solo frenando en el destino, y luego repetir la actividad tras R. */
    public void testEntregasCompletasYRepeticion() {
        clase4 juego = new clase4(); // Versión obligatoria completa.
        for (int vuelta = 0; vuelta < 2; vuelta++) { // Primera partida y partida tras el reinicio.
            for (int i = 0; i < clase4.DESTINOS.length; i++) { // Cada parada en orden.
                juego.autoX = clase4.DESTINOS[i][0]; // Coloca el auto en el destino...
                juego.autoZ = clase4.DESTINOS[i][1]; // ...activo.
                juego.velocidad = 10; // Pasa a toda velocidad: no debe contar.
                juego.comprobarEntrega(); // Aplica la regla.
                assertEquals("sin frenar no se entrega", i, juego.entregas); // Sigue en la misma.
                juego.velocidad = 0; // Ahora frena.
                juego.comprobarEntrega(); // Aplica la regla.
                assertEquals("frenando se entrega", i + 1, juego.entregas); // Avanza a la siguiente.
            }
            assertEquals(clase4.DESTINOS.length, juego.entregas); // Todas completadas.
            juego.reiniciar(); // Tecla R.
            assertEquals(0, juego.entregas); // Vuelve a empezar.
        }
    }

    /** Ningún vehículo autónomo atraviesa un edificio ni sale del mapa, y todos avanzan sin quedarse bloqueados. */
    public void testTraficoRespetaCallesYAvanza() {
        clase5 juego = new clase5(); // Crea el tráfico (sin OpenGL).
        assertTrue("al menos tres vehículos autónomos", juego.trafico.size() >= 3); // Requisito opcional.
        float[] recorrido = new float[juego.trafico.size()]; // Distancia acumulada de cada vehículo.
        float dt = 1 / 30f; // Simula 30 cuadros por segundo.
        for (int paso = 0; paso < 30 * 600; paso++) { // Diez minutos de simulación.
            for (int i = 0; i < juego.trafico.size(); i++) { // Cada vehículo.
                VehiculoAutonomo v = juego.trafico.get(i); // Vehículo actual.
                float x0 = v.x; // Posición anterior X.
                float z0 = v.z; // Posición anterior Z.
                v.actualizar(dt, juego.trafico, clase2.INICIO_X, clase2.INICIO_Z); // Avanza con el jugador parado en el inicio.
                assertFalse("NaN en posición", Float.isNaN(v.x) || Float.isNaN(v.z)); // Sin errores numéricos.
                assertTrue("vehículo " + i + " fuera de la calzada en (" + v.x + ", " + v.z + ") paso " + paso, juego.puedeCircular(v.x, v.z)); // Sin edificios ni salir.
                recorrido[i] += (float) Math.hypot(v.x - x0, v.z - z0); // Suma lo avanzado.
            }
        }
        for (int i = 0; i < recorrido.length; i++) { // Comprueba que todos circularon.
            assertTrue("el vehículo " + i + " apenas avanzó: " + recorrido[i], recorrido[i] > 1500); // Sin bloqueos.
        }
    }

    /** El auto del jugador no puede meterse dentro de un vehículo autónomo, pero sí alejarse de él. */
    public void testJugadorNoAtraviesaTrafico() {
        clase5 juego = new clase5(); // Crea el tráfico.
        VehiculoAutonomo v = juego.trafico.get(0); // Un vehículo cualquiera.
        juego.autoX = v.x; // Coloca al jugador a 4 unidades al este.
        juego.autoZ = v.z;
        juego.autoX += 4; // Separación inicial.
        assertTrue("acercarse hasta solaparse choca", juego.chocaConVehiculos(v.x + 1, v.z)); // Se acerca y se solapa.
        assertFalse("alejarse no choca", juego.chocaConVehiculos(v.x + 6, v.z)); // Se aleja.
    }

    /** Las rutas se calculan en el carril derecho y rechazan recorridos que no siguen las calles. */
    public void testRutaPorCarril() {
        float[][] r = VehiculoAutonomo.rutaPorCarril(new int[][] {{0, 0}, {0, 10}, {10, 10}, {10, 0}}); // Perímetro horario.
        assertEquals(4, r.length); // Un punto por esquina.
        assertEquals(clase1.centro(0) + 2.5f, r[0][0], 0.001f); // Esquina noroeste: desplazada hacia el interior (este).
        assertEquals(clase1.centro(0) + 2.5f, r[0][1], 0.001f); // Y hacia el interior (sur).
        try { // Un tramo diagonal no sigue ninguna calle.
            VehiculoAutonomo.rutaPorCarril(new int[][] {{0, 0}, {2, 2}, {0, 4}});
            fail("debía rechazar un tramo diagonal"); // No debe llegar aquí.
        } catch (IllegalArgumentException esperado) { // Comportamiento correcto.
            assertNotNull(esperado.getMessage()); // Tiene mensaje explicativo.
        }
    }

    /** Todos los glifos de la fuente tienen 7 filas de 5 casillas. */
    public void testFuentePixeles() {
        for (char c : "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:/.-!, ".toCharArray()) { // Todos los caracteres usados.
            String[] g = FuentePixeles.glifo(c); // Glifo del carácter.
            assertEquals(FuentePixeles.ALTO, g.length); // Siete filas.
            for (String fila : g) { // Cada fila.
                assertEquals(FuentePixeles.ANCHO, fila.length()); // Cinco casillas.
            }
        }
        assertEquals(FuentePixeles.ALTO, FuentePixeles.glifo('~').length); // Un carácter desconocido devuelve un glifo vacío válido.
    }
}
