## GraphQL to SPARQL Rewriter


### Features

* Fast and streaming (for top-level fields)
* Vendor-independent: The generated SPARQL queries can run on any SPARQL 1.1 \* endpoint.
* Self-contained queries: Use the `@rdf` and `@sparql` directives to unambiguously map the GraphQL fields to data in your SPARQL endpoint.
    * No need for expensive schema generation or data summarization
    * No need to manage additional mapping files
* Auto-mapping of fields to classes and properties via VoID and SHACL is available. However, be aware that this can take a while on large data.


\* The generated SPARQL query makes use of the LATERAL feature, however this can be polyfilled at the cost of multiple requests with jenax SPARQL polyfills in jenax-dataaccess

## Querying

The core machinery is based on the `@rdf` and `@sparql` directives.

In general, we differ between `top-level fields`, which specify sets of items and `inner fields` which correspond to properties of these items. Inner fields are inherently backed by the set of items in the RDF graph that are reached by traversing paths of RDF properties based on a field's annotation.
For top-level fields the initial set of items needs to be specified.

All fields can be annotated with an IRI using the `@rdf(iri: "...")` directive.

```GraphQl
{ myField @rdf(iri: "http://www.example.org/myIri") ... }
```

#### Selecting the Initial Set of Items

The initial set of items is conceptually specified using a SPARQL SELECT query that projects only a single variable.
An explicit SPARQL query can be specified using the `@sparql(fragement="...")` directive:
```graphql
{
  People @sparql(fragment="SELECT ?s WHERE { ?s a <http://xmlns.com/foaf/0.1/Person> })" {
    ...
  }
}
```

The annotation `@sparql(fragment="")` is generally used to state that the values of a field should be intersected with those of the supplied fragment. If the set of items is not otherwise specified, then the sparql fragment becomes its specification. Query fragments are independent so processors need to ensure that no variable name clashes arise when assembling a GraphQL's effective SPARQL query.


Providing multiple sparql fragments results in the intersection of the resources.
```graphql
{
  HumanLeaders
    @sparql(fragment="SELECT ?s WHERE { ?s a <http://xmlns.com/foaf/0.1/Person> })"
    @sparql(fragment="SELECT ?x WHERE { ?leader <https://dbpedia.org/ontology/leaderName> ?leader })" {
    ...
  }
}
```

A common use case is to select instances of given class.
The `@class` directive is a shorthand for which uses the field's IRI to produce a SPARQL fragement `SELECT ?x WHERE { ?x a $FIELD_IRI$ }`.

```graphql
{
  People @class @rdf(iri="http://xmlns.com/foaf/0.1/Person") {
    ...
  }
}
```

NOTE: All `@rdf` directives are always processed first. Afterwards, `@sparql` and its shorthands are evaluated in the specified order.



The `base` argument of the `@rdf` sets a base namespace for the annotated field and is valid for all descendant fields unless overridden.




Top level fields of a query document are by default interpreted as class names, whereas all inner fields are treated as properties.


For example, the Pokedex dataset has the class `Pokemon` (`http://pokedex.dataincubator.org/pkm/Pokemon`) whose instances carry the `rdfs:label` property.
Using auto-mapping, the following query is fully functional:
```graphql
{
  Pokemon {
    label
  }
}
```

#### Aliases vs @as
GraphQL allows declaration of aliases for a field. However, result values for that field will use the renamed field.

`@as(name="alias")` is used to specify a name by which a nested field can be referenced.
The `@as` directive is used when resolving variables in filter conditions. It does not cause any change in the resulting document structure.


`@rdf(ns="")` is always applied to the field name and never to the alias as the following example shows:

```graphql
{
  foo @rdf(base="http://www.example.org/") {
    bar     # -> http://www.example.org/bar
    baz:bar # -> http://www.example.org/bar
  }
}
```

`alias:fieldName` 

Find all people who have friends with the same firstName.
```
{
  Person @class @rdf(base: "http://xmlns.com/foaf/0.1/Person") {
    knows { firstName }
    b: knows { firstName }
  }
}
```

```sparql
?s foaf:knows ?a
?s foaf:knows ?b
```



#### Inverse Relation
The `@inverse` directive is used to traverse a field's underlying RDF property in reverse direction.

```graphql
{
  Pokemon {
    label,
    speciesOf @inverse {
      label
    } 
  }
}
```

#### Pagination
Each field can carry `limit` and `offset` arguments which controls how many underlying RDF nodes to match:


```graphql
{
  Pokemon(limit: 10, offset: 5) {
    label
  }
}
```

```graphql
{
  Pokemon(limit: 10, offset: 5) {
    label,
    colour,
    speciesOf(limit: 1) @inverse {
      label
    }
  }
}
```

#### Namespace Prefixes
The `@rdf` directive is used to supply different aspects of RDF namespace information for fields.
The directive immediately takes effect on the field it appears on.

* Aspects that affect the annotated field and all descendents:
    * `prefixes`: An object value with a mapping of prefix names to namespace IRIs.
    * `base`: A string that will be prepended to all fields *not* annotated with `@rdf` in order to form IRIs. The value of `base` will be expanded against the provided namespaces before prepeding the field name.
* Aspects that only affect the annotated field:
    * `ns`:  A string that will be prepended to the field name
    * `iri`: An explicit IRI for the field.

If multiple `@rdf` directives are present on a field then all their namespaces are combined (latter override earlier). For all other aspects the last setting per aspect takes effect.

```graphql
{
  Pokemon
    @rdf (
      prefixes: {
        rdfs: "http://www.w3.org/2000/01/rdf-schema#",
        pokedex: "http://pokedex.dataincubator.org/pkm/"
      },
      base: pokedex
    )
  {
    label @rdf(ns: rdfs),
    colour,
    sameAsLinks @rdf(iri: "http://www.w3.org/2002/07/owl#sameAs")
  }
}
```

The effective document becomes:
```graphql
{
  Pokemon @rdf(iri: "http://pokedex.dataincubator.org/pkm/Pokemon")
  {
    label @rdf(iri: "http://www.w3.org/2000/01/rdf-schema#label"),
    colour @rdf(iri: "http://pokedex.dataincubator.org/pkm/colour"),
    sameAsLinks @rdf(iri: "http://www.w3.org/2002/07/owl#sameAs")
  }
}

```

