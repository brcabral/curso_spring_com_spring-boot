package com.algaworks.brewer.repository.helper.venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.dto.VendaMes;
import com.algaworks.brewer.dto.VendaOrigem;
import com.algaworks.brewer.model.StatusVenda;
import com.algaworks.brewer.model.TipoPessoa;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.filter.VendaFilter;
import com.algaworks.brewer.repository.paginacao.PaginacaoUtil;

public class VendasImpl implements VendasQueries {
	@PersistenceContext
	private EntityManager manager;

	@Autowired
	private PaginacaoUtil paginacaoUtil;

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Page<Venda> filtrar(VendaFilter filtro, Pageable pageable) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Venda.class);
		paginacaoUtil.preparar(criteria, pageable);
		adicionarFiltro(filtro, criteria);

		return new PageImpl<>(criteria.list(), pageable, total(filtro));
	}

	private void adicionarFiltro(VendaFilter filtro, Criteria criteria) {
		criteria.createAlias("cliente", "c");

		if (filtro != null) {
			if (!StringUtils.isEmpty(filtro.getCodigo())) {
				criteria.add(Restrictions.eq("codigo", filtro.getCodigo()));
			}

			if (!StringUtils.isEmpty(filtro.getStatusVenda())) {
				criteria.add(Restrictions.eq("status", filtro.getStatusVenda()));
			}

			if (filtro.getDataCriacaoInicial() != null) {
				LocalDateTime dataInicio = LocalDateTime.of(filtro.getDataCriacaoInicial(), LocalTime.of(0, 0));
				criteria.add(Restrictions.ge("dataCriacao", dataInicio));
			}

			if (filtro.getDataCriacaoFinal() != null) {
				LocalDateTime dataFim = LocalDateTime.of(filtro.getDataCriacaoFinal(), LocalTime.of(0, 0));
				criteria.add(Restrictions.le("dataCriacao", dataFim));
			}

			if (filtro.getValorMinimo() != null) {
				criteria.add(Restrictions.ge("valorTotal", filtro.getValorMinimo()));
			}

			if (filtro.getValorMaximo() != null) {
				criteria.add(Restrictions.le("valorTotal", filtro.getValorMaximo()));
			}

			if (!StringUtils.isEmpty(filtro.getNomeCliente())) {
				criteria.add(Restrictions.ilike("c.nome", filtro.getNomeCliente(), MatchMode.ANYWHERE));
			}

			if (!StringUtils.isEmpty(filtro.getCpfCnpjCliente())) {
				criteria.add(Restrictions.eq("c.cpfCnpj", TipoPessoa.removerFormatacao(filtro.getCpfCnpjCliente())));
			}
		}
	}

	private Long total(VendaFilter filtro) {
		Criteria criteria = manager.unwrap(Session.class).createCriteria(Venda.class);
		adicionarFiltro(filtro, criteria);
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult();
	}

	@Override
	public BigDecimal valorTotalNoAno() {
		Optional<BigDecimal> optional = Optional.ofNullable(manager
				.createQuery("select sum(valorTotal) from Venda where year(dataCriacao) = :ano and status = :status",
						BigDecimal.class)
				.setParameter("ano", Year.now().getValue()).setParameter("status", StatusVenda.EMITIDA)
				.getSingleResult());
		return optional.orElse(BigDecimal.ZERO);
	}

	@Override
	public BigDecimal valorTotalNoMes() {
		Optional<BigDecimal> optional = Optional.ofNullable(manager
				.createQuery("select sum(valorTotal) from Venda where month(dataCriacao) = :mes and status = :status",
						BigDecimal.class)
				.setParameter("mes", MonthDay.now().getMonthValue()).setParameter("status", StatusVenda.EMITIDA)
				.getSingleResult());
		return optional.orElse(BigDecimal.ZERO);
	}

	@Override
	public BigDecimal valorTicketMedioNoAno() {
		Optional<BigDecimal> optional = Optional.ofNullable(manager.createQuery(
				"select sum(valorTotal)/count(*) from Venda where year(dataCriacao) = :ano and status = :status",
				BigDecimal.class).setParameter("ano", Year.now().getValue()).setParameter("status", StatusVenda.EMITIDA)
				.getSingleResult());
		return optional.orElse(BigDecimal.ZERO);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<VendaMes> totalPorMes() {
		List<VendaMes> vendaMes = manager.createNamedQuery("Vendas.totalPorMes").getResultList();

		LocalDate hoje = LocalDate.now();
		for (int i = 1; i <= 6; i++) {
			String mesIdeal = String.format("%d/%02d", hoje.getYear(), hoje.getMonthValue());

			boolean possuiMes = vendaMes.stream().filter(v -> v.getMes().equals(mesIdeal)).findAny().isPresent();
			if (!possuiMes) {
				vendaMes.add(i - 1, new VendaMes(mesIdeal, 0));
			}
			hoje = hoje.minusMonths(1);
		}

		return vendaMes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<VendaOrigem> totalPorOrigem() {
		List<VendaOrigem> vendaOrigem = manager.createNamedQuery("Vendas.porOrigem").getResultList();

		LocalDate hoje = LocalDate.now();
		for (int i = 1; i <= 6; i++) {
			String mesIdeal = String.format("%d/%02d", hoje.getYear(), hoje.getMonthValue());

			boolean possuiMes = vendaOrigem.stream().filter(v -> v.getMes().equals(mesIdeal)).findAny().isPresent();
			if (!possuiMes) {
				vendaOrigem.add(i - 1, new VendaOrigem(mesIdeal, 0, 0));
			}
			hoje = hoje.minusMonths(1);
		}

		return vendaOrigem;
	}
}
