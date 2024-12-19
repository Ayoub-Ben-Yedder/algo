package algo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static algo.TokenType.*;

/**
 * The Interpreter class is responsible for interpreting the list of tokens
 * produced by the lexer and executing the corresponding actions. It extends
 * the ErrorThrower class to handle errors and logs.
 */
public class Interpreter extends ErrorThrower{
    private Token currentToken;
    private final List<Token> tokens;
    // A map to store variables and their values
    private final Map<String, Variable> environment = new HashMap<>();
    private int currentIndex = 0;

	@Override
	public void error(String msg) {
		throw new RuntimeException("[INTERPRETER] Error at line "+currentToken.line + " Token: " + currentToken.lexeme + " : " + msg);
	}

	@Override
	public void log(String msg) {
		System.err.println("[INTERPRETER] "+msg);
	}
	
    public Interpreter(List<Token> tokens) {
        this.tokens = tokens;
        this.currentToken = tokens.get(0);
    }

    // The advance method moves to the next token in the list
    private void advance() {
        currentIndex++;
        if (currentIndex < tokens.size()) {
            currentToken = tokens.get(currentIndex);
        }
    }
    // The previous method returns the previous token in the list
    private Token previous() {
        if (currentIndex == 0) {
            error("No previous token exists.");
        }
        return tokens.get(currentIndex - 1);
    }
    // The match method checks if the current token matches any of the given types
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (currentToken.type == type) {
            	log("Matching : " + currentToken);
                advance();
                return true;
            }
        }
        return false;
    }
    // The check method checks if the current token matches any of the given types
    private boolean check(TokenType... types) {
        for (TokenType type : types) {
            if (currentToken.type == type) {
            	log("Chekcing : " + currentToken);
                return true;
            }
        }
        return false;
    }
    // The consume method consumes the current token if it matches the given type
    private Token consume(TokenType type, String errorMessage) {
        if (currentToken.type == type) {
            Token token = currentToken;
            log("Consuming: " + token);
            advance();
            return token;
        }
        error(errorMessage);
        return null;
    }

    // The interpret method is the entry point for the interpreter
    public void interpret() {
        consume(ALGO, "Expected 'Algorithme' at the start of the program.");
        consume(IDENTIFIER, "Expected program name after 'Algorithme'.");
        consume(TDO, "Expected 'TDO' after program name.");
        parseDeclarations();
        consume(BEGIN, "Expected 'Debut' to start program body.");
        while (!check(END, EOF)) {
        	parseStatement();        		
        }
        consume(END, "Expected  'Fin'");
    }
    // The parseDeclarations method parses the variable declarations in the program
    private void parseDeclarations() {
        while (match(IDENTIFIER)) {
        	String name = previous().lexeme;
        	log("Declaring : "+name);
            consume(COLON, "Expected ':' after "+name+".");
            if (!match(ENTIER, REEL, BOOL, CHAINE, CHAR)) {
                error("Expected Type after ':'");
            }
            environment.put(name, new Variable(null, previous().type));
            consume(SEMICOLON, "Expected ';' after "+previous().lexeme+".");
        }
    }
    // The parseStatement method parses a single statement in the program
    private void parseStatement() {
        if (match(PRINT)) {
        	parsePrintStatement();
        } else if (match(IF)) {
            parseIfStatement();
        } else if (match(WHILE)) {
            parseWhileStatement();
        } else if (match(IDENTIFIER)) {
            parseVariableAssign();
        } else {
            parseExpressionStatement();
        }
    }
    // The parsePrintStatement method parses the print statement in the program
    private void parsePrintStatement() {
        consume(LEFT_PAREN, "Expected '(' after ecrire");

        boolean first = true;
        while (!match(RIGHT_PAREN)) {
            if (!first) {
                consume(COMMA, "Expected ',' between expressions.");
            }
            Object value = parseExpression();
            System.out.print(value+" ");
            first = false;
        }

        System.out.println();
        consume(SEMICOLON, "Expected ';' after print statement.");
    }
    // The parseIfStatement method parses the if statement in the program
    private void parseIfStatement() {
        Object condition = parseExpression();
        consume(THEN, "Expected 'alors' after condition.");

        if (condition instanceof Boolean && (Boolean) condition) {
        	while (!check(ELSE, END_IF)) {
                parseStatement();
            }
            if (match(ELSE)) {
            	int nestingLevel = 1;
                while (nestingLevel > 0) {
                    if (check(IF)) {
                        nestingLevel++;
                        if(nestingLevel==0) {
                        	break;
                        }
                        advance();
                    } else if (check(END_IF)) {
                        nestingLevel--;
                        if(nestingLevel==0) {
                        	break;
                        }
                        advance();
                    } else {
                        advance();
                    }
                }
            }
        } else {
        	int nestingLevel = 1; 
            while (nestingLevel > 0) {
                if (check(IF)) {
                    nestingLevel++;
                    if(nestingLevel==0) {
                    	break;
                    }
                    advance();
                } else if (check(END_IF)) {
                    nestingLevel--;
                    if(nestingLevel==0) {
                    	advance();
                    	break;
                    }
                    advance();
                } else if(check(ELSE)){
                	nestingLevel--;
                	if(nestingLevel==0) {
                    	break;
                    }
                	advance();
                } else {
                    advance();
                }
            }
            if (match(ELSE)) {
            	while (!check(END_IF)) {
                    parseStatement();
                }
            }
        }
        consume(END_IF, "Expected 'finsi' to close the if block.");
    }
    // The parseWhileStatement method parses the while statement in the program
    private void parseWhileStatement() {

        int loopStartIndex = currentIndex;

        Object condition = parseExpression();
        consume(DO, "Expected 'faire' after condition.");

        if (!(condition instanceof Boolean)) {
            error("Condition in 'tantque' must evaluate to a boolean.");
        }

        while ((Boolean) condition) {
        	while (!match(END_WHILE)) {
                parseStatement();
            }

            currentIndex = loopStartIndex;
            currentToken = tokens.get(currentIndex);

            condition = parseExpression();
            consume(DO, "Expected 'faire' after condition.");
        }

        int nestedLoops = 0;

        while (true) {
            if (match(WHILE)) {
                nestedLoops++;
            } else if (match(END_WHILE)) {
                if (nestedLoops == 0) {
                    break;
                }
                nestedLoops--;
            } else {
                advance();
            }
        }

    }
    // The parseVariableAssign method parses the variable assignment in the program
    private void parseVariableAssign() {
        String name = previous().lexeme;
        consume(LESS_MINUS, "Expected '<-' after " + name);
        Object value = parseExpression();

        if (!environment.containsKey(name)) {
            error("Undefined variable: " + name);
        }

        Variable variable = environment.get(name);
        if (variable.type == ENTIER && !(value instanceof Integer)) {
            error("Type mismatch: Cannot assign non-integer to ENTIER variable.");
        } else if (variable.type == REEL && !(value instanceof Double || value instanceof Integer)) {
            error("Type mismatch: Cannot assign non-numeric to REEL variable.");
        }

        if (variable.type == REEL && value instanceof Integer) {
            value = ((Integer) value).doubleValue();
        }

        environment.put(name, new Variable(value, variable.type));
        consume(SEMICOLON, "Expected ';' after variable declaration.");
    }
    // The parseExpressionStatement method parses the expression statement in the program
    private void parseExpressionStatement() {
        parseExpression();
        consume(SEMICOLON, "Expected ';' after expression.");
    }
    // These methods parse the different types of expressions in the program and handles operator precedence
    private Object parseExpression() {
        return parseLogicalOr();
    }

    private Object parseLogicalOr() {
        Object left = parseLogicalAnd();
        while (match(OR)) {
            Object right = parseLogicalAnd();
            if (left instanceof Boolean && right instanceof Boolean) {
                left = (Boolean) left || (Boolean) right;
            } else {
                error("Invalid operands for OR.");
            }
        }
        return left;
    }

    private Object parseLogicalAnd() {
        Object left = parseEquality();
        while (match(AND)) {
            Object right = parseEquality();
            if (left instanceof Boolean && right instanceof Boolean) {
                left = (Boolean) left && (Boolean) right;
            } else {
                error("Invalid operands for AND.");
            }
        }
        return left;
    }

    private Object parseEquality() {
        Object left = parseComparison();
        while (match(EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Object right = parseComparison();
            if (left instanceof Double && right instanceof Double) {
                left = operator.type == EQUAL ? left.equals(right) : !left.equals(right);
            } else if (left instanceof Integer && right instanceof Integer) { 
            	left = operator.type == EQUAL ? left.equals(right) : !left.equals(right);
            } else if (left instanceof Boolean && right instanceof Boolean) {
                left = operator.type == EQUAL ? left.equals(right) : !left.equals(right);
            } else {
                error("Invalid operands for equality.");
            }
        }
        return left;
    }

    
    private Object parseComparison() {
        Object left = parseAddition();
        
        while (match(LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, EQUAL)) {
            Token operator = previous();
            Object right = parseAddition(); 
            
            if (left instanceof Integer && right instanceof Integer) {
                switch (operator.type) {
                    case LESS:
                        left = (Integer) left < (Integer) right;
                        break;
                    case GREATER:
                        left = (Integer) left > (Integer) right;
                        break;
                    case LESS_EQUAL:
                        left = (Integer) left <= (Integer) right;
                        break;
                    case GREATER_EQUAL:
                        left = (Integer) left >= (Integer) right;
                        break;
                    case EQUAL:
                    	left = (Integer) left == (Integer) right;
                    	break;
				default:
					break;
                }
            } else if (left instanceof Double || right instanceof Double) {
                double leftVal = left instanceof Integer ? (Integer) left : (Double) left;
                double rightVal = right instanceof Integer ? (Integer) right : (Double) right;

                switch (operator.type) {
                    case LESS:
                        left = leftVal < rightVal;
                        break;
                    case GREATER:
                        left = leftVal > rightVal;
                        break;
                    case LESS_EQUAL:
                        left = leftVal <= rightVal;
                        break;
                    case GREATER_EQUAL:
                        left = leftVal >= rightVal;
                        break;
                    case EQUAL:
                    	left = leftVal == rightVal;
                    	break;
				default:
					break;
                }
            } else {
                error("Invalid operands for comparison.");
            }
        }

        return left;
    }

    private Object parseAddition() {
        Object left = parseMultiplication();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Object right = parseMultiplication();

            if (left instanceof Integer && right instanceof Integer) {
                left = operator.type == PLUS
                    ? (Integer) left + (Integer) right
                    : (Integer) left - (Integer) right;
            } else if (left instanceof Double || right instanceof Double) {
                double leftVal = left instanceof Integer ? (Integer) left : (Double) left;
                double rightVal = right instanceof Integer ? (Integer) right : (Double) right;

                left = operator.type == PLUS ? leftVal + rightVal : leftVal - rightVal;
            } else {
                error("Invalid operands for addition or subtraction.");
            }
        }
        return left;
    }



    private Object parseMultiplication() {
        Object left = parseUnary();
        while (match(STAR, SLASH, DIV, MOD)) {
            Token operator = previous();
            Object right = parseUnary();

            if (left instanceof Integer && right instanceof Integer) {
                switch (operator.type) {
                    case STAR: left = (Integer) left * (Integer) right; break;
                    case DIV:
                        if ((Integer) right == 0) error("Division by zero.");
                        left = (Integer) left / (Integer) right; break;
                    case MOD:
                        if ((Integer) right == 0) error("Modulo by zero.");
                        left = (Integer) left % (Integer) right; break;
                    default: error("Invalid operator for integers: " + operator.lexeme);
                }
            } else if (left instanceof Double || right instanceof Double) {
                double leftVal = left instanceof Integer ? (Integer) left : (Double) left;
                double rightVal = right instanceof Integer ? (Integer) right : (Double) right;

                switch (operator.type) {
                    case STAR: left = leftVal * rightVal; break;
                    case SLASH:
                        if (rightVal == 0) error("Division by zero.");
                        left = leftVal / rightVal; break;
                    default: error("Invalid operator for doubles: " + operator.lexeme);
                }
            } else {
                error("Operands must be both integers or both doubles.");
            }
        }
        return left;
    }

    private Object parseUnary() {
        if (match(MINUS)) {
            Object operand = parsePrimary();
            if (operand instanceof Integer) {
                return -(Integer) operand;
            } else if (operand instanceof Double) {
                return -(Double) operand;
            } else {
                error("Invalid operand for unary minus.");
            }
        }
        return parsePrimary();
    }


    private Object parsePrimary() {
        if (match(ENTIER_NUMBER)) {
            return Integer.valueOf(previous().lexeme);
        }
        if (match(REEL_NUMBER)) {
            return (Double.valueOf(previous().lexeme));
        }
        if (match(STRING)) {
            return previous().lexeme;
        }
        if (match(TRUE)) {
            return true;
        }
        if (match(FALSE)) {
            return false;
        }
        if (match(LEFT_PAREN)) {
            Object expression = parseExpression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return expression;
        }
        if (match(IDENTIFIER)) {
            String name = previous().lexeme;
            Variable variable = environment.get(name);
            if (variable == null) {
                error("Undefined variable: " + name);
            }
            return variable.value;
        }

        error("Expected an expression.");
        return null;
    }

    // A helper class to store variable values and types
    private static class Variable {
        final Object value;
        final TokenType type;

        public Variable(Object value, TokenType type) {
            this.value = value;
            this.type = type;
        }
    }



}

