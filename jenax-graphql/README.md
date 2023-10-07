## GraphQL-to-SPARQL rewriter


### Features

* Fast and streaming (for top-level fields)
* Optional auto-mapping of fields to classes and properties via VoID and SHACL
* Vendor-independent (uses LATERAL which can be polyfilled with jenax SPARQL polyfills in jenax-dataaccess)

### Querying

The basic structure of a SPARQL-backed graphql query is as follows:

```graphql
{
  ClassName {
    propertyName
  }
}
```

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

#### Namespaces
The `@rdf` directive is used to supply different aspects of RDF namespace information for fields.
The directive immediately takes effect on the field it appears on.

* Aspects that affect the annotated field and all descendents:
    * `namespaces`: An object value with a mapping of prefix names to namespace IRIs.
    * `base`: A string that will be prepended to all fields *not* annotated with `@rdf` in order to form IRIs. The value of `base` will be expanded against the provided namespaces before prepeding the field name.
* Aspects that only affect the annotated field:
    * `ns`:  A string that will be prepended to the field name
    * `iri`: An explicit IRI for the field.

If multiple `@rdf` directives are present on a field then all their namespaces are combined (latter override earlier). For all other aspects the last setting per aspect takes effect.

```graphql
{
  Pokemon
    @rdf (
      namespaces: {
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
    colour @rdf(iri: "<http://pokedex.dataincubator.org/pkm/colour"),
    sameAsLinks @rdf(iri: "http://www.w3.org/2002/07/owl#sameAs")
  }
}

```

