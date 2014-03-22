import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.io.*;
import java.math.*;

public class ZeroKnowledge{
   // ZK Initiator
   public BigInteger sqr = new BigInteger("2", 10);
   public BigInteger v;
   public BigInteger s;
   public BigInteger n;
   public int rounds;
   public BigInteger[] R;
   public int[] A;
   public int subsetAsize;
   public BigInteger[] K;
   public BigInteger[] J;

   // public BigInteger senderV;
   // public BigInteger senderN;
   // public int rounds;
   public BigInteger[] RR;
   public boolean check;

   public ZeroKnowledge(){
      try{
         KeyFactory kf = KeyFactory.getInstance("RSA");
         KeyPairGenerator kpgRSA = KeyPairGenerator.getInstance("RSA");
         
         kpgRSA.initialize(512);
         KeyPair kpKeyPair = kpgRSA.genKeyPair();
         RSAPublicKeySpec x = 
            kf.getKeySpec(kpKeyPair.getPublic(), RSAPublicKeySpec.class);
         n = x.getModulus();
         
         // Returns a random 512 bit BigInteger
         s = new BigInteger(512, new SecureRandom());
         
         // Now we have our public value
         v = s.pow(2).mod(n);
      }catch (NoSuchAlgorithmException e) {}
      catch (InvalidKeySpecException e) {}
   }

   public String getPublicKey() {
      return "PUBLIC_KEY "+v+" "+n;
   }

   public void saveRounds(String msg) {
      StringTokenizer t = new StringTokenizer(msg, " ");
      t.nextToken();
      rounds = Integer.parseInt(t.nextToken());
   }

   public String getAuthorizeSet () {
      Random rnd = new Random();
      R = new BigInteger[rounds];
      String msg = "AUTHORIZE_SET ";
      for (int i = 0; i < rounds; i++) {
         R[i] = new BigInteger(256, rnd);
         msg = msg.concat(R[i].modPow(sqr,n).toString()).concat(" ");
      }
      return msg;
   }

   public void saveSubsetA (String msg) {
      StringTokenizer t = new StringTokenizer(msg," ");
      subsetAsize = t.countTokens()-1;
      A = new int[subsetAsize];
      t.nextToken();
      for (int i=0 ; i < subsetAsize ; i++) {
         A[i] = Integer.parseInt(t.nextToken());
      }
   }

   public String getSubsetK () {
      String msg = "SUBSET_K ";
      for (int i=0 ; i < subsetAsize ; i++) {
         msg += R[A[i]].multiply(s).modPow(sqr, n) + " ";
      }
      return msg;
   }

   public String getSubsetJ () {
      String msg = "SUBSET_J ";
      for (int i=0 ; i < rounds ; i++) {
         int j=0;
         for ( ; j < subsetAsize ; j++)
            if (A[j] == i) break;
         if (j != subsetAsize) continue;
         msg += R[i].modPow(sqr, n) + " ";
      }
      return msg;
   }

   public void savePublicKey (String msg) {
      StringTokenizer t = new StringTokenizer(msg, " ");
      t.nextToken();
      v = new BigInteger(t.nextToken());
      n = new BigInteger(t.nextToken());
   }

   public String getRounds(int rnds) {
      rounds = rnds;
      return "ROUNDS "+rnds;
   }

   public void saveAuthorizeSet (String msg) {
      StringTokenizer t = new StringTokenizer(msg," ");
      t.nextToken();
      RR = new BigInteger[rounds];
      for (int i=0 ; i < rounds ; i++) {
         RR[i] = new BigInteger(t.nextToken());
      }
   }

   public String getSubsetA () {
      String msg = "SUBSET_A ";
      for (int i=0 ; i < rounds ; i += 2) msg += i + " ";
      return msg;
   }

   public boolean checkSubsetK(String msg) {
      check = true;
      StringTokenizer t = new StringTokenizer(msg, " ");
      t.nextToken();
      for (int i=0 ; i < rounds ; i += 2) {
         BigInteger a1 = RR[i].multiply(v).mod(n);
         BigInteger a2 = new BigInteger(t.nextToken());
         if (!a1.equals(a2)) {
            check = false;
            return false;
         }
      }
      return true;
   }

   public boolean checkSubsetJ(String msg) {
      StringTokenizer t = new StringTokenizer(msg, " ");
      t.nextToken();
      for (int i=1 ; i < rounds ; i += 2) {
         BigInteger a1 = RR[i];
         BigInteger a2 = new BigInteger(t.nextToken());
         if (!a1.equals(a2)) {
            check = false;
            return false;
         }
      }
      return true;
   }

   public String response() {
      if (check) return "TRANSFER_REQUEST ACCEPT";
      else return "TRANSFER_REQUEST REJECT";
   }
}