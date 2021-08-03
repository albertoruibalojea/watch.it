package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.*;

@Document(collection = "users")
//@JsonFilter("filter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "User",
        description = "Representation of the User object"
)
public class User {
    @Id
    @NotBlank(message = "The email field can not be empty")
    @Email
    @Schema(required = true, example = "test@test.com")
    private String email;
    @Schema(example = "Taylor Swift")
    private String name;
    @Schema(example = "USA")
    private String country;
    @Schema(example = "https://www.24newshd.tv/uploads/facebook_post_images/2020-10-26/facebook_post_image_1603699901.jpg")
    private String picture;
    private Date birthday;
    private String password;
    private List<String> roles;

    public User() {}

    public User(String email, String name, String country, String picture, Date birthday, String password, List<String> roles) {
        this.email = email;
        this.name = name;
        this.country = country;
        this.picture = picture;
        this.birthday = birthday;
        this.password = password;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getCountry() {
        return country;
    }
    public String getPicture() {
        return picture;
    }
    public Date getBirthday() {
        return birthday;
    }
    public String getPassword() {
        return password;
    }
    public List<String> getRoles() {
        return roles;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }
    public User setName(String name) {
        this.name = name;
        return this;
    }
    public User setCountry(String country) {
        this.country = country;
        return this;
    }
    public User setPicture(String picture) {
        this.picture = picture;
        return this;
    }
    public User setBirthday(Date birthday) {
        this.birthday = birthday;
        return this;
    }
    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    public User setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }
}
