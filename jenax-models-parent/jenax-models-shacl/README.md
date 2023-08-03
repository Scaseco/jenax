A module for representing prefix definitions in RDF using the shacl vocabulary.
Topraid shacl as of 1.4.2 lacks those classes.

```java
// Any RDF resource can be viewed using the "HasPrefixes" class
HasPrefixes hasPrefixes = ModelFactory.createDefaultModel().createResource().as(HasPrefixes.class);

// Put an item
prefixes.put("rdf", RDF.getURI());

// Set view
Set<PrefixDeclaration> set = hasPrefixes.getPrefixes();

// Map view
Map<String, String> map = hasPrefixes.getMap();
```

Example Usage:

```turtle
PREFIX sh: <http://www.w3.org/ns/shacl#>
PREFIX rr: <http://www.w3.org/ns/r2rml#>
PREFIX eg: <http://www.example.org/>

## r2rml namespace declaration
eg:rr sh:declare [ sh:prefix "rr" ; sh:namespace <http://www.w3.org/ns/r2rml#>  ] .

eg:ObjectMapSparqlTarget
  a sh:SPARQLTarget ;
  sh:prefixes eg:rr ;
  sh:select "SELECT ?this { [] rr:objectMap ?this FILTER NOT EXISTS { ?this rr:parentTriplesMap [] } }" ;
  .
```
  
