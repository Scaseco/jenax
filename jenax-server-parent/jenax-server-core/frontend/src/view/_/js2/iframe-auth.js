/* global ldvConfig */
(() => {
  const addIframeAuth = () => {
    if (window.location.pathname.substring(0, 2) === '/_') // internal files
      return

    var pathname
    if (ldvConfig.fileOnly === 'yes') {
      pathname = '/' + window.location.search.substring(1)
      let searchStart = pathname.indexOf("?", 1)
      if (searchStart !== -1) {
	pathname = pathname.substring(0, searchStart)
      }
    } else {
      pathname = window.location.pathname
    }

    if (pathname.slice(0, 2) === '/*' && pathname.length > 2)
      return

    document.querySelector('body').insertAdjacentHTML(
      'beforeend',
      `<iframe src="${ldvConfig.endpointUrl}?query=ask{}" width="1" height="1" id="iframe-auth" onload="ldvLoadWindowResource()"></iframe>`)
  }

  if (ldvConfig.endpointOptions.credentials === 'include') {
    window.useIframeAuth = true
    window.addEventListener('DOMContentLoaded', (event) => addIframeAuth())
  }
})()
