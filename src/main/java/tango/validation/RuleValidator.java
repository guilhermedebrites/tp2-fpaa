package tango.validation;

import tango.model.Board;
import tango.model.EdgeConstraints;

/**
 * Fonte unica de verdade das 5 regras do Tango.
 *
 * <p>As duas estrategias de resolucao (Forca Bruta e Backtracking) compartilham
 * este mesmo validador.
 *
 * <p>Cada regra vive em seu proprio metodo privado (modularidade):
 * <ol>
 *   <li>Preenchimento completo : nenhuma celula vazia.</li>
 *   <li>Adjacencia : proibido 3 simbolos iguais em sequencia.</li>
 *   <li>Equilibrio : cada linha/coluna com exatamente N/2 Sois e N/2 Luas.</li>
 *   <li>Igualdade ({@code =}) : celulas ligadas por {@code =} sao iguais.</li>
 *   <li>Oposicao ({@code x}) : celulas ligadas por {@code x} sao opostas.</li>
 * </ol>
 *
 * <p>Dois pontos de entrada:
 * <ul>
 *   <li>{@link #isFullyValid} — valida um tabuleiro COMPLETO (usado pela Forca Bruta).</li>
 *   <li>{@link #isPartialValid} — valida apenas o que a ultima jogada afeta, servindo
 *       de poda no Backtracking (a regra 1 nao se aplica).</li>
 * </ul>
 *
 * <p>A classe e sem estado (stateless), portanto uma instancia pode ser
 * compartilhada com seguranca entre os solvers.
 */
public final class RuleValidator {

    /**
     * Valida um tabuleiro COMPLETO contra as 5 regras (usado pela Forca Bruta).
     *
     * @return {@code true} apenas se o tabuleiro estiver totalmente preenchido e
     *         satisfizer todas as regras
     */
    public boolean isFullyValid(Board board, EdgeConstraints c) {
        return rule1Complete(board)          // regra 1
                && rule2NoThreeInLine(board)  // regra 2
                && rule3Balanced(board)       // regra 3
                && rule4Equality(board, c)    // regra 4
                && rule5Opposition(board, c); // regra 5
    }

    /**
     * Valida apenas o impacto da jogada em {@code (row,col)} (poda do Backtracking).
     *
     * <p>Assume que {@code (row,col)} acabou de ser preenchida. NAO exige tabuleiro
     * completo. Cada regra olha somente a vizinhanca afetada, sem varrer o tabuleiro inteiro.
     *
     * @return {@code true} se a jogada nao viola nenhuma das regras 2 a 5
     */
    public boolean isPartialValid(Board board, EdgeConstraints c, int row, int col) {
        return rule2PartialNoThreeInLine(board, row, col)  // regra 2 (trio local)
                && rule3PartialBalance(board, row, col)    // regra 3 (linha/coluna + lookahead)
                && rule4PartialEquality(board, c, row, col)   // regra 4 (arestas = ja preenchidas)
                && rule5PartialOpposition(board, c, row, col); // regra 5 (arestas x ja preenchidas)
        // regra 1 nao se aplica a um estado parcial
    }

    // ======================================================================
    //  REGRA 1 — preenchimento completo
    // ======================================================================

    /** @return {@code true} se nenhuma celula esta vazia. */
    private boolean rule1Complete(Board board) {
        return board.isComplete();
    }

    // ======================================================================
    //  REGRA 2 — adjacencia (proibido 3 iguais em sequencia)
    // ======================================================================

    /** Versao completa: varre todos os trios horizontais e verticais do tabuleiro. */
    private boolean rule2NoThreeInLine(Board board) {
        int n = board.getN();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // trio horizontal iniciando em (i,j)
                if (j + 2 < n && isTriple(board.get(i, j), board.get(i, j + 1), board.get(i, j + 2))) {
                    return false;
                }
                // trio vertical iniciando em (i,j)
                if (i + 2 < n && isTriple(board.get(i, j), board.get(i + 1, j), board.get(i + 2, j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Versao parcial: olha apenas os trios que CONTEM {@code (row,col)}, as janelas
     * que comecam em {@code col-2, col-1, col} na horizontal (e analogamente na vertical).
     */
    private boolean rule2PartialNoThreeInLine(Board board, int row, int col) {
        int n = board.getN();
        for (int start = col - 2; start <= col; start++) {
            if (start >= 0 && start + 2 < n
                    && isTriple(board.get(row, start), board.get(row, start + 1), board.get(row, start + 2))) {
                return false;
            }
        }
        for (int start = row - 2; start <= row; start++) {
            if (start >= 0 && start + 2 < n
                    && isTriple(board.get(start, col), board.get(start + 1, col), board.get(start + 2, col))) {
                return false;
            }
        }
        return true;
    }

    /** @return {@code true} se os tres valores sao nao-vazios e iguais. */
    private boolean isTriple(int a, int b, int cc) {
        return a != Board.EMPTY && a == b && b == cc;
    }

    // ======================================================================
    //  REGRA 3 — equilibrio (N/2 de cada simbolo por linha e por coluna)
    // ======================================================================

    /** Versao completa: cada linha e cada coluna deve ter exatamente N/2 Sois e N/2 Luas. */
    private boolean rule3Balanced(Board board) {
        int n = board.getN();
        int half = n / 2;
        for (int i = 0; i < n; i++) {
            int rowSun = 0, rowMoon = 0, colSun = 0, colMoon = 0;
            for (int j = 0; j < n; j++) {
                int rv = board.get(i, j);
                if (rv == Board.SUN) rowSun++;
                else if (rv == Board.MOON) rowMoon++;

                int cv = board.get(j, i);
                if (cv == Board.SUN) colSun++;
                else if (cv == Board.MOON) colMoon++;
            }
            if (rowSun != half || rowMoon != half || colSun != half || colMoon != half) {
                return false;
            }
        }
        return true;
    }

    /**
     * Versao parcial: na linha e na coluna de {@code (row,col)}, poda se ja houver mais
     * de N/2 de um simbolo, ou se as celulas vazias restantes nao comportarem o que
     * ainda falta do outro simbolo (lookahead).
     */
    private boolean rule3PartialBalance(Board board, int row, int col) {
        return lineWithinBalance(board, row, true)   // a linha
                && lineWithinBalance(board, col, false); // a coluna
    }

    /**
     * Verifica o equilibrio (com lookahead) de uma unica linha ou coluna.
     *
     * @param index    indice da linha (se {@code isRow}) ou da coluna
     * @param isRow    {@code true} para inspecionar a linha; {@code false} para a coluna
     */
    private boolean lineWithinBalance(Board board, int index, boolean isRow) {
        int n = board.getN();
        int half = n / 2;
        int sun = 0, moon = 0, empty = 0;
        for (int k = 0; k < n; k++) {
            int v = isRow ? board.get(index, k) : board.get(k, index);
            if (v == Board.SUN) sun++;
            else if (v == Board.MOON) moon++;
            else empty++;
        }
        // ja passou do limite de um dos simbolos
        if (sun > half || moon > half) {
            return false;
        }
        // o que ainda falta de cada simbolo precisa caber nas vazias
        if ((half - sun) > empty || (half - moon) > empty) {
            return false;
        }
        return true;
    }

    // ======================================================================
    //  REGRA 4 — igualdade (=)
    // ======================================================================

    /** Versao completa: toda aresta {@code =} liga celulas de mesmo simbolo. */
    private boolean rule4Equality(Board board, EdgeConstraints c) {
        return allEdgesOfType(board, c, EdgeConstraints.EQUAL);
    }

    /** Versao parcial: arestas {@code =} de {@code (row,col)} com vizinhos JA preenchidos. */
    private boolean rule4PartialEquality(Board board, EdgeConstraints c, int row, int col) {
        return partialEdgeRule(board, c, row, col, EdgeConstraints.EQUAL);
    }

    // ======================================================================
    //  REGRA 5 — oposicao (x)
    // ======================================================================

    /** Versao completa: toda aresta {@code x} liga celulas de simbolos opostos. */
    private boolean rule5Opposition(Board board, EdgeConstraints c) {
        return allEdgesOfType(board, c, EdgeConstraints.OPPOSITE);
    }

    /** Versao parcial: arestas {@code x} de {@code (row,col)} com vizinhos JA preenchidos. */
    private boolean rule5PartialOpposition(Board board, EdgeConstraints c, int row, int col) {
        return partialEdgeRule(board, c, row, col, EdgeConstraints.OPPOSITE);
    }
    
    /**
     * Varre TODAS as arestas (horizontais e verticais) de um dado {@code type}
     * ({@code =} ou {@code x}) e confere se cada uma e respeitada. Usado na validacao
     * completa, onde ambas as celulas de cada aresta estao preenchidas.
     */
    private boolean allEdgesOfType(Board board, EdgeConstraints c, int type) {
        int n = board.getN();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - 1; j++) {
                if (c.getH(i, j) == type && !edgeConsistent(board.get(i, j), board.get(i, j + 1), type)) {
                    return false;
                }
            }
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n; j++) {
                if (c.getV(i, j) == type && !edgeConsistent(board.get(i, j), board.get(i + 1, j), type)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Confere as ate 4 arestas em torno de {@code (row,col)} para um dado {@code type},
     * considerando apenas vizinhos JA preenchidos (vizinhos vazios sao ignorados).
     */
    private boolean partialEdgeRule(Board board, EdgeConstraints c, int row, int col, int type) {
        int n = board.getN();
        int value = board.get(row, col);
        if (value == Board.EMPTY) {
            return true; // nada a checar se a celula nao esta preenchida
        }
        // esquerda: aresta H entre (row,col-1) e (row,col)
        if (col > 0 && c.getH(row, col - 1) == type && !neighborOk(board, value, row, col - 1, type)) {
            return false;
        }
        // direita: aresta H entre (row,col) e (row,col+1)
        if (col < n - 1 && c.getH(row, col) == type && !neighborOk(board, value, row, col + 1, type)) {
            return false;
        }
        // cima: aresta V entre (row-1,col) e (row,col)
        if (row > 0 && c.getV(row - 1, col) == type && !neighborOk(board, value, row - 1, col, type)) {
            return false;
        }
        // baixo: aresta V entre (row,col) e (row+1,col)
        if (row < n - 1 && c.getV(row, col) == type && !neighborOk(board, value, row + 1, col, type)) {
            return false;
        }
        return true;
    }

    /** Vizinho vazio passa (ignorado), vizinho preenchido deve respeitar a restricao. */
    private boolean neighborOk(Board board, int value, int nRow, int nCol, int type) {
        int neighbor = board.get(nRow, nCol);
        return neighbor == Board.EMPTY || edgeConsistent(value, neighbor, type);
    }

    /**
     * Regra basica de uma aresta entre dois valores preenchidos.
     *
     * @param type {@link EdgeConstraints#EQUAL} exige iguais, {@link EdgeConstraints#OPPOSITE}
     *             exige diferentes
     */
    private boolean edgeConsistent(int a, int b, int type) {
        if (type == EdgeConstraints.EQUAL) {
            return a == b;
        }
        if (type == EdgeConstraints.OPPOSITE) {
            return a != b;
        }
        return true;
    }
}
