package org.aksw.jenax.dataaccess.sparql.link.update;

import org.aksw.jenax.arq.util.exec.update.UpdateExecTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderWrapperBaseParse;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;

public class LinkSparqlUpdateUtils {
    /**
     * Create a {@link LinkSparqlUpdateTransform} that can wrap {@link LinkSparqlUpdate}
     * links such that any {@link UpdateExec} they produce are passed through the
     * given {@link UpdateExecTransform}.
     */
    public static LinkSparqlUpdateTransform newTransform(UpdateExecTransform updateExecTransform) {
        LinkSparqlUpdateTransform result = baseLink -> {
            return new LinkSparqlUpdateDelegateBase(baseLink) {
                @Override
                public UpdateExecBuilder newUpdate() {
                    return new UpdateExecBuilderWrapperBaseParse(getDelegate().newUpdate()) {
                        @Override
                        public UpdateExec build() {
                            UpdateExec before = super.build();
                            UpdateExec r = updateExecTransform.apply(before);
                            return r;
                        }
                    };
                }
            };
        };
        return result;
    }
}
