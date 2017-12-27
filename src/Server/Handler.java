

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Lazarko
 */
public class Handler implements Runnable {
    
    private SocketChannel sc;
    private String msg;
 
    private char[] word;
    Logic hangman = new Logic();
    private char[] hiddenUpdate;
    private ByteBuffer buffer;
    StringBuilder sb;
    HashMap<Integer, StringBuilder> clients = new HashMap<>();
    
  
    private final String CLIENT_DISCONNECT = "Client has disconnected";
    private final String QUIT_MSG = "QUIT";
    private final String Wrong_MSG = "Hangman: Wrong!";
    private final String CORRECT_MSG = "Hangman: Correct, guess again";
    private final String DONE_MSG = "Hangman: Done, next word";
    

   
    public Handler(SocketChannel sc, String msg, HashMap clients){
        this.sc = sc;
        this.msg = msg;
        this.clients = clients;
        this.sb = (StringBuilder) clients.get(sc.hashCode());
       
    }

    @Override
    public void run() {
        
        try{
            
            if(sb.length() == 0){
                word = hangman.findWord();
                sb = new StringBuilder(word.length*2);
                char[] hidden = hangman.hideWord(word);
                hiddenUpdate = hidden;
                sb.append(hiddenUpdate);
                sb.append(word);
            }else{
                char[] c = new char[sb.capacity()/2];
                sb.getChars(sb.capacity()/2, sb.capacity(), c, 0);
                word = c;
                hiddenUpdate = sb.substring(0, sb.capacity()/2).toCharArray();
               
                
            }
          
           System.out.println(Arrays.toString(word));
         
                     
           char[] inChar = msg.trim().toCharArray();
           char[] check = hangman.hangman(word, hiddenUpdate, inChar);
       
           
           if(msg.startsWith(QUIT_MSG) == true){
               sendToClient(QUIT_MSG);
               disconnect();
           }else if(Arrays.equals(check, hiddenUpdate) == true){
               
               sendToClient(Wrong_MSG);   
               
           } else if(Arrays.equals(check, word) == true){
               sendToClient(Arrays.toString(word) + " " + DONE_MSG);
               sb.setLength(0);
               
           } else if(Arrays.equals(check, hiddenUpdate) == false){
               StringBuilder t = new StringBuilder();
               t.append(check);
               sb.replace(0, sb.capacity()/2, t.toString());
               
               String checkString = Arrays.toString(check);
               sendToClient(checkString + " " + CORRECT_MSG);
               hiddenUpdate = check; 
             
           
           }
       
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
         clients.replace(sc.hashCode(), sb);
    }
    
    private void sendToClient(String toClient){
        buffer = ByteBuffer.wrap(toClient.getBytes());
        try{
            sc.write(buffer);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private void disconnect(){
        try{
            sc.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println(CLIENT_DISCONNECT);
        
    }
    
    
}
