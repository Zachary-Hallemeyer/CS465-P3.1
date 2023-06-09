package server;

/**
 * Class [Account] defines an Account object that contains an id and a balance
 *
 * @author Zachary M. Hallemeyer
 */
public class Account {
  private String ID;
  private int balance;

  public Account(String ID, int balance)
  {
    this.ID = ID;
    this.balance = balance;
  }

  public String getID()
  {
    return ID;
  }

  public int getBalance()
  {
    return balance;
  }

  public void setBalance(int balance)
  {
    this.balance = balance;
  }
}
