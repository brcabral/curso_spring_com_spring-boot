package com.algaworks.brewer.service;

import java.util.Optional;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.repository.Estilos;
import com.algaworks.brewer.service.exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.service.exception.NomeEstiloJaCadastradoException;

@Service
public class CadastroEstiloService {
	@Autowired
	private Estilos estilos;

	@Transactional
	public Estilo salvar(Estilo estilo) {
		Optional<Estilo> estiloOptional = estilos.findByNomeIgnoreCase(estilo.getNome());

		if (estiloOptional.isPresent()) {
			throw new NomeEstiloJaCadastradoException("Nome do estilo já cadastrado");
		}

		return estilos.saveAndFlush(estilo);
	}

//	@Transactional
//	public void excluir(Long codigo) {
//		try {
//			estilos.delete(codigo);
//			estilos.flush();
//		} catch (PersistenceException e) {
//			throw new ImpossivelExcluirEntidadeException(
//					"Não foi possível excluir o estilo. \nEle já foi utilizado em alguma cerveja.");
//		}
//	}
}
