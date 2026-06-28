package tango.solver;

import tango.model.Board;
import tango.model.EdgeConstraints;
import tango.validation.RuleValidator;

/**
 * Resolvedor por Backtracking (busca com poda).
 *
 * <p>Percorre as celulas em ordem <strong>row-major</strong> (linha a linha, da
 * esquerda para a direita) e, em cada celula vazia, tenta Sol e depois Lua. A poda
 * vem do {@link RuleValidator#isPartialValid}, o ramo e cortado assim que a jogada
 * viola qualquer regra, sem esperar o tabuleiro ficar completo.
 *
 * <p>A ordem row-major e proposital: o {@code isPartialValid} foi escrito assumindo
 * que as celulas a direita e abaixo ainda estao vazias no momento da jogada.
 *
 * <p>A mutacao e feita <strong>in-place</strong> no proprio {@link Board}, com desfazer no backtrack.
 *
 * <p>As regras nunca sao reimplementadas aqui: o solver delega 100% ao validador,
 * mantendo-o como unica fonte de verdade.
 */
public final class BacktrackingSolver {

    private final RuleValidator validator = new RuleValidator();

    /**
     * Nos explorados: cada <em>tentativa</em> de colocar um simbolo em uma celula
     * vazia conta como um no. Serve de metrica do esforco da busca (poda) para a
     * analise de complexidade.
     */
    private long nodesExplored;

    /** Ordem fixa de tentativa: primeiro Sol, depois Lua. */
    private static final int[] SYMBOLS = {Board.SUN, Board.MOON};

    /**
     * Resolve o tabuleiro in-place.
     *
     * @param board tabuleiro inicial (com dicas); fica na configuracao resolvida em caso de sucesso
     * @param c     restricoes de aresta
     * @return {@code true} se encontrou solucao; {@code false} se o espaco se esgotou sem solucao
     */
    public boolean solve(Board board, EdgeConstraints c) {
        nodesExplored = 0;
        return backtrack(board, c, 0);
    }

    /** @return numero de nos (tentativas) explorados na ultima chamada a {@link #solve}. */
    public long getNodesExplored() {
        return nodesExplored;
    }

    /**
     * Recursao por indice {@code 0..N*N-1} (row-major).
     *
     * @param index posicao linear atual; {@code row = index / N}, {@code col = index % N}
     */
    private boolean backtrack(Board board, EdgeConstraints c, int index) {
        int n = board.getN();

        // Condicao de parada: passou da ultima celula -> tabuleiro completo e
        // valido por construcao (cada jogada passou pelo isPartialValid).
        if (index == n * n) {
            return true;
        }

        int row = index / n;
        int col = index % n;

        // Celula ja preenchida (dica): apenas avanca, sem testar nem contar.
        if (!board.isEmpty(row, col)) {
            return backtrack(board, c, index + 1);
        }

        // Celula vazia: tenta Sol e depois Lua.
        for (int symbol : SYMBOLS) {
            nodesExplored++;
            board.set(row, col, symbol);

            if (validator.isPartialValid(board, c, row, col)) {
                if (backtrack(board, c, index + 1)) {
                    return true;
                }
            }

            board.set(row, col, Board.EMPTY);
        }
        return false;
    }
}
