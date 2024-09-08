/* global renderTitleAgain, ldvUpdateConfigLink, ldvQueries, ldvConfig, ldvFetchTypeQuery */

(() => {
  const labels = { '': {} }
  const labelsPending = {}

  const fetchLabelsJson = (query) => {
    return ldvFetchTypeQuery('application/json', query)
      .then((response) => {
	if (!response.ok)
	  throw response
	return response.json()
      })
  }

  const ldvAddLabelsForUris = (uris, links) => {
    if (!uris.length)
      return

    const showLabels = isLdvShowLabels()
    const lang = getLdvLabelLang()

    getLdvLabelsForUris(uris, lang).then(() => {
      links.forEach(e => {
	const labelBox = e.querySelector('.ldv-label')
	if (!labelBox)
	  return

	if (labelsPending[lang][e.href])
	  return

	if (!e.classList.contains('isLabelled')) {
	  e.classList.add('isLabelled')
	  labels[''][e.href] = labelBox.innerHTML
	} else {
	  e.removeAttribute('title')
	  labelBox.innerHTML = labels[''][e.href]
	}

	if (!labels[lang][e.href] || !('label' in labels[lang][e.href])) {
	  e.classList.remove('isLabelled')
	  return
	}

	const label = labels[lang][e.href].label
	const llang = labels[lang][e.href].lang

	if (showLabels)
	  labelBox.innerHTML = `<em>${label}` +
	    (llang ?
	     `&nbsp;<span class="ldv-lang-tag"><span>@</span><span class="ldv-lang-tag-lang">${llang}</span></span>` :
	     '') + `</em>`
	else
	  e.title = label + (llang ? ' @' + llang : '')
      })
    })
      .catch((err) => {
	err.text().then(msg => console.log(`Error fetching labels:`, {status: err.status, statusText: err.statusText, body: msg})).catch(err => console.log(`Error fetching labels:`, {status: err.status, statusText: err.statusText, err: err}))
      })
  }

  const getLdvLabelsForUris = (uris, lang) => {
    const infer = ldvConfig.infer

    labels[lang] ||= {}
    labelsPending[lang] ||= {}
    const newUris = Array.from(new Set(Array.from(uris)
				       .filter(e => !(e in labels[lang] || e in labelsPending[lang]))))
    newUris.sort()

    if (!newUris.length)
      return Promise.resolve([])

    newUris.forEach(e => labelsPending[lang][e] = true)
    const values = newUris.map(e => `<${ e.startsWith('bnode://') ? '_:' + e.slice(8) : e }>`).join(' ')
    const query = ldvQueries.fetchLabelsQuery(values, lang, infer)

    return fetchLabelsJson(query).then((json) => {
      newUris.forEach(e => delete labelsPending[lang][e])
      json.forEach(e => labels[lang][e.uri] = e)
    })
  }

  const getLdvLabelsOf = (iri) => {
    const lang = getLdvLabelLang()
    return getLdvLabelsForUris([iri], lang).then(() => labels[lang][iri])
  }

  const ldvAddLabels = () => {
    const root = document.querySelector('#graph table[id]')
    if (!root)
      return
    const links = document.querySelectorAll([
      '#graph table[id] a[href]',
      '#subtitle p a[href]',
      '#graphLookupPopup table[id] a[href]',
    ].join(','))
    const uris = Array.from(new Set(Array.from(links).map(e => e.href)))
    uris.sort()
    ldvAddLabelsForUris(uris, links)
  }

  const isLdvShowLabels = () => {
    return window.localStorage.getItem('/ldv/loadlabels') === null
  }

  const getLdvLabelLang = () => {
    const localLang = window.localStorage.getItem('/ldv/labellang')
    if (localLang)
      return localLang
    else
      return ldvConfig.labelLang
  }

  const renderLdvLabelConfig = () => {
    const labelConfigHtml = document.getElementById('loadlabels')
    const checked = isLdvShowLabels()
    const currentLang = getLdvLabelLang()
    const checkedIf = c => c ? ' checked' : ''
    const selectedIf = c => c ? ' selected' : ''
    labelConfigHtml.innerHTML = `<input type="checkbox" onclick="ldvChangeLabelConfig(this)" id="loadlabelsx"${checkedIf(checked)} />` +
      `<label for="loadlabelsx">Resolve labels</label>` +
      (ldvConfig.labelLangChoice.length > 1 ?
       ` <select class="ldv-lang-select" onchange="ldvChangeLabelLanguage(this)" id="loadlabelslang">` +
       ldvConfig.labelLangChoice.map(lang => `<option value="${lang}"${selectedIf(currentLang === lang)}>${lang}</option>`) +
       `</select>` :
       '')
  }

  const redoLabels = () => {
    ldvUpdateConfigLink()
    renderTitleAgain()
    ldvAddLabels()
  }

  const ldvChangeLabelConfig = (elem) => {
    if (elem.checked)
      window.localStorage.removeItem('/ldv/loadlabels')
    else
      window.localStorage.setItem('/ldv/loadlabels', false)
    redoLabels()
  }

  const ldvChangeLabelLanguage = (elem) => {
    if (elem.value === ldvConfig.labelLang)
      window.localStorage.removeItem('/ldv/labellang')
    else
      window.localStorage.setItem('/ldv/labellang', elem.value)
    redoLabels()
  }

  window.renderLdvLabelConfig = renderLdvLabelConfig
  window.isLdvShowLabels = isLdvShowLabels
  window.getLdvLabelLang = getLdvLabelLang
  window.ldvChangeLabelLanguage = ldvChangeLabelLanguage
  window.ldvChangeLabelConfig = ldvChangeLabelConfig
  window.ldvAddLabels = ldvAddLabels
  window.ldvAddLabelsForUris = ldvAddLabelsForUris
  window.getLdvLabelsOf = getLdvLabelsOf
})()
