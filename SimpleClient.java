import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class SimpleClient extends SimpleParser implements Runnable{
   public static String MONITOR_ADDRESS;
   public static int MONITOR_PORT;
   final int CONNECTION_TIMEOUT = 60 * 1000; // in milli-seconds
   public static String cookie = ""; // Used for server side when receive ALIVE
   Socket client = null;
   Thread runner;

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

   public SimpleClient(String monitor_address, // monitor address
                       int monitor_port, // monitor port
                       int local_port, // local port
                       String identity,
                       String password){
      super(identity, password);
      MONITOR_ADDRESS = monitor_address;
      MONITOR_PORT = monitor_port;
      HOST_PORT = local_port;
   }

   public void start(){
      if(runner == null){
         runner = new Thread(this);
         runner.start();
      }
   }

   public void run(){
      while(Thread.currentThread() == runner){
         try{
            System.out.print("Simple Client connects to monitor...");
            client = new Socket(MONITOR_ADDRESS, MONITOR_PORT);   
            HOST_ADDRESS = client.getLocalAddress().toString().substring(1);
            System.out.println("Done. Local address is " + HOST_ADDRESS);

            // Initiate input(to local) and output(to monitor) stream
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            long lastTimeout = System.currentTimeMillis();
            long currentTime; 
            
            String msg = in.readLine();
            while(msg != null){
               currentTime = System.currentTimeMillis();
               if(currentTime - lastTimeout >= CONNECTION_TIMEOUT){
                  System.out.println("**LOCAL RECONNECT MONITOR**");
                  sendIdent();
                  lastTimeout = currentTime;
                  msg = in.readLine();
                  System.out.println("MONITOR: " + msg);  
               }
               else{
                  if(msg.contains("REQUIRE:")) handleRequire(msg);
                  else if(msg.contains("RESULT:")) handleResult(msg);
                  System.out.println("MONITOR: " + msg);  
                  msg = in.readLine();
               }
            }
         }catch(IOException e){
            System.err.println(e);
            try{
               client.close();
            }catch(IOException ioe){
            }catch(NullPointerException n){
               try{
                  client.close();
               }catch(IOException ioe){}
            }
         }
      }
   }
}