package jana60.repository;

import org.springframework.data.repository.CrudRepository;

import jana60.model.Pizza;


public interface PizzaRepository extends CrudRepository<Pizza, Integer>{

	public Integer countByName(String name);
	
	
}
