package tango.io;

import tango.model.Board;
import tango.model.EdgeConstraints;

/**
 * Impressao em ASCII do tabuleiro do Tango.
 *
 * <p>Mostra as celulas (com lacunas) e as restricoes de aresta posicionadas
 * <em>entre</em> as celulas, tanto as horizontais ({@code =}/{@code x} entre
 * colunas) quanto as verticais (em linhas separadoras). Exemplo de saida:
 * <pre>
 * .   . = S   .   . x .
 *
 * .   L   .   .   .   .
 *     x
 * ...
 * </pre>
 */
public final class BoardPrinter {

    /** Largura ocupada por uma celula mais o espacador ate a proxima (passo horizontal). */
    private static final int STRIDE = 4;

    private BoardPrinter() {
    }

    /** Imprime o tabuleiro e suas restricoes em {@link System#out}. */
    public static void print(Board board, EdgeConstraints constraints) {
        System.out.print(format(board, constraints));
    }

    /**
     * Monta a representacao ASCII do tabuleiro com as restricoes.
     *
     * @return string pronta para impressao (terminada por quebra de linha)
     */
    public static String format(Board board, EdgeConstraints constraints) {
        int n = board.getN();
        if (constraints.getN() != n) {
            throw new IllegalArgumentException(
                    "dimensoes incompativeis: board N=" + n + ", constraints N=" + constraints.getN());
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(rowLine(board, constraints, i)).append('\n');
            if (i < n - 1) {
                sb.append(verticalLine(constraints, i)).append('\n');
            }
        }
        sb.append('\n');
        sb.append("Legenda: S=Sol  L=Lua  .=vazio   ==iguais  x=opostas\n");
        return sb.toString();
    }

    /** Linha com as celulas da linha {@code i} e as restricoes horizontais entre elas. */
    private static String rowLine(Board board, EdgeConstraints constraints, int i) {
        int n = board.getN();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < n; j++) {
            sb.append(cellChar(board.get(i, j)));
            if (j < n - 1) {
                sb.append(' ').append(edgeChar(constraints.getH(i, j))).append(' ');
            }
        }
        return sb.toString();
    }

    /** Linha separadora com as restricoes verticais entre a linha {@code i} e a {@code i+1}. */
    private static String verticalLine(EdgeConstraints constraints, int i) {
        int n = constraints.getN();
        char[] line = new char[STRIDE * (n - 1) + 1];
        java.util.Arrays.fill(line, ' ');
        for (int j = 0; j < n; j++) {
            line[STRIDE * j] = edgeChar(constraints.getV(i, j));
        }
        
        int end = line.length;
        while (end > 0 && line[end - 1] == ' ') {
            end--;
        }
        return new String(line, 0, end);
    }

    private static char cellChar(int value) {
        return switch (value) {
            case Board.SUN -> 'S';
            case Board.MOON -> 'L';
            default -> '.';
        };
    }

    private static char edgeChar(int constraint) {
        return switch (constraint) {
            case EdgeConstraints.EQUAL -> '=';
            case EdgeConstraints.OPPOSITE -> 'x';
            default -> ' ';
        };
    }
}
