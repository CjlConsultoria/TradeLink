/**
 * Exporta dados como CSV e faz download no browser.
 * @param {Array<Object>} data - array de objetos
 * @param {Array<{key: string, label: string}>} columns - colunas a exportar
 * @param {string} filename - nome do arquivo (sem extensão)
 */
export function exportCsv(data, columns, filename = 'export') {
  if (!data || !data.length) return
  const sep = ';'
  const header = columns.map(c => `"${c.label}"`).join(sep)
  const rows = data.map(row =>
    columns.map(c => {
      let val = row[c.key]
      if (val === null || val === undefined) val = ''
      if (typeof val === 'number') val = String(val).replace('.', ',')
      return `"${String(val).replace(/"/g, '""')}"`
    }).join(sep)
  )
  const bom = '\uFEFF'
  const csv = bom + [header, ...rows].join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
