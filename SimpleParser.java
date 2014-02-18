import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.math.*;

public class SimpleParser{
   // Constants for Diffie-Hellman encryption
   public static Random random = new Random();
   public static BigInteger p = new BigInteger("7897383601534681724700886135766287333879367007236994792380151951185032550914983506148400098806010880449684316518296830583436041101740143835597057941064647");
   public static BigInteger g = new BigInteger("2333938645766150615511255943169694097469294538730577330470365230748185729160097289200390738424346682521059501689463393405180773510126708477896062227281603");
   public static BigInteger privateKey = new BigInteger(p.bitLength(), random);
   public static BigInteger publicKey = g.modPow(privateKey, p);
   public static BigInteger monitorKey;
   public static boolean isEncryptionEnabled = false;

   public static String HOST_ADDRESS;
   public static int HOST_PORT;
   public static boolean enableManualMode = false; // will be useful
   PrintWriter out = null;
   BufferedReader in = null;
   String receivedMessage;
   String identity;
   String password;
   static String cookie = "";
   String ppChecksum = "";

   public SimpleParser(){
      getIdentification();
   }

   public SimpleParser(String ident, String pwd){
      identity = ident;
      password = pwd;
      getIdentification();
   }

   public void getIdentification(){

   }

   public String getMonitorMessage(){
      return "";
   }

   public void handleRequire(String msg){
      if(getRequireCmd(msg).compareTo("IDENT") == 0) sendIdent(); // sendEncryptedIdent();
      if(getRequireCmd(msg).compareTo("PASSWORD") == 0) sendPassword();
      if(getRequireCmd(msg).compareTo("HOST_PORT") == 0) sendHostPort();
      if(getRequireCmd(msg).compareTo("ALIVE") == 0) sendAlive();
      if(getRequireCmd(msg).compareTo("QUIT") == 0) sendQuit();
   }

   public void sendAnything(String msg){
      System.out.println("Send " + msg.toUpperCase() + "...");
      out.println(msg);
   }

   public void sendQuit(){
      System.out.println("LOCAL  : Send QUIT to Monitor");
      out.println("QUIT");
      enableManualMode = true;
   }

   public void sendIdent(){
      System.out.println("LOCAL  : Send IDENT (" + identity + ") to Monitor");
      out.println("IDENT " + identity);
   }

   public void sendEncryptedIdent(){
      System.out.println("LOCAL  : Send secure IDENT (" + identity + ") *PubKey* to Monitor");
      out.println("IDENT " + identity + " " + publicKey);
   }

   public void sendPassword(){
      System.out.println("LOCAL  : Send PASSWORD (" + password + ") to Monitor");
      out.println("PASSWORD " + password);
   }

   public void sendAlive(){
      System.out.println("LOCAL  : Send ALIVE (" + cookie + ") to Monitor");
      out.println("ALIVE " + cookie);
   }

   public void sendHostPort(){
      System.out.println("LOCAL  : Send HOST_PORT (" + HOST_ADDRESS + " " + HOST_PORT +  ") to Monitor");
      out.println("HOST_PORT " + HOST_ADDRESS + " " + HOST_PORT); // Should be changed
   }

   public void sendSignOff(){
      System.out.println("LOCAL  : Sign off from Monitor");
      out.println("SING_OFF");
   }

   public void sendChangePassword(String oldPassword, String newPassword){
      System.out.println("LOCAL  : Change password to " + newPassword);
      out.println("CHANGE_PASSWORD " + oldPassword + " " + newPassword);
   }

   public void storeCookie(String msg){
      cookie = msg.substring(8).trim();
      System.out.println("LOCAL  : Save cookie (" + cookie + ") from Monitor");
   }
   
   public void storeMonitorDHKey(String msg){
      String dhKey = msg.substring(5).trim();
      monitorKey = new BigInteger(dhKey, 32);
      System.out.println("LOCAL  : Save public key from Monitor");
   }

   /**
    * Handles result message from monitor.
    * @param  msg Message from monitor
    * @return     A string that might be:
    *               Cookie - save for the server to identify itself
    *               Other participants' info - ???
    *             
    */
   public String handleResult(String msg){
      msg = msg.substring(8).trim(); // remove RESULT from msg
      if(msg.contains("PASSWORD")) storeCookie(msg);
      if(msg.contains("IDENT")) storeMonitorDHKey(msg);
      return "";
   }

   public void handleChecksum(String msg){
         ppChecksum = msg.substring(30).trim();
   }



   public String getRequireCmd(String msg){
      return msg.substring(8).trim();
   }
}