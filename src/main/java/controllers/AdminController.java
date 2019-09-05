/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import static services.AdminService.*;
import entities.Asocijacija;
import entities.GameOfTheDay;
import entities.RegistrationRequest;
import entities.User;
import entities.User.UserType;
import entities.WordPairs;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import services.AdminService;
import util.Transaction;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="AdminController")
public class AdminController implements Serializable{
    private short adminPageTab  = 0; //0-Reg Requests, 1 - Game of the Day
    private List<RegistrationRequest> requests;
    
    private String outputMessage = "Please Choose a Date";
    private final Date currentDate = new Date(System.currentTimeMillis()-1000*60*60*24);
    private GameOfTheDay gameOnChosenDay;
    private Date chosenDate;
    
    private List<String> spojnice;
    private Map<String, WordPairs> spojniceMap;
    private String chosenPair;
    
    
    private List<String> asocijacije;
    private Map<String, Asocijacija> asocijacijeMap;
    private String chosenAsocijacija;

    @PostConstruct
    public void init(){
        initSpojnice();
        initAsocijacije();
    }
    
    public void submit() {
        try(Transaction transaction = new Transaction()) {
            if(gameOnChosenDay !=null) {
                gameOnChosenDay = transaction.get(GameOfTheDay.class, chosenDate);
                gameOnChosenDay.setPairs(spojniceMap.get(chosenPair));
                gameOnChosenDay.setAsocijacija(asocijacijeMap.get(chosenAsocijacija));
            }
            else {
                GameOfTheDay game = new GameOfTheDay(chosenDate, spojniceMap.get(chosenPair), asocijacijeMap.get(chosenAsocijacija));
                transaction.save(game);
                outputMessage = "This game can still be changed";
            }
        }
    }
    
    public boolean getSubmitDisabled(){
        return chosenDate==null || (gameOnChosenDay!=null && gameOnChosenDay.isPlayed());
    }

    private void initAsocijacije(){
        List asocijacijeResult = AdminService.getAsocijacije();

        asocijacijeMap = new HashMap<>();
        asocijacije = new LinkedList<>();

        for (Object object : asocijacijeResult) {
            Asocijacija asocijacija = (Asocijacija) object;
            asocijacije.add(asocijacija.getResultEnd().split("-")[0]);
            asocijacijeMap.put(asocijacija.getResultEnd().split("-")[0], asocijacija);
        }
    }

    private void initSpojnice(){
        List spojniceResult = getWordPairs();
        
        spojniceMap = new HashMap<>();
        spojnice = new LinkedList<>();
        for (Object object : spojniceResult) {
            WordPairs wordPairs = (WordPairs) object;
            spojnice.add(wordPairs.getText());
            spojniceMap.put(wordPairs.getText(), wordPairs);
        }
    }
    
    private void initRequests(){
        List requestsResult = getRegRequests();
        
        requests = new LinkedList<>();
        for(Object request: requestsResult)
            if(request instanceof RegistrationRequest) {
                requests.add((RegistrationRequest)request);
            }
    }
    
    public void accept(RegistrationRequest request){
        User user = new User(request, UserType.User);

        try(Transaction transaction = new Transaction()){
            deleteRegRequest(transaction, request.getUsername());
            transaction.save(user);
        }

        requests.remove(request);
    }

    public void refuse(RegistrationRequest request){
        try(Transaction transaction = new Transaction()){
            deleteRegRequest(transaction, request.getUsername());
        }
        
        if(request.isHasImage()){
            new File("C:\\Users\\Marko\\Desktop\\userImages\\"+request.getUsername() + ".jpg").delete();
            new File("C:\\Users\\Marko\\Desktop\\userImages\\"+request.getUsername() + ".png").delete();
        }
    }

    public String hasImage(RegistrationRequest req){
        if(req.isHasImage()) return "Yes";
        else return "No";
    }

    public String getChosenAsocijacija() {
        return chosenAsocijacija;
    }

    public void setChosenAsocijacija(String chosenAsocijacija) {
        this.chosenAsocijacija = chosenAsocijacija;
    }

    public List<String> getAsocijacije() {
        return asocijacije;
    }
    
    public Date getChosenDate() {
        return chosenDate;
    }

    public void setChosenDate(Date chosenDate) {
        try(Transaction transaction = new Transaction()){
            gameOnChosenDay = transaction.get(GameOfTheDay.class, chosenDate);
            if(gameOnChosenDay!=null) {
                chosenPair = gameOnChosenDay.getPairs().getText();
                if(gameOnChosenDay.isPlayed()){
                    outputMessage = "This game has already been played";
                }
                else outputMessage = "This game can still be changed";
            }
            else outputMessage = "There is no game set on this date";
        }
        
        this.chosenDate = chosenDate;
    }

    public String getOutputMessage() {
        return outputMessage;
    }

    public List<String> getSpojnice() {
        return spojnice;
    }

    public String getChosenPair() {
        return chosenPair;
    }

    public void setChosenPair(String chosenPair) {
        this.chosenPair = chosenPair;
    }
    
    
    public List<RegistrationRequest> getRequests() {
        initRequests();
        return requests;
    }
    
    public short getAdminPageTab() {
        return adminPageTab;
    }

    public void setAdminPageTab(short adminPageTab) {
        this.adminPageTab = adminPageTab;
    }
    
    public boolean isMyPage(short i){
        return i==adminPageTab;
    }

    public Date getCurrentDate() {
        return currentDate;
    }
    
}
