package util;

import com.sun.istack.NotNull;
import entities.User;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Drazen
 */
public class SessionManager {

    public static HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
    }

    public static @NotNull User getUser() {
        HttpSession session = getSession();
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if(user==null) throw new NullPointerException("No User in Session");
            return user;
        } else {
            throw new NullPointerException("No User in Session");
        }
    }
    
    public static boolean setUser(User user) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute("user", user);
            return true;
        }
        return false;
    }
    
    public static String getPlayerSide() {
        HttpSession session = getSession();
        if (session != null) {
            return (String) session.getAttribute("playerSide");
        } else {
            return null;
        }
    }
    
    public static boolean setPlayerSide(String playerSideString) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute("playerSide", playerSideString);
            return true;
        }
        return false;
    }
    
    public static String getGameMode() {
        HttpSession session = getSession();
        if (session != null) {
            return (String) session.getAttribute("gameMode");
        } else {
            return null;
        }
    }
    
    public static boolean setGameMode(String gameModeString) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute("gameMode", gameModeString);
            return true;
        }
        return false;
    }
}