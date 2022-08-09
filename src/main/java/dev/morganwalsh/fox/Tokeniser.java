package dev.morganwalsh.fox;

import static dev.morganwalsh.fox.TokenType.AND;
import static dev.morganwalsh.fox.TokenType.APOSTROPHE;
import static dev.morganwalsh.fox.TokenType.ARROW;
import static dev.morganwalsh.fox.TokenType.ASSIGN;
import static dev.morganwalsh.fox.TokenType.BANG;
import static dev.morganwalsh.fox.TokenType.BANG_EQUAL;
import static dev.morganwalsh.fox.TokenType.COLON;
import static dev.morganwalsh.fox.TokenType.COMMA;
import static dev.morganwalsh.fox.TokenType.DEFUN;
import static dev.morganwalsh.fox.TokenType.DOT;
import static dev.morganwalsh.fox.TokenType.EOF;
import static dev.morganwalsh.fox.TokenType.EQUAL;
import static dev.morganwalsh.fox.TokenType.EQUAL_EQUAL;
import static dev.morganwalsh.fox.TokenType.FALSE;
import static dev.morganwalsh.fox.TokenType.FAT_ARROW;
import static dev.morganwalsh.fox.TokenType.GREATER;
import static dev.morganwalsh.fox.TokenType.GREATER_EQUAL;
import static dev.morganwalsh.fox.TokenType.IDENTIFIER;
import static dev.morganwalsh.fox.TokenType.IF;
import static dev.morganwalsh.fox.TokenType.IMPORT;
import static dev.morganwalsh.fox.TokenType.LEFT_BRACKET;
import static dev.morganwalsh.fox.TokenType.LEFT_CURLY;
import static dev.morganwalsh.fox.TokenType.LEFT_PAREN;
import static dev.morganwalsh.fox.TokenType.LESS;
import static dev.morganwalsh.fox.TokenType.LESS_EQUAL;
import static dev.morganwalsh.fox.TokenType.MINUS;
import static dev.morganwalsh.fox.TokenType.NULL;
import static dev.morganwalsh.fox.TokenType.NUMBER;
import static dev.morganwalsh.fox.TokenType.OR;
import static dev.morganwalsh.fox.TokenType.PLUS;
import static dev.morganwalsh.fox.TokenType.QUESTION_MARK;
import static dev.morganwalsh.fox.TokenType.RIGHT_BRACKET;
import static dev.morganwalsh.fox.TokenType.RIGHT_CURLY;
import static dev.morganwalsh.fox.TokenType.RIGHT_PAREN;
import static dev.morganwalsh.fox.TokenType.SLASH;
import static dev.morganwalsh.fox.TokenType.STAR;
import static dev.morganwalsh.fox.TokenType.STRING;
import static dev.morganwalsh.fox.TokenType.TRUE;
import static dev.morganwalsh.fox.TokenType.VAR;
import static dev.morganwalsh.fox.TokenType.WHILE;
import static dev.morganwalsh.fox.TokenType.WHERE;
import static dev.morganwalsh.fox.TokenType.MATCH;
import static dev.morganwalsh.fox.TokenType.PIPE;
import static dev.morganwalsh.fox.TokenType.UNDERSCORE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokeniser {

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		// type
		keywords.put("false", FALSE);
		keywords.put("true", TRUE);
		keywords.put("null", NULL);

		// logic
		keywords.put("and", AND);
		keywords.put("or", OR);
		keywords.put("if", IF);

		// variables and functions
		keywords.put("defun", DEFUN);
		keywords.put("var", VAR);
		keywords.put("assign", ASSIGN);

		// other
		keywords.put("while", WHILE);
		keywords.put("import", IMPORT);
//		keywords.put("where", WHERE); // not implemented
		keywords.put("match", MATCH);
		
		
	}

	/**
	 * The input string to be processed into tokens, the starting point of
	 * syntactical analysis.
	 */
	private String src;

	/**
	 * The output sequence of tokens.
	 */
	private final List<Token> tokens;

	/**
	 * The first character in the current lexeme being tokenised.
	 */
	private int start;

	/**
	 * The current character being pointed to by the tokeniser.
	 */
	private int current;

	/**
	 * The current source line.
	 */
	private int line;

	public Tokeniser(String src) {
		this.src = src;
		tokens = new ArrayList<>();
		start = 0;
		current = 0;
		line = 0;
	}

	/**
	 * Produces a flat sequence of tokens representing the tokenisers input.
	 * 
	 * @return
	 */
	public List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	/*
	 * Scans the next token, adding it to the <code>tokens</code> list.
	 */
	private void scanToken() {
		char c = advance();

		switch (c) {
		case '_':
			addToken(UNDERSCORE);
			break;
		case '|':
			addToken(PIPE);
			break;
		case '\'':
			addToken(APOSTROPHE);
			break;
		case '[':
			addToken(LEFT_BRACKET);
			break;
		case ']':
			addToken(RIGHT_BRACKET);
			break;
		case '(':
			addToken(LEFT_PAREN);
			break;
		case ')':
			addToken(RIGHT_PAREN);
			break;
		case '{':
			addToken(LEFT_CURLY);
			break;
		case '}':
			addToken(RIGHT_CURLY);
			break;
		case ',':
			addToken(COMMA);
			break;
		case '.':
			addToken(DOT);
			break;
		case '+':
			addToken(PLUS);
			break;
		case '-':
			addToken(match('>') ? ARROW : MINUS);
			break;
		case '*':
			addToken(STAR);
			break;
		case '?':
			addToken(QUESTION_MARK);
			break;
		case ':':
			addToken(COLON);
			break;
		case '!':
			addToken(match('=') ? BANG_EQUAL : BANG);
			break;
		case '=':
			if (match('>'))
				addToken(FAT_ARROW);
			else
				addToken(match('=') ? EQUAL_EQUAL : EQUAL);
			break;
		case '<':
			addToken(match('=') ? LESS_EQUAL : LESS);
			break;
		case '>':
			addToken(match('=') ? GREATER_EQUAL : GREATER);
			break;
		case '/':
			if (match('/'))
				singleLineComment();
			else if (match('*'))
				blockComment();
			else
				addToken(SLASH);
			break;
		case ' ':
		case '\r':
		case '\t':
			// Ignore any whitespace
			break;

		case '\n':
			line++;
			break;
		case '"':
			string();
			break;
		default:
			if (isDigit(c))
				number();
			else if (isAlpha(c))
				identifier();
			else
				Fox.error(line, "Unexpected character in input sequence.");
			break;
		}
	}

	/**
	 * Returns the character at the current position and increments the
	 * <code>current</code> counter.
	 * 
	 * @return
	 */
	private char advance() {
		return src.charAt(current++);
	}

	/**
	 * Returns true if the <code>current</code> pointer has reached the end of the
	 * src string.
	 * 
	 * @return
	 */
	private boolean isAtEnd() {
		return current >= src.length();
	}

	/**
	 * Returns the next token without advancing the current pointer.
	 * 
	 * This is known as a lookahead, one character of lookahead to be specific.
	 * 
	 * @return
	 */
	private char peek() {
		if (isAtEnd())
			return '\0';
		return src.charAt(current);
	}

	/**
	 * Two characters of lookahead.
	 * 
	 * @return
	 */
	private char peekNext() {
		if (current + 1 >= src.length())
			return '\0';
		return src.charAt(current + 1);
	}

	/**
	 * A conditional advance() where the current character is only consumed if it is
	 * the one we are looking for.
	 * 
	 * This is doing a lookahead, similar to peek(), but here it is done to make
	 * sure it is ok to increment the current variable.
	 * 
	 * @param expected
	 * @return
	 */
	private boolean match(char expected) {
		if (isAtEnd())
			return false;
		if (src.charAt(current) != expected)
			return false;

		current++;
		return true;
	}

	/**
	 * Adds a new token representing the given type to the output sequence.
	 * 
	 * @param type
	 */
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	/**
	 * Adds a new token representing the given type and its value to the output
	 * sequence.
	 * 
	 * @param type
	 * @param literal
	 */
	private void addToken(TokenType type, Object literal) {
		String text = src.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	/**
	 * Returns true if the input character is an upper- or lower-case letter between
	 * 'a' and 'z, or an underscore character.
	 * 
	 * @param c
	 * @return
	 */
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	/**
	 * Returns true if the input character is a number between 0 and 9 (inclusive).
	 * 
	 * @param c
	 * @return
	 */
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	/**
	 * Returns true if one of the following conditions is true:
	 * 
	 * - the input character is an upper- or lower-case letter between 'a' and 'z,
	 * or an underscore character. - the input character is a number between 0 and 9
	 * (inclusive).
	 * 
	 * @param c
	 * @return
	 */
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	/**
	 * Adds a new token to the output sequence representing the scanned identifier
	 * and its value.
	 */
	private void identifier() {
		while (isAlphaNumeric(peek()))
			advance();

		// decide if token is keyword or just an identifier
		String text = src.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null)
			type = IDENTIFIER;
		addToken(type);
	}

	/**
	 * Adds a new token to the output sequence representing the scanned number and
	 * its value.
	 */
	private void number() {
		while (isDigit(peek()))
			advance();

		// Look for a fractional part.
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();

			while (isDigit(peek()))
				advance();
		}

		addToken(NUMBER, Double.parseDouble(src.substring(start, current)));
	}

	/**
	 * Adds a new token to the output sequence representing the scanned string and
	 * its value.
	 */
	private void string() {
		// If lox supported it, escape characters in strings
		// would be unescaped here
		// - Fox supports some now :D
		int currentCharacter = 1; // current char of the string
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\\') {				
				String uptoSlash = src.substring(0, current);
				String afterSlash = src.substring(current + 1, src.length());
				String afterEscapedCharacter = afterSlash.substring(1);
				
				char next = peekNext();
				if (next == '"' || next == '\\') src = uptoSlash + afterSlash;
				else if (next == 'n') src = uptoSlash + '\n' + afterEscapedCharacter;
				else if (next == 't') src = uptoSlash + '\t' + afterEscapedCharacter;
				else if (next == 'b') src = uptoSlash + '\b' + afterEscapedCharacter;
				else if (next == 'r') src = uptoSlash + '\r' + afterEscapedCharacter;
				else if (next == 'f') src = uptoSlash + '\f' + afterEscapedCharacter;
				else Fox.error(line, "Invalid escape sequence in supplied string at character '" + currentCharacter + "'.");
			}
			// newline in the string, not an escape character typed by the user
			// but one detected from the source itself
			if (peek() == '\n')
				line++;
			advance();
			currentCharacter++;
		}

		if (isAtEnd()) {
			Fox.error(line, "Unterminated string.");
			return;
		}

		// The closing ".
		advance();

		// Trim the surrounding quotes.
		String value = src.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	private void singleLineComment() {
		// A comment goes until the end of the line.
		// - peek() is used as a newline means we need to
		// increment the line number variable
		while (peek() != '\n' && !isAtEnd())
			advance();
	}

	private void blockComment() {
		boolean hadError = false;
		// block comment
		// iterate until what looks like the end of the comment is reached or the end of
		// the source is met
		while (peek() != '*' && !isAtEnd()) {
			if (advance() == '\n')
				line++;
		}
		if (isAtEnd())
			hadError = true;
		else {
			advance(); // we know its a star next if we got here
			if (isAtEnd())
				hadError = true; // quick check before advancing
			else {
				char slash = advance();
				if (slash != '/')
					hadError = true;
			}
		}
		if (hadError)
			Fox.error(line, "Block comment not terminated");
	}

}
