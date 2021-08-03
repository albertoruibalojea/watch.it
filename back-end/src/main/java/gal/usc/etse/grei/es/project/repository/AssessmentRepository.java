package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AssessmentRepository extends MongoRepository<Assessment, String> {
    @Query("{user : ?0}")
    public List<Assessment> findWhereAmI(User user);
}
