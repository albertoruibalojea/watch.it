package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.EmptyAttribute;
import gal.usc.etse.grei.es.project.exceptions.ModifiedAttribute;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.exceptions.RequiredAttribute;
import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FilmService {
    private final FilmRepository films;
    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final MongoTemplate mongo;

    @Autowired
    public FilmService(FilmRepository films, UserRepository users, AssessmentRepository assessments, MongoTemplate mongo) {
        this.films = films;
        this.users = users;
        this.assessments = assessments;
        this.mongo = mongo;
    }

    public Optional<Page<Film>> FILMS_getAll(int page, int size, Sort sort, String title, String cast, String crew, Date releaseDate, List<String> keywords, List<String> genres) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Film> result;

        if (title!=null ||cast != null || crew != null || releaseDate != null || keywords != null || genres != null) {
            Query query = new Query();
            if(title!=null){
                query.addCriteria(Criteria.where("title").regex(title));
            }

            if (cast != null) {
                query.addCriteria(Criteria.where("cast").elemMatch(Criteria.where("name").is(cast)));
            }

            if (crew != null) {
                query.addCriteria(Criteria.where("crew").elemMatch(Criteria.where("name").is(crew)));
            }

            if (releaseDate != null) {
                if (releaseDate.getYear() != null) {
                    query.addCriteria(Criteria.where("releaseDate.year").is(releaseDate.getYear()));
                }
                if (releaseDate.getMonth() != null) {
                    query.addCriteria(Criteria.where("releaseDate.month").is(releaseDate.getMonth()));
                }
                if (releaseDate.getDay() != null) {
                    query.addCriteria(Criteria.where("releaseDate.day").is(releaseDate.getDay()));
                }
            }

            if (keywords != null) {
                query.addCriteria(Criteria.where("keywords").in(keywords));
            }

            if (genres != null) {
                query.addCriteria(Criteria.where("genres").in(genres));
            }

            result = PageableExecutionUtils.getPage(
                    mongo.find(Query.of(query).with(request), Film.class),
                    request, () -> mongo.count(query, Film.class)
            );

        } else {
            result = films.findAll(request);
        }


        if (result.isEmpty())
            return Optional.empty();

        else result.map(film ->{
            film.setCast(null);
            film.setCollection(null);
            film.setCrew(null);
            film.setComments(null);
            film.setKeywords(null);
            film.setTagline(null);
            film.setProducers(null);
            film.setStatus(null);
            film.setRuntime(null);
            film.setRevenue(null);
            return Optional.of(result);
        });

        return Optional.of(result);
    }

    public Optional<Film> FILMS_getOne(String id) throws NotFoundAttribute {
        if(films.findById(id).isPresent()){
            return films.findById(id);
        } else throw new NotFoundAttribute("ID not present in petition.");
    }

    public Optional<Film> FILMS_add(Film film) throws RequiredAttribute {
        if(!film.getTitle().isEmpty()){
            films.insert(film);
            return Optional.of(film);
        } else throw new RequiredAttribute("Film title must not be empty");
    }

    public Optional<Film> FILMS_delete(String id) throws NotFoundAttribute {
        Optional<Film> film = films.findById(id);
        if(film.isPresent()) {
            films.delete(film.get());
            return film;
        } else throw new NotFoundAttribute("Film not found");
    }

    public Optional<Film> FILMS_modify(String id, List<Map<String, Object>> json) throws JsonProcessingException, JsonPatchException, ModifiedAttribute {
        Optional<Film> film = films.findById(id);

        if(film.isPresent()){
            ObjectMapper mapper = new ObjectMapper();
            PatchUtils patch = new PatchUtils(mapper);
            Film newFilm = patch.patch(film.get(), json);

            //Avoid changing the id
            newFilm.setId(id);
            films.save(newFilm);
            return Optional.of(newFilm);

        } else throw new ModifiedAttribute("Id can´t change");
    }


    //Assessments

    public Optional<Page<Assessment>> ASSESSMENTS_getAllFilm(String id, int page, int size, Sort sort) throws NotFoundAttribute {
        if(films.findById(id).isPresent()){
            Pageable request = PageRequest.of(page, size, sort);
            Example<Assessment> filter = Example.of(new Assessment().setUser(null).setId(null).setFilm(new Film().setId(id)).setComment(null).setRating(null));
            Page<Assessment> result = assessments.findAll(filter, request);

            if(result.isEmpty())
                return Optional.empty();
            else return Optional.of(result);

        } else throw new NotFoundAttribute("Film not found");
    }


    //Getters

    public FilmRepository getFilms() {
        return films;
    }
}
