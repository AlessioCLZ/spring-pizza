package jana60.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import jana60.model.security.User;

public interface UserRepository extends CrudRepository<User, Integer> {

	public Optional<User> findByUsername(String username);
}
