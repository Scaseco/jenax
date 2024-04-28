package org.aksw.jenax.arq.util.op;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

public class RewriteList
    implements Rewrite
{
    protected List<Rewrite> rewrites;

    public RewriteList(List<Rewrite> rewrites) {
        super();
        this.rewrites = rewrites;
    }

    public List<Rewrite> getRewrites() {
        return rewrites;
    }

    @Override
    public Op rewrite(Op op) {
        Op result = op;
        for (Rewrite rewrite : rewrites) {
            Op tmp = rewrite.rewrite(result);
            result = tmp;
        }
        return result;
    }


    public static Stream<Rewrite> streamFlatten(boolean recursive, Rewrite rewrite) {
        Stream<Rewrite> result;
        if (rewrite instanceof RewriteList) {
            result = ((RewriteList)rewrite).getRewrites().stream();
            if (recursive) {
                result = result.flatMap(x -> streamFlatten(recursive, rewrite));
            }
        } else {
            result = Stream.of(rewrite);
        }
        return result;
    }

    /**
     *
     * @param recursive true to flatten recursively, false to only flatten the first level
     * @param rewrites
     * @return
     */
    public static Rewrite flatten(boolean recursive, Rewrite... rewrites) {
        List<Rewrite> list = Stream.of(rewrites)
            .flatMap(item -> streamFlatten(recursive, item))
            .collect(Collectors.toList());

        Rewrite result = list.size() == 1
            ? list.get(0)
            : new RewriteList(list);

        return result;
    }
}
