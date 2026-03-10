// Forex popular
const FOREX = [
  { value: 'USD', label: 'Dólar (USD)', category: 'forex', popular: true },
  { value: 'EUR', label: 'Euro (EUR)', category: 'forex', popular: true },
  { value: 'GBP', label: 'Libra (GBP)', category: 'forex', popular: true },
  { value: 'JPY', label: 'Iene (JPY)', category: 'forex', popular: true },
  { value: 'CHF', label: 'Franco (CHF)', category: 'forex', popular: true },
  { value: 'CAD', label: 'Dólar Canadense (CAD)', category: 'forex', popular: true },
  { value: 'AUD', label: 'Dólar Australiano (AUD)', category: 'forex', popular: true },
  { value: 'NZD', label: 'Dólar Neozelandês (NZD)', category: 'forex', popular: false },
  { value: 'CNY', label: 'Yuan (CNY)', category: 'forex', popular: true },
  { value: 'ARS', label: 'Peso Argentino (ARS)', category: 'forex', popular: true },
  { value: 'MXN', label: 'Peso Mexicano (MXN)', category: 'forex', popular: false }
]

// Crypto popular
const CRYPTO = [
  { value: 'BTC', label: 'Bitcoin (BTC)', category: 'crypto', popular: true },
  { value: 'ETH', label: 'Ethereum (ETH)', category: 'crypto', popular: true },
  { value: 'BNB', label: 'BNB (BNB)', category: 'crypto', popular: true },
  { value: 'XRP', label: 'Ripple (XRP)', category: 'crypto', popular: true },
  { value: 'SOL', label: 'Solana (SOL)', category: 'crypto', popular: true },
  { value: 'ADA', label: 'Cardano (ADA)', category: 'crypto', popular: true },
  { value: 'DOGE', label: 'Dogecoin (DOGE)', category: 'crypto', popular: true },
  { value: 'DOT', label: 'Polkadot (DOT)', category: 'crypto', popular: false },
  { value: 'AVAX', label: 'Avalanche (AVAX)', category: 'crypto', popular: false },
  { value: 'MATIC', label: 'Polygon (MATIC)', category: 'crypto', popular: false },
  { value: 'LINK', label: 'Chainlink (LINK)', category: 'crypto', popular: false },
  { value: 'UNI', label: 'Uniswap (UNI)', category: 'crypto', popular: false },
  { value: 'LTC', label: 'Litecoin (LTC)', category: 'crypto', popular: false }
]

// Commodities
const COMMODITIES = [
  { value: 'OURO', label: 'Ouro', category: 'commodities', popular: true },
  { value: 'PRATA', label: 'Prata', category: 'commodities', popular: false }
]

export const MOEDAS = [...FOREX, ...CRYPTO, ...COMMODITIES]

export const MOEDAS_POPULARES = MOEDAS.filter(m => m.popular)
export const MOEDAS_FOREX = FOREX
export const MOEDAS_CRYPTO = CRYPTO
export const MOEDAS_COMMODITIES = COMMODITIES

export const PARES = [
  { value: 'BRL', label: 'Real (BRL)' },
  { value: 'USD', label: 'Dólar (USD)' }
]

export const STATUS_COLORS = {
  ATIVA: 'bg-green-100 text-green-800',
  EXECUTADA: 'bg-blue-100 text-blue-800',
  CANCELADA: 'bg-red-100 text-red-800'
}

export const TIPO_COLORS = {
  COMPRA: 'bg-emerald-100 text-emerald-800',
  VENDA: 'bg-rose-100 text-rose-800'
}

export const CATEGORIAS_ATIVO = [
  { value: 'CRYPTO', label: 'Criptomoeda' },
  { value: 'FOREX', label: 'Moeda / Forex' },
  { value: 'ACAO', label: 'Acao' },
  { value: 'COMMODITIES', label: 'Commodity' },
  { value: 'OUTRO', label: 'Outro' }
]
