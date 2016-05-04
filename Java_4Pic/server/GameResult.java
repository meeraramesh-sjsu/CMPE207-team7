package server;

public class GameResult {

    public GameResult() {
    }

    @SuppressWarnings("unused")
    public void getHint(String correctword, Server2Client serveranswer) {
    	StringBuffer currentword = serveranswer.getWord();
    	char hint_letter = '-';
    	for (int i = 0; i < currentword.length(); i++) {
    		if (String.valueOf(currentword.charAt(i)).equals("-")) {
    			hint_letter = correctword.charAt(i);
    			serveranswer.word.setCharAt(i, hint_letter);
    			break;
    		}
    	}
    	serveranswer.message = "One Hint used.";
    	serveranswer.info = "No more hints available";
    }

    public void calculate(String chosenword, Server2Client serveranswer, Client2Server clientanswer) {
        if (clientanswer.clientword.isEmpty()) {//if client sent nothing
            serveranswer.message = "";
            serveranswer.info = "Click on a letter first!";
        } else if (clientanswer.clientword.length() == 1) {//client send letter
            //case 1 : Letter does not exist    
            if (chosenword.indexOf(clientanswer.clientword) == -1) {
                serveranswer.message = "Letter does not exist";
                serveranswer.FailAttempts--;
                serveranswer.info = String.valueOf(serveranswer.FailAttempts)+" attempts are left!";
                if (serveranswer.FailAttempts == 0) {
                    serveranswer.message = "You lost!! Better luck next time";
                    serveranswer.word = new StringBuffer(chosenword);//server reveals to client the actual word
                    serveranswer.info = "Click on New Game for another chance!";
                    serveranswer.games++;
                }
            }

            char[] c = new char[clientanswer.clientword.length()];
            c = clientanswer.clientword.toCharArray();

            //if letter already guessed 
            if ((serveranswer.word.toString()).indexOf(clientanswer.clientword) != -1) {
                serveranswer.message = "Letter has already been guessed.";
                serveranswer.info = "Try another one!";

            } else {//Find where is the Letter
                for (int i = 0; i < chosenword.length(); i++) {
                    if (String.valueOf(chosenword.charAt(i)).equalsIgnoreCase(String.valueOf(c[0]))) {
                        serveranswer.word.setCharAt(i, c[0]);
                        if (serveranswer.word.indexOf("-") == -1) {//No dash, word is whole!
                            serveranswer.word = new StringBuffer(chosenword);
                            serveranswer.message = "Hurray!! You won!";
                            serveranswer.info = "Click on New Game for another word";
                            serveranswer.score++;
                            serveranswer.games++;
                        } else {
                            serveranswer.message = "Letter correct!";
                            serveranswer.info = "Keep guessing :D";
                        }
                    }

                }
            }
        } else if (clientanswer.clientword.equalsIgnoreCase(chosenword)) {//Client sent word
            serveranswer.word = new StringBuffer(chosenword);
            System.out.println("win word: " + serveranswer.word);
            serveranswer.message = "Hurray!! You won!";
            serveranswer.info = "Click on New Game for another word";
            serveranswer.score++;
            serveranswer.games++;

        } else {//Client sent wrong word
            serveranswer.message = "Word is not correct";
            serveranswer.info = "Try again!";
            serveranswer.FailAttempts--;
            if (serveranswer.FailAttempts == 0) {
                serveranswer.message = "You lost!! Better luck next time";
                serveranswer.word = new StringBuffer(chosenword);
                serveranswer.info = "Click on New Game for another chance!";
                serveranswer.games++;

            }
        }
    }
}
