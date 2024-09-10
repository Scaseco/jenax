/* global wellknown, L, ResizeObserver */

(() => {
  const makeMap = (lit) => {
    if (!lit) {
      return
    }
    const titleDiv = document.getElementById('title')
    const mapBoxDiv = document.createElement('div')
    mapBoxDiv.id = 'map'
    mapBoxDiv.classList.add('ldv-mini-map')
    const mapDiv = document.createElement('div')
    mapDiv.style.width = '100%'
    mapDiv.style.height = '100%'
    mapBoxDiv.appendChild(mapDiv)
    titleDiv.insertAdjacentElement('afterend', mapBoxDiv)
    const map = L.map(mapDiv)
    const tiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 16,
      attribution: '&copy; <a href="http://www.openstreetmap.org/fixthemap">OpenStreetMap</a>'
    }).addTo(map)
    const geojsonFeature = wellknown.parse(lit)
    const mapFeature = L.geoJSON(geojsonFeature).addTo(map)
    map.fitBounds(mapFeature.getBounds(), { maxZoom: 11 })
    const resizeObserver = new ResizeObserver(() => {
      map.invalidateSize();
    });
    resizeObserver.observe(mapDiv);
  }

  window.makeMap = makeMap
})()
