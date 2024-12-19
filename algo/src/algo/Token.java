package algo;
/**
 * The Token class represents a single token produced by the lexer.
 * Each token has a type (defined by the TokenType enum), the actual
 * text (lexeme) that was matched, and the line number in the source
 * code where the token was found (for error messages and debugging purposes).
 */
public class Token {
    final TokenType type;
    final String lexeme;
    final int line;
    Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme;
    }
}
