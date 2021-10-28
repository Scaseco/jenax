package org.aksw.jena_sparql_api.core;

import java.util.List;
import java.util.Map;

import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.binding.QuerySolutionUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class ResultSetRename
    extends ResultSetDecorator
{
    private Map<Var, Var> varMap;
    private List<String> resultVarNames;

    public ResultSetRename(ResultSet decoratee, Map<Var, Var> varMap) {
        this(decoratee, varMap, VarUtils.map(decoratee.getResultVars(), varMap));
    }

    public ResultSetRename(ResultSet decoratee, Map<Var, Var> varMap, List<String> resultVarNames) {
        super(decoratee);
        this.varMap = varMap;
        this.resultVarNames = resultVarNames;
    }

    @Override
    public QuerySolution next() {
        QuerySolution qs = super.next();
        QuerySolution result = QuerySolutionUtils.renameKeys(qs, varMap);
        return result;
    }

    @Override
    public QuerySolution nextSolution() {
        QuerySolution qs = super.nextSolution();
        QuerySolution result = QuerySolutionUtils.renameKeys(qs, varMap);
        return result;
    }

    @Override
    public Binding nextBinding() {
        Binding binding = super.nextBinding();
        Binding result = BindingUtils.renameKeys(binding, varMap);
        return result;
    }

    @Override
    public List<String> getResultVars() {
        return resultVarNames;
    }



    public static ResultSet wrapIfNeeded(ResultSet decoratee, Map<Var, Var> varMap) {
        ResultSet result;
        if(varMap == null) {
            result = decoratee;
        } else {
            result = new ResultSetRename(decoratee, varMap);
        }
        return result;
    }
}