package org.aksw.jenax.graphql.sparql.v2.schema;

import java.util.Map;

import org.aksw.jenax.graphql.sparql.v2.api2.Connective;
import org.aksw.jenax.graphql.sparql.v2.rewrite.JenaGraphQlUtils;
import org.aksw.jenax.graphql.sparql.v2.rewrite.XGraphQlUtils;
import org.aksw.jenax.graphql.sparql.v2.schema.GraphQlSchemaUtils.TypeInfo;
import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.aksw.jenax.graphql.sparql.v2.util.PrefixMap2;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

import graphql.language.FieldDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;

public class SchemaEdge {
    protected SchemaNavigator navigator;
    protected TypeDefinition<?> source;
    protected volatile PrefixMap localPrefixMap;

    protected FieldDefinition fieldDefinition;

    public SchemaEdge(SchemaNavigator navigator, TypeDefinition<?> source, FieldDefinition fieldDefinition) {
        super();
        this.navigator = navigator;
        this.source = source;
        this.fieldDefinition = fieldDefinition;
    }

    public String getName() {
        return fieldDefinition.getName();
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public boolean isCardinalityOne() {
        Type<?> targetType = fieldDefinition.getType();
        TypeInfo typeInfo = GraphQlSchemaUtils.extractTypeInfo(targetType);
        return !typeInfo.isList();
    }

    public SchemaNode getTargetSchemaNode() {
        Type<?> type = fieldDefinition.getType();
        TypeInfo typeInfo = GraphQlSchemaUtils.extractTypeInfo(type);
        String typeName = typeInfo.getTypeName();
        SchemaNode result = navigator.getOrCreateSchemaNode(typeName);
        return result;
    }

    public Connective getConnective() {
        PrefixMap localPrefixMap = PrefixMapFactory.create();
        JenaGraphQlUtils.collectPrefixes(source, localPrefixMap);
        PrefixMap finalPrefixMap = PrefixMap2.of(navigator.getBasePrefixMap(), localPrefixMap);

        // Extract the pattern
        Connective connective = XGraphQlUtils.parsePattern(fieldDefinition, finalPrefixMap);

        // If @filter is present then also get the target type's constraint
        Type<?> targetType = fieldDefinition.getType();
        if (fieldDefinition.hasDirective("filter")) {
            TypeInfo typeInfo = GraphQlSchemaUtils.extractTypeInfo(targetType);
            SchemaNode targetNode = navigator.getOrCreateSchemaNode(typeInfo.getTypeName());
            Fragment fragment = targetNode.getFragment();
            if (fragment != null) {

                Element fragmentElt = fragment.element();

                Generator<Var> vargen = VarGeneratorImpl2.create();
                Map<Var, Var> varMap = VarUtils.createJoinVarMap(
                        PatternVars.vars(connective.getElement()),
                        PatternVars.vars(fragmentElt),
                        connective.getDefaultTargetVars(),
                        fragment.vars(),
                        vargen);
                Element tmpElt = ElementUtils.applyNodeTransform(fragmentElt, new NodeTransformSubst(varMap));
                ElementGroup grp = new ElementGroup();
                ElementUtils.copyElements(grp, connective.getElement());
                ElementUtils.copyElements(grp, tmpElt);
                Element newElt = ElementUtils.groupIfNeeded(grp.getElements());
                connective = Connective.newBuilder()
                        .connectVars(connective.getConnectVars())
                        .targetVars(connective.getDefaultTargetVars())
                        .element(newElt)
                        .build();
            }
            // Create the final pattern by joining
            // the connective's 'to' variables with that of the fragment.
        }

        // TODO Think about multi-column properties
        //   such as a property connecting (city, country) -> (avgTemperature, year, quarter)

        // TODO Handle general @join between the source type and this field type
        // TODO Handle general @join between this field type and the target type


        return connective;
    }
}

