package dev.morganwalsh.fox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Fox {
	
	private static final Interpreter INTERPRETER = new Interpreter();
	static boolean hadError;
	static boolean hadRuntimeError;

	public static void main(String[] args) throws IOException {
//		if (args.length > 1) {
//			System.out.println("Usage: jlox [script]");
//			System.exit(64);
//		} else if (args.length == 1) {
//			runFile(args[0]);
//		} else {
//			runPrompt();
//		}
		runFile("src/main/resources/script.fox");
		
//		String src = "// define a function\r\n"
//				+ "defun(h1, (text) ->\r\n"
//				+ "  (\"<h1>\" + text + \"</h1>\")\r\n"
//				+ ")\r\n"
//				+ "\r\n"
//				+ "// shortcut\r\n"
//				+ "var capturedH2 = defun(h2, (text) -> \"<h2>\" + text + \"</h2>\")\r\n"
//				+ "\r\n"
//				+ "var(title, h1(\"Hello World\"))\r\n"
//				+ "\r\n"
//				+ "if((true), print(title), print(h2(\"Subtitle\")))";
//		run(src);
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));
		
		if (hadError) System.exit(65);
		if (hadRuntimeError) System.exit(70);
	}
	
	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (;;) {
			System.out.print("> ");
			String line = reader.readLine();
			if (line == null)
				break;
			run(line);
			hadError = false;
		}
	}

	private static void run(String src) {
		// Lexical analysis
		Tokeniser tokeniser = new Tokeniser(src);
		List<Token> tokens = tokeniser.scanTokens();
		
		// Syntactical analysis
		Parser parser = new Parser(tokens);
		List<Expression> expressions = parser.parse();
		
		if (hadError) return;
		
		// Static analysis
		Resolver resolver = new Resolver(INTERPRETER);
		resolver.resolve(expressions);
		
		if (hadError) return;
		
		// interpretation (execution)
		if (expressions.size() == 1) {
			System.out.println(INTERPRETER.interpret(expressions.get(0)));
		} else {
			INTERPRETER.interpret(expressions);
		}
	}
	
	public static Object evaluate(String src) {
		Interpreter interpreter = new Interpreter();
		Tokeniser tokeniser = new Tokeniser(src);
		List<Token> tokens = tokeniser.scanTokens();
		
		Parser parser = new Parser(tokens);
		List<Expression> expressions = parser.parse();
		
		Resolver resolver = new Resolver(interpreter);
		resolver.resolve(expressions);
		
		for (int i = 0; i < expressions.size() - 1; i++) {
			interpreter.interpret(expressions.get(i));
		}
		return interpreter.interpret(expressions.get(expressions.size() - 1));
	}
	
	static void error(int line, String message) {
		report(line, "", message);
	}

	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

	static void error(Token token, String message) {
		report(token.line, " at '" + token.lexeme + "'", message);
	}

	static void runtimeError(RuntimeError error) {
		System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}
}
