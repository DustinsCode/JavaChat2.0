package model;

import controller.controller;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;

/**
 * Class to send and receive messages.
 */
public class Connection {

    /** Secret Key */
    SecretKey sKey;

    /** Public Key */
    PublicKey pubKey;

    /** Trying to exit and close the connection */
    boolean exit;

    /** Message received to pass on to the view */
    String recdMessage;

    /** Client */
    Client cli;

    /** Controller.  to pass messeges to? */
    controller cont;

    /** The SocketChannel */
    SocketChannel sc;
    IvParameterSpec iv;

    public Connection(Client c, controller cont){
        this.cli = c;
        this.cont = cont;
        exit = false;
        sKey = generateAESKey();
        try {
            this.sc = SocketChannel.open();
            this.sc.connect(new InetSocketAddress(this.cli.getIpAddress(), this.cli.getPort()));
        }catch(Exception e){
            String error = "0Error connecting to the server.";
            cont.messageReceived(error);
        }
        runClient();
    }

    public void runClient(){
        try{
            //wait to receive public key
            waitForPubKey(this.sc);

            //Send our private key to the server
            byte[] secArray = RSAEncrypt(sKey.getEncoded());
            ByteBuffer b = ByteBuffer.wrap(secArray);
            this.sc.write(b);

            //Create and send IvParameterSpec
            SecureRandom r = new SecureRandom();
            byte[] ivbytes = new byte[16];
            r.nextBytes(ivbytes);
            this.iv = new IvParameterSpec(ivbytes);
            b = ByteBuffer.wrap(ivbytes);
            this.sc.write(b);

            Thread t = new Thread(new Runnable() {
                public void run(){
                    runThread(sc, iv);
                }
            });
            t.start();

            //Sends the username to the server
            byte[] userNameBytes = encrypt(formatArray(cli.getUsername().getBytes()), sKey, iv);
            ByteBuffer buff = ByteBuffer.wrap(userNameBytes);
            sc.write(buff);
            cont.messageReceived("1You are now connected to the server!");

        }catch(Exception e){
            String error = "0Error connecting to the server.";
            cont.messageReceived(error);
        }
    }

    public void sendMessage(String message){
        //TODO: make controller check if valid message
        try {
            ByteBuffer buff = ByteBuffer.wrap(encrypt(formatArray(message.getBytes()), sKey, iv));
            this.sc.write(buff);
        }catch(IOException ioe){
            String error = "0IOException occured.";
            cont.messageReceived(error);
        }
    }

    public byte[] RSAEncrypt(byte[] plaintext){
        try{
            Cipher c = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            c.init(Cipher.ENCRYPT_MODE,pubKey);
            byte[] ciphertext=c.doFinal(plaintext);
            return ciphertext;
        }catch(Exception e){
            System.out.println("RSA Encrypt Exception\n" + e);
            System.exit(1);
            return null;
        }
    }

    /**
     * Runs thread to listen for incoming messages.
     * @param sc SocketChannel we're using.
     * @param iv idk what this is but I need it.
     */
    private void runThread(SocketChannel sc, IvParameterSpec iv){
        while (true){
            if(!sc.isConnected()){
                break;
            }
            try{
                ByteBuffer buff = ByteBuffer.allocate(1024);
                sc.read(buff);
                String message = new String(decrypt(buff.array(), sKey, iv));
                message = message.trim();
                //Runs the messageReceived function in the controller.
                this.cont.messageReceived(message);

            }catch(Exception e){
                System.out.println("Got an exception in thread");
            }
        }
    }

    /**
     * Decrypts the message.
     * @param ciphertext the encrypted text
     * @param secKey the key
     * @param iv still don't know what exactly this is
     * @return
     */
    public byte[] decrypt(byte[] ciphertext, SecretKey secKey, IvParameterSpec iv){
        try{
            Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
            c.init(Cipher.DECRYPT_MODE,secKey,iv);
            byte[] plaintext = c.doFinal(ciphertext);
            return plaintext;
        }catch(Exception e){
            System.out.println("AES Decrypt Exception\n" + e);
            System.exit(1);
            return null;
        }
    }

    /**
     *  Waits for public key from server
     **/
    private void waitForPubKey(SocketChannel sc){
        try{
            ByteBuffer b = ByteBuffer.allocate(294);
            sc.read(b);
            byte[] keybytes = b.array();
            X509EncodedKeySpec keyspec = new X509EncodedKeySpec(keybytes);
            KeyFactory rsafactory = KeyFactory.getInstance("RSA");
            pubKey = rsafactory.generatePublic(keyspec);
        }catch(Exception e){
            System.out.println("Public Key Exception");
            System.exit(1);
        }
    }

    /**
     * Encrypts messages and other things.
     **/
    public byte[] encrypt(byte[] plaintext, SecretKey secKey, IvParameterSpec iv){
        try{
            Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
            c.init(Cipher.ENCRYPT_MODE,secKey,iv);
            byte[] ciphertext = c.doFinal(plaintext);
            return ciphertext;
        }catch(Exception e){
            System.out.println("AES Encrypt Exception\n" + e);
            System.exit(1);
            return null;
        }
    }

    /**
     * Formats array to send over.
     * @param arr the byte array of the message.
     * @return temp the formatted byte array.
     */
    public byte[] formatArray(byte[] arr){
        byte[] temp = new byte[1024];
        for (int i = 0; i < temp.length; i++){
            if (i < arr.length)
                temp[i] = arr[i];
        }
        return temp;
    }

    /**
     * Genereates AESKey for client.
     * */
    private SecretKey generateAESKey(){
        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secKey = keyGen.generateKey();
            return secKey;
        }catch(Exception e){
            System.out.println("Key Generation Exception");
            System.exit(1);
            return null;
        }
    }

}
