package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import javax.validation.constraints.NotBlank;

@Document(collection = "films")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "Film",
        description = "Representation of the Film object"
)

public class Film {
    @Id
    private String id;
    @NotBlank
    @Schema(required = true, example = "Miss Americana")
    private String title;
    @Schema(required = true, example = "A raw and emotionally revealing look at one of the most iconic artists of our time (...)")
    private String overview;
    @Schema(required = true, example = "Miss Americana")
    private String tagline;
    @ArraySchema(schema = @Schema(implementation = Collection.class, required = true))
    private Collection collection;
    @ArraySchema(schema = @Schema(implementation = String.class, required = true, example = "Documentary"))
    private List<String> genres;
    private Date releaseDate;
    @ArraySchema(schema = @Schema(implementation = String.class, required = true, example = "Music"))
    private List<String> keywords;
    @ArraySchema(schema = @Schema(implementation = Producer.class, required = true))
    private List<Producer> producers;
    @ArraySchema(schema = @Schema(implementation = Crew.class, required = true))
    private List<Crew> crew;
    @ArraySchema(schema = @Schema(implementation = Cast.class, required = true))
    private List<Cast> cast;
    @ArraySchema(schema = @Schema(implementation = Resource.class, required = true))
    private List<Resource> resources;
    @Schema(required = true, example = "154225523")
    private Long budget;
    private Status status;
    private Integer runtime;
    @Schema(required = true, example = "154225523")
    private Long revenue;
    @ArraySchema(schema = @Schema(implementation = Assessment.class, required = true, example = "Music"))
    private List<Assessment> comments;

    public Film() { }

    public Film(String id, String title, String overview, String tagline, Collection collection, List<String> genres, Date releaseDate, List<String> keywords, List<Producer> producers, List<Crew> crew, List<Cast> cast, List<Resource> resources, Long budget, Status status, Integer runtime, Long revenue, List<Assessment> comments) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.tagline = tagline;
        this.collection = collection;
        this.genres = genres;
        this.releaseDate = releaseDate;
        this.keywords = keywords;
        this.producers = producers;
        this.crew = crew;
        this.cast = cast;
        this.resources = resources;
        this.budget = budget;
        this.status = status;
        this.runtime = runtime;
        this.revenue = revenue;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public Film setId(String id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Film setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getOverview() {
        return overview;
    }

    public Film setOverview(String overview) {
        this.overview = overview;
        return this;
    }

    public String getTagline() {
        return tagline;
    }

    public Film setTagline(String tagline) {
        this.tagline = tagline;
        return this;
    }

    public Collection getCollection() {
        return collection;
    }

    public Film setCollection(Collection collection) {
        this.collection = collection;
        return this;
    }

    public List<String> getGenres() {
        return genres;
    }

    public Film setGenres(List<String> genres) {
        this.genres = genres;
        return this;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public Film setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Film setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public List<Producer> getProducers() {
        return producers;
    }

    public Film setProducers(List<Producer> producers) {
        this.producers = producers;
        return this;
    }

    public List<Crew> getCrew() {
        return crew;
    }

    public Film setCrew(List<Crew> crew) {
        this.crew = crew;
        return this;
    }

    public List<Cast> getCast() {
        return cast;
    }

    public Film setCast(List<Cast> cast) {
        this.cast = cast;
        return this;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public Film setResources(List<Resource> resources) {
        this.resources = resources;
        return this;
    }

    public Long getBudget() {
        return budget;
    }

    public Film setBudget(Long budget) {
        this.budget = budget;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public Film setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public Film setRuntime(Integer runtime) {
        this.runtime = runtime;
        return this;
    }

    public Long getRevenue() {
        return revenue;
    }

    public Film setRevenue(Long revenue) {
        this.revenue = revenue;
        return this;
    }

    public List<Assessment> getComments() {
        return comments;
    }

    public Film setComments(List<Assessment> comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return Objects.equals(id, film.id) && Objects.equals(title, film.title) && Objects.equals(overview, film.overview) && Objects.equals(tagline, film.tagline) && Objects.equals(collection, film.collection) && Objects.equals(genres, film.genres) && Objects.equals(releaseDate, film.releaseDate) && Objects.equals(keywords, film.keywords) && Objects.equals(producers, film.producers) && Objects.equals(crew, film.crew) && Objects.equals(cast, film.cast) && Objects.equals(resources, film.resources) && Objects.equals(budget, film.budget) && status == film.status && Objects.equals(runtime, film.runtime) && Objects.equals(revenue, film.revenue) && Objects.equals(comments, film.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, overview, tagline, collection, genres, releaseDate, keywords, producers, crew, cast, resources, budget, status, runtime, revenue, comments);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Film.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("title='" + title + "'")
                .add("overview='" + overview + "'")
                .add("tagline='" + tagline + "'")
                .add("collection=" + collection)
                .add("genres=" + genres)
                .add("releaseDate=" + releaseDate)
                .add("keywords=" + keywords)
                .add("producers=" + producers)
                .add("crew=" + crew)
                .add("cast=" + cast)
                .add("resources=" + resources)
                .add("budget=" + budget)
                .add("status=" + status)
                .add("runtime=" + runtime)
                .add("revenue=" + revenue)
                .add("comments=" + revenue)
                .toString();
    }
}
