(() => {
  const ldvConfig = {
    endpointUrl: '/sparql',
    endpointOptions: {
      mode: 'cors',
      credentials: 'same-origin',
      method: 'POST',
    },
    datasetBase: window.location.origin,
    exploreUrl: '@EXPLORE_URL@',
    graphLookup: '@GRAPH_LOOKUP@',
    reverseEnabled: 'no',
    labelLang: 'en',
    labelLangChoice: ['en', 'de', 'nl', 'fr'],
    infer: false,
    fileOnly: 'yes',
  }

  window.ldvConfig = ldvConfig
})()
