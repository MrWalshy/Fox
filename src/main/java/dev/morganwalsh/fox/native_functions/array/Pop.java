package dev.morganwalsh.fox.native_functions.array;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Pop implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		if (!(arguments.get(0) instanceof Object[])) {
			throw new RuntimeError(closingParenthesis, "Cannot pop value from non-array type.");
		}
		Object[] arr = (Object[]) arguments.get(0);
		
		if (arr.length > 0) {
			
		}
		return null;
	}

	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String toString() {
		return "<native fn>";
	}
}
