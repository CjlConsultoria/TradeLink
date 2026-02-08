export function formatCurrency(value, currency = 'BRL') {
  if (value == null) return '-'
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  }).format(value)
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