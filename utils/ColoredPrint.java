package utils;

import java.util.*;
import java.util.*;
import server.Server;
import client.Client;
import java.io.FileOutputStream;

/**
 * Class [ColoredPrint] prints strings to consoles with desired colored text
 *
 * @author Zachary M. Hallemeyer
 */
public class ColoredPrint
{
  public static final int RED = 1;
  public static final int GREEN = 2;
  public static final int YELLOW = 3;
  public static final int BLUE = 4;
  public static final int PURPLE = 5;
  public static final int CYAN = 6;

  private static final String RESET = "\u001B[0m";
  private static final String RED_COLOR = "\u001B[31m";
  private static final String GREEN_COLOR = "\u001B[32m";
  private static final String YELLOW_COLOR = "\u001B[33m";
  private static final String BLUE_COLOR = "\u001B[34m";
  private static final String PURPLE_COLOR = "\u001B[35m";
  private static final String CYAN_COLOR = "\u001B[36m";
  private static final String WHITE_COLOR = "\u001B[37m";


  // Print the provided string to console in the provided color and writes string to file
  synchronized
  public static void print(String printString, int colorCode, String outputFile)
  {
    String printColor;
    // outputList.add(printString);
    // String printColor;

    // Get the desired color coding
    switch(colorCode)
    {
      case RED: printColor = RED_COLOR;
                    break;
      case GREEN: printColor = GREEN_COLOR;
                    break;
      case YELLOW: printColor = YELLOW_COLOR;
                    break;
      case BLUE: printColor = BLUE_COLOR;
                    break;
      case PURPLE: printColor = PURPLE_COLOR;
                    break;
      case CYAN: printColor = CYAN_COLOR;
                    break;
      default: printColor = WHITE_COLOR;
               break;
    }

    System.out.println( printColor + printString + RESET );

    // Write string to output file provided
    try
    {
      FileOutputStream outputStream = new FileOutputStream(outputFile, true);
      outputStream.write((printString+"\n").getBytes());
      outputStream.close();
    }
    catch (Exception error)
    {
      System.out.println("String could not write to file: " + error);
    }
  }
}
