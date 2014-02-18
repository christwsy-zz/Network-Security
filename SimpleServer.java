import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class SimpleServer extends SimpleParser implements Runnable{
   public static int MONITOR_PORT;
   public static int LOCAL_PORT;
   String identity;
   String password;
   String cookie;
   ServerSocket server = null;
   Thread runner;

   public SimpleServer(int local_port, 
                       String ident, 
                       String pwd){
      LOCAL_PORT = local_port;
      identity = ident;
      password = pwd;
      try{
         server = new ServerSocket(LOCAL_PORT);
         MONITOR_PORT = LOCAL_PORT;
      }catch(IOException e){
         System.err.println(e);
      }
   }

   public void start(){
      if(runner == null){
         runner = new Thread(this);
         runner.start();
      }
   }

   public void run(){
      try{
         int i = 1;
         for(;;){
            // System.out.println("Starts server #" + i + " at " + LOCAL_PORT);
            Socket incoming = server.accept();
            new ConnectionHandler(incoming, i, identity, password).start();
            i++;
         }
      }catch(Exception e){
         System.out.println("Server error: " + e);
      }
   }
}

class ConnectionHandler extends SimpleParser implements Runnable{
   private Socket incoming;
   private int counter; 
   Thread runner;
        
   public ConnectionHandler (Socket i, int c, String name, String password) { 
      super(name, password);
      incoming = i;  counter = c; 
   }
  
   public void run() {
      try {
         in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
         out = new PrintWriter(incoming.getOutputStream(),true);

         HOST_PORT = SimpleServer.LOCAL_PORT;
         System.out.println("Start Simple Server..");

         while(true){
            String msg = in.readLine();
            System.out.println(msg);
            if(in == null) continue;
            if(msg.contains("REQUIRE:")) handleRequire(msg);
            else if(msg.contains("RESULT:")) handleResult(msg);
            else if(msg.contains("WAITING:")) ;
            else if(msg.contains("COMMAND_ERROR:")) ;
            else if(msg.contains("COMMENT:")) ;
            else if(msg.contains("PARTICIPANT_PASSWORD_CHECKSUM:")) handleChecksum(msg);
            else if(msg.contains("QUIT")){
               incoming.close();
               break;
            }
            else{ // Encryption used
            }
         }

         incoming.close();
      } catch (IOException e) {
      } catch (NullPointerException n) {
      } finally {
         try {
            incoming.close();
         } catch (IOException e) {
            System.err.println("Waht?! " + e);
         }
         return;
      }
   }

   public void start() {
      if (runner == null) {
         runner = new Thread(this);
         runner.start();
      }
   }
}