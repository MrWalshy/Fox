package dev.morganwalsh.fox;

import static dev.morganwalsh.fox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

	private static class ParseError extends RuntimeException {}
	
	private final List<Token> tokens;
	
	private int current;
		
	public Parser(List<Token> tokens) {
		this.tokens = tokens;
		current = 0;
	}

	public List<Expression> parse() {
		List<Expression> expressions = new ArrayList<>();
		
		while (!isAtEnd()) expressions.add(expression());
		return expressions;
	}

	/**
	 * Returns true if the current token to be consumed is the end of file token.
	 * @return
	 */
	private boolean isAtEnd() {
		return peek().type == EOF;
	}
	
	private Token peek() {
		return tokens.get(current);
	}
	
	private Token advance() {
		if (!isAtEnd()) current++;
		return previous();
	}

	private Token previous() {
		if (current == 0) return null;
		return tokens.get(current - 1);
	}
	
	private boolean check(TokenType type) {
		if (isAtEnd()) return false;
		return peek().type == type;
	}
	
	private boolean checkTwoAhead(TokenType type) {
		if (isAtEnd()) return false;
		return tokens.get(current + 2).type == type;
	}
	
	private boolean checkAhead(TokenType type, int num) {
		if (isAtEnd()) return false;
		if (current + num >= tokens.size()) return false;
		return tokens.get(current + num).type == type;
	}
	
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			// checks if the current token has the given type
			if (check(type)) {
				advance(); // consume token
				return true;
			}
		}
		return false;
	}
	
	private Token consume(TokenType type, String message) throws ParseError {
		if (check(type)) return advance();
		throw error(peek(), message);
	}

	private ParseError error(Token token, String message) {
		Fox.error(token, message);
		return new ParseError();
	}

	private Expression expression() {
//		try {
		// let it crash out for now, no synchronisation after error yet
			if (check(DEFUN) && checkTwoAhead(IDENTIFIER)) {
				consume(DEFUN, null);
				return function("function");
			}
			if (match(VAR)) return var();
			if (match(ASSIGN)) return assign();
//			if (match(LEFT_CURLY)) return block();
			return ternaryExpression();
//		} catch (ParseError error) {
//			return null;
//		}
	}

	private Expression block() {
		Expression expression = expression();
		List<Expression> expressions = new ArrayList<>();
		expressions.add(expression);
		
//		if (check(COMMA)) {
			while (!check(RIGHT_CURLY) && !isAtEnd()) {
//				consume(COMMA, "Expect ',' after expression in expression block.");
				expressions.add(expression());
			}
//		} 
		consume(RIGHT_CURLY, "Expect '}' after expression block.");
		expression = new Expression.Block(expressions);
		return expression;
	}

	private Expression function(String kind) {
		consume(LEFT_PAREN, "Expected '(' after " + kind + " declaration call.");
		Token identifier = null;
		
		if (kind.equals("function")) {
			identifier = consume(IDENTIFIER, "Expected identifier string as first argument to defun call.");
			consume(COMMA, "Expect ',' after function identifier.");
		} 
		
		// arrow function style definition starts here for the parameters and body
		// of the actual function
		consume(LEFT_PAREN, "Expected '(' before parameter list.");
		List<Token> params = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				params.add(consume(IDENTIFIER, "Expected parameter name"));
			} while (match(COMMA));
		}
		consume(RIGHT_PAREN, "Expect ')' after function parameter list.");
		consume(ARROW, "Expected an '->' after function parameter list");
		
		Expression body = getBody();
		
		// close the call to defun
		consume(RIGHT_PAREN, "Expected ')' to close defun call.");
		
		return new Expression.Function(identifier, params, body);
	}
	
	private Expression getBody() {
		if (match(LEFT_CURLY)) return block();
		else if (check(LEFT_PAREN)) {
			// arrow function style definition starts here for the parameters and body
			// of the actual function
			consume(LEFT_PAREN, "Expected '(' before parameter list.");
			List<Token> params = new ArrayList<>();
			if (!check(RIGHT_PAREN)) {
				do {
					params.add(consume(IDENTIFIER, "Expected parameter name"));
				} while (match(COMMA));
			}
			consume(RIGHT_PAREN, "Expect ')' after function parameter list.");
			consume(ARROW, "Expected an '->' after function parameter list");
			return new Expression.Function(null, params, getBody());
		}
		else return ternaryExpression();
	}

	private Expression var() {
		consume(LEFT_PAREN, "Expect '(' after var definition call.");
		Token identifier = consume(IDENTIFIER, "Expect identifier as first argument to var definer.");
		
		Expression initialiser = null;
		
		if (check(COMMA)) {
			consume(COMMA, null);
			
			if (check(LEFT_CURLY)) {
				consume(LEFT_CURLY, "Expected start of expression block after identifier argument.");
				initialiser = block();
			} else initialiser = ternaryExpression();
		}
		
		consume(RIGHT_PAREN, "Expect ')' after var definition call arguments.");
		return new Expression.Var(identifier, initialiser);
	}
	
	private Expression assign() {
		consume(LEFT_PAREN, "Expect '(' after var assign call.");
		Token identifier = consume(IDENTIFIER, "Expect identifier as first argument to var assignment.");
		consume(COMMA, "Expect ',' after identifer");
		
		Expression value = null;
		if (check(LEFT_CURLY)) {
			consume(LEFT_CURLY, "Expected start of expression block after identifier argument.");
			value = block();
		} else value = ternaryExpression();
		
		
		consume(RIGHT_PAREN, "Expect ')' after var definition call arguments.");
		return new Expression.Assign(identifier, value);
	}

	private Expression ternaryExpression() {
		Expression expression = or();
		
		if (match(QUESTION_MARK)) {
			Token operator = previous(); // use for making new token
			Expression ifTrue = expression();
			consume(COLON, "Colon expected to complete ternary conditional");
			Expression ifFalse = expression();
			Token ternary = new Token(TERNARY, "?:", null, operator.line);
			return new Expression.Ternary(expression, ifTrue, ifFalse, ternary);
		}
		return expression;
	}

	private Expression or() {
		Expression expression = and();
		
		while (match(OR)) {
			Token operator = previous();
			Expression right = and();
			expression = new Expression.Logical(expression, operator, right);
		}
		return expression;
	}

	private Expression and() {
		Expression expression = equality();
		
		while (match(AND)) {
			Token operator = previous();
			Expression right = equality();
			expression = new Expression.Logical(expression, operator, right);
		}
		return expression;
	}

	/**
	 * Grammar: equality -> comparison ( ( "!=" | "==" ) comparison )*
	 * @return
	 * @throws Exception 
	 */
	private Expression equality() throws ParseError {
		Expression expr = comparison();
		
		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expression right = comparison();
			expr = new Expression.Binary(expr, operator, right);
		}
		return expr;
	}

	/**
	 * Grammar: comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
	 * @return
	 * @throws Exception 
	 */
	private Expression comparison() throws ParseError {
		Expression expr = term();
		
		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous();
			Expression right = term();
			expr = new Expression.Binary(expr, operator, right);
		}
		return expr;
	}

	/**
	 * Grammar: term -> factor ( ( "-" | "+" ) factor )* ;
	 * @return
	 * @throws Exception 
	 */
	private Expression term() throws ParseError {
		Expression expr = factor();
		
		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expression right = factor();
			expr = new Expression.Binary(expr, operator, right);
		}
		return expr;
	}

	/**
	 * Grammar: factor -> unary ( ( "/" | "*" ) unary )* ;
	 * @return
	 * @throws Exception 
	 */
	private Expression factor() throws ParseError {
		Expression expr = unary();
		
		while (match(SLASH, STAR)) {
			Token operator = previous();
			Expression right = unary();
			expr = new Expression.Binary(expr, operator, right);
		}
		return expr;
	}

	/**
	 * Grammar: unary -> ( "!" | "-" ) unary | primary ;
	 * @return
	 * @throws Exception 
	 */
	private Expression unary() throws ParseError {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expression right = unary();
			return new Expression.Unary(operator, right);
		}
		return call();
	}
	
	private Expression call() {
		Expression expression = primary();
		
		while (true) {
			if (match(LEFT_PAREN)) expression = finishCall(expression);
			else break;
		}
		return expression;
	}
	
	/**
	 * Translation of the arguments rule
	 */
	private Expression finishCall(Expression expression) {
		List<Expression> arguments = new ArrayList<>();
		
		if (!check(RIGHT_PAREN)) {
			do {
				arguments.add(expression()); // parse each argument
			} while (match(COMMA));
		}
		
		Token rightParen = consume(RIGHT_PAREN, "Expect ')' after argument list.");
		return new Expression.Call(expression, rightParen, arguments);
	}

	/**
	 * Grammar: primary -> NUMBER | STRING | "true" | "false" | "nil" 
                         | "(" expression ")" ;
	 * @return
	 * @throws Exception 
	 */
	private Expression primary() throws ParseError {
		if (match(FALSE)) return new Expression.Literal(false);
		if (match(TRUE)) return new Expression.Literal(true);
		if (match(NULL)) return new Expression.Literal(null);
		if (match(DEFUN)) return function("anonymous");
		if (match(LEFT_CURLY)) return block();
		
		if (match(NUMBER, STRING)) return new Expression.Literal(previous().literal);
		
		if (match(IDENTIFIER)) return new Expression.Variable(previous());
		
		if (match(LEFT_PAREN)) {
			// check if a ternary or not
			// - maybe not necessary if i treat the group as a ternary expression
//			int lookahead = 1;
//			boolean isTernary = false;
//			while (!checkAhead(RIGHT_PAREN, lookahead)) {
//				if (checkAhead(QUESTION_MARK, lookahead)) {
//					isTernary = true;
//					break;
//				}
//			}
			Expression expr = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression");
			return new Expression.Grouping(expr);
		}
		
		// mustn't be a token that starts an expression to get here
		throw error(peek(), "Expected an expression");
	}
}
