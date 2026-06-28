package tango;

import tango.io.BoardPrinter;
import tango.io.BoardReader;
import tango.model.Board;
import tango.model.EdgeConstraints;
import tango.solver.BacktrackingSolver;
import tango.solver.BruteForceSolver;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;

/**
 * Ponto de entrada da aplicacao console.
 *
 * <p>Le um arquivo {@code .txt}, imprime o tabuleiro inicial, resolve com a
 * estrategia escolhida e imprime o tabuleiro final (ou aviso), junto da metrica
 * da estrategia (nos explorados / combinacoes testadas).
 *
 * <p>Uso: {@code java tango.Main <arquivo> [bt|bf]} — default {@code bt}.
 * <ul>
 *   <li>{@code bt} — Backtracking (busca com poda);</li>
 *   <li>{@code bf} — Forca Bruta (busca exaustiva).</li>
 * </ul>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java tango.Main <arquivo> [bt|bf]");
            System.err.println("  bt = Backtracking (default), bf = Forca Bruta");
            System.err.println("Exemplo: java tango.Main input/exemplo6_solvavel.txt bt");
            System.exit(2);
            return;
        }

        Path path = Path.of(args[0]);
        String strategy = (args.length >= 2 ? args[1] : "bt").toLowerCase();
        if (!strategy.equals("bt") && !strategy.equals("bf")) {
            System.err.println("Estrategia desconhecida: '" + strategy + "' (use 'bt' ou 'bf')");
            System.exit(2);
            return;
        }

        try {
            BoardReader.Puzzle puzzle = BoardReader.read(path);
            Board board = puzzle.board();
            EdgeConstraints constraints = puzzle.constraints();
            int n = board.getN();

            System.out.println("Tabuleiro inicial (" + n + "x" + n + "):");
            System.out.println();
            BoardPrinter.print(board, constraints);

            int exit = strategy.equals("bf")
                    ? runBruteForce(board, constraints)
                    : runBacktracking(board, constraints);

            if (exit != 0) {
                System.exit(exit);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo '" + path + "': " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Arquivo invalido '" + path + "': " + e.getMessage());
            System.exit(1);
        }
    }

    private static int runBacktracking(Board board, EdgeConstraints c) {
        BacktrackingSolver solver = new BacktrackingSolver();
        long startNanos = System.nanoTime();
        boolean solved = solver.solve(board, c);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        System.out.println();
        System.out.println("=== Backtracking ===");
        if (solved) {
            System.out.println("Tabuleiro final (resolvido):");
            System.out.println();
            BoardPrinter.print(board, c);
        } else {
            System.out.println("Sem solucao: o espaco de busca foi esgotado sem tabuleiro valido.");
        }
        System.out.println("Nos explorados: " + solver.getNodesExplored());
        System.out.println("Tempo: " + elapsedMs + " ms");
        return solved ? 0 : 3;
    }

    private static int runBruteForce(Board board, EdgeConstraints c) {
        BruteForceSolver solver = new BruteForceSolver();
        long startNanos = System.nanoTime();
        boolean solved = solver.solve(board, c);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        System.out.println();
        System.out.println("=== Forca Bruta ===");
        if (solver.wasAborted()) {
            int k = solver.getEmptyCount();
            BigInteger combos = BigInteger.TWO.pow(k);
            System.out.println("Estrategia inviavel: k=" + k + " celulas vazias -> 2^" + k
                    + " = " + combos + " combinacoes.");
            System.out.println("Acima do limite de seguranca (k > " + BruteForceSolver.MAX_EMPTY
                    + "); forca bruta abortada (o programa segue normalmente).");
            return 4;
        }
        if (solved) {
            System.out.println("Tabuleiro final (resolvido):");
            System.out.println();
            BoardPrinter.print(board, c);
        } else {
            System.out.println("Sem solucao: nenhuma das combinacoes satisfez as 5 regras.");
        }
        System.out.println("Celulas vazias (k): " + solver.getEmptyCount());
        System.out.println("Combinacoes testadas: " + solver.getCombinationsTested());
        System.out.println("Tempo: " + elapsedMs + " ms");
        return solved ? 0 : 3;
    }
}
