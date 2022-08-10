package dev.morganwalsh.fox.native_functions.string;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Split implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg1 = arguments.get(0);
		Object regex = arguments.get(1);
		
		if (arg1 == null || regex == null) {
			throw new RuntimeError(closingParenthesis, "Arguments must not be null.");
		} else if (!(arg1 instanceof String) || !(regex instanceof String)) {
			throw new RuntimeError(closingParenthesis, "Arguments must be strings.");
		}
		return arg1.toString().split(regex.toString());
	}

	@Override
	public int arity() {
		return 2;
	}

	@Override
	public String toString() {
		return "<native fn>";
	}
}
