package server;

public class Client2Server implements Task{
    private static final long serialVersionUID = -7386258182412348265L;
    private int action;
    protected String clientword;//the letter or word that client guesses
    protected String filename;
    
    public void setfilename(String filename) {
        this.filename=filename;
    }
    
    public  String getfilename(){return filename;}
    public void execute() {
	}
   public  String getclientword(){return clientword;}
    public int getaction(){return action;}
     public void setaction(int i) {
        action=i;
    }
    public void setclientword(String word) {
        clientword=word;
    }
    
}
