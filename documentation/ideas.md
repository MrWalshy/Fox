# Ideas

## Built-in http servers

```
// pseuodocode
var(homePage, "<h1>Hello world</h1>")
var(server, createServer("localhost", 8080))
server('() -> {
  addRoute("GET", "/", '(request, response) -> {
    logRequest()
    var(headers, defaultHttpHeaderSet("GET"))
    var(body, homePage)
    sendResponse(200, headers, body)
  })
})
```

## Looping

### Infinite loop

```
loop(print("Hello"))
```

`loop` accepts an expression

```
loop({
  printMenu()
  var(in, input())
  executeIn(in)
})
```

`loop` can accept a block of expressions

- use a `break()` function to exit the loop expression

### While loop

```
var(i, 1)
loop(i <= 10, {
  print(i)
  assign(i, i+1)
})
```

### For loop

```
for(var(i, 1), i <= 10, {
  print(i)
}, assign(i, i+1))
```

- `for` defines a scope that also includes `i`, meaning `i` is only accessible in the for loop itself

#### Nested

```
for(var(unit, 1), unit <= 10, {
  for(var(multiple, 1), multiple <= 10, {
    print(unit + " * " + multiple + " = " + (unit * multiple))
  })
}, assign(i, i+1))
```