import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.math.*;

public class SimpleParser{
   // Constant for BigInteger Radix
   static final int RADIX = 32;

   // Diffie-Hellman encryption
   public static BigInteger p = new BigInteger("7897383601534681724700886135766287333879367007236994792380151951185032550914983506148400098806010880449684316518296830583436041101740143835597057941064647");
   public static BigInteger g = new BigInteger("2333938645766150615511255943169694097469294538730577330470365230748185729160097289200390738424346682521059501689463393405180773510126708477896062227281603");
   public static BigInteger privateDHKey = new BigInteger(512, new SecureRandom());
   public static BigInteger publicDHKey = g.modPow(new BigInteger(privateDHKey.toString(RADIX), RADIX), new BigInteger(p.toString(RADIX), RADIX));
   public static BigInteger monitorDHKey;
   public static BigInteger sharedDHKey;

   // Karn
   Karn karn;

   // Zero-Knowledge 
   public static BigInteger monitorZKKey;
   public ZeroKnowledge zk = new ZeroKnowledge();

   // Certificates
   public static HashMap<String, String> certificates = new HashMap<String, String>(); 

   // Other things
   public static boolean isEncryptionEnabled = false;
   public static boolean isLoggedIn = false;
   public static String HOST_ADDRESS;
   public static int HOST_PORT;
   PrintWriter out = null;
   BufferedReader in = null;
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

   public void transferSomePoints(){
      System.out.println("***LETS TRANSFER POINTS***");
      String recipient = "JOHNE";
      int points = 20;
      String msg = "TRANSFER_REQUEST " + identity + " " + points + " FROM " + recipient;
      out.println(karn.encrypt(msg));
   }

   public void getIdentification(){
      String filename = identity + ".dat";
      try{
         BufferedReader reader = new BufferedReader(new FileReader(filename));
         String lastLine = "";
         String line;
         while((line = reader.readLine()) != null){
            if (lastLine.equals("PASSWORD"))
               this.password = line;
            if (lastLine.equals("COOKIE"))
               this.cookie = line;
            lastLine = line;
         }
      } catch(Exception e) {}
   }

   public void setCookie(String cookieString){
      cookie = cookieString;
   }

   public void handleMessage(String msg){
      if (msg.contains("REQUIRE")) handleRequire(msg);
      else if (msg.contains("RESULT")) handleResult(msg);
      else if (msg.contains("COMMENT")) ;
      else if (msg.contains("WAITING")) ;
      else if (msg.contains("COMMAND_ERROR")) ;
      else handleEncryptedCommand(msg);
   }

   public void handleComment(String msg) {
      if (msg.contains("Timeout")) sendAlive();
   }

   public void handleRequire(String msg){
      String requiredCmd = getRequireCmd(msg);
      if (requiredCmd.equals("IDENT")) sendEncryptedIdent(); // sendIdent(); // sendEncryptedIdent();
      else if (requiredCmd.equals("PASSWORD")) sendPassword();
      else if (requiredCmd.equals("HOST_PORT")) sendHostPort();
      else if (requiredCmd.equals("ALIVE")) sendAlive();
      else if (requiredCmd.equals("QUIT")) sendQuit();
      else if (requiredCmd.equals("PUBLIC_KEY")) sendAnything(zk.getPublicKey());
      else if (requiredCmd.equals("ROUNDS")){
         Random randomGenerator = new Random();
         sendAnything(zk.getRounds(16));
      }
      else if (requiredCmd.equals("AUTHORIZE_SET")) sendAnything(zk.getAuthorizeSet());
      else if (requiredCmd.equals("SUBSET_A")) sendAnything(zk.getSubsetA());
      else if (requiredCmd.equals("SUBSET_K")) sendAnything(zk.getSubsetK());
      else if (requiredCmd.equals("SUBSET_J")) sendAnything(zk.getSubsetJ());
      else if (requiredCmd.equals("TRANSFER_RESPONSE")) sendAnything(zk.response());
   }

   public void sendTransferResponse(boolean accept){
      String response;
      if (accept) response = "ACCEPT";
      else response = "DECLINE";
      String msg = "TRANSFER_RESPONSE " + response;
      System.out.println("LOCAL  : Send " + msg + " to Monitor");
      // out.println(karn.encrypt(msg));
      sendAnything(msg);
   }

   public void sendRounds(int rounds){
      rounds = 16;
      String msg = "ROUNDS " + rounds;
      System.out.println("LOCAL  : Send " + msg + " to Monitor");
      // out.println(karn.encrypt(msg));
      sendAnything(msg);
   }

   public void sendAnything(String msg){
      System.out.println("LOCAL  : Send " + msg + " to Monitor");
      // out.println(msg);
      out.println(karn.encrypt(msg));
   }

   public void sendQuit(){
      isLoggedIn = true;
      System.out.println("LOCAL  : Send QUIT to Monitor");
      out.println(karn.encrypt("QUIT"));
   }

   public void sendIdent(){
      System.out.println("LOCAL  : Send IDENT (" + identity + ") to Monitor");
      out.println("IDENT " + identity);
   }

   public void sendEncryptedIdent(){
      isEncryptionEnabled = true;
      System.out.println("LOCAL  : Send secure IDENT (" + identity + ") *PubDHKey* to Monitor");
      out.println("IDENT " + identity + " " + publicDHKey.toString(RADIX));
   }

   public void sendPassword(){
      System.out.println("LOCAL  : Send PASSWORD (" + password + ") to Monitor");
      if (isEncryptionEnabled) out.println(karn.encrypt("PASSWORD " + password));
      else out.println("PASSWORD " + password);
   }

   public void sendAlive(){
      System.out.println("LOCAL  : Send ALIVE (" + cookie + ") to Monitor");
         out.println(karn.encrypt("ALIVE " + cookie));
   }

   public void sendHostPort(){
      System.out.println("LOCAL  : Send HOST_PORT (" + HOST_ADDRESS + " " + HOST_PORT +  ") to Monitor");
      String msg = "HOST_PORT " + HOST_ADDRESS + " " + HOST_PORT;
      if (isEncryptionEnabled) out.println(karn.encrypt(msg));
      else out.println(msg);
   }

   public void sendSignOff(){
      System.out.println("LOCAL  : Sign off from Monitor");
      out.println("SING_OFF");
   }

   public void sendMakeCertificate(){
      // BigInteger temp = v;
      // String v = new String(temp.toString());
      // temp = n;
      // String n = new String(temp.toString());
      // String msg = "MAKE_CERTIFICATE " + v + " " + n;
      // System.out.println("LOCAL  : Send MAKE_CERTIFICATE (v) (n) to Monitor");
      // out.println(msg);
   }

   public void sendGetCertificate(String ident){
      String msg = "GET_CERTIFICATE " + ident;
      System.out.println("LOCAL  : Send " + msg + " to Monitor");
      out.println(msg);
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
      monitorDHKey = new BigInteger(dhKey, RADIX); // monitor key is in radix 32
      // System.out.println("LOCAL  : Save Monitor's DH Key");
      // System.out.println("PRIVATE_KEY=" + privateDHKey);
      // System.out.println("PUBLIC_KEY=" + publicDHKey);
      sharedDHKey = monitorDHKey.modPow(privateDHKey, p);
      // System.out.println("SHARE_KEY=" + sharedDHKey);
      karn = new Karn(sharedDHKey);

   }

   public void storeMonitorZKKey(String msg){
      String key = msg.substring(11).trim();
      monitorZKKey = new BigInteger(key, RADIX);
      System.out.println("LOCAL  : Save Monitor's ZK Key");
   }

   public void storeCertificate(String msg){
      String certificate[] = msg.substring(11).trim().split(" ");
      certificates.put(certificate[0], certificate[1]);
      System.out.println("LOCAL  :  Save certificate for " + certificate[0]);
   }

   public void handleResult(String msg){
      msg = msg.substring(8).trim(); // remove "RESULT: " from msg
      if (msg.contains("PASSWORD")) storeCookie(msg);
      else if (msg.contains("IDENT")) storeMonitorDHKey(msg);
      else if (msg.contains("MONITOR_KEY")) storeMonitorZKKey(msg);
      else if (msg.contains("CERTIFICATE")) storeCertificate(msg);
      else if (msg.contains("PUBLIC_KEY")) zk.savePublicKey(msg);
      else if (msg.contains("ROUNDS")) zk.saveRounds(msg);
      else if (msg.contains("AUTHORIZE_SET")) zk.saveAuthorizeSet(msg);
      else if (msg.contains("SUBSET_A")) zk.saveSubsetA(msg);
      else if (msg.contains("SUBSET_K")) zk.checkSubsetK(msg);
      else if (msg.contains("SUBSET_J")) zk.checkSubsetJ(msg);
      else if (msg.contains("HOST_PORT")) transferSomePoints();
   }

   public void handleChecksum(String msg){
      ppChecksum = msg.substring(30).trim();
   }

   public void handleEncryptedCommand(String msg){
      String plainText = karn.decrypt(msg);
      System.out.println("DECRYPT: " + plainText);
      handleMessage(plainText);
   } 

   public String getRequireCmd(String msg){
      // if (!isEncryptionEnabled)
         return msg.substring(8).trim();
      // else
         // return karn.decrypt(msg).substring(8).trim();
   }
}