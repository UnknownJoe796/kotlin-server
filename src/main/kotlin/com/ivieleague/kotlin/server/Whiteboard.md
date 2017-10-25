# Whiteboard

## Cascading calls for SQL



## URL Action Enumeration

All potential actions are enumerated and available via the `multi` endpoint.

This allows for the same flexibility that GraphQL has.

For example, say I had a non-trivial SQL query that I wanted results from that just didn't quite fit KQL.

```
"notes/complex-query" to { input -> ... }
"notes/get" to <default KQL fetch>
"notes/query" to <default KQL query>
"notes/update" to <default KQL update>
"notes/delete" to <default KQL delete>
```

Now I could perform multiple operations within the `multi` like so:

```json
[
  {
    "url":"notes/",
    "method": "POST",
    "data":{},
    "out":"first"
  },
  {
    "url":"notes/complex-query",
    "method":"POST",
    "data":{
      "id": "${first._id}"
    }
  }
]
```

Is adherence to REST a good thing?