package dev.morganwalsh.fox.native_functions.array;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Array implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		if (arguments.get(0) instanceof Double) {
			Double arg = (Double) arguments.get(0);
			return new Object[arg.intValue()];
		}
		throw new RuntimeError(closingParenthesis, "Supplied argument '" + arguments.get(0) + "' was not a number representing the desired capacity.");
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
