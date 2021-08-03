package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

}
