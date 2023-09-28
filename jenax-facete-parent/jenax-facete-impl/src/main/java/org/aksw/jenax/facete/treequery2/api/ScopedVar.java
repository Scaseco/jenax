package org.aksw.jenax.facete.treequery2.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class ScopedVar {
    protected String scopeName;
    protected String pathName;
    protected Var baseVar;

    public ScopedVar(String scopeName, String pathName, Var baseVar) {
        super();
        this.scopeName = scopeName;
        this.pathName = pathName;
        this.baseVar = baseVar;
    }

    public String getScopeName() {
        return scopeName;
    }

    public String getPathName() {
        return pathName;
    }

    public Var getBaseVar() {
        return baseVar;
    }

    public Var asVar() {
        return ScopedVar.scopeVar(baseVar, scopeName, pathName);
    }

    @Override
    public String toString() {
        return "ScopedVar [scopeName=" + scopeName + ", pathName=" + pathName + ", baseVar=" + baseVar + "]";
    }

    public static ScopedVar of(String scopeName, String pathName, Var baseVar) {
        return new ScopedVar(scopeName, pathName, baseVar);
    }

    public static final Pattern SCOPED_VAR_PATTERN = Pattern.compile("^_(([^_]|__)*)_(([^_]|__)*)_(.*)_$");


    public static Var scopeVar(Var var, String scopeName, String pathName) {
        Var result;
        if (scopeName.isEmpty() && pathName.isEmpty()) {
            result = var;
        } else {
            String originalName = var.getName();
            String scopedName = "_" + scopeName.replace("_", "__") + "_" + pathName.replace("_", "__") + "_" + originalName + "_";
            result = Var.alloc(scopedName);
        }
        return result;
    }

    public static ScopedVar unscopeVar(Var var) {
        ScopedVar result;

        String originalName = var.getName();

        // Var name must match pattern _BASENAME_SCOPE_VARNAME_
        // Where base name is optional
        // underscores are not allowed in basename and scope
        Matcher matcher = SCOPED_VAR_PATTERN.matcher(originalName);
        if (matcher.matches()) {
            result = new ScopedVar(matcher.group(1).replace("__", "_"), matcher.group(3).replace("__", "_"), Var.alloc(matcher.group(5)));

        } else {
            throw new IllegalArgumentException("Scoped variables must start and end with an underscore (_).");
        }
        return result;
    }

    public static void main(String[] args) {
        FacetPathMappingImpl fpm = new FacetPathMappingImpl();

        Var var = Var.alloc("foo_bar");
        Var scopedVar = scopeVar(var, "s_cop__e", "pa_th");
        System.out.println(scopedVar);

        ScopedVar sc = unscopeVar(scopedVar);
        Var unscopedVar = sc.getBaseVar();
        System.out.println(unscopedVar);
        System.out.println(sc.getScopeName());
        System.out.println(sc.getPathName());

        FacetPath fp = FacetPath.newAbsolutePath().resolve(FacetStep.fwd(RDFS.label)).resolve(FacetStep.fwd(RDF.type));
        fp = FacetPath.newAbsolutePath();
        ScopedVar sc2 = FacetPathMappingImpl.resolveVar(fpm, "base", Vars.s, fp);
        System.out.println(sc2.asVar());
    }

}
