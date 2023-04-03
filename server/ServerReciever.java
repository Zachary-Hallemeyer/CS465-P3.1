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


/**
 * Class [ServerThread] accepts data some client associated with the instance of
 * this class and sends the data to all current clients
 *
 * @author Zachary M. Hallemeyer
 */
public class ServerReciever extends Thread {

  Socket socket;

  public ServerReciever(Socket socket) {
    this.socket = socket;
  }

  // This function accepts data from client and sends the data to all clients
  public void run() {
    System.out.println("Server reciever started");
    try {
      // Create a new input stream for client
      ObjectInputStream inputStream = new ObjectInputStream(
                                            socket.getInputStream());

      // Get message from input stream
      try {

        Message newMessage = (Message) inputStream.readObject();
        // Parse and process message
        parseMessage(newMessage);

      }
      catch (Exception error){
        System.out.println("Data from client could not be converted to message object: " + error);
      }

    }
    catch(Exception error) {
      System.out.println("Could not process message from peer: " + error);
    }
  }

  private void parseMessage(Message message) {
    System.out.println("Parsing Message");
    // Check if client is requesting account list
    if(message.getType() == MessageTypes.ACCOUNTS_REQUEST) {
      Message accountListMessage = new Message(MessageTypes.ACCOUNTS_REQUEST,
                                               Server.accounts.keySet()
                                               .toArray(String[]::new));
      sendMessageToClient(accountListMessage);
    }

    // Check if client is requesting a transaction
    if(message.getType() == Message.TRANSACTION
        && isValidTransactionMessage(message)) {
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
  }

  public void sendMessageToClient(Message message) {
    System.out.println("Sending message to client");
    try {

      ObjectOutputStream outputStream = new ObjectOutputStream(
                                              socket.getOutputStream());
      outputStream.writeObject(message);
      outputStream.flush();

    }
    catch (Exception error){
      // TODO
      System.out.println("Uh oh stinky: " + error);
    }
  }

  public void closeSocket() {
    System.out.println("Closing Socket");
    // Close socket and input stream
    try {
      socket.close();
    }
    catch (Exception error) {}
  }

  // Message should be in the following form: <string>,<string>,<integer>
  private boolean isValidTransactionMessage(Message message) {
      String[] messageArray = message.getContent().toString().split(",");
      int desiredStringCount = 3;
      int withdrawKeyIndex = 0;
      int depositKeyIndex = 1;
      int amountToMoveIndex = 2;
      int amountToMove = 0;

      // Check if correct string count
      if(messageArray.length != desiredStringCount) {
        return false;
      }

      // Check if withdraw and deposit accounts exist in system


      // Check if 'amountToMove' is a number
      try {
        amountToMove = Integer.parseInt(messageArray[amountToMoveIndex]);
      }
      // 'amountToMove' is not a number
      catch (Exception error){
        return false;
      }

      return true;
  }

  private boolean isAccountInSystem(String accountID) {

    for(String accountKey : Server.accounts.keySet()) {
      if(accountID.equals(accountKey)) {
        return true;
      }
    }

    return false;
  }

}
