package server;

import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;

import server.Account;
import server.AccountManager;
import server.TransactionManager;
import server.ServerReciever;
import utils.ColoredPrint;
import utils.PropertyHandler;

/**
 * Class [Server] creates a server based on the ip and port in serverConfig.
 * Then it will create a list of accounts provied by AccountConfig.
 * Next the class will listen for requests from clients and create a
 * ServerReciever thread to handle the requests.
 *
 * @author Zachary M. Hallemeyer
 */
public class Server
{
  static String serverOutputFile = "ServerOutput.txt";
  static HashMap<String, Account> accounts = new HashMap<String, Account>();
  static TransactionManager transactionManager;

  // Create server and accounts based on config files. Then it will listen
  // for requests from clients and create a ServerReciever thread to handle the
  // requests
  public static void main(String[] args)
  {
    // Initialize local variables
    Socket clientRequest = null;
    int port = 8888;
    String accountConfigFile = "server/AccountConfig.txt";
    int numOfAccounts;
    int defaultBalance;

    // Clear output file
    try
    {
      File file = new File(serverOutputFile);
      file.delete();
    }
    catch (Exception error)
    {
      // Do nothing as file just did not exist
    }

    // Initialize Transaction Manager
    TransactionManager transactionManager = new TransactionManager();

    // Initialize Accounts
    try
    {
      PropertyHandler propertyHandler = new PropertyHandler(accountConfigFile);
      numOfAccounts = Integer.parseInt(propertyHandler.getProperty("numOfAccounts"));
      defaultBalance = Integer.parseInt(propertyHandler.getProperty("defaultBalance"));
    }
    catch(Exception error)
    {
      ColoredPrint.print("Error in account config file: " + error, ColoredPrint.RED, serverOutputFile);
      ColoredPrint.print("Terminating program", ColoredPrint.RED, serverOutputFile);
      return;
    }
    accounts = AccountManager.createAccounts(numOfAccounts, defaultBalance);

    // Initialize server
    try( ServerSocket socket = new ServerSocket(port) )
    {
      ColoredPrint.print("Server started", ColoredPrint.PURPLE, serverOutputFile);
      // Run server (Listen for client requests)
      while(true)
      {
        try
        {
          clientRequest = socket.accept();

          // Start reciever thread for client request
          new ServerReciever(clientRequest).start();

        }
        catch (IOException error)
        {
          ColoredPrint.print("I/O error running server: " + error, ColoredPrint.RED, serverOutputFile);
        }
      }
    }
    catch(IOException error)
    {
      ColoredPrint.print("I/O error initializing server: " + error, ColoredPrint.RED, serverOutputFile);
    }
  }
}
