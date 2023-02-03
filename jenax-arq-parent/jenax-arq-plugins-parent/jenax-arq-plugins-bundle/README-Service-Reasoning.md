---
title: Reasoning
parent: Service Plugins
nav_order: 10
---

# Reasoning Plugin

This plugin features ad-hoc `owl:sameAs` and `rdfs` reasoning using the following variants:

```sparql
SELECT * { SERVICE <rdfs:> { ?s a :Person } }
SELECT * { SERVICE <sameAs:> { ?s a :Person } }
SELECT * { SERVICE <sameAs+rdfs:> { ?s a :Person } }
```

## SameAs Reasoning

Loading the JenaX plugin bundle makes `SERVICE <sameAs:>` automatically available (inferencing on owl:sameAs links). No special setup is required.
SameAs reasoning is implemented as an independent `DatasetGraph` wrapper. As a consequence, it does not consider information such as
`<p> rdfs:subPropertyOf owl:sameAs`.

### Programmatic Use

The class `DatasetGraphSameAs` contains the various static methods for wrapping a base dataset with one that performs this inferencing.
The simplest form is:
```
DatasetGraph dsg = DatasetGraphSameAs.wrap(DatasetGraphFactory.createTxnMem());
```
We recommend use of `createTxnMem` because it seems it can in general answer `contains()` checks much faster than `DatasetGraphFactory.create()` (as of jena-4.7.0) - which is important when duplicates are not allowed (default).

Optionally, a cache size can be configured. For that many accessed resources, their sameAs links will be stored in the cache in order to reduce lookups on the underlying dataset.
Any modification of the dataset invalidates the cache. If there can be changes to the base dataset then caching should be disabled as chances will not be reflected.


There is also the option to create a 'tabeling' wrapper that pre-caches *all* sameAs links.
Because in this mode *all* sameAs links are known, any check for whether a resource has any sameAs links can be made solely from the cache
(without a request to the underlying dataset).
Any update to the dataset "unsets" the cache, and any request against an "cache" index will first attempt to fill it.

```
DatasetGraph dsg = DatasetGraphSameAs.wrapWithTable(DatasetGraphFactory.createTxnMem());
```

### Assembler Configuration
The `jxp:DatasetSameAs` assembler enables wrapping of a base dataset to add sameAs inferencing capabilities.
The set of predicates which to treat as 'sameAs' can be configured.

```turtle
<#datasetWithSameAs> a jxp:DatasetSameAs ;
    jxp:cacheSize 10000 ; # 0 = disable caching, -1 = prefetch all sameAs statements
    jxp:predicate owl:sameAs ;
    jxp:allowDuplicates false ; # 'True' disables the check for whether an inferred statement has already been produced
    ja:dataset <#baseDataset> .
```


## RDFS Reasoning
### Assembler Configuration

The reasoning plugin defines the following assemblers:

#### jxp:DatasetRDFS
This assembler wraps the configured base dataset with ad-hoc RDFS inferencing against the preconfigured RDFS ontology.
Any query against that dataset will match inferencable data. 
Our reasoner contains improvements aimed at providing better general performance over Jena's existing RDFS engine (when compared to jena-4.7.0).
It is designed as a drop in replacement for Jena's existing RDFS engine.


```turtle
@prefix jxp: <http://jenax.aksw.org/plugin#> .

<#datasetRDFS>
  a jxp:DatasetRDFS ;
  ja:rdfsSchema "/path/to/rdfs.ttl" ;
  ja:dataset <#baseDataset> .
```

#### jxp:datasetRDFSEnabled
This assembler enables the use of `SERVICE <rdfs:>` and `SERVICE <sameAs+rdfs:>`. Inferencing is only performed with the respective `SERVICE` use; SPARQL statements will otherwise be executed conventionally.
Technically, the specified RDFS file is processed into a `ConfigRDFS<Node>` instance and put into the base dataset's context and the result is the base dataset.
Making use of the respective `SERVICE` then executes the request against the configured context.
Note, that because this assembler modifies the base dataset's context, it is not possible to create multiple inferencing 'view' on the same dataset directly with this assembler.
Another wrapper that adds an independent copy of the context would be needed.

```turtle
@prefix jxp: <http://jenax.aksw.org/plugin#> .

<#datasetRDFS>
  a jxp:DatasetRDFSEnabled ;
  ja:rdfsSchema "/path/to/rdfs.ttl" ;
  ja:dataset <#baseDataset> .
```

