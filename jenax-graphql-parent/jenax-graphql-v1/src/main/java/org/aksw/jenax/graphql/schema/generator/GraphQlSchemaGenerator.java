package org.aksw.jenax.graphql.schema.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.node.NodeCollection;
import org.aksw.jenax.arq.util.prefix.ShortNameMgr;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.graphql.sparql.DatasetMetadata;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.exec.QueryExecBuilderAdapter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import graphql.language.Argument;
import graphql.language.Definition;
import graphql.language.Directive;
import graphql.language.Document;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectTypeDefinition.Builder;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeName;

/**
 * Limitations:
 * <ul>
 *   <li>Splitting a type does not preserve its property order. (As properties are moved to a base class)</li>
 * </ul>
 */
public class GraphQlSchemaGenerator {

    /** Node for the pseudo-class of untyped instances - i.e. all instances that have to rdf:type property. */
    public static final Node UNTYPED = NodeFactory.createLiteralString("untyped");

    public static final Node EMPTY = NodeFactory.createLiteralString("emptyType");

    public static final Type TYPE_SCALAR = TypeName.newTypeName("Scalar").build();

    protected ShortNameMgr shortNameMgr = new ShortNameMgr();
    protected DatasetMetadata datasetMetadata;

    public record TypeInfo(Set<Node> subjectTypes, Node property, Set<Node> objectTypes, boolean maxResourceCard, Set<Node> objectDatatypes, boolean maxLiteralCard) {}
    public record ClassInfo(Node name, Map<Node, PropertyInfo> propertyMap, Set<Node> superTypes) {}
    public record PropertyInfo(Node name, Set<Node> objectTypes, boolean maxResourceCard, Set<Node> objectDatatypes, boolean maxLiteralCard) {
        @Override
        public PropertyInfo clone() {
            return new PropertyInfo(name, new LinkedHashSet<>(objectTypes), maxResourceCard, new LinkedHashSet<>(objectDatatypes), maxLiteralCard);
        }
    }

    public record ExclusionType(Node baseClass, Set<Node> excludedProperties) {}

    protected Map<Node, ClassInfo> classMap = new LinkedHashMap<>();
    protected Map<Set<Node>, Node> unionClassToName = new LinkedHashMap<>();

    protected Map<ExclusionType, Node> exclusionTypeMap = new LinkedHashMap<>();

    /** Artificial classes referred to by their properties. */
    protected Map<Set<PropertyInfo>, Node> propertiesToStructuralClass = new LinkedHashMap<>();

    public static List<TypeInfo> summarize(RdfDataSource dataSource) {
        Query dataSummary = SparqlStmtMgr.loadQuery("data-summary.rq");
        Table table = dataSource.asLinkSource().newQuery().query(dataSummary).table();
        // System.err.println(ResultSetFormatter.asText(table.toRowSet().asResultSet()));
        List<TypeInfo> result = table.toRowSet().stream().map(b -> new TypeInfo(
            (Set<Node>)NodeCollection.extractOrNull(b.get("sTypes")),
            b.get("p"),
            (Set<Node>)NodeCollection.extractOrNull(b.get("allOTypes")),
            ((Number)b.get("maxOCard").getLiteral().getValue()).longValue() > 1,
            (Set<Node>)NodeCollection.extractOrNull(b.get("allODTypes")),
            ((Number)b.get("maxODCard").getLiteral().getValue()).longValue() > 1))
        .toList();

        return result;
    }

    public Document process(List<TypeInfo> list) {
        init(list);

        // Replace each set of object types with a single union object type.
        for (ClassInfo ci : new ArrayList<>(classMap.values())) {
            for (PropertyInfo pi : new ArrayList<>(ci.propertyMap().values())) {
                makeUnionTypeProperty(pi);
            }
        }

        Document result = convert();
        return result;
    }

    protected void makeUnionTypeProperty(PropertyInfo pi) {
        Set<Node> ots = new LinkedHashSet<>(pi.objectTypes());
        if (ots.size() > 1) {
            // The types being combined into a union may result in properties with union types
            // that themselves need to be combined into a single type again
            Node newType = createUnionType(ots);
            // TODO Replace the type of pi with the new type
            pi.objectTypes().clear();
            pi.objectTypes().add(newType);
        }
    }

    protected void init(List<TypeInfo> list) {
        Multimap<Node, TypeInfo> sIndex = HashMultimap.create();
        list.forEach(item -> item.subjectTypes().forEach(st -> sIndex.put(st, item)));

        System.err.println(list.iterator().next());

        Map<Node, Collection<TypeInfo>> map = sIndex.asMap();
        for (Entry<Node, Collection<TypeInfo>> sts : map.entrySet()) {
            Node sourceType = sts.getKey();
            System.err.println("Class: " + sourceType);

            Collection<TypeInfo> typeInfos = sts.getValue();

            // Index properties of that class
            Set<Node> properties = new LinkedHashSet<>();
            Map<Node, PropertyInfo> pMap = new LinkedHashMap<>();

            Multimap<Node, Node> propertyTypes = HashMultimap.create();
            Multimap<Node, Node> propertyDTypes = HashMultimap.create();
            Map<Node, Boolean> propertyRCard = new HashMap<>();
            Map<Node, Boolean> propertyLCard = new HashMap<>();

            for (TypeInfo typeInfo : typeInfos) {
                // pMap.computeIfAbsent(typeInfo.property(), x -> new PropertyInfo())
                Node p = typeInfo.property();
                properties.add(p);
                propertyTypes.putAll(p, typeInfo.objectTypes());
                propertyDTypes.putAll(p, typeInfo.objectDatatypes());
                propertyLCard.compute(p, (k, v) -> (v == null ? false : v) || typeInfo.maxLiteralCard());
                propertyRCard.compute(p, (k, v) -> (v == null ? false : v) || typeInfo.maxResourceCard());
            }

            ClassInfo classInfo = new ClassInfo(sourceType, new LinkedHashMap<>(), new LinkedHashSet<>());
            for (Node p : properties) {
                Set<Node> objectTypes = (Set<Node>)propertyTypes.get(p);
                Set<Node> objectDTypes = (Set<Node>)propertyDTypes.get(p);
                System.err.println("  Property: " + p);
                System.err.println("    Types : " + objectTypes);
                System.err.println("    DTypes: " + objectDTypes);
                if (objectTypes == null) {
                    objectTypes = new LinkedHashSet<>(); // Set.of();
                }

                if (objectDTypes == null) {
                    objectDTypes = new LinkedHashSet<>(); // Set.of();
                }

                boolean rCard = propertyRCard.get(p);
                boolean lCard = propertyLCard.get(p);
                PropertyInfo pi = new PropertyInfo(p, objectTypes, rCard, objectDTypes, lCard);
                classInfo.propertyMap().put(p, pi);
                // createType(objectTypes);
            }
            registerClass(classInfo);
        }

        // Add an empty 'untyped' class if it does not exist yet.
        classMap.computeIfAbsent(UNTYPED, cls -> {
            return new ClassInfo(cls, new LinkedHashMap<>(), new LinkedHashSet<>());
        });
    }

    protected void registerClass(ClassInfo classInfo) {
        Node className = classInfo.name();
        Set<PropertyInfo> propertySet = new LinkedHashSet<>(classInfo.propertyMap().values());
        classMap.put(className, classInfo);
        // propertiesToClass.put(propertySet, className);
    }

    public String toName(Node node) {
        String result;
        if (node.isURI()) {
            String iri = node.getURI();
            result = shortNameMgr.allocate(iri).shortName();
        } else if (node.isLiteral()) {
            result = node.getLiteralLexicalForm();
        } else {
            throw new RuntimeException("Unexpected node name: " + node);
        }
        // Sanitize here or in name mgr?
        result = sanitize(result);
        return result;
    }

    public static String sanitize(String name) {
        return name;
    }

    protected Document convert() {

        for (Entry<Node, ClassInfo> e : new ArrayList<>(classMap.entrySet())) {
            e.setValue(materialize(e.getValue()));
        }

        // TODO Should all types map to list types? Most likely this is useful - but we could check for classes with just a single instance.

        List<FieldDefinition> queryFields = classMap.values().stream()
            .map(x -> FieldDefinition.newFieldDefinition()
                    .name(toName(x.name()))
                    .type(ListType.newListType(TypeName.newTypeName(toName(x.name())).build()).build())
                    .build())
            .filter(x -> !x.getName().startsWith("class") || x.getName().endsWith("dummy"))
            // .map(x -> (Definition)x)
            .toList();

        ObjectTypeDefinition query = ObjectTypeDefinition.newObjectTypeDefinition()
            .name("Query")
            .fieldDefinitions(queryFields)
            .build();

        List<Definition> definitions = classMap.values().stream()
            .map(this::convertType)
            .map(x -> (Definition)x)
            .toList();

        Document result = Document.newDocument()
            .definitions(definitions)
            .definition(query)
            .build();
        return result;
    }


    public boolean subsumes(Node parent, Node child) {
        boolean result;
        if (Objects.equals(parent, child)) {
            result = true;
        } else {
            ClassInfo ci = classMap.get(parent);
            result = ci.superTypes().stream().anyMatch(p -> subsumes(p, child));
        }
        return result;
    }


    public Set<Node> normalize(Set<Node> types) {
        Set<Node> result = new LinkedHashSet<>(types);
        for (Node parent : types) {
            Iterator<Node> it = result.iterator();
            while (it.hasNext()) {
                Node child = it.next();
                if (parent != child) {
                    if (subsumes(parent, child)) {
                        it.remove();
                    }
                }
            }
        }
        return result;
    }

    protected ClassInfo materialize(ClassInfo classInfo) {
        Node name = classInfo.name();
        if (name.isURI() && name.getURI().equals("http://example.org/class18")) {
            System.err.println("DEBUG POINT");
        }

        Map<Node, PropertyInfo> propertyMap = createPropertyMap(name);
        normalize(propertyMap);
        for (PropertyInfo pi : propertyMap.values()) {
            Node pName = pi.name();
            Set<Node> objectTypes = pi.objectTypes();

            if (true) {
                makeUnionTypeProperty(pi);
            }
            else {
                if (objectTypes.size() > 1) {
                    Node unionTypeName = unionClassToName.get(objectTypes);
                    if (unionTypeName == null) {
                        throw new RuntimeException("Class " + name + ", property: " + pName + ": Unexpectedly found no type name for union of " + objectTypes);
                    }
                    objectTypes.clear();
                    objectTypes.add(unionTypeName);
                }
            }
        }

        for (Entry<Node, PropertyInfo> e : propertyMap.entrySet()) {
            PropertyInfo pi = e.getValue();
            Set<Node> objectTypes = pi.objectTypes();
            if (objectTypes.size() > 1) {
                throw new IllegalStateException("Unexpected multivalued property on class " + classInfo.name() + ": " + pi.name() + " - " + objectTypes);
            }
        }

        ClassInfo result = new ClassInfo(name, propertyMap, new LinkedHashSet<>());
        return result;
    }

    protected ObjectTypeDefinition convertType(ClassInfo classInfo) {
        Node nameNode = classInfo.name();
        String name = toName(nameNode);

        Directive dir = Directive.newDirective()
            .name("uri")
            .argument(Argument.newArgument("value", StringValue.of(toURI(nameNode))).build())
            .build();

        if (name.startsWith("class") || name.equals("untyped")) {
            dir = null;
        }

        List<Type> implementz = classInfo.superTypes().stream()
            .map(t -> TypeName.newTypeName(toName(t)).build())
            .map(t -> (Type)t)
            .toList();

        List<FieldDefinition> fieldDefs =
            classInfo.propertyMap().values().stream()
            .map(this::convertProperty)
            .collect(Collectors.toCollection(ArrayList::new));

        // Add the uri field
        String FieldName_URI = "uri";
        FieldDefinition uriField = FieldDefinition.newFieldDefinition()
            .name(FieldName_URI)
            .directive(Directive.newDirective().name("to").build())
            .type(TYPE_SCALAR)
            .build();

        boolean hasUriField = fieldDefs.stream().anyMatch(x -> x.getName().equals(FieldName_URI));
        if (!hasUriField) {
            fieldDefs.add(0, uriField);
        }

        if (fieldDefs.isEmpty()) {
            FieldDefinition dummyField = FieldDefinition.newFieldDefinition()
                .name("_" + name + "_dummy")
                .type(TYPE_SCALAR)
                .build();
            fieldDefs.add(dummyField);
        }

        Builder resultBuilder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name(name)
            .implementz(implementz)
            .fieldDefinitions(fieldDefs);
            // .definitions(fieldDefs)
            //.directive(dir)

        if (dir != null) {
            resultBuilder = resultBuilder.directive(dir);
        }

        ObjectTypeDefinition result = resultBuilder.build();
//        InterfaceTypeDefinition result = InterfaceTypeDefinition.newInterfaceTypeDefinition()
//            .name(name)
//            .implementz(implementz)
//            // .fieldDefinitions(fieldDefs)
//            .definitions(fieldDefs)
//            .directive(dir)
//            .build();

        return result;
    }


    public static String toURI(Node node) {
        String result = node.isURI()
            ? node.getURI()
            : node.isLiteral()
                ? node.getLiteralLexicalForm()
                : null;

        Objects.requireNonNull(result);

        return result;
    }

    protected FieldDefinition convertProperty(PropertyInfo propertyInfo) {
        Set<Node> objectTypes = propertyInfo.objectTypes();
        if (objectTypes.size() > 1) {
            throw new IllegalStateException("Property had more than 1 type; multiple types should have been combined into a single new one: " + propertyInfo);
        }

        Node objectType = objectTypes.isEmpty() ? null : objectTypes.iterator().next();

        FieldDefinition result;
        String name = toName(propertyInfo.name());

        Directive dir = Directive.newDirective()
            .name("uri")
            // StringValue.of(toURI(objectType))
            .argument(Argument.newArgument("value", StringValue.of(toURI(propertyInfo.name()))).build())
            .build();

        if (objectType != null) {
            String pTypeName = toName(objectType);
            Type type = TypeName.newTypeName(pTypeName).build();

            if (propertyInfo.maxResourceCard || propertyInfo.maxLiteralCard) {
                type = ListType.newListType(type).build();
            }

            result = FieldDefinition.newFieldDefinition()
                .name(name)
                .type(type)
                .directive(dir)
                .build();
        } else {
            Type type = TYPE_SCALAR;
            if (propertyInfo.maxResourceCard || propertyInfo.maxLiteralCard) {
                type = ListType.newListType(type).build();
            }

            result = FieldDefinition.newFieldDefinition()
                .name(name)
                .type(type)
                .directive(dir)
                .build();
        }
        // TODO Cardinality

        return result;
    }

    /**
     * Creates a union type for the given types. Thereby conflicting properties are resolved by possibly splitting the
     * involved type definitions into common non-conflicting types.
     *
     * originalType := commonNonConflictingType UNION originalRemainingType
     * resolvedType := commonNonConflictingType UNION newlyIntroducedType
     *
     */
    protected Node createUnionType(Set<Node> types) {
        types = new LinkedHashSet<>(types);
        // Check if there already is an entry for the given set of types.
        Node result = unionClassToName.get(types);

        if (result == null) {
            if (types.size() == 0) {
                result = getOrCreateUnionType(types);
            } else if (types.size() == 1) {
                result = types.iterator().next();
            } else {
                // Allocate a type name for the input types
                result = getOrCreateUnionType(types);
                ClassInfo resultCi = classMap.get(result);

                // Create the combined property map across the classes -
                // the property types are sets
                Map<Node, PropertyInfo> propertyMap = createPropertyMap(types);
                normalize(propertyMap);

                // Create union types for the property types
                // Replace each set of object types with a single union object type.
                // for (PropertyInfo pi : new ArrayList<>(conflictPropertyMap.values())) {
                for (PropertyInfo pi : propertyMap.values()) {
                    makeUnionTypeProperty(pi);
                }

                // Find all properties that differ from the original definition
                Set<Node> conflictProperties = new LinkedHashSet<>();

                for (Node typeName : types) {
                    Map<Node, PropertyInfo> pm = createPropertyMap(typeName);
                    for (Entry<Node, PropertyInfo> e : propertyMap.entrySet()) {
                        Node p = e.getKey();
                        Set<Node> allSeenObjectTypes = e.getValue().objectTypes();
                        Set<Node> allSeenDObjectTypes = e.getValue().objectDatatypes();

                        PropertyInfo pi = pm.get(p);
                        // ClassInfo ci = classMap.get(typeName);
                        // PropertyInfo pi = ci.propertyMap().get(p);
                        if (pi != null) {
                            Set<Node> objectTypes = pi.objectTypes();
                            Set<Node> objectDatatypes = pi.objectDatatypes();

                            if (!objectTypes.equals(allSeenObjectTypes) || !objectDatatypes.equals(allSeenDObjectTypes)) {
                                conflictProperties.add(p);
                            }
                        }
                    }
                }

                // Sever conflicting properties from the original types
                Set<Node> safeTypes = types.stream()
                    .map(type -> severProperty(type, conflictProperties))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

                // Create a new type from the conflicting properties
                Map<Node, PropertyInfo> conflictPropertyMap = propertyMap.entrySet().stream()
                    .filter(e -> conflictProperties.contains(e.getKey()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

                // Create a new type that contains all conflicting properties
                Node conflictResolvedType;
                if (!conflictPropertyMap.isEmpty()) {
                    // getOrCreateStructuralType indexes by properties! must not change the object types!
                    conflictResolvedType = getOrCreateStructuralType(conflictPropertyMap);
                    safeTypes.add(conflictResolvedType);
                }

                // Create a new type from the union of all safe types
                // result = getOrCreateType(safeTypes);

                // Also map the original types to the resolved type
                Node safeTypeName = unionClassToName.get(safeTypes);
                if (safeTypeName != null) {
//                    if (!safeTypeName.equals(result)) {
//                        throw new RuntimeException("Based on " + types + ": safe type name already used: " + safeTypes + " mapped to " + safeTypeName + " and attempt to override with " + result);
//                    }
                    result = safeTypeName;
                    unionClassToName.put(safeTypes, result);
                } else {
                    unionClassToName.put(safeTypes, result);
                }

                // unionClassToName.put(types, result);

                // TODO Create union types for properties with set types

                if (safeTypes.stream().map(x -> x.toString()).anyMatch(x -> x.contains("class15"))) {
                    System.err.println("DEBUG POINT");
                }

                resultCi.superTypes().addAll(safeTypes);

                // Sanity check: materialization is expected to work
                // Update: No, it may fail because some types may not be defined yet
                // only the name exists but properties and super classes may not have been set yet.
                // materialize(resultCi);

                // Get or create a type with the conflicting properties mapped to union types
    //            for (Node type : types) {
    //            	severProperty(result, conflictingProperties);
    //            }
                System.err.println("Union: " + types);
                System.err.println("  PropertyMap: " + propertyMap);
            }
        }
        return result;
    }

    protected Node getEmptyType() {
        Node result = EMPTY;
        classMap.computeIfAbsent(result, r -> new ClassInfo(r, new LinkedHashMap<>(), new LinkedHashSet<>()));
        return result;
    }

    protected Node getOrCreateUnionType(Set<Node> superTypes) {
        Node result;
        if (superTypes.size() == 0) {
            result = getEmptyType();
        } else if (superTypes.size() == 1) {
            result = superTypes.iterator().next();
        } else {
            Set<Node> superTypesKey = new LinkedHashSet<>(superTypes);
            result = unionClassToName.computeIfAbsent(superTypesKey, set -> {
                Node r = allocateClassName();
                ClassInfo ci = new ClassInfo(r, new LinkedHashMap<>(), new LinkedHashSet<>());
                classMap.put(r, ci);
                return r;
            });
        }
        return result;
    }

    protected Node getOrCreateStructuralType(Map<Node, PropertyInfo> propertyMap) {
        Set<PropertyInfo> propertySet = propertyMap.values().stream()
            .map(x -> x.clone())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (propertySet.isEmpty()) {
            return getEmptyType();
        }

        // XXX Perhaps reuse registerClass method? (blocks use of propertiesToClass.computeIfAbsent).
        return propertiesToStructuralClass.computeIfAbsent(propertySet, pMap -> {
            Node r = allocateClassName();
            ClassInfo ci = new ClassInfo(r, propertyMap, new LinkedHashSet<>());
            classMap.put(r, ci);
            return r;
        });
    }

    /** Given a class with property p, create a new class b without p.
     *  A then becomes b union p. Returns the new class without the property.
     *
     *  This is a transitive operation that traverses all super classes and severs the properties from them.
     *
     *  Returns the severed class name.
     */
    protected Node severProperty(Node cls, Set<Node> exclusions) {
        Node result;
        if (exclusions.isEmpty()) {
            result = cls;
        } else {
            ClassInfo ci = classMap.get(cls);

            // createUnionTypes for each property
            for (PropertyInfo pi : ci.propertyMap.values()) {
                makeUnionTypeProperty(pi);
            }

            Map<Node, PropertyInfo> safeProperties = new LinkedHashMap<>();
            Map<Node, PropertyInfo> conflictProperties = new LinkedHashMap<>();
            ci.propertyMap().forEach((p, pi) -> {
                if (exclusions.contains(p)) {
                    conflictProperties.put(p, pi);
                } else {
                    safeProperties.put(p, pi);
                }
            });

            // Issue: It can erroneously happen that newProperties is empty and still a new super type was created

            Set<Node> oldSuperTypes = new LinkedHashSet<>(ci.superTypes());
            Set<Node> newSuperTypes = oldSuperTypes.stream()
                .map(superType -> severProperty(superType, exclusions))
                .collect(Collectors.toCollection(LinkedHashSet::new));

                // Cases to consider:
                // - There were no super types and all properties were in conflict -> returned safe type is the empty type
                // - For class A, There were no super types, and some but not all properties were in conflict ->
                //     Create a structural class B with the non conflict properties
                //     Remove conflicting properties from A state A extends B
                //     Return B

                if (newSuperTypes.equals(oldSuperTypes) && conflictProperties.isEmpty()) {
                // if (newProperties.isEmpty()) {
                    result = cls;
                } else if (conflictProperties.isEmpty() && newSuperTypes.size() == 1) {
                    // If only one super type remained and there are no additional properties then the super class becomes the result
                    result = newSuperTypes.iterator().next();
                } else if (newSuperTypes.size() > 1 && conflictProperties.isEmpty() && safeProperties.isEmpty()) {
                    result = getOrCreateUnionType(newSuperTypes);
                } else {
                    // if nothing was severed return the original class name
                    // to determine this, we need to sever the properties from the super classes
//                    if (newProperties.isEmpty()) {
//                        result = cls;
//                    } else {

                    // The name of the class without conflicting properties
                    Node conflictFreeClassName = allocateExclusionClassName(cls, exclusions);

                    // Issue: An exclusion class could also be identified by its structure - so its two different approaches to refer to the
                    // same thing!

                    // The exclusion class might already be a super class of the initial one!
                    if (oldSuperTypes.contains(conflictFreeClassName)) {
                        result = cls;
                    } else {
                        if (conflictProperties.values().stream().anyMatch(x -> x.objectTypes().size() > 1)) {
                            System.err.println("DEBUG POINT 3");
                        }

                        ClassInfo conflictFreeCi = classMap.get(conflictFreeClassName);
                        if (conflictFreeCi == null) {
                            if (true) { throw new RuntimeException("should not happen"); }
                            conflictFreeCi = new ClassInfo(conflictFreeClassName, conflictProperties, new LinkedHashSet<>());
                            classMap.put(conflictFreeClassName, conflictFreeCi);
                        } else {
                            System.err.println("Found existing exclusion class: " + conflictFreeCi);
                        }

                        if (newSuperTypes.isEmpty()) {
                            // result = getEmptyType();
                            result = conflictFreeClassName;
                        } else {
                            if (!Objects.equals(conflictFreeClassName, EMPTY)) {
                                newSuperTypes.add(conflictFreeClassName);
                            }
                            result = createUnionType(newSuperTypes);

                            if (false) {
                                if (conflictProperties.isEmpty()) {
                                    conflictFreeCi.superTypes().addAll(newSuperTypes);
                                    result = conflictFreeClassName;
    //                                result = createUnionType(newSuperTypes);
    //                                newSuperTypes.clear();
    //                                newSuperTypes.add(result);
                                } else {

                                // Create a new type that only contains the new super types
                                // result = getOrCreateUnionType(newSuperTypes);
        //	                    if (!newProperties.isEmpty()) {
                                    Node tmp = allocateClassName();
                                    ClassInfo newCi = new ClassInfo(tmp, new LinkedHashMap<>(), new LinkedHashSet<>());
                                    classMap.put(tmp, newCi);
                                    newCi.superTypes().addAll(new LinkedHashSet<>(newSuperTypes));
                                    // newCi.superTypes().add(result);
                                    newCi.propertyMap().putAll(conflictProperties);
                                    result = tmp;
                                }
                            }
                        }

                        if (newSuperTypes.contains(ci.name())) {
                            System.err.println("DEBUG POINT 4");
                        }

                        // 'Move' all conflicting properties to a new super type of cls.
                        ci.propertyMap().clear();
                        ci.propertyMap().putAll(safeProperties);

    //                    if (newSuperTypes.stream().map(x -> x.toString()).anyMatch(x -> x.contains("class15"))) {
    //                        System.err.println("DEBUG POINT");
    //                    }

                        ci.superTypes().clear();
                        ci.superTypes().addAll(new LinkedHashSet<>(newSuperTypes));

                        // Add the type containing the severed properties as a super type
                        // Note: This will change the order of the properties - i.e. conflicting properties
                        // will be grouped to the end
                        ci.superTypes().add(conflictFreeClassName);

                        if (ci.superTypes().stream().map(x -> x.toString()).anyMatch(x -> x.contains("class15"))) {
                            System.err.println("DEBUG POINT");
                        }
                    }
                    // result = conflictFreeClassName;
                }
        }
        ClassInfo xxx = classMap.get(result);
        materialize(xxx);
        return result;
    }

    int classCounter = 0;

    protected Node allocateClassName() {
        if (classCounter == 30) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 21) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 22) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 116) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 16) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 17) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 21) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 37) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 117) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 11) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 7) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 0) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 19) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 8) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 37) {
            System.err.println("DEBUG POINT");
        }
        if (classCounter == 103) {
            System.err.println("DEBUG POINT");
        }
        return NodeFactory.createURI("http://example.org/class" + (classCounter++));
    }

    /** Allocate a name based on the remaining properties. */
    protected Node allocateExclusionClassName(Node baseName, Set<Node> exclusionProperties) {
        // Node result = getOrCreateStructuralType(finalMap);
        ExclusionType et = new ExclusionType(baseName, new LinkedHashSet<>(exclusionProperties));
        // Node result = exclusionTypeMap.computeIfAbsent(et, x -> allocateClassName());
        Node result = exclusionTypeMap.computeIfAbsent(et, x -> {
            Map<Node, PropertyInfo> pMap = createPropertyMap(baseName);
            Map<Node, PropertyInfo> finalMap = pMap.entrySet().stream()
                .filter(e -> !exclusionProperties.contains(e.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            Node r = getOrCreateStructuralType(finalMap);
            return r;
        });
        return result;
    }

    /** Allocate a name based on the base class name and the set of excluded properties. */
    protected Node allocateExclusionClassNameOld(Node baseName, Set<Node> exclusionProperties) {
        Map<Node, PropertyInfo> pMap = createPropertyMap(baseName);
        Map<Node, PropertyInfo> finalMap = pMap.entrySet().stream()
            .filter(e -> !exclusionProperties.contains(e.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        ExclusionType et = new ExclusionType(baseName, new LinkedHashSet<>(exclusionProperties));
        Node result = exclusionTypeMap.computeIfAbsent(et, x -> allocateClassName());
        return result;
    }

    protected Map<Node, PropertyInfo> createPropertyMap(Node type) {
        Map<Node, PropertyInfo> result = new HashMap<>();
        collectPropertyMap(type, result);
        return result;
    }

    protected void normalize(Map<Node, PropertyInfo> propertyMap) {
        propertyMap.values().forEach(this::normalize);
    }

    protected void normalize(PropertyInfo pi) {
        Set<Node> before = pi.objectTypes();
        Set<Node> after = normalize(before);
        pi.objectTypes().clear();
        pi.objectTypes().addAll(after);
    }

    /** Compute the set of properties and the union of all mentioned types for the given classes. */
    protected Map<Node, PropertyInfo> createPropertyMap(Set<Node> types) {
        Map<Node, PropertyInfo> result = new HashMap<>();
        collectPropertyMap(types, result);
        return result;
    }

    protected void collectPropertyMap(Set<Node> types, Map<Node, PropertyInfo> result) {
        for (Node type : types) {
            collectPropertyMap(type, result);
        }
    }

    protected void collectPropertyMap(Node type, Map<Node, PropertyInfo> result) {
        ClassInfo ci = classMap.get(type);
        if (ci == null) {
            throw new IllegalStateException("No class info found for: " + type);
        }

        Multimap<Node, Node> propertyTypes = HashMultimap.create();
        Multimap<Node, Node> propertyDTypes = HashMultimap.create();
        Set<Node> properties = new HashSet<>();
        Map<Node, Boolean> propertyRCard = new HashMap<>();
        Map<Node, Boolean> propertyLCard = new HashMap<>();


        // properties.addAll(ci.properties().keySet());
        ci.propertyMap().forEach((property, typeInfo) -> {
            properties.add(property);
            propertyTypes.putAll(property, typeInfo.objectTypes());
            propertyDTypes.putAll(property, typeInfo.objectDatatypes());
            propertyLCard.compute(property, (k, v) -> (v == null ? false : v) || typeInfo.maxLiteralCard());
            propertyRCard.compute(property, (k, v) -> (v == null ? false : v) || typeInfo.maxResourceCard());
        });

        for (Node p : properties) {
            Set<Node> objectTypes = (Set<Node>)propertyTypes.get(p);
            Set<Node> objectDTypes = (Set<Node>)propertyDTypes.get(p);
            boolean rCard = propertyRCard.get(p);
            boolean lCard = propertyLCard.get(p);

            PropertyInfo pi = new PropertyInfo(p, objectTypes, rCard, objectDTypes, lCard);

            PropertyInfo before = result.get(p);
            if (before != null) {
                before.objectTypes().addAll(objectTypes);
                before.objectDatatypes().addAll(objectDTypes);
                pi = before;
//                if (!before.equals(pi)) {
//                    throw new IllegalStateException("Inconistent property definition: " + before + " -> " + pi);
//                }
            }

            result.put(p, pi);
        }

        Set<Node> superTypes = ci.superTypes();
        System.err.println("Collecting property map for super types of " + type + ": " + superTypes);
        collectPropertyMap(superTypes, result);
    }

    public GraphQlSchemaGenerator setDatasetMetadata(DatasetMetadata datasetMetadata) {
        this.datasetMetadata = datasetMetadata;
        return this;
    }
}


//public void run() {
//    VoidDataset v = datasetMetadata.getVoidDataset();
//    List<FieldDefinition> fieldDefinitions = new ArrayList<>();
//
//    // For each class {
//    //   add an entry to the "Query" object type definition
//    //
//    //   create a union type of all interfaces
//    // }
//
//    ObjectTypeDefinition.newObjectTypeDefinition()
//        // .fieldDefinitions(fieldDefinitions)
//        .build();
//}


//protected void createUnionClass(List<VoidClassPartition> classes) {
//// Create the union of all involved properties
//
//// Find out all properties with conflicting types:
////  The properties which have more than one most specific type.
//
////
//
//// We need a lookup whether for set of properties there already exists a generated "structural" class.
//
//// structural classes are auto generated and are names for sets of properties.
//
//// we need to be able to find the structural class that covers as many properties as possible of the structural class being generated.
//
//// and then we need to be able to split structural classes with a common core and then two subclasses - one of the original
//// conflicting properties and one for the other conflicting ones.
//
//// ResolvedType implements CommonNonConflictingProperties, ResolvedConflictingProperties
//
//
//}
//
//public void resolveConflicts() {
//
//}

// From the result set we know:
// - the subject type sets (STS)
// - the properties for each STS
// - the set of target types for each STS

// In graphql we want to be able to start a query which each available RDF type.

// So we need to break down the summary on the individual classes.
// The possibly advantage of the co-occurrent types is that we may be able to group related things together.

// For each class st, we iterate each property p. We can check whether the object property types are consisent
// for all types co-occurent with st.



//String b = "void/sportal/compact/";
//List<String> voidAll = List.of("qb2.rq", "qbAllBut2.rq", "qc3.rq", "qc5.rq" ,"qcAllBut35.rq", "qdAll.rq", "qeAll.rq", "qf10.rq", "qf1.rq", "qf2.rq", "qf3.rq", "qf4.rq", "qf5.rq", "qf6.rq", "qf7.rq", "qf8.rq", "qf9.rq", "qx1.rq");
//List<Query> voidQueries = voidAll.stream().map(s -> b + s).map(SparqlStmtMgr::loadQuery).toList();
//
//DatasetMetadata meta = DatasetMetadata.fetch(RdfDataSources.of(model),
//    voidQueries,
//    DatasetMetadata.defaultShaclQueries);
//RDFDataMgr.write(System.err, meta.getVoidDataset().getModel(), RDFFormat.TURTLE_PRETTY);
//
//for (VoidClassPartition cp : meta.getVoidDataset().getClassPartitionMap().values()) {
//    Node classNode = cp.getVoidClass();
//    System.err.println(classNode);
//
//    for (VoidPropertyPartition ppRaw : cp.getPropertyPartitions()) {
//        VoidPropertyPartitionX pp = ppRaw.as(VoidPropertyPartitionX.class);
//
//        Node propertyNode = pp.getVoidProperty();
//        if (propertyNode == null || !propertyNode.getURI().endsWith("nextProcess")) {
//            continue;
//        }
//
//        System.err.println("  " + pp.getVoidProperty());
//        Map<Node, VoidClassPartition> cpMap = pp.getClassPartitionMap();
//
//        // Node propertyNode = pp.getVoidProperty();
//
//        for (VoidClassPartition ppcp : pp.getClassPartitionMap().values()) {
//            System.err.println("    " + ppcp.getVoidClass());
//        }
//        // propertyPartition.get
//    }
//
//}


// For each class:
// Collect all of its properties
// For each property, collect all target types
// A minimal class defines just one property with one type.

// So the question is: should we group individual properties into larger classes (bottom-up)
// or should be split conflicting classes (top-down)?
//Set<Node> ps = typeInfos.stream().map(TypeInfo::property).collect(Collectors.toSet());
//Set<Node> tts = typeInfos.stream().map(TypeInfo::objectTypes).flatMap(Collection::stream).collect(Collectors.toSet());
//Set<Node> ods = typeInfos.stream().map(TypeInfo::objectDatatypes).flatMap(Collection::stream).collect(Collectors.toSet());



//sIndex.forEach((k, v) -> {
//System.err.println(k + ": " + v);

//Set<Node> ots = pi.objectTypes();
//if (ots.size() > 1) {
//  System.err.println("Trying to create union type for: " + ots);
//  if ("[\"untyped\", http://www.w3.org/2002/07/owl#Class]".equals(ots.toString())) {
//      System.err.println("DEBUG POINT");
//  }
//
//  if ("[http://example.org/class1, \"untyped\"]".equals(ots.toString())) {
//      System.err.println("DEBUG POINT 5");
//  }
//
//  Node newType = createUnionType(ots);
//  // TODO Replace the type of pi with the new type
//  pi.objectTypes().clear();
//  pi.objectTypes().add(newType);
//}

//
//class TypeModel {
//  protected Map<Node, TypeDefinition> typeDefinitionMap = new LinkedHashMap<>();
//
//}
//
//class PropertyDefinition {
//  protected TypeModel typeModel;
//  // protected TypeDefinition type;
//
//  protected String name;
//  protected List<Node> typeNames = new ArrayList<>();
//
//  // TypeDefinition type,
//  public PropertyDefinition(String name) {
//      super();
//      this.name = name;
//  }
//}
//
//class TypeDefinition {
//  // protected TypeModel typeModel;
//
//  protected Node name;
//  protected List<Node> superTypeNames = new ArrayList<>();
//
//  // Defined properties on this type
//  protected Map<Node, PropertyDefinition> fieldDefinitionMap = new LinkedHashMap<>();
//
//  public TypeDefinition(Node name) {
//      super();
//      this.name = name;
//  }
//
//  public Node getName() {
//      return name;
//  }
//
//  public List<Node> getSuperTypeNames() {
//      return superTypeNames;
//  }
//
//  public Map<Node, PropertyDefinition> getFieldDefinitionMap() {
//      return fieldDefinitionMap;
//  }
//}

// protected Map<Set<Node>, Node> superClassesToClass = new HashMap<>();

/*
public static void main(String[] args) {
    // Model model = RDFDataMgr.loadModel("/home/raven/Datasets/demo-orowan/demo-orowan_RDF_graph_data.rdf.void.ttl");
    Model model = RDFDataMgr.loadModel("/home/raven/Datasets/demo-orowan/demo-orowan_RDF_graph_data.rdf");
    List<TypeInfo> summary = summarize(RdfDataSources.of(model));
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    schemaGenerator.process(summary);
}
*/

// protected Map<Set<Node>, Node> rawToResolvedType = new HashMap<>();


