package org.aksw.jenax.io.kryo.jena;

import java.util.List;

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
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_ANY;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphPlain;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.ModelFactory;
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
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.Binding0;
import org.apache.jena.sparql.engine.binding.Binding1;
import org.apache.jena.sparql.engine.binding.Binding2;
import org.apache.jena.sparql.engine.binding.Binding3;
import org.apache.jena.sparql.engine.binding.Binding4;
import org.apache.jena.sparql.engine.binding.BindingOverMap;
import org.apache.jena.sparql.engine.binding.BindingProject;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.expr.E_Add;
import org.apache.jena.sparql.expr.E_AdjustToTimezone;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Call;
import org.apache.jena.sparql.expr.E_Cast;
import org.apache.jena.sparql.expr.E_Coalesce;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_Datatype;
import org.apache.jena.sparql.expr.E_DateTimeDay;
import org.apache.jena.sparql.expr.E_DateTimeHours;
import org.apache.jena.sparql.expr.E_DateTimeMinutes;
import org.apache.jena.sparql.expr.E_DateTimeMonth;
import org.apache.jena.sparql.expr.E_DateTimeSeconds;
import org.apache.jena.sparql.expr.E_DateTimeTZ;
import org.apache.jena.sparql.expr.E_DateTimeTimezone;
import org.apache.jena.sparql.expr.E_DateTimeYear;
import org.apache.jena.sparql.expr.E_Divide;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_FunctionDynamic;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_IRI2;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.E_IsLiteral;
import org.apache.jena.sparql.expr.E_IsNumeric;
import org.apache.jena.sparql.expr.E_IsTriple;
import org.apache.jena.sparql.expr.E_IsURI;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_MD5;
import org.apache.jena.sparql.expr.E_Multiply;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.E_Now;
import org.apache.jena.sparql.expr.E_NumAbs;
import org.apache.jena.sparql.expr.E_NumCeiling;
import org.apache.jena.sparql.expr.E_NumFloor;
import org.apache.jena.sparql.expr.E_NumRound;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.E_OpNumericIntegerDivide;
import org.apache.jena.sparql.expr.E_OpNumericMod;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_SHA1;
import org.apache.jena.sparql.expr.E_SHA224;
import org.apache.jena.sparql.expr.E_SHA256;
import org.apache.jena.sparql.expr.E_SHA384;
import org.apache.jena.sparql.expr.E_SHA512;
import org.apache.jena.sparql.expr.E_SameTerm;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrAfter;
import org.apache.jena.sparql.expr.E_StrBefore;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrContains;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrEncodeForURI;
import org.apache.jena.sparql.expr.E_StrEndsWith;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.E_StrLength;
import org.apache.jena.sparql.expr.E_StrLowerCase;
import org.apache.jena.sparql.expr.E_StrReplace;
import org.apache.jena.sparql.expr.E_StrStartsWith;
import org.apache.jena.sparql.expr.E_StrSubstring;
import org.apache.jena.sparql.expr.E_StrUUID;
import org.apache.jena.sparql.expr.E_StrUpperCase;
import org.apache.jena.sparql.expr.E_Subtract;
import org.apache.jena.sparql.expr.E_TripleFn;
import org.apache.jena.sparql.expr.E_TripleObject;
import org.apache.jena.sparql.expr.E_TriplePredicate;
import org.apache.jena.sparql.expr.E_TripleSubject;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.E_URI2;
import org.apache.jena.sparql.expr.E_UUID;
import org.apache.jena.sparql.expr.E_UnaryMinus;
import org.apache.jena.sparql.expr.E_UnaryPlus;
import org.apache.jena.sparql.expr.E_Version;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
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
import org.apache.jena.sparql.graph.GraphFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

/**
 * Note: KryoRegistrator is an interface introduced by spark; hence we cannot use it
 * in this common package.
 */
public class JenaKryoRegistratorLib {
    public static void registerClasses(Kryo kryo) {
        // The NodeValue list should be complete for jena 3.17.0
        Serializer<Expr> exprSerializer = new ExprSerializerViaString();

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

        kryo.register(ExprVar.class, exprSerializer);

        // XXX The use of this serializer here should become obsolete as it doesn't preserve java types - especially
        //  custom non-jena ones.
        // Resorting to this serializer should work for all jena types though.
        Serializer<Expr> fallbackExprSerializer = new ExprSerializerViaString();

        kryo.register(E_Add.class, new ExprFunction2Serializer<>(E_Add::new));
        kryo.register(E_AdjustToTimezone.class, new ExprFunctionNSerializer<>(args -> new E_AdjustToTimezone(args.get(0), args.get(1))));
        kryo.register(E_BNode.class, new E_BNodeSerializer());
        kryo.register(E_Bound.class, new ExprFunction1Serializer<>(E_Bound::new));
        kryo.register(E_Call.class, fallbackExprSerializer);
        kryo.register(E_Cast.class, fallbackExprSerializer);
        kryo.register(E_Coalesce.class, new ExprFunctionNSerializer<>(args -> new E_Coalesce(new ExprList(args))));
        kryo.register(E_Conditional.class, new ExprFunction3Serializer<>(E_Conditional::new));
        kryo.register(E_Datatype.class, new ExprFunction1Serializer<>(E_Datatype::new));
        kryo.register(E_DateTimeDay.class, new ExprFunction1Serializer<>(E_DateTimeDay::new));
        kryo.register(E_DateTimeHours.class, new ExprFunction1Serializer<>(E_DateTimeHours::new));
        kryo.register(E_DateTimeMinutes.class, new ExprFunction1Serializer<>(E_DateTimeMinutes::new));
        kryo.register(E_DateTimeMonth.class, new ExprFunction1Serializer<>(E_DateTimeMonth::new));
        kryo.register(E_DateTimeSeconds.class, new ExprFunction1Serializer<>(E_DateTimeSeconds::new));
        kryo.register(E_DateTimeTimezone.class, new ExprFunction1Serializer<>(E_DateTimeTimezone::new));
        kryo.register(E_DateTimeTZ.class, new ExprFunction1Serializer<>(E_DateTimeTZ::new));
        kryo.register(E_DateTimeYear.class, new ExprFunction1Serializer<>(E_DateTimeYear::new));
        kryo.register(E_Divide.class, new ExprFunction2Serializer<>(E_Divide::new));
        kryo.register(E_Equals.class, new ExprFunction2Serializer<>(E_Equals::new));
        kryo.register(E_Exists.class, fallbackExprSerializer);
        kryo.register(E_Function.class, new E_FunctionSerializer<>(E_Function::new));
        // E_FunctionDynamic subclass of E_Call
        kryo.register(E_FunctionDynamic.class, fallbackExprSerializer);
        kryo.register(E_GreaterThan.class, new ExprFunction2Serializer<>(E_GreaterThan::new));
        kryo.register(E_GreaterThanOrEqual.class, new ExprFunction2Serializer<>(E_GreaterThanOrEqual::new));
        kryo.register(E_IRI.class, new E_IRISerializer<>(E_IRI::new));
        kryo.register(E_IRI2.class, new E_IRI2Serializer<>(E_IRI2::new));
        kryo.register(E_IsBlank.class, new ExprFunction1Serializer<>(E_IsBlank::new));
        kryo.register(E_IsIRI.class, new ExprFunction1Serializer<>(E_IsIRI::new));
        kryo.register(E_IsLiteral.class, new ExprFunction1Serializer<>(E_IsLiteral::new));
        kryo.register(E_IsNumeric.class, new ExprFunction1Serializer<>(E_IsNumeric::new));
        kryo.register(E_IsTriple.class, new ExprFunction1Serializer<>(E_IsTriple::new));
        kryo.register(E_IsURI.class, new ExprFunction1Serializer<>(E_IsURI::new));
        kryo.register(E_Lang.class, new ExprFunction1Serializer<>(E_Lang::new));
        kryo.register(E_LangMatches.class, new ExprFunction2Serializer<>(E_LangMatches::new));
        kryo.register(E_LessThan.class, new ExprFunction2Serializer<>(E_LessThan::new));
        kryo.register(E_LessThanOrEqual.class, new ExprFunction2Serializer<>(E_LessThanOrEqual::new));
        kryo.register(E_LogicalAnd.class, new ExprFunction2Serializer<>(E_LogicalAnd::new));
        kryo.register(E_LogicalNot.class, new ExprFunction1Serializer<>(E_LogicalNot::new));
        kryo.register(E_LogicalOr.class, new ExprFunction2Serializer<>(E_LogicalOr::new));
        kryo.register(E_MD5.class, new ExprFunction1Serializer<>(E_MD5::new));
        kryo.register(E_Multiply.class, new ExprFunction2Serializer<>(E_Multiply::new));
        kryo.register(E_NotEquals.class, new ExprFunction2Serializer<>(E_NotEquals::new));
        kryo.register(E_NotExists.class, fallbackExprSerializer);
        kryo.register(E_NotOneOf.class, new E_OneOfBaseSerializer<>(E_NotOneOf::new));
        kryo.register(E_Now.class);
        kryo.register(E_NumAbs.class, new ExprFunction1Serializer<>(E_NumAbs::new));
        kryo.register(E_NumCeiling.class, new ExprFunction1Serializer<>(E_NumCeiling::new));
        kryo.register(E_NumFloor.class, new ExprFunction1Serializer<>(E_NumFloor::new));
        kryo.register(E_NumRound.class, new ExprFunction1Serializer<>(E_NumRound::new));
        kryo.register(E_OneOf.class, new E_OneOfBaseSerializer<>(E_OneOf::new));
        kryo.register(E_OpNumericIntegerDivide.class, new ExprFunction2Serializer<>(E_OpNumericIntegerDivide::new));
        kryo.register(E_OpNumericMod.class, new ExprFunction2Serializer<>(E_OpNumericMod::new));
        kryo.register(E_Random.class);
        kryo.register(E_Regex.class, new ExprFunctionNSerializer<>(args -> new E_Regex(
                getOrNull(args, 0), getOrNull(args, 1), getOrNull(args, 2))));
        kryo.register(E_SameTerm.class, new ExprFunction2Serializer<>(E_SameTerm::new));
        kryo.register(E_SHA1.class, new ExprFunction1Serializer<>(E_SHA1::new));
        kryo.register(E_SHA224.class, new ExprFunction1Serializer<>(E_SHA224::new));
        kryo.register(E_SHA256.class, new ExprFunction1Serializer<>(E_SHA256::new));
        kryo.register(E_SHA384.class, new ExprFunction1Serializer<>(E_SHA384::new));
        kryo.register(E_SHA512.class, new ExprFunction1Serializer<>(E_SHA512::new));
        kryo.register(E_Str.class, new ExprFunction1Serializer<>(E_Str::new));
        kryo.register(E_StrAfter.class, new ExprFunction2Serializer<>(E_StrAfter::new));
        kryo.register(E_StrBefore.class, new ExprFunction2Serializer<>(E_StrBefore::new));
        kryo.register(E_StrConcat.class, new ExprFunctionNSerializer<>(args -> new E_StrConcat(new ExprList(args))));
        kryo.register(E_StrContains.class, new ExprFunction2Serializer<>(E_StrContains::new));
        kryo.register(E_StrDatatype.class, new ExprFunction2Serializer<>(E_StrDatatype::new));
        kryo.register(E_StrEncodeForURI.class, new ExprFunction1Serializer<>(E_StrEncodeForURI::new));
        kryo.register(E_StrEndsWith.class, new ExprFunction2Serializer<>(E_StrEndsWith::new));
        kryo.register(E_StrLang.class, new ExprFunction2Serializer<>(E_StrLang::new));
        kryo.register(E_StrLength.class, new ExprFunction1Serializer<>(E_StrLength::new));
        kryo.register(E_StrLowerCase.class, new ExprFunction1Serializer<>(E_StrLowerCase::new));
        kryo.register(E_StrReplace.class, new ExprFunctionNSerializer<>(args -> new E_StrReplace(
                getOrNull(args, 0), getOrNull(args, 1), getOrNull(args, 2), getOrNull(args, 3))));
        kryo.register(E_StrStartsWith.class, new ExprFunction2Serializer<>(E_StrStartsWith::new));
        kryo.register(E_StrSubstring.class, new ExprFunctionNSerializer<>(args -> new E_StrSubstring(
                getOrNull(args, 0), getOrNull(args, 1), getOrNull(args, 2))));
        kryo.register(E_StrUpperCase.class, new ExprFunction1Serializer<>(E_StrUpperCase::new));
        kryo.register(E_StrUUID.class);
        kryo.register(E_Subtract.class, new ExprFunction2Serializer<>(E_Subtract::new));
        kryo.register(E_TripleFn.class, new ExprFunction3Serializer<>(E_TripleFn::new));
        kryo.register(E_TripleObject.class, new ExprFunction1Serializer<>(E_TripleObject::new));
        kryo.register(E_TriplePredicate.class, new ExprFunction1Serializer<>(E_TriplePredicate::new));
        kryo.register(E_TripleSubject.class, new ExprFunction1Serializer<>(E_TripleSubject::new));
        kryo.register(E_UnaryMinus.class, new ExprFunction1Serializer<>(E_UnaryMinus::new));
        kryo.register(E_UnaryPlus.class, new ExprFunction1Serializer<>(E_UnaryPlus::new));
        kryo.register(E_URI.class, new E_IRISerializer<>(E_URI::new));
        kryo.register(E_URI2.class, new E_IRI2Serializer<>(E_URI2::new));
        kryo.register(E_UUID.class);
        kryo.register(E_Version.class);


        kryo.register(ExprList.class, new ExprListSerializer());
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
        kryo.register(Triple[].class);

        kryo.register(Quad.class, new QuadSerializer());
        kryo.register(Quad[].class);

        kryo.register(PrefixMappingImpl.class, new PrefixMappingSerializer(Lang.TURTLE, RDFFormat.TURTLE_PRETTY));

        kryo.register(ModelCom.class, GenericCollectionSerializer.create(ModelCom.class, Triple.class,
                m -> m.getGraph().stream(), () -> (ModelCom)ModelFactory.createDefaultModel(), (m, t) -> m.getGraph().add(t)));

        kryo.register(GraphPlain.class, GenericCollectionSerializer.create(GraphPlain.class, Triple.class,
                Graph::stream, () -> (GraphPlain)GraphFactory.createPlainGraph(), Graph::add));

        kryo.register(GraphMem.class, GenericCollectionSerializer.create(GraphMem.class, Triple.class,
                Graph::stream, () -> (GraphMem)GraphFactory.createGraphMem(), Graph::add));

        // kryo.register(ModelCom.class, new ModelSerializerViaRiot(Lang.RDFTHRIFT, RDFFormat.RDF_THRIFT_VALUES));
        // kryo.register(DatasetImpl.class, new DatasetSerializer(Lang.RDFTHRIFT, RDFFormat.RDF_THRIFT_VALUES));
        kryo.register(DatasetImpl.class, GenericCollectionSerializer.create(DatasetImpl.class, Quad.class,
                ds -> ds.asDatasetGraph().stream(), () -> (DatasetImpl)DatasetFactory.create(), (ds, q) -> ds.asDatasetGraph().add(q)));

        Serializer<Binding> bindingSerializer = new BindingSerializer();
        kryo.register(BindingRoot.class, bindingSerializer);
        kryo.register(Binding0.class, bindingSerializer);
        kryo.register(Binding1.class, bindingSerializer);
        kryo.register(Binding2.class, bindingSerializer);
        kryo.register(Binding3.class, bindingSerializer);
        kryo.register(Binding4.class, bindingSerializer);
        kryo.register(BindingOverMap.class, bindingSerializer);
        kryo.register(BindingProject.class, bindingSerializer);
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

        //kryo.register(org.apache.jena.rdf.model.RDFNode.class, new RDFNodeSerializer<>(Function.identity(), gson));
        //kryo.register(org.apache.jena.rdf.model.Resource.class, new RDFNodeSerializer<>(RDFNode::asResource, gson));
        //kryo.register(org.apache.jena.rdf.model.impl.R.class, new RDFNodeSerializer<>(RDFNode::asResource, gson));
        kryo.register(ResourceImpl.class, new RDFNodeSerializer<>(RDFNode::asResource));
        kryo.register(PropertyImpl.class, new RDFNodeSerializer<>(n -> ResourceFactory.createProperty(n.asResource().getURI())));
        kryo.register(LiteralImpl.class, new RDFNodeSerializer<>(RDFNode::asLiteral));


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

    private static <T> T getOrNull(List<T> list, int i) {
        return getOrDefault(list, i, null);
    }

    private static <T> T getOrDefault(List<T> list, int i, T dflt) {
        T result = i >= list.size() ? dflt : list.get(i);
        return result;
    }
}
