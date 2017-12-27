
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Lazarko
 */
public class Logic {
      public char[] hideWord(char[] word){
        int length = word.length;
           Character c = '_';
           char[] hiddenWord = new char[length];
           for(int i = 0; i < length; i++){
               hiddenWord[i] = c.charValue();
           }
           return hiddenWord;
       }
   
    
       public char[] findWord()throws FileNotFoundException{
           char[] NewWord = findRandomWord();
           return NewWord;
       }
    
    
       
       public char[] hangman(char[] word, char[] hidden, char[] guess){
           if(guess.length == 1){
             char[] newHidden = new char[hidden.length];  
             char c = Character.toLowerCase(guess[0]);
             for(int i = 0; i < hidden.length; i++){
                 if(Character.toLowerCase(word[i]) == c){
                     newHidden[i] = c;
                 }else {
                     newHidden[i] = hidden[i];
                 }
             }
             return newHidden;
           }else if(Arrays.equals(guess, word) == true){
               return word;
           }else{
               return hidden;
           }
       }
        
        // Den här metodet returnerar ett ord från "words.txt" filen 
        // mha readFromFile() metoden, och därmed returnerar en char[]
        private char[] findRandomWord() throws FileNotFoundException{
            ArrayList<String> listWords = readFromFile();
            int NO_WORDS = listWords.size();
            Random rand = new Random();
            int randIndex = rand.nextInt(NO_WORDS) + 1;
            String s = listWords.get(randIndex);
            char[] word = s.toCharArray();
            return word;
        }
        // Läser av innehållet från "words.txt" och sparar varje ord i en 
        // ArrayList<String> och returnerar det
        private ArrayList<String> readFromFile() throws FileNotFoundException{
            ArrayList<String> words;
            Scanner sc = new Scanner(new File("words.txt"));
            words = new ArrayList<String>();
            while(sc.hasNextLine()){
                String word = sc.nextLine();
                words.add(word);
            }
            return words;
        }
    
}
