package dev.morganwalsh.fox;

public enum TokenType {

	// Single-character tokens.
	LEFT_PAREN, RIGHT_PAREN, COMMA, MINUS, PLUS, SLASH, STAR, QUESTION_MARK, COLON, DOT, LEFT_CURLY, RIGHT_CURLY,
	
	// One or two character tokens.
	BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, TERNARY, ARROW,
		
	// Literals.
	IDENTIFIER, STRING, NUMBER,
	
	// Keywords.
	AND, FALSE, DEFUN, WHILE, NULL, OR, PRINT, TRUE, VAR, IF, ASSIGN, IMPORT,

	EOF
}
