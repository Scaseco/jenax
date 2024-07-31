## GraphQL to SPARQL Rewriter


### Features

* **Fully streaming** JSON generation from SPARQL result sets.
* Rewrites GraphQL to a single **SPARQL query**.
* Vendor-independent: The generated SPARQL queries can run on any SPARQL 1.1 \* endpoint.
* Self-contained queries
    * No need for expensive schema generation or data summarization
    * No need to manage additional mapping files
* 


### Limitations and Pitfalls

* The generated SPARQL query makes use of the LATERAL feature, however this can be polyfilled at the cost of multiple requests with jenax SPARQL polyfills in jenax-dataaccess
* The GraphQL-to-SPARQL rewriter makes the following assumptions:
    * Inter-UNION order preservation: Given graph patterns A, B, C, then it is expected that `UNION(A, UNION(B, C))` yields all bindings of A before B, and all bindings of B before C.
    * Intra-UNION order preservation: ORDER BY clauses within a union must be preserved.


### Core Concepts

A GraphQL query is specified by a GraphQL *document* which contains one *query operation definiton*. A *query operation definition* is primarily composed of *fields*, which can have *arguments*, carry annotations called *directives*.


### Mapping SPARQL to JSON

Each GraphQL field is associated with the following aspects:
* a SPARQL pattern
    * of which one list of variables act as the *source* and
    * another list of variables that act as the *target*.


```
{

}
```



# Reference

## GraphQL Directive: `@prefix`

The `@prefix` directive is designed to manage and define namespace prefixes in a GraphQL schema. This directive can be used to either specify a single prefix with an IRI or map multiple prefixes to their corresponding IRIs.

### Usage

The `@prefix` directive accepts two possible configurations:

1. **Single Prefix Definition**: Use the `name` and `iri` arguments to define a single prefix.
2. **Multiple Prefix Mapping**: Use the `map` argument to define multiple prefixes in a key-value format.

#### Arguments

- **`name`** (`String`): The prefix name to be used.
- **`iri`** (`String`): The IRI (Internationalized Resource Identifier) associated with the prefix.
- **`map`** (`Map<String, String>`): A map of prefix names to their corresponding IRIs.

#### Examples

1. **Single Prefix Definition**

   Define a single prefix using the `name` and `iri` arguments:

   ```graphql
   @prefix(name: "rdf", iri: "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
   ```

2. **Multiple Prefix Mapping**

   Define multiple prefixes using the `map` argument:

   ```graphql
   @prefix(
     map: {
       rdf: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
       rdfs: "http://www.w3.org/2000/01/rdf-schema#"
     }
   )
   ```

#### Notes

- When using the `map` argument, you can define multiple prefix-to-IRI mappings in a single directive instance.
- The `name` and `iri` arguments should not be used together with the `map` argument; choose one configuration based on your needs.




## GraphQL Directive: `@pattern`

The `@pattern` directive is used to associate a field in your GraphQL schema with a SPARQL graph pattern. It allows you to define how variables within a SPARQL query map to the fields in your schema, providing flexibility in connecting fields to specific parts of the SPARQL graph.

### Purpose

The directive is particularly useful in RDF and knowledge graph scenarios, where the data is modeled as triples. It lets you specify how variables in the pattern relate to each other across different fields.

#### Key Concepts

- **Source Variables (`from`)**: These variables represent the starting point of the field’s graph pattern. They typically join with the parent field’s target variables by default.
- **Target Variables (`to`)**: These variables represent the output of the field’s graph pattern.
- **SPARQL Pattern (`of`)**: The SPARQL graph pattern, expressed as a string, which specifies the relationship between variables.

#### Usage

The `@pattern` directive supports the following arguments:

- **`of`** (`String`): The SPARQL graph pattern that defines the relationship between variables.
- **`from`** (`String | [String]`): The source variable(s) for this field. If only a single variable is used, it can be passed directly as a string. Otherwise, an array is used.
- **`to`** (`String | [String]`): The target variable(s) for this field. Similar to `from`, this can be a string or an array.

#### Example

Here is an example demonstrating how to define a `MusicalArtist` type using the `@pattern` directive:

```graphql
type MusicalArtist @pattern(of: "?s a dbo:MusicalArtist", from: "s", to: "s") {
  label @pattern(of: "?s rdfs:label ?o", from: "s", to: "o")
}
```

#### Explanation

1. **MusicalArtist Field**: The `MusicalArtist` type is associated with the graph pattern `?s a dbo:MusicalArtist`, where the `s` variable acts as both the source and target.
2. **Label Field**: The `label` field is defined with a nested pattern `?s rdfs:label ?o`, where `s` is the source and `o` is the target.

#### Rule for Implicit Joins

By default, a field’s source variables (defined by `from`) are automatically joined with its parent’s target variables (defined by `to`). This allows seamless chaining of patterns without redundant variable specification.

#### Notes

- If there is only one source or target variable, the array brackets (`[]`) can be omitted.
- This directive is designed to handle more complex SPARQL graph patterns and facilitate better integration with RDF data sources.




## GraphQL Directive: `@join`

The `@join` directive allows you to explicitly define how variables in a parent field are joined with variables in a child field. This directive is particularly useful when the implicit join rule does not apply, such as when working with composite keys or specific variable subsets.

### Purpose

While implicit joins automatically connect a parent’s target variables with a child’s source variables, the `@join` directive provides fine-grained control for cases where:
- The parent field’s target variables form a composite key (e.g., multiple variables like `?cityName` and `?countryName`).
- You need to join only a subset of these variables with the child field’s source variables.

#### Arguments

- **`parent`** (`String | [String]`): Specifies the parent field's variable(s) to be joined.
- **`this`** (`String | [String]`): Specifies the child field's variable(s) that should be connected with the parent’s variable(s).

#### Usage

The `@join` directive can be used when there is a need to manually specify how variables in a child field relate to those in a parent field, typically in more complex SPARQL scenarios.

#### Example

The following example demonstrates how to use the `@join` directive in a GraphQL schema:

```graphql
{
  Location @pattern(of: "?x :city ?cityName ; :country ?countryName",
                    from: ["cityName", "countryName"], to: ["cityName", "countryName"]) {

    cityName @pattern(of: "BIND(?x AS ?y)", from: "x", to: "y") @join(parent: "cityName")
}
```

#### Explanation

1. **Location Field**: The `Location` field’s pattern includes a composite key formed by `cityName` and `countryName` (with both acting as target variables).
2. **CityName Field**: The `cityName` field’s pattern maps the value of `x` to `y`, where `y` is effectively a bound copy of `x`.
3. **Explicit Join**: The `@join(parent: "cityName")` directive ensures that the `x` variable (specified in `from: "x"`) joins with the parent’s `cityName` variable instead of relying on the implicit join rule.

#### Variable Handling and Flexibility

- When there is only a single variable to be joined, the array brackets can be omitted (e.g., `"cityName"` instead of `["cityName"]`).
- The `@join` directive provides more control over complex joining scenarios, such as those involving composite keys or selective joins.

#### Implicit vs. Explicit Joins

- **Implicit Joins**: Automatically connect a parent’s target variables with a child’s source variables based on the default variable inheritance rule. No `@join` directive is needed in these cases.
- **Explicit Joins**: The `@join` directive is required when more specific joins are needed, such as connecting only a subset of composite keys or customizing how variables are linked.

#### Notes

- Use the `@join` directive when you need more precision in how variables between parent and child fields are connected, especially when dealing with complex data models or SPARQL patterns.


## GraphQL Directive: `@index`

The `@index` directive allows you to transform the output of a GraphQL field into a JSON object, where the keys are derived from a specified SPARQL expression. This is especially useful when you need to index data by certain variables in a SPARQL query and control the structure of the JSON output based on the cardinality of the data.

### Purpose

This directive is designed to facilitate the indexing of field outputs by a specified SPARQL expression, turning the field into a JSON object where the keys are determined by that expression. Additionally, it allows you to control the cardinality (i.e., whether the value for each key is a single item or an array) using a `oneIf` condition.

#### Arguments

- **`by`** (`String`): A SPARQL expression that determines the keys in the indexed output.
- **`oneIf`** (`String`): A SPARQL expression that controls whether the value for each key should be treated as a single item (if the expression evaluates to `true`) or an array (if it evaluates to `false`). By default, this is set to `"false"`, meaning the value is treated as an array unless explicitly overridden.

#### How It Works

The `@index` directive converts a field’s output into a JSON object, where:
- The keys are derived from the `by` argument.
- The values are determined based on the `oneIf` argument:
  - If `oneIf` evaluates to `true`, a single value is expected for each key.
  - If `oneIf` evaluates to `false`, multiple values are allowed, and they are returned as an array.

#### Example

Consider the following GraphQL schema:

```graphql
type Triples @pattern(
  of: "?s ?p ?o", 
  from: "s", 
  to: "o"
) @index(by: "?p", oneIf: "false")
```

Given the RDF triples:

```sparql
PREFIX : <http://www.example.org/>
:s1 :p1 :o1 .
:s1 :p2 :o2 .
:s2 :p1 :o1 .
```

The expected JSON output would be:

```json
{
  "http://www.example.org/p1": ["http://www.example.org/o1", "http://www.example.org/o1"],
  "http://www.example.org/p2": ["http://www.example.org/o2"]
}
```

#### Detailed Explanation

1. **Indexing by SPARQL Expression**: The `by` argument specifies that the output should be indexed by the value of `?p`. Each unique value of `?p` becomes a key in the resulting JSON object.

2. **Handling Cardinality with `oneIf`**: The `oneIf` argument is set to `"false"`, meaning that the value for each key is treated as an array, allowing multiple values for the same key. If the `oneIf` condition were set to a SPARQL expression that evaluates to `true`, only a single value would be allowed for each key, and any additional values would trigger an error.

3. **Default Behavior**: If the `oneIf` argument is omitted, it defaults to `"false"`, meaning the values are treated as arrays unless explicitly specified otherwise.

#### Notes

- The `by` argument must be a valid SPARQL expression, typically a variable or an expression that resolves to a value.
- The `oneIf` argument provides flexibility in defining whether the output for each key should be a single value or an array, depending on the cardinality of the data.
- When the `oneIf` condition is `true` for a key, only one value is expected. If multiple values are encountered, an error will be reported in the GraphQL output.

#### Practical Use Cases

The `@index` directive is particularly useful when:
- You need to convert a list of triples into a JSON object indexed by a specific predicate or property.
- You want to control how cardinality is handled in your JSON output, deciding whether to treat the values as single items or arrays based on SPARQL conditions.


## GraphQL Directives: `@one` and `@many`

The `@one` and `@many` directives control the cardinality of a field within a GraphQL schema. These directives are particularly useful in RDF and SPARQL-based contexts, where fields often correspond to graph patterns that involve relationships with varying cardinalities.

### Purpose

These directives allow you to specify whether a field should be treated as a single-valued or multi-valued field. By default, fields are treated as multi-valued (`@many`), which is typical in RDF data where properties often have multiple values.

#### Arguments

Both directives accept the following arguments:

- **`self`** (`Boolean`): Controls whether the directive applies to the field it appears on.
- **`cascade`** (`Boolean`): Controls whether the directive cascades to child fields, affecting their cardinality as well.

#### Default Behavior

- **`@many`** is the default cardinality for all fields, as fields are often mapped to RDF graph patterns that can yield multiple target values (1:n relationships).
- When applied, the directives determine whether a field is considered single-valued (`@one`) or multi-valued (`@many`), and can optionally cascade this behavior to child fields.

#### Usage

These directives can be applied to fields to control their cardinality and the cardinality of their child fields:

- **`self`** (`true` by default): If `true`, the directive applies to the field itself.
- **`cascade`** (`false` by default): If `true`, the directive applies to all child fields as well.

#### Example

Consider the following example:

```graphql
type Parent @one(self: false, cascade: true) { 
  # The Parent field is still effectively @many, but the cardinality cascades to its children
  Child1 # Child1 inherits @one cardinality from Parent
  Child2 # Child2 also inherits @one cardinality from Parent
}
```

#### Detailed Explanation

1. **Parent Field**: The `@one(self: false, cascade: true)` directive is applied. This configuration means that:
   - `self: false`: The `@one` directive does **not** apply to the `Parent` field itself. The field remains multi-valued (`@many`).
   - `cascade: true`: The `@one` behavior cascades to the child fields (`Child1` and `Child2`), making them single-valued.

2. **Child Fields**: Both `Child1` and `Child2` automatically inherit the `@one` cardinality from the `Parent` due to the cascading effect. They are treated as single-valued fields.

#### Understanding Cardinality Control

- **`@one` Directive**: Specifies that the field is single-valued. If a field mapped to a SPARQL pattern yields more than one value, it will trigger an error in the GraphQL output.
- **`@many` Directive**: Specifies that the field is multi-valued, allowing it to contain an array of values (this is the default).

#### Practical Use Cases

The `@one` and `@many` directives are useful when you need precise control over the expected cardinality of fields, especially in cases where:
- You expect a single value (e.g., a unique identifier or singular property) and want to enforce this constraint.
- You want to apply consistent cardinality rules across a hierarchy of fields using cascading behavior.

#### Notes

- By default, fields are assumed to be `@many` unless explicitly overridden.
- The `cascade` argument allows you to propagate cardinality rules down to child fields, reducing the need for redundant annotations.

