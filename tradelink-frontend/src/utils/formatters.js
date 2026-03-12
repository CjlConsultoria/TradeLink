// Códigos ISO 4217 válidos que o Intl.NumberFormat aceita
const VALID_CURRENCY_CODES = new Set([
  'BRL', 'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD',
  'CNY', 'HKD', 'SGD', 'KRW', 'MXN', 'ARS', 'CLP', 'COP', 'PEN',
  'UYU', 'BOB', 'PYG', 'TWD', 'THB', 'IDR', 'MYR', 'PHP', 'INR',
  'ZAR', 'TRY', 'RUB', 'PLN', 'CZK', 'HUF', 'SEK', 'NOK', 'DKK',
  'ILS', 'AED', 'SAR', 'EGP', 'NGN', 'KES'
])

export function formatCurrency(value, currency = 'BRL') {
  if (value == null) return '-'
  const cur = (currency || 'BRL').toUpperCase()
  if (VALID_CURRENCY_CODES.has(cur)) {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: cur,
      minimumFractionDigits: 2,
      maximumFractionDigits: 8
    }).format(value)
  }
  // Fallback para moedas não-ISO (USDT, BTC, ETH, etc.)
  const formatted = new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  }).format(value)
  return `${formatted} ${cur}`
}

export function formatPercent(value) {
  if (value == null) return '-'
  const num = Number(value)
  const sign = num >= 0 ? '+' : ''
  return sign + num.toFixed(2) + '%'
}

export function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

export function formatDateShort(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('pt-BR')
}