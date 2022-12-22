---
title: Function Binder
nav_order: 50
---

## Function Binder
The function binder utility makes it easy to expose Java methods as SPARQL functions.

### Limitations
* Multi-methods are not yet supported - i.e. it is not possible to define multiple Java methods (with varying parameter type lists) as implementations of the same SPARQL function IRI.

### Basic Usage

The code below shows how to register a custom "reverse string" function with Jena's `FunctionRegistry`.

```java
/* A class with methods annotated with IRIs via @Iri and/or @IriNs */
public class SparqlFnLibString {
    /**
     * Definition of the "string-reverse" SPARQL function for use as in:
     *
     * SELECT * { BIND(<http://www.example.org/reverse>("hello") AS ?x) }
     */
    @IriNs("http://www.example.org/")
    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString()
    }    
}

/**
 * The entry point of the custom Jena plugin.
 * The fully qualified class name must be placed into the file
 *   src/main/resources/META-INF/services/org.apache.jena.sys.JenaSubsystemLifecycle
 */
public class InitMyJenaPlugin
    implements JenaSubsystemLifecycle
{
    public void start() {
        FunctionBinder binder = JenaExtensionUtil.createFunctionBinder(FunctionRegistry.get());
        binder.registerAll(SparqlFnLib.class);
    }
}
```

The `registerAll(Class<?>)` method registers all appropriately annotated *static* methods with the configured function registry.
Use the overload `registerAll(Class<?>, Object)` to register all appropriately annotated methods of an object. The object must be
an instance of the given class.

### Default Values

The `@DefaultValue("lexicalValue")` annotation can be used to annotate parameters.
Once a parameter is annotated with a default value then all remaining parameters must have default value annotations as well.

```java
    /**
     * A function that increments a value by a given amount. The amount defaults to 1.
     *
     * SELECT * {
     *   BIND(eg:inc(1) AS ?x) // uses default value of 1
     *   BIND(eg:inc(1, 2) AS ?y)
     * }
     */
    @IriNs("http://www.example.org/")
    public static long inc(long value, @DefaultValue("1") int amount) {
        return value + amount;
    }    
```

### Registering Custom Type Conversions

By default, Jena's `TypeMapper` is consulted for mapping between RDF literal types and Java classes.
In addition, it is possible to access a FunctionBinder's underlying converter registry in order to define
custom two-way conversions between the TypeMapper's Java classes and parameter types.
For example, Jena's GeoSPARQL extension uses the `GeometryWrapper` Java class to capture geometry RDF literals.
However, `GeometryWrapper` internally also wraps Java Topology Suite (JTS) `Geometry`.
In order to allow `GeometryWrapper` instances to be used as arguments for `Geometry` parameters the following snippet can be used
to register custom coercions:

```java
FunctionBinder binder = JenaExtensionUtil.createFunctionBinder(FunctionRegistry.get());
FunctionGenerator generator = binder.getFunctionGenerator();

// Define two-way Geometry - GeometryWrapper coercions
generator.getConverterRegistry()
    .register(Geometry.class, GeometryWrapper.class,
            geometry -> new GeometryWrapper(geometry, WKTDatatype.URI),
            GeometryWrapper::getParsingGeometry)
    ;

binder.registerAll(SparqlFnLibGeo.class);
 
class SparqlFnLibGeo {    
	@IriNs(GeoSPARQL_URI.GEOF_URI)
	public static Geometry simplifyDp(
			Geometry geom, // Because of the coercion we can use `Geometry` here instead of `GeometryWrapper`
			@DefaultValue("0") double tolerance,
			@DefaultValue("true") boolean ensureValid) {
		DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(geom);
		simplifier.setDistanceTolerance(tolerance);
		simplifier.setEnsureValid(ensureValid);
		Geometry result = simplifier.getResultGeometry();
		return result;
	}
}
```

## VarArgs

Variable argument lists are supported. Thereby, the type `Node` accepts any RDF type.
The following example shows an excerpt of `array` function implementations.

```java
public class SparqlLibArrayFn {
    /** SELECT * { BIND(array:of(1, 'string', true) AS ?arr) }*/
    @IriNs(JenaExtensionArray.NS)
    public static NodeList of(Node... nodes) {
        return new NodeListImpl(Arrays.asList(nodes));
    }

    @IriNs(JenaExtensionArray.NS)
    public static Node get(NodeList nodes, int index) {
        return nodes.get(index);
    }
}
```

