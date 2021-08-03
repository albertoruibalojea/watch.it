package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.exceptions.NotFoundAttribute;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("friendships")
@Tag(name = "Friendship API", description = "Friendship related operations")
@SecurityRequirement(name = "JWT")
public class FriendshipController {
    private final FriendshipService friendships;
    private final LinkRelationProvider relationProvider;

    public FriendshipController(FriendshipService friendships, LinkRelationProvider relationProvider) {
        this.friendships = friendships;
        this.relationProvider = relationProvider;
    }


    @GetMapping(
            path = "{friendship}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal or @userService.areFriends(#email, principal)")
    @Operation(
            operationId = "getOneFriendship",
            summary = "Gets a friendship"
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
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<Friendship> getOneFriendship(@Parameter(name = "Friendship id", required = true) @PathVariable("friendship") String friendship) {
        try{
            Optional<Friendship> result = friendships.FRIENDSHIPS_getOne(friendship);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(FriendshipController.class).getOneFriendship(friendship)).withSelfRel();
                Link all = linkTo(methodOn(UserController.class).getAllFriendships(0, 20, null, result.get().getUser())).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
                Link user = linkTo(methodOn(UserController.class).getOne(result.get().getUser())).withRel(relationProvider.getItemResourceRelFor(User.class));
                Link friend = linkTo(methodOn(UserController.class).getOne(result.get().getFriend())).withRel(relationProvider.getItemResourceRelFor(User.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .header(HttpHeaders.LINK, user.toString())
                        .header(HttpHeaders.LINK, friend.toString())
                        .body(result.get());
            }
        } catch (NotFoundAttribute message){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.notFound().build();
    }


    @PatchMapping(
            path = "{friendship}",
            consumes = "application/json-patch+json"
    )
    @PreAuthorize("@friendshipService.areYouTheFriend(#friendship, principal)")
    @Operation(
            operationId = "acceptFriendship",
            summary = "GThe friend accepts a friendship"
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
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content
            )
    })
    ResponseEntity<Friendship> acceptFriendship(@Parameter(name = "Friendship id", required = true) @PathVariable("friendship") String friendship,
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
        try{
            Optional<Friendship> result = friendships.FRIENDSHIPS_accept(friendship, json);

            if(result.isPresent()) {
                Link self = linkTo(methodOn(FriendshipController.class).acceptFriendship(friendship, json)).withSelfRel();
                Link all = linkTo(methodOn(UserController.class).getAllFriendships(0, 20, null, result.get().getUser())).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
                Link user = linkTo(methodOn(UserController.class).getOne(result.get().getUser())).withRel(relationProvider.getItemResourceRelFor(User.class));
                Link friend = linkTo(methodOn(UserController.class).getOne(result.get().getFriend())).withRel(relationProvider.getItemResourceRelFor(User.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .header(HttpHeaders.LINK, user.toString())
                        .header(HttpHeaders.LINK, friend.toString())
                        .body(result.get());
            }
        } catch (JsonPatchException message){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.notFound().build();
    }
}
