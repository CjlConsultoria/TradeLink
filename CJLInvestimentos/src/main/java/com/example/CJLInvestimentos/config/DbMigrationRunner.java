package com.example.CJLInvestimentos.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Executa migrações SQL ao iniciar a aplicação (ex.: colunas novas no Render onde o ddl-auto pode não ter rodado).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class DbMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                "ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS acesso_bloqueado_por_admin boolean NOT NULL DEFAULT false"
            );
            log.debug("Migração: coluna acesso_bloqueado_por_admin verificada/criada.");
        } catch (Exception e) {
            log.warn("Migração acesso_bloqueado_por_admin: {} (pode já existir ou ser outro banco)", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE tb_carteiras ADD COLUMN IF NOT EXISTS margem_erro numeric(5,2)");
            jdbcTemplate.execute("ALTER TABLE tb_carteiras ADD COLUMN IF NOT EXISTS moeda_referencia_rebalance varchar(10)");
            jdbcTemplate.execute("ALTER TABLE tb_carteiras ADD COLUMN IF NOT EXISTS rebalance_ativo boolean");
            log.debug("Migração: colunas de rebalanceamento verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração rebalanceamento: {}", e.getMessage());
        }

        // Sistema de convite / ativação de conta
        try {
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cpf varchar(14) UNIQUE");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS whatsapp varchar(30)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cep varchar(10)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS logradouro varchar(200)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS numero varchar(20)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS complemento varchar(100)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS bairro varchar(100)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cidade varchar(100)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS estado varchar(2)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS termo_aceito boolean");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS termo_aceito_em timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS token_convite varchar(128) UNIQUE");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS token_convite_expiracao timestamp");
            log.debug("Migração: colunas de convite/ativação verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração convite/ativação: {}", e.getMessage());
        }

        // Sistema de exclusão / auto-gestão
        try {
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS auto_gestao boolean NOT NULL DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS relatorio_compl_baixado boolean NOT NULL DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS relatorio_compl_baixado_em timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS data_exclusao timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS stripe_customer_id varchar(128)");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS current_period_end timestamp with time zone");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS subscription_status varchar(20) DEFAULT 'NONE'");
            jdbcTemplate.execute("ALTER TABLE tb_faturas ALTER COLUMN empresa_id DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE tb_faturas ADD COLUMN IF NOT EXISTS user_id bigint REFERENCES tb_usuarios(id)");
            log.debug("Migração: colunas de auto-gestão/exclusão verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração auto-gestão/exclusão: {}", e.getMessage());
        }

        // Sistema de auto-cadastro / trial
        try {
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS trial_inicio timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS trial_fim timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS auto_cadastro boolean NOT NULL DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS cnpj varchar(18)");
            jdbcTemplate.execute("ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS trial_inicio timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS trial_fim timestamp");
            jdbcTemplate.execute("ALTER TABLE tb_empresas ADD COLUMN IF NOT EXISTS auto_cadastro boolean NOT NULL DEFAULT false");
            log.debug("Migração: colunas de auto-cadastro/trial verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração auto-cadastro/trial: {}", e.getMessage());
        }

        // FAQ
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_faqs (
                    id bigserial PRIMARY KEY,
                    pergunta text NOT NULL,
                    resposta text NOT NULL,
                    categoria varchar(100),
                    ordem integer NOT NULL DEFAULT 0,
                    ativo boolean NOT NULL DEFAULT true,
                    created_at timestamp NOT NULL DEFAULT now(),
                    updated_at timestamp NOT NULL DEFAULT now()
                )
            """);
            log.debug("Migração: tabela tb_faqs verificada/criada.");
        } catch (Exception e) {
            log.warn("Migração FAQ: {}", e.getMessage());
        }

        // Chat
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_chat_conversas (
                    id bigserial PRIMARY KEY,
                    user_id bigint NOT NULL REFERENCES tb_usuarios(id),
                    status varchar(20) NOT NULL DEFAULT 'ABERTA',
                    assunto varchar(200),
                    created_at timestamp NOT NULL DEFAULT now(),
                    updated_at timestamp NOT NULL DEFAULT now()
                )
            """);
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_chat_mensagens (
                    id bigserial PRIMARY KEY,
                    conversa_id bigint NOT NULL REFERENCES tb_chat_conversas(id),
                    remetente_id bigint NOT NULL REFERENCES tb_usuarios(id),
                    conteudo text NOT NULL,
                    lida boolean NOT NULL DEFAULT false,
                    created_at timestamp NOT NULL DEFAULT now()
                )
            """);
            log.debug("Migração: tabelas de chat verificadas/criadas.");
        } catch (Exception e) {
            log.warn("Migração chat: {}", e.getMessage());
        }

        // Seed FAQ
        try {
            Integer faqCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_faqs", Integer.class);
            if (faqCount == null || faqCount < 5) {
                // Limpa FAQs existentes para inserir o set completo
                jdbcTemplate.execute("DELETE FROM tb_faqs");
                seedFaqs();
                log.info("Migração: {} FAQs inseridas.", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_faqs", Integer.class));
            }
        } catch (Exception e) {
            log.warn("Migração seed FAQ: {}", e.getMessage());
        }

        // Plano tipo (CONSULTOR / AUTO_GESTAO) + seed plano auto-gestão
        try {
            jdbcTemplate.execute("ALTER TABLE tb_planos ADD COLUMN IF NOT EXISTS tipo varchar(20) NOT NULL DEFAULT 'CONSULTOR'");
            // Cria plano Auto-Gestão se não existir
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_planos WHERE tipo = 'AUTO_GESTAO'", Integer.class
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("""
                    INSERT INTO tb_planos (nome, tipo, max_usuarios, preco, ativo, created_at)
                    VALUES ('Auto-Gestão', 'AUTO_GESTAO', 1, 9.99, true, now())
                """);
                log.info("Migração: plano Auto-Gestão criado com preço R$9,99.");
            }
            log.debug("Migração: coluna tipo em tb_planos verificada/criada.");
        } catch (Exception e) {
            log.warn("Migração plano tipo: {}", e.getMessage());
        }
    }

    private void seedFaqs() {
        String[][] faqs = {
            // Conta e Cadastro
            {"Como criar minha conta no TradeLink?", "Acesse a página de cadastro e escolha seu perfil: Investidor (para gerenciar seus próprios investimentos) ou Consultor/Assessor (para gerenciar carteiras de clientes). Preencha seus dados pessoais, endereço, crie uma senha e aceite os termos de uso. Você ganha 5 dias de trial gratuito com acesso completo!", "Conta", "1"},
            {"Esqueci minha senha, como recuperar?", "Na tela de login, clique em 'Esqueci minha senha'. Informe seu e-mail cadastrado e enviaremos um link de redefinição. O link expira em 24 horas. Caso não receba, verifique sua caixa de spam.", "Conta", "2"},
            {"Como alterar meus dados pessoais?", "Acesse Configurações no menu lateral. Lá você pode atualizar nome, telefone/WhatsApp, endereço e alterar sua senha. O CPF e e-mail não podem ser alterados por segurança.", "Conta", "3"},
            {"O que é o período de trial gratuito?", "Ao se cadastrar, você recebe 5 dias de acesso completo ao sistema sem precisar informar cartão de crédito. Após o trial, escolha um plano para continuar usando. Durante o trial, todas as funcionalidades estão disponíveis.", "Conta", "4"},
            {"Como funciona o convite do consultor para clientes?", "O consultor envia um convite por e-mail com um link exclusivo. Ao clicar, o cliente preenche seus dados pessoais (nome, CPF, endereço, senha) e ativa a conta. Automaticamente fica vinculado ao consultor que o convidou.", "Conta", "5"},
            {"Posso ter conta como investidor e consultor?", "Cada e-mail é vinculado a apenas um perfil. Se você é consultor e quer gerenciar seus próprios investimentos, pode se adicionar como cliente na sua própria empresa. Para perfis separados, use e-mails diferentes.", "Conta", "6"},
            {"Como excluir minha conta?", "Entre em contato com o suporte pelo chat. Por segurança, a exclusão de conta é feita manualmente após verificação de identidade. Seus dados serão removidos conforme nossa política de privacidade.", "Conta", "7"},

            // Carteiras e Alocação
            {"O que é uma Carteira no TradeLink?", "Uma Carteira é um modelo de investimento com estratégia definida. Pense como um template: ex. Conservadora (70% renda fixa), Moderada (mix balanceado), Agressiva (foco em cripto). O consultor define a alocação ideal (% por ativo) uma vez, e atribui múltiplos clientes. O sistema calcula automaticamente para cada cliente.", "Carteiras", "10"},
            {"Como criar uma carteira?", "No menu Carteiras, clique em '+ Nova Carteira'. Defina um nome descritivo e uma descrição da estratégia. Depois, na tela de detalhes, configure a Alocação Ideal definindo os ativos e seus percentuais-alvo.", "Carteiras", "11"},
            {"O que é Alocação Ideal?", "É a distribuição percentual que você define como meta para uma carteira. Exemplo: EUR 30%, BTC 20%, USD 50%. A soma deve ser 100%. O sistema usa esses percentuais para calcular se cada cliente precisa comprar ou vender ativos para manter o equilíbrio.", "Carteiras", "12"},
            {"Como funciona a margem de erro?", "A margem de erro (padrão 5%) define a tolerância para o rebalanceamento. Se a alocação ideal do EUR é 30% com margem de 5%, o sistema só sugere ação se o EUR ficar abaixo de 28.5% (comprar) ou acima de 31.5% (vender). Isso evita rebalanceamentos desnecessários por pequenas variações.", "Carteiras", "13"},
            {"Como a alocação se replica para cada cliente?", "O consultor define os percentuais UMA VEZ na carteira (ex: EUR 30%, BTC 20%). Quando múltiplos clientes estão vinculados, o sistema calcula INDIVIDUALMENTE para cada um baseado no saldo de cada cliente. Joao com R$10.000 precisa de R$3.000 em EUR. Maria com R$5.000 precisa de R$1.500 em EUR. O cálculo é proporcional ao saldo de cada um.", "Carteiras", "14"},
            {"Posso ter múltiplas carteiras?", "Sim! Crie quantas carteiras quiser: Conservadora, Moderada, Agressiva, Cripto, Forex, etc. Cada uma com sua própria alocação ideal. Um mesmo cliente pode estar em múltiplas carteiras.", "Carteiras", "15"},
            {"Como atribuir clientes a uma carteira?", "Na tela de detalhes da carteira, na seção Clientes, clique em 'Atribuir Cliente' e selecione o cliente desejado. Ele imediatamente passa a receber as recomendações e análises daquela carteira.", "Carteiras", "16"},
            {"O que é a moeda de referência?", "É a moeda base para calcular os valores do portfolio. Padrão é USD (dólar). Se definir BRL, todos os cálculos de alocação serão feitos convertendo ativos para reais. Escolha a moeda que melhor reflete a realidade dos seus clientes.", "Carteiras", "17"},

            // Recomendações e Operações
            {"O que são Recomendações?", "Recomendações são orientações de compra ou venda que o consultor envia aos clientes. Cada recomendação inclui: tipo (COMPRA/VENDA), par de moedas, preço de entrada, preço alvo, stop loss e quantidade. O cliente recebe notificação e pode registrar a operação executada.", "Recomendações", "20"},
            {"Como funcionam as recomendações automáticas?", "No Painel de Rebalanceamento, o sistema analisa todos os portfolios e identifica quais clientes estão desbalanceados. Com um clique em 'Gerar Recomendações', o sistema cria automaticamente recomendações de compra/venda para reequilibrar cada portfolio. O que levaria horas na planilha leva segundos!", "Recomendações", "21"},
            {"Qual a diferença entre modo Quantidade e Percentual?", "No modo Quantidade, você define um valor fixo (ex: comprar 100 EUR). No modo Percentual, você define um % do portfolio (ex: comprar 5% do portfolio em EUR). No modo percentual, cada cliente recebe uma quantidade diferente proporcional ao seu saldo. Ideal para recomendações em lote.", "Recomendações", "22"},
            {"Como registrar uma operação?", "Ao receber uma recomendação, clique em 'Registrar Operação'. Informe: tipo (compra/venda), preço executado, quantidade, data de execução e uma observação opcional. O portfolio é atualizado automaticamente com os novos saldos.", "Recomendações", "23"},
            {"O que significa marcar como 'Resolvida'?", "Marcar como resolvida indica que você já tomou ação sobre aquela recomendação (executou, ignorou conscientemente, etc). Isso ajuda o consultor a acompanhar o progresso e não fica como pendente no seu dashboard.", "Recomendações", "24"},
            {"O consultor vê minhas operações?", "Sim, o consultor tem visibilidade das operações registradas por seus clientes. Isso permite acompanhar se as recomendações estão sendo seguidas e analisar a performance real do portfolio.", "Recomendações", "25"},
            {"Posso editar ou excluir uma operação?", "Sim! Na lista de operações, clique em Editar para corrigir valores ou em Excluir para remover. O portfolio é recalculado automaticamente. Atenção: a exclusão reverte os saldos ao estado anterior.", "Recomendações", "26"},

            // Rebalanceamento
            {"O que é o Painel de Rebalanceamento?", "É a ferramenta central do consultor. Mostra uma visão consolidada de todos os clientes e suas alocações. Identifica quem está OK, quem precisa de ATENÇÃO e quem está em situação CRÍTICA. Permite gerar recomendações em lote para reequilibrar múltiplos clientes de uma vez.", "Rebalanceamento", "30"},
            {"O que significam os status OK, ATENÇÃO e CRÍTICO?", "OK (verde): todos os ativos estão dentro da margem de tolerância. ATENÇÃO (amarelo): 1-2 ativos fora da faixa ideal. CRÍTICO (vermelho): 3 ou mais ativos desbalanceados, ou algum ativo com desvio maior que 15%. Quanto mais vermelho, mais urgente é rebalancear.", "Rebalanceamento", "31"},
            {"Como funciona o rebalanceamento em lote?", "No painel, selecione os clientes desbalanceados usando os checkboxes. Clique em 'Gerar Recomendações'. O sistema calcula automaticamente as quantidades exatas de compra/venda para cada cliente individualmente e gera as recomendações. Tudo em segundos!", "Rebalanceamento", "32"},
            {"Posso filtrar clientes no painel?", "Sim! Filtre por nome do cliente, carteira específica, status (OK/ATENÇÃO/CRÍTICO) e ativo específico. Use a aba 'Por Ativo' para ver quais clientes precisam comprar ou vender cada ativo específico.", "Rebalanceamento", "33"},
            {"Como o sistema calcula a quantidade ideal?", "O cálculo é: (percentual_alvo × valor_total_portfolio) / preco_do_ativo. Exemplo: se o alvo é 30% EUR e o cliente tem $10.000, o ideal é $3.000 em EUR. Se EUR custa $1.10, precisa ter ~2.727 EUR. Se tem menos, recomenda COMPRA. Se tem mais, recomenda VENDA.", "Rebalanceamento", "34"},

            // Portfolio e Ativos
            {"Como adicionar ativos ao meu portfolio?", "Na tela 'Meu Portfolio', clique em '+ Adicionar Ativo'. Informe o símbolo (ex: EUR, BTC), categoria (Crypto, Forex, Ação, Commodities), quantidade que você possui e opcionalmente um preço manual. O sistema busca cotações automaticamente.", "Portfolio", "40"},
            {"De onde vêm os preços dos ativos?", "O TradeLink busca cotações em tempo real de diversas fontes. Se um ativo não tiver cotação automática, você pode definir um 'preço manual' que será usado nos cálculos. As cotações são atualizadas a cada 60 segundos.", "Portfolio", "41"},
            {"O que é o portfolio e como funciona?", "O portfolio mostra todos os seus ativos com quantidade, preço atual, valor total e percentual de alocação. É atualizado automaticamente quando você registra operações. Inclui gráfico de alocação (pizza) mostrando a distribuição visual dos seus investimentos.", "Portfolio", "42"},
            {"Como funciona o histórico de movimentações?", "Toda vez que você registra um aporte (depósito) ou saque, fica registrado no histórico. Isso permite acompanhar a evolução do portfolio ao longo do tempo e calcular performance real descontando aportes.", "Portfolio", "43"},

            // Pagamentos e Planos
            {"Quais são os planos disponíveis?", "Para investidores individuais: Auto-Gestão por R$9,99/mês. Para consultores: planos a partir de R$49,90/mês (Básico, Pro e Enterprise), variando pelo número de clientes permitidos. Todos incluem acesso completo às funcionalidades.", "Pagamentos", "50"},
            {"Quais formas de pagamento são aceitas?", "Aceitamos cartão de crédito, boleto bancário e PIX. Os pagamentos são processados de forma segura pelo Stripe. A renovação é mensal e automática para cartão de crédito.", "Pagamentos", "51"},
            {"Como cancelar minha assinatura?", "Acesse a tela de Faturas e gerencie sua assinatura. Você pode cancelar a qualquer momento. O acesso continua até o final do período pago. Após o cancelamento, seus dados ficam preservados por 90 dias caso queira reativar.", "Pagamentos", "52"},
            {"O que acontece se meu pagamento atrasar?", "Há um período de tolerância de 5 dias após o vencimento. Durante esse período, o acesso continua normalmente. Após os 5 dias, o acesso é bloqueado até a regularização. Suas informações não são perdidas.", "Pagamentos", "53"},
            {"O que é o plano Auto-Gestão?", "É o plano para investidores individuais que querem gerenciar seus próprios investimentos sem um consultor. Por R$9,99/mês, você tem acesso ao portfolio pessoal, cotações em tempo real, relatórios, gráficos e suporte via chat.", "Pagamentos", "54"},
            {"Posso mudar de plano?", "Sim! Consultores podem fazer upgrade ou downgrade entre os planos Básico, Pro e Enterprise. A mudança é aplicada no próximo ciclo de cobrança. Entre em contato com o suporte para solicitar a alteração.", "Pagamentos", "55"},

            // Cotações
            {"Como funcionam as cotações?", "O sistema busca cotações de moedas, criptomoedas e outros ativos em tempo real. Na tela de Cotações, você pode filtrar por moeda, par, fonte e faixa de preço. As cotações são atualizadas automaticamente a cada 60 segundos.", "Cotações", "60"},
            {"Posso forçar atualização das cotações?", "Sim! Na tela de cotações, há um botão de refresh que força a busca de novas cotações. Para um par específico, clique no botão de atualizar do card de cotação.", "Cotações", "61"},

            // Relatórios
            {"Que tipos de relatórios estão disponíveis?", "Para consultores: Histórico de operações, Resumo com gráficos, Por carteira, Por cliente, Recomendações resolvidas e Perdas e ganhos. Para clientes: Histórico, Resumo de ganhos/perdas e Gráficos por período. Todos exportáveis em PDF.", "Relatórios", "70"},
            {"Como exportar relatórios em PDF?", "Em qualquer tela de relatórios, clique no botão 'Exportar PDF'. O sistema gera um documento com os dados filtrados pelo período e critérios selecionados. O PDF é baixado automaticamente para seu computador.", "Relatórios", "71"},
            {"Os relatórios incluem gráficos?", "Sim! A aba de Resumo e Gráficos inclui gráfico de barras (operações por período) e gráfico de rosca (operações por moeda/par). Os gráficos são interativos e também aparecem nos PDFs exportados.", "Relatórios", "72"},

            // Suporte e Chat
            {"Como funciona o suporte?", "O suporte funciona via chat integrado no sistema. Primeiro, nosso assistente virtual tentará resolver sua dúvida automaticamente com base nas perguntas frequentes. Se precisar de atendimento humano, você será transferido para um atendente.", "Suporte", "80"},
            {"Qual o horário de atendimento?", "O assistente virtual funciona 24 horas por dia, 7 dias por semana. O atendimento humano está disponível em horário comercial (seg-sex, 9h-18h). Fora do horário, deixe sua mensagem e responderemos no próximo dia útil.", "Suporte", "81"},
            {"Posso receber notificações?", "Sim! Configure nas Configurações: notificações por e-mail, push no navegador e/ou Telegram. Você será avisado sobre novas recomendações, operações registradas e atualizações importantes.", "Suporte", "82"},

            // Segurança
            {"Meus dados estão seguros?", "Sim! Utilizamos criptografia SSL/TLS, senhas armazenadas com BCrypt (hash irreversível), autenticação JWT com tokens temporários, e processamento de pagamentos via Stripe (certificado PCI DSS). Seus dados nunca são compartilhados com terceiros.", "Segurança", "90"},
            {"Posso ativar autenticação de dois fatores?", "Atualmente a segurança é baseada em senha forte + token JWT com expiração. Estamos trabalhando na implementação de 2FA (autenticação de dois fatores) que estará disponível em breve.", "Segurança", "91"},

            // Funcionalidades Avançadas
            {"O que acontece quando o consultor me exclui?", "Você não perde acesso ao sistema. Sua conta fica independente e você pode: assinar o plano Auto-Gestão (R$9,99/mês) para continuar gerenciando seu portfolio, baixar um relatório completo gratuito (1 vez), ou ser vinculado a outro consultor.", "Conta", "95"},
            {"Como funciona a visualização de impacto?", "Antes de enviar uma recomendação, o consultor pode clicar em 'Ver Impacto' para simular como a operação afetaria a alocação de cada cliente. Mostra o antes e depois, mudanças de status e valores estimados.", "Recomendações", "96"},
            {"O que são Snapshots de Performance?", "O sistema tira 'fotos' periódicas do portfolio para calcular a performance ao longo do tempo. Isso permite comparar a evolução da carteira com benchmarks como Buy & Hold de Bitcoin, mostrando se a estratégia está gerando valor.", "Portfolio", "97"}
        };

        for (String[] faq : faqs) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO tb_faqs (pergunta, resposta, categoria, ordem, ativo, created_at, updated_at) VALUES (?, ?, ?, ?, true, now(), now())",
                    faq[0], faq[1], faq[2], Integer.parseInt(faq[3])
                );
            } catch (Exception e) {
                log.warn("Erro ao inserir FAQ '{}': {}", faq[0].substring(0, 30), e.getMessage());
            }
        }
    }
}
