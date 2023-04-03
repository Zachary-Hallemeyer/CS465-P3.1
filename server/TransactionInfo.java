package server;

import java.sql.Timestamp;

public class TransactionInfo {
  public String withdrawAccountID;
  public String depositAccountID;


  public TransactionInfo(String withdrawAccountID, String depositAccountID) {
    this.withdrawAccountID = withdrawAccountID;
    this.depositAccountID  = depositAccountID;
    this.timeStart         = new Timestamp(System.currentTimeMillis()).getTime();
  }

  public void setTimeEnd() {
    this.timeEnd = new Timestamp(System.currentTimeMillis()).getTime();
  }
}
