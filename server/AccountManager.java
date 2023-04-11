package server;

import java.util.*;
import server.Server;
import server.Account;
import java.util.Map;

import utils.ColoredPrint;

/*
 * Class [AccountManager] provides overhead for accounts. It is able to create
 * new accounts and print existing accounts
 *
 * @author Zachary M. Hallemeyer
 */
public class AccountManager
{
  public static boolean printAccountInUse = false;

  // Create the number of accounts with the starting balance provided
  // by the parameters. Then the list of accounts will be returned
  public static HashMap<String, Account> createAccounts(int numOfAccounts,
                                                        int startingBalance)
  {
    HashMap<String, Account> accounts = new HashMap<String, Account>();
    Account newAccount;

    for(int index = 0; index < numOfAccounts; index++)
    {
      newAccount = new Account(Integer.toString(index), startingBalance);
      accounts.put(Integer.toString(index), newAccount);
    }

    return accounts;
  }

  // Print accounts in their current state provided by parameter
  // Prints account and sum if boolean showAccounts is true
  // Prints sum if boolean showAccounts is false
  public static void printAccounts(HashMap<String, Account> accounts,
                                   boolean showAccounts)
  {
    int sum = 0;

    synchronized(accounts)
    {
      Collection<Account> accountValues = accounts.values();
      ArrayList<Account> accountList = new ArrayList<Account>(accountValues);

      for(int index = 0; index < accountList.size(); index++)
      {
        sum += accountList.get(index).getBalance();
        if(showAccounts)
        {
          ColoredPrint.print("ID: " + accountList.get(index).getID() + ", Account Balance: "
                                + accountList.get(index).getBalance(),
                              ColoredPrint.PURPLE, Server.serverOutputFile);
        }
      }

      ColoredPrint.print("The Total sum of balances is: $" + sum, ColoredPrint.PURPLE, Server.serverOutputFile);
    }
  }
}
