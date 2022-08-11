package dev.morganwalsh.fox;

import static dev.morganwalsh.fox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

import dev.morganwalsh.fox.Expression.Case;
import dev.morganwalsh.fox.Expression.CasePattern;

public class Parser {

	private static class ParseError extends RuntimeException {
	}

	private final List<Token> tokens;

	private int current;
	private int loopDepth;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
		current = 0;
		loopDepth = 0;
	}

	public List<Expression> parse() {
		List<Expression> expressions = new ArrayList<>();

		while (!isAtEnd())
			expressions.add(expression());
		return expressions;
	}

	/**
	 * Returns true if the current token to be consumed is the end of file token.
	 * 
	 * @return
	 */
	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}

	private Token previous() {
		if (current == 0)
			return null;
		return tokens.get(current - 1);
	}

	private boolean check(TokenType type) {
		if (isAtEnd())
			return false;
		return peek().type == type;
	}

	private boolean checkTwoAhead(TokenType type) {
		if (isAtEnd())
			return false;
		return tokens.get(current + 2).type == type;
	}

	private boolean checkAhead(TokenType type, int num) {
		if (isAtEnd())
			return false;
		if (current + num >= tokens.size())
			return false;
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
		if (check(type))
			return advance();
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
		if (match(IMPORT))
			return importExpression();
		if (match(VAR))
			return var();
		if (match(ASSIGN))
			return assign(previous());
		if (match(MATCH))
			return matchExpression();
		if (match(WHILE))
			return whileLoop();
		if (match(BREAK))
			return breakExpression();
		return ternaryExpression();
//		} catch (ParseError error) {
//			return null;
//		}
	}

	private Expression breakExpression() {
		Token previous = previous();
		consume(LEFT_PAREN, "Expected '(' after break.");
		consume(RIGHT_PAREN, "Expected ')' to complete call to break.");
		if (loopDepth < 1) error(previous, "Can only use 'break' in a loop expression.");
		return new Expression.ControlFlow(previous);
		
	}

	private Expression whileLoop() {
		consume(LEFT_PAREN, "Expected a '(' after 'while'");
		loopDepth++;
		Expression condition = expression();
		Expression body = null;
		
		if (match(COMMA)) {
			body = expression();
		}
		loopDepth--;
		consume(RIGHT_PAREN, "Expected ')' after while loop body.");
		return body == null ? new Expression.While(null, condition)
				: new Expression.While(condition, body);
	}

	private Expression importExpression() {
		consume(LEFT_PAREN, "Expected '(' after import declaration.");
		Token file = consume(STRING, "Expected file location.");
		consume(RIGHT_PAREN, "Expected ')' after file location.");
		return new Expression.Import(file);
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
	
	private Expression matchExpression() {
		Expression valueUnderTest = expression();
		Token leftCurly = consume(LEFT_CURLY, "Expected '{' after match value.");
		List<Expression.Case> cases = new ArrayList<>();

		while (!check(RIGHT_CURLY) && !isAtEnd()) {
			cases.add(matchCase());
		}
		if (cases.size() < 1)
			throw error(leftCurly, "Match expression cannot contain 0 cases.");
		consume(RIGHT_CURLY, "Expected a '}' to close match expression.");

		return new Expression.Match(leftCurly, valueUnderTest, cases);
	}

	private Case matchCase() {
		Expression condition = null;
		condition = casePattern();
		Token caseArrow = consume(FAT_ARROW, "Expected '=>' between case expression and case body.");
		Expression body = expression();
		return new Expression.Case(caseArrow, condition, body);
	}
	
	private Expression casePattern() {
		Expression expression = null;
		
		// any custom case patterns
		if (match(LEFT_PAREN)) {
			// first expression of the pattern to match against
			expression = expression();
			
			// separator
			if (!match(PIPE)) error(previous(), "Invalid case pattern supplied.");
			
			do {
				Token operator = previous();
				Expression right = expression();
				expression = new Expression.CasePattern(expression, operator, right);
			} while (match(PIPE));
			consume(RIGHT_PAREN, "Expected ')' to close case pattern.");
		} else if (match(UNDERSCORE)) {
			// default case
			Token previous = previous();
			expression = new Expression.CasePattern(null, previous, null);
		} else {
			// not a case pattern
			expression = expression();
		}
		
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
		if (kind.equals("function")) {
			// no need to consume if anonymous
			consume(LEFT_PAREN, "Expected '(' before parameter list.");
		}
		List<Token> params = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				params.add(consume(IDENTIFIER, "Expected parameter name"));
			} while (match(COMMA));
		}
		consume(RIGHT_PAREN, "Expect ')' after function parameter list.");
		consume(ARROW, "Expected an '->' after function parameter list");

		Expression body = getBody();

		if (kind.equals("function")) {
			// close the call to defun, not necessary for anonymous function
			consume(RIGHT_PAREN, "Expected ')' to close defun call.");
		}

		return new Expression.Function(identifier, params, body);
	}

	private Expression getBody() {
//		if (match(LEFT_CURLY)) return block();
//		else if (check(LEFT_PAREN)) {
//			// arrow function style definition starts here for the parameters and body
//			// of the actual function
//			consume(LEFT_PAREN, "Expected '(' before parameter list.");
//			List<Token> params = new ArrayList<>();
//			if (!check(RIGHT_PAREN)) {
//				do {
//					params.add(consume(IDENTIFIER, "Expected parameter name"));
//				} while (match(COMMA));
//			}
//			consume(RIGHT_PAREN, "Expect ')' after function parameter list.");
//			consume(ARROW, "Expected an '->' after function parameter list");
//			return new Expression.Function(null, params, getBody());
//		}
//		else return ternaryExpression();
		return expression();
	}

	private Expression var() {
		consume(LEFT_PAREN, "Expect '(' after var definition call.");
		Token identifier = consume(IDENTIFIER, "Expect identifier as first argument to var definer.");

		Expression initialiser = null;

		if (check(COMMA)) {
			consume(COMMA, null);

//			if (check(LEFT_CURLY)) {
//				consume(LEFT_CURLY, "Expected start of expression block after identifier argument.");
//				initialiser = block();
//			} else initialiser = ternaryExpression();
			initialiser = expression();
		}

		consume(RIGHT_PAREN, "Expect ')' after var definition call arguments.");
		return new Expression.Var(identifier, initialiser);
	}

	private Expression assign(Token type) {
		Token identifier = null;
		if (type.type == ASSIGN) {
			consume(LEFT_PAREN, "Expect '(' after var assign call.");
			identifier = consume(IDENTIFIER, "Expect identifier as first argument to var assignment.");
			consume(COMMA, "Expect ',' after identifer");
		} else if (type.type == IDENTIFIER) { 
			identifier = type;
			consume(LEFT_ARROW, "Expected '<-' after identifer for assignment expression.");
		} else {
			error(type, "Unknown token type '" + type.lexeme + "'.");
		}

		Expression value = null;
		if (check(LEFT_CURLY)) {
			consume(LEFT_CURLY, "Expected start of expression block after identifier argument.");
			value = block();
		} else
			value = ternaryExpression();

		if (type.type == ASSIGN)
			consume(RIGHT_PAREN, "Expect ')' after var definition call arguments.");
		return new Expression.Assign(identifier, value);
	}
	
	private Expression assignArray(Token identifier) {
		Expression e = new Expression.Variable(identifier);
		// consume the [
		consume(LEFT_BRACKET, "Expected an '['.");
		
		// consume the index
		Expression index = expression();
		// - lexeme in identifier is used by the interpreter
		// - add the index to the literal instead for array assignment
		Token token = new Token(identifier.type, identifier.lexeme, index, identifier.line);
		// consume the ]
		consume(RIGHT_BRACKET, "Expected an ']' after index.");
		// consume the <-
		consume(LEFT_ARROW, "Expected a '<-' after index argument.");
		// consume the expression
		Expression value = expression();
		// return a new assignment
		return new Expression.Assign(token, value);
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
//			if (inMatchExpression) expression = new Expression.CasePattern(expression, operator, right);
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
			if (match(LEFT_PAREN))
				expression = finishCall(expression);
			else if (match(LEFT_BRACKET))
				expression = finishArrayCall(expression);
			else
				break;
		}
		return expression;
	}

	private Expression finishArrayCall(Expression expression) {
		Expression index = expression();
		Expression upperBound = null;

		if (match(COMMA)) {
			upperBound = expression();
		}
		Token closingBracket = consume(RIGHT_BRACKET, "Expected a ']' to close the array call.");
		
		// check for assignment call, consume if present
//		Token assignmentArrow = check(LEFT_ARROW) ? advance() : null;
//		Expression afterArrow = null;
		// create a new assignment expression and return that if 
		// assignment arrow is not null, it will need to contain the array call
		// to be assigned to
		// - pass the assignment arrow as the closing bracket if it is not null;
//		if (assignmentArrow != null) {
//			closingBracket = assignmentArrow;
//			// get the expression after the assignment arrow
//			afterArrow = expression();
//		}
		Expression arrayCall = new Expression.ArrayCall(expression, index, upperBound, closingBracket);;
		
		// if there was an assignment arrow, return an assignment expression
//		return new Expression.Assign(assignmentArrow, arrayCall)
		
		return arrayCall; 
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
	 * Grammar: primary -> NUMBER | STRING | "true" | "false" | "nil" | "("
	 * expression ")" ;
	 * 
	 * @return
	 * @throws Exception
	 */
	private Expression primary() throws ParseError {
		if (match(FALSE))
			return new Expression.Literal(false);
		if (match(TRUE))
			return new Expression.Literal(true);
		if (match(NULL))
			return new Expression.Literal(null);
		if (match(APOSTROPHE))
			return function("anonymous");
		if (match(LEFT_CURLY))
			return block();
		if (match(LEFT_BRACKET))
			return array();

		if (match(NUMBER, STRING))
			return new Expression.Literal(previous().literal);

		if (match(IDENTIFIER)) {
			if (check(LEFT_BRACKET)) {
				// peek ahead and look for the closing bracket, if what follows
				// is not an arrow, it is not an assignment to an array
				// - let it fall through for the call expression
				int i = 0;
				while (!checkAhead(RIGHT_BRACKET, i) && !isAtEnd()) i++;
				if (checkAhead(LEFT_ARROW, i + 1)) return assignArray(previous());
			}
			if (check(LEFT_ARROW)) return assign(previous());
			else return new Expression.Variable(previous());
		}

		if (match(LEFT_PAREN)) {
			// look for arrow
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

	private Expression array() {
		List<Expression> elements = new ArrayList<>();

		while (!check(RIGHT_BRACKET) && !isAtEnd()) {
			elements.add(expression());
			if (check(COMMA))
				consume(COMMA, null);
		}
		if (previous().type == COMMA)
			error(previous(), "Expected an expression after comma");
		Token rightBracket = consume(RIGHT_BRACKET, "Expect ']' after array declaration");

		return new Expression.Array(elements, rightBracket);
	}
}
