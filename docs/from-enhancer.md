## JenaX From-Enhancer

The From-Enhancer (FE) plugin features:

* Rewriting of FROM clauses as FILTER statements.
* Remapping graph names to custom filter expressions.


### Relevant Namespaces

```sparql
PREFIX fe:        <http://jena.apache.org/from-enhancer#>
PREFIX xdt:       <http://jsa.aksw.org/dt/sparql/>
```

Note that the FE plugin loads jenax's datatype extensions for jena. Most importantly this includes the `xdt:expr` datatype which is used to capture SPARQL expressions and get warnings about syntax errors (if any) during RDF parsing.
The datatype is implemented in the class `RDFDatatypeExpr`.

### Assembler Configuration

The type `fe:DatasetFromAsFilter` refers to the main dataset wrapper to rewrite FROM clauses as filter statements in.
For example, given the initial query
```sparql
SELECT * FROM <x> { ?s ?p ?o }
```
it will be rewritten to
```sparql
SELECT * { GRAPH ?g { ?s ?p ?o } FILTER (?g = <x>) }
```
before being sent to the actual query evaluation engine.

It is possible to map specific graph IRIs to custom SPARQL expressions using `fe:alias` as demonstrated below.

```turtle
<urn:example:root> a fe:DatasetFromAsFilter
  ; ja:baseDataset <urn:example:base>

  # The boolean constants true / false can be used to match all / no graphs, respectively.
  ; fe:alias [ fe:graph <urn:example:all> ; fe:expr "true"^^xdt:expr ]

  # An IRI constant is used to remap a graph to the given one.
  ; fe:alias [ fe:graph <urn:example:dbpedia> ; fe:expr "<http://dbpedia.org/>"^^xdt:expr ]

  # Expressions can mention a single variable which during query rewriting will be bound to graph names
  ; fe:alias [ fe:graph <urn:exmaple:regex> ; fe:expr "regex(str(?g), '')"^^xdt:expr ]
  .
  
<urn:example:base> a ja:MemoryDataset .
```

### Implementation

* Assembler interpreter: `org.aksw.jenax.arq.fromasfilter.assembler.DatasetAssemblerFromAsFilter`
* DatasetWrapper: `org.aksw.jenax.arq.fromasfilter.dataset.DatasetGraphFromAsFilter`
* QueryEngineFactory: `org.aksw.jenax.arq.fromasfilter.engine.QueryEngineFactoryFromAsFilter`

