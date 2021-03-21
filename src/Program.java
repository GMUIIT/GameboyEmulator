/**
 * This is the main program code. (Edited by Angel)
 * 
 * If you want to compile and run this, open the src folder in the terminal.
 * 
 * You compile this with this command:
 * javac -d compiled Program.java
 * 
 * run with this command:
 * java -cp compiled Program
 */
 
// Since java doesn't have type defs, assume the following:
// unsigned Byte is a short
// signed Byte is a byte
// unsigned word is an int
// signed Word is a short

public class Program {

  private short[] cartridgeMemory = new short[0x200000];

  /**
   * 
   */
  public void loadCartridge() {
    // loads the catridge if we want to I guess
    System.out.println("Cartridge is loaded!");
  }

  /**
   * 
   */
  public void renderScreen() {
    // Renders the screen?
    System.out.println("The screen is rendering!");
  }

  /**
   * (edited by Angel)
   * Code from http://www.codeslinger.co.uk/pages/projects/gameboy/opcodes.html
   */
  public void emulatorUpdate() {
    final int MAXCYCLES = 69905;
    int thisCycles = 0;

    while (thisCycles < MAXCYCLES) {
      //int cycles = ExecuteNextOpcode();
      //cyclesThisUpdate+=cycles;
      //UpdateTimers(cycles);
      //UpdateGraphics(cycles);
      //DoInterupts();
    }

    //renderScreen();
  }

  /**
   * 
   */
  public static void tester() {
    // Put tester code here!
    CPU cpu = new CPU();
    Bus bus = new Bus();

    System.out.println("This is a tester!");
  }

  /**
   * Main function.
   * Makes the java program actually run. (edited by Angel)
   * @param args : the arguments in the command line.
   * We might make that the file name of the ROM it runs for all I know... (edited by Angel)
   */
  public static void main(String[] args) {
    tester();
  }
}
