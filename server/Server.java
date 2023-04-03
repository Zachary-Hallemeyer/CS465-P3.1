package server;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import server.Account;
import server.AccountManager;
import server.TransactionManager;
import server.ServerReciever;

public class Server {

  static HashMap<String, Account> accounts = new HashMap<String, Account>();
  static TransactionManager transactionManager;
  // static List<Account> accounts = new ArrayList<Account>();

  public static void main(String[] args) {
    // Initialize local variables
    Socket clientRequest = null;
    int port = 8888;

    // Initialize Transaction Manager
    TransactionManager transactionManager = new TransactionManager();
    // Initialize Accounts
    accounts = AccountManager.createAccounts(10, 100);

    // Initialize server
    try( ServerSocket socket = new ServerSocket(port) ){

      System.out.println("Server started");
      // Run server (Listen for client requests)
      while(true) {
        try {
          clientRequest = socket.accept();

          // Start reciever thread for client request
          new ServerReciever(clientRequest).start();

        }
        catch (IOException error) {
          System.out.println("I/O error running server: " + error);
        }
      }

    }
    catch(IOException error) {
      System.out.print("I/O error initializing server: " + error);
    }
  }
}
