package jana60.repository;

import org.springframework.data.repository.CrudRepository;
import jana60.model.Pizza;
import java.util.List;
import jana60.model.Image;

public interface ImageRepository extends CrudRepository<Image, Integer> {
	
	public List<Image> findByPizza(Pizza pizza);
}
