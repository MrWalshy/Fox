package dev.morganwalsh.fox.native_functions.array;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class Merge implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arr1 = arguments.get(0);
		Object arr2 = arguments.get(1);
		
		String error = "%s was not an array.";
		if (!(arr1 instanceof Object[])) {
			throw new RuntimeError(closingParenthesis, String.format(error, "arg1"));
		} else if (!(arr2 instanceof Object[])) {
			throw new RuntimeError(closingParenthesis, String.format(error, "arg2"));
		}
		Object[] array1 = (Object[]) arr1;
		Object[] array2 = (Object[]) arr2;
		Object[] merged = new Object[array1.length + array2.length];
		
		System.arraycopy(array1, 0, merged, 0, array1.length);
		System.arraycopy(array2, 0, merged, array1.length, array2.length);
		
		return merged;
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
