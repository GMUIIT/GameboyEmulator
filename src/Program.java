/**
 * This is the main program code. (Edited by Angel) Test by Alex
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

import java.io.*;

public class Program {  
  private static CPU cpu;
  private static MemoryMap memMap;

  public static void emulatorStart() {
    cpu = new CPU();
    cpu.regSet = new RegisterSet();
    memMap = new MemoryMap();
  }

  //#region ---- ---- ---- ---- ---- Emulator Functions

  /**
   * 
   */
  public static byte[] loadCartridge() {
    String fileName = "Tetris (Japan) (En).gb";
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
   * 
   */
  public static void renderScreen() {
    // Renders the screen?
    System.out.println("The screen is rendering!");
  }

  /**
   * (edited by Angel)
   * Code from http://www.codeslinger.co.uk/pages/projects/gameboy/opcodes.html
   */
  public static void emulatorUpdate() {
    final int MAXCYCLES = 69905;
    int cycles_count = 0;

    while (cycles_count < MAXCYCLES) {
      int cycles = cpu.executeNextOpcode();
      cycles_count += cycles;
      //UpdateTimers(cycles);
      //UpdateGraphics(cycles);
      //DoInterupts();
    }

    //renderScreen();
  }

  //#endregion

  /**
   * 
   */
  public static void tester() {
    // Put tester code here!
    // System.out.println(memMap.cartridgeMemory);
    // memMap.cartridgeMemory = loadCartridge();
    // System.out.println(memMap.cartridgeMemory);

    // System.out.println("This is a tester!");
  }

  /**
   * Main function.
   * Makes the java program actually run. (edited by Angel)
   * @param args : the arguments in the command line.
   * We might make that the file name of the ROM it runs for all I know... (edited by Angel)
   */
  public static void main(String[] args) {
    emulatorStart();
    tester();

    // while(true) {
    //   emulatorUpdate();
    // }
  }
}
