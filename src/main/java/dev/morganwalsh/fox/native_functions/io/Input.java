package dev.morganwalsh.fox.native_functions.io;

import java.util.List;
import java.util.Scanner;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.Token;

public class Input implements FoxCallable {

	private final static Scanner sc = new Scanner(System.in);
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		System.out.print(arguments.get(0).toString());
		return sc.nextLine();
	}

	@Override
	public int arity() {
		return 1;
	}

	@Override
	public String toString() {
		return "<native fn>";
	}
}
