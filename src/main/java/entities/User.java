/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Marko
 */

@Entity
@Table(name="user")
public class User implements Serializable{
    public enum Gender {Male, Female, Other}
    public enum UserType {User, Supervisor, Administrator}
    
    private String firstName;
    private String lastName;
    private String email;
    private String profession;
    @Id
    private String username;
    private byte[] password;
    @Enumerated(EnumType.ORDINAL)
    private
    Gender gender;
    @Temporal(javax.persistence.TemporalType.DATE)
    private
    Date birthday;
    private boolean hasImage;
    @Enumerated(EnumType.ORDINAL)
    private
    UserType type;

    public User(){}
    
    public User(RegistrationRequest request, UserType type){
        this.firstName = request.firstName;
        this.lastName = request.lastName;
        this.email = request.email;
        this.profession = request.profession;
        this.username = request.username;
        this.password = request.password;
        this.gender = request.gender;
        this.birthday = request.birthday;
        this.hasImage = request.hasImage;
        this.type = type;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }
    
    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }
    
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }
    
}
