# Ideas

## Built-in http servers

```
// pseuodocode
var(homePage, "<h1>Hello world</h1>")
var(server, createServer("localhost", 8080))
server('() -> {
  addRoute("GET", "/", '(response, request) -> {
    logRequest()
    homePage
  })
})
```