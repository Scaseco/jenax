/* global ldvLoadSubResource, ldvAddLabels, ldvDef */

(() => {
  const globals = { resolved: {} }

  const ldvBlankNodes = (rootIri, resolved) => {
    const root = document.querySelector('#graph table[id]')
    if (!root)
      return

    resolved ||= globals.resolved

    if (rootIri)
      resolved[rootIri] = 1

    const links = document.querySelectorAll('#graph table[id] td.table-object a[href]')
    const bnodes = Array.from(
      new Set(
	Array.from(links)
	  .map(e => e.href)
	  .filter(e => e.startsWith('bnode://'))
	  .filter(e => !(e in resolved))
      ))
    bnodes.sort()
    if (!bnodes.length) // no more new blank nodes to resolve, fetch new labels
      ldvAddLabels()
    else
      ldvResolveSubNodes(bnodes, links, resolved)
  }

  const ldvResolveSubNodes = (bnodes, links, resolved) => {
    if (!bnodes.length)
      return

    const next = (bnodes, links) => {
      if (bnodes.length === 1)
	ldvBlankNodes(null, resolved) // all current nodes resolved, recurse looking for new blank nodes
      else
	ldvResolveSubNodes(bnodes.slice(1), links, resolved)
    }

    resolved ||= globals.resolved

    const node = bnodes[0]
    if (node in resolved) {
      next(bnodes, links)
      return
    }

    resolved[node] = 0

    const gNode = node.startsWith('bnode://') ? '_:' + node.slice(8) : node

    const es = []
    const heads = []
    let i = 0
    links.forEach(e => {
      if (e.href === node) {
	const head = e.outerHTML
	heads.push(head)
	const id = `bn_${i++}_${e.href}`
	e.outerHTML = `<div id="${id}" class="ldv-sub-node">` +
	  `<div>${head}</div>` +
	  `<div class="ldv-sub-node-sub">Loading...</div>` +
	  `</div>`
	const newE = document.getElementById(id)
	newE.removeAttribute('id')
	es.push(newE)
      }
    })

    ldvLoadSubResource(gNode)
      .then((html) => {
	resolved[node] = 1
	es.forEach(e => {
	  const p = e.parentElement
	  const head = heads.shift()

	  let overflow = ''
	  if (p.classList.contains('ldv-objects-box')) {
	    p.classList.add('ldv-objects-box-expand')
	    overflow = 'ldv-sub-node-overflow '
	  }

	  e.outerHTML = `<div class="${overflow}ldv-sub-node">` +
	    `<div>${head}</div>` +
	    `<div class="ldv-sub-node-sub">${html}</div>` +
	    `</div>`
	  resolved[node] += 1
	})
      })
      .catch((err) => {
	resolved[node] = -1
	console.log(`${node}: ${err}`)
	es.forEach(e => {
	  const head = heads.shift()
	  const expandButton = e.nextElementSibling
	  e.outerHTML = head
	  // restore expand button that was changed in load.js:ldvNavigate/ldvLoadInlinePlus
	  if (expandButton && expandButton.textContent === ldvDef.collapseButtonText)
	    expandButton.textContent = ldvDef.expandButtonText
	  resolved[node] -= 1
	})
      })
      .finally(() => {
	next(bnodes, links)
      })

  }

  const ldvUnresolveSubNodes = (nodes, links, resolved) => {
    resolved ||= globals.resolved

    links.forEach(link => {
      const subNodes = link.querySelectorAll(':scope table[id]')
      subNodes.forEach(n => delete resolved[n.id])
    })
    nodes.forEach(node => delete resolved[node])
  }

  window.ldvBlankNodes = ldvBlankNodes
  window.ldvResolveSubNodes = ldvResolveSubNodes
  window.ldvUnresolveSubNodes = ldvUnresolveSubNodes
})()
