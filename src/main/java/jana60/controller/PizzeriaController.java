package jana60.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jana60.model.ImageForm;
import jana60.model.Ingredient;
import jana60.model.Pizza;
import jana60.repository.IngredientRepository;
import jana60.repository.PizzaRepository;
import jana60.service.ImageService;

@Controller
@RequestMapping("/")
public class PizzeriaController {

	@Autowired
	private PizzaRepository repo;
	
	@Autowired
	private IngredientRepository ingRepo;
	
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
	
	@GetMapping("/ingredients")
	public String ingredients(Model model)
	{
		model.addAttribute("ingList",ingRepo.findAllByOrderByName());
		model.addAttribute("newIngredient", new Ingredient());
		return "/ingredient/list";
	}
	
	@GetMapping("/pizzas/add")
	public String pizzaForm(Model model) {
	    model.addAttribute("pizza", new Pizza());
	    model.addAttribute("ingList", ingRepo.findAllByOrderByName());
	    model.addAttribute("imageForm", new ImageForm());
	    return "/pizza/add";
	  }
	
	@PostMapping("/pizzas/add") //sezione di aggiunta della pizza al database
	  public String save(@Valid @ModelAttribute("pizza") Pizza formPizza, BindingResult br, Model model) {
		
		boolean hasErrors = br.hasErrors();
		boolean validateName = true;
		
		if(formPizza.getId() != null) //vedo se la pizza inserita da form ha un id
		{
			Pizza notUpdatedPizza = repo.findById(formPizza.getId()).get(); //se lo ha la raccolgo dal DB
			if (notUpdatedPizza.getName().equalsIgnoreCase(formPizza.getName())) //se ha un nome uguale a quella presente nel DB
				validateName= false; //non richiedo il controllo del nome
		}
		
		if(validateName && repo.countByName(formPizza.getName()) > 0)
		{
			br.addError(new FieldError("pizza", "name", "I nomi delle pizze devono essere unici"));
		     hasErrors = true;
		}
				
		if (hasErrors) 
		{
		    model.addAttribute("ingList", ingRepo.findAllByOrderByName());
			return "/pizza/add";
	    } 
		else 
		{
	    	try
	    	{
	    		repo.save(formPizza);
	    	}
	    	catch (Exception e)
	    	{
	    		model.addAttribute("errorMessage", "Non è possibile salvare la pizza inserita");
	    	    model.addAttribute("ingList", ingRepo.findAllByOrderByName());
	    		return "/pizza/add";
	    	}
	      return "redirect:/pizzas"; 
	    }
	  }
	
	@PostMapping("ingredients/save")
	  public String saveIngredient(@Valid @ModelAttribute("newIngredient") Ingredient formIngredient,
	      BindingResult br, Model model) {
	    if (br.hasErrors()) {
	      // ricarico la pagina
	      model.addAttribute("ingList", ingRepo.findAllByOrderByName());
	      return "ingredient/list";

	    } else {
	      // salvo la category
	      ingRepo.save(formIngredient);
	      return "redirect:/ingredients";
	    }

	  }
	
	@GetMapping("/pizzas/edit/{id}")
	public String edit(@PathVariable("id") Integer pizzaId, Model model)
	{
		Optional<Pizza> result = repo.findById(pizzaId); //noi cerchiamo la pizza collegata all'id che diamo tramite url tra le pizze che ha nel DB
		
		if(result.isPresent())
		{
			model.addAttribute("pizza", result.get());
		    model.addAttribute("ingList", ingRepo.findAllByOrderByName());
		    model.addAttribute("imageForm", service.createImageForm(pizzaId));
			return "/pizza/add";
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Questa pizza non è presente nel menù");
		}
	}
	
	@GetMapping("pizzas/{id}")
	public String detail (@PathVariable("id") Integer pizzaId, Model model)
	{
		
		Optional<Pizza> result = repo.findById(pizzaId);
		
		if(result.isPresent())
		{
			Pizza modelPizza = result.get();
			model.addAttribute("pizza", modelPizza);
		    return "/pizza/detail";
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Questa pizza non è presente nel menù");
		}
		
	}
	
	@GetMapping("/pizzas/delete/{id}")
	  public String delete(@PathVariable("id") Integer pizzaId, RedirectAttributes ra) {
	    Optional<Pizza> result = repo.findById(pizzaId);
	    if (result.isPresent()) {
	      // repo.deleteById(bookId);
	      result.get().setActive(false);
	      repo.save(result.get());
	      ra.addFlashAttribute("successMessage", "Pizza" + result.get().getName() + " cancellata!");
	      return "redirect:/pizzas";
	    } else {
	      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
	          "La pizza numero " + pizzaId + " non è presente nel menù");
	    }
	  }
	
	
	// Mappatura della ricerca
	
	@GetMapping("/search")
	  public String search(@RequestParam(name = "queryName") String queryName, Model model) {

	    if (queryName != null && queryName.isEmpty()) {
	    	queryName = null;
	    }

	    List<Pizza> pizzas = repo.findByNameContainingIgnoreCase(queryName);
	    model.addAttribute("pizzaList", pizzas);
	    return "/pizza/list";
	  }
	
	@GetMapping("/advanced_search")
	  public String advancedSearch() {
	    return "search";
	  }
	
	//mappatura immagini
	
	@Autowired
	  private ImageService service;
	
	@PostMapping("image/save/{id}") //faccio il form di salvataggio immagini solo dentro la pagina di edit pizze
	  public String saveImage(@PathVariable("id") Integer pizzaId, @ModelAttribute("imageForm") ImageForm imageForm) {
	    // devo salvare l'immagine su database
	    try {
	    	imageForm.setPizza(repo.findById(pizzaId).get());
	      service.createImage(imageForm);
	      return "redirect:/pizzas/edit/" + pizzaId;
	    } catch (IOException e) {
	      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save image");
	    }
	  }
	
	/*
	 * Controller che in base all'id dell'Image restituisce il contenuto
	 */
	@RequestMapping(value = "image/{imageId}/content", produces = MediaType.IMAGE_JPEG_VALUE)
	  public ResponseEntity<byte[]> getImageContent(@PathVariable("imageId") Integer imageId) {
	    // recupero il content dal database
	    byte[] content = service.getImageContent(imageId);
	    // preparo gli headers della response con il tipo di contenuto
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_JPEG);
	    // ritorno il contenuto, gli headers e lo status http
	    return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
	  }
	
	  
}
