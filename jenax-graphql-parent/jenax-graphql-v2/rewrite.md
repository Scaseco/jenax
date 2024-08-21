
So we create a rootNode without children,
then place it into the builder, add children 



TreeNode<T> {
  T data;
  TreeNode<T> children;
}


Field.newBuilder(data)
  .addChildBuilder(childBuilder)
  .build();


FieldBuilder {
  
}


Var handles:



Input pattern:
{ ?s ?p ?o }


Desired output:
{
  "s1": {
    "p": [ "p1", ..., "pn"]
    "o": [ "o1", ..., "on"]
  }
}


GraphQL:
{
  @sparql(pattern: "?s ?p ?o") @index(by: "s", oneIf: "true")
  root
    p @ref(var: s)
    o @ref(var: o)
}

`oneIf: "true"` causes each value in the index to only have a single value.

Structure:
Agg.indexBy("s",
  Agg.object(Map.of(
      "p", Agg.array(Agg.literal("p")),
      "o", Agg.array(Agg.literal("o"))
    )
  )
)



@indexBy: The output type of a field with this annotation is OBJECT.





Input pattern:
{ ?s ?p ?o }


{
  @sparql(pattern: "?s ?p ?o") @index(by: "s")
  root
    p @ref(var: s) @one
    o @ref(var: o) @one
}

Desired output:
{
  "s1": [
    { "p": "p1", "o": "o1" }
    ...
    { "p": "pn", "o": "on" }
  ]
}

Without `oneIf`, each value of the index is an array. The @one annotation prevents the keys to become arrays.

Agg.indexBy("s",
  Agg.array(
    input -> input, // Each input binding maps to a fresh array element
    Agg.object(Map.of(
      "p", Agg.literal("p"),
      "o", Agg.literal("o")
    ))
  )
)












Input pattern:
{ ?s ?p ?o }
  {?o rdfs:label ?l }


Desired output:
{
  "s1": {
    "p": [ "p1", ..., "pn"]
    "os": [
      { "v": "o1", "l": "l1" }
      ...
      { "v": "on", "l": "ln" }
    ]
  }
}


GraphQL:
{
  @sparql(pattern: "?s ?p ?o") @index(by: "s", oneIf: "true")
  root
    p @ref(var: s)
    os
      o @ref(var: o) @one
      l @match(iri: 'rdfs:label) @one
}

`oneIf: "true"` causes each value in the index to only have a single value.

Structure:
Agg.indexBy("s",
  Agg.object(Map.of(
      "p", Agg.array(Agg.literal("p")),
      "os", Agg.array(
        Agg.object(Map.of(
          "v", Agg.literal("o"),
          "l", Agg.substate(state2)
        )
      )
    )
  )
)

// We need to know which (immediate?) substate references a state has.




Map substate = {
  state2: Agg.literal("l")  // Transition into sub-state!
}



Maybe the accumulator changes the state in a context rather than returning the next state?

The point is, that we need to expose the state/accumulator which will receive the next bindings.



So the AccDriver has the reference to which state accepts the next binding.
If the state does not accept it (transition returns null), then the driver checks for any parent state that accepts the input.




 context @static(value: {
    foo: bar
  })


query
context @jsonLdContext
root
  


GraphQL:
{
  @sparql(pattern: "DISTINCT ?s { ?s ?p ?o }") @index(by: "s", oneIf: "true")
  root
    id @ref(var: s) @one
    p @pattern("?s ?p ?o", src:"s" tgt: "p")
    o @pattern("?s ?p ?o", src:"s" tgt: "p")
}


{
  s1: { p: [], o[] } },
  s2: { p: [], o[] } }
}

  
  

   
   
      
