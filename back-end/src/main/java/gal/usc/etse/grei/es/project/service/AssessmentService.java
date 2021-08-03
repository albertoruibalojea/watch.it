package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.EmptyAttribute;
import gal.usc.etse.grei.es.project.exceptions.ModifiedAttribute;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.repository.FilmRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssessmentService {
    private final AssessmentRepository assessments;
    private final FilmRepository films;
    private final UserRepository users;

    @Autowired
    public AssessmentService(AssessmentRepository assessments, FilmRepository films, UserRepository users) {
        this.assessments = assessments;
        this.films = films;
        this.users = users;
    }

    public Optional<Assessment> ASSESSMENTS_add(Assessment assessment) throws EmptyAttribute, NotFoundAttribute {
        if(films.findById(assessment.getFilm().getId()).isPresent()){
            if(users.findById(assessment.getUser().getEmail()).isPresent()){
                if(!assessment.getComment().isEmpty()){
                    assessment.setFilm(new Film().setId(assessment.getFilm().getId()));
                    assessments.insert(assessment);
                    return Optional.of(assessment);

                } else throw new EmptyAttribute("Empty comment");
            } else throw new NotFoundAttribute("User not found");
        } else throw new NotFoundAttribute("Film not found");
    }

    public Optional<Assessment> ASSESSMENTS_delete(String id) throws NotFoundAttribute {
        Optional<Assessment> assessment = assessments.findById(id);

        if(assessment.get().getId().equals(id)){
            assessments.delete(assessment.get());
            return assessment;

        } else throw new NotFoundAttribute("Film not found");
    }

    public Optional<Assessment> ASSESSMENTS_modify(String id, List<Map<String, Object>> json) throws JsonPatchException, ModifiedAttribute, NotFoundAttribute {
        Optional<Assessment> assessment = assessments.findById(id);

        if(assessment.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            PatchUtils patch = new PatchUtils(mapper);
            Assessment newAssessment = patch.patch(assessment.get(), json);

            //Check if id, user and film didn´t change
            if (assessment.get().getId().equals(newAssessment.getId())) {
                if (assessment.get().getUser().equals(newAssessment.getUser())) {
                    if (assessment.get().getFilm().equals(newAssessment.getFilm())) {
                        assessments.save(newAssessment);

                        return assessment;

                    } else throw new ModifiedAttribute("Film can´t change");
                } else throw new ModifiedAttribute("User can´t change");
            } else throw new ModifiedAttribute("Id can´t change");
        } else throw new NotFoundAttribute("Film not found");
    }



    public boolean isMyOwner(String assessmentid, String principal){
        return (assessments.findById(assessmentid).get().getUser().getEmail().equals(principal));
    }
}
