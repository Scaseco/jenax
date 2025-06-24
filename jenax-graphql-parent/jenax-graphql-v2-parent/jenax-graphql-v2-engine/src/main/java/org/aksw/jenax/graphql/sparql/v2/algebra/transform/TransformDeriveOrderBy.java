package org.aksw.jenax.graphql.sparql.v2.algebra.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.join.JoinKey;

public class TransformDeriveOrderBy
    extends TransformCopy
{
    @Override
    public Op transform(OpUnion opUnion, Op left, Op right) {
        // TODO Auto-generated method stub
        return super.transform(opUnion, left, right);
    }

    private static List<Op> flattenUnion(Op op) {
        List<Op> result = new ArrayList<>();
        flattenUnion(op, result);
        return result;
    }

    private static void flattenUnion(Op op, List<Op> outOps) {
        if (op instanceof OpUnion u) {
            flattenUnion(u.getLeft(), outOps);
            flattenUnion(u.getRight(), outOps);
        } else if (op instanceof OpDisjunction d) {
            d.getElements().forEach(x -> flattenUnion(x, outOps));
        } else {
            outOps.add(op);
        }
    }

    @Override
    public Op transform(OpLateral opLateral, Op left, Op right) {

        Set<Var> visibleVarsLhs = OpVars.visibleVars(left);

        List<Op> rhs = flattenUnion(right);
        System.err.println("Join keys for " + left);
        for (Op rhsOp : rhs) {
            // Set<Var> visibleVarsRhs = OpVars.visibleVars(rhsOp);
            Set<Var> mentionedVarsRhs = new HashSet<>(OpVars.mentionedVars(rhsOp));
            JoinKey jk = JoinKey.create(visibleVarsLhs, mentionedVarsRhs);
            System.out.println("  with " + rhsOp + ": " + jk);
        }


//        System.out.println("Join key for " + left + ": " + jk);
//        System.out.println(rhs);

        // TODO Auto-generated method stub
        return super.transform(opLateral, left, right);
    }



    public static void main(String[] args) {

        String queryStr = """
SELECT  *
WHERE
  {   { BIND("state_0" AS ?state)}
    UNION
      { { SELECT  *
          WHERE
            { { SELECT  ?field1_s
                WHERE
                  { ?field1_s  <http://www.wikidata.org/prop/direct/P31>  <http://www.wikidata.org/entity/Q11424>
                    FILTER EXISTS { ?field1_s  <http://www.w3.org/2000/01/rdf-schema#label>  ?l
                                    FILTER langMatches(lang(?l), "en")
                                    FILTER contains(lcase(str(?l)), lcase("die hard"))
                                  }
                  } ORDER BY ?field1_s
              }
            }
          LIMIT   2
        }
        LATERAL
          {   { BIND("state_1" AS ?state)
                BIND(?field1_s AS ?v_0)
              }
            UNION
              { BIND("state_2" AS ?state)
                BIND(?field1_s AS ?field1_field1_bindvar_1)
                BIND(?field1_field1_bindvar_1 AS ?v_0)
              }
            UNION
              { BIND("state_3" AS ?state)
                ?field1_s  <http://www.w3.org/2000/01/rdf-schema#label>  ?field1_field2_l
                FILTER ( lang(?field1_field2_l) = "en" )
                BIND(?field1_s AS ?v_0)
                BIND(?field1_field2_l AS ?v_1)
              }
            UNION
              { BIND("state_4" AS ?state)
                ?field1_s  <http://schema.org/description>  ?field1_field3_l
                FILTER ( lang(?field1_field3_l) = "en" )
                BIND(?field1_s AS ?v_0)
                BIND(?field1_field3_l AS ?v_1)
              }
            UNION
              { BIND("state_5" AS ?state)
                { SELECT  ?field1_s ?field1_field4_o
                  WHERE
                    { ?field1_s  <http://www.wikidata.org/prop/direct/P18>  ?field1_field4_o }
                  ORDER BY ?field1_field4_o
                  LIMIT   1
                }
                BIND(?field1_s AS ?v_0)
                BIND(?field1_field4_o AS ?v_1)
              }
            UNION
              { BIND("state_6" AS ?state)
                { SELECT  ?field1_s (<http://www.w3.org/2001/XMLSchema#gYear>(MAX(?o)) AS ?field1_field5_date)
                  WHERE
                    { ?field1_s  <http://www.wikidata.org/prop/direct/P577>  ?o }
                  GROUP BY ?field1_s
                }
                BIND(?field1_s AS ?v_0)
                BIND(?field1_field5_date AS ?v_1)
              }
            UNION
              { BIND("state_7" AS ?state)
                { SELECT  ?field1_s ?field1_field6_id
                  WHERE
                    { ?field1_s  <http://www.wikidata.org/prop/direct/P1874>  ?o
                      BIND(IRI(concat("https://www.netflix.com/title/", str(?o))) AS ?field1_field6_id)
                    }
                }
                BIND(?field1_s AS ?v_0)
                BIND(?field1_field6_id AS ?v_1)
              }
            UNION
              { BIND("state_8" AS ?state)
                { SELECT  ?field1_s (MIN(?o) AS ?field1_field7_age)
                  WHERE
                    { ?field1_s !<file:///p>/<http://www.wikidata.org/prop/direct/P2899> ?o }
                  GROUP BY ?field1_s
                }
                BIND(?field1_s AS ?v_0)
                BIND(?field1_field7_age AS ?v_1)
              }
            UNION
              { BIND("state_9" AS ?state)
                { SELECT DISTINCT  ?field1_s (str(?l) AS ?field1_field8_x)
                  WHERE
                    { ?field1_s <http://www.wikidata.org/prop/direct/P136>/<http://www.w3.org/2000/01/rdf-schema#label> ?l
                      FILTER langMatches(lang(?l), "en")
                    }
                }
                BIND(?field1_s AS ?v_0)
                BIND(?field1_field8_x AS ?v_1)
              }}
      }}

        """;

        Query beforeQuery = QueryFactory.create(queryStr);
        Op beforeOp = Algebra.compile(beforeQuery);
        Op afterOp = Transformer.transform(new TransformDeriveOrderBy(), beforeOp);
        Query afterQuery = OpAsQuery.asQuery(afterOp);

        // System.out.println(afterQuery);
    }
}
