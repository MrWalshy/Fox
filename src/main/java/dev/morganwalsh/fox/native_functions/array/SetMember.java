package dev.morganwalsh.fox.native_functions.array;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class SetMember implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object array = arguments.get(0);
		Object index = arguments.get(1);
		Object value = arguments.get(2);
		if (!(array instanceof Object[])) {
			throw new RuntimeError(closingParenthesis, "Can only set an element in an array");
		} else if (!(index instanceof Double)) {
			throw new RuntimeError(closingParenthesis, "Second argument must be a number.");
		}
		Object[] arr = (Object[]) array;
		int i = ((Double) index).intValue();
		
		if (i < 0 || i >= arr.length) {
			throw new RuntimeError(closingParenthesis, "Array index out of bounds '" + i + "'.");
		}
		arr[i] = value;
		return arr;
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
