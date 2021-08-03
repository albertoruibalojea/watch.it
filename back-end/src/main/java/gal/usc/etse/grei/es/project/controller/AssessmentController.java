package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.EmptyAttribute;
import gal.usc.etse.grei.es.project.exceptions.ModifiedAttribute;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("assessments")
@Tag(name = "Assessment API", description = "Assessment related operations")
@SecurityRequirement(name = "JWT")
public class AssessmentController {
    private final AssessmentService assessments;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public AssessmentController(AssessmentService assessments, LinkRelationProvider relationProvider) {
        this.assessments = assessments;
        this.relationProvider = relationProvider;
    }


    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "addAssessment",
            summary = "Creates a new assessment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessment details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request: Empty comment",
                    content = @Content
            )
    })
    ResponseEntity<Assessment> add(@RequestBody @Valid Assessment assessment) {
        try {
            Optional<Assessment> result = assessments.ASSESSMENTS_add(assessment);

            if (result.isPresent()) {
                Link film = linkTo(methodOn(FilmController.class).getOne(assessment.getFilm().getId())).withSelfRel();
                Link filmAssessments = linkTo(methodOn(FilmController.class).getAllAssessmentsFilm(0, 20, null, assessment.getFilm().getId())).withSelfRel();

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, film.toString())
                        .header(HttpHeaders.LINK, filmAssessments.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (EmptyAttribute message){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.notFound().build();
    }


    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@assessmentService.isMyOwner(#id, principal) or hasRole('ADMIN')")
    @Operation(
            operationId = "deleteAssessment",
            summary = "Deletes an assessment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessment details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or film not found",
                    content = @Content
            )
    })
    ResponseEntity<Assessment> delete(@Parameter(name = "Assessment id", required = true) @PathVariable("id") String id){
        try {
            Optional<Assessment> result = assessments.ASSESSMENTS_delete(id);

            if (result.isPresent()) {
                Link userAssessments = linkTo(methodOn(UserController.class).getAllAssessmentsUser(0, 20, null, result.get().getUser().getEmail())).withSelfRel();
                Link filmAssessments = linkTo(methodOn(FilmController.class).getAllAssessmentsFilm(0, 20, null, id)).withSelfRel();

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, userAssessments.toString())
                        .header(HttpHeaders.LINK, filmAssessments.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.notFound().build();
    }


    @PatchMapping(
            path = "{id}",
            consumes = "application/json-patch+json"
    )
    @PreAuthorize("@assessmentService.isMyOwner(#id, principal)")
    @Operation(
            operationId = "addAssessment",
            summary = "Creates a new assessment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessment details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request: You can only modify the comment",
                    content = @Content
            )
    })
    ResponseEntity<Assessment> modify(@Parameter(name = "Assessment id", required = true) @PathVariable("id") String id,
                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                              description = "Modification patch operations to be executed",
                                              content = @Content(
                                                      mediaType = "application/json-patch+json",
                                                      schema = @Schema(type = "object"),
                                                      examples = @ExampleObject(
                                                              value = "[{\"op\": \"replace\",\"path\": \"name\", \"value\": \"test\"}]"
                                                      )
                                              )
                                      )
                                      @RequestBody List<Map<String, Object>> json) {
        try {
            Optional<Assessment> result = assessments.ASSESSMENTS_modify(id, json);

            if (result.isPresent()) {
                Link self = linkTo(methodOn(AssessmentController.class).modify(id, json)).withSelfRel();
                Link userAssessments = linkTo(methodOn(UserController.class).getAllAssessmentsUser(0, 20, null, result.get().getUser().getEmail())).withSelfRel();
                Link filmAssessments = linkTo(methodOn(FilmController.class).getAllAssessmentsFilm(0, 20, null, id)).withSelfRel();

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, userAssessments.toString())
                        .header(HttpHeaders.LINK, filmAssessments.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (ModifiedAttribute message){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (JsonPatchException message){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.notFound().build();
    }
}
