package dev.morganwalsh.fox.native_functions;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Length implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg = arguments.get(0);
		if (arg instanceof String) {
			return ((double) ((String) arg).length());
		} else if (arg instanceof Object[]) {
			return ((double) ((Object[]) arg).length);
		}
		throw new RuntimeError(closingParenthesis, "'" + arguments.get(0).toString() + "' was not a string. Can only retrieve length of strings.");
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
