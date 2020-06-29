package com.example.demo.controller;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
	private final List<String> mensagemsDeErro = new ArrayList<>();
	
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
			for (ObjectError object : binding.getAllErrors()) {
				mensagemsDeErro.add(object.getDefaultMessage());
			}
			
			return paginaCadastroPessoa();
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
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa, @RequestParam("sexo") String sexo) {
		mensagemsDeErro.clear();
		return  paginaCadastroPessoa().addObject("pessoas", processaCondicoes(nomepesquisa, sexo));
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
	
	@GetMapping("**/pesquisapessoa")
	public void imprimePDF(@RequestParam("nomepesquisa") String nomepesquisa, @RequestParam("sexo") 
	String sexo, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		List<Pessoa> pessoas = processaCondicoes(nomepesquisa, sexo);
		byte[] pdf = ReportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		response.setContentLength(pdf.length);
		response.setContentType("application/octet-stream");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf" );
		response.setHeader(headerKey, headerValue);
		response.getOutputStream().write(pdf);
		
	}
	
	public boolean pesquisaValida(String nome, String sexo) {
		boolean valido = true;
		valido = pesquisaNomeValida(nome);
		valido = pesquisaSexoValida(sexo);
		return valido;
	}
	
	public boolean pesquisaSexoValida(String sexo) {
		if(sexo == null || sexo.isEmpty()) {
			String mensagem = "Sexo não informado";
			if (!mensagemsDeErro.contains(mensagem)) {				
				mensagemsDeErro.add(mensagem);
			}
			return false;
		}
		return true;
	}
	
	public boolean pesquisaNomeValida(String nome) {
		boolean valido = true;
		
		if (nome == null || nome.isEmpty()) {
			String mensagem = "Nome da pesquisa não foi informado";
			if (!mensagemsDeErro.contains("Nome da pesquisa não foi informado")) {				
				mensagemsDeErro.add(mensagem);
			}
			valido = false;
		}
		return valido;
	}
	
	public List<Pessoa> processaCondicoes(String nomepesquisa, String sexo) {
		if (pesquisaValida(nomepesquisa, sexo)) return pessoaRepository.findPessoaByNameAndSex(nomepesquisa, sexo);
		
		if(pesquisaNomeValida(nomepesquisa)) return pessoaRepository.findPessoaByName(nomepesquisa);
		
		if (pesquisaSexoValida(sexo)) return pessoaRepository.findPessoaBySex(sexo);
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		Iterable<Pessoa> it = pessoaRepository.findAll();
		for (Pessoa p : it) {
			pessoas.add(p);
		}
	
		return pessoas;
	}

	
	
	public ModelAndView paginaCadastroPessoa() {
		ModelAndView andView =  new ModelAndView(telaCadastroPessoa);
		Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();		
		andView.addObject("pessoas", pessoasIt);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("msg", mensagemsDeErro);
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
