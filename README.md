---
title: Features
nav_order: 20
---

Unofficial extensions for jena - refactoring of https://github.com/SmartDataAnalytics/jena-sparql-api

## Maven Dependencies

We recommend to import the jenax `bom` file in order to ensure consistent versions among the dependencies:

```xml
<dependency>
    <groupId>org.aksw.jenax</groupId>
    <artifactId>jenax-bom</artifactId>
    <version>${jenax.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

## Highlights

Some components that deserve particular recognition:

* `FunctionBinder`: Annotate Java functions and let the function binder do the work to enable their use in SPARQL.
* `Reprogen` (Resource Proxy Generator): Tired of writing code to brigde the Java object world with the RDF graph world? Let reprogen generate those bindings from annotated interfaces. Supports generation of mutable `java.util.{Set, List, Map}` views that read/write through to a Jena Model. Do you need an approach to generate IRIs for your classes in a controlled and *deterministic* way from only a subset of the RDF graph that backs your object graph? Then you may want to give Reprogen's annotation-based skolemizer a try.
* `SliceCache`: Low-latency caching of slices of SPARQL result sets. Clients can read slices of data concurrently while they are being retrieved. A configurable amount of pages is kept in an LRU (least-recently-used) memory cache and after a configurable delay they are synced to disk.
* `BinarySearcher`: A hadoop-based implementation for performing binary search on seekable streams. Allows for looking up information by subject in (compressed) ntriples files. The DBpedia Databus is a data catalog system that forsaw this use case and already provides the appropriate metadata.







