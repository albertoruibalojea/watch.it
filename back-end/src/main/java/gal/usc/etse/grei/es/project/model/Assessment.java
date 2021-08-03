package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "assessments")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Assessment {
    @Id
    private String id;
    @NotNull
    @Schema(implementation = User.class, required = true)
    private User user;
    @NotNull
    @Schema(implementation = Film.class, required = true)
    private Film film;
    @NotBlank
    @Schema(required = true, example = "Wow such a powerful documentary")
    private String comment;
    @Schema(required = true, example = "10")
    private Integer rating;

    public Assessment(){
        this.id = "";
        this.user = null;
        this.film = null;
        this.comment = "";
        this.rating = 0;
    }

    public Assessment(String id, User user, Film film, String comment, Integer rating) {
        this.id = id;
        this.user = user;
        this.film = film;
        this.comment = comment;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public Assessment setId(String id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Assessment setUser(User user) {
        this.user = user;
        return this;
    }

    public Film getFilm() {
        return film;
    }

    public Assessment setFilm(Film film) {
        this.film = film;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Assessment setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Integer getRating() {
        return rating;
    }

    public Assessment setRating(Integer rating) {
        this.rating = rating;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assessment that = (Assessment) o;
        return Objects.equals(id, that.id) && Objects.equals(rating, that.rating) && Objects.equals(user, that.user) && Objects.equals(film, that.film) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rating, user, film, comment);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Assessment.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("rating=" + rating)
                .add("user=" + user.getEmail())
                .add("film=" + film.getId())
                .add("comment='" + comment + "'")
                .toString();
    }
}
