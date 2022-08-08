package dev.morganwalsh.fox.native_functions.io;

import java.nio.charset.Charset;
import java.util.List;

import dev.morganwalsh.fox.FoxCallable;
import dev.morganwalsh.fox.Interpreter;
import dev.morganwalsh.fox.RuntimeError;
import dev.morganwalsh.fox.Token;

public class CharacterToString implements FoxCallable {

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis) {
		if (!(arguments.get(0) instanceof Double)) {
			throw new RuntimeError(closingParenthesis, "Expected a valid numerical character representation.");
		}
		return String.valueOf((char) ((double)arguments.get(0)));
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
