// Service worker mínimo para receber Web Push
self.addEventListener('push', (event) => {
  let data = { title: 'TradeLink', body: '' }
  if (event.data) {
    try {
      data = event.data.json()
    } catch (_) {
      data.body = event.data.text()
    }
  }
  event.waitUntil(
    self.registration.showNotification(data.title || 'TradeLink', {
      body: data.body || 'Nova notificação',
      icon: '/favicon.ico',
      tag: 'tradelink-notif'
    })
  )
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      if (clientList.length) clientList[0].focus()
      else if (clients.openWindow) clients.openWindow('/')
    })
  )
})
