package com.example.demo.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;


@Entity
public class Pessoa implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Temporal(TemporalType.DATE)
	private Date dataNascimento;
	
	@Enumerated(EnumType.STRING)
	private Cargo cargo;

	@NotNull(message = "Nome não pode ser nulo")
	@NotEmpty(message = "Nome não pode estar vazio")
	private String nome;
	
	@NotNull(message = "Sobrenome não pode ser nulo")
	@NotEmpty(message = "Sobrenome não pode estar vazio")
	private String sobrenome;
	
	
	@Min(value = 18, message = "Idade Invalida")
	private Integer idade;
	
	@Column(columnDefinition = "varchar(1)")
	private String sexo;
	
	@OneToMany(mappedBy = "pessoa")
	private List<Telefone> telefones = null;
	
	@ManyToOne
	private Profissao profissao;
	
	@Lob
	private byte[] curriculo;
	
	@Column(columnDefinition = "varchar(60)")
	private String nomeFileCurriculo;
	
	@Column(columnDefinition = "varchar(60)")
	private String tipoFileCurriculo;
	
	public Pessoa() {
		// TODO Auto-generated constructor stub
	}
	
	public Integer getIdade() {
		return idade;
	}

	public void setIdade(Integer idade) {
		this.idade = idade;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSobrenome() {
		return sobrenome;
	}

	public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome;
	}

	public List<Telefone> getTelefones() {
		return telefones;
	}

	public void setTelefones(List<Telefone> telefones) {
		this.telefones = telefones;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public Profissao getProfissao() {
		return profissao;
	}

	public void setProfissao(Profissao profissao) {
		this.profissao = profissao;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public Date getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public byte[] getCurriculo() {
		return curriculo;
	}

	public void setCurriculo(byte[] curriculo) {
		this.curriculo = curriculo;
	}

	public String getNomeFileCurriculo() {
		return nomeFileCurriculo;
	}

	public void setNomeFileCurriculo(String nomeFileCurriculo) {
		this.nomeFileCurriculo = nomeFileCurriculo;
	}

	public String getTipoFileCurriculo() {
		return tipoFileCurriculo;
	}

	public void setTipoFileCurriculo(String tipoFileCurriculo) {
		this.tipoFileCurriculo = tipoFileCurriculo;
	}
}
