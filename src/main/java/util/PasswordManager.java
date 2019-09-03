/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marko
 */
public class PasswordManager {
    private static MessageDigest digest;
    
    static{
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PasswordManager.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
    
    public static byte[] createPasswordDigest(String password){
        return digest.digest(password.getBytes());
    }
    
    public static boolean checkPassword(String password, byte[] digest){
        return Arrays.equals(digest, createPasswordDigest(password));
    }
}
