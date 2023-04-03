package server;

import java.util.*;
import server.Account;
import java.util.Map;

public class AccountManager {

  public static HashMap<String, Account> createAccounts(int numOfAccounts,
                                                        int startingBalance) {
    HashMap<String, Account> accounts = new HashMap<String, Account>();
    Account newAccount;

    for(int index = 0; index < numOfAccounts; index++) {
      newAccount = new Account(Integer.toString(index), startingBalance);
      accounts.put(Integer.toString(index), newAccount);
    }

    return accounts;
  }

  public static void printAccounts(HashMap<String, Account> accounts) {
    int sum = 0;
    for(Account account : accounts.values()) {
      sum += account.getBalance();
      System.out.println("ID: " + account.getID() + ", Account Balance: "
                                                + account.getBalance());

    }
    System.out.println("Total: " + sum);
  }

}
