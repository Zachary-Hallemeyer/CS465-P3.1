package server;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.*;
import java.sql.Timestamp;

import server.Account;
import server.Transaction;
import server.ServerReciever;
import utils.ColoredPrint;


/**
 * Class [TransactionManager] provides overhead for all transactions on server
 * It will initialize transactions and check for conflicts between transactions
 *
 * @author Zachary M. Hallemeyer
 */
public class TransactionManager
{

  public static List<Transaction> transactionList = new ArrayList<Transaction>();

  private static int numOfActiveTransactions = 0;
  private static boolean canPrint = true;
  public static int transactionCounter = 0;

  // Initialize transactions and creates a thread for the new transaction with
  // the class Transaction
  synchronized
  public static void initializeWriteTransaction(Account accountWithdraw,
                                   Account accountDeposit,
                                   int amountToMove,
                                   ServerReciever serverReciever)
  {
    // Create new transaction
    ColoredPrint.print("Transaction " + transactionCounter + " initialized", ColoredPrint.GREEN, Server.serverOutputFile);
    Transaction transaction = new Transaction(accountWithdraw, accountDeposit,
                            amountToMove, serverReciever, transactionCounter);
    // Start new transaction thread
    transaction.start();
    transactionCounter++;
    numOfActiveTransactions++;

    // Add Transaction to transaction set
    transactionList.add(transaction);
  }

  public static boolean isConflict(Transaction transaction)
  {
    boolean isConflict = false;

    // Enter synchronized section
    synchronized (transactionList)
    {
      // Loop through transactions and check for conflicts
      for(int index = 0; index < transactionList.size(); index++)
      {
        Transaction currentTransaction = transactionList.get(index);

        // Check if current transaction ended after this transaction started
        if (currentTransaction.getEndID() > transaction.getStartID())
        {
          // Check if current transaction used the same accounts as this transaction
          if(checkOverlap(currentTransaction, transaction))
          {
            ColoredPrint.print("Conflict found between transaction " + transaction.getStartID() + " and " + currentTransaction.getStartID(), ColoredPrint.RED, Server.serverOutputFile);
            isConflict = true;
          }
        }
      }

      // Check if there are no coflicts
      if(!isConflict)
      {
        // Set end transaction count
        transaction.setEndID(transactionCounter);
        transactionCounter++;
      }

      numOfActiveTransactions--;
      return isConflict;
    }
  }

  // Return true if there is an overlap in the two provided transactions
  // Return false if there is no overlap
  private static boolean checkOverlap(Transaction transactionOne, Transaction transactionTwo)
  {
    return
      transactionOne.getWithdrawAccountID()
      .equals(transactionTwo.getWithdrawAccountID()) ||
      transactionOne.getDepositAccountID()
      .equals(transactionTwo.getWithdrawAccountID()) ||
      transactionOne.getWithdrawAccountID()
      .equals(transactionTwo.getDepositAccountID()) ||
      transactionOne.getDepositAccountID()
      .equals(transactionTwo.getDepositAccountID());
  }

  public static void printAccounts(int transactionCount)
  {
    // If there are no other active transactions, print accounts
    if(numOfActiveTransactions <= 0)
    {
      ColoredPrint.print("After Transaction " + transactionCount,
                          ColoredPrint.PURPLE, Server.serverOutputFile);
      AccountManager.printAccounts(Server.accounts, false);
    }
  }
}
