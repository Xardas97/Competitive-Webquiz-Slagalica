/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import database.HibernateUtil;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.hibernate.Session;

/**
 *
 * @author Marko
 */
public class PointsManager {
    public static class IntegerWrapper{
        public int value;
        public IntegerWrapper(){ value=0; }
    }
    
    public static int slagalica(String word, boolean[] buttons){
        int points = 0;
        
        boolean wordAcceptable = true;
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        
        if(session.createQuery("FROM AcceptableWord WHERE word=:word")
                    .setString("word", word)
                    .uniqueResult()==null) wordAcceptable=false;
        
        session.getTransaction().commit();
        session.close();
        
        if(wordAcceptable) for(boolean button: buttons) if(!button) points+=2;
        return points;
    }
    
    public static int mojBroj(String expression, String desiredNumber, IntegerWrapper difference){
        if(expression==null || "".equals(expression)) return 0;
        try{
            ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
            int result = (int) scriptEngine.eval(expression);
            difference.value = Math.abs(result - Integer.parseInt(desiredNumber));
            return 10;
        } catch (ScriptException ex) {
            return 0;
        }
    }
}
