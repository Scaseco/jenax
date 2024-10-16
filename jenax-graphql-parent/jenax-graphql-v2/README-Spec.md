

@prefix(name: "eg", iri: "http://www.example.org/")

prefix is a well established term in the RDF domain.
Argument naming design rationale: Prefix introduces a new name, hence `name`.
Strictly speaking, this is a prefix declaration so @prefixDecl might be more succinct, but 
The value is always an absolute IRI, hence `iri`.


Prefixes are resolved in a preprocessor pass.

Rationale:
It seems much better if patterns can refer to prefixes that are defined on the same node.
field @pattern(of: 'eg:foo ...') @prefix(name: 'eg', iri: '...')



` field @emitRdfKey(iri: 'propertyName', reverse: true)`
declare which rdf property should be emitted on a field in RDF output mode. the emitted property may be different from the matched one.

the emitted property can be dynamic and based on the variables available at that field.
@emitRdfKey(expr: '?p', reverseExpr: '!STRSTARTS(STR(?p), 'urn:')')


@emitJsonKey
Same as @emitRdfKey, however for json output mode.

Define new variables based on sparql expressions:
Variable must net yet exist on the current node
@bind(var: 'var', expr: 'str(?s)')


@pattern defines the primary pattern for field - it can only exist once.

# Declare inclusion of a child pattern into this field field on this node.
Q: Should we allow remapping variables of the referenced field?
@includePattern(field: 'fieldPattern', varMap: {})

# 
@filter(expr: )


Aliases: Only used to reference fields. Should be globally unique, but not required (two different subtrees may use the same alias) - ambiguous matches raise errors.
@alias(name: 'foo')


explicit injection of graph patterns:
@inject(pattern: 'SELECT ?x { ?x foo bar}')


@inject(pattern: '?x foo bar', var: x)
Join the given pattern with the specified variable with the current field's pattern's source variables.
number of variables must match.
all non joining variables are renamed to avoid name clashes.





sorting by variables of remote fields, implicit injection of graph patterns.

@orderBy



field
  @orderBy(var: ..., field: '')
  @orderBy(var:)


Ordery by is similar to @inject.



Filtering by a field: @filter(expr: "?fieldName < ?anotherFieldName)

The filter expression is a special sparql expression: the variables are first resolved to field names, and then substituted with the field's targets. Hence, using a field with multiple target variable is an error.
A specific variable of a field can be selected with ?fieldName_varName.

fieldName_SRC


Meta-variables: SRC, TGT - used to refer to a fields source and target (hyper) variables.




Traversals with graph:
cascade: false -> graph only applies to the current pattern.
cascade: true -> all sub patterns will be nested in the same graph.

GRAPH <foo> {
  { }
  LATERAL { }
}

vs

GRAPH <foo> {
}
LATERAL {
  GRAPH <foo> {
  }
}

@graph(var: )
@graph(iri: 'someIri', self: false, cascade: true)



@graph(inherit: true) opposite of cascade true - probably not needed.


@graph // without argument matches any graph?



@embedJson(if: 'condition', onError: WARN | FAIL | IGNORE)
Only applies to JSON mode.
If the condition is met, then try to parse the lexical form of any output value that is an RDF literal as a json document.
If the parsing succeeds, then make the JSON document part of the output document (rather than creating a string literal that contains JSON).

`@embedJson` is a shorthand for @embedJson(if: 'true', onError: IGNORE)





Field type rules:
GraphQL fields are tied to the _production_ of elements for the parent and the _consumption_ of elements from its children.

Fields can produce objects, literals, arrays and entries (and null).

Most fields produce an entry for the consuming parent object.
An entry can consume literals, objects, arrays - but not other entries.

If an entry has children, the consuming type defaults to object.





