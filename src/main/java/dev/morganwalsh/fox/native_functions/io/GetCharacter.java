package dev.morganwalsh.fox.native_functions.io;

import java.util.List;

import dev.morganwalsh.fox.Fox;
import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.Token;

public class GetCharacter implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		double input = (double) Fox.sc.next().charAt(0);
		return input;
	}

	@Override
	public int arity() {
		return 0;
	}

	@Override
	public String toString() {
		return "<native fn>";
	}
}
