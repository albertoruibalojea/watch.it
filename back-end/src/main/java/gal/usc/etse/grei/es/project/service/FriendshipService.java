package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.repository.FriendshipRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FriendshipService {
    private final FriendshipRepository friendships;
    private final UserRepository users;

    public FriendshipService(FriendshipRepository friendships, UserRepository users) {
        this.friendships = friendships;
        this.users = users;
    }

    public Optional<Friendship> FRIENDSHIPS_getOne(String friendship) throws NotFoundAttribute {
        if(friendships.findById(friendship).isPresent()){
            return Optional.of(friendships.findById(friendship).get());
        } else throw new NotFoundAttribute("Friendship not found");
    }

    public Optional<Friendship> FRIENDSHIPS_accept(String friendshipid, List<Map<String, Object>> json) throws JsonPatchException {
        Optional<Friendship> friendship = friendships.findById(friendshipid);

        ObjectMapper mapper = new ObjectMapper();
        PatchUtils patch = new PatchUtils(mapper);
        Friendship newFriendship = patch.patch(friendship.get(), json);

        if(newFriendship.getConfirmed()){
            newFriendship.setSince(this.getDate());
        }

        friendships.save(newFriendship);
        return Optional.of(newFriendship);
    }

    public boolean areYouTheFriend(String friendshipid, String principal){
        Optional<Friendship> friendship = friendships.findById(friendshipid);
        if(friendship.get().getFriend().equals(principal))
            return true;
        else return false;
    }


    //Metodos auxiliares para usar clases del modelo
    public Date getDate(){
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String datetext = localDate.format(formatter);

        String[] parts = datetext.split("-");
        Date date = new Date();
        date.setDay(Integer.getInteger(parts[0]));
        date.setMonth(Integer.getInteger(parts[1]));
        date.setYear(Integer.getInteger(parts[2]));

        return date;
    }
}
