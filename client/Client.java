package client;

import java.util.*;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.File;

import client.ClientProxy;
import server.AccountManager;
import server.Account;
import message.Message;
import message.MessageTypes;
import utils.ColoredPrint;
import utils.PropertyHandler;

 /**
 * Class [Client] allows a user to send a configurable amount of
 * random transaction to the associated OCC transaction server.
 * When this program is run, the program will request a list of accounts from
 * the server. With this list, it will then create a configurable amount of
 * ClientProxy threads to send one random transaction each. After the threads
 * are completed, the program will shut down
 *
 * @author Zachary M. Hallemeyer
 */
public class Client
{

  static String clientOutputFile = "ClientOutput.txt";
  static boolean startSendingTransactions = false;
  static String[] accounts;
  static int serverPort;
  static String serverIP;
  static int activeThreads = 0;

  // Gets config options and requests a list of accounts from the server.
  // Then the server create a configurable amount of ClientProxy threads
  // to send one random transaction each
  public static void main(String[] args)
  {
    int currentPort = 8889;
    int numOfTransactions;
    int minAmountToMove;
    int maxAmountToMove;
    String serverConfigFile = "server/ServerConfig.txt";
    String transactionConfigFile = "client/TransactionConfig.txt";

    // Clear output file
    try
    {
      File file = new File(clientOutputFile);
      file.delete();
    }
    catch (Exception error)
    {
      // Do nothing as file just did not exist
    }

    // Get server ip and port from server config
    try
    {
      PropertyHandler propertyHandler = new PropertyHandler(serverConfigFile);
      serverIP = propertyHandler.getProperty("IP");
      serverPort = Integer.parseInt(propertyHandler.getProperty("PORT"));
    }
    catch(Exception error)
    {
      ColoredPrint.print("Error in server config file: " + error, ColoredPrint.RED, clientOutputFile);
      ColoredPrint.print("Terminating program", ColoredPrint.RED, clientOutputFile);
      return;
    }

    // Get amount of transactions from transaction config
    try
    {
      PropertyHandler propertyHandler = new PropertyHandler(transactionConfigFile);
      numOfTransactions = Integer.parseInt(propertyHandler.getProperty("numOfTransactions"));
      minAmountToMove = Integer.parseInt(propertyHandler.getProperty("minAmountToMove"));
      maxAmountToMove = Integer.parseInt(propertyHandler.getProperty("maxAmountToMove"));
    }
    catch(Exception error)
    {
      ColoredPrint.print("Error in transaction config file: " + error, ColoredPrint.RED, clientOutputFile);
      ColoredPrint.print("Terminating program", ColoredPrint.RED, clientOutputFile);
      return;
    }

    // Get accounts from serve
    initializeAccounts(serverIP, serverPort);

    // For each transaction, create a ClientProxy thread to handle each transaction
    for(int index = 0; index < numOfTransactions; index++)
    {
      Message transaction = createRandomTransaction(minAmountToMove,
                                                    maxAmountToMove);
      new ClientProxy(currentPort + index, serverPort, serverIP, transaction)
                      .start();
      activeThreads++;
    }
    startSendingTransactions = true;
  }

  // Requests a list of all accounts from server. If a valid list is not recieved
  // The function will shut down the program
  @SuppressWarnings("unchecked")
  private static void initializeAccounts(String serverIP, int serverPort)
  {
    ColoredPrint.print("Initializing Accounts", ColoredPrint.PURPLE, clientOutputFile);
    try
    {
      Socket serverConnection = new Socket(serverIP, serverPort);

      // Create input stream for data from server
      ObjectOutputStream outputStream = new ObjectOutputStream(
        serverConnection.getOutputStream());
      // Request list of accounts from server
      outputStream.writeObject(new Message(MessageTypes.ACCOUNTS_REQUEST, null));
      outputStream.flush();

      // Create input stream for data from server
      ObjectInputStream inputStream = new ObjectInputStream(
      serverConnection.getInputStream());

      // Convert message to a list of strings which are account keys
      Message newMessage = (Message) inputStream.readObject();
      accounts = (String[])newMessage.getContent();
    }
    catch (Exception error)
    {
      ColoredPrint.print("Error in initializing accounts: " + error, ColoredPrint.RED, clientOutputFile);
      ColoredPrint.print("Terminating program", ColoredPrint.RED, clientOutputFile);
    }
  }

  // This function returns a random transaction by choosing: random withdraw account,
  // random deposit account, and random amount to move
  private static Message createRandomTransaction(int minAmountToMove,
                                                 int maxAmountToMove)
  {
    int withdrawIndex;
    int depositIndex;

    // Get random withdraw and deposit index
    // (loop until the random indexes are not the same)
    do
    {
      withdrawIndex = getRandomInt(0, Client.accounts.length);
      depositIndex = getRandomInt(0, Client.accounts.length);
    }
    while(withdrawIndex == depositIndex);

    String withdrawKey =  Client.accounts[withdrawIndex];
    String depositKey  = Client.accounts[depositIndex];

    int amountToMove  = getRandomInt(minAmountToMove, maxAmountToMove);

    // return random transaction
    return new Message(MessageTypes.TRANSACTION, withdrawKey + ","
                                                 + depositKey + ","
                                                 + String.valueOf(amountToMove));
  }

  // return a random integer (min and max inclusive)
  private static int getRandomInt(int min, int max)
  {
    return min + (int)(Math.random() * (max - min));
  }

  // Check if there are no remaining active ClinetProxy threads
  // If there are no active threads, send request to server to print all accounts
  // and balances
  // If there are active threads, then do nothing
  // This function is called after a transaction commited message is recieved from server
  public static void clientProxyFinished()
  {
    activeThreads--;
    if(activeThreads <= 0)
    {
      try
      {
        Socket serverConnection = new Socket(serverIP, serverPort);

        // Create input stream for data from server
        ObjectOutputStream outputStream = new ObjectOutputStream(
        serverConnection.getOutputStream());
        // Request list of accounts from server
        outputStream.writeObject(new Message(MessageTypes.PRINT_ACCOUNTS, null));
        outputStream.flush();
      }
      catch (Exception error)
      {
        ColoredPrint.print("Error in connecting to server to print account: " + error
                           , ColoredPrint.RED, clientOutputFile);
      }
    }
  }
}
