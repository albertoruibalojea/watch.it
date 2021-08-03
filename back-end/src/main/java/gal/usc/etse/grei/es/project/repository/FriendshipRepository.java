package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Friendship;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface FriendshipRepository extends MongoRepository<Friendship, String> {
    @Query("{'$or' : [{user : ?0}, {friend : ?0}]}")
    public List<Friendship> findWhereAmI(String user);

    @Query("{'$or' : [{user : ?0, friend : ?1}, {friend : ?0, user : ?1}]}")
    public Friendship findByUserOrFriend(String user, String friend);

    @Query("{user : ?0, friend : ?1}")
    public Friendship findByFriendship(String user, String friend);
}
