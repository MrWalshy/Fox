package dev.morganwalsh.fox.native_functions;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class TypeOf implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg = arguments.get(0);
		
		if (arg == null) return "null";
		if (arg instanceof FoxCallable) return "function";
		if (arg instanceof String) {
			try {
				if (arg.equals("true") || arg.equals("false")) {
					return "boolean";
				}
				Double.valueOf(arg.toString());
				return "number";
			} catch (Exception e) {
				return "string";
			}
		}
		if (arg instanceof Double) return "number";
		if (arg instanceof Object[]) return "array";
		if (arg instanceof Boolean) return "boolean";

		throw new RuntimeError(closingParenthesis, "Not a valid type");
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
