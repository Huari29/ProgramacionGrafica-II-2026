package com.graphics; // Reúne la versión final con las otras tres lecciones.

import static org.lwjgl.glfw.GLFW.*; // Permite consultar la tecla que controla el minimapa.
import static org.lwjgl.opengl.GL33.*; // Permite cambiar viewport, recorte y buffers de dibujo.

/**
 * CLASE 4: CIUDAD TERMINADA, ENTREGAS Y MINIMAPA.
 * Orden de lectura: estado, controles, entregas, decoración y segundo pase de dibujo.
 * Hereda clase3, que hereda clase2, que a su vez hereda clase1.
 * Los semáforos son decorativos: cambian de color por tiempo pero no detienen al auto.
 * El tráfico autónomo y la interfaz superpuesta (mejoras opcionales) están en clase5.
 */
public class clase4 extends clase3 {

    // ==================== 1. ESTADO DEL JUEGO ====================
    private boolean mostrarMapa = true; // Muestra el minimapa desde el inicio.
    protected int entregas = 0; // Cuenta las entregas completadas; también identifica el siguiente destino.
    protected float tiempo = 0; // Segundos de la partida (se detiene al completar las entregas y se reinicia con R).
    protected float reloj = 0; // Segundos totales de animación: nunca se detiene, así los semáforos siguen cambiando al ganar.
    protected static final int[][] DESTINOS_CELDAS = { // Cada fila es {fila, columna} de una intersección; así siempre cae sobre una calle.
        {10, 4}, // Primera entrega: calle sur, cerca del inicio (distrito sur).
        {10, 10}, // Segunda entrega: esquina sureste (barrio nuevo).
        {6, 10}, // Tercera entrega: avenida este (distrito este).
        {0, 8}, // Cuarta entrega: calle norte del distrito este.
        {0, 0} // Quinta entrega: esquina noroeste (casco antiguo).
    };
    protected static final float[][] DESTINOS = destinosEnMundo(); // Mismas paradas convertidas a coordenadas X, Z del mundo.

    /** Convierte cada celda {fila, columna} en la posición del mundo de su centro. */
    private static float[][] destinosEnMundo() {
        float[][] mundo = new float[DESTINOS_CELDAS.length][2]; // Reserva X y Z para cada parada.
        for (int i = 0; i < DESTINOS_CELDAS.length; i++) { // Recorre las paradas.
            mundo[i][0] = centro(DESTINOS_CELDAS[i][1]); // La columna determina X.
            mundo[i][1] = centro(DESTINOS_CELDAS[i][0]); // La fila determina Z.
        }
        return mundo; // Devuelve las coordenadas listas para usar.
    }

    // ==================== 2. CONTROLES Y REINICIO ====================

    /** Añade el interruptor del minimapa a los controles anteriores. */
    @Override // Amplía las teclas definidas por clase3.
    protected void tecla(int key) {
        super.tecla(key); // Conserva salida, cámara, reinicio y luces.
        if (key == GLFW_KEY_M) { // Comprueba si se pulsó la tecla del mapa.
            mostrarMapa = !mostrarMapa; // Alterna entre mostrar y ocultar la vista superior.
        }
    }

    /** Reinicia el vehículo y el progreso de las entregas. */
    @Override // Amplía el reinicio definido en clase2.
    protected void reiniciar() {
        super.reiniciar(); // Restaura posición, velocidad y orientación del vehículo.
        entregas = 0; // Vuelve a seleccionar la primera parada.
        tiempo = 0; // Reinicia el cronómetro de la partida.
    }

    // ==================== 3. REGLAS DE LAS ENTREGAS ====================

    /** Actualiza el vehículo y comprueba si llegó y frenó en el destino activo. */
    @Override // Añade el objetivo del juego al movimiento heredado.
    protected void actualizar(float deltaTime) {
        super.actualizar(deltaTime); // Procesa aceleración, giro, colisiones y título de la ventana.
        reloj += deltaTime; // La animación avanza siempre, incluso después de ganar.
        if (entregas >= DESTINOS.length) { // Comprueba si ya se completaron todas las paradas.
            return; // Conserva el tiempo final y evita leer fuera del arreglo.
        }
        tiempo += deltaTime; // Suma los segundos de este cuadro al cronómetro.
        comprobarEntrega(); // Aplica la regla de llegada.
    }

    /** Regla de entrega, separada de OpenGL para poder probarla sin abrir una ventana. */
    protected void comprobarEntrega() {
        if (entregas >= DESTINOS.length) { // Sin paradas pendientes no hay nada que comprobar.
            return; // Evita leer fuera del arreglo.
        }
        float distanciaX = autoX - DESTINOS[entregas][0]; // Calcula la separación horizontal al destino activo.
        float distanciaZ = autoZ - DESTINOS[entregas][1]; // Calcula la separación en profundidad al destino.
        float distanciaCuadrada = distanciaX * distanciaX + distanciaZ * distanciaZ; // Mide cercanía sin calcular raíz cuadrada.
        boolean estaCerca = distanciaCuadrada < 3 * 3; // Acepta un radio de llegada de tres unidades.
        boolean estaFrenando = Math.abs(velocidad) < 1; // Exige circular a menos de una unidad por segundo.
        if (estaCerca && estaFrenando) { // Solo completa la entrega si ambas condiciones se cumplen.
            entregas++; // Selecciona la siguiente parada o completa el juego.
        }
    }

    /** Compone el progreso que se añade al título de la ventana. */
    @Override // Amplía los indicadores de día/noche y faros de clase3.
    protected String estadoExtra() {
        String mensaje = super.estadoExtra(); // Recupera los indicadores de iluminación.
        if (entregas == DESTINOS.length) { // Selecciona el texto de victoria al completar todas las paradas.
            mensaje += " | GANASTE en " + (int) tiempo + " s! R: jugar otra vez"; // Muestra tiempo final y opción de reinicio.
        } else { // Durante el recorrido muestra progreso e instrucciones.
            mensaje += " | Entregas " + entregas + "/" + DESTINOS.length; // Indica cuántas paradas se completaron.
            mensaje += " (frena en la marca dorada) | " + (int) tiempo + " s"; // Explica la condición de entrega y el tiempo.
        }
        return mensaje; // Entrega el texto a actualizarTitulo() de clase2.
    }

    @Override // Añade la tecla del minimapa a la lista de controles del título.
    protected String controles() {
        return super.controles() + ", M minimapa"; // Conserva los controles anteriores.
    }

    // ==================== 4. ESCENA FINAL Y DESTINO ====================

    /** Añade decoración y señal del destino a la escena iluminada. */
    @Override // Amplía el dibujo acumulado de las tres etapas anteriores.
    protected void escena() {
        super.escena(); // Dibuja la ciudad, el auto y las farolas.
        if (!vistaMapa) { // Los detalles pequeños solo son necesarios en la vista principal.
            decorarCiudad(); // Añade árboles, bancos, ventanas y señalización urbana.
        }
        if (entregas < DESTINOS.length) { // Dibuja un objetivo únicamente mientras queden entregas.
            dibujarDestino(); // Coloca la marca dorada en la parada activa.
        }
    }

    /** Marca la próxima parada con una plataforma y una baliza flotante. */
    private void dibujarDestino() {
        float x = DESTINOS[entregas][0]; // Lee el X de la próxima entrega.
        float z = DESTINOS[entregas][1]; // Lee el Z de la próxima entrega.
        entero("uEmision", 1); // Hace que el objetivo sea visible incluso de noche.
        float lado = 5; // Tamaño de la plataforma en la vista principal.
        if (vistaMapa) { // En el minimapa la ciudad se ve pequeña: el destino se agranda y pulsa para localizarlo rápido.
            lado = 9 + (float) Math.sin(reloj * 4) * 1.2f; // Oscila entre 7,8 y 10,2 unidades.
        }
        caja(x, 0.06f, z, lado, 0.08f, lado, 1, 0.72f, 0.12f); // Dibuja una marca dorada sobre el asfalto.
        if (!vistaMapa) { // Evita añadir una baliza tridimensional al mapa pequeño.
            float alturaBaliza = 3.5f + (float) Math.sin(reloj * 2) * 0.3f; // Hace oscilar la baliza suavemente.
            cajaGirada(x, alturaBaliza, z, 0.8f, 0.8f, 0.8f, 1, 0.8f, 0.15f, reloj); // Dibuja el cubo giratorio del objetivo.
        }
        entero("uEmision", 0); // Devuelve a los siguientes objetos su iluminación normal.
    }

    // ==================== 5. DECORACIÓN DE LAS MANZANAS ====================

    /** Decide qué decoración corresponde a cada tipo de parcela. */
    private void decorarCiudad() {
        for (int fila = 0; fila < FILAS; fila++) { // Recorre las filas del mapa.
            for (int columna = 0; columna < COLUMNAS; columna++) { // Recorre las columnas de esa fila.
                float x = centro(columna); // Obtiene el centro horizontal de la parcela.
                float z = centro(fila); // Obtiene el centro de la parcela en profundidad.
                int tipo = MAPA[fila][columna]; // Lee el contenido de la celda.
                if (tipo == PARQUE) { // Detecta una parcela de parque.
                    dibujarParque(x, z); // Añade árboles y un banco.
                }
                if (tipo == EDIFICIO) { // Detecta una parcela con edificio.
                    dibujarVentanas(x, z, alturaEdificio(fila, columna), fila, columna); // Usa la misma altura que clase1.
                }
                if (tipo != CALLE) { // La señalización se coloca junto a las manzanas, no en celdas de calle.
                    dibujarPasoPeatonal(x, z); // Añade el cruce pintado sobre la calle contigua.
                    dibujarSemaforo(x + 4, z - 4, fila, columna); // Coloca el semáforo dentro de la acera.
                }
            }
        }
    }

    /** Construye cuatro árboles y un banco utilizando cajas. */
    private void dibujarParque(float x, float z) {
        float[] posiciones = {-2.5f, 2.5f}; // Define desplazamientos respecto al centro de la parcela.
        for (float desplazamientoX : posiciones) { // Selecciona el lado izquierdo o derecho del parque.
            for (float desplazamientoZ : posiciones) { // Selecciona el lado delantero o trasero.
                float arbolX = x + desplazamientoX; // Convierte el desplazamiento local en coordenada X mundial.
                float arbolZ = z + desplazamientoZ; // Convierte el desplazamiento local en coordenada Z mundial.
                caja(arbolX, 1.2f, arbolZ, 0.35f, 2, 0.35f, 0.38f, 0.22f, 0.12f); // Dibuja el tronco marrón.
                caja(arbolX, 2.7f, arbolZ, 2, 2.3f, 2, 0.12f, 0.42f, 0.23f); // Dibuja la copa verde del árbol.
            }
        }
        caja(x, 0.65f, z, 3, 0.25f, 0.8f, 0.55f, 0.30f, 0.13f); // Dibuja el asiento de madera del banco.
        caja(x, 0.4f, z, 2, 0.6f, 0.35f, 0.22f, 0.24f, 0.24f); // Dibuja el soporte oscuro del banco.
        caja(x, 1, z + 0.35f, 3, 0.7f, 0.15f, 0.55f, 0.30f, 0.13f); // Dibuja el respaldo detrás del asiento.
    }

    /** Distribuye ventanas por pisos en las cuatro paredes; de noche unas brillan y otras quedan apagadas. */
    private void dibujarVentanas(float x, float z, float altura, int fila, int columna) {
        int piso = 0; // Cuenta los pisos para variar el patrón de ventanas encendidas.
        for (float y = 1.7f; y < altura; y += 2) { // Recorre los pisos separados por dos unidades de altura.
            int numero = 0; // Índice de la ventana dentro de la fachada.
            for (float desplazamiento = -2; desplazamiento <= 2; desplazamiento += 2) { // Coloca tres ventanas por fachada.
                for (int fachada = 0; fachada < 4; fachada++) { // Norte, sur, oeste y este.
                    int semilla = piso * 5 + numero * 3 + fachada * 7 + fila * 11 + columna * 13; // Mezcla posiciones para un patrón reproducible.
                    boolean encendida = noche && semilla % 4 != 0; // De noche, tres de cada cuatro ventanas están encendidas.
                    float r = 0.50f; // Rojo del cristal diurno.
                    float g = 0.68f; // Verde del cristal diurno.
                    float b = 0.80f; // Azul del cristal diurno.
                    if (noche) { // Cambia la paleta según el ambiente.
                        r = 0.95f; // Luz cálida de habitación encendida.
                        g = 0.75f; // Componente verde de esa luz.
                        b = 0.38f; // Componente azul de esa luz.
                        if (!encendida) { // Las ventanas apagadas se ven oscuras.
                            r = 0.08f; // Cristal oscuro.
                            g = 0.10f; // Cristal oscuro.
                            b = 0.15f; // Cristal oscuro.
                        }
                    }
                    entero("uEmision", encendida ? 1 : 0); // Solo las ventanas encendidas ignoran la iluminación (emisión visual).
                    if (fachada == 0) { // Fachada norte.
                        caja(x + desplazamiento, y, z - 3.51f, 0.8f, 0.9f, 0.04f, r, g, b);
                    } else if (fachada == 1) { // Fachada sur.
                        caja(x + desplazamiento, y, z + 3.51f, 0.8f, 0.9f, 0.04f, r, g, b);
                    } else if (fachada == 2) { // Fachada oeste.
                        caja(x - 3.51f, y, z + desplazamiento, 0.04f, 0.9f, 0.8f, r, g, b);
                    } else { // Fachada este.
                        caja(x + 3.51f, y, z + desplazamiento, 0.04f, 0.9f, 0.8f, r, g, b);
                    }
                }
                numero++; // Pasa a la siguiente ventana de la fachada.
            }
            piso++; // Pasa al siguiente piso.
        }
        entero("uEmision", 0); // Restablece la iluminación normal de los demás elementos.
    }

    /** Dibuja las franjas blancas sobre la calle contigua al norte de la manzana. */
    private void dibujarPasoPeatonal(float x, float z) {
        for (int desplazamiento = -3; desplazamiento <= 3; desplazamiento++) { // Coloca siete franjas paralelas.
            caja(x + desplazamiento, 0.045f, z - 8, 0.45f, 0.04f, 2, 0.85f, 0.87f, 0.83f); // Eleva la pintura un poco sobre el suelo.
        }
    }

    /** Construye un semáforo decorativo: rojo 5 s, verde 5 s y amarillo 2 s, con desfase según su manzana. */
    private void dibujarSemaforo(float x, float z, int fila, int columna) {
        caja(x, 1.7f, z, 0.18f, 2.8f, 0.18f, 0.18f, 0.20f, 0.22f); // Dibuja el poste sobre la acera.
        caja(x, 3.1f, z, 0.55f, 1.2f, 0.45f, 0.08f, 0.10f, 0.12f); // Dibuja la carcasa de las tres luces.
        float desfase = ((fila + columna) / 2 % 2) * 6; // Manzanas vecinas quedan a medio ciclo: no todos los semáforos cambian a la vez.
        int fase = (int) ((reloj + desfase) % 12); // Repite un ciclo de segundos comprendidos entre 0 y 11.
        for (int indice = 0; indice < 3; indice++) { // Recorre rojo arriba, amarillo al centro y verde abajo.
            boolean encendida = false; // Parte de una bombilla apagada.
            if (indice == 0) { // Selecciona la bombilla roja.
                encendida = fase < 5; // Mantiene rojo durante los primeros cinco segundos.
            } else if (indice == 1) { // Selecciona la bombilla amarilla.
                encendida = fase >= 10; // Mantiene amarillo en los últimos dos segundos del ciclo.
            } else { // Selecciona la bombilla verde.
                encendida = fase >= 5 && fase < 10; // Mantiene verde durante los cinco segundos intermedios.
            }
            float brillo = 0.15f; // Conserva un color tenue cuando la bombilla está apagada.
            entero("uEmision", 0); // Configura inicialmente una superficie sin emisión.
            if (encendida) { // Comprueba si esta bombilla corresponde a la fase activa.
                brillo = 1; // Usa intensidad completa para su color.
                entero("uEmision", 1); // Hace que la bombilla se vea encendida.
            }
            float rojo = 0; // Componente roja inicialmente ausente.
            float verde = 0; // Componente verde inicialmente ausente.
            if (indice < 2) { // Rojo y amarillo necesitan componente roja.
                rojo = brillo; // Añade rojo con la intensidad elegida.
            }
            if (indice > 0) { // Amarillo y verde necesitan componente verde.
                verde = brillo; // Añade verde; rojo más verde produce amarillo.
            }
            float altura = 3.45f - indice * 0.35f; // Separa verticalmente las tres bombillas.
            caja(x, altura, z - 0.24f, 0.28f, 0.25f, 0.06f, rojo, verde, 0.02f); // Dibuja la bombilla frente a la carcasa.
        }
        entero("uEmision", 0); // Evita que el siguiente objeto herede la emisión del semáforo.
    }

    // ==================== 6. MINIMAPA: SEGUNDO PASE DE DIBUJO ====================

    /** Indica si el minimapa está visible (clase5 lo consulta para colocar su interfaz sin solaparlo). */
    protected boolean mapaVisible() {
        return mostrarMapa; // Devuelve el estado alternado con la tecla M.
    }

    /** Calcula el recuadro del minimapa {x, y, lado} en píxeles, con Y medida desde abajo como en OpenGL. */
    protected int[] rectanguloMapa() {
        int dimensionMenor = Math.min(ancho, alto); // Busca la dimensión que limita el espacio disponible.
        int lado = Math.min(300, (int) (dimensionMenor * 0.36f)); // Limita el mapa a 300 píxeles y al 36 % de la dimensión menor.
        int margen = Math.min(18, dimensionMenor / 20); // Calcula una separación adaptable respecto a los bordes.
        int x = ancho - lado - margen; // Ubica el recuadro cerca del borde derecho.
        int y = alto - lado - margen; // Ubica el recuadro arriba; OpenGL mide Y desde abajo.
        return new int[] {x, y, lado}; // Devuelve la posición y el tamaño cuadrado.
    }

    /** Dibuja la escena principal y después la misma ciudad desde arriba, en un recuadro. */
    @Override // Amplía el cuadro completo definido en clase1.
    protected void dibujarFrame() {
        super.dibujarFrame(); // Dibuja primero la vista normal de la ciudad.
        if (!mostrarMapa) { // Comprueba si el usuario ocultó el minimapa con M.
            return; // Conserva únicamente la imagen principal.
        }
        int[] recuadro = rectanguloMapa(); // Calcula el encuadre según el tamaño actual de la ventana.
        int x = recuadro[0]; // Esquina izquierda del recuadro.
        int y = recuadro[1]; // Esquina inferior del recuadro.
        int lado = recuadro[2]; // Lado del cuadrado.
        glEnable(GL_SCISSOR_TEST); // Activa el recorte para no borrar el resto de la escena.
        glScissor(x - 3, y - 3, lado + 6, lado + 6); // Selecciona el mapa más un borde de tres píxeles.
        glClearColor(0.8f, 0.87f, 0.94f, 1); // Define un color claro para el marco.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Borra solo el recuadro exterior gracias al scissor.
        glScissor(x, y, lado, lado); // Reduce el recorte al interior del minimapa.
        glClearColor(0.06f, 0.10f, 0.15f, 1); // Define el fondo oscuro del mapa.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpia color y profundidad dentro del mapa.
        glViewport(x, y, lado, lado); // Redirige la proyección al recuadro cuadrado.
        entero("uMapa", 1); // Selecciona la proyección ortográfica del shader de clase1.
        vistaMapa = true; // Indica a escena() que omita decoración pequeña y baliza flotante.
        try { // Asegura que el estado de dibujo se restaure incluso si el segundo pase falla.
            escena(); // Dibuja otra vez la misma ciudad, ahora vista desde arriba.
            dibujarIndicadorAuto(); // Resalta la posición y el frente del jugador en el mapa.
        } finally { // El siguiente cuadro debe volver a la configuración de pantalla completa.
            vistaMapa = false; // Reactiva los detalles de la escena principal.
            entero("uMapa", 0); // Recupera la proyección en perspectiva.
            glDisable(GL_SCISSOR_TEST); // Permite que la próxima limpieza abarque toda la pantalla.
            glViewport(0, 0, ancho, alto); // Recupera el área de dibujo de la ventana completa.
        }
        if (glGetError() != GL_NO_ERROR) { // Comprueba que el pase del mapa no haya generado errores OpenGL.
            throw new IllegalStateException("Error OpenGL en minimapa"); // Expone el error en la consola.
        }
    }

    /** Dibuja una marca cian y una punta blanca por encima de los edificios del minimapa. */
    private void dibujarIndicadorAuto() {
        cajaGirada(autoX, 25, autoZ, 3.6f, 0.1f, 5.2f, 0.1f, 1, 1, angulo); // Marca la posición con un rectángulo cian orientado (más grande que antes: la ciudad ahora se ve más pequeña).
        float frenteX = -(float) Math.sin(angulo); // Calcula la dirección frontal en el eje X.
        float frenteZ = -(float) Math.cos(angulo); // Calcula la dirección frontal en el eje Z.
        float puntaX = autoX + frenteX * 3.2f; // Desplaza la punta hacia delante en X.
        float puntaZ = autoZ + frenteZ * 3.2f; // Desplaza la punta hacia delante en Z.
        caja(puntaX, 26, puntaZ, 1.6f, 0.1f, 1.6f, 1, 1, 1); // Dibuja la punta blanca encima del indicador cian: indica la orientación.
    }

    /** Punto de entrada de la versión con los requisitos obligatorios. */
    public static void main(String[] args) {
        clase4 aplicacion = new clase4(); // Crea la versión con todas las etapas acumuladas.
        aplicacion.run(); // Inicia el ciclo de vida completo de la aplicación.
    }
}
