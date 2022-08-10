package dev.morganwalsh.fox.native_functions.string;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Replace implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg1 = arguments.get(0);
		Object regex = arguments.get(1);
		Object replacement = arguments.get(2);
		
		if (arg1 == null || regex == null) {
			throw new RuntimeError(closingParenthesis, "String and regex must not be null.");
		} else if (!(arg1 instanceof String) || !(regex instanceof String)) {
			throw new RuntimeError(closingParenthesis, "Arguments must be strings.");
		}
		
		try {
			return arg1.toString().replaceAll(regex.toString(), replacement == null ? null : replacement.toString());
		} catch (PatternSyntaxException e) {
			throw new RuntimeError(closingParenthesis, "Invalid regex supplied: " + e.getMessage());
		}
	}

	@Override
	public int arity() {
		return 3;
	}

	@Override
	public String toString() {
		return "<native fn>";
	}
}
