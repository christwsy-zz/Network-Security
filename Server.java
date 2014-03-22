/*
This program means to be a standalong passive server which accept
connections and handles commands from the monitor.

To use, in Terminal (or other command prompt) type:
 java Server session_file

NOTE: session_file must in this particular format
 */


import java.util.*;
import java.io.*;
import java.net.*;

class Server{
   public static int HOST_PORT = 20000 +(int)(Math.random()*1000); // Random port listen on local
   SimpleServer ss;

   public Server(String name, String cookie) {
      ss = new SimpleServer(HOST_PORT, name, cookie);
      ss.cookie = cookie;
      System.out.println("***PORT:" + HOST_PORT+";IDENT="+name+";COOKIE="+ss.cookie+"***\n");
   }

   public static void main(String[] args) {
      Server server;
      if (args.length != 2){
         System.err.println("Insufficient parameters.");
         System.exit(1);
      }else{
         server = new Server(args[0], args[1]);
         server.ss.start();  //Start Local Server
      }
   }       
}