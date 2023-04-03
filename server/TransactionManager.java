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

public class TransactionManager{

  // public static HashMap<String, Transaction> transactionSet = new HashMap<String, Transaction>();
  public static List<Transaction> transactionList = new ArrayList<Transaction>();

  public static int transactionID = 0;

  public static boolean validationCheckInUse = false;

  public static void initializeWriteTransaction(Account accountWithdraw,
                                           Account accountDeposit,
                                           int amountToMove,
                                           ServerReciever serverReciever) {

    System.out.println("Init transaction");
    Transaction transaction = new Transaction(accountWithdraw, accountDeposit, amountToMove,
                    serverReciever, String.valueOf(transactionID));
    transaction.start();

    transactionID++;

    // Add Transaction to transaction set
    transactionList.add(transaction);
  }

  public static boolean isConflict(Transaction transaction) {
    while(validationCheckInUse) ;
    validationCheckInUse = true;

    boolean isConflict = false;

    for(int index = 0; index < transactionList.size(); index++) {
      Transaction currentTransaction = transactionList.get(index);

      // Check if current transaction ended after this transaction started

      if (currentTransaction.getTimeEnd() > transaction.getTimeStart()){

        // Check if current transaction used the same accounts as this transaction
        if(checkOverlap(currentTransaction, transaction)) {
             isConflict = true;
           }
      }
    }

    validationCheckInUse = false;
    // return false;
    return isConflict;
  }

  private static boolean checkOverlap(Transaction transactionOne, Transaction transactionTwo)
  {
    boolean tester =     transactionOne.getWithdrawAccountID().equals(transactionTwo.getWithdrawAccountID()) ||
                         transactionOne.getDepositAccountID().equals(transactionTwo.getWithdrawAccountID()) ||
                         transactionOne.getWithdrawAccountID().equals(transactionTwo.getDepositAccountID()) ||
                         transactionOne.getDepositAccountID().equals(transactionTwo.getDepositAccountID());

    System.out.println(transactionOne.getWithdrawAccountID() + " " + transactionOne.getDepositAccountID()
                       + "|" + transactionTwo.getWithdrawAccountID() + " " + transactionTwo.getDepositAccountID()
                       + " = " + tester);

    return
    transactionOne.getWithdrawAccountID().equals(transactionTwo.getWithdrawAccountID()) ||
    transactionOne.getDepositAccountID().equals(transactionTwo.getWithdrawAccountID()) ||
    transactionOne.getWithdrawAccountID().equals(transactionTwo.getDepositAccountID()) ||
    transactionOne.getDepositAccountID().equals(transactionTwo.getDepositAccountID());
  }

}
