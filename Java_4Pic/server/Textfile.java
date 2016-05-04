package server;
import java.io.*;
import java.util.Random;

public class Textfile {
   
    //count number of lines in the words.txt file 
    public int countlines(String filename) throws IOException {
    	System.out.println("File is " + filename + "\n");
        LineNumberReader reader  = new LineNumberReader(new FileReader(filename));
        int cnt = 0;
        String lineRead = "";
        while ((lineRead = reader.readLine()) != null) {}
        cnt = reader.getLineNumber(); 
        reader.close();
        System.out.println("Count is" + cnt + "\n");
        return cnt;
    }
    
    //Method to choose a random word from words.txt
    public String chooseword(String file,int count){
        String chosen = new String();
        System.out.println("File is " + file + " Count is" + count);
	
	int Min=1;
	int Max=count;
	int randomWord = Min + (int)(Math.random() * ((Max - Min) + 1));
	System.out.println(randomWord);
        try {
        BufferedReader in = new BufferedReader(new FileReader(file));
        int cnt=0;
        
        while((cnt<randomWord) && (chosen = in.readLine()) != null ){ //skip other lines till the random chosen
            cnt ++;
        }
        in.close();
        } catch (IOException e) {
        }
        return chosen;
    }
    
    //Create a dashed word which contains equal number of dashes as the letters of the word picked from words.txt
     public String dashme(String word){
        /* Setting dashes */
	char positions[] = new char[word.length()];
            for (int i = 0; i < word.length(); i++) {
                positions[i] = '-';
            }
            String dashes = new String(positions);
        return dashes;
    }
     
}
