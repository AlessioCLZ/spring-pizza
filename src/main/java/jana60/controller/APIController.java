package jana60.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jana60.model.Image;
import jana60.model.Pizza;
import jana60.repository.ImageRepository;
import jana60.repository.IngredientRepository;
import jana60.repository.PizzaRepository;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class APIController {

	@Autowired
	private PizzaRepository pizzaRepo;
	
	@Autowired
	private IngredientRepository ingRepo;
	
	@Autowired
	private ImageRepository imgRepo;
	
	//mappatura e crud delle pizze
	
	@GetMapping("/pizzas") //all items getter
	public List<Pizza> getAll()
	{
		return (List<Pizza>) pizzaRepo.findAll();
	}
	
	@GetMapping("/pizzas/{id}") //single item getter
	public Pizza getById(@PathVariable Integer id)
	{
		Optional<Pizza> pizza = pizzaRepo.findById(id);
		if(pizza.isPresent()) 
			return pizza.get();
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pizza non trovata");
	}
	
	@PostMapping
	public Pizza newPizza (@Valid @RequestBody Pizza pizza)
	{
		try {
			return pizzaRepo.save(pizza);
		}catch(Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossibile aggiungere la pizza al menù");
		}
	}
	
	@DeleteMapping("/pizzas/{id}")
	public String deletePizzaById(@PathVariable Integer id)
	{
		Optional<Pizza> pizzaToDelete = pizzaRepo.findById(id);
		if(pizzaToDelete.isPresent())
		{
			Pizza pizzaToWorkOn = pizzaToDelete.get();
			
			pizzaToWorkOn.setIngredients(null); //cancello il collegamento della pizza agli ingredienti e faccio update del DB
			pizzaRepo.save(pizzaToWorkOn);
			
			deleteImageByPizzaId(id);
			
			pizzaRepo.deleteById(id);
			
			return "Pizza con id "+ id + " cancellata";
			
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pizza non trovata");
		}
	}

	@DeleteMapping("/pizzas/{id}/images")
	private void deleteImageByPizzaId(@PathVariable Integer pizzaId) {
		Optional<Pizza> optionalPizza = pizzaRepo.findById(pizzaId);
		if(optionalPizza.isPresent())
		{
			List<Image> imageListToDelete = optionalPizza.get().getImages();
			for(Image img : imageListToDelete)
			{
				img.setPizza(null);
				imgRepo.delete(img);
			}
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pizza non trovata");
		}
	}
	
	
}
