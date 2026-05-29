package Modelo;

/**
 * Modelo del laberinto. Contiene el mapa, los pellets y la lógica de colisiones.
 */
public class Laberinto {

    public static final int TAM_CELDA = 24;
    public static final int FILAS = 21;
    public static final int COLUMNAS = 19;

    // 0 = camino con pellet, 1 = pared, 2 = camino vacío, 3 = power-up
    private int[][] mapa;
    private int totalPellets;
    private int pelletsRestantes;

    // Mapa original para reiniciar
    private static final int[][] MAPA_ORIGINAL = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
        {1,3,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,3,1},
        {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,0,1},
        {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
        {1,1,1,1,0,1,1,1,2,1,2,1,1,1,0,1,1,1,1},
        {1,1,1,1,0,1,2,2,2,2,2,2,2,1,0,1,1,1,1},
        {1,1,1,1,0,1,2,1,1,2,1,1,2,1,0,1,1,1,1},
        {2,2,2,2,0,2,2,1,2,2,2,1,2,2,0,2,2,2,2},
        {1,1,1,1,0,1,2,1,1,1,1,1,2,1,0,1,1,1,1},
        {1,1,1,1,0,1,2,2,2,2,2,2,2,1,0,1,1,1,1},
        {1,1,1,1,0,1,2,1,1,1,1,1,2,1,0,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
        {1,3,0,1,0,0,0,0,0,2,0,0,0,0,0,1,0,3,1},
        {1,1,0,1,0,1,0,1,1,1,1,1,0,1,0,1,0,1,1},
        {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
        {1,0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    public Laberinto() {
        inicializar();
    }

    public void inicializar() {
        mapa = new int[FILAS][COLUMNAS];
        totalPellets = 0;
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLUMNAS; c++) {
                mapa[f][c] = MAPA_ORIGINAL[f][c];
                if (mapa[f][c] == 0 || mapa[f][c] == 3) totalPellets++;
            }
        }
        pelletsRestantes = totalPellets;
    }

    /**
     * Verifica si la celda en (col, fila) es una pared.
     */
    public boolean esPared(int col, int fila) {
        if (col < 0 || col >= COLUMNAS || fila < 0 || fila >= FILAS) return true;
        return mapa[fila][col] == 1;
    }

    /**
     * Consume el pellet en la posición dada. Retorna puntos ganados.
     */
    public int consumirPellet(int col, int fila) {
        if (col < 0 || col >= COLUMNAS || fila < 0 || fila >= FILAS) return 0;
        int celda = mapa[fila][col];
        if (celda == 0) {
            mapa[fila][col] = 2;
            pelletsRestantes--;
            return 10;
        } else if (celda == 3) {
            mapa[fila][col] = 2;
            pelletsRestantes--;
            return 50;
        }
        return 0;
    }

    /**
     * Verifica si el pellet en la posición es un power-up.
     */
    public boolean esPowerUp(int col, int fila) {
        if (col < 0 || col >= COLUMNAS || fila < 0 || fila >= FILAS) return false;
        return mapa[fila][col] == 3;
    }

    public int[][] getMapa() { return mapa; }
    public int getPelletsRestantes() { return pelletsRestantes; }
    public int getTotalPellets() { return totalPellets; }

    public boolean todosConsumidos() {
        return pelletsRestantes <= 0;
    }
}
