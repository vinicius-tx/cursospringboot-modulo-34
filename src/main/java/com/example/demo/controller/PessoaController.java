package com.example.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.model.Pessoa;
import com.example.demo.model.Telefone;
import com.example.demo.repository.PessoaRepository;
import com.example.demo.repository.ProfissaoRepository;
import com.example.demo.repository.TelefoneRepository;

@Controller
public class PessoaController {

	private final String telaCadastroPessoa = "cadastro/cadastropessoa";
	private final String telaTelefone = "cadastro/telefones";
	private final List<String> mensagemsDeErro = new ArrayList<>();
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	private boolean retornoDowloadPaginaValida = false;
	
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		return paginaCadastroPessoa();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		return paginaCadastroPessoa();
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult binding, final MultipartFile file) throws IOException {
		mensagemsDeErro.clear();
		erroJpaBindingMessages(binding);
		if (!mensagemsDeErro.isEmpty()) return paginaCadastroPessoa();
		pessoaRepository.save(insereCurriculo(pessoa, file));
		return paginaCadastroPessoa();
	}
	
	
	@GetMapping("**/baixarCurriculo/{idpessoa}")
	public ModelAndView baixarCurriculo(@PathVariable("idpessoa") Long id, HttpServletResponse response) throws IOException {
		mensagemsDeErro.clear();
		Pessoa pessoa = pessoaRepository.findById(id).get();
		response = processarDowloadComoResposta(pessoa.getCurriculo(), response, 
				pessoa.getTipoFileCurriculo(), pessoa.getNomeFileCurriculo());
		return retornoDowloadPaginaValida ? paginaCadastroPessoa(): null; 
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
	
	@PostMapping("**/pesquisapessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("sexo") String sexo, @PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {
		mensagemsDeErro.clear();
		return  paginaCadastroPessoa()
					.addObject("pessoas", processaCondicoes(nomepesquisa, sexo, false, pageable))
					.addObject("nomepesquisa", nomepesquisa);
	}
	
	@GetMapping("**/pesquisapessoa")
	public void imprimePDF(@RequestParam("nomepesquisa") String nomepesquisa, @RequestParam("sexo") 
	String sexo, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<Pessoa> pessoas = processaCondicoes(nomepesquisa, sexo, true);
		byte[] pdf = ReportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		response = processarDowloadComoResposta(pdf, response, "application/octet-stream", "relatorio.pdf");	
	}
	
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoasPorPaginacao(@PageableDefault(size = 5) Pageable pageable, 
			@RequestParam("nomepesquisa") String nomepesquisa) {		
		return paginaCadastroPessoa().addObject("pessoas", processaCondicoes(nomepesquisa, null, false, pageable))
				.addObject("nomepesquisa", nomepesquisa);
	}
	
	private HttpServletResponse processarDowloadComoResposta(byte[] arquivo, HttpServletResponse response, String tipoDeConteudo, String nome) throws IOException {
		if(arquivo == null)  {
			mensagemsDeErro.add("Sem documento para baixar :( ");
			retornoDowloadPaginaValida = true;
			return response;
		}
		
		retornoDowloadPaginaValida = false;
		response.setContentLength(arquivo.length);
		response.setContentType(tipoDeConteudo);
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", nome);
		response.setHeader(headerKey, headerValue);
		response.getOutputStream().write(arquivo);
		return response;
	}

	public void erroJpaBindingMessages(BindingResult binding) {
		if (binding.hasErrors()) {
			for (ObjectError object : binding.getAllErrors()) {
				mensagemsDeErro.add(object.getDefaultMessage());
			}			
		}
	}
	
	public Pessoa insereCurriculo(Pessoa pessoa, MultipartFile file) throws IOException {
		if (pessoa.getId() != null) {
			pessoa = pessoaRepository.findById(pessoa.getId()).get();
			if (file.getSize() > 0 ) return atribuirFile(pessoa, file);
		}
		return file.getSize() > 0 ? atribuirFile(pessoa, file): pessoa;		
	}
	
	private Pessoa atribuirFile(Pessoa pessoa, MultipartFile file) throws IOException {
		pessoa.setCurriculo(file.getBytes());	
		pessoa.setTipoFileCurriculo(file.getContentType());
		pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		return pessoa;
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
	
	private List<Pessoa> processaCondicoes(String nomepesquisa, String sexo, boolean escolhaNulloOuTudo) {
		if (pesquisaValida(nomepesquisa, sexo)) return pessoaRepository.findPessoaByNameAndSex(nomepesquisa, sexo);
		
		if(pesquisaNomeValida(nomepesquisa)) return pessoaRepository.findPessoaByName(nomepesquisa);
		
		if (pesquisaSexoValida(sexo)) return pessoaRepository.findPessoaBySex(sexo);
		
		return processaCondicoes(escolhaNulloOuTudo);
	}
	
	private Page<Pessoa> processaCondicoes(String nomepesquisa, String sexo, boolean escolhaNulloOuTudo, Pageable pageable) {
		pesquisaNomeValida(nomepesquisa);
		Page<Pessoa> pessoaPage = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		return pessoaPage;
	}

	private List<Pessoa> processaCondicoes(boolean escolhaNulloOuTudo) {
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		Iterator<Pessoa> it = pessoaRepository.findAll().iterator();
		it.forEachRemaining(pessoa -> pessoas.add(pessoa));
		return (escolhaNulloOuTudo == true) ? pessoas : null;
	}
	
	public ModelAndView paginaCadastroPessoa() {
		ModelAndView andView =  new ModelAndView(telaCadastroPessoa);
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("msg", mensagemsDeErro);
		andView.addObject("profissoes", profissaoRepository.findAll());
		andView.addObject("nomepesquisa", "");
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
