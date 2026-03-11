const PptxGenJS = require('pptxgenjs')
const fs = require('fs')

// ========== COLOR PALETTE ==========
const C = {
  navy: '1E1B4B', navyLight: '312E81', indigo: '6366F1', indigoLight: 'E0E7FF',
  white: 'FFFFFF', gray50: 'F9FAFB', gray100: 'F3F4F6', gray200: 'E5E7EB',
  gray500: '6B7280', gray700: '374151', gray900: '111827',
  emerald: '10B981', emeraldBg: 'D1FAE5', amber: 'F59E0B', amberBg: 'FEF3C7',
  blue: '3B82F6', blueBg: 'DBEAFE', red: 'EF4444', redBg: 'FEE2E2',
  teal: '14B8A6', tealBg: 'CCFBF1'
}

function addBg(slide, color = C.white) {
  slide.background = { color }
}

function addHeader(slide, title, subtitle) {
  slide.addShape('rect', { x: 0, y: 0, w: '100%', h: 1.2, fill: { color: C.navy } })
  slide.addText(title, { x: 0.5, y: 0.15, w: 9, h: 0.55, fontSize: 22, fontFace: 'Arial', color: C.white, bold: true })
  if (subtitle) slide.addText(subtitle, { x: 0.5, y: 0.65, w: 9, h: 0.35, fontSize: 12, fontFace: 'Arial', color: C.indigoLight })
}

function addFooter(slide, text = 'TradeLink — Plataforma de Gestão de Investimentos') {
  slide.addText(text, { x: 0, y: '92%', w: '100%', h: 0.4, fontSize: 8, fontFace: 'Arial', color: C.gray500, align: 'center' })
}

function card(slide, x, y, w, h, opts = {}) {
  slide.addShape('roundRect', { x, y, w, h, rectRadius: 0.1, fill: { color: opts.fill || C.white }, shadow: { type: 'outer', blur: 6, offset: 2, color: '00000015' } })
}

function statCard(slide, x, y, value, label, color) {
  card(slide, x, y, 2.1, 1.1)
  slide.addText(value, { x: x + 0.15, y: y + 0.15, w: 1.8, h: 0.5, fontSize: 26, bold: true, color: color, fontFace: 'Arial' })
  slide.addText(label, { x: x + 0.15, y: y + 0.6, w: 1.8, h: 0.3, fontSize: 10, color: C.gray500, fontFace: 'Arial' })
}

// ========== GENERATE PPTX ==========
async function createPPTX() {
  const pptx = new PptxGenJS()
  pptx.author = 'TradeLink'
  pptx.title = 'TradeLink — Guia Completo da Plataforma'
  pptx.layout = 'LAYOUT_WIDE'

  // ---- SLIDE 1: Cover ----
  let s = pptx.addSlide()
  addBg(s, C.navy)
  s.addText('◈', { x: 4.2, y: 1.2, w: 1.5, h: 1.5, fontSize: 72, fontFace: 'Arial', color: C.indigo, align: 'center' })
  s.addText('TradeLink', { x: 1, y: 2.7, w: 8, h: 0.8, fontSize: 44, fontFace: 'Arial', color: C.white, bold: true, align: 'center' })
  s.addText('Plataforma Completa de Gestão de Investimentos', { x: 1, y: 3.5, w: 8, h: 0.5, fontSize: 16, fontFace: 'Arial', color: C.indigoLight, align: 'center' })
  s.addText('Guia Completo para Consultores e Clientes', { x: 1, y: 4.2, w: 8, h: 0.4, fontSize: 12, fontFace: 'Arial', color: C.gray500, align: 'center' })

  // ---- SLIDE 2: Overview ----
  s = pptx.addSlide()
  addHeader(s, 'Visão Geral', 'O que é o TradeLink')
  addBg(s)
  const overviewItems = [
    ['Gestão Centralizada', 'Gerencie todos os clientes, carteiras e recomendações em um só lugar'],
    ['Automatização', 'Copy Trading, Rebalanceamento automático e alertas inteligentes'],
    ['Transparência', 'Clientes acompanham performance, metas e recomendações em tempo real'],
    ['Segurança', 'Autenticação OTP, criptografia e controle de acesso por perfil']
  ]
  overviewItems.forEach((item, i) => {
    const yPos = 1.5 + (i * 0.85)
    card(s, 0.5, yPos, 9, 0.75)
    s.addText(item[0], { x: 0.7, y: yPos + 0.08, w: 2.5, h: 0.3, fontSize: 13, bold: true, color: C.indigo, fontFace: 'Arial' })
    s.addText(item[1], { x: 0.7, y: yPos + 0.38, w: 8.5, h: 0.3, fontSize: 11, color: C.gray700, fontFace: 'Arial' })
  })
  addFooter(s)

  // ---- SLIDE 3: Planilha vs Sistema ----
  s = pptx.addSlide()
  addHeader(s, 'Planilha → Sistema', 'Como migrar suas rotinas')
  addBg(s)
  const mappings = [
    ['Cadastro de clientes (Excel)', 'Tela de Clientes + Cadastro automático'],
    ['Carteiras em planilha', 'Módulo Carteiras com ativos em tempo real'],
    ['Recomendações via WhatsApp', 'Kanban de Recomendações + notificações'],
    ['Controle de performance manual', 'Dashboard com gráficos automáticos'],
    ['Metas no papel', 'Módulo Metas com progresso visual'],
    ['Alertas via e-mail manual', 'Alertas de Preço automáticos']
  ]
  // Table header
  s.addShape('rect', { x: 0.5, y: 1.45, w: 9, h: 0.4, fill: { color: C.navy } })
  s.addText('Antes (Planilha)', { x: 0.6, y: 1.47, w: 4.3, h: 0.35, fontSize: 11, bold: true, color: C.white, fontFace: 'Arial' })
  s.addText('Agora (TradeLink)', { x: 5, y: 1.47, w: 4.3, h: 0.35, fontSize: 11, bold: true, color: C.white, fontFace: 'Arial' })
  mappings.forEach((m, i) => {
    const yPos = 1.9 + (i * 0.52)
    const bg = i % 2 === 0 ? C.gray50 : C.white
    s.addShape('rect', { x: 0.5, y: yPos, w: 9, h: 0.48, fill: { color: bg } })
    s.addText(m[0], { x: 0.6, y: yPos + 0.05, w: 4.3, h: 0.38, fontSize: 10.5, color: C.red, fontFace: 'Arial' })
    s.addText(m[1], { x: 5, y: yPos + 0.05, w: 4.3, h: 0.38, fontSize: 10.5, color: C.emerald, fontFace: 'Arial' })
  })
  addFooter(s)

  // ---- SLIDE 4: Dashboard Consultor ----
  s = pptx.addSlide()
  addHeader(s, 'Dashboard do Consultor', 'Visão geral do seu negócio')
  addBg(s)
  statCard(s, 0.5, 1.5, '156', 'Clientes ativos', C.indigo)
  statCard(s, 2.8, 1.5, 'R$ 45M', 'Capital sob gestão', C.emerald)
  statCard(s, 5.1, 1.5, '+12.5%', 'Rentabilidade média', C.blue)
  statCard(s, 7.4, 1.5, '23', 'Recomendações abertas', C.amber)
  card(s, 0.5, 2.9, 4.3, 2.2)
  s.addText('Evolução do Capital', { x: 0.7, y: 3, w: 3, h: 0.3, fontSize: 12, bold: true, color: C.gray900, fontFace: 'Arial' })
  s.addText('📈 Gráfico de linha mostrando evolução mensal\ndo capital total sob gestão com comparativo\nde benchmark (CDI, IBOV)', { x: 0.7, y: 3.4, w: 3.8, h: 1.5, fontSize: 10, color: C.gray500, fontFace: 'Arial' })
  card(s, 5.1, 2.9, 4.4, 2.2)
  s.addText('Distribuição por Tipo', { x: 5.3, y: 3, w: 3, h: 0.3, fontSize: 12, bold: true, color: C.gray900, fontFace: 'Arial' })
  s.addText('🥧 Gráfico de pizza com a distribuição\nde ativos: Ações, FIIs, Renda Fixa,\nCriptomoedas, Internacional', { x: 5.3, y: 3.4, w: 3.8, h: 1.5, fontSize: 10, color: C.gray500, fontFace: 'Arial' })
  addFooter(s)

  // ---- SLIDE 5: Kanban Recomendações ----
  s = pptx.addSlide()
  addHeader(s, 'Kanban de Recomendações', 'Gerencie sugestões de compra/venda')
  addBg(s)
  const cols = [
    { title: 'Pendente', color: C.amber, items: ['PETR4 — Comprar\nPreço: R$ 38,50', 'VALE3 — Comprar\nPreço: R$ 62,30'] },
    { title: 'Em Análise', color: C.blue, items: ['ITUB4 — Vender\nPreço: R$ 28,90'] },
    { title: 'Aceita', color: C.emerald, items: ['WEGE3 — Comprar\nPreço: R$ 35,20', 'ABEV3 — Comprar\nPreço: R$ 12,80'] },
    { title: 'Recusada', color: C.red, items: ['MGLU3 — Vender\nPreço: R$ 8,50'] }
  ]
  cols.forEach((col, i) => {
    const xPos = 0.3 + (i * 2.4)
    s.addShape('roundRect', { x: xPos, y: 1.45, w: 2.2, h: 0.4, rectRadius: 0.05, fill: { color: col.color } })
    s.addText(col.title, { x: xPos, y: 1.47, w: 2.2, h: 0.35, fontSize: 11, bold: true, color: C.white, fontFace: 'Arial', align: 'center' })
    col.items.forEach((item, j) => {
      card(s, xPos, 2 + (j * 1.15), 2.2, 1)
      s.addText(item, { x: xPos + 0.1, y: 2.05 + (j * 1.15), w: 2, h: 0.85, fontSize: 9.5, color: C.gray700, fontFace: 'Arial' })
    })
  })
  addFooter(s)

  // ---- SLIDE 6: Copy Trading ----
  s = pptx.addSlide()
  addHeader(s, 'Copy Trading', 'Replique operações automaticamente')
  addBg(s)
  card(s, 0.5, 1.5, 9, 1.5)
  s.addText('Como funciona:', { x: 0.7, y: 1.6, w: 8, h: 0.35, fontSize: 14, bold: true, color: C.gray900, fontFace: 'Arial' })
  s.addText([
    { text: '1. ', options: { bold: true, color: C.indigo } }, { text: 'Crie uma carteira modelo com os ativos desejados\n' },
    { text: '2. ', options: { bold: true, color: C.indigo } }, { text: 'Vincule os clientes que devem seguir essa carteira\n' },
    { text: '3. ', options: { bold: true, color: C.indigo } }, { text: 'Ao alterar a carteira modelo, todos os clientes vinculados recebem a recomendação automaticamente' }
  ], { x: 0.7, y: 2, w: 8.5, h: 0.9, fontSize: 11, color: C.gray700, fontFace: 'Arial' })
  card(s, 0.5, 3.3, 4.3, 1.8)
  s.addText('Vantagens para o Consultor', { x: 0.7, y: 3.4, w: 3.5, h: 0.35, fontSize: 13, bold: true, color: C.indigo, fontFace: 'Arial' })
  s.addText('• Economia de tempo (horas → segundos)\n• Padronização das recomendações\n• Escalabilidade para mais clientes\n• Histórico completo de operações', { x: 0.7, y: 3.8, w: 3.8, h: 1.1, fontSize: 10.5, color: C.gray700, fontFace: 'Arial' })
  card(s, 5.1, 3.3, 4.4, 1.8)
  s.addText('Vantagens para o Cliente', { x: 5.3, y: 3.4, w: 3.5, h: 0.35, fontSize: 13, bold: true, color: C.emerald, fontFace: 'Arial' })
  s.addText('• Recebe recomendações instantaneamente\n• Pode aceitar ou recusar cada operação\n• Transparência total das decisões\n• Acompanha performance em tempo real', { x: 5.3, y: 3.8, w: 3.8, h: 1.1, fontSize: 10.5, color: C.gray700, fontFace: 'Arial' })
  addFooter(s)

  // ---- SLIDE 7: Rebalanceamento ----
  s = pptx.addSlide()
  addHeader(s, 'Rebalanceamento Automático', 'Mantenha carteiras alinhadas à estratégia')
  addBg(s)
  card(s, 0.5, 1.5, 9, 2)
  s.addText('O sistema calcula automaticamente os ajustes necessários para manter a alocação-alvo da carteira.', { x: 0.7, y: 1.6, w: 8.5, h: 0.4, fontSize: 12, color: C.gray700, fontFace: 'Arial' })
  // Table
  const rebData = [
    ['PETR4', '25%', '30%', '-5%', 'Vender R$ 5.000'],
    ['VALE3', '25%', '20%', '+5%', 'Comprar R$ 5.000'],
    ['ITUB4', '25%', '25%', '0%', 'Manter'],
    ['WEGE3', '25%', '25%', '0%', 'Manter']
  ]
  s.addShape('rect', { x: 0.7, y: 2.15, w: 8.5, h: 0.35, fill: { color: C.indigo } })
  s.addText('Ativo          Alvo     Atual     Diff        Ação', { x: 0.8, y: 2.17, w: 8.3, h: 0.3, fontSize: 10, bold: true, color: C.white, fontFace: 'Courier New' })
  rebData.forEach((r, i) => {
    const yPos = 2.55 + (i * 0.35)
    s.addText(`${r[0]}          ${r[1]}       ${r[2]}       ${r[3]}        ${r[4]}`, { x: 0.8, y: yPos, w: 8.3, h: 0.3, fontSize: 10, color: C.gray700, fontFace: 'Courier New' })
  })
  addFooter(s)

  // ---- SLIDE 8: Portfolio Cliente ----
  s = pptx.addSlide()
  addHeader(s, 'Portfolio do Cliente', 'Acompanhamento detalhado')
  addBg(s)
  statCard(s, 0.5, 1.5, 'R$ 285K', 'Patrimônio total', C.indigo)
  statCard(s, 2.8, 1.5, '+18.3%', 'Rentabilidade', C.emerald)
  statCard(s, 5.1, 1.5, '12', 'Ativos', C.blue)
  statCard(s, 7.4, 1.5, '3', 'Carteiras', C.amber)
  card(s, 0.5, 2.9, 9, 2.2)
  s.addText('Composição da Carteira', { x: 0.7, y: 3, w: 3, h: 0.3, fontSize: 12, bold: true, color: C.gray900, fontFace: 'Arial' })
  const assets = [
    ['PETR4', 'Ações', 'R$ 45.000', '+12.5%'],
    ['VALE3', 'Ações', 'R$ 38.500', '+8.2%'],
    ['XPML11', 'FIIs', 'R$ 52.000', '+15.1%'],
    ['Tesouro IPCA+', 'Renda Fixa', 'R$ 80.000', '+6.8%'],
    ['BTC', 'Cripto', 'R$ 25.000', '+45.2%']
  ]
  s.addShape('rect', { x: 0.7, y: 3.4, w: 8.5, h: 0.3, fill: { color: C.gray100 } })
  s.addText('Ativo                Tipo              Valor            Rent.', { x: 0.8, y: 3.4, w: 8, h: 0.3, fontSize: 9.5, bold: true, color: C.gray500, fontFace: 'Courier New' })
  assets.forEach((a, i) => {
    s.addText(`${a[0].padEnd(20)}${a[1].padEnd(18)}${a[2].padEnd(17)}${a[3]}`, { x: 0.8, y: 3.75 + (i * 0.28), w: 8, h: 0.25, fontSize: 9.5, color: C.gray700, fontFace: 'Courier New' })
  })
  addFooter(s)

  // ---- SLIDE 9: Performance ----
  s = pptx.addSlide()
  addHeader(s, 'Performance e Relatórios', 'Análise detalhada de resultados')
  addBg(s)
  card(s, 0.5, 1.5, 4.3, 2.5)
  s.addText('Rentabilidade Mensal', { x: 0.7, y: 1.6, w: 3, h: 0.3, fontSize: 12, bold: true, color: C.gray900, fontFace: 'Arial' })
  s.addText('📊 Gráfico de barras comparando\nrentabilidade mensal da carteira\nvs CDI e IBOV\n\nMédia 12 meses: +1.8% a.m.', { x: 0.7, y: 2, w: 3.8, h: 1.8, fontSize: 10, color: C.gray500, fontFace: 'Arial' })
  card(s, 5.1, 1.5, 4.4, 2.5)
  s.addText('Rentabilidade Acumulada', { x: 5.3, y: 1.6, w: 3, h: 0.3, fontSize: 12, bold: true, color: C.gray900, fontFace: 'Arial' })
  s.addText('📈 Gráfico de linha com evolução\nacumulada nos últimos 12 meses\n\nCarteira: +22.5%\nCDI: +12.8%\nIBOV: +15.2%', { x: 5.3, y: 2, w: 3.8, h: 1.8, fontSize: 10, color: C.gray500, fontFace: 'Arial' })
  card(s, 0.5, 4.2, 9, 1)
  s.addText('Exportação: ', { x: 0.7, y: 4.3, w: 1.5, h: 0.3, fontSize: 11, bold: true, color: C.gray900, fontFace: 'Arial' })
  s.addText('Relatórios em PDF com análise completa, histórico de operações, comparativo com benchmarks e projeções futuras. Envio automático por email.', { x: 2.2, y: 4.3, w: 7, h: 0.7, fontSize: 10.5, color: C.gray700, fontFace: 'Arial' })
  addFooter(s)

  // ---- SLIDE 10: Metas ----
  s = pptx.addSlide()
  addHeader(s, 'Metas Financeiras', 'Defina e acompanhe objetivos')
  addBg(s)
  const metas = [
    { nome: 'Aposentadoria', atual: 'R$ 285K', meta: 'R$ 1M', pct: 28.5, cor: C.indigo },
    { nome: 'Casa Própria', atual: 'R$ 120K', meta: 'R$ 500K', pct: 24, cor: C.emerald },
    { nome: 'Reserva de Emergência', atual: 'R$ 45K', meta: 'R$ 50K', pct: 90, cor: C.teal },
    { nome: 'Viagem Europa', atual: 'R$ 8K', meta: 'R$ 25K', pct: 32, cor: C.amber }
  ]
  metas.forEach((m, i) => {
    const yPos = 1.5 + (i * 0.95)
    card(s, 0.5, yPos, 9, 0.85)
    s.addText(m.nome, { x: 0.7, y: yPos + 0.08, w: 3, h: 0.25, fontSize: 12, bold: true, color: C.gray900, fontFace: 'Arial' })
    s.addText(`${m.atual} / ${m.meta}`, { x: 0.7, y: yPos + 0.35, w: 3, h: 0.2, fontSize: 10, color: C.gray500, fontFace: 'Arial' })
    // Progress bar background
    s.addShape('roundRect', { x: 4, y: yPos + 0.3, w: 4.5, h: 0.25, rectRadius: 0.05, fill: { color: C.gray200 } })
    // Progress bar fill
    s.addShape('roundRect', { x: 4, y: yPos + 0.3, w: 4.5 * (m.pct / 100), h: 0.25, rectRadius: 0.05, fill: { color: m.cor } })
    s.addText(`${m.pct}%`, { x: 8.6, y: yPos + 0.08, w: 0.7, h: 0.25, fontSize: 11, bold: true, color: m.cor, fontFace: 'Arial', align: 'right' })
  })
  addFooter(s)

  // ---- SLIDE 11: HeatMap ----
  s = pptx.addSlide()
  addHeader(s, 'Heat Map de Cotações', 'Visualize o mercado em tempo real')
  addBg(s)
  card(s, 0.5, 1.5, 9, 3.6)
  const heatItems = [
    { t: 'PETR4\n+2.5%', c: C.emerald, x: 0.7, w: 2.8, h: 1.4 },
    { t: 'VALE3\n-1.2%', c: C.red, x: 3.6, w: 2.2, h: 1.4 },
    { t: 'ITUB4\n+0.8%', c: '34D399', x: 5.9, w: 1.8, h: 1.4 },
    { t: 'WEGE3\n+3.1%', c: C.emerald, x: 7.8, w: 1.5, h: 1.4 },
    { t: 'MGLU3\n-4.5%', c: C.red, x: 0.7, w: 2, h: 1 },
    { t: 'ABEV3\n+1.1%', c: '34D399', x: 2.8, w: 2, h: 1 },
    { t: 'BBDC4\n-0.3%', c: 'FCA5A5', x: 4.9, w: 1.8, h: 1 },
    { t: 'RENT3\n+2.8%', c: C.emerald, x: 6.8, w: 2.5, h: 1 }
  ]
  heatItems.forEach(item => {
    s.addShape('roundRect', { x: item.x, y: item.h === 1.4 ? 1.7 : 3.2, w: item.w, h: item.h, rectRadius: 0.08, fill: { color: item.c } })
    s.addText(item.t, { x: item.x, y: item.h === 1.4 ? 1.9 : 3.35, w: item.w, h: item.h * 0.7, fontSize: 12, bold: true, color: C.white, fontFace: 'Arial', align: 'center', valign: 'middle' })
  })
  addFooter(s)

  // ---- SLIDE 12: Alertas ----
  s = pptx.addSlide()
  addHeader(s, 'Alertas de Preço', 'Notificações inteligentes')
  addBg(s)
  card(s, 0.5, 1.5, 9, 1.2)
  s.addText('Configure alertas personalizados para acompanhar seus ativos:', { x: 0.7, y: 1.6, w: 8.5, h: 0.3, fontSize: 12, color: C.gray700, fontFace: 'Arial' })
  s.addText('• Alerta quando ativo atingir preço-alvo (acima ou abaixo)\n• Alerta de variação percentual no dia\n• Notificação por email, push e no sistema', { x: 0.7, y: 2, w: 8.5, h: 0.6, fontSize: 11, color: C.gray500, fontFace: 'Arial' })
  const alertExamples = [
    { ativo: 'PETR4', tipo: 'Preço acima de R$ 40,00', status: 'Ativo', cor: C.emerald },
    { ativo: 'VALE3', tipo: 'Preço abaixo de R$ 58,00', status: 'Disparado', cor: C.amber },
    { ativo: 'ITUB4', tipo: 'Variação > 5% no dia', status: 'Ativo', cor: C.emerald },
    { ativo: 'BTC', tipo: 'Preço acima de $70.000', status: 'Ativo', cor: C.emerald }
  ]
  alertExamples.forEach((a, i) => {
    const yPos = 3 + (i * 0.6)
    card(s, 0.5, yPos, 9, 0.5)
    s.addText(a.ativo, { x: 0.7, y: yPos + 0.05, w: 1.5, h: 0.35, fontSize: 11, bold: true, color: C.gray900, fontFace: 'Arial' })
    s.addText(a.tipo, { x: 2.3, y: yPos + 0.05, w: 4.5, h: 0.35, fontSize: 10.5, color: C.gray700, fontFace: 'Arial' })
    s.addShape('roundRect', { x: 7.5, y: yPos + 0.1, w: 1.2, h: 0.3, rectRadius: 0.05, fill: { color: a.cor } })
    s.addText(a.status, { x: 7.5, y: yPos + 0.1, w: 1.2, h: 0.3, fontSize: 9, bold: true, color: C.white, fontFace: 'Arial', align: 'center' })
  })
  addFooter(s)

  // ---- SLIDE 13: Segurança ----
  s = pptx.addSlide()
  addHeader(s, 'Segurança', 'Proteção em todos os níveis')
  addBg(s)
  const secItems = [
    ['Autenticação OTP', 'Código de verificação enviado por email para cada login', C.indigo],
    ['Criptografia', 'Dados sensíveis criptografados em trânsito e em repouso', C.emerald],
    ['Controle de Acesso', 'Perfis separados: Admin, Consultor e Cliente com permissões específicas', C.blue],
    ['Auditoria', 'Registro completo de todas as ações na plataforma', C.amber]
  ]
  secItems.forEach((item, i) => {
    const yPos = 1.5 + (i * 1)
    card(s, 0.5, yPos, 9, 0.9)
    s.addShape('roundRect', { x: 0.7, y: yPos + 0.15, w: 0.5, h: 0.5, rectRadius: 0.08, fill: { color: item[2] } })
    s.addText(item[0], { x: 1.4, y: yPos + 0.1, w: 7.5, h: 0.35, fontSize: 13, bold: true, color: C.gray900, fontFace: 'Arial' })
    s.addText(item[1], { x: 1.4, y: yPos + 0.45, w: 7.5, h: 0.3, fontSize: 10.5, color: C.gray500, fontFace: 'Arial' })
  })
  addFooter(s)

  // ---- SLIDE 14: Ferramentas Extra ----
  s = pptx.addSlide()
  addHeader(s, 'Ferramentas Extras', 'Simulador, Watchlist, Comparador')
  addBg(s)
  card(s, 0.5, 1.5, 2.8, 2.5)
  s.addText('Simulador', { x: 0.7, y: 1.6, w: 2.4, h: 0.35, fontSize: 14, bold: true, color: C.indigo, fontFace: 'Arial' })
  s.addText('Projete cenários de\ninvestimento com diferentes\naportes, prazos e\nrentabilidades esperadas', { x: 0.7, y: 2, w: 2.4, h: 1.5, fontSize: 10.5, color: C.gray700, fontFace: 'Arial' })
  card(s, 3.6, 1.5, 2.8, 2.5)
  s.addText('Watchlist', { x: 3.8, y: 1.6, w: 2.4, h: 0.35, fontSize: 14, bold: true, color: C.amber, fontFace: 'Arial' })
  s.addText('Acompanhe ativos de\ninteresse com cotações\nem tempo real sem\nprecisar comprar', { x: 3.8, y: 2, w: 2.4, h: 1.5, fontSize: 10.5, color: C.gray700, fontFace: 'Arial' })
  card(s, 6.7, 1.5, 2.8, 2.5)
  s.addText('Comparador', { x: 6.9, y: 1.6, w: 2.4, h: 0.35, fontSize: 14, bold: true, color: C.emerald, fontFace: 'Arial' })
  s.addText('Compare moedas e ativos\nside-by-side com gráficos\ninterativos e dados\nhistóricos', { x: 6.9, y: 2, w: 2.4, h: 1.5, fontSize: 10.5, color: C.gray700, fontFace: 'Arial' })
  addFooter(s)

  // ---- SLIDE 15: Encerramento ----
  s = pptx.addSlide()
  addBg(s, C.navy)
  s.addText('◈', { x: 4.2, y: 1.5, w: 1.5, h: 1.2, fontSize: 60, fontFace: 'Arial', color: C.indigo, align: 'center' })
  s.addText('Obrigado!', { x: 1, y: 2.7, w: 8, h: 0.7, fontSize: 36, fontFace: 'Arial', color: C.white, bold: true, align: 'center' })
  s.addText('TradeLink — Transformando a gestão de investimentos', { x: 1, y: 3.4, w: 8, h: 0.4, fontSize: 14, fontFace: 'Arial', color: C.indigoLight, align: 'center' })
  s.addText('Dúvidas? Entre em contato pelo chat de suporte na plataforma.', { x: 1, y: 4.1, w: 8, h: 0.35, fontSize: 11, fontFace: 'Arial', color: C.gray500, align: 'center' })

  const fileName = 'TradeLink-Guia-Completo.pptx'
  await pptx.writeFile({ fileName })
  console.log(`✅ PPTX criado: ${fileName}`)
  return fileName
}

// ========== GENERATE HTML ==========
function createHTML() {
  const html = `<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TradeLink — Guia Completo</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
:root{--navy:#1e1b4b;--navy-light:#312e81;--indigo:#6366f1;--indigo-light:#e0e7ff;--white:#fff;--gray-50:#f9fafb;--gray-100:#f3f4f6;--gray-200:#e5e7eb;--gray-500:#6b7280;--gray-700:#374151;--gray-900:#111827;--emerald:#10b981;--amber:#f59e0b;--blue:#3b82f6;--red:#ef4444;--teal:#14b8a6}
body{font-family:'Inter',system-ui,-apple-system,sans-serif;background:var(--gray-50);color:var(--gray-900);line-height:1.6}
.hero{background:linear-gradient(135deg,var(--navy) 0%,var(--navy-light) 100%);color:var(--white);padding:4rem 2rem;text-align:center;position:relative;overflow:hidden}
.hero::before{content:'';position:absolute;inset:0;background:radial-gradient(circle at 30% 50%,rgba(99,102,241,0.15) 0%,transparent 50%)}
.hero h1{font-size:clamp(2rem,5vw,3.5rem);font-weight:800;letter-spacing:-0.03em;margin-bottom:.5rem}
.hero p{font-size:clamp(1rem,2vw,1.25rem);color:#a5b4fc;max-width:600px;margin:0 auto}
nav.sticky-nav{position:sticky;top:0;z-index:50;background:var(--white);border-bottom:1px solid var(--gray-200);box-shadow:0 1px 3px rgba(0,0,0,0.08);padding:0 1rem;overflow-x:auto}
nav.sticky-nav ul{display:flex;gap:.5rem;list-style:none;max-width:1100px;margin:0 auto;padding:.5rem 0}
nav.sticky-nav a{display:block;padding:.5rem .75rem;font-size:.8125rem;font-weight:500;color:var(--gray-500);text-decoration:none;border-radius:6px;white-space:nowrap;transition:all .2s}
nav.sticky-nav a:hover{background:var(--indigo-light);color:var(--indigo)}
.container{max-width:1100px;margin:0 auto;padding:2rem 1rem}
section{margin-bottom:3rem}
.section-title{font-size:1.75rem;font-weight:700;color:var(--gray-900);margin-bottom:.5rem;letter-spacing:-0.02em}
.section-subtitle{font-size:1rem;color:var(--gray-500);margin-bottom:1.5rem}
.card{background:var(--white);border-radius:12px;padding:1.5rem;box-shadow:0 1px 3px rgba(0,0,0,0.08);border:1px solid var(--gray-200)}
.stats-row{display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:1rem;margin-bottom:2rem}
.stat-card{background:var(--white);border-radius:12px;padding:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,0.08);border:1px solid var(--gray-200);text-align:center}
.stat-card .value{font-size:1.75rem;font-weight:800}
.stat-card .label{font-size:.75rem;color:var(--gray-500);text-transform:uppercase;letter-spacing:.05em;margin-top:.25rem}
.browser-frame{background:var(--white);border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.12);border:1px solid var(--gray-200);margin:1.5rem 0}
.browser-bar{background:var(--gray-100);padding:.5rem 1rem;display:flex;align-items:center;gap:.75rem;border-bottom:1px solid var(--gray-200)}
.browser-dots{display:flex;gap:6px}
.browser-dots span{width:10px;height:10px;border-radius:50%}
.browser-url{flex:1;background:var(--white);border-radius:4px;padding:4px 10px;font-size:.75rem;color:var(--gray-500);border:1px solid var(--gray-200)}
.browser-content{padding:1rem;background:var(--gray-50);min-height:200px}
.mapping-table{width:100%;border-collapse:collapse;border-radius:8px;overflow:hidden}
.mapping-table th{background:var(--navy);color:var(--white);padding:.75rem 1rem;text-align:left;font-size:.875rem}
.mapping-table td{padding:.625rem 1rem;font-size:.8125rem;border-bottom:1px solid var(--gray-200)}
.mapping-table tr:nth-child(even){background:var(--gray-50)}
.mapping-table .old{color:var(--red);font-weight:500}
.mapping-table .new{color:var(--emerald);font-weight:500}
.feature-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:1rem}
.feature-card{background:var(--white);border-radius:12px;padding:1.5rem;box-shadow:0 1px 3px rgba(0,0,0,0.08);border:1px solid var(--gray-200);transition:transform .2s,box-shadow .2s}
.feature-card:hover{transform:translateY(-2px);box-shadow:0 4px 16px rgba(0,0,0,0.12)}
.feature-card h3{font-size:1rem;font-weight:600;margin-bottom:.5rem}
.feature-card p{font-size:.8125rem;color:var(--gray-500);line-height:1.5}
.badge{display:inline-block;padding:.25rem .75rem;border-radius:9999px;font-size:.75rem;font-weight:600}
.badge-blue{background:#dbeafe;color:#1d4ed8}
.badge-green{background:#d1fae5;color:#065f46}
.badge-amber{background:#fef3c7;color:#92400e}
.badge-red{background:#fee2e2;color:#991b1b}
.kanban{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:.75rem}
.kanban-col{background:var(--gray-50);border-radius:8px;padding:.75rem}
.kanban-header{padding:.5rem;border-radius:6px;color:var(--white);text-align:center;font-size:.8125rem;font-weight:600;margin-bottom:.5rem}
.kanban-card{background:var(--white);border-radius:6px;padding:.625rem;margin-bottom:.5rem;box-shadow:0 1px 2px rgba(0,0,0,0.06);font-size:.8125rem;border:1px solid var(--gray-200)}
.heatmap-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:.5rem}
.heat-item{border-radius:8px;padding:1rem;text-align:center;color:var(--white);font-weight:700;font-size:.875rem}
.heat-green{background:var(--emerald)}
.heat-red{background:var(--red)}
.heat-light-green{background:#34d399}
.heat-light-red{background:#fca5a5;color:var(--gray-700)}
.progress-bar{width:100%;height:8px;background:var(--gray-200);border-radius:4px;overflow:hidden;margin:.5rem 0}
.progress-fill{height:100%;border-radius:4px;transition:width .5s ease}
.security-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:1rem}
.security-card{display:flex;gap:.75rem;padding:1rem;background:var(--white);border-radius:10px;border:1px solid var(--gray-200);box-shadow:0 1px 3px rgba(0,0,0,0.06)}
.security-icon{width:40px;height:40px;border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:1.25rem;flex-shrink:0}
footer{background:var(--navy);color:#a5b4fc;padding:2rem;text-align:center;font-size:.875rem}
@media(max-width:640px){.hero{padding:2.5rem 1rem}.container{padding:1.5rem .75rem}.heatmap-grid{grid-template-columns:repeat(2,1fr)}.kanban{grid-template-columns:1fr 1fr}}
</style>
</head>
<body>

<div class="hero">
  <p style="font-size:3rem;margin-bottom:.5rem">◈</p>
  <h1>TradeLink</h1>
  <p>Plataforma Completa de Gestão de Investimentos</p>
</div>

<nav class="sticky-nav">
  <ul>
    <li><a href="#overview">Visão Geral</a></li>
    <li><a href="#mapping">Planilha → Sistema</a></li>
    <li><a href="#dashboard">Dashboard</a></li>
    <li><a href="#kanban">Kanban</a></li>
    <li><a href="#copy">Copy Trading</a></li>
    <li><a href="#portfolio">Portfolio</a></li>
    <li><a href="#performance">Performance</a></li>
    <li><a href="#metas">Metas</a></li>
    <li><a href="#heatmap">Heat Map</a></li>
    <li><a href="#alertas">Alertas</a></li>
    <li><a href="#seguranca">Segurança</a></li>
  </ul>
</nav>

<div class="container">

  <!-- Visão Geral -->
  <section id="overview">
    <h2 class="section-title">Visão Geral</h2>
    <p class="section-subtitle">Tudo o que você precisa para gerenciar investimentos de forma profissional</p>
    <div class="feature-grid">
      <div class="feature-card"><h3>📊 Gestão Centralizada</h3><p>Gerencie todos os clientes, carteiras e recomendações em uma plataforma única e intuitiva.</p></div>
      <div class="feature-card"><h3>⚡ Automatização</h3><p>Copy Trading, Rebalanceamento automático e alertas inteligentes que economizam horas do seu dia.</p></div>
      <div class="feature-card"><h3>🔍 Transparência</h3><p>Clientes acompanham performance, metas e recomendações em tempo real, fortalecendo a confiança.</p></div>
      <div class="feature-card"><h3>🔒 Segurança</h3><p>Autenticação OTP, criptografia ponta a ponta e controle granular de acesso por perfil.</p></div>
    </div>
  </section>

  <!-- Planilha vs Sistema -->
  <section id="mapping">
    <h2 class="section-title">Planilha → Sistema</h2>
    <p class="section-subtitle">Veja como suas rotinas da planilha funcionam agora no TradeLink</p>
    <div class="browser-frame">
      <div class="browser-bar">
        <div class="browser-dots"><span style="background:#ff5f57"></span><span style="background:#febc2e"></span><span style="background:#28c840"></span></div>
        <div class="browser-url">tradelink.app/migração</div>
      </div>
      <div class="browser-content" style="padding:0">
        <table class="mapping-table">
          <thead><tr><th style="width:50%">❌ Antes (Planilha)</th><th>✅ Agora (TradeLink)</th></tr></thead>
          <tbody>
            <tr><td class="old">Cadastro de clientes no Excel</td><td class="new">Tela de Clientes com cadastro automático</td></tr>
            <tr><td class="old">Carteiras em planilha separada</td><td class="new">Módulo Carteiras com ativos em tempo real</td></tr>
            <tr><td class="old">Recomendações via WhatsApp</td><td class="new">Kanban de Recomendações + notificações</td></tr>
            <tr><td class="old">Controle de performance manual</td><td class="new">Dashboard com gráficos automáticos</td></tr>
            <tr><td class="old">Metas anotadas no papel</td><td class="new">Módulo Metas com progresso visual</td></tr>
            <tr><td class="old">Alertas via e-mail manual</td><td class="new">Alertas de Preço automáticos no sistema</td></tr>
            <tr><td class="old">Relatórios montados à mão</td><td class="new">Relatórios PDF gerados automaticamente</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>

  <!-- Dashboard -->
  <section id="dashboard">
    <h2 class="section-title">Dashboard</h2>
    <p class="section-subtitle">Visão geral completa em uma única tela</p>
    <div class="browser-frame">
      <div class="browser-bar">
        <div class="browser-dots"><span style="background:#ff5f57"></span><span style="background:#febc2e"></span><span style="background:#28c840"></span></div>
        <div class="browser-url">tradelink.app/dashboard</div>
      </div>
      <div class="browser-content">
        <div class="stats-row">
          <div class="stat-card"><div class="value" style="color:var(--indigo)">156</div><div class="label">Clientes ativos</div></div>
          <div class="stat-card"><div class="value" style="color:var(--emerald)">R$ 45M</div><div class="label">Capital sob gestão</div></div>
          <div class="stat-card"><div class="value" style="color:var(--blue)">+12.5%</div><div class="label">Rentabilidade média</div></div>
          <div class="stat-card"><div class="value" style="color:var(--amber)">23</div><div class="label">Recomendações</div></div>
        </div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem">
          <div class="card"><h3 style="font-size:.875rem;font-weight:600;margin-bottom:.75rem">Evolução do Capital</h3>
            <svg viewBox="0 0 400 120" style="width:100%"><polyline fill="none" stroke="var(--indigo)" stroke-width="2.5" points="10,100 60,85 120,70 180,55 240,40 300,35 360,20 390,15"/><polyline fill="none" stroke="var(--gray-200)" stroke-width="1.5" stroke-dasharray="4" points="10,95 60,88 120,80 180,72 240,64 300,56 360,48 390,44"/></svg>
          </div>
          <div class="card"><h3 style="font-size:.875rem;font-weight:600;margin-bottom:.75rem">Distribuição</h3>
            <div style="width:120px;height:120px;margin:0 auto;border-radius:50%;background:conic-gradient(var(--indigo) 0% 30%,var(--emerald) 30% 50%,var(--amber) 50% 70%,var(--blue) 70% 85%,var(--teal) 85% 100%)"></div>
            <div style="display:flex;flex-wrap:wrap;gap:.5rem;margin-top:.75rem;justify-content:center;font-size:.75rem">
              <span><span style="color:var(--indigo)">●</span> Ações 30%</span>
              <span><span style="color:var(--emerald)">●</span> FIIs 20%</span>
              <span><span style="color:var(--amber)">●</span> RF 20%</span>
              <span><span style="color:var(--blue)">●</span> Cripto 15%</span>
              <span><span style="color:var(--teal)">●</span> Intl 15%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Kanban -->
  <section id="kanban">
    <h2 class="section-title">Kanban de Recomendações</h2>
    <p class="section-subtitle">Gerencie sugestões de compra e venda visualmente</p>
    <div class="browser-frame">
      <div class="browser-bar">
        <div class="browser-dots"><span style="background:#ff5f57"></span><span style="background:#febc2e"></span><span style="background:#28c840"></span></div>
        <div class="browser-url">tradelink.app/kanban</div>
      </div>
      <div class="browser-content">
        <div class="kanban">
          <div class="kanban-col">
            <div class="kanban-header" style="background:var(--amber)">Pendente</div>
            <div class="kanban-card"><strong>PETR4</strong> — Comprar<br><small style="color:var(--gray-500)">R$ 38,50 • Ações</small></div>
            <div class="kanban-card"><strong>VALE3</strong> — Comprar<br><small style="color:var(--gray-500)">R$ 62,30 • Ações</small></div>
          </div>
          <div class="kanban-col">
            <div class="kanban-header" style="background:var(--blue)">Em Análise</div>
            <div class="kanban-card"><strong>ITUB4</strong> — Vender<br><small style="color:var(--gray-500)">R$ 28,90 • Ações</small></div>
          </div>
          <div class="kanban-col">
            <div class="kanban-header" style="background:var(--emerald)">Aceita</div>
            <div class="kanban-card"><strong>WEGE3</strong> — Comprar<br><small style="color:var(--gray-500)">R$ 35,20 • Ações</small></div>
            <div class="kanban-card"><strong>ABEV3</strong> — Comprar<br><small style="color:var(--gray-500)">R$ 12,80 • Ações</small></div>
          </div>
          <div class="kanban-col">
            <div class="kanban-header" style="background:var(--red)">Recusada</div>
            <div class="kanban-card"><strong>MGLU3</strong> — Vender<br><small style="color:var(--gray-500)">R$ 8,50 • Ações</small></div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Copy Trading -->
  <section id="copy">
    <h2 class="section-title">Copy Trading</h2>
    <p class="section-subtitle">Replique operações automaticamente para múltiplos clientes</p>
    <div class="feature-grid" style="grid-template-columns:repeat(3,1fr)">
      <div class="feature-card" style="text-align:center;border-top:3px solid var(--indigo)">
        <div style="font-size:2rem;margin-bottom:.5rem">1️⃣</div>
        <h3>Crie a Carteira Modelo</h3>
        <p>Monte a carteira com os ativos e alocações ideais</p>
      </div>
      <div class="feature-card" style="text-align:center;border-top:3px solid var(--emerald)">
        <div style="font-size:2rem;margin-bottom:.5rem">2️⃣</div>
        <h3>Vincule os Clientes</h3>
        <p>Selecione quais clientes seguirão esta carteira</p>
      </div>
      <div class="feature-card" style="text-align:center;border-top:3px solid var(--amber)">
        <div style="font-size:2rem;margin-bottom:.5rem">3️⃣</div>
        <h3>Automatize</h3>
        <p>Alterações são replicadas automaticamente para todos</p>
      </div>
    </div>
  </section>

  <!-- Portfolio -->
  <section id="portfolio">
    <h2 class="section-title">Portfolio do Cliente</h2>
    <p class="section-subtitle">Acompanhamento completo do patrimônio</p>
    <div class="browser-frame">
      <div class="browser-bar">
        <div class="browser-dots"><span style="background:#ff5f57"></span><span style="background:#febc2e"></span><span style="background:#28c840"></span></div>
        <div class="browser-url">tradelink.app/portfolio</div>
      </div>
      <div class="browser-content">
        <div class="stats-row">
          <div class="stat-card"><div class="value" style="color:var(--indigo)">R$ 285K</div><div class="label">Patrimônio</div></div>
          <div class="stat-card"><div class="value" style="color:var(--emerald)">+18.3%</div><div class="label">Rentabilidade</div></div>
          <div class="stat-card"><div class="value" style="color:var(--blue)">12</div><div class="label">Ativos</div></div>
          <div class="stat-card"><div class="value" style="color:var(--amber)">3</div><div class="label">Carteiras</div></div>
        </div>
        <table class="mapping-table">
          <thead><tr><th>Ativo</th><th>Tipo</th><th>Valor</th><th>Rent.</th></tr></thead>
          <tbody>
            <tr><td><strong>PETR4</strong></td><td>Ações</td><td>R$ 45.000</td><td style="color:var(--emerald);font-weight:600">+12.5%</td></tr>
            <tr><td><strong>XPML11</strong></td><td>FIIs</td><td>R$ 52.000</td><td style="color:var(--emerald);font-weight:600">+15.1%</td></tr>
            <tr><td><strong>Tesouro IPCA+</strong></td><td>Renda Fixa</td><td>R$ 80.000</td><td style="color:var(--emerald);font-weight:600">+6.8%</td></tr>
            <tr><td><strong>BTC</strong></td><td>Cripto</td><td>R$ 25.000</td><td style="color:var(--emerald);font-weight:600">+45.2%</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>

  <!-- Performance -->
  <section id="performance">
    <h2 class="section-title">Performance</h2>
    <p class="section-subtitle">Análise detalhada de resultados com comparativos</p>
    <div class="browser-frame">
      <div class="browser-bar">
        <div class="browser-dots"><span style="background:#ff5f57"></span><span style="background:#febc2e"></span><span style="background:#28c840"></span></div>
        <div class="browser-url">tradelink.app/performance</div>
      </div>
      <div class="browser-content">
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem">
          <div class="card">
            <h3 style="font-size:.875rem;font-weight:600;margin-bottom:.75rem">Rentabilidade Acumulada</h3>
            <svg viewBox="0 0 400 140" style="width:100%">
              <polyline fill="none" stroke="var(--indigo)" stroke-width="2.5" points="10,120 50,105 100,95 150,80 200,60 250,55 300,40 350,30 390,20"/>
              <polyline fill="none" stroke="var(--amber)" stroke-width="1.5" stroke-dasharray="5" points="10,118 50,110 100,102 150,94 200,86 250,78 300,70 350,62 390,55"/>
              <polyline fill="none" stroke="var(--gray-200)" stroke-width="1.5" stroke-dasharray="3" points="10,115 50,108 100,100 150,92 200,84 250,76 300,68 350,60 390,52"/>
            </svg>
            <div style="display:flex;gap:1rem;font-size:.75rem;margin-top:.5rem">
              <span><span style="color:var(--indigo)">━</span> Carteira +22.5%</span>
              <span><span style="color:var(--amber)">╌</span> CDI +12.8%</span>
              <span style="color:var(--gray-500)">┈ IBOV +15.2%</span>
            </div>
          </div>
          <div class="card">
            <h3 style="font-size:.875rem;font-weight:600;margin-bottom:.75rem">Rentabilidade Mensal</h3>
            <div style="display:flex;align-items:flex-end;gap:4px;height:100px">
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:60%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:45%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:80%"></div>
              <div style="flex:1;background:var(--red);border-radius:3px 3px 0 0;height:20%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:70%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:55%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:90%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:65%"></div>
              <div style="flex:1;background:var(--red);border-radius:3px 3px 0 0;height:15%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:75%"></div>
              <div style="flex:1;background:var(--indigo);border-radius:3px 3px 0 0;height:85%"></div>
              <div style="flex:1;background:var(--emerald);border-radius:3px 3px 0 0;height:95%"></div>
            </div>
            <div style="display:flex;justify-content:space-between;font-size:.625rem;color:var(--gray-500);margin-top:.25rem"><span>Jan</span><span>Fev</span><span>Mar</span><span>Abr</span><span>Mai</span><span>Jun</span><span>Jul</span><span>Ago</span><span>Set</span><span>Out</span><span>Nov</span><span>Dez</span></div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Metas -->
  <section id="metas">
    <h2 class="section-title">Metas Financeiras</h2>
    <p class="section-subtitle">Defina e acompanhe seus objetivos de forma visual</p>
    <div style="display:grid;gap:1rem">
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <div style="flex:1;min-width:150px"><strong>🏠 Casa Própria</strong><br><small style="color:var(--gray-500)">R$ 120K / R$ 500K</small></div>
        <div style="flex:2;min-width:200px"><div class="progress-bar"><div class="progress-fill" style="width:24%;background:var(--indigo)"></div></div></div>
        <span class="badge badge-blue">24%</span>
      </div>
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <div style="flex:1;min-width:150px"><strong>🏖️ Aposentadoria</strong><br><small style="color:var(--gray-500)">R$ 285K / R$ 1M</small></div>
        <div style="flex:2;min-width:200px"><div class="progress-bar"><div class="progress-fill" style="width:28.5%;background:var(--emerald)"></div></div></div>
        <span class="badge badge-green">28.5%</span>
      </div>
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <div style="flex:1;min-width:150px"><strong>🛡️ Reserva Emergência</strong><br><small style="color:var(--gray-500)">R$ 45K / R$ 50K</small></div>
        <div style="flex:2;min-width:200px"><div class="progress-bar"><div class="progress-fill" style="width:90%;background:var(--teal)"></div></div></div>
        <span class="badge badge-green">90%</span>
      </div>
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <div style="flex:1;min-width:150px"><strong>✈️ Viagem Europa</strong><br><small style="color:var(--gray-500)">R$ 8K / R$ 25K</small></div>
        <div style="flex:2;min-width:200px"><div class="progress-bar"><div class="progress-fill" style="width:32%;background:var(--amber)"></div></div></div>
        <span class="badge badge-amber">32%</span>
      </div>
    </div>
  </section>

  <!-- HeatMap -->
  <section id="heatmap">
    <h2 class="section-title">Heat Map de Cotações</h2>
    <p class="section-subtitle">Visualize o mercado em tempo real</p>
    <div class="browser-frame">
      <div class="browser-bar">
        <div class="browser-dots"><span style="background:#ff5f57"></span><span style="background:#febc2e"></span><span style="background:#28c840"></span></div>
        <div class="browser-url">tradelink.app/heatmap</div>
      </div>
      <div class="browser-content">
        <div class="heatmap-grid">
          <div class="heat-item heat-green" style="grid-column:span 2;padding:1.5rem">PETR4<br>+2.5%</div>
          <div class="heat-item heat-red">VALE3<br>-1.2%</div>
          <div class="heat-item heat-light-green">ITUB4<br>+0.8%</div>
          <div class="heat-item heat-red">MGLU3<br>-4.5%</div>
          <div class="heat-item heat-light-green">ABEV3<br>+1.1%</div>
          <div class="heat-item heat-light-red">BBDC4<br>-0.3%</div>
          <div class="heat-item heat-green">RENT3<br>+2.8%</div>
        </div>
      </div>
    </div>
  </section>

  <!-- Alertas -->
  <section id="alertas">
    <h2 class="section-title">Alertas de Preço</h2>
    <p class="section-subtitle">Configure notificações inteligentes para seus ativos</p>
    <div style="display:grid;gap:.75rem">
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <strong style="min-width:80px">PETR4</strong>
        <span style="flex:1;color:var(--gray-500);font-size:.875rem;min-width:200px">Preço acima de R$ 40,00</span>
        <span class="badge badge-green">Ativo</span>
      </div>
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <strong style="min-width:80px">VALE3</strong>
        <span style="flex:1;color:var(--gray-500);font-size:.875rem;min-width:200px">Preço abaixo de R$ 58,00</span>
        <span class="badge badge-amber">Disparado</span>
      </div>
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <strong style="min-width:80px">ITUB4</strong>
        <span style="flex:1;color:var(--gray-500);font-size:.875rem;min-width:200px">Variação > 5% no dia</span>
        <span class="badge badge-green">Ativo</span>
      </div>
      <div class="card" style="display:flex;align-items:center;gap:1rem;flex-wrap:wrap">
        <strong style="min-width:80px">BTC</strong>
        <span style="flex:1;color:var(--gray-500);font-size:.875rem;min-width:200px">Preço acima de $70.000</span>
        <span class="badge badge-green">Ativo</span>
      </div>
    </div>
  </section>

  <!-- Segurança -->
  <section id="seguranca">
    <h2 class="section-title">Segurança</h2>
    <p class="section-subtitle">Proteção em todos os níveis da plataforma</p>
    <div class="security-grid">
      <div class="security-card">
        <div class="security-icon" style="background:var(--indigo-light)">🔐</div>
        <div><h3 style="font-size:.875rem;font-weight:600">Autenticação OTP</h3><p style="font-size:.8125rem;color:var(--gray-500)">Código de verificação enviado por email para cada login</p></div>
      </div>
      <div class="security-card">
        <div class="security-icon" style="background:#d1fae5">🛡️</div>
        <div><h3 style="font-size:.875rem;font-weight:600">Criptografia</h3><p style="font-size:.8125rem;color:var(--gray-500)">Dados criptografados em trânsito e em repouso</p></div>
      </div>
      <div class="security-card">
        <div class="security-icon" style="background:#dbeafe">👤</div>
        <div><h3 style="font-size:.875rem;font-weight:600">Controle de Acesso</h3><p style="font-size:.8125rem;color:var(--gray-500)">Perfis com permissões específicas por papel</p></div>
      </div>
      <div class="security-card">
        <div class="security-icon" style="background:#fef3c7">📋</div>
        <div><h3 style="font-size:.875rem;font-weight:600">Auditoria</h3><p style="font-size:.8125rem;color:var(--gray-500)">Registro completo de todas as ações</p></div>
      </div>
    </div>
  </section>

</div>

<footer>
  <p style="font-size:1.5rem;margin-bottom:.5rem">◈</p>
  <p><strong>TradeLink</strong> — Transformando a gestão de investimentos</p>
  <p style="margin-top:.5rem;color:var(--gray-500)">Dúvidas? Fale conosco pelo chat de suporte na plataforma.</p>
</footer>

</body>
</html>`

  fs.writeFileSync('TradeLink-Apresentacao.html', html, 'utf-8')
  console.log('✅ HTML criado: TradeLink-Apresentacao.html')
}

// ========== RUN ==========
async function main() {
  try {
    await createPPTX()
    createHTML()
    console.log('\n🎉 Ambas apresentações criadas com sucesso!')
  } catch (err) {
    console.error('Erro:', err)
    process.exit(1)
  }
}

main()
