package tango.model;

/**
 * Restricoes de aresta entre celulas adjacentes do Tango.
 *
 * <p>Sao guardadas em duas matrizes, permitindo validacao em O(1) por aresta
 * (evita usar {@code Map}):
 * <ul>
 *   <li>{@code h} de tamanho {@code N x (N-1)} - restricao entre {@code (i,j)} e {@code (i,j+1)};</li>
 *   <li>{@code v} de tamanho {@code (N-1) x N} - restricao entre {@code (i,j)} e {@code (i+1,j)}.</li>
 * </ul>
 *
 * <p>Cada posicao assume um de tres valores:
 * <ul>
 *   <li>{@link #NONE} ({@code 0}) - sem restricao;</li>
 *   <li>{@link #EQUAL} ({@code 1}) - simbolos iguais ({@code =});</li>
 *   <li>{@link #OPPOSITE} ({@code 2}) - simbolos opostos ({@code x}).</li>
 * </ul>
 */
public final class EdgeConstraints {

    public static final int NONE = 0;
    public static final int EQUAL = 1;
    public static final int OPPOSITE = 2;

    private final int n;
    private final int[][] h; // N x (N-1)
    private final int[][] v; // (N-1) x N

    /**
     * Cria as matrizes de restricao vazias (todas {@link #NONE}) para um tabuleiro {@code N x N}.
     *
     * @param n dimensao do tabuleiro
     */
    public EdgeConstraints(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("N deve ser positivo: " + n);
        }
        this.n = n;
        this.h = new int[n][n - 1];
        this.v = new int[n - 1][n];
    }

    /** @return a dimensao N do tabuleiro associado. */
    public int getN() {
        return n;
    }

    /**
     * @return restricao horizontal entre {@code (i,j)} e {@code (i,j+1)}
     *         ({@link #NONE}/{@link #EQUAL}/{@link #OPPOSITE})
     */
    public int getH(int i, int j) {
        return h[i][j];
    }

    /** Define a restricao horizontal entre {@code (i,j)} e {@code (i,j+1)}. */
    public void setH(int i, int j, int value) {
        checkValue(value);
        h[i][j] = value;
    }

    /**
     * @return restricao vertical entre {@code (i,j)} e {@code (i+1,j)}
     *         ({@link #NONE}/{@link #EQUAL}/{@link #OPPOSITE})
     */
    public int getV(int i, int j) {
        return v[i][j];
    }

    /** Define a restricao vertical entre {@code (i,j)} e {@code (i+1,j)}. */
    public void setV(int i, int j, int value) {
        checkValue(value);
        v[i][j] = value;
    }

    private static void checkValue(int value) {
        if (value < NONE || value > OPPOSITE) {
            throw new IllegalArgumentException("restricao invalida: " + value);
        }
    }
}
