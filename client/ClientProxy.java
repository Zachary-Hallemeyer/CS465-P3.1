package client;

import java.lang.Thread;
import java.lang.Math;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import message.Message;
import message.MessageTypes;
import client.Client;

public class ClientProxy extends Thread{

  int portToListenOn;
  int serverPort;
  String serverIP;
  Message transaction;

  public ClientProxy(int portToListenOn, int serverPort, String serverIP) {
    this.portToListenOn = portToListenOn;
    this.serverPort = serverPort;
    this.serverIP = serverIP;
    transaction = createRandomTransaction();
  }

  public void run() {
    System.out.println("Running client proxy");

    try {
      // Connect to server
      Socket serverConnection = new Socket(serverIP, serverPort);
      ObjectOutputStream outputStream = new ObjectOutputStream(
                                            serverConnection.getOutputStream());

      // Send Transaction to server
      sendTransactionToServer(outputStream);
      // sendTransactionToServer(outputStream);

      // Open input stream
      ObjectInputStream inputStream = new ObjectInputStream(
                                          serverConnection.getInputStream());
      // // Listen for response from server
      try {
        Message newMessage = (Message) inputStream.readObject();

        // sendTransactionToServer(outputStream);
        parseMessage(newMessage, outputStream);
      }
      catch(Exception error) {

      }

    }
    catch(IOException error) {
      // TODO
    }

    System.out.println("Thread ending");
  }

  private void parseMessage(Message message, ObjectOutputStream outputStream) {
    if(message.getType() == MessageTypes.TRANSACTION_FAIL) {
      System.out.println("Transaction has been aborted...Restarting");
      this.run();
    }
    if(message.getType() == MessageTypes.TRANSACTION_COMMIT) {
      System.out.println("Transction commited!!!");
    }
  }

  private void sendTransactionToServer(ObjectOutputStream outputStream) {
    try {
      // Send Transaction to server
      System.out.println("Sending transction to server: " + transaction.getContent());
      outputStream.writeObject(transaction);
      outputStream.flush();
    }
    catch (Exception error) {}
  }

  private Message createRandomTransaction() {
    // Make these min and max configurable in config file
    int minAmountToMove = 10;
    int maxAmountToMove = 50;

    int withdrawIndex;
    int depositIndex;

    do
    {
      withdrawIndex = getRandomInt(0, Client.accounts.length);
      depositIndex = getRandomInt(0, Client.accounts.length);
    }
    while(withdrawIndex == depositIndex);

    String withdrawKey =  Client.accounts[withdrawIndex];
    String depositKey  = Client.accounts[depositIndex];

    int amountToMove  = getRandomInt(minAmountToMove, maxAmountToMove);

    return new Message(MessageTypes.TRANSACTION, withdrawKey + ","
                                                 + depositKey + ","
                                                 + String.valueOf(amountToMove));
  }

  private int getRandomInt(int min, int max) {
    return min + (int)(Math.random() * (max - min));
  }

}
