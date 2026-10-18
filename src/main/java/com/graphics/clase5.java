package com.graphics; // Mejoras opcionales, en el mismo paquete que las etapas anteriores.

import java.util.ArrayList; // Lista del tráfico autónomo.
import java.util.List; // Tipo de la lista.
import static org.lwjgl.glfw.GLFW.*; // Teclas H y P.
import static org.lwjgl.opengl.GL33.*; // Transparencia y profundidad para la interfaz 2D.

/**
 * CLASE 5: MEJORAS OPCIONALES (bonificación).
 * 1) Tráfico autónomo: cuatro vehículos que recorren rutas cerradas por las calles.
 * 2) Interfaz superpuesta: panel con velocidad, luces, entregas y sector; ayuda (H) y pausa (P).
 * Hereda clase4, así que clase4 sigue siendo una versión completa y segura de los requisitos obligatorios.
 */
public class clase5 extends clase4 {

    // ==================== 1. ESTADO ====================
    private boolean mostrarAyuda = true; // La ayuda de controles aparece al iniciar y se oculta con H.
    private boolean pausa = false; // P congela el movimiento, el tráfico y los semáforos.
    protected final List<VehiculoAutonomo> trafico = crearTrafico(); // Vehículos autónomos (se crean sin OpenGL).

    /** Define las rutas: cada una es una lista de intersecciones {fila, columna} que forma un circuito cerrado. */
    private static List<VehiculoAutonomo> crearTrafico() {
        List<VehiculoAutonomo> lista = new ArrayList<>(); // Acumula los vehículos.
        lista.add(new VehiculoAutonomo(VehiculoAutonomo.rutaPorCarril(new int[][] {{0, 0}, {0, 10}, {10, 10}, {10, 0}}), 0, 0.15f, 0.35f, 0.85f)); // Azul: perímetro exterior en sentido horario.
        lista.add(new VehiculoAutonomo(VehiculoAutonomo.rutaPorCarril(new int[][] {{2, 2}, {2, 8}, {8, 8}, {8, 2}}), 1, 0.95f, 0.80f, 0.15f)); // Amarillo: circuito interior horario.
        lista.add(new VehiculoAutonomo(VehiculoAutonomo.rutaPorCarril(new int[][] {{0, 6}, {6, 6}, {6, 10}, {0, 10}}), 0, 0.90f, 0.90f, 0.92f)); // Blanco: distrito este, antihorario.
        lista.add(new VehiculoAutonomo(VehiculoAutonomo.rutaPorCarril(new int[][] {{6, 0}, {10, 0}, {10, 6}, {6, 6}}), 2, 0.20f, 0.75f, 0.35f)); // Verde: distrito sur, antihorario.
        return lista; // Devuelve los cuatro vehículos.
    }

    // ==================== 2. CONTROLES ====================

    @Override // Añade ayuda y pausa a las teclas existentes.
    protected void tecla(int key) {
        super.tecla(key); // Conserva todos los controles anteriores.
        if (key == GLFW_KEY_H) { // Tecla de ayuda.
            mostrarAyuda = !mostrarAyuda; // Muestra u oculta el panel de controles.
        }
        if (key == GLFW_KEY_P) { // Tecla de pausa.
            pausa = !pausa; // Alterna la pausa.
        }
    }

    @Override // El reinicio también devuelve el tráfico a sus puntos de partida.
    protected void reiniciar() {
        super.reiniciar(); // Auto, velocidad, entregas y cronómetro.
        for (VehiculoAutonomo v : trafico) { // Recorre el tráfico.
            v.reiniciar(); // Cada vehículo vuelve a su punto inicial.
        }
    }

    @Override // Añade sector y pausa al título.
    protected String estadoExtra() {
        String texto = super.estadoExtra() + " | " + sectorEn(autoX, autoZ); // Sector donde está el auto.
        return pausa ? texto + " | PAUSA" : texto; // Avisa si está en pausa.
    }

    @Override // Añade H y P a la lista de controles del título.
    protected String controles() {
        return super.controles() + ", H ayuda, P pausa"; // Conserva los anteriores.
    }

    // ==================== 3. TRÁFICO AUTÓNOMO ====================

    @Override // Con la pausa, el tiempo entre cuadros pasa a ser cero y todo se congela.
    protected void actualizar(float deltaTime) {
        float dt = pausa ? 0 : deltaTime; // La pausa anula el avance del tiempo.
        super.actualizar(dt); // Jugador, entregas y reloj de animación.
        for (VehiculoAutonomo v : trafico) { // Recorre el tráfico.
            v.actualizar(dt, trafico, autoX, autoZ); // Cada vehículo avanza según el tiempo entre cuadros.
        }
    }

    @Override // El jugador no puede atravesar a los vehículos autónomos.
    protected boolean chocaConVehiculos(float x, float z) {
        for (VehiculoAutonomo v : trafico) { // Revisa cada vehículo.
            float nuevaX = x - v.x; // Separación en X si el auto se moviera a (x, z).
            float nuevaZ = z - v.z; // Separación en Z si se moviera.
            float actualX = autoX - v.x; // Separación actual en X.
            float actualZ = autoZ - v.z; // Separación actual en Z.
            boolean choca = nuevaX * nuevaX + nuevaZ * nuevaZ < 3 * 3; // Círculos de radio 1,5 que se solapan.
            boolean seAcerca = nuevaX * nuevaX + nuevaZ * nuevaZ < actualX * actualX + actualZ * actualZ; // Solo bloquea si se acerca: permite separarse.
            if (choca && seAcerca) { // Impacto inminente.
                return true; // Rechaza el movimiento.
            }
        }
        return false; // Ningún vehículo estorba.
    }

    @Override // Dibuja el tráfico en la escena principal y en el minimapa.
    protected void escena() {
        super.escena(); // Ciudad, jugador, farolas y decoración.
        for (VehiculoAutonomo v : trafico) { // Recorre el tráfico.
            dibujarVehiculo(v.x, v.z, v.angulo, v.rojo, v.verde, v.azul, noche); // Mismo modelo que el jugador, con faros visibles de noche.
            if (vistaMapa) { // En el minimapa un coche real mide pocos píxeles: se añade una marca del color del vehículo.
                caja(v.x, 24, v.z, 3.4f, 0.1f, 3.4f, v.rojo, v.verde, v.azul); // Marca cuadrada por encima de los edificios.
            }
        }
    }

    // ==================== 4. INTERFAZ 2D SUPERPUESTA ====================

    @Override // Después de la escena y el minimapa dibuja la interfaz.
    protected void dibujarFrame() {
        super.dibujarFrame(); // Escena 3D y minimapa.
        glDisable(GL_DEPTH_TEST); // La interfaz se pinta encima de todo, en orden de dibujo.
        glEnable(GL_BLEND); // Activa la transparencia de los paneles.
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // Mezcla clásica: color * alfa + fondo * (1 - alfa).
        entero("uMapa", 2); // Modo 2 del shader: coordenadas en píxeles.
        vector2("uPantalla", ancho, alto); // El shader necesita el tamaño de la ventana para convertir píxeles.
        try { // Restaura el estado aunque falle algo.
            dibujarInterfaz(); // Paneles y textos.
        } finally {
            entero("uMapa", 0); // Vuelve a la perspectiva.
            glDisable(GL_BLEND); // Desactiva la transparencia.
            glEnable(GL_DEPTH_TEST); // Reactiva la profundidad para el siguiente cuadro.
        }
        if (glGetError() != GL_NO_ERROR) { // Comprueba errores OpenGL.
            throw new IllegalStateException("Error OpenGL en la interfaz"); // Los hace visibles.
        }
    }

    /** Rectángulo en píxeles con la esquina superior izquierda en (x, y). */
    private void rect(float x, float y, float w, float h, float r, float g, float b, float alfa) {
        decimal("uAlfa", alfa); // Opacidad del rectángulo.
        caja(x + w / 2, y + h / 2, 0, w, h, 1, r, g, b); // caja() usa el centro; el shader 2D ignora Z.
    }

    /** Texto con la fuente de píxeles; unidad = tamaño en píxeles de cada casilla. */
    private void texto(String t, float x, float y, float unidad, float r, float g, float b) {
        decimal("uAlfa", 1); // Texto opaco.
        float cursor = x; // Posición horizontal del siguiente carácter.
        for (char c : t.toCharArray()) { // Recorre los caracteres.
            String[] filas = FuentePixeles.glifo(c); // Siete filas del glifo.
            for (int f = 0; f < FuentePixeles.ALTO; f++) { // Recorre cada fila del glifo.
                int col = 0; // Casilla actual.
                while (col < FuentePixeles.ANCHO) { // Recorre las casillas de la fila.
                    if (filas[f].charAt(col) != '#') { // Casilla apagada.
                        col++; // Pasa a la siguiente.
                        continue; // Sigue buscando.
                    }
                    int inicio = col; // Comienza una racha de casillas encendidas.
                    while (col < FuentePixeles.ANCHO && filas[f].charAt(col) == '#') { // Extiende la racha.
                        col++; // Avanza mientras haya píxeles encendidos.
                    }
                    float largo = (col - inicio) * unidad; // Una sola caja por racha: menos llamadas de dibujo.
                    caja(cursor + inicio * unidad + largo / 2, y + (f + 0.5f) * unidad, 0, largo, unidad, 1, r, g, b); // Dibuja la racha.
                }
            }
            cursor += FuentePixeles.AVANCE * unidad; // Avanza al siguiente carácter.
        }
    }

    /** Compone panel de estado, ayuda, pausa, mensaje final y rótulo del norte. */
    private void dibujarInterfaz() {
        float u = Math.max(1, Math.min(3, Math.min(ancho / 300, alto / 200))); // Tamaño de casilla según la ventana (1 a 3 px).
        float paso = 9 * u; // Alto de una línea de texto.
        float margen = 14 * u / 2; // Separación respecto a los bordes.

        // ---- Panel de estado (abajo a la izquierda) ----
        float panelW = FuentePixeles.ancho("SECTOR: DISTRITO ESTE", u) + 8 * u; // Ancho fijado por la línea más larga.
        float panelH = paso * 6.6f + 4 * u; // Alto para seis líneas.
        float px = margen; // Esquina izquierda del panel.
        float py = alto - panelH - margen; // Esquina superior del panel.
        rect(px, py, panelW, panelH, 0.04f, 0.06f, 0.10f, 0.72f); // Fondo translúcido.
        float tx = px + 4 * u; // Inicio del texto.
        float ty = py + 3 * u; // Primera línea.
        texto("SECTOR: " + sectorEn(autoX, autoZ), tx, ty, u, 0.75f, 0.9f, 1f); // Nombre del sector actual.
        ty += paso; // Siguiente línea.
        int kmh = Math.round(Math.abs(velocidad) * 3.6f); // Velocidad en km/h.
        texto(String.valueOf(kmh), tx, ty, u * 2, 1f, 1f, 1f); // Cifras grandes.
        texto("KM/H", tx + FuentePixeles.ancho(String.valueOf(kmh), u * 2) + 4 * u, ty + 7 * u, u, 0.7f, 0.7f, 0.7f); // Unidad.
        ty += paso * 1.7f; // Deja sitio a las cifras grandes.
        float barraW = panelW - 8 * u; // Ancho total de la barra.
        rect(tx, ty, barraW, 3 * u, 0.2f, 0.2f, 0.25f, 1); // Fondo de la barra.
        float fraccion = Math.min(1, Math.abs(velocidad) / 16f); // 16 es la velocidad máxima.
        rect(tx, ty, barraW * fraccion, 3 * u, 0.3f + 0.7f * fraccion, 0.9f - 0.5f * fraccion, 0.25f, 1); // Verde a rojo según la velocidad.
        ty += paso * 0.7f; // Siguiente línea.
        rect(tx, ty + u, 5 * u, 5 * u, faros ? 1f : 0.3f, faros ? 0.85f : 0.3f, faros ? 0.3f : 0.3f, 1); // Indicador amarillo si los faros están encendidos.
        texto("FAROS " + (faros ? "ON" : "OFF") + (noche ? "  NOCHE" : "  DIA"), tx + 7 * u, ty, u, 1f, 1f, 1f); // Estado de luces y ambiente.
        ty += paso; // Siguiente línea.
        String entrega = entregas >= DESTINOS.length ? "ENTREGAS COMPLETAS" : "ENTREGA " + (entregas + 1) + "/" + DESTINOS.length; // Progreso.
        texto(entrega, tx, ty, u, 1f, 0.8f, 0.2f); // Dorado, como el destino.
        ty += paso; // Siguiente línea.
        texto("TIEMPO " + (int) tiempo + " S", tx, ty, u, 0.85f, 0.85f, 0.85f); // Cronómetro de la partida.

        // ---- Rótulo del norte sobre el minimapa ----
        if (mapaVisible()) { // Solo si el minimapa está visible.
            int[] m = rectanguloMapa(); // Recuadro del minimapa (Y desde abajo).
            float mx = m[0] + 3 * u; // Esquina interior izquierda, pegada al borde para no tapar manzanas.
            float my = alto - m[1] - m[2] + 3 * u; // Esquina interior superior (Y medida desde arriba).
            rect(mx - u, my - u, 13 * u, 9 * u, 0.06f, 0.10f, 0.15f, 1); // Fondo del mismo color que el mapa: la etiqueta siempre se lee.
            rect(mx + 2 * u, my, u, 4 * u, 1, 1, 1, 1); // Asta de la flecha.
            rect(mx + u, my + u, 3 * u, u, 1, 1, 1, 1); // Punta de la flecha.
            texto("N", mx + 6 * u, my, u, 1, 1, 1); // Letra N: el norte está arriba.
        }

        // ---- Mensaje de fin de partida ----
        if (entregas >= DESTINOS.length) { // Todas las entregas hechas.
            String m1 = "ENTREGAS COMPLETAS EN " + (int) tiempo + " S"; // Resumen.
            String m2 = "R: JUGAR OTRA VEZ"; // Instrucción.
            float libre = (mapaVisible() ? rectanguloMapa()[0] : ancho) - 2 * margen; // Ancho libre: el mensaje nunca debe tapar el minimapa.
            float e = FuentePixeles.ancho(m1, u * 2) <= libre ? u * 2 : u; // Usa letra grande solo si cabe.
            float w = FuentePixeles.ancho(m1, e); // Ancho real del mensaje.
            float bx = margen + (libre - w) / 2; // Centrado en el espacio libre.
            float by = alto * 0.30f; // Bajo la esquina del minimapa y por encima del auto.
            rect(bx - 4 * u, by - 3 * u, w + 8 * u, 9 * e * 2 + 4 * u, 0.04f, 0.06f, 0.10f, 0.8f); // Fondo.
            texto(m1, bx, by, e, 0.4f, 1f, 0.5f); // Título verde.
            texto(m2, bx + (w - FuentePixeles.ancho(m2, e)) / 2, by + 9 * e, e, 1f, 1f, 1f); // Instrucción de reinicio.
        }

        // ---- Ayuda de controles (arriba a la izquierda) ----
        if (mostrarAyuda && entregas < DESTINOS.length) { // Visible al inicio; H la oculta y desaparece sola al terminar.
            String[] lineas = {"CONTROLES", "W/S O FLECHAS: ACELERAR/FRENAR", "A/D O FLECHAS: GIRAR", "ESPACIO: FRENO",
                "C: CAMARA   N: DIA/NOCHE", "F: FAROS    M: MINIMAPA", "R: REINICIAR  P: PAUSA", "H: OCULTAR AYUDA  ESC: SALIR"}; // Texto de ayuda.
            float ancho1 = FuentePixeles.ancho("W/S O FLECHAS: ACELERAR/FRENAR", u) + 8 * u; // Ancho del panel.
            rect(margen, margen, ancho1, paso * lineas.length + 4 * u, 0.04f, 0.06f, 0.10f, 0.72f); // Fondo translúcido.
            for (int i = 0; i < lineas.length; i++) { // Escribe cada línea.
                texto(lineas[i], margen + 4 * u, margen + 3 * u + i * paso, u, i == 0 ? 1f : 0.9f, i == 0 ? 0.85f : 0.9f, i == 0 ? 0.3f : 0.9f); // La cabecera va en dorado.
            }
        }

        // ---- Pausa ----
        if (pausa) { // Oscurece la pantalla y muestra el rótulo.
            rect(0, 0, ancho, alto, 0, 0, 0, 0.35f); // Velo oscuro.
            float w = FuentePixeles.ancho("PAUSA", u * 4); // Ancho del rótulo.
            texto("PAUSA", ancho / 2f - w / 2, alto / 2f - 14 * u, u * 4, 1, 1, 1); // Centrado.
        }
    }

    /** Punto de entrada de la versión final con las mejoras opcionales. */
    public static void main(String[] args) {
        clase5 aplicacion = new clase5(); // Crea la versión completa.
        aplicacion.run(); // Inicia la aplicación.
    }
}
