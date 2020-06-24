package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Telefone;

@Repository
@Transactional
public interface TelefoneRepository extends CrudRepository<Telefone, Long>{
	
	@Query("select t from Telefone t where t.pessoa.id = ?1")
	List<Telefone> getTelefones(Long pessoaId);
	
	@Query("select t.pessoa.id from Telefone t where t.id = ?1")
	Long getPessoaId(Long idTelefone);
	
	@Modifying
	@Query("delete Telefone t where t.pessoa.id = ?1")
	void deletarRegistroTelefonePorPessoa(Long id);
	
}
