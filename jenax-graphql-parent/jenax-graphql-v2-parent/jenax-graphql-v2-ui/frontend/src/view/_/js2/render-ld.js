// SPDX-License-Identifier: MIT
// based on https://github.com/zazuko/trifid-renderer-simple

/* global jsonld, ldvAddLabels, ldvBlankNodes, ldvAddLabelsForUris, getLdvLabelLang, getLdvLabelsOf, ldvConfig, ldvDef */

(() => {
  const termRegEx = new RegExp('(#|/)([^#/]*)$')
  const titlePredicates = ['http://schema.org/name', 'http://schema.org/headline', 'http://purl.org/dc/terms/title', 'http://www.w3.org/2000/01/rdf-schema#label', 'http://www.w3.org/2004/02/skos/core#prefLabel']
  const globals = {}

  const mod = (n, d) => ((n % d) + d) % d

  const hashCode = (s) => {
    var black = '000000'
    var hash = s.split('').reduce(function(a, b) {
      a = ((a << 5) - a) + b.charCodeAt(0)
      return a & a
    }, 0)
    hash = mod(hash, 0xffffff).toString(16)
    hash = black.substring(0, 6 - hash.length) + hash
    return hash.charAt(0) + hash.charAt(3) + hash.charAt(1) + hash.charAt(4) + hash.charAt(2) + hash.charAt(5)
  }

  const compareShortestValue = (map) => (a, b) =>
	map[a].length < map[b].length
	? -1 :
	map[b].length < map[a].length
	? 1 :
	map[a] === map[b]
	? (a < b) ? -1 : 1 :
	map[a] < map[b]
	? -1 :
	map[b] < map[a]
	? 1 : 0

  const iriLabel = (iri) => {
    const parts = termRegEx.exec(iri)

    if (!parts || parts.length === 0 || parts[parts.length - 1].length === 0) {
      return null
    }

    const localpart = parts[parts.length - 1]
    const begin = iri.substring(0, iri.length - localpart.length + 1)
    if (globals.prefixMap) {
      const prefixes = Object.keys(globals.prefixMap)
      prefixes.sort(compareShortestValue(globals.prefixMap))
      for (var i = 0; i < prefixes.length; ++i) {
	var p = prefixes[i]
	const pFull = globals.prefixMap[p]
	if (p === '@vocab')
	  p = ''
	if (iri.substring(0, pFull.length) === pFull) {
	  return `<span class="ldv-ns-color" style="color:#${hashCode(pFull)}">&#9640;&nbsp;</span>`
	    + `<span class="ldv-label"><span class="ldv-ns-label"><span class="ldv-ns-label-prefix">${p}</span><span class="ldv-ns-label-colon">:</span></span>${iri.substring(pFull.length)}</span>`
	}
      }
    }
    return `<span class="ldv-ns-color" style="color:#${hashCode(begin)}">&#9640;&nbsp;</span><span class="ldv-label">${localpart}</span>`
  }

  const findPrefLabelLangValue = (values, lang) => {
    const withLang = values.find(e => e['@language'] === lang)
    if (withLang)
      return withLang['@value']
    const withoutLang = values.find(e => !'@language' in e)
    if (withoutLang)
      return withoutLang['@value']
    return null
  }

  const subjectLabel = (subject, titlePredicates) => {
    const labelLang = getLdvLabelLang()
    return titlePredicates.reduce(function (label, titlePredicate) {
      return label || (titlePredicate in subject ? findPrefLabelLangValue(subject[titlePredicate], labelLang) : null)
    }, null)
  }

  const subjectSortId = (subject, titlePredicates) => {
    const label = subjectLabel(subject, titlePredicates) || subject['@id']

    if (subject['@id'].slice(0, 2) !== '_:') {
      return '0' + label // IRIs
    } else {
      return '1' + label // blank nodes
    }
  }

  const subjectSort = (myIri, titlePredicates) => (a, b) => {
    if (a['@id'] === b['@id'])
      return 0
    if (a['@id'] === myIri)
      return -1
    if (b['@id'] === myIri)
      return 1
    return subjectSortId(a, titlePredicates).localeCompare(subjectSortId(b, titlePredicates), undefined, { sensitivity: 'base', numeric: true })
  }

  const renderNodeLabelOrPlain = (node) => {
    if (typeof node === 'object') {
      if ('@type' in node && node['@type'] === ldvDef.moreResultsObjId)
	return String.fromCodePoint(0x10fff)
      return renderNode(node, '@id' in node ? iriLabel(node['@id']) : '')
    }
    return node
  }

  const nodeSort = (a, b) => {
    return renderNodeLabelOrPlain(a).localeCompare(renderNodeLabelOrPlain(b), undefined, { sensitivity: 'base', numeric: true })
  }

  const predicateLabel = (iri, vocab) => {
    const predicate = 'http://www.w3.org/2000/01/rdf-schema#label'
    const language = navigator.language || navigator.userLanguage

    for (var i = 0; i < vocab.length; i++) {
      const subject = vocab[i]

      if (subject['@id'] === iri && predicate in subject) {
	const objects = subject[predicate]

	for (var j = 0; j < objects.length; j++) {
          if (!('@language' in objects[j]) || objects[j]['@language'] === language) {
            return objects[j]['@value']
          }
	}
      }
    }

    return iriLabel(iri)
  }

  const render = (elementId, html) => {
    const element = document.getElementById(elementId)

    if (element) {
      element.innerHTML = html
    }
  }

  const renderLink = (iri, label) => {
    const origin = globals.datasetBase

    const loadMore = (iri === ldvDef.moreResultsObjId) ? ' onclick="return ldvLoadMore(this)"' : ''
    var navigate, same

    if (iri.slice(0, origin.length) === origin)
      navigate = iri.slice(origin.length)

    if (!navigate && globals.etld1) {
      const parsed = new URL(iri)
      same = parsed.hostname === globals.etld1 || parsed.hostname.endsWith(`.${globals.etld1}`)
    }

    if (iri === ldvDef.moreResultsObjId)
      label = 'Load more results'
    if (iri === label)
      label = `<span class="ldv-label">${label}</span>`

    return `<a href="${iri}" title="${iri}"` +
      (loadMore ? loadMore : ' onclick="return ldvNavigate(this,event)"') +
      (navigate || same ? '' : ' target="_blank"') + // open IRIs with the same origin in the same tab, all others in a new tab
      `>${label}</a>` +
      `<span class="ldv-expand-button" onclick="return ldvLoadInlinePlus(this)">${ldvDef.expandButtonText}</span>`
  }

  const renderTitle = (myIri, graph, titlePredicates) => {
    const subject = graph.filter(function (subject) {
      return subject['@id'] === myIri
    }).shift()

    if (!subject)
      return ''

    const title = subjectLabel(subject, titlePredicates)

    if (!title)
      return ''

    return `<h1>${title}</h1>`
  }

  const renderSticky = (myIri, graph) => {
    const resource = `<h4><a href="${myIri}">${bnodeMap(myIri, false)}</a></h4>`

    const subject = graph.filter(function (subject) {
      return subject['@id'] === myIri
    }).shift()

    var typeElements = ''

    if (subject && subject['@type']) {
      typeElements = 'a ' + subject['@type'].map(function (type) {
	return renderLink(type, iriLabel(type))
      }).join(', ')
    }

    const type = '<p>' + typeElements + '</p>'

    return '<span>' + resource + type + '</span>'
  }

  const renderPredicate = (iri, label) => {
    return renderLink(iri, '<b>' + (label || iri) + '</b>')
  }

  const renderIri = (iri, label) => {
    return renderLink(iri, label || iri)
  }

  const renderBlankNode = (blankNode) => {
    return `<a href="#${blankNode}">${blankNode}</a>`
  }

  const renderLiteral = (literal) => {
    if (typeof literal === 'string') {
      return `<span>${literal}</span>`
    } else {
      if ('@language' in literal) {
	return `<span><span>${literal['@value']}</span>&nbsp;` +
	  `<span>@<span>${literal['@language']}</span></span></span>`
      } else if ('@type' in literal) {
	return `<span><span>${literal['@value']}</span> ` +
	  `<span class="ldv-literal-datatype-p">(<span>` +
	  renderIri(literal['@type'], iriLabel(literal['@type'])) +
	  `</span>)</span></span>`
      } else {
	return `<span><span>${literal['@value']}</span></span>`
      }
    }
  }

  const bnodeMap = (bnodeId, returnNum) => {
    if (!bnodeId.startsWith('bnode://')) {
      if (returnNum)
	throw `Non-bnode ${bnodeId} in bnodeMap`
      else
	return bnodeId
    }

    const id = bnodeId.substring(8)
    const dict = (globals.bnodes ||= { _ : 0 })
    let num

    if (id in dict) {
      num = dict[id]
    } else {
      num = dict[id] = dict._
      dict._++
    }

    if (returnNum)
      return num
    else
      return `_:b${num}`
  }

  const renderBlankNodeLink = (bnodeId) => {
    const num = bnodeMap(bnodeId, true)

    return `<a style="--bnum:${num}" href="${bnodeId}" onclick="return ldvNavigate(this,event)"><span class="ldv-label">_:b${num}</span></a>`
  }

  const renderNode = (node, label) => {
    if (typeof node === 'object') {
      if ('@id' in node) {
	if (node['@id'].startsWith('bnode://')) {
	  return renderBlankNodeLink(node['@id']);
	} else if (node['@id'].indexOf('_:') !== 0) {
          return renderIri(node['@id'], label)
	} else {
          return renderBlankNode(node['@id'])
	}
      } else {
	return renderLiteral(node)
      }
    } else {
      return renderLiteral(node)
    }
  }

  const renderObjectElements = (objects) => {
    return objects.map(function (object) {
      return `<div class="ldv-objects-box">` +
	renderNode(object, '@id' in object ? iriLabel(object['@id']) : '') +
	(ldvConfig.graphLookup === 'yes' ?
	 `<span class="ldv-graph-lookup-button" onclick="return ldvLookupGraph(this)">?g</span>` : '') +
	`</div>`
    }).join('')
  }

  const renderTable = (myIri, subject, vocab) => {
    var head = '<thead class="table-subject"></thead>'

    if (subject['@id'] !== myIri)
      head = `<thead><tr><th colspan="2">${renderNode(subject)}</th></tr></thead>`

    const predicates = Object.keys(subject).slice()
    predicates.sort()
    var rows = predicates.map(function (predicate) {
      var objects = subject[predicate]
      if (Array.isArray(objects)) {
	objects = objects.slice()
	objects.sort(nodeSort)
      }

      if (predicate.slice(0, 1) === '@') {
	if (predicate === '@type') {
          predicate = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'

          objects = objects.map(function (type) {
            return {'@id': type}
          })
	} else {
          return ''
	}
      }

      var isReverse = (predicate.slice(0, 18) === ldvDef.reversePropPrefix + ':')
      if (isReverse)
	predicate = predicate.slice(18)

      return `<tr${isReverse ? ' class="rdf-inverse"' : ''}>` +
	`<td class="table-predicate col-lg-4">` +
	(predicate === ldvDef.sourceGraphPropId ? '<span class="ldv-source-graph-prop">source graph</span>' :
	 (isReverse ? "is " : "") +
	 renderPredicate(predicate, predicateLabel(predicate, vocab)) +
	 (isReverse ? " of" : "")) +
	`</td>` +
	`<td class="table-object col-lg-8">${renderObjectElements(objects)}</td>` +
	`</tr>`
    }).join('')

    return `<table id="${subject['@id']}" class="table table-striped table-graph">` +
      head +
      `<tbody>${rows}</tbody></table>`
  }

  const renderTables = (myIri, graph, vocab, titlePredicates) => {
    const subjects = graph.sort(subjectSort(myIri, titlePredicates))

    return subjects.map(function (subject) {
      return renderTable(myIri, subject, vocab)
    }).join('')
  }

  const embeddedGraph = (json) => {
    return jsonld.promises.flatten(json, {}).then(function (flat) {
      return jsonld.promises.expand(flat).then(function (json) {
	// if data contains quads, merge them all together
	return json.reduce(function (json, item) {
          if (item['@graph']) {
            return json.concat(item['@graph'])
          }

          return json.concat(item)
	}, [])
      })
    })
  }

  const renderTitleWithLang = (iri, graph) => {
    const title = renderTitle(iri, graph, titlePredicates)
    if (title !== '') {
      render('title', title)
    } else {
      getLdvLabelsOf(iri).then((e) => {
	if (e)
	  render('title', 'label' in e ? `<h1>${e.label}</h1>` : ``)
      })
    }
  }

  const renderTitleAgain = () => {
    const json = JSON.parse(document.getElementById('data').innerHTML)
    embeddedGraph(json).then(graph => {
      if (!graph.length)
	return
      const iri = graph[0]['@id']
      renderTitleWithLang(iri, graph)
    })
  }

  const findETLDWithCookie = () => {
    var i = 0,
	domain = document.domain,
	p = domain.split('.'),
	s = '_findETLD1_' + (new Date()).getTime()

    while (i < (p.length - 1) && document.cookie.indexOf(s + '=' + s) == -1) {
      domain = p.slice(-1 - (++i)).join('.')
      document.cookie = `${s}=${s};domain=${domain};`
    }
    document.cookie = `${s}=;expires=${(new Date(0)).toUTCString()};domain=${domain};`
    return domain
  }

  const renderLd = (iri, datasetBase, localMode, json) => {
    globals.prefixMap = json['@context']
    Promise.all([
      embeddedGraph({} /*'vocab'*/),
      embeddedGraph(json /*'data'*/)
    ]).then(results => {
      const vocab = results[0]
      const graph = results[1]

      globals.datasetBase = datasetBase
      globals.localMode = localMode
      globals.etld1 = findETLDWithCookie()

      renderTitleWithLang(iri, graph)
      render('subtitle', renderSticky(iri, graph))
      render('graph', renderTables(iri, graph, vocab, titlePredicates))

      ldvAddLabels()
      ldvBlankNodes(iri)
    }).catch(function (error) {
      console.error(error)
    })
  }

  const renderSubNode = (bnode, json) => {
    return new Promise((resolve, reject) => {
      Promise.all([
	embeddedGraph({} /*'vocab'*/),
	embeddedGraph(json /*'data'*/)
      ]).then((r) => {
	const bg = r[1]
	resolve(renderTables(bnode, bg, r[0], titlePredicates))
      }).catch((err) => {
	console.error(err)
      })
    })
  }

  const renderMoreResults = (json, s, p, elem, cell) => {
    return embeddedGraph(json)
      .then(graph => {
	const subject = graph.find(elem => elem['@id'] === s)
	if (!subject)
	  return

        if (p === 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')
	  p = '@type'

	var objects = subject[p]
	if (Array.isArray(objects)) {
	  objects = objects.slice()
	  objects.sort(nodeSort)
	}

	if (p === '@type') {
          p = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'

          objects = objects.map(function (type) {
	    return {'@id': type}
          })
	}

	const newUris = objects.filter(e => e['@id']).map(e => e['@id'])

	const moreHtml = renderObjectElements(objects)
	cell.insertAdjacentHTML('beforeend', moreHtml)
	elem.closest('div').remove()

	ldvAddLabelsForUris(newUris, cell.querySelectorAll('a[href]'))
	ldvBlankNodes(null)
      })
  }

  window.renderMoreResults = renderMoreResults
  window.renderSubNode = renderSubNode
  window.renderTitleAgain = renderTitleAgain
  window.renderLd = renderLd
})()
