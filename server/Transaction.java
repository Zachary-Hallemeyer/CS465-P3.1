package server;

import java.lang.Thread;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.*;

import server.Account;
import server.AccountManager;
import server.TransactionManager;
import server.Server;
import server.ServerReciever;
import message.Message;
import message.MessageTypes;
import utils.ColoredPrint;

/**
 * Class [Transaction] contains the ability to process transactions
 * It is called by TransactionManager as a thread. The transaction is a
 * movement of money between one account to another
 * Once the transaction is finished processing, it will use TransactionManager
 * to validate its transaction.
 * If the transaction is validated, the transaction will be commited
 * Otherwise, the transaction will be aborted and an abort message will
 * be sent to client
 *
 * @author Zachary M. Hallemeyer
 */
public class Transaction extends Thread
{

  Account accountWithdraw;
  Account accountDeposit;
  int amountToMove;
  ServerReciever serverReciever;
  String transactionID;

  int startID;
  int endID = 0;

  public Transaction(Account accountWithdraw, Account accountDeposit,
                     int amountToMove, ServerReciever serverReciever,
                     int startID)
   {
    this.accountWithdraw  = accountWithdraw;
    this.accountDeposit   = accountDeposit;
    this.amountToMove     = amountToMove;
    this.serverReciever   = serverReciever;
    this.transactionID    = transactionID;
    this.startID        = startID;
  }

  public int getStartID()
  {
    return startID;
  }

  public int getEndID()
  {
    return endID;
  }

  public void setEndID(int endID)
  {
    this.endID = endID;
  }

  public String getWithdrawAccountID()
  {
    return accountWithdraw.getID();
  }

  public String getDepositAccountID()
  {
    return accountDeposit.getID();
  }

  // Calls beginTransaction
  public void run()
  {
    beginTransaction();
  }

  // Preforms the transactions with temporary variables and
  // calls commitTransaction to validate and commit the transaction
  public void beginTransaction()
  {
    // Get balances
    int accountWithdrawBalance = accountWithdraw.getBalance();
    ColoredPrint.print("Transaction " + startID + ": Reading withdraw Account "
                       + accountWithdraw.getID() + " with the balance: " +
                       accountWithdrawBalance, ColoredPrint.CYAN,
                       Server.serverOutputFile);
    int accountDepositBalance = accountDeposit.getBalance();
    ColoredPrint.print("Transaction " + startID + ": Reading deposit Account "
                       + accountDeposit.getID() + " with the balance: " +
                       accountDepositBalance, ColoredPrint.CYAN,
                       Server.serverOutputFile);

    // Move 'money' between accounts
    accountWithdrawBalance -= amountToMove;
    accountDepositBalance  += amountToMove;

    // Attempt to commit transaction
    commitTransaction(accountWithdrawBalance, accountDepositBalance);
  }

  // This functions checks if there are any conflicts with other transactions
  // with the use of TransactionManager.isConflict.
  // If there are no conflicts, the transaction is commited
  // Otherwise, the transaction is aborted and an abort message is sent to client
  synchronized
  public void commitTransaction(int accountWithdrawBalance,
                                int accountDepositBalance)
  {

    // Check is conflict
    if(TransactionManager.isConflict(this))
    {
      // Abort transaction
      abortTransaction();
    }
    // commit transaction
    else
    {
      // Commit values
      accountWithdraw.setBalance(accountWithdrawBalance);
      accountDeposit.setBalance(accountDepositBalance);
      ColoredPrint.print("Transaction " + startID + ": writing withdraw Account "
                         + accountWithdraw.getID() + " with the new balance: " +
                         accountWithdrawBalance, ColoredPrint.CYAN,
                         Server.serverOutputFile);
      ColoredPrint.print("Transaction " + startID + ": writing deposit Account "
                         + accountDeposit.getID() + " with the new balance: " +
                         accountDepositBalance, ColoredPrint.CYAN,
                         Server.serverOutputFile);
      // Print account values and commit message
      ColoredPrint.print("Transaction Started " + startID + ", Ended " + endID, ColoredPrint.GREEN, Server.serverOutputFile);
      TransactionManager.printAccounts(startID);
      // Send commit message to client
      serverReciever.sendMessageToClient(new Message(MessageTypes.TRANSACTION_COMMIT, startID));

      // Close ServerReciever thread socket
      serverReciever.closeSocket();
    }
  }

  // Aborts transaction, and sends abort message to client
  public void abortTransaction()
  {
    ColoredPrint.print("Aborting Transaction " + startID, ColoredPrint.RED, Server.serverOutputFile);
    Message message = new Message(MessageTypes.TRANSACTION_FAIL, startID);
    serverReciever.sendMessageToClient(message);
    // Close ServerReciever thread socket
    serverReciever.closeSocket();
  }

}
