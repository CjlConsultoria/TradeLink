/**
 * Validação de CNPJ e CPF (dígitos verificadores - módulo 11).
 */

export function apenasDigitos (valor) {
  if (valor == null) return ''
  return String(valor).replace(/\D/g, '')
}

function mod11 (soma) {
  const r = soma % 11
  return r < 2 ? 0 : 11 - r
}

export function isValidCnpj (cnpj) {
  const s = apenasDigitos(cnpj)
  if (s.length !== 14) return false
  if (new Set(s).size <= 1) return false
  const p1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
  const p2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]
  let soma = 0
  for (let i = 0; i < 12; i++) soma += parseInt(s[i], 10) * p1[i]
  if (mod11(soma) !== parseInt(s[12], 10)) return false
  soma = 0
  for (let i = 0; i < 13; i++) soma += parseInt(s[i], 10) * p2[i]
  return mod11(soma) === parseInt(s[13], 10)
}

export function isValidCpf (cpf) {
  const s = apenasDigitos(cpf)
  if (s.length !== 11) return false
  if (new Set(s).size <= 1) return false
  let soma = 0
  for (let i = 0; i < 9; i++) soma += parseInt(s[i], 10) * (10 - i)
  if (mod11(soma) !== parseInt(s[9], 10)) return false
  soma = 0
  for (let i = 0; i < 10; i++) soma += parseInt(s[i], 10) * (11 - i)
  return mod11(soma) === parseInt(s[10], 10)
}

const reEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
export function isValidEmail (email) {
  if (!email || typeof email !== 'string') return false
  return reEmail.test(email.trim())
}

/** Telefone: aceita 10 ou 11 dígitos (com DDD). */
export function isValidTelefone (tel) {
  const s = apenasDigitos(tel)
  return s.length === 10 || s.length === 11
}

export function formatarCnpj (cnpj) {
  if (cnpj == null || cnpj === '') return ''
  const s = apenasDigitos(cnpj)
  if (s.length !== 14) return String(cnpj).trim()
  return `${s.slice(0, 2)}.${s.slice(2, 5)}.${s.slice(5, 8)}/${s.slice(8, 12)}-${s.slice(12)}`
}

export function formatarCpf (cpf) {
  if (cpf == null || cpf === '') return ''
  const s = apenasDigitos(cpf)
  if (s.length !== 11) return String(cpf).trim()
  return `${s.slice(0, 3)}.${s.slice(3, 6)}.${s.slice(6, 9)}-${s.slice(9)}`
}
