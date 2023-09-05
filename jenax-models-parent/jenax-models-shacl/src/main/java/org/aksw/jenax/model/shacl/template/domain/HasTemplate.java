package org.aksw.jenax.model.shacl.template.domain;

import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * A class to attache a template string to an RDF resource.
 * In the case of shacl, the template may be specified on a 'view' resource that references a shacl shape.
 * For convenience it may also be viable to use this property on a shacl shape directly.
 */
@ResourceView
public interface HasTemplate
    extends Resource
{
    @Iri(ShaclTemplateTerms.template)
    Set<Node> getTemplateNodes();

    /** Get a template that matches the given datatype. Raises an exception if there is more than one match. */
    default String getTemplate(String datatype) {
        Set<Node> nodes = getTemplateNodes();
        Set<Node> matches = nodes.stream().filter(Node::isLiteral).filter(node -> node.getLiteralDatatypeURI().equals(datatype)).collect(Collectors.toSet());
        Node match = IterableUtils.expectZeroOrOneItems(matches);
        String result = match == null ? null : match.getLiteralLexicalForm();
        return result;
    }
}
