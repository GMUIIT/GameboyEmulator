import java.util.HashMap;

/**
 * Enum for each register type
 */
enum Registers {
  AF, BC, DE, HL, SP, PC;
};

public class RegisterSet {
  private final int UPPER_BYTE = 0xFF00;
  private final int LOWER_BYTE = 0x00FF;

  private HashMap<Registers, Integer> registerMap = new HashMap<Registers, Integer>();
  
  /**
   * Constructor method. Sets all the 16-bit registers to 0.
   */
  public RegisterSet() {
    registerMap.put(Registers.AF, 0);
    registerMap.put(Registers.BC, 0);
    registerMap.put(Registers.DE, 0);
    registerMap.put(Registers.HL, 0);
    registerMap.put(Registers.SP, 0);
    registerMap.put(Registers.PC, 0);
  }
  
  /**
   * Override method to set all the values directly.
   * @param AF  16-bit AF register
   * @param BC  16-bit BC register
   * @param DE  16-bit DE register
   * @param HL  16-bit HL register
   * @param SP  16-bit Stack Pointer Register
   * @param PC  16-bit Program Counter Register
   */
  public RegisterSet(int AF, int BC, int DE, int HL, int SP, int PC) {
    registerMap.put(Registers.AF, AF);
    registerMap.put(Registers.BC, BC);
    registerMap.put(Registers.DE, DE);
    registerMap.put(Registers.HL, HL);
    registerMap.put(Registers.SP, SP);
    registerMap.put(Registers.PC, PC);
  }

  /**
   * Gets the 16-bit word of a register
   * @param register register to get
   * @return value of the register
   */
  public int getByte(Registers register) {
    return registerMap.get(register);
  }

  /**
   * Gets the 8-bit upper byte of a register
   * @param register register to get
   * @return value of the register
   */
  public int getUpperByte(Registers register) {
    return registerMap.get(register) >> 8;
  }

  /**
   * Gets the 8-bit lower byte of a register
   * @param register register to get
   * @return value of the register
   */
  public int getLowerByte(Registers register) {
    return registerMap.get(register) & LOWER_BYTE;
  }

  /**
   * Sets the 16-bit word of a register
   * @param register register to set
   * @param value value to set
   */
  public void setByte(Registers register, int value) {
    registerMap.put(register, value);
  }

  /**
   * Sets the 8-bit upper byte of a register
   * @param register register to set
   * @param value value to set
   */
  public void setUpperByte(Registers register, int value) {
    int val = (registerMap.get(register) & LOWER_BYTE) | ((value & LOWER_BYTE) << 8);
    registerMap.put(register, val);
  }

  /**
   * Sets the 8-bit lower byte of a register
   * @param register register to set
   * @param value value to set
   */
  public void setLowerByte(Registers register, int value) {
    int val = (registerMap.get(register) & UPPER_BYTE) | (value & LOWER_BYTE);
    registerMap.put(register, val);
  }

  /*
  public void exampleUsage() {
    RegisterSet regSet = new RegisterSet();
    regSet.setByte(Registers.AF, 15);

    regSet.setUpperByte(Registers.HL, 3);
    regSet.setLowerByte(Registers.AF, 3);

    int value = regSet.getUpperByte(Registers.AF);    // Gives you the value at A
    System.out.println(value);
  }
  */
}