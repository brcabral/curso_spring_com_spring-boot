package com.algaworks.brewer.service;

import java.util.Optional;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.repository.Clientes;
import com.algaworks.brewer.service.exception.CpfCnpjJaCadastradoException;
import com.algaworks.brewer.service.exception.ImpossivelExcluirEntidadeException;

@Service
public class CadastroClienteService {
	@Autowired
	private Clientes clientes;

	@Transactional
	public void salvar(Cliente cliente) {
		Optional<Cliente> clienteExistente = clientes.findByCpfCnpj(cliente.getCpfCnpjSemFormatacao());

		if (clienteExistente.isPresent() && cliente.isNovo()) {
			throw new CpfCnpjJaCadastradoException("CPF/CNPJ já cadastrado!");
		}

		clientes.save(cliente);
	}

//	@Transactional
//	public void excluir(Long codigo) {
//		try {
//			clientes.delete(codigo);
//			clientes.flush();
//		} catch (PersistenceException e) {
//			throw new ImpossivelExcluirEntidadeException(
//					"Não foi possível excluir o cliente. \nEle já foi utilizada em alguma venda");
//		}
//	}
}
