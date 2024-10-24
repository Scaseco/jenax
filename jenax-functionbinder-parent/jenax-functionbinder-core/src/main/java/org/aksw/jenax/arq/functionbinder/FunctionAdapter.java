package org.aksw.jenax.arq.functionbinder;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aksw.commons.util.convert.ConverterRegistries;
import org.aksw.commons.util.convert.ConverterRegistry;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.sse.builders.SSE_ExprBuildException;
import org.apache.jena.sparql.util.Context;

/**
 * Jena function implementation that delegates to a backing Java method.
 *
 * Instances of this class are usually generated using the {@link FunctionGenerator#wrap(Method)}.
 * Use the {@link FunctionBinder} for creating function bindings and registration of them with Jena.
 */
public class FunctionAdapter
    implements Function
{
    // TODO Add support for deriving the invocation target from the first argument
    protected Object invocationTarget;
    protected Method method;

    protected java.util.function.Function<Object, NodeValue> returnValueConverter;
    protected ConverterRegistry converterRegistry;
    protected TypeMapper typeMapper;

    protected Param[] params;
    protected int mandatoryArgsCount;

    public FunctionAdapter(
            Method method,
            Object invocationTarget,
            java.util.function.Function<Object, NodeValue> returnValueConverter,
            Param[] params,
            TypeMapper typeMapper,
            ConverterRegistry converterRegistry) {
        super();
        this.method = method;
        this.invocationTarget = invocationTarget;
        this.returnValueConverter = returnValueConverter;
        this.params = params;
        this.converterRegistry = converterRegistry;
        this.typeMapper = typeMapper;
    }

    @Override
    public void build(String uri, ExprList args, Context context) {

    }

//	protected Object exprToJava() {
//		register(GeometryWrapper.class, Geometry.class, GeometryWrapper::getParsingGeometry, g -> new GeometryWrapper(WKTDatatype.URI, g));
//	}

    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        Object[] javaArgs = buildJavaArgs(binding, args, env);
        NodeValue result = invokeWithJavaArgs(javaArgs);
        return result;
//
//		Object targetReturnVal = resultValueConverter == null
//				? val
//				: resultValueConverter.getFunction().apply(val);
//
//		Node node = NodeFactory.createLiteralByValue(val, resultRdfDatatype);
//		NodeValue nv = NodeValue.makeNode(node);
//
//		Class<?> returnType = method.getReturnType();
//
//		RDFDatatype dtype = typeMapper.getTypeByValue(val);
//		if (dtype == null) {
//			Class<?> targetReturnType = returnTypeConversionMap.get(returnType);
//
//			if (targetReturnType == null) {
//				throw new RuntimeException(String.format("Could not find a mapping from %s to an RDF datatype", returnType));
//			}
//
//			dtype = typeMapper.getTypeByClass(targetReturnType);
//
//
//			Converter converter = converterRegistry.getConverter(returnType, targetReturnType)
//			if (converter == null) {
//				throw new RuntimeException(String.format("No converter found from %s to %s", returnType, targetReturnType));
//			}
//
//			Object obj = converter.getFunction().apply(val);
//			Node node = NodeFactory.createLiteralByValue(obj, dtype);
//			NodeValue nv = NodeValue.makeNode(node);
//
//		}
//
//
//
//		// TODO Auto-generated method stub
//		return null;
    }

    /**
     * Invoke the wrapped method with prior built java arguments and
     * return the result as a NodeValue.
     */
    public NodeValue invokeWithJavaArgs(Object[] javaArgs) {
        Object val;
        try {
            val = method.invoke(invocationTarget, javaArgs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ExprEvalException(e);
        }
        NodeValue result = returnValueConverter.apply(val);
        if (result == null) {
            throw new ExprEvalException(String.format("Null returned from %s for arguments %s", method, javaArgs));
        }
        return result;
    }

    /** Given a list of argument expressions and a binding, derive the list of corresponding Java objects to be
     *  used as the actual function arguments. */
    public Object[] buildJavaArgs(Binding binding, ExprList args, FunctionEnv env) {
        int argCount = args.size();
        int paramCount = params.length;
        Object[] javaArgs = new Object[paramCount];
        boolean isVarArgs = method.isVarArgs();
        int varArgOffset = paramCount - 1;

        if (argCount < mandatoryArgsCount || (argCount > paramCount && !isVarArgs)) {
            throw new SSE_ExprBuildException(
                    String.format("at least %d and at most %d args expected but %d provided",
                                mandatoryArgsCount, params.length, argCount));
        }

        Param lastParam = paramCount > 0 ? params[varArgOffset] : null;
        Object javaVarArgsArr = null;
        int varArgCount = 0;
        if (isVarArgs) {
            varArgCount = Math.max(0, argCount - paramCount + 1);
            javaVarArgsArr = Array.newInstance(lastParam.getParamClass().getComponentType(), varArgCount);
            javaArgs[paramCount - 1] = javaVarArgsArr;
        }

        for (int i = 0; i < Math.max(argCount, paramCount); ++i) {
            Object javaArg;

            Param param = i < paramCount ? params[i] : lastParam;
            Class<?> inputClass = param.getInputClass();
            Class<?> paramType = param.getParamClass();
            boolean isInVarArgs = isVarArgs && i >= varArgOffset;
            if (isInVarArgs) {
                paramType = paramType.getComponentType();
            }

            if (i < argCount) {
                Expr expr = args.get(i);

                // Attempt to convert the NodeValue's java object if its type does not match the
                // expected parameter type

    //				if (Expr.NONE.equals(expr)) {
    //					javaArg = null;
    //				} else {
                NodeValue arg = expr.eval(binding, env);

                NodeValue nv = arg.getConstant();
                Node node = nv.asNode();
                Object intermediateObj;
                if (inputClass != null && Node.class.isAssignableFrom(inputClass)) {
                    intermediateObj = node;
                } else {
                    intermediateObj = node.getLiteralValue();
                }

    //				try {
                    javaArg = ConverterRegistries.convert(converterRegistry, intermediateObj, paramType);
    //				} catch (Exception e) {
    //					throw new ExprEvalException(e);
    //				}
    //				else {
    //					throw new IllegalArgumentException("Constant expression expected, got: " + expr);
    //				}
            } else {
                javaArg = param.defaultValue;
            }

            if (isInVarArgs) {
                // Handle the case where there is a vararg parameter but no argument is given
                if (i < argCount) {
                    Array.set(javaVarArgsArr, i - varArgOffset, javaArg);
                }
            } else {
                javaArgs[i] = javaArg;
            }
        }
        return javaArgs;
    }

    public Object getInvocationTarget() {
        return invocationTarget;
    }

    public Method getMethod() {
        return method;
    }
}
