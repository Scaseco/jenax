package org.aksw.jenax.io.kryo.jena;

import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.Tuple0;
import org.apache.jena.atlas.lib.tuple.Tuple1;
import org.apache.jena.atlas.lib.tuple.Tuple2;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.Tuple4;
import org.apache.jena.atlas.lib.tuple.Tuple5;
import org.apache.jena.atlas.lib.tuple.Tuple6;
import org.apache.jena.atlas.lib.tuple.Tuple7;
import org.apache.jena.atlas.lib.tuple.Tuple8;
import org.apache.jena.atlas.lib.tuple.TupleN;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_ANY;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.Binding0;
import org.apache.jena.sparql.engine.binding.Binding1;
import org.apache.jena.sparql.engine.binding.Binding2;
import org.apache.jena.sparql.engine.binding.Binding3;
import org.apache.jena.sparql.engine.binding.Binding4;
import org.apache.jena.sparql.engine.binding.BindingOverMap;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_Datatype;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDateTime;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDecimal;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDouble;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDuration;
import org.apache.jena.sparql.expr.nodevalue.NodeValueFloat;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueLang;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.expr.nodevalue.NodeValueSortKey;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.google.gson.Gson;

/**
 * Note: KryoRegistrator is an interface introduced by spark; hence we cannot use it
 * in this common package.
 */
public class JenaKryoRegistratorLib {
    public static void registerClasses(Kryo kryo) {
        // The NodeValue list should be complete for jena 3.17.0
        Serializer<Expr> exprSerializer = new ExprSerializer();

        kryo.register(NodeValueBoolean.class, exprSerializer);
        kryo.register(NodeValueDecimal.class, exprSerializer);
        kryo.register(NodeValueDouble.class, exprSerializer);
        kryo.register(NodeValueDateTime.class, exprSerializer);
        kryo.register(NodeValueDuration.class, exprSerializer);
        kryo.register(NodeValueFloat.class, exprSerializer);
        kryo.register(NodeValueInteger.class, exprSerializer);
        kryo.register(NodeValueLang.class, exprSerializer);
        kryo.register(NodeValueNode.class, exprSerializer);
        kryo.register(NodeValueSortKey.class, exprSerializer);
        kryo.register(NodeValueString.class, exprSerializer);

        // This list is incomplete - use class path scanning?
        // However class path scanning would probably slows down startup time alot for that number of classes...
        kryo.register(ExprVar.class, exprSerializer);
        kryo.register(E_Equals.class, exprSerializer);
        kryo.register(E_BNode.class, exprSerializer);
        kryo.register(E_Datatype.class, exprSerializer);
        kryo.register(E_IRI.class, exprSerializer);
        kryo.register(E_Datatype.class, exprSerializer);
        kryo.register(E_StrConcat.class, exprSerializer);
        kryo.register(Expr.class, exprSerializer);

        kryo.register(VarExprList.class, new VarExprListSerializer());
        kryo.register(SortCondition.class, new SortConditionSerializer());

        kryo.register(Query.class, new QuerySerializer());

        // Using allowValues false in order to retain RDF terms exactly
        // Serializer<Node> nodeSerializer = new GenericNodeSerializerViaThrift(false);
        Serializer<Node> nodeSerializer = new GenericNodeSerializerCustom();


        registerNodeSerializers(kryo, nodeSerializer);
        kryo.register(Var.class, new VarSerializer());
        kryo.register(Node_Variable.class, new VariableNodeSerializer());
        kryo.register(Node_ANY.class, new ANYNodeSerializer());

        kryo.register(Node[].class); //, new NodeArraySerializer());

        kryo.register(Triple.class, new TripleSerializer());
        kryo.register(org.apache.jena.graph.Triple[].class);

        kryo.register(PrefixMappingImpl.class, new PrefixMappingSerializer(Lang.TURTLE, RDFFormat.TURTLE_PRETTY));

        kryo.register(ModelCom.class, new ModelSerializer(Lang.RDFTHRIFT, RDFFormat.RDF_THRIFT_VALUES));
        kryo.register(DatasetImpl.class, new DatasetSerializer(Lang.RDFTHRIFT, RDFFormat.RDF_THRIFT_VALUES));
        kryo.register(DatasetOneNgImpl.class, new DatasetOneNgSerializer());

        Serializer<Binding> bindingSerializer = new BindingSerializer();
        kryo.register(BindingRoot.class, bindingSerializer);
        kryo.register(Binding0.class, bindingSerializer);
        kryo.register(Binding1.class, bindingSerializer);
        kryo.register(Binding2.class, bindingSerializer);
        kryo.register(Binding3.class, bindingSerializer);
        kryo.register(Binding4.class, bindingSerializer);
        kryo.register(BindingOverMap.class, bindingSerializer);
        // kryo.register(BindingHashMap.class, bindingSerializer);

        Serializer<Tuple<Node>> tupleOfNodesSerializer = new TupleSerializer<>(Node.class);
        kryo.register(Tuple.class, tupleOfNodesSerializer);
        kryo.register(Tuple0.class, tupleOfNodesSerializer);
        kryo.register(Tuple1.class, tupleOfNodesSerializer);
        kryo.register(Tuple2.class, tupleOfNodesSerializer);
        kryo.register(Tuple3.class, tupleOfNodesSerializer);
        kryo.register(Tuple4.class, tupleOfNodesSerializer);
        kryo.register(Tuple5.class, tupleOfNodesSerializer);
        kryo.register(Tuple6.class, tupleOfNodesSerializer);
        kryo.register(Tuple7.class, tupleOfNodesSerializer);
        kryo.register(Tuple8.class, tupleOfNodesSerializer);
        kryo.register(TupleN.class, tupleOfNodesSerializer);

        Gson gson = new Gson();

        //kryo.register(org.apache.jena.rdf.model.RDFNode.class, new RDFNodeSerializer<>(Function.identity(), gson));
        //kryo.register(org.apache.jena.rdf.model.Resource.class, new RDFNodeSerializer<>(RDFNode::asResource, gson));
        //kryo.register(org.apache.jena.rdf.model.impl.R.class, new RDFNodeSerializer<>(RDFNode::asResource, gson));
        kryo.register(ResourceImpl.class, new RDFNodeSerializer<>(RDFNode::asResource, gson));
        kryo.register(PropertyImpl.class, new RDFNodeSerializer<>(n -> ResourceFactory.createProperty(n.asResource().getURI()), gson));
        kryo.register(LiteralImpl.class, new RDFNodeSerializer<>(RDFNode::asLiteral, gson));


        // Serializers for sending encountered exceptions over a wire
        kryo.register(QueryExceptionHTTP.class, new QueryExceptionHTTPSerializer());
    }


    public static void registerNodeSerializers(Kryo kryo, Serializer<Node> nodeSerializer) {
        kryo.register(Node.class, nodeSerializer);
        kryo.register(Node_Blank.class, nodeSerializer);
        kryo.register(Node_URI.class, nodeSerializer);
        kryo.register(Node_Literal.class, nodeSerializer);
        kryo.register(Node_Triple.class, nodeSerializer);
    }
}
