// This is so the code is seperated from everything else.
// It's called src because it's inside of a folder called src.

/**
 * This is a tester program. (Edited by Angel)
 * 
 * If you want to compile and run this tester, open the src folder in the terminal.
 * 
 * You compile this with this command:
 * javac -d compiled Program.java
 * 
 * run with this command:
 * java -cp compiled Program
 */
 
public class Program {
  public static void main(String[] args) {
    // Put tester code here!
    CPU cpu = new CPU();
    Bus bus = new Bus();

    System.out.println("This is a tester!");
  }
}
