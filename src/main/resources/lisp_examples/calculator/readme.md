// Original: http://norvig.com/lispy.html
// - I've translated norvigs version of Lispy calculator into Fox

// Symbol      -> string
// Number      -> number
// Atom        -> Symbol | Number
// List        -> List
// Expression  -> (Atom, List)
// Environment -> Array

// Symbol is interpreted as a variable name, where
// its value is the variable's value. x => "hello"

// Number evaluates to itself, 12 => 12

// Conditional
// - (if test then else)
// - Evaluates test. If true, evaluates and returns then,
//   otherwise evaluates and returns else
// - (if (> 10 1) ("10 is definitely greater than one") ("Oof"))

// Definition
// - (define symbol expression)
// - Defines a new variable named symbol and assigns
//   it the value of evaluating expression.

// Procedure call
// - (call arg...)
// - If call is anything other than if, define or quote,
//   it is treated as a procedure. First, the call is 
//   evaluated, then the arg's. Then the procedure itself 
//   (the result of evaluating call) is applied to the list of 
//   arg values.

// var(program, "(begin (define r 10) (* pi ( * r r)))")
// eval(parse(program))

//var(tokens, tokenise("(* 10 (+ 5 5))")) // tokeniser ok
//var(ast, parse(tokens)) // ast good

// tokens produced:
// [
//   [symbol, *], 
//   [number, 10], 
//   [
//     [symbol, +], 
//     [number, 5], 
//     [number, 5]
//   ]
// ]

//var(env, standardEnvironment(null))
//printArr(ast)
//println("")
//printArr(env)
//println("")

//var(result, eval(ast, env))
//println(result)

// Infix = prefix = postfix
// except for actual examples
// a + b = (+ a b) = (a b +)
// 1 + 1 = (+ 1 1) = 2

// (a + b) * c = (* (+ a b) c) = ((a b +) c *)
// (1 + 2) * 3 = (* (+ 1 2) 3) = 9

// a * (b + c) = (* a (+ b c)) = (a (b c +) *)
// 1 * (2 + 3) = (* 1 (+ 2 3)) = 5

// a / b + c / d = (+ (/ a b) (/ c d)) = ((a b /) (c d /) +)
// 1 / 2 + 3 / 4 = (+ (/ 1 2) (/ 3 4)) = 1.25