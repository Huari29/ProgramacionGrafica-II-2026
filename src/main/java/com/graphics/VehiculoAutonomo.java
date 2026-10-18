package com.graphics; // Junto al resto de clases; no depende de OpenGL, así puede probarse sin ventana.

import java.util.List; // Lista de vehículos con los que puede tener que mantener distancia.

/**
 * Vehículo del tráfico autónomo: recorre una ruta cerrada de puntos situados en el carril derecho de las calles.
 * Mantiene su propia posición, orientación y velocidad; gira suavemente en las esquinas y frena si hay otro vehículo delante.
 * Convención igual que el auto del jugador: el frente local es -Z y el ángulo 0 apunta hacia -Z (norte del minimapa).
 */
public final class VehiculoAutonomo {

    public static final float VELOCIDAD_CRUCERO = 7; // Unidades por segundo (unos 25 km/h): más lento que el jugador, que llega a 16.
    public static final float GIRO_MAXIMO = 2.6f; // Radianes por segundo que puede girar; limita la curvatura en las esquinas.
    public static final float RADIO_LLEGADA = 2; // Distancia a la que se considera alcanzado un punto de la ruta.
    public static final float DISTANCIA_SEGUIMIENTO = 8; // Distancia a la que empieza a frenar por un vehículo delante.
    public static final float DISTANCIA_MINIMA = 4.5f; // Distancia a la que se detiene por completo.
    public static final float DESPLAZAMIENTO_CARRIL = 2.5f; // Separación entre el eje de la calle y el centro de cada carril.

    public float x; // Posición X actual en el mundo.
    public float z; // Posición Z actual en el mundo.
    public float angulo; // Orientación en radianes; cero apunta hacia -Z.
    public float velocidad; // Velocidad actual en unidades por segundo (nunca negativa: solo avanza).
    public final float rojo; // Componente roja de la carrocería.
    public final float verde; // Componente verde de la carrocería.
    public final float azul; // Componente azul de la carrocería.

    private final float[][] ruta; // Puntos {x, z} de la ruta cerrada, ya desplazados al carril derecho.
    private final int indiceInicial; // Punto desde el que arranca; sirve para reiniciar.
    private int objetivo; // Índice del punto hacia el que se dirige ahora.

    /** Crea un vehículo que parte del punto indiceInicial de la ruta, mirando hacia el siguiente. */
    public VehiculoAutonomo(float[][] ruta, int indiceInicial, float rojo, float verde, float azul) {
        this.ruta = ruta; // Guarda la ruta que recorrerá indefinidamente.
        this.indiceInicial = indiceInicial; // Recuerda el punto de partida para reiniciar.
        this.rojo = rojo; // Guarda el color de carrocería.
        this.verde = verde; // Guarda el color de carrocería.
        this.azul = azul; // Guarda el color de carrocería.
        reiniciar(); // Coloca el vehículo en su estado inicial.
    }

    /** Devuelve el vehículo al punto inicial de su ruta, detenido y orientado hacia el siguiente punto. */
    public final void reiniciar() {
        x = ruta[indiceInicial][0]; // Coloca el vehículo sobre el punto inicial.
        z = ruta[indiceInicial][1]; // Coloca el vehículo sobre el punto inicial.
        objetivo = (indiceInicial + 1) % ruta.length; // Se dirige al punto siguiente.
        velocidad = 0; // Arranca detenido y acelera progresivamente.
        angulo = anguloHacia(ruta[objetivo][0] - x, ruta[objetivo][1] - z); // Mira hacia su primer destino.
    }

    /** Ángulo (con la convención del proyecto) de un vector dirección (dx, dz). */
    static float anguloHacia(float dx, float dz) {
        return (float) Math.atan2(-dx, -dz); // Con dx = 0 y dz < 0 (norte) devuelve 0, igual que el auto del jugador.
    }

    /** Normaliza un ángulo al intervalo (-PI, PI] para calcular siempre el giro más corto. */
    static float normalizar(float a) {
        while (a > Math.PI) { // Si es mayor que media vuelta...
            a -= (float) (2 * Math.PI); // ...se resta una vuelta completa.
        }
        while (a <= -Math.PI) { // Si es menor que menos media vuelta...
            a += (float) (2 * Math.PI); // ...se suma una vuelta completa.
        }
        return a; // Devuelve el ángulo equivalente más pequeño.
    }

    /**
     * Avanza el vehículo deltaTime segundos.
     * trafico: vehículos autónomos (incluido este, que se ignora); el jugador se indica por separado.
     */
    public void actualizar(float deltaTime, List<VehiculoAutonomo> trafico, float jugadorX, float jugadorZ) {
        float dx = ruta[objetivo][0] - x; // Separación horizontal al punto objetivo.
        float dz = ruta[objetivo][1] - z; // Separación en profundidad al punto objetivo.
        if (dx * dx + dz * dz < RADIO_LLEGADA * RADIO_LLEGADA) { // Si ya llegó a este punto...
            objetivo = (objetivo + 1) % ruta.length; // ...pasa al siguiente y vuelve al primero al terminar (ruta cerrada).
            dx = ruta[objetivo][0] - x; // Recalcula la dirección hacia el nuevo objetivo.
            dz = ruta[objetivo][1] - z; // Recalcula la dirección hacia el nuevo objetivo.
        }

        float error = normalizar(anguloHacia(dx, dz) - angulo); // Cuánto le falta girar para mirar al objetivo.
        float giro = Math.max(-GIRO_MAXIMO * deltaTime, Math.min(GIRO_MAXIMO * deltaTime, error)); // Limita el giro por cuadro.
        angulo += giro; // Gira suavemente en lugar de cambiar de rumbo de golpe.

        float factorCurva = (float) Math.cos(Math.min(Math.abs(error), Math.PI / 2)); // 1 si va recto; 0 si el objetivo está a 90 grados.
        float deseada = VELOCIDAD_CRUCERO * Math.max(0.35f, factorCurva); // Reduce la velocidad al tomar una esquina.
        deseada = Math.min(deseada, limitePorVehiculoDelante(trafico, jugadorX, jugadorZ)); // Frena si hay alguien delante.

        if (velocidad < deseada) { // Si va más lento de lo deseado...
            velocidad = Math.min(deseada, velocidad + 4 * deltaTime); // ...acelera 4 unidades/s cada segundo.
        } else { // Si va más rápido de lo deseado...
            velocidad = Math.max(deseada, velocidad - 12 * deltaTime); // ...frena con más fuerza (12 unidades/s cada segundo).
        }

        x += -(float) Math.sin(angulo) * velocidad * deltaTime; // Avanza en X según su orientación (el tiempo entre cuadros evita depender de los FPS).
        z += -(float) Math.cos(angulo) * velocidad * deltaTime; // Avanza en Z según su orientación.
    }

    /** Velocidad máxima permitida por los vehículos que tiene delante; Float.MAX_VALUE si nada estorba. */
    private float limitePorVehiculoDelante(List<VehiculoAutonomo> trafico, float jugadorX, float jugadorZ) {
        float limite = Float.MAX_VALUE; // Sin obstáculos, no hay límite.
        for (VehiculoAutonomo otro : trafico) { // Revisa cada vehículo autónomo.
            if (otro != this && (float) Math.cos(otro.angulo - angulo) > 0.7f) { // Solo tiene en cuenta a los que van en su mismo sentido (evita bloqueos mutuos en cruces).
                limite = Math.min(limite, limiteHacia(otro.x, otro.z)); // Se queda con la restricción más fuerte.
            }
        }
        limite = Math.min(limite, limiteHacia(jugadorX, jugadorZ)); // El jugador siempre cuenta, vaya hacia donde vaya.
        return limite; // Devuelve la velocidad máxima segura.
    }

    /** Velocidad máxima segura respecto a un obstáculo en (ox, oz): solo importa si está delante y en su carril. */
    private float limiteHacia(float ox, float oz) {
        float frenteX = -(float) Math.sin(angulo); // Componente X del frente del vehículo.
        float frenteZ = -(float) Math.cos(angulo); // Componente Z del frente del vehículo.
        float relativoX = ox - x; // Posición del obstáculo respecto a este vehículo, en X.
        float relativoZ = oz - z; // Posición del obstáculo respecto a este vehículo, en Z.
        float adelante = relativoX * frenteX + relativoZ * frenteZ; // Producto escalar: distancia medida a lo largo del frente.
        float lateral = Math.abs(relativoX * -frenteZ + relativoZ * frenteX); // Distancia medida hacia el lado (perpendicular al frente).
        if (adelante <= 0 || adelante >= DISTANCIA_SEGUIMIENTO || lateral >= 2.4f) { // Detrás, lejos o en otro carril: no molesta.
            return Float.MAX_VALUE; // Sin restricción.
        }
        if (adelante <= DISTANCIA_MINIMA) { // Demasiado cerca...
            return 0; // ...debe detenerse.
        }
        return VELOCIDAD_CRUCERO * (adelante - DISTANCIA_MINIMA) / (DISTANCIA_SEGUIMIENTO - DISTANCIA_MINIMA); // En medio: frena de forma proporcional.
    }

    /**
     * Convierte una lista de intersecciones {fila, columna} en puntos de ruta situados en el carril derecho.
     * Las intersecciones consecutivas deben compartir fila o columna; la ruta se cierra sola (la última enlaza con la primera).
     * En cada esquina, el punto es la intersección desplazada a la derecha del sentido de entrada y del de salida.
     */
    public static float[][] rutaPorCarril(int[][] celdas) {
        int n = celdas.length; // Número de intersecciones.
        float[][] puntos = new float[n][2]; // Un punto {x, z} por intersección.
        for (int i = 0; i < n; i++) { // Recorre cada intersección de la ruta.
            int[] anterior = celdas[(i + n - 1) % n]; // Intersección de la que viene.
            int[] actual = celdas[i]; // Intersección actual.
            int[] siguiente = celdas[(i + 1) % n]; // Intersección a la que va.
            int entraX = Integer.signum(actual[1] - anterior[1]); // Sentido de entrada en X (columnas = X).
            int entraZ = Integer.signum(actual[0] - anterior[0]); // Sentido de entrada en Z (filas = Z).
            int saleX = Integer.signum(siguiente[1] - actual[1]); // Sentido de salida en X.
            int saleZ = Integer.signum(siguiente[0] - actual[0]); // Sentido de salida en Z.
            if ((entraX != 0 && entraZ != 0) || (saleX != 0 && saleZ != 0)) { // Un tramo diagonal no sigue ninguna calle.
                throw new IllegalArgumentException("Las intersecciones consecutivas deben compartir fila o columna"); // Informa del error de diseño.
            }
            if (entraX == -saleX && entraZ == -saleZ) { // Dar media vuelta no está soportado.
                throw new IllegalArgumentException("La ruta no puede dar media vuelta en una intersección"); // Informa del error.
            }
            float desplazamientoX = -entraZ * DESPLAZAMIENTO_CARRIL; // La derecha de (dx, dz) es (-dz, dx): componente X de la entrada.
            float desplazamientoZ = entraX * DESPLAZAMIENTO_CARRIL; // Componente Z de la entrada.
            if (entraX != saleX || entraZ != saleZ) { // Si hay giro, se suma también el carril de la calle de salida.
                desplazamientoX += -saleZ * DESPLAZAMIENTO_CARRIL; // Componente X de la salida.
                desplazamientoZ += saleX * DESPLAZAMIENTO_CARRIL; // Componente Z de la salida.
            }
            puntos[i][0] = clase1.centro(actual[1]) + desplazamientoX; // Centro X de la intersección más el desplazamiento de carril.
            puntos[i][1] = clase1.centro(actual[0]) + desplazamientoZ; // Centro Z de la intersección más el desplazamiento de carril.
        }
        return puntos; // Devuelve los puntos de la ruta cerrada.
    }
}
