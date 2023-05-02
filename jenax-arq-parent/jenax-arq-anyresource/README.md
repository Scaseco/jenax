---
title: AnyResource
parent: ARQ Extensions
nav_order: 40
---

## AnyResource

This tiny module introduces a `Resource` implementation that allows for wrapping even literals with the `Resource` API.
As a consequence, literals can be described with properties which means that those described literals will appear in subject positions of the RDF graph.
This is useful in application development to annotate arbitrary nodes with presentation information.

### Maven

```xml
<dependency>
    <groupId>org.aksw.jena-sparql-api</groupId>
    <artifactId>jena-sparql-api-any-resource</artifactId>
</dependency>
```

### Example

```java
Node node = NodeFactory.createLiteral("subject");
Model model = ModelFactory.createDefaultModel();
RDFNode n = model.asRDFNode(node);

Resource res = n.as(AnyResource.class);
res.addProperty(RDFS.comment, "This is a subject");

RDFDataMgr.write(System.out, x.getModel(), RDFFormat.NTRIPLES);
```

Output:

```
"subject" <http://www.w3.org/2000/01/rdf-schema#comment> "This is a subject" .
```

