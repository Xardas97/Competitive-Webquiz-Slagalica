/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import exceptions.UploadFailedException;
import util.PasswordManager;
import entities.RegistrationRequest;
import entities.User.Gender;
import entities.User;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.ManagedBean;
import javax.enterprise.context.SessionScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;

import org.primefaces.model.UploadedFile;
import org.primefaces.shaded.commons.io.FilenameUtils;
import util.HttpSessionManager;
import util.Transaction;

/**
 *
 * @author Marko
 */

@ManagedBean
@SessionScoped
@Named(value="LoginController")
public class LoginController implements Serializable {
    private static final String IMAGE_FOLDER = "C:\\Users\\Marko\\Desktop\\userImages\\";
    private short firstPageTab = 0; //0-login, 1-register, 2-change pass
    private final String redirect = "?faces-redirect=true";
    
    private String firstName;
    private String lastName;
    private String email;
    private String profession;
    private String username;
    private String password;
    private String confirmPassword;
    private String oldPassword;
    private Gender gender;
    private Date birthday;
    private String errorMessage="";
    private UploadedFile uploadedFile=null;
      
    public void register(){
        if(!password.equals(confirmPassword)){ errorMessage = "Passwords don't match"; return; }
        
        try(Transaction transaction = new Transaction()) {

            if (transaction.get(User.class, username) != null || transaction.get(RegistrationRequest.class, username) != null) {
                errorMessage = "Username already exists";
                return;
            }

            String extension = FilenameUtils.getExtension(uploadedFile.getFileName());
            if (extension != null) {
                try {
                    saveImage(extension);
                } catch (UploadFailedException e) {
                    return;
                }
            }

            transaction.save(createRegistrationRequest(extension));
        }
        
        errorMessage = "";
        firstPageTab = 0;
    }

    public void changePassword(){
        if(!password.equals(confirmPassword)) {
            errorMessage = "Passwords don't match";
            return;
        }
        
        try(Transaction transaction = new Transaction()) {
            User user = transaction.get(User.class, username);

            if (user != null) {
                if (PasswordManager.checkPassword(oldPassword, user.getPassword())) {
                    //username and password accurate, we can change his password
                    //User is still connected to the database, we don't need to manually update it
                    user.setPassword(PasswordManager.createPasswordDigest(password));

                    errorMessage = "";
                    firstPageTab = 0;
                } else errorMessage = "Wrong Old Password";
            } else errorMessage = "No such user exists";
        }
    }
 
    /*public void createAdmin() {
        User user = new User();
        user.setFirstName("admin");
        user.setLastName("admin");
        user.setEmail("admin@slagalica.com");
        user.setProfession(null);
        user.setUsername("admin");
        user.setPassword(PasswordManager.createPasswordDigest("pwd"));
        user.setGender(Gender.Other);
        user.setBirthday(null);
        user.setHasImage(false);
        user.setType(UserType.Administrator);
        
        try(Transaction transaction = new Transaction()) {

        transaction.save(user);
        
        closeTransaction(transaction);
        
        errorMessage = "";
        firstPageTab = 0;
    }*/
    
    public String enterAsGuest(){
        errorMessage = "";
        return "menu"+redirect;
    }
    
    public String login(){
        User user;
        try(Transaction transaction = new Transaction()) {
            user = transaction.get(User.class, username);
        }
        
        if(user!=null){
            if(PasswordManager.checkPassword(password, user.getPassword())){
                //username and password accurate, we can log him in
                HttpSessionManager.setUser(user);
                errorMessage = "";
                switch(user.getType()){
                    case User: return "menu"+redirect;
                    case Administrator: return "admin"+redirect;
                    case Supervisor: return "supervisor"+redirect;
                }
            }
            else errorMessage = "Wrong password";
        } 
        else errorMessage = "No such user exists";
        
        return "index";
    }

    public String logout(){
        HttpSessionManager.getSession().invalidate();
        errorMessage = "";
        return "index"+redirect;
    }

    private void saveImage(String extension) throws UploadFailedException {
        if(!("jpg".equals(extension.toLowerCase()) || "png".equals(extension.toLowerCase()))){
            errorMessage = "Just 'jpg' and 'png' images are supported";
            throw new UploadFailedException();
        }
        try{
            InputStream input = uploadedFile.getInputstream();
            BufferedImage img = ImageIO.read(input);
            if(img.getHeight()>300 || img.getWidth()>300){
                errorMessage = "Wrong image size, max: 300x300px, yours: " + img.getHeight() + "x" + img.getWidth() + "px";
                throw new UploadFailedException();
            }
            Path filePath = Paths.get(IMAGE_FOLDER + username+"."+extension);
            Files.createFile(filePath);
            ImageIO.write(img, extension, new File(filePath.toString()));

        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            errorMessage = "Image upload failed";
            throw new UploadFailedException();
        }
    }

    private RegistrationRequest createRegistrationRequest(String extension) {
        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setProfession(profession);
        request.setUsername(username);
        request.setPassword(PasswordManager.createPasswordDigest(password));
        request.setGender(gender);
        request.setBirthday(birthday);
        if(extension!=null) request.setHasImage(true);
        return request;
    }

    public Gender[] getGenderStates(){
        return Gender.values();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public short getFirstPageTab() {
        return firstPageTab;
    }

    public void setFirstPageTab(short firstPageTab) {
        errorMessage = "";
        this.firstPageTab = firstPageTab;
    }
    
    public boolean isMyPage(short i){
        return i==firstPageTab;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }
    
}
