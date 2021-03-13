// This is so the code is seperated from everything else.
// It's called src because it's inside of a folder called src.

/**
 * This is a tester program. (Edited by Angel)
 * You compile this with this command:
 * javac -d compiled program.java
 * 
 * run with this command:
 * java -cp compiled program
 */
 
public class program {
  public static void main(String[] args) {
    // Put tester code here!
    CPU cpu = new CPU();
    Bus bus = new Bus();

    System.out.println("This is a tester!");
  }
}
