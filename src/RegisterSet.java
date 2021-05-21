import java.util.HashMap;

enum Reg_16 {
  AF, BC, DE, HL, SP, PC;
};

enum Reg_8 {
  A, F, B, C, D, E, H, L;

  public int value;
  Reg_8() { this.value = this.ordinal(); }
};

/**
 * Register Set Class.
 * 
 * This stores all the registers for the CPU and stuff.
 * Registers can be accessed either by Byte or by Word.
 */
public class RegisterSet {
  // Stores all the register values
  private HashMap<Reg_16, Integer> registerMap = new HashMap<Reg_16, Integer>();

  // This is so we can get the register types via index values
  public Reg_16[] register16List = Reg_16.values();
  public Reg_8[] register8List = Reg_8.values();

  /**
   * Constructor method. Sets all the 16-bit Reg_16 to initialized values.
   */
  public RegisterSet() {
    registerMap.put(Reg_16.AF, 0x01B0);
    registerMap.put(Reg_16.BC, 0x0013);
    registerMap.put(Reg_16.DE, 0x00D8);
    registerMap.put(Reg_16.HL, 0x014D);
    registerMap.put(Reg_16.SP, 0xFFFE);
    registerMap.put(Reg_16.PC, 0x100);
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
    registerMap.put(Reg_16.AF, AF);
    registerMap.put(Reg_16.BC, BC);
    registerMap.put(Reg_16.DE, DE);
    registerMap.put(Reg_16.HL, HL);
    registerMap.put(Reg_16.SP, SP);
    registerMap.put(Reg_16.PC, PC);
  }

  //#region ---- ---- ---- ---- ---- Public Methods

  /**
   * Gets the 16-bit word of a register
   * @param register 16-bit register to get
   * @return value of the register
   */
  public int getWord(Reg_16 register) {
    return registerMap.get(register);
  }

  /**
   * Gets the 8-bit byte of a register
   * @param register 8-bit register to get
   * @return value of the register
   */
  public int getByte(Reg_8 register) {
    Reg_16 reg = register16List[register.value >> 1];
    int val = registerMap.get(reg);

    if (register.value % 2 > 0)
      return val & 0x00FF;
    else
      return (val & 0xFF00) >> 8;
  }

  /**
   * Sets the 16-bit word of a register
   * @param register register to set
   * @param value value to set
   */
  public void setWord(Reg_16 register, int value) {
    registerMap.put(register, value);
  }

  /**
   * Sets the 8-bit byte of a register
   * @param register register to set
   * @param value value to set
   */
  public void setByte(Reg_8 register, int value) {
    Reg_16 reg = register16List[register.value >> 1];
    int val = registerMap.get(reg);

    if (register.value % 2 > 0)
      val = (val & 0xFF00) | (value & 0x00FF);
    else
      val = ((value & 0x00FF) << 8) | (val & 0x00FF);

    registerMap.put(reg, val);
  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Wrapper Methods

  /**
   * Sets the Program Counter
   * @param value
   */
  public void setPC(int value) { setWord(Reg_16.PC, value); }
  /**
   * Gets the Program Counter
   * @return
   */
  public int getPC() { return getWord(Reg_16.PC); }

  /**
   * Sets the Program Counter
   * @param value
   */
  public void setSP(int value) { setWord(Reg_16.SP, value); }
  /**
   * Gets the Program Counter
   * @return
   */
  public int getSP() { return getWord(Reg_16.SP); }

  /**
   * Sets the A register
   * @param value
   */
  public void setA(int value) { setByte(Reg_8.A, value); }
  /**
   * Gets the A register
   * @return
   */
  public int getA() { return getByte(Reg_8.A); }

  //#endregion

  //#region ---- ---- ---- ---- ---- Flag Methods

  // Methods for the zero flag in the F register
  public boolean getZeroFlag() { return (getByte(Reg_8.F) & 0x80) > 0; }
  public void setZeroFlag() { setByte(Reg_8.F, getByte(Reg_8.F) | 0x80); }
  public void clearZeroFlag() { setByte(Reg_8.F, getByte(Reg_8.F) & ~0x80); }

  // Methods for the Negative flag in the F register
  public boolean getNegativeFlag() { return (getByte(Reg_8.F) & 0x40) > 0; }
  public void setNegativeFlag() { setByte(Reg_8.F, getByte(Reg_8.F) | 0x40); }
  public void clearNegativeFlag() { setByte(Reg_8.F, getByte(Reg_8.F) & ~0x40); }

  // Methods for the Half-Carry flag in the F register
  public boolean getHalfCarryFlag() { return (getByte(Reg_8.F) & 0x20) > 0; }
  public void setHalfCarryFlag() { setByte(Reg_8.F, getByte(Reg_8.F) | 0x20); }
  public void clearHalfCarryFlag() { setByte(Reg_8.F, getByte(Reg_8.F) & ~0x20); }

  // Methods for the Carry flag in the F register
  public boolean getCarryFlag() { return (getByte(Reg_8.F) & 0x10) > 0; }
  public void setCarryFlag() { setByte(Reg_8.F, getByte(Reg_8.F) | 0x10); }
  public void clearCarryFlag() { setByte(Reg_8.F, getByte(Reg_8.F) & ~0x10); }

  //#endregion

  /**
   * Returns a string representation of the flags stored in this register set
   * @return String of flags
   */
  public String getFlags() {
    String val =
    String.format(
      "Zero:        %d\n" +
      "Negative:    %d\n" +
      "Half Carry:  %d\n" +
      "Carry:       %d\n",
      getZeroFlag()       ? 1 : 0,
      getNegativeFlag()   ? 1 : 0,
      getHalfCarryFlag()  ? 1 : 0,
      getCarryFlag()      ? 1 : 0
    );
    return val;
  }

  @Override
  public String toString() {
    String val
    = String.format("AF: 0x%04x\t A: 0x%02x\t F: 0x%02x\n", getWord(Reg_16.AF), getByte(Reg_8.A), getByte(Reg_8.F))
    + String.format("BC: 0x%04x\t B: 0x%02x\t C: 0x%02x\n", getWord(Reg_16.BC), getByte(Reg_8.B), getByte(Reg_8.C))
    + String.format("DE: 0x%04x\t D: 0x%02x\t E: 0x%02x\n", getWord(Reg_16.DE), getByte(Reg_8.D), getByte(Reg_8.E))
    + String.format("HL: 0x%04x\t H: 0x%02x\t L: 0x%02x\n", getWord(Reg_16.HL), getByte(Reg_8.H), getByte(Reg_8.L))
    + String.format("\nSP: 0x%04x\nPC: 0x%04x\n", getWord(Reg_16.SP), getWord(Reg_16.PC));
    return val;
  }

  /**
   * Test to make sure the registerSet class works properly.
   * You can run this test by running this command:
   * 
   * javac -d compiled RegisterSet.java
   * java -cp compiled RegisterSet
   * 
   */
  public static void main(String[] args) {
    RegisterSet regSet = new RegisterSet();

    System.out.printf("16-bit Register Tests\n");

    regSet.setWord(Reg_16.BC, 0x1384);
    regSet.setWord(Reg_16.DE, 0x4852);
    regSet.setWord(Reg_16.HL, 0x2672);
    regSet.setWord(Reg_16.SP, 0x9836);
    regSet.setWord(Reg_16.PC, 0x0001);

    System.out.printf("BC: %02x B:%02x C:%02x\n", regSet.getWord(Reg_16.BC), regSet.getByte(Reg_8.B), regSet.getByte(Reg_8.C));
    System.out.printf("DE: %02x D:%02x E:%02x\n", regSet.getWord(Reg_16.DE), regSet.getByte(Reg_8.D), regSet.getByte(Reg_8.E));
    System.out.printf("HL: %02x H:%02x L:%02x\n", regSet.getWord(Reg_16.HL), regSet.getByte(Reg_8.H), regSet.getByte(Reg_8.L));
    System.out.printf("SP: %02x PC: %02x\n", regSet.getWord(Reg_16.SP), regSet.getWord(Reg_16.PC));
    System.out.printf("8-bit Register Tests\n");

    regSet.setByte(Reg_8.B, 0x38);
    regSet.setByte(Reg_8.C, 0x85);
    regSet.setByte(Reg_8.D, 0x10);
    regSet.setByte(Reg_8.E, 0x08);
    regSet.setByte(Reg_8.H, 0x12);
    regSet.setByte(Reg_8.L, 0x31);
    regSet.setByte(Reg_8.A, 0x72);

    System.out.printf("BC: %02x B:%02x C:%02x\n", regSet.getWord(Reg_16.BC), regSet.getByte(Reg_8.B), regSet.getByte(Reg_8.C));
    System.out.printf("DE: %02x D:%02x E:%02x\n", regSet.getWord(Reg_16.DE), regSet.getByte(Reg_8.D), regSet.getByte(Reg_8.E));
    System.out.printf("HL: %02x H:%02x L:%02x\n", regSet.getWord(Reg_16.HL), regSet.getByte(Reg_8.H), regSet.getByte(Reg_8.L));
    System.out.printf("AF: %02x A:%02x F:%02x\n", regSet.getWord(Reg_16.AF), regSet.getByte(Reg_8.A), regSet.getByte(Reg_8.F));
  }
}