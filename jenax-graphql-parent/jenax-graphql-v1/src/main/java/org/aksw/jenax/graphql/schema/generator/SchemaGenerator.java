package org.aksw.jenax.graphql.schema.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.node.NodeCollection;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.graphql.sparql.DatasetMetadata;
import org.aksw.jenax.model.voidx.api.VoidClassPartition;
import org.aksw.jenax.model.voidx.api.VoidDataset;
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
import graphql.language.ObjectTypeDefinition;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeName;
//
//class TypeModel {
//    protected Map<Node, TypeDefinition> typeDefinitionMap = new LinkedHashMap<>();
//
//}
//
//class PropertyDefinition {
//    protected TypeModel typeModel;
//    // protected TypeDefinition type;
//
//    protected String name;
//    protected List<Node> typeNames = new ArrayList<>();
//
//    // TypeDefinition type,
//    public PropertyDefinition(String name) {
//        super();
//        this.name = name;
//    }
//}
//
//class TypeDefinition {
//    // protected TypeModel typeModel;
//
//    protected Node name;
//    protected List<Node> superTypeNames = new ArrayList<>();
//
//    // Defined properties on this type
//    protected Map<Node, PropertyDefinition> fieldDefinitionMap = new LinkedHashMap<>();
//
//    public TypeDefinition(Node name) {
//        super();
//        this.name = name;
//    }
//
//    public Node getName() {
//        return name;
//    }
//
//    public List<Node> getSuperTypeNames() {
//        return superTypeNames;
//    }
//
//    public Map<Node, PropertyDefinition> getFieldDefinitionMap() {
//        return fieldDefinitionMap;
//    }
//}



public class SchemaGenerator {

    protected ShortNameMgr shortNameMgr = new ShortNameMgr();
    // Issue/Limitation: When we split a type, then we may not be able to preserve the property order

    // static { JenaSystem.init(); }

    protected DatasetMetadata datasetMetadata;

    public record TypeInfo(Set<Node> subjectTypes, Node property, Set<Node> objectTypes, long maxResourceCard, Set<Node> objectDatatypes, long maxLiteralCard) {}

    public record ClassInfo(Node name, Map<Node, PropertyInfo> propertyMap, Set<Node> superTypes) {}
    public record PropertyInfo(Node name, Set<Node> objectTypes, long maxResourceCard, Set<Node> objectDatatypes, long maxLiteralCard) {}

    protected Map<Node, ClassInfo> classMap = new HashMap<>();

    protected Map<Set<Node>, Node> unionClassToName = new HashMap<>();

    /** Artificial classes referred to by their properties. */
    protected Map<Set<PropertyInfo>, Node> propertiesToStructuralClass = new HashMap<>();
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

    public static List<TypeInfo> summarize(RdfDataSource dataSource) {
        Query dataSummary = SparqlStmtMgr.loadQuery("data-summary.rq");
        Table table = QueryExecBuilderAdapter.adapt(dataSource.newQuery().query(dataSummary)).table();
        System.out.println(ResultSetFormatter.asText(table.toRowSet().asResultSet()));
        List<TypeInfo> result = table.toRowSet().stream().map(b -> new TypeInfo(
            (Set<Node>)NodeCollection.extractOrNull(b.get("sTypes")),
            b.get("p"),
            (Set<Node>)NodeCollection.extractOrNull(b.get("allOTypes")),
            ((Number)b.get("maxOCard").getLiteral().getValue()).longValue(),
            (Set<Node>)NodeCollection.extractOrNull(b.get("allODTypes")),
            ((Number)b.get("maxODCard").getLiteral().getValue()).longValue()))
        .toList();

        return result;
    }

    public Document process(List<TypeInfo> list) {
        init(list);

        for (ClassInfo ci : new ArrayList<>(classMap.values())) {
            for (PropertyInfo pi : new ArrayList<>(ci.propertyMap().values())) {
                 Node newType = createUnionType(pi.objectTypes());
                 // TODO Replace the type of pi with the new type
                 pi.objectTypes().clear();
                 pi.objectTypes.add(newType);
            }
        }


        // From the result set we know:
        // - the subject type sets (STS)
        // - the properties for each STS
        // - the set of target types for each STS

        // In graphql we want to be able to start a query which each available RDF type.

        // So we need to break down the summary on the individual classes.
        // The possibly advantage of the co-occurrent types is that we may be able to group related things together.

        // For each class st, we iterate each property p. We can check whether the object property types are consisent
        // for all types co-occurent with st.



//        String b = "void/sportal/compact/";
//        List<String> voidAll = List.of("qb2.rq", "qbAllBut2.rq", "qc3.rq", "qc5.rq" ,"qcAllBut35.rq", "qdAll.rq", "qeAll.rq", "qf10.rq", "qf1.rq", "qf2.rq", "qf3.rq", "qf4.rq", "qf5.rq", "qf6.rq", "qf7.rq", "qf8.rq", "qf9.rq", "qx1.rq");
//        List<Query> voidQueries = voidAll.stream().map(s -> b + s).map(SparqlStmtMgr::loadQuery).toList();
//
//        DatasetMetadata meta = DatasetMetadata.fetch(RdfDataSources.of(model),
//            voidQueries,
//            DatasetMetadata.defaultShaclQueries);
//        RDFDataMgr.write(System.out, meta.getVoidDataset().getModel(), RDFFormat.TURTLE_PRETTY);
//
//        for (VoidClassPartition cp : meta.getVoidDataset().getClassPartitionMap().values()) {
//            Node classNode = cp.getVoidClass();
//            System.out.println(classNode);
//
//            for (VoidPropertyPartition ppRaw : cp.getPropertyPartitions()) {
//                VoidPropertyPartitionX pp = ppRaw.as(VoidPropertyPartitionX.class);
//
//                Node propertyNode = pp.getVoidProperty();
//                if (propertyNode == null || !propertyNode.getURI().endsWith("nextProcess")) {
//                    continue;
//                }
//
//                System.out.println("  " + pp.getVoidProperty());
//                Map<Node, VoidClassPartition> cpMap = pp.getClassPartitionMap();
//
//                // Node propertyNode = pp.getVoidProperty();
//
//                for (VoidClassPartition ppcp : pp.getClassPartitionMap().values()) {
//                    System.out.println("    " + ppcp.getVoidClass());
//                }
//                // propertyPartition.get
//            }
//
//        }
        Document result = convert();
        return result;
    }

    protected void init(List<TypeInfo> list) {
        Multimap<Node, TypeInfo> sIndex = HashMultimap.create();
        list.forEach(item -> item.subjectTypes().forEach(st -> sIndex.put(st, item)));

        System.out.println(list.iterator().next());

        Map<Node, Collection<TypeInfo>> map = sIndex.asMap();
        for (Entry<Node, Collection<TypeInfo>> sts : map.entrySet()) {
            Node sourceType = sts.getKey();
            System.out.println("Class: " + sourceType);

            Collection<TypeInfo> typeInfos = sts.getValue();

            // Index properties of that class
            Set<Node> properties = new HashSet<>();
            Multimap<Node, Node> propertyTypes = HashMultimap.create();
            Multimap<Node, Node> propertyDTypes = HashMultimap.create();

            for (TypeInfo typeInfo : typeInfos) {
                properties.add(typeInfo.property);
                propertyTypes.putAll(typeInfo.property(), typeInfo.objectTypes());
                propertyDTypes.putAll(typeInfo.property(), typeInfo.objectDatatypes());
            }

//            Set<Node> ps = typeInfos.stream().map(TypeInfo::property).collect(Collectors.toSet());
//            Set<Node> tts = typeInfos.stream().map(TypeInfo::objectTypes).flatMap(Collection::stream).collect(Collectors.toSet());
//            Set<Node> ods = typeInfos.stream().map(TypeInfo::objectDatatypes).flatMap(Collection::stream).collect(Collectors.toSet());
            ClassInfo classInfo = new ClassInfo(sourceType, new HashMap<>(), Set.of());
            for (Node p : properties) {
                Set<Node> objectTypes = (Set<Node>)propertyTypes.get(p);
                Set<Node> objectDTypes = (Set<Node>)propertyDTypes.get(p);
                System.out.println("  Property: " + p);
                System.out.println("    Types : " + objectTypes);
                System.out.println("    DTypes: " + objectDTypes);
                if (objectTypes == null) {
                    objectTypes = Set.of();
                }

                if (objectDTypes == null) {
                    objectDTypes = Set.of();
                }

                PropertyInfo pi = new PropertyInfo(p, objectTypes, -1, objectDTypes, -1);
                classInfo.propertyMap().put(p, pi);
                // createType(objectTypes);
            }
            registerClass(classInfo);

            // For each class:
            // Collect all of its properties
            // For each property, collect all target types
            // A minimal class defines just one property with one type.

            // So the question is: should we group individual properties into larger classes (bottom-up)
            // or should be split conflicting classes (top-down)?



//        sIndex.forEach((k, v) -> {
//            System.out.println(k + ": " + v);
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


    public void createModel() {
//        for (ClassInfo ci : classMap.values()) {
//
//        }
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
        List<FieldDefinition> queryFields = classMap.values().stream()
            .map(x -> FieldDefinition.newFieldDefinition()
                    .name(toName(x.name()))
                    .type(TypeName.newTypeName(toName(x.name())).build())
                    .build())
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


    protected ObjectTypeDefinition convertType(ClassInfo classInfo) {
        Node nameNode = classInfo.name();
        String name = toName(nameNode);

        Directive dir = Directive.newDirective()
                .name("uri")
                .argument(Argument.newArgument("value", StringValue.of(toURI(nameNode))).build())
                .build();

        List<Type> implementz = classInfo.superTypes().stream()
                .map(t -> TypeName.newTypeName(toName(t)).build())
                .map(t -> (Type)t)
                .toList();

        List<FieldDefinition> fieldDefs = classInfo.propertyMap().values().stream()
                .map(this::convertProperty)
                .toList();

        ObjectTypeDefinition result = ObjectTypeDefinition.newObjectTypeDefinition()
                .name(name)
                .implementz(implementz)
                .fieldDefinitions(fieldDefs)
                .directive(dir)
                .build();

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
            throw new IllegalStateException("Property had more than 1 type; multiple types should have been combined into a single new one.");
        }

        Node objectType = objectTypes.iterator().next();

        Directive dir = Directive.newDirective()
                .name("uri")
                .argument(Argument.newArgument("value", StringValue.of(toURI(objectType))).build())
                .build();

        String pTypeName = toName(objectType);
        TypeName typeName = TypeName.newTypeName(pTypeName).build();

        // TODO Cardinality

        String name = toName(propertyInfo.name());
        FieldDefinition result = FieldDefinition.newFieldDefinition()
            .name(name)
            .type(typeName)
            .directive(dir)
            .build();

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
        // Check if there already is an entry for the given set of types.
        Node result = unionClassToName.get(types);

        if (result == null) {
            // Derive a union type over the given types:

            // Create the property where the types are sets
            Map<Node, PropertyInfo> propertyMap = createPropertyMap(types);

            // Find all properties that differ from the original definition
            Set<Node> conflictProperties = new HashSet<>();

            for (Entry<Node, PropertyInfo> e : propertyMap.entrySet()) {
                Node p = e.getKey();
                Set<Node> pos = e.getValue().objectTypes();
                for (Node typeName : types) {
                    ClassInfo ci = classMap.get(typeName);
                    PropertyInfo pi = ci.propertyMap().get(p);
                    if (pi != null) {
                        Set<Node> objectTypes = pi.objectTypes();
                        if (!objectTypes.equals(pos)) {
                            conflictProperties.add(p);
                        }
                    }
                }
            }

            Set<Node> safeTypes = types.stream()
                .map(type -> severProperty(type, conflictProperties))
                .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<Node, PropertyInfo> conflictPropertyMap = propertyMap.entrySet().stream()
                .filter(e -> conflictProperties.contains(e.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

            if (!conflictPropertyMap.isEmpty()) {
                Node conflictResolvedType = getOrCreateStructuralType(conflictPropertyMap);
                safeTypes.add(conflictResolvedType);
            }
            result = getOrCreateType(safeTypes);

            // Get or create a type with the conflicting properties mapped to union types
//            for (Node type : types) {
//            	severProperty(result, conflictingProperties);
//            }
            System.out.println("Union: " + types);
            System.out.println("  PropertyMap: " + propertyMap);
        }

        return result;
    }

    protected Node getOrCreateType(Set<Node> superTypes) {
        return unionClassToName.computeIfAbsent(superTypes, set -> {
            Node r = allocateClassName(null, null);
            ClassInfo ci = new ClassInfo(r, new LinkedHashMap<>(), superTypes);
            classMap.put(r, ci);
            return r;
        });
    }

    protected Node getOrCreateStructuralType(Map<Node, PropertyInfo> propertyMap) {
        Set<PropertyInfo> propertySet = new LinkedHashSet<>(propertyMap.values());
        // XXX Perhaps reuse registerClass method? (blocks use of propertiesToClass.computeIfAbsent).
        return propertiesToStructuralClass.computeIfAbsent(propertySet, pMap -> {
            Node r = allocateClassName(null, null);
            ClassInfo ci = new ClassInfo(r, propertyMap, Set.of());
            classMap.put(r, ci);
            return r;
        });
    }

    /** Given a class with property p, create a new class b without p.
     *  A then becomes b union p. Returns the new class without the property.
     *
     *  This is a transitive operation that traverses all super classes and severs the properties from them.
     */
    protected Node severProperty(Node cls, Set<Node> exclusions) {
        // TODO Recurse through the types implements

        Node result;
        if (exclusions.isEmpty()) {
            result = cls;
        } else {
            ClassInfo ci = classMap.get(cls);

            Map<Node, PropertyInfo> oldProperties = new LinkedHashMap<>();
            Map<Node, PropertyInfo> newProperties = new LinkedHashMap<>();
            ci.propertyMap().forEach((p, pi) -> {
                if (exclusions.contains(p)) {
                    newProperties.put(p, pi);
                } else {
                    oldProperties.put(p, pi);
                }
            });

            // The name of the class without conflicting properties
            Node conflictFreeClassName = allocateClassName(cls, exclusions);

            // The name of the class containing only the conflicting part
            Node severedClassName = allocateClassName(conflictFreeClassName, exclusions);
            result = severedClassName;
        }
        return result;
    }

    int classCounter = 0;

    protected Node allocateClassName(Node baseName, Set<Node> exclusions) {
        return NodeFactory.createURI("http://example.org/" + (classCounter++));
    }

    public static final Node UNTYPED = NodeFactory.createLiteralString("untyped");

    /** Compute the set of properties and their types for the given classes. */
    protected Map<Node, PropertyInfo> createPropertyMap(Set<Node> types) {
        Map<Node, PropertyInfo> result = new HashMap<>();
        types.forEach(type -> {
            ClassInfo ci = classMap.get(type);
            if (ci == null) {
                throw new IllegalStateException("No class info found for: " + type);
            }

            Multimap<Node, Node> propertyTypes = HashMultimap.create();
            Multimap<Node, Node> propertyDTypes = HashMultimap.create();
            Set<Node> properties = new HashSet<>();
            // properties.addAll(ci.properties().keySet());
            ci.propertyMap().forEach((property, typeInfo) -> {
                properties.add(property);
                propertyTypes.putAll(property, typeInfo.objectTypes());
                propertyDTypes.putAll(property, typeInfo.objectDatatypes());
            });

            for (Node p : properties) {
                Set<Node> objectTypes = (Set<Node>)propertyTypes.get(p);
                Set<Node> objectDTypes = (Set<Node>)propertyDTypes.get(p);
                PropertyInfo pi = new PropertyInfo(p, objectTypes, -1, objectDTypes, -1);
                result.put(p, pi);
            }
        });
        return result;
    }

    public SchemaGenerator setDatasetMetadata(DatasetMetadata datasetMetadata) {
        this.datasetMetadata = datasetMetadata;
        return this;
    }

    public void run() {
        VoidDataset v = datasetMetadata.getVoidDataset();
        List<FieldDefinition> fieldDefinitions = new ArrayList<>();

        // For each class {
        //   add an entry to the "Query" object type definition
        //
        //   create a union type of all interfaces
        // }

        ObjectTypeDefinition.newObjectTypeDefinition()
            // .fieldDefinitions(fieldDefinitions)
            .build();


    }

    protected void createUnionClass(List<VoidClassPartition> classes) {
        // Create the union of all involved properties

        // Find out all properties with conflicting types:
        //  The properties which have more than one most specific type.

        //

        // We need a lookup whether for set of properties there already exists a generated "structural" class.

        // structural classes are auto generated and are names for sets of properties.

        // we need to be able to find the structural class that covers as many properties as possible of the structural class being generated.

        // and then we need to be able to split structural classes with a common core and then two subclasses - one of the original
        // conflicting properties and one for the other conflicting ones.

        // ResolvedType implements CommonNonConflictingProperties, ResolvedConflictingProperties


    }

    public void resolveConflicts() {

    }
}
