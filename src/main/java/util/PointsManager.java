/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import games.MojBroj;
import games.Slagalica;

/**
 *
 * @author Marko
 */
public class PointsManager {
    public static int slagalica(Slagalica slagalica){
        int points = 0;
        
        boolean wordAcceptable = true;

        try(Transaction transaction = new Transaction()) {
            if (transaction.createQuery("FROM AcceptableWord WHERE word=?")
                    .setParameter(0, slagalica.getChosenWord())
                    .uniqueResult() == null) wordAcceptable = false;
        }
        
        if(wordAcceptable) for(boolean button: slagalica.getButtons()) if(!button) points+=2;
        return points;
    }
    
    public static int mojBroj(MojBroj mojBroj){
        if(mojBroj.getChosenExpression()==null || "".equals(mojBroj.getChosenExpression())) return 0;
        try{
            ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
            int result = (int) scriptEngine.eval(mojBroj.getChosenExpression());
            mojBroj.setDifference(Math.abs(result - Integer.parseInt(mojBroj.getDesiredNumber())));
            return 10;
        } catch (ScriptException ex) {
            return 0;
        }
    }
}
