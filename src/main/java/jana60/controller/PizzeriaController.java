package jana60.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jana60.model.Pizza;
import jana60.repository.PizzaRepository;

@Controller
@RequestMapping("/")
public class PizzeriaController {

	@Autowired
	private PizzaRepository repo;
	
	@GetMapping
	public String index (Model model)
	{
		return "index";
	}
	
	@GetMapping("/pizzas")
	public String pizzas(Model model)
	{
		List<Pizza> pizzaList = (List<Pizza>) repo.findAll();
		model.addAttribute("pizzaList", pizzaList);
		return "/pizza/list";
	}
	
	@GetMapping("/pizzas/add")
	public String pizzaForm(Model model) {
	    model.addAttribute("pizza", new Pizza());
	    return "/pizza/add";
	  }
	
	@PostMapping("/pizzas/add") //sezione di aggiunta della pizza al database
	  public String save(@Valid @ModelAttribute("pizza") Pizza formPizza, BindingResult br) {
	    // testo se ci sono errori di validazione
	    if (br.hasErrors()) {
	      // se ci sono errori non salvo il book su database ma ritorno alla form precaricata
	      return "/pizzas/add";
	    } else {
	      // se non ci sono errori salvo il book che arriva dalla form
	      repo.save(formPizza);
	      return "redirect:/pizzas"; // non cercare un template, ma fai la HTTP redirect a quel path
	    }
	  }
}
