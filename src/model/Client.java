package model;

/**
 * Client class to handle client things.
 *
 * @author Dustin Thurston (@xStaticVoid)
 */
public class Client {

    /** Ip address*/
    private String ip;

    /** Port number*/
    private int port;

    /** Username*/
    private String username;

    /** admin rights? */
    private boolean admin;

    /**
     * Default Constructor.  Each provided from intro screen.
     * @param ip address to connect to
     * @param port number the server is on
     * @param username the user wants to be recognized as
     */
    public Client(String ip, int port, String username){
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.admin = false; //by default.  Server will decide if admin or not.
    }

    /**
     * Set's new username
     * @param newName new name to try.
     */
    public void setUsername(String newName){
        this.username = newName;
    }

    /**
     * Set's user's admin rights.
     * @param isAdmin true or false.
     */
    public void setAdmin(boolean isAdmin){
        this.admin = isAdmin;
    }

    /**
     * Get the username
     * @return the username
     */
    public String getUsername(){
        return this.username;
    }

    public String getIpAddress() {return this.ip;}

    public int getPort() { return this.port;}
}
