package com.graphics; // Mantiene esta lección junto a las clases anteriores.

import java.util.ArrayList; // Lista dinámica para reunir las farolas mientras se recorre el mapa.
import java.util.List; // Tipo de la lista de farolas.
import static org.lwjgl.glfw.GLFW.*; // Incluye las constantes de las teclas N y F.

/**
 * CLASE 3: ILUMINACIÓN DE LA CIUDAD.
 * Conserva ciudad y conducción; añade sol, farolas y focos del vehículo.
 * El shader calcula iluminación local: este ejemplo todavía no proyecta sombras.
 * Cambio de la ampliación: las farolas ya no son 9 posiciones escritas a mano; se generan a partir de MAPA.
 */
public class clase3 extends clase2 {

    // ==================== 1. ESTADO Y POSICIONES DE LAS LUCES ====================
    protected boolean noche = true; // Inicia la escena con iluminación nocturna.
    protected boolean faros = true; // Inicia los focos del auto encendidos.
    protected static final int MAX_LUCES = 48; // Tamaño del arreglo GLSL: capacidad máxima de farolas que el shader puede recibir.
    protected static final float[][] LUCES = generarLuces(); // Cada fila contiene X, Y, Z de una bombilla; se calcula desde MAPA.
    private static final float[] LUCES_PLANAS = aplanar(LUCES); // Mismo dato como x,y,z,x,y,z... para enviarlo al shader en una sola llamada.

    /** Coloca una farola en la esquina noroeste de cada manzana y otra en la sureste de las manzanas del borde. */
    private static float[][] generarLuces() {
        List<float[]> lista = new ArrayList<>(); // Acumula las posiciones encontradas.
        for (int fila = 0; fila < FILAS; fila++) { // Recorre las filas del mapa.
            for (int columna = 0; columna < COLUMNAS; columna++) { // Recorre las columnas de cada fila.
                if (MAPA[fila][columna] == CALLE) { // Las farolas se colocan junto a las manzanas, no sobre la calzada.
                    continue; // Omite las celdas de calle.
                }
                float x = centro(columna); // Centro X de la manzana.
                float z = centro(fila); // Centro Z de la manzana.
                lista.add(new float[] {x - 4, 4.5f, z - 4}); // Esquina noroeste de la acera: ilumina la calle norte y la oeste (posición original).
                boolean tocaBordeSur = fila == FILAS - 2; // La última fila de manzanas da a la calle del borde sur.
                boolean tocaBordeEste = columna == COLUMNAS - 2; // La última columna de manzanas da a la calle del borde este.
                if (tocaBordeSur || tocaBordeEste) { // Las calles del borde sur y este no tienen manzanas al otro lado.
                    lista.add(new float[] {x + 4, 4.5f, z + 4}); // Esquina sureste: evita que esas calles queden a oscuras.
                }
            }
        }
        return lista.toArray(new float[0][]); // Convierte la lista en la matriz que usa el resto del programa.
    }

    /** Convierte una matriz de posiciones en un arreglo plano x, y, z, x, y, z... */
    private static float[] aplanar(float[][] posiciones) {
        float[] plano = new float[posiciones.length * 3]; // Reserva tres números por farola.
        for (int i = 0; i < posiciones.length; i++) { // Recorre cada farola.
            plano[i * 3] = posiciones[i][0]; // Copia X.
            plano[i * 3 + 1] = posiciones[i][1]; // Copia Y.
            plano[i * 3 + 2] = posiciones[i][2]; // Copia Z.
        }
        return plano; // Devuelve el arreglo listo para glUniform3fv.
    }

    // ==================== 2. CONTROLES E INDICADORES ====================

    /** Añade los interruptores de iluminación a los controles existentes. */
    @Override // Amplía el método de teclado de clase2.
    protected void tecla(int key) {
        super.tecla(key); // Conserva ESC, C y R.
        if (key == GLFW_KEY_N) { // Comprueba la tecla de cambio de ambiente.
            noche = !noche; // Alterna entre noche y día.
        }
        if (key == GLFW_KEY_F) { // Comprueba el interruptor de los focos del auto.
            faros = !faros; // Enciende los faros si estaban apagados y viceversa.
        }
    }

    /** Prepara el texto que clase2 incorpora al título de la ventana. */
    @Override // Añade información al espacio reservado para indicadores.
    protected String estadoExtra() {
        String ambiente = "Dia"; // Usa día como texto inicial.
        if (noche) { // Comprueba si está seleccionado el ambiente nocturno.
            ambiente = "Noche"; // Reemplaza el texto por el estado real.
        }
        String estadoFaros = "OFF"; // Usa apagado como texto inicial de los focos.
        if (faros) { // Comprueba si las luces del auto están activas.
            estadoFaros = "ON"; // Indica que los faros están encendidos.
        }
        return " | " + ambiente + " | Faros " + estadoFaros; // Devuelve ambos indicadores.
    }

    @Override // Añade las teclas de iluminación a la lista de controles del título.
    protected String controles() {
        return super.controles() + ", N dia/noche, F faros"; // Conserva los controles anteriores.
    }

    @Override // Hace que los faros del modelo del auto se vean encendidos cuando el interruptor F está activo.
    protected boolean farosEncendidos() {
        return faros; // Emisión visual del modelo: no confundir con la luz que proyectan (eso lo hace el shader).
    }

    @Override // Distingue el día de la noche también en el fondo, no solo en las superficies.
    protected float[] colorCielo() {
        if (noche) { // Cielo nocturno.
            return new float[] {0.02f, 0.04f, 0.09f}; // Azul casi negro.
        }
        return new float[] {0.53f, 0.74f, 0.93f}; // Cielo diurno azul claro.
    }

    // ==================== 3. DATOS QUE RECIBE EL SHADER ====================

    /** Actualiza los uniforms de iluminación antes de dibujar la ciudad. */
    @Override // Sustituye el método vacío de clase1.
    protected void prepararLuces() {
        entero("uNoche", noche ? 1 : 0); // Comunica al shader si debe usar iluminación nocturna.
        entero("uFaros", faros ? 1 : 0); // Activa o desactiva el cálculo de los dos focos en el shader.
        entero("uEmision", 0); // Hace que los objetos normales reciban iluminación.
        entero("uNumLuces", LUCES.length); // Indica cuántas posiciones del arreglo son farolas reales.
        vectores("uLuces", LUCES_PLANAS); // Envía todas las posiciones de las farolas en una única llamada.

        float frenteX = -(float) Math.sin(angulo); // Calcula hacia dónde apunta el auto en X.
        float frenteZ = -(float) Math.cos(angulo); // Calcula hacia dónde apunta el auto en Z.
        vector("uAuto", autoX, 0.7f, autoZ); // Envía el centro del vehículo a la altura de sus focos.
        vector("uFrente", frenteX, 0, frenteZ); // Envía la dirección frontal del vehículo.
    }

    // ==================== 4. MODELOS DE LAS FAROLAS ====================

    /** Dibuja postes y bombillas en las mismas posiciones usadas para calcular la luz. */
    @Override // Añade geometría a la ciudad y al auto heredados.
    protected void escena() {
        super.escena(); // Dibuja la ciudad y el vehículo de las primeras dos lecciones.
        for (float[] luz : LUCES) { // Selecciona una farola a la vez.
            float x = luz[0]; // Lee la posición horizontal del poste.
            float y = luz[1]; // Lee la altura de la bombilla.
            float z = luz[2]; // Lee la posición del poste en profundidad.
            caja(x, 2.2f, z, 0.18f, 4.4f, 0.18f, 0.20f, 0.24f, 0.28f); // Dibuja el poste delgado y oscuro.
            if (noche) { // Las bombillas aparentan estar encendidas únicamente de noche.
                entero("uEmision", 1); // Evita que la bombilla sea oscurecida por la iluminación.
            }
            caja(x, y, z, 0.7f, 0.35f, 0.7f, 1, 0.83f, 0.42f); // Dibuja la bombilla de color cálido.
            entero("uEmision", 0); // Restablece el material normal para el siguiente objeto.
        }
    }

    // ==================== 5. CÁLCULO DE LUZ EN LA GPU ====================

    /** Devuelve el programa GLSL que calcula el color iluminado de cada fragmento. */
    @Override // Reemplaza el shader de color plano de clase1.
    protected String fragmentShader() {
        return """
            #version 330 core // Selecciona la versión GLSL correspondiente a OpenGL 3.3.
            in vec3 vMundo; // Recibe la posición del fragmento en la ciudad.
            in vec3 vNormal; // Recibe la dirección perpendicular a la superficie.
            uniform vec3 uColor; // Recibe el color base de la caja.
            uniform vec3 uLuces[@MAX_LUCES@]; // Recibe las posiciones de las farolas (capacidad fija, se usan uNumLuces).
            uniform int uNumLuces; // Cantidad real de farolas enviadas desde Java.
            uniform vec3 uAuto; // Recibe la posición del auto a la altura de los faros.
            uniform vec3 uFrente; // Recibe la dirección hacia la que apunta el vehículo.
            uniform int uNoche; // Vale 1 de noche y 0 de día.
            uniform int uFaros; // Vale 1 cuando los focos están encendidos.
            uniform int uEmision; // Vale 1 si el objeto debe conservar su color sin oscurecerse.
            uniform int uMapa; // 0 escena principal, 1 minimapa, 2 interfaz 2D.
            uniform float uAlfa; // Opacidad de los elementos de la interfaz 2D.
            out vec4 color; // Entrega el color RGBA final al framebuffer.

            void main() { // Se ejecuta para cada fragmento visible de una caja.
                if (uMapa == 2) { // La interfaz 2D usa colores directos y admite transparencia.
                    color = vec4(uColor, uAlfa); // Copia el color con la opacidad pedida.
                    return; // Termina sin calcular iluminación.
                }
                if (uEmision == 1 || uMapa == 1) { // Bombillas, ventanas encendidas y minimapa usan colores directos.
                    color = vec4(uColor, 1.0); // Conserva el color base con opacidad completa.
                    return; // Termina el shader sin calcular iluminación.
                }

                vec3 normal = normalize(vNormal); // Convierte la normal interpolada en un vector unitario.
                vec3 luz = vec3(0.48); // Define la luz ambiental diurna que llega a todas las caras.
                float intensidadSol = 0.65; // Define la fuerza de la iluminación direccional diurna.
                if (uNoche == 1) { // Ajusta el ambiente si es de noche.
                    luz = vec3(0.14, 0.18, 0.27); // Usa una luz ambiental tenue y azulada, suficiente para distinguir la calle.
                    intensidadSol = 0.12; // Conserva una pequeña luz direccional nocturna (la luna).
                }
                vec3 direccionSol = normalize(vec3(0.4, 1.0, 0.3)); // Define una fuente lejana por su dirección.
                float incidenciaSol = max(dot(normal, direccionSol), 0.0); // Lambert: una cara recibe más luz si mira al sol.
                luz += vec3(intensidadSol) * incidenciaSol; // Suma la contribución direccional al ambiente.

                if (uNoche == 1) { // Calcula la iluminación de las farolas solo de noche.
                    for (int indice = 0; indice < uNumLuces; indice++) { // Acumula el aporte de cada bombilla real.
                        vec3 haciaLuz = uLuces[indice] - vMundo; // Forma el vector desde la superficie hacia la farola.
                        float distancia = length(haciaLuz); // Mide cuántas unidades separan superficie y bombilla.
                        float alcance = max(1.0 - distancia / 30.0, 0.0); // Ventana lineal: la luz llega a cero a 30 unidades y no hay saltos.
                        float difusa = max(dot(normal, normalize(haciaLuz)), 0.0); // Calcula la incidencia de la luz sobre la cara.
                        float atenuacion = 1.0 + 0.12 * distancia + 0.045 * distancia * distancia; // Reduce el alcance con la distancia.
                        vec3 colorFarola = vec3(1.0, 0.73, 0.34); // Define el tono cálido de la farola.
                        luz += colorFarola * difusa * alcance * 4.0 / atenuacion; // Suma el aporte atenuado de esta bombilla.
                    }
                }

                if (uFaros == 1) { // Calcula los conos únicamente si están encendidos.
                    for (int indice = 0; indice < 2; indice++) { // Repite el cálculo para los dos faros.
                        vec3 lateral = vec3(-uFrente.z, 0.0, uFrente.x); // Obtiene la dirección hacia el lado derecho del auto.
                        float separacion = -0.55; // Selecciona inicialmente el faro izquierdo.
                        if (indice == 1) { // Comprueba si corresponde calcular el segundo faro.
                            separacion = 0.55; // Desplaza el segundo faro al lado derecho.
                        }
                        vec3 origen = uAuto + uFrente * 1.36 + lateral * separacion; // Ubica el faro delante de la carrocería.
                        vec3 haciaSuperficie = vMundo - origen; // Forma el vector del faro al fragmento.
                        float distancia = length(haciaSuperficie); // Mide la distancia recorrida por la luz.
                        vec3 eje = normalize(uFrente + vec3(0.0, -0.10, 0.0)); // Inclina el foco ligeramente hacia el suelo.
                        float alineacion = dot(normalize(haciaSuperficie), eje); // Un valor cercano a 1 indica el centro del haz.
                        float cono = smoothstep(0.85, 0.97, alineacion); // Suaviza el borde entre el exterior y el interior del foco.
                        float difusa = max(dot(normal, -normalize(haciaSuperficie)), 0.0); // Mide cuánto mira la cara hacia el faro.
                        float atenuacion = 1.0 + 0.04 * distancia * distancia; // Disminuye la intensidad al alejarse.
                        vec3 colorFaro = vec3(1.0, 0.94, 0.72); // Define una luz frontal blanca y cálida.
                        luz += colorFaro * cono * difusa * 8.0 / atenuacion; // Añade el aporte del foco a la iluminación total.
                    }
                }

                color = vec4(uColor * luz, 1.0); // Multiplica el material por toda la luz acumulada.
            }
            """.replace("@MAX_LUCES@", String.valueOf(MAX_LUCES)); // Inserta la capacidad del arreglo en el texto GLSL.
    }

    /** Punto de entrada de la tercera lección. */
    public static void main(String[] args) {
        clase3 aplicacion = new clase3(); // Crea la etapa con iluminación.
        aplicacion.run(); // Inicia el ciclo de vida heredado de clase1.
    }
}
