---
title: Path Finding Extensions
parent: RDF/SPARQL Plugins
has_children: false
nav_order: 20
---

## PathFinding
The property function `norse:simplePaths` matches up to `?n` paths between the RDF terms `?from` and `?to` w.r.t. a property path expression string `?pathExprStr`.

## Example Data

```ttl
# data.ttl
PREFIX eg: <http://www.example.org/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>

#INSERT DATA {
eg:anne foaf:knows eg:x .
eg:y foaf:knows eg:x .
eg:bob foaf:knows eg:y .
#}
```

## SPARQL Usage

```sparql
PREFIX eg: <http://www.example.org/>
PREFIX norse: <https://w3id.org/aksw/norse#>

SELECT ?path {
  VALUES (?from ?to ?pathExprStr ?n) {
    (eg:anne eg:bob "(!rdf:type|^!rdf:type)+" 100)
  }

  ?from norse:simplePaths(?pathExprStr ?path ?to ?n) .

  # 
  BIND(norse:array.last(?path) AS ?target) # should be eg:bob
}
```

The query above yields the following result for `data.ttl` below:

```
"<http://www.example.org/anne> <http://xmlns.com/foaf/0.1/knows> true <http://www.example.org/x> <http://xmlns.com/foaf/0.1/knows> false <http://www.example.org/y> <http://xmlns.com/foaf/0.1/knows> false <http://www.example.org/bob>"^^norse:array
```

Paths are encoded as `norse:array`s of pattern `startNode (property isForward reachedNode)*`.
A path thus always has at least one startNode followed by zero-or-more "steps". A step is always three consecutive array elements and holds: the traversed property, the direction (forwards=true, backwards=false) and the reached node.

## Working with Paths
As paths are encoded as `norse:array`s, norse's set of functions for arrays can be applied.

### Extract the Final Target Node of a Path

Use `array.last`: `BIND(norse:array.last(?path) AS ?target)`

### Unnesting Path

The steps that need to be taken are:
* Obtain the length of a path using `norse:array.size`.
* Generate a sequence of numbers to access the steps. This can be accomplished using `norse:number:range` which generates numbers similar to a for-loop.
* Extract the array elements of each step using `norse:array.get`.


```sparql
PREFIX eg: <http://www.example.org/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX norse: <https://w3id.org/aksw/norse#>

SELECT ?idx ?property ?direction ?reachedNode
  VALUES (?from ?to ?pathExprStr ?n) {
    (eg:anne eg:bob "(!rdf:type|^!rdf:type)+" 100)
  }

  ?from norse:simplePaths(?pathExprStr ?path ?to ?n) .

  BIND(norse:array.size(?path) AS ?pathLen)
  (1 ?pathLen 3) norse:number.range ?i # for (?i = 1; i < ?pathLen; ?i +=3) { ... }
  BIND(xsd:int((?i - 1) / 3) AS ?idx)
  BIND(norse:array.get(?path, ?i + 0) AS ?property)
  BIND(norse:array.get(?path, ?i + 1) AS ?direction)
  BIND(norse:array.get(?path, ?i + 2) AS ?reachedNode)
}

```

Result:
```txt
------------------------------------------------------------------------------------------------------------------------------
| idx                                         | property                          | direction | reachedNode                  |
==============================================================================================================================
| "0"^^<http://www.w3.org/2001/XMLSchema#int> | <http://xmlns.com/foaf/0.1/knows> | true      | <http://www.example.org/x>   |
| "1"^^<http://www.w3.org/2001/XMLSchema#int> | <http://xmlns.com/foaf/0.1/knows> | false     | <http://www.example.org/y>   |
| "2"^^<http://www.w3.org/2001/XMLSchema#int> | <http://xmlns.com/foaf/0.1/knows> | false     | <http://www.example.org/bob> |
------------------------------------------------------------------------------------------------------------------------------
```


