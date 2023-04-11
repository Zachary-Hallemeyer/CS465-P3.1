package client;

import java.util.*;
import java.lang.Thread;
import java.lang.Math;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileWriter;

import message.Message;
import message.MessageTypes;
import client.Client;
import utils.ColoredPrint;

/**
 * Class [ClientProxy] is a thread that is called by Client class. The thread
 * will create a random transaction and send it to the server and will listen for
 * a reponse from the server. If the server replies with a commit success message
 * the thread will shutdown. If the server replies with an abort message
 * the thread will restart.
 *
 * @author Zachary M. Hallemeyer
 */
public class ClientProxy extends Thread
{
  int portToListenOn;
  int serverPort;
  String serverIP;
  Message transaction;

  // Constructor
  public ClientProxy(int portToListenOn, int serverPort, String serverIP,
                     Message transaction)
  {
    this.portToListenOn = portToListenOn;
    this.serverPort = serverPort;
    this.serverIP = serverIP;
    this.transaction = transaction;
  }

  // This function connects to server and sends a random transaction.
  // It will then wait for a response.
  // If the server replies with a commit success message, the thread will shutdown.
  // If the server replies with an abort message, the thread will restart.
  public void run()
  {
    ColoredPrint.print("Running client proxy", ColoredPrint.PURPLE, Client.clientOutputFile);

    try
    {
      // Connect to server
      Socket serverConnection = new Socket(serverIP, serverPort);
      ObjectOutputStream outputStream = new ObjectOutputStream(
                                            serverConnection.getOutputStream());

      // Send Transaction to server
      sendTransactionToServer(outputStream);

      // Open input stream
      ObjectInputStream inputStream = new ObjectInputStream(
                                          serverConnection.getInputStream());
      // // Listen for response from server
      try
      {
        Message newMessage = (Message) inputStream.readObject();
        parseMessage(newMessage, outputStream);
      }
      catch(Exception error)
      {
        ColoredPrint.print("Error in recieving transaction message from server: " + error, ColoredPrint.RED, Client.clientOutputFile);
      }

    }
    catch(IOException error)
    {
      ColoredPrint.print("Error in connecting to server: " + error, ColoredPrint.RED, Client.clientOutputFile);
    }
  }

  // Parses message from server.
  // If the server replies with a commit success message, the thread will shutdown.
  // If the server replies with an abort message, the thread will restart.
  private void parseMessage(Message message, ObjectOutputStream outputStream)
  {
    // Check if abort
    if(message.getType() == MessageTypes.TRANSACTION_FAIL)
    {
      ColoredPrint.print(message.getContent() + " Transaction has been aborted...Restarting", ColoredPrint.RED, Client.clientOutputFile);
      // Restart thread
      this.run();
    }
    // Check if success
    if(message.getType() == MessageTypes.TRANSACTION_COMMIT)
    {
      ColoredPrint.print(message.getContent() + " Transction commited!!!", ColoredPrint.GREEN, Client.clientOutputFile);
      // When finished call clientProxyFinished in Client
      Client.clientProxyFinished();
    }
  }

  // This function sends transaction Message to server
  private void sendTransactionToServer(ObjectOutputStream outputStream)
  {
    try
    {
      // Send Transaction to server
      ColoredPrint.print("Sending transction to server: " + transaction.getContent(), ColoredPrint.PURPLE, Client.clientOutputFile);
      while(!Client.startSendingTransactions);
      outputStream.writeObject(transaction);
      outputStream.flush();
    }
    catch (Exception error)
    {
      ColoredPrint.print("Error in sending transaction to server: " + error, ColoredPrint.RED, Client.clientOutputFile);
    }
  }

}
