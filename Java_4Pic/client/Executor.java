package client;

import server.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Executor extends javax.swing.JFrame implements Runnable,ClientInterface {
    //GuessGameUI page;
    private Client2Server clientinput;//the actions of the client
    private Server2Client serverinput;
    private boolean running;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
   
    /*
	 * Receive an image file from a connected socket and save it to a file.
	 */
	public static String receiveFile(InputStream is, String fileName) throws Exception
	{
		String fileInES = System.getProperty("user.dir") + "/client/" + fileName;
	    // read 4 bytes containing the file size
	    byte[] bSize = new byte[4];
	    int offset = 0;
	    while (offset < bSize.length)
	    {
	        int bRead = is.read(bSize, offset, bSize.length - offset);
	        offset += bRead;
	    }
	    // Convert the 4 bytes to an int
	    int fileSize;
	    fileSize = (int) (bSize[0] & 0xff) << 24 
	               | (int) (bSize[1] & 0xff) << 16 
	               | (int) (bSize[2] & 0xff) << 8
	               | (int) (bSize[3] & 0xff);

	    // buffer to read from the socket
	    // 8k buffer is good enough
	    byte[] data = new byte[8 * 1024];

	    int bToRead;
	    FileOutputStream fos = new FileOutputStream(fileInES);
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    while (fileSize > 0)
	    {
	        // make sure not to read more bytes than filesize
	        if (fileSize > data.length) bToRead = data.length;
	        else bToRead = fileSize;
	        int bytesRead = is.read(data, 0, bToRead);
	        if (bytesRead > 0)
	        {
	            bos.write(data, 0, bytesRead);
	            fileSize -= bytesRead;
	        }
	    }
	    bos.close();
		return fileInES;
	}

    public void setClientinput(Client2Server clientinput) {
        this.clientinput = clientinput;
    }
    private Socket clientSocket = null;
    Executor(String host, int port, Client2Server clientinput) throws UnknownHostException, IOException {//thread constructor
        this.clientinput = clientinput;
        InetAddress server = InetAddress.getByName(host);
        System.out.println(server);
       this.clientSocket = new Socket(server, port);
        
        if (this.clientSocket == null) {
            System.out.println("socket null");
        }
        this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
        this.in = new ObjectInputStream(this.clientSocket.getInputStream());
        try
		  {
        	System.out.println("Connected");
			// The file name must be simple file name, without file separator '/'
		    String filename=  receiveFile(in, "image.jpeg");
		    clientinput.setfilename(filename);
		    System.out.print("Recieved image from Server");
		  }
		  catch (Exception e)
		  {
		      e.printStackTrace();
		  }
    }

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        running = true;

        while (running) {
            if (clientinput.getaction() == 9) {
                try {
                    running = false;
                    //Close streams and exit
                    out.close();
                    in.close();
                    clientSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    public void stop() {
        running = false;
    }

    //sends the client's guess to the server and returns the response from server
    public Server2Client sendclientanswer(Client2Server clientanswer) {
        try {//send
            out.reset();
            out.writeObject(clientanswer);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //receive
        Server2Client a = new Server2Client();
        Object serverObj = new Object();
        try {
        	if(clientinput.getaction()==2)
        	{
        		  String filename;
				try {
					filename = receiveFile(in, "image.jpeg");
	       		    clientinput.setfilename(filename);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       		    System.out.print("Recieved image from Server");
        		
        	}
        	serverObj = in.readObject();

        } catch (IOException ex) {
            Logger.getLogger(Executor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe.toString());
            return null;
        }
        
        if (serverObj instanceof Server2Client) {
            a = (Server2Client) serverObj;
            return a;
        }else
        	System.out.println("Not instance");
        System.out.println("Returning nothing");
        return null;
    }

    public void setEnvironment(final Server2Client serveranswer) {
    }
}
