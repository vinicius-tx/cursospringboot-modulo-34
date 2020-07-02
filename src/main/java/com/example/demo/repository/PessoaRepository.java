package com.example.demo.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Pessoa;

@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	
	@Query("select p from Pessoa p where p.nome like %?1%")
	List<Pessoa> findPessoaByName(String nome);

	@Query("select p from Pessoa p where p.nome like %?1% and p.sexo = ?2")
	List<Pessoa> findPessoaByNameAndSex(String nomepesquisa, String sexo);

	@Query("select p from Pessoa p where p.sexo = ?1")
	List<Pessoa> findPessoaBySex(String sexo);
	
	default Page<Pessoa> findPessoaByNamePage(String nome, Pageable pageable) {
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		ExampleMatcher exampleMatcher = ExampleMatcher
					.matchingAny()
					.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);
		Page<Pessoa> pessoas = findAll(example, pageable);
		return pessoas;
	}
	
	
}

