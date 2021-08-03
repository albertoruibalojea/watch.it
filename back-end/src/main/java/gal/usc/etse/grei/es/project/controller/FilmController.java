package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.EmptyAttribute;
import gal.usc.etse.grei.es.project.exceptions.ModifiedAttribute;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.exceptions.RequiredAttribute;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.FilmService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("films")
@Tag(name = "Film API", description = "Film related operations")
@SecurityRequirement(name = "JWT")
public class FilmController {
    private final FilmService films;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public FilmController(FilmService films, LinkRelationProvider relationProvider) {
        this.films = films;
        this.relationProvider = relationProvider;
    }


    //Films

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllFilms",
            summary = "Get the list of films, paginated"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            )
    })
    ResponseEntity<Page<Film>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "keywords", required = false) List<String> keywords,
            @RequestParam(name = "genres", required = false) List<String> genres,
            @RequestParam(name = "cast", required = false) String cast,
            @RequestParam(name = "crew", required = false) String crew,
            @RequestParam(name = "releaseDate.year", required = false) Integer year,
            @RequestParam(name = "releaseDate.month", required = false) Integer month,
            @RequestParam(name = "releaseDate.day", required = false) Integer day
    ){
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if (string.startsWith("+")) {
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Date releaseDate = null;
        if (day != null || month != null || year != null) {
            releaseDate = new Date(day, month, year);
        }

        Optional<Page<Film>> result = films.FILMS_getAll(page, size, Sort.by(criteria), title, cast, crew, releaseDate, keywords, genres);

        if(result.isPresent()) {
            Page<Film> data = result.get();
            Pageable metadata = data.getPageable();

            Link self = linkTo(methodOn(FilmController.class).getAll(page, size, sort, title, keywords, genres, cast, crew, year, month, day)
            ).withSelfRel();

            Link first = linkTo(methodOn(FilmController.class).getAll(metadata.first().getPageNumber(), size, sort, title, keywords, genres, cast, crew, year, month, day)
            ).withRel(IanaLinkRelations.FIRST);

            Link last = linkTo(methodOn(FilmController.class).getAll(data.getTotalPages() - 1, size, sort, title, keywords, genres, cast, crew, year, month, day)
            ).withRel(IanaLinkRelations.LAST);

            Link next = linkTo(methodOn(FilmController.class).getAll(metadata.next().getPageNumber(), size, sort, title, keywords, genres, cast, crew, year, month, day)
            ).withRel(IanaLinkRelations.NEXT);

            Link previous = linkTo(methodOn(FilmController.class).getAll(metadata.previousOrFirst().getPageNumber(), size, sort, title, keywords, genres, cast, crew, year, month, day)
            ).withRel(IanaLinkRelations.PREVIOUS);

            Link one = linkTo(methodOn(FilmController.class).getOne(null)
            ).withRel(relationProvider.getItemResourceRelFor(Film.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(result.get());
        }

        return ResponseEntity.notFound().build();
    }


    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getOneFilm",
            summary = "Get a single film details"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            )
    })
    ResponseEntity<Film> getOne(@Parameter(name = "Film id", required = true)@PathVariable("id") String id) {
        try{
            Optional<Film> result = films.FILMS_getOne(id);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(FilmController.class).getOne(id)).withSelfRel();
                Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.notFound().build();
    }


    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "addFilm",
            summary = "Creates a new film"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request: you must set at least the title",
                    content = @Content
            )
    })
    ResponseEntity<Film> add(@RequestBody @Valid Film film) {
        try {
            Optional<Film> result = films.FILMS_add(film);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(FilmController.class).add(film)).withSelfRel();
                Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
        } catch (RequiredAttribute message) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.notFound().build();
    }


    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "addFilm",
            summary = "Deletes a film"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            )
    })
    ResponseEntity<Film> delete(@Parameter(name = "Film id", required = true) @PathVariable("id") String id) {
        try {
            Optional<Film> result = films.FILMS_delete(id);

            if(result.isPresent()) {
                Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.notFound().build();
    }


    @PatchMapping(
            path = "{id}",
            consumes = "application/json-patch+json"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "modifyFilm",
            summary = "Modifies a film"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request: check your json+patch",
                    content = @Content
            )
    })
    ResponseEntity<Film> modify(@Parameter(name = "Film id", required = true) @PathVariable("id") String id,
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
            Optional<Film> result = films.FILMS_modify(id, json);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(FilmController.class).modify(id, json)).withSelfRel();
                Link all = linkTo(FilmController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
        } catch (ModifiedAttribute message) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (JsonPatchException message){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException message){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.notFound().build();
    }


    //Assessments

    @GetMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllAssessmentsFromFilm",
            summary = "Get the list of Assessments from this film, paginated"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessments details",
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
                    description = "User not found",
                    content = @Content
            )
    })
    ResponseEntity<Page<Assessment>> getAllAssessmentsFilm(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable String id) throws NotFoundAttribute {
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.startsWith("ASC")){
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("DESC")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        try {
            Optional<Page<Assessment>> result = films.ASSESSMENTS_getAllFilm(id, page, size, Sort.by(criteria));

            if (result.isPresent()) {
                Page<Assessment> data = result.get();
                Pageable metadata = data.getPageable();

                Link film = linkTo(methodOn(FilmController.class).getOne(id)).withSelfRel();

                Link first = linkTo(methodOn(FilmController.class).getAllAssessmentsFilm(metadata.first().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.FIRST);

                Link last = linkTo(methodOn(FilmController.class).getAllAssessmentsFilm(data.getTotalPages() - 1, size, sort, id)
                ).withRel(IanaLinkRelations.LAST);

                Link next = linkTo(methodOn(FilmController.class).getAllAssessmentsFilm(metadata.next().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.NEXT);

                Link previous = linkTo(methodOn(FilmController.class).getAllAssessmentsFilm(metadata.previousOrFirst().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.PREVIOUS);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, film.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.notFound().build();
    }

}
