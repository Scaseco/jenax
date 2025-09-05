/* global ldvDef, ldvStartpageMoreClassesInstQuery, ldvStartpageMoreClassesOntQuery */
(() => {
  const ldvQueries = {
    askQuery: (iri, reverseEnabled) => `ASK {` +
 [`{
    BIND(iri(replace(replace("${iri}", '\\\\(', '%28'), '\\\\)', '%29')) AS ?s)
    ?s ?p ?o
 }`, `{
    BIND(<${iri}> AS ?s)
    ?s ?p ?o
 }`, ... reverseEnabled === 'yes' ? [`{
    BIND(iri(replace(replace("${iri}", '\\\\(', '%28'), '\\\\)', '%29')) AS ?o)
    ?s ?p ?o
 }`, `{
    BIND(<${iri}> AS ?o)
    ?s ?p ?o
 }`] : [] ].join(` UNION `) + `
}
`,
    describeQuery: (iri, infer, reverseEnabled) => `CONSTRUCT {
  ?s ?p ?o .
} {
  ${ infer ? 'SERVICE <sameAs+rdfs:> {' : '' }
    BIND(<${iri}> AS ?x_)
    LATERAL {
        { BIND(?x_ AS ?x) }
      UNION
        { BIND(iri(replace(replace(str(?x_), '\\\\(', '%28'), '\\\\)', '%29')) AS ?x) }
    }
    LATERAL {` +
    [`{
        BIND(?x AS ?s_) .
        LATERAL {
            { # Forward, types
              { SELECT DISTINCT ?s_ ?p ?o_ { BIND(rdf:type AS ?p) ?s_ ?p ?o_ } }
            }
          UNION
            { # Forward, non-types
              { SELECT DISTINCT ?s_ ?p { ?s_ ?p ?o_ FILTER(?p != rdf:type) } LIMIT 1000 }
              LATERAL {
                  {
                    { SELECT * { ?s_ ?p ?o_ } LIMIT 10 }
                  }
                UNION
                  {
                    { SELECT ?s_ ?p (COUNT(*) AS ?oCnt) { SELECT * { ?s_ ?p ?o_ } LIMIT 11 } GROUP BY ?s_ ?p }
                    FILTER(?oCnt > 10)
                    BIND(strdt("...", <${ldvDef.moreResultsObjId}>) AS ?o_)
                  }
              }
            }
          UNION
           { # Forward, graphs that contain the resource as subject
             BIND(<${ldvDef.sourceGraphPropId}> AS ?p)
             { SELECT DISTINCT ?o_ ?s_ { GRAPH ?o_ { ?s_ a ?z } } }
           }
        }
        BIND(<http://ns.aksw.org/function/forceBnodeIri>(?s_) AS ?s)
        BIND(<http://ns.aksw.org/function/forceBnodeIri>(?o_) AS ?o)
      }`, ... reverseEnabled === 'yes' ?
    [`{
        BIND(?x AS ?s_)
        LATERAL {
          { # Reverse
            { SELECT DISTINCT ?s_ ?rp { SELECT ?s_ ?rp { ?o ?rp ?s_  } LIMIT 10000 } LIMIT 1000 }
            LATERAL {
                {
                  { SELECT * { ?o_ ?rp ?s_ } LIMIT 10 }
                }
              UNION
                {
                  { SELECT ?s_ ?rp (COUNT(*) AS ?oCnt) { SELECT * { ?o_ ?rp ?s_ } LIMIT 11 } GROUP BY ?s_ ?rp }
                  FILTER(?oCnt > 10)
                  BIND(strdt("...", <${ldvDef.moreResultsObjId}>) AS ?o_)
                }
            }
          }
        }
        BIND(URI(concat("urn:x-ldv:reverse:", str(?rp))) AS ?p)
        BIND(<http://ns.aksw.org/function/forceBnodeIri>(?s_) AS ?s)
        BIND(<http://ns.aksw.org/function/forceBnodeIri>(?o_) AS ?o)
      }`] : [] ].join(` UNION `) + `
  }
  ${ infer ? '}' : '' }
}
`,
    loadMoreQuery: (s, p, limit, offset, infer) => p === ldvDef.classesInstPropId ? ldvStartpageMoreClassesInstQuery(limit, offset)
      : p === ldvDef.classesOntPropId ? ldvStartpageMoreClassesOntQuery(limit, offset)
      : `CONSTRUCT {
  <${ s.startsWith('_:') ? 'bnode://' + s.slice(2) : s }> <${p}> ?o .
} {
  ${ infer ? 'SERVICE <sameAs+rdfs:> {' : '' }
  { SELECT ?o {
      <${s}> <${p}> ?o_ . bind(<http://ns.aksw.org/function/forceBnodeIri>(?o_) as ?o) .
    } LIMIT ${limit} OFFSET ${offset}
  } UNION {
    { SELECT (count(?ox) AS ?oCnt) {
        {
          SELECT ?ox {
            <${s}> <${p}> ?ox
          } LIMIT ${limit + 1} OFFSET ${offset}
        }
      }
    } bind(if(?oCnt>10,strdt('...',<${ldvDef.moreResultsObjId}>),coalesce()) AS ?o)
  }
  ${ infer ? '}' : '' }
}
`,
    loadMoreReverseQuery: (o, p, limit, offset, infer) => `CONSTRUCT {
  <${ o.startsWith('_:') ? 'bnode://' + o.slice(2) : o }> <${ldvDef.reversePropPrefix}:${p}> ?s .
} {
  ${ infer ? 'SERVICE <sameAs+rdfs:> {' : '' }
  { SELECT ?s {
      ?s_ <${p}> <${o}> . bind(<http://ns.aksw.org/function/forceBnodeIri>(?s_) as ?s)
    } LIMIT ${limit} OFFSET ${offset}
  } UNION {
    { SELECT (count(?sx) AS ?sCnt) {
        {
          SELECT ?sx {
            ?sx <${p}> <${o}>
          } LIMIT ${limit + 1} OFFSET ${offset}
        }
      }
    } bind(if(?sCnt>10,strdt('...',<${ldvDef.moreResultsObjId}>),coalesce()) AS ?s)
  }
  ${ infer ? '}' : '' }
}
`,
    fetchLabelsQuery: (uris, lang, infer) => `PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>

JSON {
    "uri": ?uri,
    "label": ?label,
    "lang": ?lang
  } WHERE {
    VALUES ?uri_ { ${uris} }
    bind(<http://ns.aksw.org/function/forceBnodeIri>(?uri_) as ?uri)
    LATERAL {
      ${ infer ? 'SERVICE <sameAs+rdfs:> {' : '' }
      SELECT ?uri ?uri_ ?label ?lang {
        VALUES ?langCand { "${lang}" ""}
        {
          ?uri_ rdfs:label|skos:prefLabel ?label .
          BIND(lang(?label) AS ?lang) .
        } UNION {
          # Wikidata normalized labels
          BIND(STRAFTER(STR(?uri_), "http://www.wikidata.org/prop/direct/") AS ?wdPid)
          FILTER(?wdPid != "")
          BIND(IRI(CONCAT(STR("http://www.wikidata.org/entity/"), ?wdPid)) AS ?claimForP)
          ?claimForP rdfs:label ?label .
          BIND(lang(?label) AS ?lang) .
        } UNION {
          # Wikidata non-normalized labels
          BIND(STRAFTER(STR(?uri_), "http://www.wikidata.org/prop/direct-normalized/") AS ?wdPid)
          FILTER(?wdPid != "")
          BIND(IRI(CONCAT(STR("http://www.wikidata.org/entity/"), ?wdPid)) AS ?claimForP)
          ?claimForP rdfs:label ?tmp .
          BIND(lang(?tmp) AS ?lang) .
          BIND(CONCAT(STR(?tmp), " (normalized)") AS ?label)
        }
        FILTER(?lang = ?langCand) .
      } LIMIT 1
      ${ infer ? '}' : '' }
    }
  }
`,
    geoQuery: (iri, infer) => `CONSTRUCT {
  <${iri}> <http://www.opengis.net/ont/geosparql#asWKT> ?wktLiteral
} WHERE {
  ${ infer ? 'SERVICE <sameAs+rdfs:> {' : '' }
  <${iri}> <http://www.opengis.net/ont/geosparql#asWKT> ?wktLiteral
  ${ infer ? '}' : '' }
}
`,
    graphLookupQuery: (lookupId, pattern) => `PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
CONSTRUCT {
  ?id <${ldvDef.sourceGraphPropId}> ?graph .
} WHERE {
  BIND(<${lookupId}> as ?id)
  GRAPH ?graph { ${pattern} }
}`,
  }

  window.ldvQueries = ldvQueries
})()
