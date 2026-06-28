package tango.solver;

import tango.model.Board;
import tango.model.EdgeConstraints;
import tango.validation.RuleValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolvedor por Forca Bruta (busca exaustiva).
 *
 * <p>Coleta as {@code k} celulas vazias e enumera todas as {@code 2^k} combinacoes
 * via um contador binario: para cada combinacao, o bit {@code b} decide o simbolo da
 * b-esima celula vazia (0 = Sol, 1 = Lua). Cada combinacao preenche o tabuleiro e e
 * testada com {@link RuleValidator#isFullyValid} (tabuleiro COMPLETO). A primeira que
 * passa nas 5 regras e a solucao (early-exit).
 *
 * <p>Serve para evidenciar a explosao combinatoria e contrastar com a poda do
 * Backtracking. As regras nunca sao reimplementadas aqui, toda a validacao e
 * delegada ao validador compartilhado.
 *
 * <p><strong>Guarda de seguranca:</strong> se {@code k > }{@link #MAX_EMPTY}, a
 * enumeracao nao e executada (seriam {@code 2^k} combinacoes); a estrategia e abortada
 * sinalizando {@link #wasAborted()}, sem travar o programa.
 */
public final class BruteForceSolver {

    /** Limite de celulas vazias acima do qual a forca bruta e considerada inviavel. */
    public static final int MAX_EMPTY = 24;

    private final RuleValidator validator = new RuleValidator();

    private long combinationsTested;
    private int emptyCount;
    private boolean aborted;

    /**
     * Resolve o tabuleiro por busca exaustiva.
     *
     * @param board tabuleiro inicial (com dicas); fica resolvido in-place em caso de sucesso
     * @param c     restricoes de aresta
     * @return {@code true} se achou solucao; {@code false} se esgotou o espaco OU se abortou
     *         pela guarda de seguranca (verifique {@link #wasAborted()} para distinguir)
     */
    public boolean solve(Board board, EdgeConstraints c) {
        combinationsTested = 0;
        aborted = false;

        List<int[]> empties = collectEmptyCells(board);
        emptyCount = empties.size();
        int k = emptyCount;

        // Guarda de seguranca: nao tente um espaco inviavel.
        if (k > MAX_EMPTY) {
            aborted = true;
            return false;
        }

        long total = 1L << k; // 2^k combinacoes
        for (long mask = 0; mask < total; mask++) {
            combinationsTested++;
            applyCombination(board, empties, mask);
            if (validator.isFullyValid(board, c)) {
                return true;
            }
        }

        // Esgotou sem solucao: devolve as celulas vazias ao estado original.
        clearCells(board, empties);
        return false;
    }

    /** @return numero de combinacoes testadas na ultima chamada a {@link #solve}. */
    public long getCombinationsTested() {
        return combinationsTested;
    }

    /** @return numero de celulas vazias (k) da ultima chamada. */
    public int getEmptyCount() {
        return emptyCount;
    }

    /** @return {@code true} se a ultima chamada abortou pela guarda de seguranca (k grande). */
    public boolean wasAborted() {
        return aborted;
    }

    /** Coleta as coordenadas {@code [row,col]} das celulas vazias, em ordem row-major. */
    private List<int[]> collectEmptyCells(Board board) {
        int n = board.getN();
        List<int[]> empties = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board.isEmpty(i, j)) {
                    empties.add(new int[]{i, j});
                }
            }
        }
        return empties;
    }

    /** Preenche as celulas vazias conforme os bits de {@code mask} (bit 0 = Sol, bit 1 = Lua). */
    private void applyCombination(Board board, List<int[]> empties, long mask) {
        for (int b = 0; b < empties.size(); b++) {
            int[] cell = empties.get(b);
            int symbol = ((mask >> b) & 1L) == 0 ? Board.SUN : Board.MOON;
            board.set(cell[0], cell[1], symbol);
        }
    }

    /** Restaura as celulas (originalmente vazias) para vazias. */
    private void clearCells(Board board, List<int[]> empties) {
        for (int[] cell : empties) {
            board.set(cell[0], cell[1], Board.EMPTY);
        }
    }
}
