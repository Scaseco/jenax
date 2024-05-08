---
title: SPARQL Polyfill Model
parent: SPARQL Polyfill
nav_order: 30
---

# SPARQL Polyfill Model

This module provides Java classes to read and write rules for SPARQL Polyfill system.

A polyfill transformation rule comprises the following elements:

* A label for presentation to the user
* A comment with more detailed information for the user
* A condition for when the the polyfill transformation should be suggested.
* The polyfill transformation which rewrites SPARQL queries. Currently only Java class names that implement the transformation are supported.

## Conditions

## Transformations

The class names referenced by the transformation must implement any of the following interfaces below.
The polyfill system will set up a `org.aksw.jenax.dataaccess.sparql.datasource.DataSourceTransform` instance that can wrap an existing data source
such that the configured SPARQL statement transformations are applied.

Supported transformations:

* `org.apache.jena.sparql.algebra.Transform`
* `org.apache.jena.sparql.algebra.optimize.Rewrite`
* `org.apache.jena.sparql.expr.ExprTransform`
* `org.aksw.jenax.dataaccess.sparql.datasource.DataSourceWrapper`

## Status of this Document

* The types in the model still use java URIs which should be replaced with ones in the `norse:polyfill.` namespace.


## Rules


```turtle
[ a                          <java://org.aksw.jenax.model.polyfill.domain.api.PolyfillSuggester>;
  rdfs:comment               "Client-side execution of LATERAL";
  rdfs:label                 "Generic - LATERAL";
  norse:polyfill.condition   [ a                           <java://org.aksw.jenax.model.polyfill.domain.api.PolyfillConditionQuery>;
                               norse:polyfill.queryString  "PREFIX : <http://www.example.org/polyfill/lateral/> SELECT * { :s :p ?o . LATERAL { ?o :p :o } }"
                             ];
  norse:polyfill.suggestion  [ a                         <java://org.aksw.jenax.model.polyfill.domain.api.PolyfillRewriteJava>;
                               norse:polyfill.javaClass  "org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithLocalLateral"
                             ]
] .
```

```java
model.createResource().as(PolyfillSuggester.class)
    .setLabel("Generic - LATERAL")
    .setComment("Client-side execution of LATERAL")
    .setCondition(model.createResource().as(PolyfillConditionQuery.class)
        .setQueryString("PREFIX : <http://www.example.org/polyfill/lateral/> SELECT * { :s :p ?o . LATERAL { ?o :p :o } }"))
    .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
        .setJavaClass(RdfDataSourceWithLocalLateral.class.getName()));
```

Models for specifying vendor-specific sequences of sparql query transforms.

## Not used
This module is in design phase.
Its not yet clear to what extent an RDF vocubulary for custom query rewriters / optimizers is needed.
The main use case would be customizability with third party plugins.

However, in general it seems more worthwhile to auto-detect limitations of triple stores.
Auto-detectors could be registered using plain java SPI plugins.
