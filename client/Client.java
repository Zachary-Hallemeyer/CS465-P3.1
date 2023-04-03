package client;

import java.util.*;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import client.ClientProxy;
import server.AccountManager;
import server.Account;
import message.Message;
import message.MessageTypes;

public class Client {

  static String[] accounts;

  public static void main(String[] args) {
    int currentPort = 8889;
    int numOfTransactions = 2;
    int serverPort = 8888;
    String serverIP = "127.0.0.1";

    initializeAccounts(serverIP, serverPort);

    for(int index = 0; index < numOfTransactions; index++) {
      new ClientProxy(currentPort + index,
                      serverPort,
                      serverIP).start();
    }

  }

  @SuppressWarnings("unchecked")
  private static void initializeAccounts(String serverIP, int serverPort) {
    System.out.println("Initializing Accounts");
    try {
      Socket serverConnection = new Socket(serverIP, serverPort);
      // Create input stream for data from server
      ObjectOutputStream outputStream = new ObjectOutputStream(
        serverConnection.getOutputStream());
      outputStream.writeObject(new Message(MessageTypes.ACCOUNTS_REQUEST, null));
      outputStream.flush();

      // Create input stream for data from server
      ObjectInputStream inputStream = new ObjectInputStream(
      serverConnection.getInputStream());

      Message newMessage = (Message) inputStream.readObject();
      accounts = (String[])newMessage.getContent();
      System.out.println(accounts);
      for(int index = 0; index < accounts.length; index++) {
        System.out.println(accounts[index]);
      }

    }
    catch (Exception error) {
      // TODO
      System.out.println("Uh oh: " + error);
    }
  }

}
