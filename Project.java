import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.math.*;

class Project {
   // Constants for Monitor-Client-Server communication
   public static String MONITOR_ADDRESS = "helios.ececs.uc.edu"; // Remote monitor's address 
   // public static String MONITOR_ADDRESS = "localhost"; // Remote monitor's address 
   public static int MONITOR_PORT = 8180; // Remote monitor's port listen on Helios
   public static int HOST_PORT = 20000 +(int)(Math.random()*1000); // Random port listen on local
   public static int MAX_CONNECTION = 5;
   SimpleClient sc;
   SimpleServer ss;

   static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
   static Random rnd = new Random();

   /**
    * For the use of generating random user ident and password
    * @param  len length of the random string
    * @return     a random string of that length
    */
   static String randomString(int len)
   {
      StringBuilder sb = new StringBuilder(len);
      for( int i = 0; i < len; i++ ) 
         sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
      return sb.toString();
   }

   public Project(String name, String password) {
      System.out.println("***IDENT="+name+";PASS="+password+"***\n");
      sc = new SimpleClient(MONITOR_ADDRESS, MONITOR_PORT, HOST_PORT, name, password);
      ss = new SimpleServer(HOST_PORT, name, password);
   }

   public static void main(String[] args) {
      Project project;
      if (args.length == 2) {
         project = new Project(args[0], args[1]);
      }
      else{
         // System.out.println("java Project Monitor_Address Monitor_Port Identity");
         System.out.println("Random identity and password generated.");
         project = new Project(randomString(5), randomString(12));
      }
      project.sc.start(); //Start Active Client
      System.out.println("Le me start server!");
      project.ss.start();  //Start Local Server
   }       
}
