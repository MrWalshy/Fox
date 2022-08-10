package dev.morganwalsh.fox.native_functions.utils;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Number implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg = arguments.get(0);
		
		try {
			return Double.valueOf(arg.toString());
		} catch (NumberFormatException nfe) {
			throw new RuntimeError(closingParenthesis, "Cannot parse '" + arg.toString() + "' as a number.");
		}
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
