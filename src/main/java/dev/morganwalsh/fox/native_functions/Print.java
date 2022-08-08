package dev.morganwalsh.fox.native_functions;

import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.Token;

public class Print implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		Object arg = arguments.get(0);
		System.out.println(arg.toString());
		return arg.toString();
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
