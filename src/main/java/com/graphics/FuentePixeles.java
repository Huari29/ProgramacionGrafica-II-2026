package com.graphics; // Junto al resto de clases; no depende de OpenGL.

import java.util.HashMap; // Diccionario carácter -> filas del glifo.
import java.util.Map; // Tipo del diccionario.

/**
 * Fuente de píxeles 5 x 7 para la interfaz superpuesta. No hay texturas ni archivos de fuente:
 * cada letra es una tabla de 7 filas de 5 casillas ('#' = píxel encendido) que clase5 dibuja como rectángulos.
 * Solo incluye mayúsculas, dígitos y los signos que usa la interfaz (sin tildes).
 */
public final class FuentePixeles {

    public static final int ANCHO = 5; // Casillas de ancho de cada glifo.
    public static final int ALTO = 7; // Casillas de alto de cada glifo.
    public static final int AVANCE = 6; // Casillas que avanza el cursor por carácter (ancho + 1 de separación).

    private static final String[] DATOS = { // Formato: carácter; fila0; fila1; ...; fila6
        "0;.###.;#...#;#..##;#.#.#;##..#;#...#;.###.",
        "1;..#..;.##..;..#..;..#..;..#..;..#..;.###.",
        "2;.###.;#...#;....#;...#.;..#..;.#...;#####",
        "3;.###.;#...#;....#;..##.;....#;#...#;.###.",
        "4;...#.;..##.;.#.#.;#..#.;#####;...#.;...#.",
        "5;#####;#....;####.;....#;....#;#...#;.###.",
        "6;..##.;.#...;#....;####.;#...#;#...#;.###.",
        "7;#####;....#;...#.;..#..;.#...;.#...;.#...",
        "8;.###.;#...#;#...#;.###.;#...#;#...#;.###.",
        "9;.###.;#...#;#...#;.####;....#;...#.;.##..",
        "A;.###.;#...#;#...#;#####;#...#;#...#;#...#",
        "B;####.;#...#;#...#;####.;#...#;#...#;####.",
        "C;.###.;#...#;#....;#....;#....;#...#;.###.",
        "D;####.;#...#;#...#;#...#;#...#;#...#;####.",
        "E;#####;#....;#....;####.;#....;#....;#####",
        "F;#####;#....;#....;####.;#....;#....;#....",
        "G;.###.;#...#;#....;#.###;#...#;#...#;.###.",
        "H;#...#;#...#;#...#;#####;#...#;#...#;#...#",
        "I;.###.;..#..;..#..;..#..;..#..;..#..;.###.",
        "J;..###;...#.;...#.;...#.;...#.;#..#.;.##..",
        "K;#...#;#..#.;#.#..;##...;#.#..;#..#.;#...#",
        "L;#....;#....;#....;#....;#....;#....;#####",
        "M;#...#;##.##;#.#.#;#.#.#;#...#;#...#;#...#",
        "N;#...#;##..#;#.#.#;#..##;#...#;#...#;#...#",
        "O;.###.;#...#;#...#;#...#;#...#;#...#;.###.",
        "P;####.;#...#;#...#;####.;#....;#....;#....",
        "Q;.###.;#...#;#...#;#...#;#.#.#;#..#.;.##.#",
        "R;####.;#...#;#...#;####.;#.#..;#..#.;#...#",
        "S;.####;#....;#....;.###.;....#;....#;####.",
        "T;#####;..#..;..#..;..#..;..#..;..#..;..#..",
        "U;#...#;#...#;#...#;#...#;#...#;#...#;.###.",
        "V;#...#;#...#;#...#;#...#;#...#;.#.#.;..#..",
        "W;#...#;#...#;#...#;#.#.#;#.#.#;##.##;#...#",
        "X;#...#;#...#;.#.#.;..#..;.#.#.;#...#;#...#",
        "Y;#...#;#...#;.#.#.;..#..;..#..;..#..;..#..",
        "Z;#####;....#;...#.;..#..;.#...;#....;#####",
        ":;.....;..#..;..#..;.....;..#..;..#..;.....",
        "/;....#;....#;...#.;..#..;.#...;#....;#....",
        ".;.....;.....;.....;.....;.....;.....;..#..",
        "-;.....;.....;.....;#####;.....;.....;.....",
        "!;..#..;..#..;..#..;..#..;..#..;.....;..#..",
        ",;.....;.....;.....;.....;.....;..#..;.#...",
        " ;.....;.....;.....;.....;.....;.....;.....",
    };

    private static final Map<Character, String[]> GLIFOS = new HashMap<>(); // Se llena una sola vez al cargar la clase.
    private static final String[] VACIO = {".....", ".....", ".....", ".....", ".....", ".....", "....."}; // Glifo de los caracteres desconocidos.

    static { // Bloque estático: convierte cada cadena de DATOS en su arreglo de filas.
        for (String dato : DATOS) { // Recorre las definiciones.
            String[] partes = dato.split(";", -1); // Separa el carácter de sus siete filas.
            String[] glifo = new String[ALTO]; // Reserva las filas del glifo.
            System.arraycopy(partes, 1, glifo, 0, ALTO); // Copia las siete filas.
            GLIFOS.put(partes[0].charAt(0), glifo); // Asocia el carácter con su glifo.
        }
    }

    private FuentePixeles() { // Clase de utilidades: no se instancia.
    }

    /** Devuelve las siete filas del glifo de un carácter (las minúsculas se muestran como mayúsculas). */
    public static String[] glifo(char c) {
        String[] glifo = GLIFOS.get(Character.toUpperCase(c)); // Busca el carácter en mayúscula.
        return glifo == null ? VACIO : glifo; // Si no existe, devuelve un espacio en blanco.
    }

    /** Ancho en píxeles de un texto con casillas de tamaño unidad (sin la separación final). */
    public static float ancho(String texto, float unidad) {
        return Math.max(0, texto.length() * AVANCE - 1) * unidad; // Cada carácter ocupa AVANCE casillas menos la última separación.
    }
}
