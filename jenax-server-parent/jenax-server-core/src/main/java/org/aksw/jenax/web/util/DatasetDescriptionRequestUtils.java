package org.aksw.jenax.web.util;

import java.util.Arrays;

import org.aksw.jenax.arq.util.dataset.DatasetDescriptionUtils;
import org.apache.jena.sparql.core.DatasetDescription;
import org.springframework.web.bind.ServletRequestUtils;

import jakarta.servlet.http.HttpServletRequest;

public class DatasetDescriptionRequestUtils {
    public static DatasetDescription extractDatasetDescriptionAny(HttpServletRequest req) {
        DatasetDescription result = extractDatasetDescriptionQuery(req);
        DatasetDescription b = extractDatasetDescriptionUpdate(req);
        DatasetDescriptionUtils.mergeInto(result, b);

        return result;
    }

    public static DatasetDescription extractDatasetDescriptionQuery(HttpServletRequest req) {
        DatasetDescription result = extractDatasetDescriptionCommon(req, "default-graph-uri", "named-graph-uri");
        return result;
    }

    public static DatasetDescription extractDatasetDescriptionUpdate(HttpServletRequest req) {
        DatasetDescription result = extractDatasetDescriptionCommon(req, "using-graph-uri", "using-named-graph-uri");
        return result;
    }

    public static DatasetDescription extractDatasetDescriptionCommon(HttpServletRequest req, String dguParamName, String nguParamName) {
        DatasetDescription result = new DatasetDescription();
        result.addAllDefaultGraphURIs(Arrays.asList(ServletRequestUtils.getStringParameters(req, dguParamName)));
        result.addAllNamedGraphURIs(Arrays.asList(ServletRequestUtils.getStringParameters(req, nguParamName)));
        return result;
    }
}
