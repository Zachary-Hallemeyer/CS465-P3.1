package server;

import java.lang.Thread;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import server.Account;
import server.Server;
import message.Message;
import message.MessageTypes;
import utils.ColoredPrint;


/**
 * Class [ServerThread] accepts messages from clients.
 * The accepted requests are either a request for a list of existing clients
 * or a transaction message.
 *
 * @author Zachary M. Hallemeyer
 */
public class ServerReciever extends Thread
{

  Socket socket;

  public ServerReciever(Socket socket)
  {
    this.socket = socket;
  }

  // This function accepts a Message from a client and parse the message with
  // the function parseMessage
  public void run()
  {
    try
    {
      // Create a new input stream for client
      ObjectInputStream inputStream = new ObjectInputStream(
                                            socket.getInputStream());

      // Get message from input stream
      try
      {

        Message newMessage = (Message) inputStream.readObject();
        // Parse and process message
        parseMessage(newMessage);

      }
      catch (Exception error)
      {
        ColoredPrint.print("Data from client could not be converted to message object: " + error, ColoredPrint.RED, Server.serverOutputFile);
      }

    }
    catch(Exception error)
    {
      ColoredPrint.print("Could not process message from peer: " + error, ColoredPrint.RED, Server.serverOutputFile);
    }
  }

  // The function parses Messages from Clients
  // If the message is a request for a list of existing accounts:
  //  A list of accounts is sent to the client
  // If the message is a transaction:
  //  The transaction is parsed and will be processed with the use of
  //  the class TransactionManager (if the transaction is valid)
  private void parseMessage(Message message)
  {
    // Check if client is requesting account list
    if(message.getType() == MessageTypes.ACCOUNTS_REQUEST)
    {
      Message accountListMessage = new Message(MessageTypes.ACCOUNTS_REQUEST,
                                               Server.accounts.keySet()
                                               .toArray(String[]::new));
      sendMessageToClient(accountListMessage);
    }

    // Check if client is requesting a transaction
    if(message.getType() == Message.TRANSACTION
        && isValidTransactionMessage(message))
    {
      // TODO: Put this code into a function with validation check
      String[] messageArray = message.getContent().toString().split(",");
      String accountWithdrawKey = messageArray[0];
      String accountDepositKey = messageArray[1];
      int amountToMove = Integer.parseInt(messageArray[2]);

      Server.transactionManager.initializeWriteTransaction(
                                    Server.accounts.get(accountWithdrawKey),
                                    Server.accounts.get(accountDepositKey),
                                    amountToMove,
                                    this
                                    );
    }
    if(message.getType() == MessageTypes.PRINT_ACCOUNTS)
    {
      AccountManager.printAccounts(Server.accounts, true);
    }
  }

  // Sends the Message provided by parameter to the assoicated client
  public void sendMessageToClient(Message message)
  {
    try
    {
      // Send message to client
      ObjectOutputStream outputStream = new ObjectOutputStream(
                                              socket.getOutputStream());
      outputStream.writeObject(message);
      outputStream.flush();
    }
    catch (Exception error)
    {
      ColoredPrint.print("Message to client failed: " + error, ColoredPrint.RED, Server.serverOutputFile);
    }
  }

  // Closes this class' socket
  public void closeSocket()
  {
    // Close socket and input stream
    try
    {
      socket.close();
    }
    catch (Exception error)
    {
      ColoredPrint.print("Error in closing socket: " + error, ColoredPrint.RED, Server.serverOutputFile);
    }
  }

  // Returns true if the transaction is valid
  // Returns false otherwise
  // Message should be in the following form: <string>,<string>,<integer>
  private boolean isValidTransactionMessage(Message message)
  {
      String[] messageArray = message.getContent().toString().split(",");
      int desiredStringCount = 3;
      int withdrawKeyIndex = 0;
      int depositKeyIndex = 1;
      int amountToMoveIndex = 2;
      int amountToMove = 0;

      // Check if correct string count
      if(messageArray.length != desiredStringCount)
      {
        return false;
      }

      // Check if withdraw and deposit accounts exist in system
      if(   !isAccountInSystem(messageArray[withdrawKeyIndex])
         && !isAccountInSystem(messageArray[depositKeyIndex]))
      {
        return false;
      }

      // Check if 'amountToMove' is a number
      try
      {
        amountToMove = Integer.parseInt(messageArray[amountToMoveIndex]);
      }
      // 'amountToMove' is not a number
      catch (Exception error)
      {
        ColoredPrint.print("Transaction is not in valid form: " + error, ColoredPrint.RED, Server.serverOutputFile);
        return false;
      }

      return true;
  }

  // Returns true if the accountID is in the system
  // Returns false otherwise
  private boolean isAccountInSystem(String accountID)
  {

    for(String accountKey : Server.accounts.keySet())
    {
      if(accountID.equals(accountKey))
      {
        return true;
      }
    }

    return false;
  }

}
