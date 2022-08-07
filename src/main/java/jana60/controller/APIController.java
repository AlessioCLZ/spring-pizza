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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jana60.model.Image;
import jana60.model.Ingredient;
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
	public Pizza getPizzaById(@PathVariable Integer id)
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
			
			deleteImagesByPizzaId(id);
			
			pizzaRepo.deleteById(id);
			
			return "Pizza con id "+ id + " cancellata";
			
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pizza non trovata");
		}
	}

	@DeleteMapping("/pizzas/{pizzaId}/images")
	private void deleteImagesByPizzaId(@PathVariable Integer pizzaId) {
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
	
	@PutMapping("/pizzas/{id}")
	public Pizza update (@PathVariable Integer id, @Valid @RequestBody Pizza pizza)
	{
		Optional<Pizza> updatePizza = pizzaRepo.findById(id);
		
		if(updatePizza.isPresent())
		{
			pizza.setId(id);
			return pizzaRepo.save(pizza);			
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pizza non trovata");
		}
	}
	
	
	@GetMapping("/ingredients/{id}") //single item getter
	public Ingredient getIngredientById(@PathVariable Integer id)
	{
		Optional<Ingredient> ingredient = ingRepo.findById(id);
		if(ingredient.isPresent()) 
			return ingredient.get();
		else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingrediente non trovato");
	}
	
	@PostMapping
	public Ingredient newIngredient (@Valid @RequestBody Ingredient ingredient)
	{
		try {
			return ingRepo.save(ingredient);
		}catch(Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossibile aggiungere l'ingrediente alla lista");
		}
	}
	
	@DeleteMapping("/ingredients/{id}")
	public String deleteIngredientById (@PathVariable Integer id)
	{
		Optional<Ingredient> ingredientToDelete = ingRepo.findById(id);
		if (ingredientToDelete.isPresent())
		{
			ingredientToDelete.get().setPizzas(null);
			ingRepo.save(ingredientToDelete.get());	
			ingRepo.deleteById(id);
			return "Ingrediente con id " +id+ " cancellato";
		}
		else
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingrediente non trovato");
		}
		
	}
}
