package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.example.demo.model.Pessoa;
import com.example.demo.model.Telefone;
import com.example.demo.repository.PessoaRepository;
import com.example.demo.repository.TelefoneRepository;

@Controller
public class PessoaController {

	private final String telaCadastroPessoa = "cadastro/cadastropessoa";
	private final String telaTelefone = "cadastro/telefones";
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		return paginaCadastroPessoa();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		return paginaCadastroPessoa();
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa")
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult binding) {
		
		if (binding.hasErrors()) {
			List<String> msg = new ArrayList<>();
			for (ObjectError object : binding.getAllErrors()) {
				msg.add(object.getDefaultMessage());
			}
			
			return paginaCadastroPessoa().addObject("msg", msg);
		}
		
		pessoaRepository.save(pessoa);
		return paginaCadastroPessoa();
	}
	
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		return paginaCadastroPessoa().addObject("pessoaobj", pessoa);
	}
	
	@GetMapping("removerpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {
		telefoneRepository.deletarRegistroTelefonePorPessoa(idpessoa);
		pessoaRepository.deleteById(idpessoa);
		return paginaCadastroPessoa();
	}
	
	@PostMapping("**/pesquisapessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa) {
		return paginaCadastroPessoa()
				.addObject("pessoas", pessoaRepository.findPessoaByName(nomepesquisa));
	}
	
	@GetMapping("**/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {
		return paginaTelefone(idpessoa);
		
	}
	
	@PostMapping("**/addfonepessoa/{pessoaid}")
	public ModelAndView addFonePessoa(Telefone telefone, 
			@PathVariable("pessoaid") Long pessoaId) {
		
		if (!telefone.isValido()) {
			return paginaTelefone(pessoaId).addObject("msg", telefone.getErrorMessageUser());
		}
		
		Pessoa pessoa = pessoaRepository.findById(pessoaId).get();
		telefone.setPessoa(pessoa);
		telefoneRepository.save(telefone);
		return paginaTelefone(pessoaId);
	}
	
	@GetMapping("removertelefone/{idtelefone}")
	public ModelAndView removerTelefone(@PathVariable("idtelefone") Long idTelefone) {
		Long idPessoa = telefoneRepository.getPessoaId(idTelefone);
		telefoneRepository.deleteById(idTelefone);
		return 	paginaTelefone(idPessoa);		
	}
	
	public ModelAndView paginaCadastroPessoa() {
		ModelAndView andView =  new ModelAndView(telaCadastroPessoa);
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();		
		andView.addObject("pessoas", pessoasIt);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("msg", "");
		return andView;
	}
	
	public ModelAndView paginaTelefone(Long id) {
		ModelAndView modelAndView = new ModelAndView(telaTelefone);
		Pessoa pessoa = pessoaRepository.findById(id).get();
		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(id));
		modelAndView.addObject("msg", "");
		return modelAndView;
	}
}
