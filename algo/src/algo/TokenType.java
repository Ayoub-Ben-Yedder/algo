package algo;
/**
 * Enum representing the different types of tokens that can be encountered
 * in the source code. These tokens are used by the lexer to categorize
 * the input text into meaningful symbols for the parser.
 */
public enum TokenType {
	  LEFT_PAREN, RIGHT_PAREN,
	  MINUS, PLUS, SEMICOLON, SLASH, STAR,
	  COLON, COMMA,

	  EQUAL, BANG_EQUAL,
	  GREATER, GREATER_EQUAL,
	  LESS, LESS_EQUAL, LESS_MINUS,

	  IDENTIFIER, STRING, ENTIER_NUMBER, REEL_NUMBER,

	  ALGO, TDO, BEGIN, END,
	  ENTIER, REEL, CHAINE, CHAR, BOOL,
	  IF, THEN, END_IF, ELSE,
	  NOT, AND, OR,
	  TRUE, FALSE,
	  WHILE, DO, END_WHILE,
	  PRINT, DIV, MOD,

	  EOF
}