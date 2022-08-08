package dev.morganwalsh.fox.native_functions;

import java.util.List;

import dev.morganwalsh.fox.Fox;
import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Evaluate implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg = arguments.get(0);
		if (arg instanceof String) {
			return Fox.evaluate((String)arg);
		}
		throw new RuntimeError(closingParenthesis, "Value passed to evaluate must be a string");
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
