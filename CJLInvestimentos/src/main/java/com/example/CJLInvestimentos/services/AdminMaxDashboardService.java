package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.dtos.response.AdminMaxDashboardResponse;
import com.example.CJLInvestimentos.dtos.response.AdminMaxDashboardResponse.*;
import com.example.CJLInvestimentos.entities.Empresa;
import com.example.CJLInvestimentos.entities.Fatura;
import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.entities.enums.StatusFatura;
import com.example.CJLInvestimentos.entities.enums.SubscriptionStatus;
import com.example.CJLInvestimentos.repositories.CarteiraRepository;
import com.example.CJLInvestimentos.repositories.EmpresaRepository;
import com.example.CJLInvestimentos.repositories.FaturaRepository;
import com.example.CJLInvestimentos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMaxDashboardService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final CarteiraRepository carteiraRepository;
    private final FaturaRepository faturaRepository;

    public AdminMaxDashboardResponse getDashboardStats() {
        List<Empresa> todasEmpresas = empresaRepository.findAll();
        List<User> todosUsuarios = userRepository.findAll();
        List<Fatura> todasFaturas = faturaRepository.findAll();

        long totalEmpresas = todasEmpresas.size();
        long empresasAtivas = todasEmpresas.stream().filter(e -> Boolean.TRUE.equals(e.getAtivo())).count();
        long empresasInativas = totalEmpresas - empresasAtivas;

        long totalConsultores = todosUsuarios.stream().filter(u -> u.getRole() == Role.Admin).count();
        long totalClientes = todosUsuarios.stream().filter(u -> u.getRole() == Role.Cliente).count();
        long totalCarteiras = carteiraRepository.count();

        // Distribuição por subscription status
        Map<String, Long> empresasPorSubscription = new LinkedHashMap<>();
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            long count = todasEmpresas.stream()
                    .filter(e -> e.getSubscriptionStatus() == status)
                    .count();
            empresasPorSubscription.put(status.name(), count);
        }

        // Distribuição por plano
        Map<String, Long> planoCount = new LinkedHashMap<>();
        for (Empresa e : todasEmpresas) {
            String planoNome = e.getPlano() != null ? e.getPlano().getNome() : "Sem plano";
            planoCount.merge(planoNome, 1L, Long::sum);
        }
        List<PlanoDistribuicao> empresasPorPlano = planoCount.entrySet().stream()
                .map(entry -> PlanoDistribuicao.builder()
                        .nome(entry.getKey())
                        .quantidade(entry.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getQuantidade(), a.getQuantidade()))
                .collect(Collectors.toList());

        // Receita - classificação por tipo
        List<Fatura> faturasPagas = todasFaturas.stream()
                .filter(f -> f.getStatus() == StatusFatura.PAGA)
                .collect(Collectors.toList());

        BigDecimal receitaTotal = faturasPagas.stream()
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Faturas de empresas (assinatura de planos)
        List<Fatura> faturasEmpresa = faturasPagas.stream()
                .filter(f -> f.getEmpresa() != null).collect(Collectors.toList());
        // Faturas individuais (auto-gestão + relatórios)
        List<Fatura> faturasIndividuais = faturasPagas.stream()
                .filter(f -> f.getUser() != null && f.getEmpresa() == null).collect(Collectors.toList());
        List<Fatura> faturasRelatorio = faturasIndividuais.stream()
                .filter(f -> f.getDescricaoServico() != null && f.getDescricaoServico().toLowerCase().contains("relat"))
                .collect(Collectors.toList());
        List<Fatura> faturasAutoGestao = faturasIndividuais.stream()
                .filter(f -> !faturasRelatorio.contains(f)).collect(Collectors.toList());

        BigDecimal receitaEmpresas = faturasEmpresa.stream()
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receitaAutoGestao = faturasAutoGestao.stream()
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receitaRelatorios = faturasRelatorio.stream()
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth mesAtual = YearMonth.now();
        Instant inicioMes = mesAtual.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant fimMes = mesAtual.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        java.util.function.Predicate<Fatura> noMesAtual = f -> f.getDataPagamento() != null
                && !f.getDataPagamento().isBefore(inicioMes) && f.getDataPagamento().isBefore(fimMes);

        BigDecimal receitaMesAtual = faturasPagas.stream().filter(noMesAtual)
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receitaEmpresasMes = faturasEmpresa.stream().filter(noMesAtual)
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receitaAutoGestaoMes = faturasAutoGestao.stream().filter(noMesAtual)
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receitaRelatoriosMes = faturasRelatorio.stream().filter(noMesAtual)
                .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Clientes com auto-gestão ativa
        long clientesAutoGestaoAtivos = todosUsuarios.stream()
                .filter(u -> Boolean.TRUE.equals(u.getAutoGestao())
                        && u.getCurrentPeriodEnd() != null
                        && u.getCurrentPeriodEnd().isAfter(Instant.now()))
                .count();

        long faturasPendentes = todasFaturas.stream()
                .filter(f -> f.getStatus() == StatusFatura.PENDENTE)
                .count();

        long faturasVencidas = todasFaturas.stream()
                .filter(f -> f.getStatus() == StatusFatura.VENCIDA)
                .count();

        // Receita mensal detalhada (últimos 6 meses)
        List<AdminMaxDashboardResponse.ReceitaMensal> receitaMensal = calcularReceitaMensal(faturasPagas);

        // Crescimento mensal (últimos 6 meses)
        List<CrescimentoMensal> crescimentoMensal = calcularCrescimentoMensal(todasEmpresas, todosUsuarios);

        // Empresas recentes (últimas 5 criadas)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<EmpresaResumo> empresasRecentes = todasEmpresas.stream()
                .filter(e -> e.getCreatedAt() != null)
                .sorted(Comparator.comparing(Empresa::getCreatedAt).reversed())
                .limit(5)
                .map(e -> {
                    long usrs = todosUsuarios.stream()
                            .filter(u -> u.getEmpresa() != null && u.getEmpresa().getId().equals(e.getId()))
                            .count();
                    return EmpresaResumo.builder()
                            .id(e.getId())
                            .nome(e.getNome())
                            .planoNome(e.getPlano() != null ? e.getPlano().getNome() : "Sem plano")
                            .ativa(Boolean.TRUE.equals(e.getAtivo()))
                            .subscriptionStatus(e.getSubscriptionStatus() != null ? e.getSubscriptionStatus().name() : "NONE")
                            .totalUsuarios(usrs)
                            .createdAt(e.getCreatedAt().format(dtf))
                            .build();
                })
                .collect(Collectors.toList());

        return AdminMaxDashboardResponse.builder()
                .totalEmpresas(totalEmpresas)
                .empresasAtivas(empresasAtivas)
                .empresasInativas(empresasInativas)
                .totalConsultores(totalConsultores)
                .totalClientes(totalClientes)
                .totalCarteiras(totalCarteiras)
                .empresasPorSubscription(empresasPorSubscription)
                .empresasPorPlano(empresasPorPlano)
                .receitaTotal(receitaTotal)
                .receitaMesAtual(receitaMesAtual)
                .receitaEmpresas(receitaEmpresas)
                .receitaAutoGestao(receitaAutoGestao)
                .receitaRelatorios(receitaRelatorios)
                .receitaEmpresasMes(receitaEmpresasMes)
                .receitaAutoGestaoMes(receitaAutoGestaoMes)
                .receitaRelatoriosMes(receitaRelatoriosMes)
                .clientesAutoGestaoAtivos(clientesAutoGestaoAtivos)
                .receitaMensal(receitaMensal)
                .faturasPendentes(faturasPendentes)
                .faturasVencidas(faturasVencidas)
                .crescimentoMensal(crescimentoMensal)
                .empresasRecentes(empresasRecentes)
                .build();
    }

    private List<AdminMaxDashboardResponse.ReceitaMensal> calcularReceitaMensal(List<Fatura> faturasPagas) {
        YearMonth mesAtual = YearMonth.now();
        ZoneId zone = ZoneId.systemDefault();
        List<AdminMaxDashboardResponse.ReceitaMensal> resultado = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth mes = mesAtual.minusMonths(i);
            String mesStr = mes.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Instant inicio = mes.atDay(1).atStartOfDay(zone).toInstant();
            Instant fim = mes.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();

            java.util.function.Predicate<Fatura> noMes = f -> f.getDataPagamento() != null
                    && !f.getDataPagamento().isBefore(inicio) && f.getDataPagamento().isBefore(fim);

            BigDecimal emp = faturasPagas.stream().filter(noMes)
                    .filter(f -> f.getEmpresa() != null)
                    .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal rel = faturasPagas.stream().filter(noMes)
                    .filter(f -> f.getUser() != null && f.getEmpresa() == null
                            && f.getDescricaoServico() != null && f.getDescricaoServico().toLowerCase().contains("relat"))
                    .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ag = faturasPagas.stream().filter(noMes)
                    .filter(f -> f.getUser() != null && f.getEmpresa() == null
                            && (f.getDescricaoServico() == null || !f.getDescricaoServico().toLowerCase().contains("relat")))
                    .map(Fatura::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

            resultado.add(AdminMaxDashboardResponse.ReceitaMensal.builder()
                    .mes(mesStr).empresas(emp).autoGestao(ag).relatorios(rel).build());
        }
        return resultado;
    }

    private List<CrescimentoMensal> calcularCrescimentoMensal(List<Empresa> empresas, List<User> usuarios) {
        YearMonth mesAtual = YearMonth.now();
        List<CrescimentoMensal> resultado = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth mes = mesAtual.minusMonths(i);
            String mesStr = mes.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            long novasEmpresas = empresas.stream()
                    .filter(e -> e.getCreatedAt() != null)
                    .filter(e -> YearMonth.from(e.getCreatedAt()).equals(mes))
                    .count();

            long novosUsuarios = usuarios.stream()
                    .filter(u -> u.getCreatedAt() != null)
                    .filter(u -> YearMonth.from(u.getCreatedAt()).equals(mes))
                    .count();

            resultado.add(CrescimentoMensal.builder()
                    .mes(mesStr)
                    .empresas(novasEmpresas)
                    .usuarios(novosUsuarios)
                    .build());
        }

        return resultado;
    }
}
