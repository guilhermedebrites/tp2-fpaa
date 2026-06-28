package tango.model;

/**
 * Tabuleiro do Tango.
 *
 * <p>Modela uma grade {@code N x N} onde cada celula assume um de tres valores:
 * <ul>
 *   <li>{@link #EMPTY} ({@code 0}) — celula vazia;</li>
 *   <li>{@link #SUN} ({@code 1}) — Sol;</li>
 *   <li>{@link #MOON} ({@code 2}) — Lua.</li>
 * </ul>
 *
 * <p>Esta classe guarda apenas o estado e utilitarios de manipulacao. As regras
 * do jogo ficam fora dela (no validador).
 */
public final class Board {

    public static final int EMPTY = 0;
    public static final int SUN = 1;
    public static final int MOON = 2;

    private final int n;
    private final int[][] grid;

    /**
     * Cria um tabuleiro {@code n x n} totalmente vazio.
     *
     * @param n dimensao do tabuleiro (deve ser positivo e par)
     */
    public Board(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("N deve ser positivo: " + n);
        }
        this.n = n;
        this.grid = new int[n][n];
    }

    /**
     * Cria um tabuleiro a partir de uma matriz existente.
     *
     * <p>A matriz e copiada (defensivamente); alteracoes externas posteriores nao
     * afetam o tabuleiro.
     *
     * @param grid matriz {@code N x N} com valores em {0,1,2}
     */
    public Board(int[][] grid) {
        if (grid == null || grid.length == 0) {
            throw new IllegalArgumentException("grid nao pode ser vazio");
        }
        this.n = grid.length;
        this.grid = new int[n][n];
        for (int i = 0; i < n; i++) {
            if (grid[i].length != n) {
                throw new IllegalArgumentException(
                        "grid deve ser quadrada N x N; linha " + i + " tem " + grid[i].length + " colunas");
            }
            for (int j = 0; j < n; j++) {
                int v = grid[i][j];
                if (v < EMPTY || v > MOON) {
                    throw new IllegalArgumentException(
                            "valor invalido em (" + i + "," + j + "): " + v);
                }
                this.grid[i][j] = v;
            }
        }
    }

    /** @return a dimensao N do tabuleiro. */
    public int getN() {
        return n;
    }

    /**
     * @return o valor da celula {@code (i,j)} — um de {@link #EMPTY}/{@link #SUN}/{@link #MOON}
     */
    public int get(int i, int j) {
        return grid[i][j];
    }

    /**
     * Define o valor da celula {@code (i,j)}.
     *
     * @param value um de {@link #EMPTY}/{@link #SUN}/{@link #MOON}
     */
    public void set(int i, int j, int value) {
        if (value < EMPTY || value > MOON) {
            throw new IllegalArgumentException("valor invalido: " + value);
        }
        grid[i][j] = value;
    }

    /** @return {@code true} se a celula {@code (i,j)} esta vazia. */
    public boolean isEmpty(int i, int j) {
        return grid[i][j] == EMPTY;
    }

    /** @return {@code true} se nenhuma celula esta vazia (regra do preenchimento completo). */
    public boolean isComplete() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /** @return uma copia independente deste tabuleiro (deep copy). */
    public Board copy() {
        return new Board(grid);
    }
}
