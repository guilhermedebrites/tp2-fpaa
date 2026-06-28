package tango.io;

import tango.model.Board;
import tango.model.EdgeConstraints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Leitor do formato {@code .txt} do Tango.
 *
 * <p>Formato esperado:
 * <pre>
 * N                     (dimensao, deve ser par)
 * &lt;N linhas de grid&gt;     (simbolos . / S / L separados por espaco)
 * H:
 * &lt;N linhas&gt;             (cada uma com N-1 simbolos . / = / x — arestas horizontais)
 * V:
 * &lt;N-1 linhas&gt;           (cada uma com N simbolos . / = / x — arestas verticais)
 * </pre>
 *
 * <p>Linhas em branco e espacos extras sao toleradas. Qualquer desvio do formato
 * gera uma {@link IllegalArgumentException} com mensagem clara.
 */
public final class BoardReader {

    private BoardReader() {
    }

    /**
     * O tabuleiro inicial e suas restricoes de aresta.
     */
    public record Puzzle(Board board, EdgeConstraints constraints) {
    }

    /**
     * Le um puzzle a partir de um caminho de arquivo.
     *
     * @param path caminho do arquivo {@code .txt}
     * @return o tabuleiro inicial e as restricoes
     * @throws IOException              se houver erro de leitura do arquivo
     * @throws IllegalArgumentException se o conteudo nao seguir o formato esperado
     */
    public static Puzzle read(Path path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return read(br);
        }
    }

    /**
     * Le um puzzle a partir de um {@link Reader} (util para testes).
     *
     * @param reader fonte do conteudo
     * @return o tabuleiro inicial e as restricoes
     * @throws IOException              se houver erro de leitura
     * @throws IllegalArgumentException se o conteudo nao seguir o formato esperado
     */
    public static Puzzle read(Reader reader) throws IOException {
        BufferedReader br = (reader instanceof BufferedReader b) ? b : new BufferedReader(reader);

        // Coleta apenas as linhas com conteudo (ignora linhas em branco).
        List<String> lines = new ArrayList<>();
        String raw;
        while ((raw = br.readLine()) != null) {
            String trimmed = raw.strip();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }

        Cursor cursor = new Cursor(lines);

        // 1) N (par).
        int n = parseN(cursor.next("dimensao N"));

        // 2) Grid: N linhas.
        Board board = new Board(n);
        for (int i = 0; i < n; i++) {
            String[] tokens = tokens(cursor.next("linha " + (i + 1) + " do grid"));
            requireCount(tokens, n, "linha " + (i + 1) + " do grid", "celulas");
            for (int j = 0; j < n; j++) {
                board.set(i, j, parseCell(tokens[j], i, j));
            }
        }

        EdgeConstraints constraints = new EdgeConstraints(n);

        // 3) Bloco H: N linhas de N-1 simbolos.
        requireMarker(cursor.next("marcador 'H:'"), "H:");
        for (int i = 0; i < n; i++) {
            String[] tokens = tokens(cursor.next("linha " + (i + 1) + " do bloco H"));
            requireCount(tokens, n - 1, "linha " + (i + 1) + " do bloco H", "restricoes");
            for (int j = 0; j < n - 1; j++) {
                constraints.setH(i, j, parseEdge(tokens[j], "H", i, j));
            }
        }

        // 4) Bloco V: N-1 linhas de N simbolos.
        requireMarker(cursor.next("marcador 'V:'"), "V:");
        for (int i = 0; i < n - 1; i++) {
            String[] tokens = tokens(cursor.next("linha " + (i + 1) + " do bloco V"));
            requireCount(tokens, n, "linha " + (i + 1) + " do bloco V", "restricoes");
            for (int j = 0; j < n; j++) {
                constraints.setV(i, j, parseEdge(tokens[j], "V", i, j));
            }
        }

        return new Puzzle(board, constraints);
    }

    private static int parseN(String line) {
        int n;
        try {
            n = Integer.parseInt(line.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("primeira linha deve ser o numero N, encontrado: '" + line + "'");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("N deve ser positivo, encontrado: " + n);
        }
        if (n % 2 != 0) {
            throw new IllegalArgumentException("N deve ser par (regra do equilibrio exige paridade), encontrado: " + n);
        }
        return n;
    }

    private static int parseCell(String token, int i, int j) {
        return switch (token) {
            case ".", "_" -> Board.EMPTY;
            case "S", "s" -> Board.SUN;
            case "L", "l" -> Board.MOON;
            default -> throw new IllegalArgumentException(
                    "celula invalida em (" + i + "," + j + "): '" + token + "' (esperado '.', 'S' ou 'L')");
        };
    }

    private static int parseEdge(String token, String block, int i, int j) {
        return switch (token) {
            case ".", "_" -> EdgeConstraints.NONE;
            case "=" -> EdgeConstraints.EQUAL;
            case "x", "X", "*" -> EdgeConstraints.OPPOSITE;
            default -> throw new IllegalArgumentException(
                    "restricao invalida no bloco " + block + " em (" + i + "," + j + "): '" + token
                            + "' (esperado '.', '=' ou 'x')");
        };
    }

    private static void requireMarker(String line, String expected) {
        String got = line.strip();
        String normalizedExpected = expected.replace(":", "");
        String normalizedGot = got.replace(":", "");
        if (!normalizedGot.equalsIgnoreCase(normalizedExpected)) {
            throw new IllegalArgumentException("esperado marcador '" + expected + "', encontrado: '" + line + "'");
        }
    }

    private static String[] tokens(String line) {
        return line.strip().split("\\s+");
    }

    private static void requireCount(String[] tokens, int expected, String where, String what) {
        if (tokens.length != expected) {
            throw new IllegalArgumentException(
                    where + " deve ter " + expected + " " + what + ", encontrado " + tokens.length);
        }
    }

    /** Cursor sequencial sobre a lista de linhas, com erro se faltar conteudo. */
    private static final class Cursor {
        private final List<String> lines;
        private int pos;

        Cursor(List<String> lines) {
            this.lines = lines;
        }

        String next(String expecting) {
            if (pos >= lines.size()) {
                throw new IllegalArgumentException("fim inesperado do arquivo; esperava: " + expecting);
            }
            return lines.get(pos++);
        }
    }
}
