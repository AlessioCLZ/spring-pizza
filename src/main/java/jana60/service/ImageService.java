package jana60.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jana60.model.Image;
import jana60.model.ImageForm;
import jana60.model.Pizza;
import jana60.repository.ImageRepository;
import jana60.repository.PizzaRepository;

/*
 * Visto che c'è molta logica invece di usare direttamente i repository nel controller
 * creiamo una classe service che espone i metodi che ci servono e si occupa della logica
 */

@Service
public class ImageService {
	
	@Autowired
	private ImageRepository repo;
	
	@Autowired
	private PizzaRepository pizzaRepo;
	
	 /*
	   * A partire dal pizzaId cerco tutte le immagini associate
	*/
	public List<Image> getImagesByPizzaId(Integer pizzaId)
	{
		Pizza pizza = pizzaRepo.findById(pizzaId).get();
		return repo.findByPizza(pizza);
	}
	
	/*
	   * A partire dal pizzaId creo un'istanza di ImageForm associata
	   * a quella pizza
	   */
	
	public ImageForm createImageForm(Integer pizzaId)
	{
		Pizza pizza = pizzaRepo.findById(pizzaId).get();
		ImageForm img = new ImageForm();
		img.setPizza(pizza);
		return img;
				
	}
	
	/*
	   * A partire da un oggetto ImageForm deve creare un oggetto di tipo Image
	   * e salvarlo su database
	   */
	
	public Image createImage(ImageForm imageForm) throws IOException {
	    // creo un oggetto Image vuoto
	    Image imgToSave = new Image();
	    // lo inizializzo coi dati di ImageForm
	    imgToSave.setPizza(imageForm.getPizza());
	    // trasformo il content MultipartFile in un byte[] e lo passo all'Image da salvare
	    if (imageForm.getContentMultipart() != null) {
	      byte[] contentSerialized = imageForm.getContentMultipart().getBytes();
	      imgToSave.setContent(contentSerialized);
	    }
	    // salvo Image su database
	    return repo.save(imgToSave);
	  }
	
	/* a partire dall'id dell'Image restituisce il content */
	  public byte[] getImageContent(Integer id) {
	    Image img = repo.findById(id).get();
	    return img.getContent();
	  }
	
}
