package dev.morganwalsh.fox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Fox {
	
	private static final Interpreter INTERPRETER = new Interpreter();
	static boolean hadError;
	static boolean hadRuntimeError;
	public static Scanner sc = new Scanner(System.in);
	
	private static String workingDirectory;
	private static Path launchScriptLocation;
	static Path launchScriptDirectory;
	static Path currentExecutionDirectory;

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String path) throws IOException {
		// for the resolver to resolve relative links
		workingDirectory = System.getProperty("user.dir");
		launchScriptLocation = Path.of(workingDirectory, "\\", path);
		launchScriptDirectory = launchScriptLocation.getParent();
		currentExecutionDirectory = launchScriptDirectory;
		
		byte[] bytes = Files.readAllBytes(launchScriptLocation);
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
			String output = run(line);
			System.out.println("\n" + line + " ===> " + output);
			hadError = false;
		}
	}

	private static String run(String src) {
		// Lexical analysis
		Tokeniser tokeniser = new Tokeniser(src);
		List<Token> tokens = tokeniser.scanTokens();
		
		// Syntactical analysis
		Parser parser = new Parser(tokens);
		List<Expression> expressions = parser.parse();
		
		if (hadError) return null;
		
		// Static analysis
		Resolver resolver = new Resolver(INTERPRETER);
		resolver.resolve(expressions);
		
		if (hadError) return null;
		
		return INTERPRETER.interpret(expressions);
	}
	
	public static Object evaluate(String src) {
		Tokeniser tokeniser = new Tokeniser(src);
		List<Token> tokens = tokeniser.scanTokens();
		
		Parser parser = new Parser(tokens);
		List<Expression> expressions = parser.parse();
		
		Resolver resolver = new Resolver(INTERPRETER);
		resolver.resolve(expressions);
		
		for (int i = 0; i < expressions.size() - 1; i++) {
			INTERPRETER.interpret(expressions.get(i));
		}
		return INTERPRETER.interpret(expressions.get(expressions.size() - 1));
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
