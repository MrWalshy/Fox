# Ideas

## Features for arrays

A negative indices corresponding to reverse access of the array, like in Python:

```
var(arr, [1,2,3])
print(arr[-1]) // 3
```

Adding to arrays using a library function instead of with merge directly:

```
defun(addTo, (arr, value) -> merge(arr, [value]))

var(arr1, [1,2,3]) // [1,2,3]
var(arr2, addTo([], 4)) // [4]
var(arr3, merge(arr1, arr2)) // [1,2,3,4]
```

A spread syntax for merging arrays:

```
var(arr1, [1, 2, 3])
var(arr2, [...arr1, 4, 5, 6]) // 1,2,3,4,5,6
```

### Functional programming features

filter:

```
filter(arr, '(e) -> e > 5)

filter([1,2,5,6,7]) // [6, 7]
```

## Maps (key:value pairings)

```
var(map, {
  key <- value,
  key2 <- value
})
```

## Alternate assignment syntax

```
var(a,10)
a <- 22
print(a) // 22
```

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

foreach loop:

```
foreach(element in iterable, {

})
```