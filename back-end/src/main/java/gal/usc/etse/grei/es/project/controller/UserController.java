package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.AlreadyExistsAttribute;
import gal.usc.etse.grei.es.project.exceptions.ModifiedAttribute;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.exceptions.RequiredAttribute;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
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
@RequestMapping("users")
@Tag(name = "User API", description = "User related operations")
@SecurityRequirement(name = "JWT")
public class UserController {
    private final UserService users;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public UserController(UserService users, LinkRelationProvider relationProvider) {
        this.users = users;
        this.relationProvider = relationProvider;
    }


    //Users

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllUsers",
            summary = "Get the list of users, paginated"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            )
    })
    ResponseEntity<Page<User>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "name", defaultValue = "") String name
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.equals("ASC") || string.equals("ASC.name") || string.isEmpty()){
                return Sort.Order.asc("name");
            } else if (string.equals("DESC") || string.equals("DESC.name")) {
                return Sort.Order.desc("name");
            } else if(string.equals("ASC.country")){
                return Sort.Order.asc("country");
            } else if (string.equals("DESC.country")) {
                return Sort.Order.desc("country");
            } else if(string.equals("ASC.picture")){
                return Sort.Order.asc("picture");
            } else if (string.equals("DESC.picture")) {
                return Sort.Order.desc("picture");
            } else if(string.equals("ASC.birthday")){
                return Sort.Order.asc("birthday");
            } else if (string.equals("DESC.birthday")) {
                return Sort.Order.desc("birthday");
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Optional<Page<User>> result = users.USERS_getAll(email, name, page, size, Sort.by(criteria));

        if(result.isPresent()) {
            Page<User> data = result.get();
            Pageable metadata = data.getPageable();

            Link self = linkTo(methodOn(UserController.class).getAll(page, size, sort, email, name)
            ).withSelfRel();

            Link first = linkTo(methodOn(UserController.class).getAll(metadata.first().getPageNumber(), size, sort, email, name)
            ).withRel(IanaLinkRelations.FIRST);

            Link last = linkTo(methodOn(UserController.class).getAll(data.getTotalPages() - 1, size, sort, email, name)
            ).withRel(IanaLinkRelations.LAST);

            Link next = linkTo(methodOn(UserController.class).getAll(metadata.next().getPageNumber(), size, sort, email, name)
            ).withRel(IanaLinkRelations.NEXT);

            Link previous = linkTo(methodOn(UserController.class).getAll(metadata.previousOrFirst().getPageNumber(), size, sort, email, name)
            ).withRel(IanaLinkRelations.PREVIOUS);

            Link one = linkTo(methodOn(UserController.class).getOne(null)
            ).withRel(relationProvider.getItemResourceRelFor(User.class));

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
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal or @userService.areFriends(#email, principal) or hasRole('ADMIN')")
    @Operation(
            operationId = "getOneUser",
            summary = "Get a single user details",
            description = "Get the details for a given user. To see the user details " +
                    "you must be the requested user, his friend, or have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            )
    })
    ResponseEntity<User> getOne(@Parameter(description = "User email", required = true) @PathVariable("email") String email) {
        try{
            Optional<User> result = users.USERS_getOne(email);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getOne(email)).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

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
    @Operation(
            operationId = "createUser",
            summary = "Creates a new user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict: this user already exists or there are missing attributes",
                    content = @Content
            ),
    })
    ResponseEntity<User> add(@RequestBody @Valid User user){
        try{
            Optional<User> result = users.USERS_add(user);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).add(user)).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
        } catch (AlreadyExistsAttribute | RequiredAttribute message){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return ResponseEntity.notFound().build();
    }


    @DeleteMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "deleteUser",
            summary = "Deletes an user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
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
    @PreAuthorize("#email == principal")
    ResponseEntity<User> delete(@Parameter(name = "User email", required = true) @PathVariable("email") String email) {
        try{
            Optional<User> result = users.USERS_delete(email);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).delete(email)).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

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


    @PatchMapping(
            path = "{email}",
            consumes = "application/json-patch+json",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal")
    @Operation(
            operationId = "modifyUser",
            summary = "Modifies an user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict: you can´t change email or birthday",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Unprocessable request",
                    content = @Content
            )
    })
    ResponseEntity<User> modify(@Parameter(name = "User email", required = true) @PathVariable("email") String email,
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
        if(json.stream().anyMatch(op -> op.get("path").toString().contains("email") || op.get("path").toString().contains("birthday"))) {
            try {
                Optional<User> result = users.USERS_modify(email, json);

                if (result.isPresent()) {
                    Link self = linkTo(methodOn(UserController.class).modify(email, json)).withSelfRel();
                    Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

                    return ResponseEntity.ok()
                            .header(HttpHeaders.LINK, self.toString())
                            .header(HttpHeaders.LINK, all.toString())
                            .body(result.get());
                }
            } catch (NotFoundAttribute message) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (ModifiedAttribute message) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            } catch (JsonPatchException message) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return ResponseEntity.notFound().build();
        } else return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
    }




    //Friendships

    @GetMapping(
            path = "{email}/friendships",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal")
    @Operation(
            operationId = "getAllFriendships",
            summary = "Get the list of friendships, paginated"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The friendship details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship not found",
                    content = @Content
            )
    })
    ResponseEntity<Page<Friendship>> getAllFriendships(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable String email) {
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
            Optional<Page<Friendship>> result = users.FRIENDSHIPS_getAll(email, page, size, Sort.by(criteria));

            if (result.isPresent()) {
                Page<Friendship> data = result.get();
                Pageable metadata = data.getPageable();

                Link self = linkTo(methodOn(UserController.class).getAllFriendships(page, size, sort, email)).withSelfRel();

                Link first = linkTo(methodOn(UserController.class).getAllFriendships(metadata.first().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.FIRST);

                Link last = linkTo(methodOn(UserController.class).getAllFriendships(data.getTotalPages() - 1, size, sort, email)
                ).withRel(IanaLinkRelations.LAST);

                Link next = linkTo(methodOn(UserController.class).getAllFriendships(metadata.next().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.NEXT);

                Link previous = linkTo(methodOn(UserController.class).getAllFriendships(metadata.previousOrFirst().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.PREVIOUS);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
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


    @PostMapping(
            path = "{email}/friendships",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal or @userService.areFriends(#email, principal)")
    @Operation(
            operationId = "createFriendship",
            summary = "Creates a new friendship"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The friendship details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict: this friendship already exists",
                    content = @Content
            ),
    })
    ResponseEntity<Friendship> addFriendship(@Parameter(name = "User email", required = true) @PathVariable("email") String email, @RequestBody @Valid Friendship friend) {
        try{
            Optional<Friendship> result = users.FRIENDSHIPS_add(email, friend);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).addFriendship(email, friend)).withSelfRel();
                Link all = linkTo(methodOn(UserController.class).getAllFriendships(0, 20, null, email)).withSelfRel();

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AlreadyExistsAttribute message) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return ResponseEntity.notFound().build();
    }


    @DeleteMapping(
            path = "{email}/friendships/{friendshipid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal")
    @Operation(
            operationId = "deleteFriendship",
            summary = "Creates a friendship"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The friendship details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship not found",
                    content = @Content
            )
    })
    ResponseEntity<Friendship> deleteFriendship(@Parameter(name = "User email", required = true) @PathVariable("email") String email, @Parameter(name = "Friendship id", required = true) @PathVariable("friendshipid") String friendshipid) {
        try{
            Optional<Friendship> result = users.FRIENDSHIPS_delete(email, friendshipid);

            if(result.isPresent()) {
                Link all = linkTo(methodOn(UserController.class).getAllFriendships(0, 20, null, email)).withSelfRel();

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute notFoundAttribute) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AlreadyExistsAttribute alreadyExistsAttribute) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.notFound().build();
    }





    //Assessments

    @GetMapping(
            path = "{email}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal or @userService.areFriends(#email, principal) or hasRole('ADMIN')")
    @Operation(
            operationId = "getAllAssessmentsFromUser",
            summary = "Get the list of Assessments from this user, paginated"
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
    ResponseEntity<Page<Assessment>> getAllAssessmentsUser(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable String email) throws NotFoundAttribute {
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
            Optional<Page<Assessment>> result = users.ASSESSMENTS_getAllUser(email, page, size, Sort.by(criteria));

            if (result.isPresent()) {
                Page<Assessment> data = result.get();
                Pageable metadata = data.getPageable();

                Link user = linkTo(methodOn(UserController.class).getOne(email)
                ).withSelfRel();

                Link first = linkTo(methodOn(UserController.class).getAllAssessmentsUser(metadata.first().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.FIRST);

                Link last = linkTo(methodOn(UserController.class).getAllAssessmentsUser(data.getTotalPages() - 1, size, sort, email)
                ).withRel(IanaLinkRelations.LAST);

                Link next = linkTo(methodOn(UserController.class).getAllAssessmentsUser(metadata.next().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.NEXT);

                Link previous = linkTo(methodOn(UserController.class).getAllAssessmentsUser(metadata.previousOrFirst().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.PREVIOUS);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, user.toString())
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
