## GraphQL to SPARQL Rewriter

# OUT OF DATE

**This document is out of date. The current version is available at:** https://smartdataanalytics.github.io/RdfProcessingToolkit/graphql/

### Features

* Fast and **fully streaming** JSON serialization from SPARQL result sets
* Vendor-independent: The generated SPARQL queries can run on any SPARQL 1.1 \* endpoint.
* Self-contained queries: Use the `@rdf` and `@sparql` directives to unambiguously map the GraphQL fields to data in your SPARQL endpoint.
    * No need for expensive schema generation or data summarization
    * No need to manage additional mapping files
* Optional support for auto-mapping fields to classes and properties. Auto-mapping performs data summarization which can take a while on large data. Data summarization builds VoID and SHACL models.

\* The generated SPARQL query makes use of the LATERAL feature, however this can be polyfilled at the cost of multiple requests with jenax SPARQL polyfills in jenax-dataaccess



## Building SPARQL queries with GraphQL

The core machinery is based on the `@rdf` and `@sparql` directives.

### Prefixes and Namespaces

The essential aspect for bridging GraphQL and SPARQL is to annotate fields with IRIs. The explicit way to accomplish this is using the `@rdf(iri: )` directive.

```graphql
{
  label @rdf(iri: "http://www.w3.org/2000/01/rdf-schema#label")
}
```

In the following we show all the supported approaches for easing this task and making it less repetetive.
Note, that ultimately these approaches are just implicit variants of the explicit `@rdf(iri: )` annotation.

A set of prefixes can be declared with `@rdf(prefixes:)`. The prefixes are available for use on the field itself and any descending fields.
`@rdf(iri:)` is evaluated only after a field's _effective_ set of prefixes has been computed. In the example below the field `label` is thus effectively annotated with `@rdf(iri: "http://www.w3.org/2000/01/rdf-schema#")
```graphql
{
  label @rdf(iri: "rdfs:label")
    @rdf(prefixes: {
      rdfs: "http://www.w3.org/2000/01/rdf-schema#"
    })
}
```

Often a field name matches the last part of an IRI. In the example above, we have repeatedly written `label`. We can eliminate the repetition using `@rdf(ns: )`. It is like `@rdf(iri:)` with the addition that it appends the field name.
```graphql
{
  label @rdf(ns: "rdfs")
    @rdf(prefixes: {
      rdfs: "http://www.w3.org/2000/01/rdf-schema#"
    })
}
```

Setting a `base` IRI on a field implicitely sets `@rdf(iri:)` for the annotated field and all descendants that are not otherwise annotated.
```graphql
{
  label
    @rdf(prefixes: {
      rdfs: "http://www.w3.org/2000/01/rdf-schema#"
    }),
    base: "rdfs")
}
```

#### Pitfalls
The values for `iri`, `ns` and `base` are always resolved against a field's effective prefixes.
You should avoid defining prefixes that are also used as IRI schemas, such as `http`, `https` or `urn`.
In the example below, the IRI for `label` is expanded to `http://www.example.org///www.w3.org/2000/01/rdf-schema#label`.

```graphql
{
  label @rdf(iri: "http://www.w3.org/2000/01/rdf-schema#label")
    @rdf(prefixes: {
      http: "http://www.example.org/"
    })
}
```

### Selecting the Initial Set of Items
The core machinery is based on the `@rdf` and `@sparql` directives.

In general, we differ between `top-level fields`, which specify sets of items and `inner fields` which correspond to properties of these items.
Inner fields are thus inherently backed by the set of items in the RDF graph that are reached by traversing paths of RDF properties based on a field's annotation.
For top-level fields, the initial set of items needs to be specified.

The initial set of items is conceptually specified using a SPARQL SELECT query that projects only a single variable.
An explicit SPARQL query that projects a single variable can be specified using the `@sparql(fragment:)` directive:
```graphql
{
  People @sparql(fragment: "SELECT ?s WHERE { ?s a <http://xmlns.com/foaf/0.1/Person> })" {
    ...
  }
}
```

The annotation `@sparql(fragment:)` is generally used to state that the values of a field should be intersected with those of the supplied fragment.
If the set of items is not otherwise specified, then the sparql fragment becomes its specification.
It is possible to specify multiple query fragments. Variables in different fragments are considered as different ones.

```graphql
{
  HumanLeaders
    @sparql(fragment="SELECT ?x WHERE { ?x a <http://xmlns.com/foaf/0.1/Person> })")
    # ?x in the fragment below is different from that above.
    @sparql(fragment="SELECT ?leader WHERE { ?leader <https://dbpedia.org/ontology/leaderName> ?x })" {
    ...
  }
}
```

A common use case is to select instances of given class.
The `@class` directive is a shorthand which uses the field's IRI to produce the SPARQL fragment `SELECT ?x WHERE { ?x a $FIELD_IRI$ }`.

```graphql
{
  People @class @rdf(iri="http://xmlns.com/foaf/0.1/Person")
}
```

All `@rdf` directives are always processed first. Afterwards, `@sparql` and its shorthands are evaluated in the specified order.


The following example specifies the initial set of items to be the intersection of items that are `foaf:Person` and those that have a `dbo:leaderName`.
If `@class` was omitted, then the `@sparql(fragment:)` alone would be used as the specification of initial items.
```graphql
{
  HumanLeaders @class
    @rdf(iri: "foaf:Person")
    @rdf(prefixes: {
      dbo: "http://dbpedia.org/ontology/"
      foaf: "http://xmlns.com/foaf/0.1/"
    })
    @sparql(fragment="SELECT ?leader WHERE { ?leader dbo:leaderName ?x })" {
  }
}
```

**If the set of initial items is otherwise unspecified, it defaults to the set of subjects in the graphs.**

### Cardinality

The directives `@one` and `@many` control whether fields are mapped to JSON arrays. By default, all fields are considered `@many`.

Both directives have the following optional parameters:

* `cascade`: If false, then the directive only affects the annotated field. If true, then the cardinality is set as the default for all descendants of the annotated field. Descendant fields can override the default with further annotations. Default: `false`.
* `self`: If false, then the annotated field is not affected by the directive. Default: `true`



The following examples show how to use the annotations to specify that the `firstName` and `lastName` fields of a person should not be arrays:

```graphql
{
  People { /* Implicitly @many */
    firstName @one
    lastName @one
    friends
  }
}
```



The same example could alternatively be represented as below where `@one` declared as cascading and exceptions are specified with `@many`.

Note, that `self: false` prevents `@one` from affecting the `People` field, so this field will result in a JSON array of people.

```graphql
{
  People @one(self: false, cascade: true) {
    firstName
    lastName
    friends @many
  }
}
```



### Pagination (Limit and Offset)

Limit and offset can be provided as arguments to any field.

```graphql
{
  People(limit: 10, offset: 5) @class @rdf(iri: "foaf:Person")
    @rdf(prefixes: {
      foaf: "http://xmlns.com/foaf/0.1/"
    })
}
```


### Ordering
Use the `orderBy` argument. It accepts and object where the keys are field references and value can be `ASC` or `DESC`.
Nested fields can be be annotated with `@as(name="alias")` which allows them to be referenced with a "flat" name.

```graphql
{
  field(orderBy: { a: ASC, b: DESC })
    a
    b
}
```

### References to nested fields
```
{
  label `@as(name="alias")`
}
```

#### GraphQL aliases vs @as
GraphQL allows declaration of aliases for a field. However, result values for that field will use the renamed field.

`@as(name="alias")` is used to specify a name by which a nested field can be referenced.
The `@as` directive is used when resolving variables in filter conditions. It does not cause any change in the resulting document structure.


`@rdf(ns:)` is always applied to the field name and never to the alias as the following example shows:

```graphql
{
  foo @rdf(base="http://www.example.org/") {
    bar     # -> http://www.example.org/bar
    baz:bar # -> http://www.example.org/bar
  }
}
```

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

