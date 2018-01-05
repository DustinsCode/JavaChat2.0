package controller;

import model.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import model.Connection;

public class controller implements controllerInterface{

    @FXML
    private TextField usernameText;

    @FXML
    private TextField ipText;

    @FXML
    private TextField portText;

    public void exitHandler() {
        //TODO: force nice disconnect from server
        System.exit(0);
    }

    public void loginButtonPushed(ActionEvent event){
        String username = usernameText.getText();
        String ip = ipText.getText();
        String port = portText.getText();
        int portNum = portCheck(port);
        if(ipCheck(ip) && portNum != -1){
            Client c = new Client(ip, portNum, username);
            Connection conn = new Connection(c, this);
        }
        System.out.println("Login button pushed!");
    }

    public void messageReceived(String message){

    }

    public void sendButtonPushed(ActionEvent event){

    }

    /**
     * Checks for valid IP address format XXX.XXX.XXX.XXX
     * @param ip address given by user
     * @return valid or invalid IP format
     */
    public static boolean ipCheck(String ip){
        try{
            String[] iparray = ip.split("\\.");
            int[] ipIntArray = new int[iparray.length];
            for(int i = 0; i < iparray.length; i++){
                ipIntArray[i] = Integer.parseInt(iparray[i]);
            }
            if(ipIntArray.length != 4){
                return false;
            }else{
                return true;
            }
        }catch(Exception e){
            return false;
        }
    }

    /**
     * Checks for valid port number entered by the user.
     * @param port the entered value
     * @return the value, or -1 if invalid
     */
    public static int portCheck(String port){
        try{
            int portNum = Integer.parseInt(port);
            if(portNum > 0 && portNum < 65535){
                return portNum;
            }

            return -1;
        }catch(NumberFormatException nfe){
            return -1;
        }
    }
}

