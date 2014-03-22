import java.io.*;
import java.util.Date;
import java.math.BigInteger;
import java.security.*;

class Karn
{
   final int RADIX = 32;
   final int PADSIZE = 40; // Plain text buffer

   private byte[] key;
   private byte[] key_left;
   private byte[] key_right;

   static SecureRandom sr = null;
   static MessageDigest md = null;

   Karn(BigInteger keyBi) {
      if (sr == null) sr = new SecureRandom();
      key = keyBi.toByteArray();

      // Digest encryption needs keys split into two halves
      key_left = new byte[key.length / 2];
      key_right = new byte[key.length / 2];


      for (int i = 0; i < key.length / 2; i++) {
         key_left[i] = key[i];
         key_right[i] = key[(i + key.length / 2)];
      }
      try{
         md = MessageDigest.getInstance("SHA");
      } catch (NoSuchAlgorithmException e) {
         System.err.println("Yow! NoSuchAlgorithmException. Abandon all hope");
      }
   }

   // Encrypt the string using the karn algorithm
   String encrypt(String plaintext) {
      byte[] plain_left, plain_right;
      byte[] ciph_left, ciph_right;
      byte[] digest;
      
      // These buffers are used for the encryption.
      byte input[] = StringToBytes(plaintext); // Pad the string 
        
      plain_left =  new byte[PADSIZE/2];
      plain_right = new byte[PADSIZE/2];

      ciph_left =  new byte[PADSIZE/2];
      ciph_right =  new byte[PADSIZE/2];

      digest = new byte[PADSIZE/2];  // Temp storage for the hash

      // Our pointer into the workspace
      int cursor = 0;
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      // Guard Byte for the ciphertext
      out.write(42);

      while (cursor < input.length) {
         // Copy the next slab into the left and right 
         for (int i=0 ; i < PADSIZE/2 ; i++) {
            plain_left[i] = input[cursor + i];
            plain_right[i] = input[cursor + PADSIZE/2 + i];        
         }
         
         // Hash the left plaintext with the left key
         md.reset(); // Start the hash fresh
         md.update(plain_left);
         md.update(key_left);
         digest = md.digest(); // Get out the digest bits 
         // XOR the digest with the right plaintext for the right c-text 
         // Right half
         for (int i=0 ; i < PADSIZE/2 ; i++)
            ciph_right[i] = (byte)(digest[i] ^ plain_right[i]);
         
         // Now things get a little strange
         md.reset();
         md.update(ciph_right);
         md.update(key_right);
         digest = md.digest();
         for (int i=0 ; i < PADSIZE/2 ; i++)
            ciph_left[i] = (byte) (digest[i] ^ plain_left[i]);
         
         out.write(ciph_left,0,PADSIZE/2);
         out.write(ciph_right,0,PADSIZE/2);
         cursor += PADSIZE;
      }
      BigInteger bi_out = new BigInteger(out.toByteArray());
      return(bi_out.toString(RADIX));
   }

   String decrypt(String ciphertext){
      BigInteger bi = new BigInteger(ciphertext, RADIX);
      byte[] input = bi.toByteArray();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(input, 1, input.length - 1);
      input = out.toByteArray();

      byte[] plain_left = new byte[PADSIZE / 2];
      byte[] plain_right = new byte[PADSIZE / 2];

      byte[] ciph_left = new byte[PADSIZE / 2];
      byte[] ciph_right = new byte[PADSIZE / 2];

      byte[] digest = new byte[PADSIZE / 2];

      int cursor = 0;
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      while (cursor < input.length)
      {
         for (int j = 0; j < PADSIZE / 2; j++) {
            ciph_left[j] = input[(cursor + j)];
            ciph_right[j] = input[(cursor + PADSIZE / 2 + j)];
         }

         md.reset();
         md.update(ciph_right);
         md.update(key_right);
         digest = md.digest();
         for (int j = 0; j < PADSIZE / 2; j++) {
            plain_left[j] = (byte)(digest[j] ^ ciph_left[j]);
         }

         md.reset();
         md.update(plain_left);
         md.update(key_left);
         digest = md.digest();
         for (int j = 0; j < PADSIZE / 2; j++)
            plain_right[j] = (byte)(digest[j] ^ ciph_right[j]);
         buffer.write(plain_left, 0, PADSIZE / 2);
         buffer.write(plain_right, 0, PADSIZE / 2);
         cursor += PADSIZE;
      }

      String str = StripPadding(buffer.toByteArray());
      return str;
   }

   private byte[] StringToBytes(String input)
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      byte[] arrayOfByte = input.getBytes();
      int i = input.length();

      out.write(arrayOfByte, 0, i);
      out.write(0);

      int j = 40 - (i + 1) % 40;
      arrayOfByte = new byte[j];
      sr.nextBytes(arrayOfByte);
      out.write(arrayOfByte, 0, j);
      return out.toByteArray();
   }

   private String StripPadding(byte[] input)
   {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      for (int i = 0; (i < input.length) && (input[i] != 0); i++)
         buffer.write(input[i]);
      return new String(buffer.toByteArray());
   }
}