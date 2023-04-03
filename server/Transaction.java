package server;

import java.lang.Thread;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.*;
import java.sql.Timestamp;

import server.Account;
import server.AccountManager;
import server.TransactionManager;
import server.Server;
import server.ServerReciever;
import message.Message;
import message.MessageTypes;

public class Transaction extends Thread {

  Account accountWithdraw;
  Account accountDeposit;
  int amountToMove;
  ServerReciever serverReciever;
  String transactionID;

  long timeStart;
  long timeEnd = 0;

  public Transaction(Account accountWithdraw, Account accountDeposit,
                     int amountToMove, ServerReciever serverReciever,
                     String transactionID) {
    this.accountWithdraw  = accountWithdraw;
    this.accountDeposit   = accountDeposit;
    this.amountToMove     = amountToMove;
    this.serverReciever   = serverReciever;
    this.transactionID    = transactionID;
    this.timeStart        = new Timestamp(System.currentTimeMillis())
                                          .getTime();
  }

  public long getTimeStart() {
    return timeStart;
  }

  public long getTimeEnd() {
    return timeEnd;
  }

  public String getWithdrawAccountID() {
    return accountWithdraw.getID();
  }

  public String getDepositAccountID() {
    return accountDeposit.getID();
  }

  public void run() {
    beginTransaction();
  }

  public void beginTransaction() {
    // Get balances
    int accountWithdrawBalance = accountWithdraw.getBalance();

    int accountDepositBalance = accountDeposit.getBalance();

    // Move 'money' between accounts
    accountWithdrawBalance -= amountToMove;
    accountDepositBalance  += amountToMove;

    // Attempt to commit transaction
    commitTransaction(accountWithdrawBalance, accountDepositBalance);
  }

  public void commitTransaction(int accountWithdrawBalance,
                                int accountDepositBalance) {

    if(TransactionManager.isConflict(this)) {
      abortTransaction();
    }
    else {
      accountWithdraw.setBalance(accountWithdrawBalance);
      accountDeposit.setBalance(accountDepositBalance);
      timeEnd = new Timestamp(System.currentTimeMillis()).getTime();
      System.out.println("Transaction Started " + timeStart + ", Ended " + timeEnd);
      AccountManager.printAccounts(Server.accounts);

      serverReciever.sendMessageToClient(new Message(MessageTypes.TRANSACTION_COMMIT, ""));

      serverReciever.closeSocket();
    }
  }

  public void abortTransaction() {
    System.out.println("Aborting transction");
    Message message = new Message(MessageTypes.TRANSACTION_FAIL, "");
    serverReciever.sendMessageToClient(message);
  }

}
