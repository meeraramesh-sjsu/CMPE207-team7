package server;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Handler extends Thread implements Serializable {

    private Socket clientSocket;
    protected String chosenword;
    Server2Client clientdata = new Server2Client();//server's response to client
    protected String Filename;
    Textfile file;
    
    /*  * Send a file to a connected socket.
     * <p>
     * First it sends file size in 4 bytes then the file's content.
     * </p>
     * <p>
     * Note: File size is limited to a 32bit signed integer, 2GB
     * </p>
     * 
     * @param os
     *           OutputStream of the connected socket
     * @param fileName
     *           The complete file's path of the image to send
     * @throws Exception
     * @see {@link receiveFile} for an example how to receive file at other side.
     * 
     */
    public static void sendFile(OutputStream os, String fileName) throws Exception
    {
        // File to send
        File myFile = new File(fileName);
        int fSize = (int) myFile.length();
        if (fSize < myFile.length())
        {
            System.out.println("File is too big'");
            throw new IOException("File is too big.");
        }

        // Send the file's size
        byte[] bSize = new byte[4];
        bSize[0] = (byte) ((fSize & 0xff000000) >> 24);
        bSize[1] = (byte) ((fSize & 0x00ff0000) >> 16);
        bSize[2] = (byte) ((fSize & 0x0000ff00) >> 8);
        bSize[3] = (byte) (fSize & 0x000000ff);
        // 4 bytes containing the file size
        os.write(bSize, 0, 4);

        // In case of memory limitations set this to false
        boolean noMemoryLimitation = true;

        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try
        {
            if (noMemoryLimitation)
            {
                // Use to send the whole file in one chunk
                byte[] outBuffer = new byte[fSize];
                int bRead = bis.read(outBuffer, 0, outBuffer.length);
                os.write(outBuffer, 0, bRead);
            }
            else
            {
                // Use to send in a small buffer, several chunks
                int bRead = 0;
                byte[] outBuffer = new byte[8 * 1024];
                while ((bRead = bis.read(outBuffer, 0, outBuffer.length)) > 0)
                {
                    os.write(outBuffer, 0, bRead);
                }
            }
            os.flush();
        }
        finally
        {
            bis.close();
        }
    }

    
    Handler(Socket s, String filename) {
        System.out.print("Server handler called");
            //thread constructor
            file = new Textfile();
            Filename = filename;//filename of the dictionary to  choose word from it
            this.clientSocket = s;
            //initialize Server2Client response values
            clientdata.score = 0;
            clientdata.FailAttempts = 10;
            clientdata.games = 0;
            clientdata.message = "";
            clientdata.info = "";

            //choose word from dictionary
            try {
                chosenword = file.chooseword(filename, file.countlines(filename));
            } catch (IOException ex) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Chosenword= "+chosenword);
            String image_Name="/home/karthik/Downloads/4Pic1Game-master/Java_Server/server/images/" + chosenword + ".jpeg";
            clientdata.setImageName(image_Name);
            //Creates the dashed word to send to the Client
            clientdata.word = new StringBuffer(file.dashme(chosenword));
            
    }//end of constructor

    @Override
    public void run() {
        try {
            boolean running = true;
            Object clientObj;
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            
             //creates the output stream to send the object to the client
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
				try {
					sendFile(out,clientdata.imageName);
					System.out.println("Sent a file to client");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            } catch (IOException e) {
                System.out.println(e.toString());
            }
            //creates the input stream to read the object which the client sent
            try {
                in = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                System.out.println(e.toString());
                return;
            }
           
            while (running) {
                //read object that the client have sent
                try {
                    clientObj = in.readObject();
                    //System.out.println("Received :" + ((Client2Server) clientObj).clientword);
                } catch (ClassNotFoundException cnfe) {
                    System.out.println(cnfe.toString());
                    return;
                } catch (OptionalDataException ode) {
                    System.out.println(ode.toString());
                    return;
                } catch (IOException ioe) {
                    System.out.println(ioe.toString());
                    return;
                }
                GameResult game = new GameResult();//creates the game class that implements the hangman cases
                if (clientObj instanceof Client2Server) {
                    if (((Client2Server) clientObj).getaction() == 1) {//Send action
                        //Implement the server's response based on the client's actions on the game
                        game.calculate(chosenword, clientdata, (Client2Server) clientObj);
                    }
                    else if (((Client2Server) clientObj).getaction() == 2) {//New Game
                        //Initiallize failattempts and delete previously viewed messages
                        clientdata.FailAttempts = 10;
                        clientdata.message = "";
                        clientdata.info = "";
                        //Make a new choice
                        chosenword = file.chooseword(Filename, file.countlines(Filename));
                        clientdata.word = new StringBuffer(file.dashme(chosenword));
                        try {
                        	   System.out.println("Chosenword= "+chosenword);
                               String image_Name="/home/karthik/Downloads/4Pic1Game-master/Java_Server/server/images/" + chosenword + ".jpeg";
                               clientdata.setImageName(image_Name);
							sendFile(out, clientdata.imageName);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                    else if (((Client2Server) clientObj).getaction() == 3) {//stop the client's running thread in the server side
                        running=false;
                    }
                }
                
                //send the servers response to the client
                try {
                    out.reset();
                    out.writeObject(clientdata);
                    out.flush();
                } catch (IOException e) {
                    System.out.println(e.toString());
                }

            }
            
            //closes the streams if the server wants to stop running
            try {
                out.close();
                in.close();

            } catch (IOException ioe) {
                System.out.println(ioe.toString());
            }

            System.out.println("Connection Closed");
            clientSocket.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
}
