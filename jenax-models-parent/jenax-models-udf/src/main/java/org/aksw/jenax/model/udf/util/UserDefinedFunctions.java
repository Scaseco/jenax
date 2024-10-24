package org.aksw.jenax.model.udf.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.function.FixpointIteration;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.model.shacl.util.ShPrefixUtils;
import org.aksw.jenax.model.udf.api.UdfDefinition;
import org.aksw.jenax.model.udf.api.UdfVocab;
import org.aksw.jenax.model.udf.api.UserDefinedFunctionResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.user.ExprTransformExpand;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.function.user.UserDefinedFunctionFactory;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class UserDefinedFunctions {
    private static final Logger logger = LoggerFactory.getLogger(UserDefinedFunctions.class);

    public static ExtendedIterator<UserDefinedFunctionResource> listUserDefinedFunctions(Model model) {
        return model.listSubjectsWithProperty(RDF.type, UdfVocab.UserDefinedFunction)
                .mapWith(x -> x.as(UserDefinedFunctionResource.class));
    }

    public static void registerAll(Map<String, UserDefinedFunctionDefinition> map) {
        UserDefinedFunctionFactory f = UserDefinedFunctionFactory.getFactory();

        for(UserDefinedFunctionDefinition udfd : map.values()) {
            f.add(udfd.getUri(), udfd.getBaseExpr(), udfd.getArgList());
        }

    }

    public static Map<String, UserDefinedFunctionDefinition> load(Model model, Set<String> activeProfiles) {
        Map<String, UserDefinedFunctionDefinition> result = new LinkedHashMap<>();

        List<UserDefinedFunctionResource> fns = listUserDefinedFunctions(model).toList();
        for(UserDefinedFunctionResource fn : fns) {
            resolveUdf(result, fn, activeProfiles);
        }

        return result;
    }

    public static String forceIri(Resource r) {
        String result = r.isURIResource()
                ? r.getURI()
                : "_:" + r.getId().getLabelString();

        Objects.requireNonNull(result, "Could not craft IRI from " + r);

        return result;
    }

    public static void resolveUdf(
            Map<String, UserDefinedFunctionDefinition> result,
            //UserDefinedFunctionFactory f,
            UserDefinedFunctionResource fn,
            Set<String> activeProfiles)
    {
        String fnIri = forceIri(fn);

        UserDefinedFunctionDefinition fnUdfd = result.get(fnIri);

        if(fnUdfd == null) {

            // First check which of the udf definitions are active under the given profiles
            // If there are multiple ones, raise an exception with the conflicts
            List<UdfDefinition> activeUdfs = new ArrayList<>();
            for(UdfDefinition def : fn.getDefinitions()) {
                Set<Resource> requiredProfiles = def.getProfiles();
                Set<String> requiredProfileIris = requiredProfiles.stream()
                        .filter(RDFNode::isURIResource)
                        .map(Resource::getURI)
                        .collect(Collectors.toSet());;

                Set<String> overlap = Sets.intersection(requiredProfileIris, activeProfiles);
                if(requiredProfiles.isEmpty() || !overlap.isEmpty()) {
                    activeUdfs.add(def);
                }
            }

            if(activeUdfs.isEmpty()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("User defined function " + fnIri + " has no candidate for profiles " + activeProfiles);
                }
            } else if(activeUdfs.size() > 1) {
                throw new RuntimeException("Expected exactly 1 definition for " + fnIri + "; got: " + activeUdfs);
            } else {

                UdfDefinition activeUdf = Iterables.getFirst(activeUdfs, null);

                // Resolve alias references
                Resource ra = activeUdf.getAliasFor();

                if (Boolean.TRUE.equals(activeUdf.mapsToPropertyFunction())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Mapped property function: " + activeUdf + ", aliasFor: " + ra);
                    }
                    UserDefinedFunctionDefinition ud = new UserDefinedFunctionDefinition(fnIri,
                            ExprUtils.parse("<" + fnIri + ">(?s)"),
                            Arrays.asList(Vars.s));

                    result.put(ud.getUri(), ud);
                } else if(ra != null) {
                    UserDefinedFunctionResource alias = ra.as(UserDefinedFunctionResource.class);
                    if(alias != null) {
                        resolveUdf(result, alias, activeProfiles);

                        String iri = forceIri(alias);
                        // Try to resolve the definition
                        // TODO Possibly try to resolve against Jena's function registry
                        UserDefinedFunctionDefinition udfd = result.get(iri);
                        if(udfd == null) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Could not resolve " + iri);
                            }
                            //throw new RuntimeException("Could not resolve " + iri);
                        } else {

                            //UserDefinedFunctionResource udf = alias.as(UserDefinedFunctionResource.class);
                            UserDefinedFunctionDefinition ud = new UserDefinedFunctionDefinition(fnIri, udfd.getBaseExpr(), udfd.getArgList());

                            result.put(ud.getUri(), ud);
                            //f.add(fnIri, udfd.getBaseExpr(), udfd.getArgList());
                        }
                    }
                } else {
                    UserDefinedFunctionDefinition udfd = toJena(fnIri, activeUdf);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Registering " + udfd);
                    }

                    result.put(udfd.getUri(), udfd);
                }
            }
        }
    }

    public static Expr expandMacro(Map<String, UserDefinedFunctionDefinition> macros, String udfUri, Expr ... args) {
        Expr e = new E_Function(udfUri, new ExprList(Arrays.asList(args)));
        Expr result = expandMacro(macros, e);
        return result;
    }

    public static Expr expandMacro(Map<String, UserDefinedFunctionDefinition> macros, Expr e) {
        ExprTransform xform = new ExprTransformExpand(macros);
        e = FixpointIteration.apply(100, e, x -> ExprTransformer.transform(xform, x));
        e = FixpointIteration.apply(100, e, ExprLib::foldConstants);
        //e = ExprLib.foldConstants(e);
        return e;
    }

    /** Evaluate a specific macro */
    public static NodeValue eval(Map<String, UserDefinedFunctionDefinition> macros, String udfUri, Expr ... args) {
        Expr expr = expandMacro(macros, udfUri, args);
        NodeValue result = ExprUtils.eval(expr);
        return result;
    }

    public static UserDefinedFunctionDefinition toJena(String iri, UdfDefinition r) {
        PrefixMapping pm = Prefixes.adapt(ShPrefixUtils.collect(r));

        if (logger.isDebugEnabled()) {
            logger.debug("Processing user defined function definition: " + iri + ": " + pm);
        }

        List<String> paramsStr = r.getParams();
        List<Var> params = paramsStr.stream()
                .map(Var::alloc)
                .collect(Collectors.toList());
        String exprStr = r.getExpr();
        if (exprStr == null) {
            throw new NullPointerException("No expression present on resource: " + iri);
        }

        Expr e = ExprUtils.parse(exprStr, pm);

        UserDefinedFunctionDefinition result = new UserDefinedFunctionDefinition(iri, e, params);
        return result;
    }
}
