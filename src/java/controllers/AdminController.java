/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

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
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Marko
 */

@ManagedBean
@ViewScoped
@Named(value="AdminController")
public class AdminController implements Serializable{
    short adminPageTab  = 0; //0-Reg Requests, 1 - Game of the Day
    List<RegistrationRequest> requests;
    
    String outputMessage = "Please Choose a Date";
    Date currentDate = new Date(System.currentTimeMillis()-1000*60*60*24);
    GameOfTheDay gameOnChosenDay;
    Date chosenDate;
    
    List<String> spojnice;
    private Map<String, WordPairs> spojniceMap;
    String chosenPair;
    
    
    List<String> asocijacije;
    private Map<String, Asocijacija> asocijacijeMap;
    String chosenAsocijacija;
    
    public void initAsocijacije(){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        List asocijacijeResult = session.createQuery("FROM Asocijacija").list();
        
        session.getTransaction().commit();
        session.close();
        
        asocijacijeMap = new HashMap<>();
        asocijacije = new LinkedList<>();
        for(Object object: asocijacijeResult)
            if(object instanceof Asocijacija){
                Asocijacija asocijacija = (Asocijacija) object;
                asocijacije.add(asocijacija.getResultEnd().split("-")[0]); 
                asocijacijeMap.put(asocijacija.getResultEnd().split("-")[0], asocijacija);
            }       
    }
    
    @PostConstruct
    public void init(){
        initSpojnice();
        initAsocijacije();
    }
    
    public void submit() {
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        if(gameOnChosenDay !=null){
            gameOnChosenDay = (GameOfTheDay) session.get(GameOfTheDay.class, chosenDate);
            gameOnChosenDay.setPairs(spojniceMap.get(chosenPair));
            gameOnChosenDay.setAsocijacija(asocijacijeMap.get(chosenAsocijacija));
        }
        else{
            GameOfTheDay game = new GameOfTheDay(chosenDate, spojniceMap.get(chosenPair), asocijacijeMap.get(chosenAsocijacija));
            session.save(game);
            outputMessage = "This game can still be changed";
        }

        session.getTransaction().commit();
        session.close();
    }
    
    public boolean getSubmitDisabled(){
        return chosenDate==null || (gameOnChosenDay!=null && gameOnChosenDay.isPlayed());
    }
    
    public void initSpojnice(){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        List spojniceResult = session.createQuery("FROM WordPairs").list();
        
        session.getTransaction().commit();
        session.close();
        
        spojniceMap = new HashMap<>();
        spojnice = new LinkedList<>();
        for(Object object: spojniceResult)
            if(object instanceof WordPairs){
                WordPairs wordPairs = (WordPairs) object;
                spojnice.add(wordPairs.getText()); 
                spojniceMap.put(wordPairs.getText(), wordPairs);
            }
    }
    
    public void initRequests(){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        List requestsResult = session.createQuery("FROM RegistrationRequest").list();
        
        session.getTransaction().commit();
        session.close();
        
        requests = new LinkedList<>();
        for(Object request: requestsResult)
            if(request instanceof RegistrationRequest)
                requests.add((RegistrationRequest)request);
    }
    
    public void accept(RegistrationRequest request){
        User user = new User(request, UserType.User);
        
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        Query removeFromRequestsQuery = session.createQuery("DELETE FROM RegistrationRequest WHERE username=:username");
        removeFromRequestsQuery.setString("username", request.getUsername());
        removeFromRequestsQuery.executeUpdate();
        
        session.save(user);
        
        session.getTransaction().commit();
        session.close();
        
        requests.remove(request);
    }

    public void refuse(RegistrationRequest request){
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        Query removeFromRequestsQuery = session.createQuery("DELETE FROM RegistrationRequest WHERE username=:username");
        removeFromRequestsQuery.setString("username", request.getUsername());
        removeFromRequestsQuery.executeUpdate();
        
        session.getTransaction().commit();
        session.close();
        
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
        Session session = database.HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        gameOnChosenDay = (GameOfTheDay) session.get(GameOfTheDay.class, chosenDate);
        if(gameOnChosenDay!=null) {
            chosenPair = gameOnChosenDay.getPairs().getText();
            if(gameOnChosenDay.isPlayed()){
                outputMessage = "This game has already been played";
            }
            else outputMessage = "This game can still be changed";
        }
        else outputMessage = "There is no game set on this date";
        
        session.getTransaction().commit();
        session.close();
        
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
