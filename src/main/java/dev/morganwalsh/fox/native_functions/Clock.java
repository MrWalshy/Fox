package dev.morganwalsh.fox.native_functions;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.Token;

public class Clock implements FoxCallable {
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		return (double) System.currentTimeMillis() / 1000.0;
	}

	@Override
	public int arity() {
		return 0;
	}

	@Override
	public String toString() {
		return "<native fn>";
	}
}
