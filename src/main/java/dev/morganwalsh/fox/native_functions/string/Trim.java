package dev.morganwalsh.fox.native_functions.string;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Trim implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg1 = arguments.get(0);
		
		if (!(arg1 instanceof String)) {
			throw new RuntimeError(closingParenthesis, "Can only trim strings");
		}
		return arg1.toString().trim();
	}

	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return 1;
	}

}
