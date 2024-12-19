package algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static algo.TokenType.*;

/**
 * The Lexer class is responsible for scanning the source code and converting it
 * into a list of tokens. It extends the ErrorThrower class to handle errors and logs.
 */

public class Lexer extends ErrorThrower {
	// A map to store keywords and their corresponding token types
	private static final Map<String, TokenType> keywords;

	  static {
	    keywords = new HashMap<>();
	    keywords.put("non",        NOT);
	    keywords.put("et",         AND);
	    keywords.put("ou",         OR);
	    keywords.put("vrai",       TRUE);
	    keywords.put("faux",       FALSE);
	    keywords.put("si",         IF);
	    keywords.put("alors",      THEN);
	    keywords.put("sinon",      ELSE);
	    keywords.put("finsi",      END_IF);
	    keywords.put("Algorithme", ALGO);
	    keywords.put("TDO",        TDO);
	    keywords.put("Debut",      BEGIN);
	    keywords.put("Fin",        END);
	    keywords.put("entier",     ENTIER);
	    keywords.put("reel",       REEL);
	    keywords.put("char",       CHAR);
	    keywords.put("chaine",     CHAINE);
	    keywords.put("bool",       BOOL);
	    keywords.put("tantque",    WHILE);
	    keywords.put("faire",      DO);
	    keywords.put("fintantque", END_WHILE);
	    keywords.put("ecrire",     PRINT);
	    keywords.put("div",        DIV);
	    keywords.put("mod",        MOD);
	  }
	  
	  private final String source;
	  private final List<Token> tokens = new ArrayList<>();

	  private int start = 0;
	  private int current = 0;
	  private int line = 1;

	  @Override
	  public void error(String msg) {
		  throw new RuntimeException("[LEXER] Error at line "+line+" : "+msg);
	  }
	  @Override
	  public void log(String msg) {
		  System.err.println("[LEXER] "+msg);
      }

	  Lexer(String source) {
	    this.source = source;
	  }

	  // The scanTokens method scans the source code and returns a list of tokens
	  List<Token> scanTokens() {
	    while (!isAtEnd()) {
	      start = current;
	      scanToken();
	    }

	    tokens.add(new Token(EOF, "", line));
	    return tokens;
	  }
	  // The scanToken method scans the next token in the source code
	  private void scanToken() {
	    char c = advance();
	    switch (c) {
	      case '(': addToken(LEFT_PAREN);  break;
	      case ')': addToken(RIGHT_PAREN); break;
	      case '-': addToken(MINUS);       break;
	      case '+': addToken(PLUS);        break;
	      case '/': addToken(SLASH);       break;
	      case ':': addToken(COLON);       break;
	      case ',': addToken(COMMA);       break;
	      case ';': addToken(SEMICOLON);   break;
	      case '*': addToken(STAR);        break;
	      case '=': addToken(EQUAL);       break;
	      case '!':
	    	  if(match('=')) { addToken(BANG_EQUAL); break;}
	    	  else {error("Unexpected character.");  break;}
	      case '<':
	    	  if(match('=')) addToken(LESS_EQUAL);
	    	  else if(match('-')) addToken(LESS_MINUS);
	    	  else addToken(LESS);
	          break;
	      case '>':
	        addToken(match('=') ? GREATER_EQUAL : GREATER);
	        break;
	      case ' ':
	      case '\r':
	      case '\t':
	        break;

	      case '\n':
	        line++;
	        break;
	      case '"': string(); break;
	      default:
	        if (isDigit(c)) {
	          number();
	        } else if (isAlpha(c)) {
	          identifier();
	        } else {
	          error("Unexpected character.");
	        }
	        break;
	    }
	  }
	  // The following methods are used to scan different types of tokens
	  private void identifier() {
	    while (isAlphaNumeric(peek())) advance();
	    String text = source.substring(start, current);
	    TokenType type = keywords.get(text);
	    if (type == null) type = IDENTIFIER;
	    addToken(type);
	  }
	  private void number() {
		    boolean isDouble = false;
		    while (isDigit(peek())) advance();
		    if (peek() == '.' && isDigit(peekNext())) {
		        isDouble = true;
		        advance();
		        while (isDigit(peek())) advance();
		    }
		    String text = source.substring(start, current);
		    try {
		        addToken(isDouble ? REEL_NUMBER : ENTIER_NUMBER, text);
		    } catch (NumberFormatException e) {
		        error("Invalid number format: " + text);
		    }
		}

	  private void string() {
	    while (peek() != '"' && !isAtEnd()) {
	      if (peek() == '\n') line++;
	      advance();
	    }

	    if (isAtEnd()) {
			error("Unterminated string.");
			return;
	    }
	    advance();
	    String value = source.substring(start + 1, current - 1);
	    addToken(STRING, value);
	  }
	  // The following methods are utility methods used by the lexer
	  private boolean match(char expected) {
	    if (isAtEnd()) return false;
	    if (source.charAt(current) != expected) return false;

	    current++;
	    return true;
	  }
	  
	  private char peek() {
	    if (isAtEnd()) return '\0';
	    return source.charAt(current);
	  }
	  
	  private char peekNext() {
	    if (current + 1 >= source.length()) return '\0';
	    return source.charAt(current + 1);
	  } 
	  
	  private boolean isAlpha(char c) {
	    return (c >= 'a' && c <= 'z') ||
	           (c >= 'A' && c <= 'Z') ||
	            c == '_';
	  }

	  private boolean isAlphaNumeric(char c) {
	    return isAlpha(c) || isDigit(c);
	  }
	  
	  private boolean isDigit(char c) {
	    return c >= '0' && c <= '9';
	  } 
	  
	  private boolean isAtEnd() {
	    return current >= source.length();
	  }
	  
	  private char advance() {
	    return source.charAt(current++);
	  }

	  private void addToken(TokenType type) {
	    addToken(type, null);
	  }

	  private void addToken(TokenType type, String lexeme) {
	    String text = source.substring(start, current);
	    log("at line "+line+", TokenType:"+type+" "+ text);
	    if(lexeme==null) {
	    	tokens.add(new Token(type, text, line));	    	
	    }else {
	    	tokens.add(new Token(type, lexeme, line));
	    }
	  }

}



