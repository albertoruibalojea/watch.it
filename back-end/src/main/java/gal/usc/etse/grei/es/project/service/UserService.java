package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.AlreadyExistsAttribute;
import gal.usc.etse.grei.es.project.exceptions.ModifiedAttribute;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.exceptions.RequiredAttribute;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.repository.FriendshipRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final FriendshipRepository friendships;
    private final PasswordEncoder encoder;
    private final MongoTemplate mongo;

    @Autowired
    public UserService(UserRepository users, AssessmentRepository assessments, FriendshipRepository friendships, PasswordEncoder encoder, MongoTemplate mongo) {
        this.users = users;
        this.assessments = assessments;
        this.friendships = friendships;
        this.encoder = encoder;
        this.mongo = mongo;
    }


    //Users

    public Optional<Page<User>> USERS_getAll(String email, String name, int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Example<User> filter = null;

        if(!email.isEmpty() && name.isEmpty()){
            filter = Example.of(new User().setEmail(email).setName(null).setBirthday(null).setRoles(null).setCountry(null).setPicture(null).setPassword(null));
        }
        else if(email.isEmpty() && !name.isEmpty()){
            filter = Example.of(new User().setEmail(null).setName(name).setBirthday(null).setRoles(null).setCountry(null).setPicture(null).setPassword(null));
        }
        else if(email.isEmpty() && name.isEmpty()){
            filter = Example.of(new User().setEmail(null).setName(null).setBirthday(null).setRoles(null).setCountry(null).setPicture(null).setPassword(null));
        }
        Page<User> result = users.findAll(filter, request);

        if(result.isEmpty())
            return Optional.empty();

        else result.map(user ->{
            user.setEmail(null);
            user.setRoles(null);
            user.setPassword(null);
            return Optional.of(result);
        });

        return Optional.of(result);
    }

    public Optional<User> USERS_getOne(String email) throws NotFoundAttribute {
        if(users.findById(email).isPresent()){
            Optional<User> user = users.findById(email);
            user.get().setPassword("");
            return user;
        } else throw new NotFoundAttribute("User not present in database.");
    }

    public Optional<User> USERS_add(User user) throws AlreadyExistsAttribute, RequiredAttribute {
        if (users.findById(user.getEmail()).isPresent()){
            throw new AlreadyExistsAttribute("User already exists");
        } else {
            if(!user.getEmail().isEmpty() || (user.getEmail() == null)) {
                if(!user.getName().isEmpty()){
                    if(user.getBirthday() != null){
                        user.setPassword(encoder.encode(user.getPassword()));
                        users.insert(user);
                        return Optional.of(user);
                    } else throw new RequiredAttribute("Birthday not present in petition");
                } else throw new RequiredAttribute("Name not present in petition");
            } else throw new RequiredAttribute("Email not present in petition");
        }
    }

    public Optional<User> USERS_delete(String email) throws NotFoundAttribute {
        Optional<User> user = users.findById(email);

        if(user.isPresent()){
            users.delete(user.get());

            //We also need to delete friendships and comments
            List<Friendship> friendshipsOf = friendships.findWhereAmI(user.get().getEmail());
            for(Friendship f : friendshipsOf){
                friendships.delete(f);
            }

            Optional<User> user2 = Optional.of(new User());
            user2.get().setEmail(email);
            List<Assessment> assessmentsOf = assessments.findWhereAmI(user2.get());
            for(Assessment a : assessmentsOf){
                assessments.delete(a);
            }

            return user;

        } else throw new NotFoundAttribute("User not found");
    }

    public Optional<User> USERS_modify(String email, List<Map<String, Object>> json) throws JsonPatchException, ModifiedAttribute, NotFoundAttribute {
        Optional<User> user = users.findById(email);

        if(user.isPresent()){
            ObjectMapper mapper = new ObjectMapper();
            PatchUtils patch = new PatchUtils(mapper);
            User newUser = patch.patch(user.get(), json);

            //Check if email and birthday didn´t change
            if(user.get().getEmail().equals(newUser.getEmail())){
                if(user.get().getBirthday().equals(newUser.getBirthday())){
                    users.save(newUser);
                    return Optional.of(newUser);

                } else throw new ModifiedAttribute("Birthday can´t change");
            } else throw new ModifiedAttribute("Email can´t change");
        } else throw new NotFoundAttribute("User not found");
    }


    //Friendships

    public Optional<Page<Friendship>> FRIENDSHIPS_getAll(String user_email, int page, int size, Sort sort) throws NotFoundAttribute {
        Pageable request = PageRequest.of(page, size, sort);

        if(users.findById(user_email).isPresent()){
            Criteria criteria = new Criteria();
            criteria.orOperator(Criteria.where("user").is(user_email),Criteria.where("friend").is(user_email));
            Query query = Query.query(criteria).with(request);

            List<Friendship> result = mongo.find(query, Friendship.class);
            return Optional.of(new PageImpl<>(result, PageRequest.of(page, size, sort), result.size()));

        } else throw new NotFoundAttribute("User not found");
    }

    public Optional<Friendship> FRIENDSHIPS_add(String user_email, Friendship newFriendship) throws AlreadyExistsAttribute, NotFoundAttribute {
        if(users.findById(user_email).isPresent()){
            if(users.findById(newFriendship.getFriend()).isPresent()){

                if(friendships.findByUserOrFriend(user_email, newFriendship.getFriend())==null){
                    newFriendship.setUser(user_email);
                    newFriendship.setConfirmed(false);
                    friendships.insert(newFriendship);
                    return Optional.of(newFriendship);
                }
                else throw new AlreadyExistsAttribute("Friendship already exists");

            } else throw new NotFoundAttribute("User friend not found");
        } else throw new NotFoundAttribute("This email doesn´t belong to any user");
    }

    public Optional<Friendship> FRIENDSHIPS_delete(String user_email, String friendshipid) throws AlreadyExistsAttribute, NotFoundAttribute {
        if(users.findById(user_email).isPresent()){
            Optional<Friendship> friendship = friendships.findById(friendshipid);

            if(friendship != null){
                friendships.delete(friendship.get());
                return friendship;
            }
            else throw new AlreadyExistsAttribute("Friendship already exists");

        } else throw new NotFoundAttribute("This email doesn´t belong to any user");
    }


    public boolean areFriends(String user, String friend){
        if(friendships.findByUserOrFriend(user, friend) != null){
            return true;
        } else return false;
    }


    //Assessments

    public Optional<Page<Assessment>> ASSESSMENTS_getAllUser(String email, int page, int size, Sort sort) throws NotFoundAttribute {
        if(users.findById(email).isPresent()){
            Pageable request = PageRequest.of(page, size, sort);
            Example<Assessment> filter = Example.of(new Assessment().setUser(new User().setEmail(email)).setId(null).setFilm(null).setComment(null).setRating(null));
            Page<Assessment> result = assessments.findAll(filter, request);

            if(result.isEmpty())
                return Optional.empty();
            else return Optional.of(result);

        } else throw new NotFoundAttribute("User not found");
    }
}
