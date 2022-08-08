package dev.morganwalsh.fox;

import java.util.List;

public interface FoxCallable {

	Object call(Interpreter interpreter, List<Object> arguments, Token closingParenthesis);

	int arity();
}
