import "./style.scss"
import Yasgui from '@zazuko/yasgui'
import GeoPlugin from 'yasgui-geo-tg'

Yasgui.Yasr.registerPlugin('geo', GeoPlugin)
Yasgui.Yasr.defaults.pluginOrder.push('geo')

var yasgui = null

function getCurrent() {
  if(yasgui != null) {
    return yasgui.current()
  }
  return null
}
;(function () {
  var _orig = Yasgui.Yasr.plugins.table.prototype.getUriLinkFromBinding
  Yasgui.Yasr.plugins.table.prototype.getUriLinkFromBinding = function(e, t) {
    var tmp = document.createElement('div')
    tmp.innerHTML = _orig.call(this, e, t)
    var a = tmp.getElementsByTagName('A')
    if (a.length > 0) {
      a[0].setAttribute('onclick', 'window.open("../view/?*?' + a[0].href + '");return !1')
    }
    return tmp.innerHTML
  }
})()

const element = document.createElement('div')
element.setAttribute('id', 'yasgui')
document.body.appendChild(element)
yasgui = new Yasgui(element, {
  requestConfig: { endpoint: document.location.href },
  copyEndpointOnNewTab: false,
})

