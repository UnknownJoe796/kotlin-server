# KQL Design

## Goals

- Strictly formatted
- Uses HTTP and JSON
- Clear results
- Clear security
- Sockets
- Mutations are queries - you can't get information you don't have without running another query
- Multi-instruction - You can put multiple instructions in a single HTTP request

## Wrapper Object

```json
{
  "request": "GET/QUERY/POST/PATCH/DELETE",
  "table": "TableName",
  "id": "asdfjia-ofe8932hf-u9sd",
  "content": { "comment": "Actual request data goes here" }
}
```

#### Query

`POST rest/table-name/query`

```json
{
  "condition": {
    "type": "All",
    "conditions": [
      {"type": "Equal", "value":"title", "equals":"My Note Title" },
      {"type": "NotEqual", "value":"content", "doesNotEqual": ""}
    ]
  },
  "output":{
    "title": true,
    "content": true
  }
}
```

```json
[
  {
    "id": "asd9fd0sfj3",
    "title": "My Note Title",
    "content": "This isn't empty."
  },
  {
    "id": "389274hfsdd",
    "title": "My Note Title",
    "content": "This is another note."
  }
]
```

#### Get

`POST rest/table-name/<id>/get`

```json
{
  "title": true,
  "content": true,
  "related": {
    "title": true
  }
}
```

```json
{
  "id": "fd8f83",
  "title": "Note Testing",
  "content": "I'm testing a bunch of notes here.",
  "related": [
    { 
      "id": "asd9fd0sfj3",
      "title": "My Note Title"
    },
    {
      "id": "389274hfsdd",
      "title": "My Note Title",
    }
  ]
}
```

#### Create New

`POST rest/table-name`

```json
{
  "title": "New Note",
  "content": "I'm changing the content",
  "related": [
    {"title": "Related", "Content": "A new related note"},
    {"id": "jasdkfj3"}
  ]
}
```

#### Update

`PATCH rest/table-name/<id>`

```json
{
  "content": "I'm changing the content",
  "+related": [
    {"title": "Related", "Content": "A new related note"},
    {"id": "jasdkfj3"}
  ],
  "-related": [
    {"id": "f8j3jadk"}
  ]
}
```

```json
{
  "content": "I'm changing the content",
  "related": [
    {"title": "Related", "Content": "A new related note"},
    {"id": "jasdkfj3"}
  ]
}
```