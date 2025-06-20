/* global renderLd, ldvConfig, ldvFetchTypeQuery, ldvFindMap, ldvDef, Yasqe */
(() => {
  const loadStartpage = () => {
    document.getElementById('startpage').innerHTML = `
<button onclick="ldvStartpageClassesOnt()">List ontology classes</button>
<button onclick="ldvStartpageClassesInst()">List instance classes</button>
<button onclick="ldvStartpageInstancesRand()">Random instances</button>
<button onclick="ldvStartpageIriInput()">IRI</button>
<button onclick="ldvStartpageSparql()">SPARQL query</button>
<div id="yasqe" style="display: none"></div>
`
    const bIri = `urn:x-ldv:classes-root`
    const describeQuery = `
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

CONSTRUCT {
  ?s ?p ?o 
} WHERE {
  BIND(<${bIri}> AS ?s) .
  {
    BIND(<urn:x-ldv:reverse:http://www.w3.org/2000/01/rdf-schema#subClassOf> AS ?p )
    {
      SELECT ?o WHERE {
        {
          ?o a owl:Class .
        } UNION {
          ?o a rdfs:Class .
        }
        FILTER NOT EXISTS {
          {
            ?o rdfs:subClassOf []
          } MINUS {
            ?o rdfs:subClassOf owl:Thing
          }
        }
      }
    }
  }
}
`
    fetchJsonLd(describeQuery)
      .then((json) => {
	document.getElementById('data').innerHTML = JSON.stringify(json)
	renderLd(bIri, ldvConfig.datasetBase, ldvConfig.localMode, json)
      })

  }

  const fetchJsonLd = (query) => {
    return ldvFetchTypeQuery('application/ld+json', query)
      .then((response) => {
	if (!response.ok)
	  throw response
	return response.json()
      })
  }

  const startpageMoreClassesOntQuery = (limit, offset) => {
    const bIri = ldvDef.classesOntPropId
    return `
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

CONSTRUCT {
  ?s ?p ?o .
} {
  BIND(BNODE() as ?s)
  VALUES ?p { <${bIri}> }
  {
    SELECT ?o {
      {
        ?o a owl:Class
      } UNION {
        ?o a rdfs:Class
      }
    } LIMIT ${limit} OFFSET ${offset}
  } UNION {
      LATERAL {
        SELECT (count(?ox) AS ?oCnt) {
          {
	    SELECT ?ox {
	      {
	        ?ox a owl:Class
	      } UNION {
	        ?ox a rdfs:Class
	      }
	    } LIMIT ${limit + 1} OFFSET ${offset}
          }
        }
      } bind(if(?oCnt>10,strdt('...',<${ldvDef.moreResultsObjId}>),coalesce()) AS ?o)
  }
}
`
  }
  const startpageClassesOnt = () => {
    document.getElementById('graph').innerHTML = `Loading...`
    const bIri = ldvDef.classesOntPropId
    const describeQuery = `
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>

CONSTRUCT {
  ?s ?p ?o .
} {
  BIND(BNODE() as ?s)
  VALUES ?p { <${bIri}> }
  {
    SELECT ?o {
      {
        ?o a owl:Class
      } UNION {
        ?o a rdfs:Class
      }
    } LIMIT 10
  } UNION {
      LATERAL {
        SELECT (count(?ox) AS ?oCnt) {
          {
	    SELECT ?ox {
	      {
	        ?ox a owl:Class
	      } UNION {
	        ?ox a rdfs:Class
	      }
	    } LIMIT 11
          }
        }
      } bind(if(?oCnt>10,strdt('...',<${ldvDef.moreResultsObjId}>),coalesce()) AS ?o)
  }
}
`
    fetchJsonLd(describeQuery)
      .then((json) => {
	document.getElementById('data').innerHTML = JSON.stringify(json)
	renderLd(bIri, ldvConfig.datasetBase, ldvConfig.localMode, json)
      })
  }

  const startpageMoreClassesInstQuery = (limit, offset) => {
    const bIri = ldvDef.classesInstPropId
    return `
CONSTRUCT {
  ?s ?p ?o .
} {
  BIND(BNODE() as ?s)
  VALUES ?p { <${bIri}> }
  {
    SELECT DISTINCT ?o {
      [] a ?o
    } LIMIT ${limit} OFFSET ${offset}
  } UNION {
      LATERAL {
        SELECT (count(?ox) AS ?oCnt) {
          {
	    SELECT DISTINCT ?ox {
              [] a ?ox
	    } LIMIT ${limit + 1} OFFSET ${offset}
          }
        }
      } bind(if(?oCnt>10,strdt('...',<${ldvDef.moreResultsObjId}>),coalesce()) AS ?o)
  }
}
`
  }

  const startpageClassesInst = () => {
    document.getElementById('graph').innerHTML = `Loading...`
    const bIri = ldvDef.classesInstPropId
    const describeQuery = `
CONSTRUCT {
  ?s ?p ?o .
} {
  BIND(BNODE() as ?s)
  VALUES ?p { <${bIri}> }
  {
    SELECT DISTINCT ?o {
      [] a ?o
    } LIMIT 10
  } UNION {
      LATERAL {
        SELECT (count(?ox) AS ?oCnt) {
          {
	    SELECT DISTINCT ?ox {
              [] a ?ox
	    } LIMIT 11
          }
        }
      } bind(if(?oCnt>10,strdt('...',<${ldvDef.moreResultsObjId}>),coalesce()) AS ?o)
  }
}
`
    fetchJsonLd(describeQuery)
      .then((json) => {
	document.getElementById('data').innerHTML = JSON.stringify(json)
	renderLd(bIri, ldvConfig.datasetBase, ldvConfig.localMode, json)
      })
  }

  const startpageInstancesRand = () => {
    document.getElementById('graph').innerHTML = `Loading...`
    const bIri = ldvDef.instancesPropId
    const describeQuery = `
CONSTRUCT {
  ?s ?p ?o .
} {
  BIND(BNODE() as ?s)
  VALUES ?p { <${bIri}> }
  {
    SELECT DISTINCT ?o {
      ?o ?px []
    } ORDER BY RAND() LIMIT 10
  }
}
`
    fetchJsonLd(describeQuery)
      .then((json) => {
	document.getElementById('data').innerHTML = JSON.stringify(json)
	renderLd(bIri, ldvConfig.datasetBase, ldvConfig.localMode, json)
	ldvFindMap(bIri, json)
      })
  }

  const startpageIriInput = () => {
    const iri = window.prompt('IRI?')
    if (iri !== null) {
      const navigate = (ldvConfig.fileOnly === 'yes' ? `?` : `/`) + (ldvConfig.localMode ? '*' : '') + '?' + iri
      window.location = navigate
    }
  }

  const startpageSparql = () => {
    const now = document.getElementById('yasqe').style.display
    if (!window.yasqe_loaded) {
      const yasgui_style = document.createElement('link')
      yasgui_style.rel = 'stylesheet'
      yasgui_style.type = 'text/css'
      yasgui_style.href = '/yasgui/yasgui.min.css'
      
      const yasgui_script = document.createElement('script')
      yasgui_script.setAttribute('src', '/yasgui/yasgui.min.js')
      yasgui_script.onload = () => {
	const load_yasqe = document.createElement('script')
	const yasqe = new Yasqe(document.getElementById('yasqe'), {
	  requestConfig: {
	    endpoint: '/sparql',
	  },
	  value: `CONSTRUCT {
  ?s ?p ?o
} WHERE {
  {
    SELECT ?s ?p ?o WHERE {
      ?s ?p ?o
    } LIMIT 10
  }
}
`
	})
	yasqe.query = () => {
	  const rt = {
	    catch: (_f) => {}
	  }
	  const typ = yasqe.getQueryType()
	  if (typ !== "CONSTRUCT" && typ !== "DESCRIBE") {
	    alert("Only queries returning a graph can be used")
	    return rt
	  }
	  document.getElementById('graph').innerHTML = `Loading...`
	  fetchJsonLd(yasqe.getQueryWithValues())
	    .then((json) => {
	      document.getElementById('data').innerHTML = JSON.stringify(json)
	      var bIri
	      if (json['@id'])
		bIri = json['@id']
	      else if (json['@graph'] && json['@graph'].length > 0)
		bIri = json['@graph'][0]['@id']
	      if (bIri) {
		renderLd(bIri, ldvConfig.datasetBase, ldvConfig.localMode, json)
		ldvFindMap(bIri, json)
	      } else {
		document.getElementById('graph').innerHTML = `No result`
	      }
	    })
	    .catch((err) => {
	      document.getElementById('graph').innerHTML = `Error: <div id="query-error"></div>`
	      err.text().then((text) => document.getElementById('query-error').innerHTML = text)
	    })
	  return rt
	}
	window.yasqe = yasqe
      }
      document.getElementsByTagName('head')[0].append(yasgui_style, yasgui_script)
      window.yasqe_loaded = true
    }
    document.getElementById('yasqe').style.display = now === 'none' ? 'block' : 'none'
  }

  window.loadStartpage = loadStartpage
  window.ldvStartpageClassesOnt = startpageClassesOnt
  window.ldvStartpageClassesInst = startpageClassesInst
  window.ldvStartpageInstancesRand = startpageInstancesRand
  window.ldvStartpageIriInput = startpageIriInput
  window.ldvStartpageSparql = startpageSparql
  window.ldvStartpageMoreClassesInstQuery = startpageMoreClassesInstQuery
  window.ldvStartpageMoreClassesOntQuery = startpageMoreClassesOntQuery
})()
