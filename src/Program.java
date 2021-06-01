import java.io.*;
import java.util.concurrent.*;

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
public class Program {  
  private static CPU cpu;
  private static TimeUnit time = TimeUnit.MILLISECONDS;

  //#region ---- ---- ---- ---- ---- Emulator Functions

  /**
   * Reads a rom file and returns a series of bytes
   * @param fileName
   * @return
   */
  public static byte[] loadCartridge(String fileName) {
    byte[] cartridgeMemory = new byte[0x8000];

    try {
      FileInputStream inputStream = new FileInputStream(fileName);

      int total = 0, nRead = 0;

      while((nRead = inputStream.read(cartridgeMemory)) != -1) { total += nRead; }
      inputStream.close();        

      System.out.println("Read " + total + " bytes");
    }
    catch(FileNotFoundException ex) { System.out.println("Unable to open file '" + fileName + "'"); }
    catch(IOException ex) { System.out.println("Error reading file '" + fileName + "'"); }

    return cartridgeMemory;
  }

  /**
   * Override Method for loadCartridge that by default runs the Tetris Rom
   * @return
   */
  public static byte[] loadCartridge() {
    return loadCartridge("Tetris (Japan) (En).gb");
  }

  /**
   * Renders the screen. Presumably this is where the JFrame stuff should be
   * here to display the screen graphics somehow.
   */
  public static void renderScreen() {
    System.out.println("The screen is rendering!");
  }

  /**
   * Causes the program to pause for the specified amount of time in milliseconds.
   * This is so there's no need to try/catch everytime the function is needed.
   * @param debugTime
   */
  public static void delay(int debugTime) {
    try { time.sleep(debugTime); } catch (Exception e) { }
  }

  // Global variable used by startDebugAt that controls when opcode / reg data is printed
  // to the command line.
  private static boolean isDebug;

  /**
   * Starts printing the current opcode and the flags to the cmdln when the program is at an addrress.
   * @param address
   * @param delaystep
   */
  public static void startDebugAt(short address, int delaystep) {
    if (cpu.regSet.getPC() == address) { isDebug = true; }
    if (isDebug) {
      System.out.println("OP Code: " + cpu.current_opcode);
      System.out.println(cpu.regSet + cpu.regSet.getFlagsShort());

      delay(delaystep);
    }
  }

  /**
   * Stops the execution of the program when the program counter at an address.
   * @param address
   */
  public static void breakpointAtAddress(short address) {
    if (cpu.regSet.getPC() == address) {
      System.out.println("\nBREAKPOINT at address: " + String.format("0x%04x", address));
      System.out.println("OP Code: " + cpu.current_opcode);
      System.out.println(cpu.regSet + cpu.regSet.getFlagsShort());

      try {
        System.out.print("Press Enter to step through: ");
        System.in.read();
        System.out.println("Pressed enter!\n");
      } catch (Exception e) { }
    }
  }

  /**
   * Update step of the emulator. Gets called every frame for the emulator
   * Code from http://www.codeslinger.co.uk/pages/projects/gameboy/opcodes.html
   */
  public static void emulatorUpdate() {
    final int MAXCYCLES = 69905;  // The amount of cycles that get executed every 60 Hz. (4194304/60)
    int cycles_count = 0;

    while (cycles_count < MAXCYCLES) {
      int cycles = cpu.executeNextOpcode();
      cycles_count += cycles;
      //UpdateTimers(cycles);
      //UpdateGraphics(cycles);
      //DoInterupts();

      // Debugging:
      // startDebugAt((short)0x02b4, 150L);
      // breakpointAtAddress((short)0x02b4);
    }

    //renderScreen();
  }

  //#endregion

  /**
   * Main function.
   * Makes the java program actually run. (edited by Angel)
   * @param args : the arguments in the command line.
   * We might make that the file name of the ROM it runs for all I know... (edited by Angel)
   */
  public static void main(String[] args) {
    cpu = new CPU();

    // Main loop for the emulator
    while(true) {
      emulatorUpdate();
      delay(16);  // This is so it tries to update the cpu at 60 hz. Might be better to compare times instead.
      System.out.println(".");
    }
  }
}
