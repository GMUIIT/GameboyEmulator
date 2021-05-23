import java.util.HashMap;

/**
 * CPU class.
 * Has all opcodes and stuff.
 * 
 * The #region and the #endregion are there for code folding basically since this is a very large
 * file and has many sections to it. This is also because I use an extention for it in VS Code. (Edited by Angel)
 */
public class CPU {

  /**
   * Constructor function for the class.
   * Not sure what to do with it... (Edited by Angel)
   */
  public CPU() {
    System.out.println("This is a new CPU!");
    initializeHashmaps();
  }

//#region ---- ---- ---- ---- ---- Registers ---- ---- ---- ---- ---- ---- ---- ----
  public RegisterSet regSet = new RegisterSet();

  // private RegisterSet save0 = new RegisterSet(0, 0, 0, 0, 0, 0);
  // private RegisterSet save1 = new RegisterSet(0, 0, 0, 0, 0, 0);
  // private RegisterSet save2 = new RegisterSet(0, 0, 0, 0, 0, 0);
  // private RegisterSet save3 = new RegisterSet(0, 0, 0, 0, 0, 0);

//#endregion

//#region ---- ---- ---- ---- ---- Dissassembly Decode Table ---- ---- ---- ---- ---- ----
  
  /**
   * Argument modifiers for Jump Instructions specifying certain conditions to jump.
   */
  enum CC_t {
    NZ, Z, NC, C;

    public int index;

    CC_t() { this.index = this.ordinal(); }
  }

  /**
   * ALU operation types for 8-bit ALU operations.
   */
  enum Alu_t {
    ADD, ADC, SUB, SBC, AND, XOR, OR, CP;

    public int index;

    Alu_t() { this.index = this.ordinal(); }
  }

  /**
   * Roll/shift register commands
   */
  enum Rot_t {
    RLC, RRC, RL, RR, SLA, SRA, SWAP, SRL;

    public int index;

    Rot_t() { this.index = this.ordinal(); }
  }

  /**
   * Assorted operations on accumulator/flags
   */
  enum Z7_t {
    RLCA, RRCA, RLA, RRA, DAA, CPL, SCF, CCF;

    public int index;

    Z7_t() { this.index = this.ordinal(); }
  }

  // Opcode Argument Types
  final Reg_8[] r_args = { Reg_8.B, Reg_8.C, Reg_8.D, Reg_8.E, Reg_8.H, Reg_8.L, null, Reg_8.A };
  final Reg_16[] rp_args = { Reg_16.BC, Reg_16.DE, Reg_16.HL, Reg_16.SP };
  final Reg_16[] rp2_args = { Reg_16.BC, Reg_16.DE, Reg_16.HL, Reg_16.AF };
  final CC_t[] cc_args = CC_t.values();
  final Alu_t[] alu_args = Alu_t.values();
  final Rot_t[] rot_args = Rot_t.values();
  final Z7_t[] z7_args = Z7_t.values();

  @FunctionalInterface
  interface func { public void invoke(); }

  @FunctionalInterface
  interface func_reg8 { public void invoke(Reg_8 source); }

  HashMap<Alu_t, func_reg8> AluMap = new HashMap<Alu_t, func_reg8>();
  HashMap<Alu_t, func> AluImMap = new HashMap<Alu_t, func>();
  HashMap<Z7_t, func> Z7Map = new HashMap<Z7_t, func>();
  HashMap<Rot_t, func_reg8> RotMap = new HashMap<Rot_t, func_reg8>();

  public void initializeHashmaps() {
    AluMap.put(Alu_t.ADC, (source) -> ADC(source));
    AluMap.put(Alu_t.ADD, (source) -> ADD(source));
    AluMap.put(Alu_t.AND, (source) -> AND(source));
    AluMap.put(Alu_t.CP,  (source) -> CP(source));
    AluMap.put(Alu_t.OR,  (source) -> OR(source));
    AluMap.put(Alu_t.SBC, (source) -> SBC(source));
    AluMap.put(Alu_t.SUB, (source) -> SUB(source));
    AluMap.put(Alu_t.XOR, (source) -> XOR(source));

    AluImMap.put(Alu_t.ADC, () -> ADC_IM());
    AluImMap.put(Alu_t.ADD, () -> ADD_IM());
    AluImMap.put(Alu_t.AND, () -> AND_IM());
    AluImMap.put(Alu_t.CP,  () -> CP_IM());
    AluImMap.put(Alu_t.OR,  () -> OR_IM());
    AluImMap.put(Alu_t.SBC, () -> SBC_IM());
    AluImMap.put(Alu_t.SUB, () -> SUB_IM());
    AluImMap.put(Alu_t.XOR, () -> XOR_IM());

    Z7Map.put(Z7_t.RLCA, () -> RLCA());
    Z7Map.put(Z7_t.RRCA, () -> RRCA());
    Z7Map.put(Z7_t.RLA,  () -> RLA());
    Z7Map.put(Z7_t.RRA,  () -> RRA());
    Z7Map.put(Z7_t.DAA,  () -> DAA());
    Z7Map.put(Z7_t.CPL,  () -> CPL());
    Z7Map.put(Z7_t.SCF,  () -> SCF());
    Z7Map.put(Z7_t.CCF,  () -> CCF());

    RotMap.put(Rot_t.RLC,  (source) -> RLC(source));
    RotMap.put(Rot_t.RRC,  (source) -> RRC(source));
    RotMap.put(Rot_t.RL,   (source) -> RL(source));
    RotMap.put(Rot_t.RR,   (source) -> RR(source));
    RotMap.put(Rot_t.SLA,  (source) -> SLA(source));
    RotMap.put(Rot_t.SRA,  (source) -> SRA(source));
    RotMap.put(Rot_t.SWAP, (source) -> SWAP(source));
    RotMap.put(Rot_t.SRL,  (source) -> SRL(source));
  }

//#endregion

//#region ---- ---- ---- ---- ----  Opcodes  ---- ---- ---- ---- ---- ---- ---- ----

  //#region ---- ---- ---- ---- ---- Load Opcodes

  /**
   * Loads a value from an 8-bit register to another register
   * @param destination
   * @param source
   */
  void LD(Reg_8 destination, Reg_8 source) {
    if (destination == null || source == null) { return; }

    regSet.setByte(destination, regSet.getByte(source));
  }

  /**
   * Loads a value from an 8-bit register to another register
   * @param destination
   * @param source
   */
  void LD_16(Reg_16 destination, Reg_16 source) {
    if (destination == null || source == null) { return; }

    regSet.setWord(destination, regSet.getWord(source));
  }

  /**
   * Indirect loading
   * @param register
   * @param isAdest controls whether A is the destination or not
   */
  void LD_IN(Reg_16 register, boolean isAdest) {

  }

  /**
   * 8-bit load immediate
   * @param destination
   */
  void LD_IM(Reg_8 destination) {

  }

  /**
   * Put A into address $FF00 + register C, or put value at
   * memory address $FF00 + register C to register A.
   * @param isAdest
   */
  void LD_C(boolean isAdest) {

  }

  /**
   * Put value A into memory address at nn, or put value at
   * memory address to register A.
   * @param isAdest
   */
  void LD_A(boolean isAdest) {

  }

  /**
   * 16-bit load immediate
   * @param destination
   * @param isdest
   */
  void LD_16_IM(Reg_16 register, boolean isdest) {

  }
  
  /**
   * Load value at address (HL)/A into A/(HL). Increment HL.
   * @param register
   * @param isAdest controls whether A is the destination or not
   */
  void LDI(Reg_16 register, boolean isAdest) {
    
  }

  /**
   * Load value at address (HL)/A into A/(HL). Decrement HL.
   * @param register
   * @param isAdest controls whether A is the destination or not
   */
  void LDD(Reg_16 register, boolean isAdest) {

  }
    
  /**
   * Put A into memory address $FF00+n, or Put memory address $FF00+n into A.
   * @param isAdest controls whether A is the destination or not
   */
  void LDH(boolean isAdest) {

  }

  /**
   * Put SP + n effective address into HL.
   */
  void LDHL() {

  }

  /**
   * 
   */
  void POP(Reg_16 destination) {

  }
  
  /**
   * 
   */
  void PUSH(Reg_16 source) {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- ALU Opcodes
  /**
   * 
   * @param source
   */
  void ADC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() + value + (regSet.getCarryFlag() ? 1 : 0);
    int before = regSet.getA();

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if ((result & 0xFF00) > 0) regSet.setCarryFlag();
    else regSet.clearCarryFlag();

    if (((before & 0x0F) + (value & 0x0F)) > 0x0F) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.clearNegativeFlag();
    regSet.setA(result & 0xFF);
  }

  /**
   * 
   */
  void ADC_IM() {

  }

  /**
   * 
   * @param source
   */
  void ADD(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() + value;
    int before = regSet.getA();

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if ((result & 0xFF00) > 0) regSet.setCarryFlag();
    else regSet.clearCarryFlag();

    if (((before & 0x0F) + (value & 0x0F)) > 0x0F) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.clearNegativeFlag();
    regSet.setA(result & 0xFF);
  }

  /**
   * 
   */
  void ADD_IM() {

  }

  /**
   * 
   */
  void ADD_HL(Reg_16 source) {
    int value = regSet.getWord(source);
    int result = regSet.getWord(Reg_16.HL) + value;
    int before = regSet.getWord(Reg_16.HL);

    if ((result & 0xF0000) > 0) regSet.setCarryFlag();
    else regSet.clearCarryFlag();

    if (((before & 0x00FF) + (value & 0x00FF)) > 0x00FF) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.clearNegativeFlag();
    regSet.setWord(Reg_16.HL, result);
  }

  /**
   * 
   */
  void ADD_SP() {

  }

  /**
   * 
   * @param source
   */
  void SUB(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() - value;
    int before = regSet.getA();

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if (value > before) regSet.setCarryFlag();
    else regSet.clearCarryFlag();

    if (((before & 0x0F) - (value & 0x0F)) < 0) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.setNegativeFlag();
    regSet.setA(result & 0xFF);
  }

  /**
   * 
   */
  void SUB_IM() {

  }

  /**
   * 
   * @param source
   */
  void SBC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() - value + (regSet.getCarryFlag() ? 1 : 0);
    int before = regSet.getA();

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if (value > before) regSet.setCarryFlag();
    else regSet.clearCarryFlag();

    if (((before & 0x0F) - (value & 0x0F)) < 0) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.setNegativeFlag();
    regSet.setA(result & 0xFF);
  }

  /**
   * 
   */
  void SBC_IM() {

  }
  
  /**
   * 
   * @param source
   */
  void AND(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() & value;

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    regSet.clearNegativeFlag();
    regSet.clearCarryFlag();
    regSet.setHalfCarryFlag();

    regSet.setA(result & 0xFF);
  }

  /**
   * 
   */
  void AND_IM() {

  }

  /**
   * 
   * @param source
   */
  void OR(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() | value;

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    regSet.clearNegativeFlag();
    regSet.clearCarryFlag();
    regSet.clearHalfCarryFlag();

    regSet.setA(result & 0xFF);
  }

  /**
   * 
   */
  void OR_IM() {

  }

  /**
   * 
   * @param source
   */
  void XOR(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() ^ value;

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    regSet.clearNegativeFlag();
    regSet.clearCarryFlag();
    regSet.clearHalfCarryFlag();

    regSet.setA(result & 0xFF);
  }

  /**
   * 
   */
  void XOR_IM() {

  }

  /**
   * 
   * @param source
   */
  void CP(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() - value + (regSet.getCarryFlag() ? 1 : 0);
    int before = regSet.getA();

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if (value > before) regSet.setCarryFlag();
    else regSet.clearCarryFlag();

    if (((before & 0x0F) - (value & 0x0F)) < 0) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.setNegativeFlag();
  }

  /**
   * 
   */
  void CP_IM() {

  }

  /**
   * 
   * @param source
   */
  void INC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = ++value;

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if ((result & 0xF0) > 0) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.clearNegativeFlag();
    if (source != null) { regSet.setByte(source, result & 0xFF); }
  }

  /**
   * 
   * @param source
   */
  void INC_16(Reg_16 source) {
    int value = regSet.getWord(source);
    int result = ++value;

    regSet.setWord(source, result);
  }

  /**
   * 
   * @param source
   */
  void DEC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = --value;

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if ((result & 0x0F) < 0) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.setNegativeFlag();
    if (source != null) { regSet.setByte(source, result & 0xFF); }
  }

  /**
   * 
   * @param source
   */
  void DEC_16(Reg_16 source) {
    int value = regSet.getWord(source);
    int result = --value;

    regSet.setWord(source, result);
  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Misc Opcodes

  /**
   * Swaps the upper and lower nibbles of the source registers
   * @param source
   */
  void SWAP(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = ((value & 0xf0) >> 4) + ((value & 0x0f) << 4);

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();
    
    regSet.clearNegativeFlag();
    regSet.clearCarryFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xff);
  }

  /**
   * Decimal Adjust Operation. Adds or substracts 6 to the lower nibble or higher nibble
   * of the A register depending on which flags are set.
   * This is to correct BCD addition problems
   */
  void DAA() {
    int result = regSet.getA();

    if (regSet.getNegativeFlag()) {
      if (regSet.getHalfCarryFlag()) { result = (result - 0x06) & 0xFF; }
      if (regSet.getCarryFlag()) { result -= 0x60; }
    }
    else {
      if (regSet.getHalfCarryFlag() || (result & 0xF) > 0x09) { result += 0x06; }
      if (regSet.getCarryFlag() || (result > 0x9F)) { result += 0x60; }
    }

    if (result != 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if (result >= 0x100) regSet.setCarryFlag();

    regSet.clearHalfCarryFlag();
    regSet.setA(result & 0xFF);
  }

  /**
   * Compliments the A register (flips all the bits)
   */
  void CPL() {
    regSet.setA((~regSet.getA()) & 0xFF);
    regSet.setNegativeFlag();
    regSet.setHalfCarryFlag();
  }

  /**
   * Compliments the carry flag
   */
  void CCF() {
    if (regSet.getCarryFlag()) { regSet.clearCarryFlag(); }
    else { regSet.setCarryFlag(); }

    regSet.clearHalfCarryFlag();
    regSet.clearNegativeFlag();
  }

  /**
   * Sets the carry flag
   */
  void SCF() {
    regSet.setCarryFlag();

    regSet.clearHalfCarryFlag();
    regSet.clearNegativeFlag();
  }

  /**
   * No Operation... Nothing happens of course :P
   */
  void NOP() { }

  /**
   * Power down CPU until an interrupt occurs. Use this
   * when ever possible to reduce energy consumption.
   */
  void HALT() {
    /** NOTE: Cannot implement until interrupts are implemented */
  }
  
  /**
   * Halt CPU & LCD display until button pressed.
   */
  void STOP() {
    /** NOTE: Cannot implement until interrupts are implemented */
  }

  /**
   * Disables Interrupts
   */
  void DI() {
    /** NOTE: Cannot implement until interrupts are implemented */
  }
  
  /**
   * Enables Interrupts
   */
  void EI() {
    /** NOTE: Cannot implement until interrupts are implemented */
  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Rotates and Shifts 

  /**
   * Circular left rotates register A. Like a logical shift but wraps from bit 7 back to bit 0.
   * It sets/resets the carry flag based on the initial 7th bit value in register A.
   */
  void RLCA() {
    int result = regSet.getA() << 1;
    
    if ((result & 0x100) > 0) {
      regSet.setCarryFlag();
      result |= 1;
    }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    regSet.setA(result & 0xFF);
  }

  /**
   * Circular left rotates register A, and sets bit 0 to the carry flag.
   * It sets/resets the carry flag based on the initial 7th bit value.
   */
  void RLA() {
    int result = regSet.getA() << 1;
    
    if (regSet.getCarryFlag()) { result |= 0x01; }
    if ((result & 0x100) > 0) { regSet.setCarryFlag(); }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    regSet.setA(result & 0xFF);
  }

  /**
   * Circular right rotates register A. Like a logical shift but wraps from bit 0 back to bit 7.
   * It sets/resets the carry flag based on the initial 0th bit value in register A.
   */
  void RRCA() {
    int result = regSet.getA() >> 1;
    
    if ((regSet.getA() & 0x1) > 0) {
      regSet.setCarryFlag();
      result |= 0x80;
    }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    regSet.setA(result & 0xFF);
  }

  /**
   * Circular right rotates register A, and sets bit 7 to the carry flag.
   * It sets/resets the carry flag based on the initial 7th bit value.
   */
  void RRA() {
    int result = regSet.getA() >> 1;
    
    if (regSet.getCarryFlag()) { result |= 0x80; }
    if ((regSet.getA() & 0x1) > 0) { regSet.setCarryFlag(); }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    regSet.setA(result & 0xFF);
  }

  /**
   * Like RLCA but works for the other registers (extended opcode)
   * @param source
   */
  void RLC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = value << 1;
    
    if ((result & 0x100) > 0) {
      regSet.setCarryFlag();
      result |= 1;
    }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xFF);
  }
  
  /**
   * Like RLA but works for the other registers (extended opcode)
   * @param source
   */
  void RL(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = value << 1;
    
    if (regSet.getCarryFlag()) { result |= 0x01; }
    if ((result & 0x100) > 0) { regSet.setCarryFlag(); }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xFF);
  }
  
  /**
   * Like RRCA but works for the other registers (extended opcode)
   * @param source
   */
  void RRC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = value >> 1;
    
    if ((value & 0x1) > 0) {
      regSet.setCarryFlag();
      result |= 0x80;
    }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xFF);
  }
  
  /**
   * Like RRA but works for the other registers (extended opcode)
   * @param source
   */
  void RR(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = value >> 1;
    
    if (regSet.getCarryFlag()) { result |= 0x80; }
    if ((value & 0x1) > 0) { regSet.setCarryFlag(); }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xFF);
  }

  /**
   * Left Shifts the source register. Also resets the 0th bit of the source register.
   * @param source
   */
  void SLA(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = value << 1;

    if ((result & 0x100) > 0) { regSet.setCarryFlag(); }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xFF);
  }
  
  /**
   * Right shifts the source register. Keeps the value in the 7th bit so it like an arithmetic shift.
   * @param source
   */
  void SRA(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = (value >> 1) + (value & 0x80);
    
    if ((value & 0x1) > 0) { regSet.setCarryFlag(); }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xFF);
  }
  
  /**
   * Right shifts the source register. Also resets bit 0 of the source register.
   * @param source
   */
  void SRL(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = (value >> 1);
    
    if ((value & 0x1) > 0) { regSet.setCarryFlag(); }
    else { regSet.clearCarryFlag(); }

    if (result != 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.clearHalfCarryFlag();

    if (source != null) regSet.setByte(source, result & 0xFF);
  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Bit Opcodes

  /**
   * Used to check if the bit in position b is set/reset in the source register
   * @param b
   * @param source
   */
  void BIT(int b, Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    if ((value & (1 << b)) > 0) { regSet.clearZeroFlag(); }
    else { regSet.setZeroFlag(); }

    regSet.clearNegativeFlag();
    regSet.setHalfCarryFlag();
  }

  /**
   * Sets the bit in position b in the source register
   * @param b
   * @param source
   */
  void SET(int b, Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    value |= 1 << b;

    if (source != null) { regSet.setByte(source, value); }
  }

  /**
   * Resets the bit in position b in the source register
   * @param b
   * @param source
   */
  void RES(int b, Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    value &= ~(1 << b);

    if (source != null) { regSet.setByte(source, value); }
  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Jump Opcodes

  /**
   * Jump to address at nn.
   */
  void JP() {
    short operand = 0; // Ideally read from memory to get the address?
    regSet.setPC(operand);
  }

  /**
   * Jump to address at nn if the flag condition is true
   * @param flag_condition
   */
  void JP_CC(CC_t flag_condition) {
    short operand = 0; // Ideally read from memory to get the address?
    switch(flag_condition) {
      case NZ: if (!regSet.getZeroFlag()) regSet.setPC(operand); break;
      case Z:  if (regSet.getZeroFlag()) regSet.setPC(operand); break;
      case NC: if (!regSet.getCarryFlag()) regSet.setPC(operand); break;
      case C:  if (regSet.getCarryFlag()) regSet.setPC(operand); break;
    }
  }

  /**
   * Jump to address contained in HL.
   */
  void JP_HL() {
    regSet.setPC(regSet.getWord(Reg_16.HL));
  }
  
  /**
   * Add n to current address and jump to it.
   */
  void JR() {

  }

  /**
   * Add n to current address and jump to it, if the flag condition is true.
   * @param flag_condition
   */
  void JR_CC(CC_t flag_condition) {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Call Opcodes

  /**
   * Push address of next instruction onto stack and then jump to address nn.
   */
  void CALL() {

  }

  /**
   * Push address of next instruction onto stack and then jump to address nn,
   * if the flag condition is true.
   * @param flag_condition
   */
  void CALL_CC(CC_t flag_condition) {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Restart Opcodes

  /**
   * Push present address onto stack. Jump to address $0000 + n.
   * @param n
   */
  void RST(int n) {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Return Opcodes

  /**
   * Pop two bytes from stack & jump to that address.
   */
  void RET() {

  }

  /**
   * Pop two bytes from stack & jump to that address,
   * if the flag condition is true.
   * @param flag_condition
   */
  void RET_CC(CC_t flag_condition) {

  }
  
  /**
   * Pop two bytes from stack & jump to that address, then enable interrupts.
   */
  void RETI() {

  }

  //#endregion
//#endregion

//#region ---- ---- ---- ---- ----   Opcode Decoding   ---- ---- ---- ---- ---- ----

  final int M_CYCLE = 4;          // Clock cycle for a single-byte operation in the Gameboy
  final int X_MASK = 0b11000000;  // Mask for the X portion of the arguments in the Opcode.
  final int Y_MASK = 0b00111000;  // Mask for the Y portion of the arguments in the Opcode.
  final int Z_MASK = 0b00000111;  // Mask for the Z portion of the arguments in the Opcode.
  final int P_MASK = 0b00110000;  // Mask for the P portion of the arguments in the Opcode.
  final int Q_MASK = 0b00001000;  // Mask for the Q portion of the arguments in the Opcode.

  public String current_opcode = "not decoded";
  
  /**
   * 
   * @param y_arg
   * @param z_arg
   * @param p_arg
   * @param q_arg
   * @return
   */
  public int x0_Opcodes(int y_arg, int z_arg, int p_arg, int q_arg) {
    switch(z_arg) {
      case 0:
        switch(y_arg) {
          case 0: NOP(); current_opcode = "NOP"; return M_CYCLE;
          case 1: LD_16_IM(Reg_16.SP, false); current_opcode = "LD (nn), SP"; return M_CYCLE;
          case 2: STOP(); current_opcode = "STOP"; return M_CYCLE;
          case 3: JR(); current_opcode = "JR"; return M_CYCLE;
          default: JR_CC(cc_args[y_arg-4]); current_opcode = "JR " + cc_args[y_arg-4] + ", d"; return M_CYCLE;
        }
      case 1:
        if (q_arg == 0) {
          LD_16_IM(rp_args[p_arg], true); current_opcode = "LD " + rp_args[p_arg] + ", nn"; return M_CYCLE;
        } else {
          ADD_HL(rp_args[p_arg]); current_opcode = "ADD HL, " + rp_args[p_arg]; return M_CYCLE;
        }
      case 2:
        if (q_arg == 0) {
          switch(p_arg) {
            case 0: LD_IN(Reg_16.BC, false); current_opcode = "LD (BC), A"; break;
            case 1: LD_IN(Reg_16.DE, false); current_opcode = "LD (BC), A"; break;
            case 2:   LDI(Reg_16.HL, false); current_opcode = "LDI (HL+), A"; break;
            case 3:   LDD(Reg_16.HL, false); current_opcode = "LDD (HL-), A"; break;
          }
        } else {
          switch(p_arg) {
            case 0: LD_IN(Reg_16.BC, true); current_opcode = "LD A, (BC)"; break;
            case 1: LD_IN(Reg_16.DE, true); current_opcode = "LD A, (DE)"; break;
            case 2:   LDI(Reg_16.HL, true); current_opcode = "LDI A, (HL)"; break;
            case 3:   LDD(Reg_16.HL, true); current_opcode = "LDD A, (HL)"; break;
          }
        }
        break;
      case 3:
        if (q_arg == 0) {
          INC_16(rp_args[p_arg]);  current_opcode = "INC " + rp_args[p_arg]; return M_CYCLE;
        } else {
          DEC_16(rp_args[p_arg]);  current_opcode = "DEC " + rp_args[p_arg]; return M_CYCLE;
        }
      case 4:   INC(r_args[y_arg]); current_opcode = "INC " + (r_args[y_arg] != null ? r_args[y_arg].toString() : "(HL)"); return M_CYCLE;
      case 5:   DEC(r_args[y_arg]); current_opcode = "DEC " + (r_args[y_arg] != null ? r_args[y_arg].toString() : "(HL)"); return M_CYCLE;
      case 6: LD_IM(r_args[y_arg]); current_opcode = "LD "  + ((r_args[y_arg] != null) ? r_args[y_arg].toString() : "(HL)") + ", " + "n"; return M_CYCLE;
      case 7: Z7Map.get(z7_args[y_arg]).invoke(); current_opcode = "" + Z7_t.values()[y_arg]; return M_CYCLE;
    }
    return 0;
  }

  /**
   * 
   * @param y_arg
   * @param z_arg
   * @param p_arg
   * @param q_arg
   * @return
   */
  public int x1_Opcodes(int y_arg, int z_arg, int p_arg, int q_arg) {
    if ((y_arg == 6) && (z_arg == 6)) { HALT(); current_opcode = "HALT"; return M_CYCLE; }
    else {
      LD(r_args[y_arg], r_args[z_arg]);

      current_opcode = "LD " +
      ((r_args[y_arg] != null) ? r_args[y_arg].toString() : "(HL)") + ", " +
      ((r_args[z_arg] != null) ? r_args[z_arg].toString() : "(HL)");
      return M_CYCLE;
    }
  }

  /**
   * 
   * @param y_arg
   * @param z_arg
   * @param p_arg
   * @param q_arg
   * @return
   */
  public int x2_Opcodes(int y_arg, int z_arg, int p_arg, int q_arg) {
    AluMap.get(alu_args[y_arg]).invoke(r_args[z_arg]);
    current_opcode = Alu_t.values()[y_arg] + " A, " + ((r_args[z_arg] != null) ? r_args[z_arg].toString() : "(HL)");
    return M_CYCLE;
  }

  /**
   * 
   * @param y_arg
   * @param z_arg
   * @param p_arg
   * @param q_arg
   * @return
   */
  public int x3_Opcodes(int y_arg, int z_arg, int p_arg, int q_arg) {
    switch(z_arg) {
      case 0:
        if (y_arg < 4) { RET_CC(cc_args[y_arg]); current_opcode = "RET " + cc_args[y_arg]; return M_CYCLE; }
        else {
          switch(y_arg) {
            case 0: LDH(false); current_opcode = "LDH (n), A"; return M_CYCLE;
            case 1: ADD_SP(); current_opcode = "ADD SP, d"; return M_CYCLE;
            case 2: LDH(true); current_opcode = "LDH A, (n)"; return M_CYCLE;
            case 3: LDHL(); current_opcode = "LDHL SP, n"; return M_CYCLE;
          }
        }
      case 1:
        if (q_arg == 0) { POP(rp2_args[p_arg]); current_opcode = "POP " + rp2_args[p_arg]; return M_CYCLE; }
        else {
          switch(p_arg) {
            case 0: RET(); current_opcode = "RET"; return M_CYCLE;
            case 1: RETI(); current_opcode = "RETI"; return M_CYCLE;
            case 2: JP_HL(); current_opcode = "JP (HL)"; return M_CYCLE;
            case 3: LD_16(Reg_16.SP, Reg_16.HL); current_opcode = "LD SP, HL"; return M_CYCLE;
          }
        }
      case 2:
        if (y_arg < 4) { JP_CC(cc_args[y_arg]); current_opcode = "JP " + cc_args[y_arg] + ", nn"; return M_CYCLE; }
        else {
          switch(y_arg) {
            case 4: LD_C(false); current_opcode = "LD (0xFF00+C),A"; return M_CYCLE;
            case 5: LD_A(false); current_opcode = "LD (nn),A"; return M_CYCLE;
            case 6: LD_C(true); current_opcode = "LD A,(0xFF00+C)"; return M_CYCLE;
            case 7: LD_A(true); current_opcode = "LD A,(nn)"; return M_CYCLE;
            default: return M_CYCLE;
          }
        }
      case 3:
        if (y_arg == 0) { JP(); current_opcode = "JP nn" ; return M_CYCLE; }
        else if (y_arg == 6) { DI(); current_opcode = "DI"; return M_CYCLE; }
        else if (y_arg == 7) { EI(); current_opcode = "EI"; return M_CYCLE; }
      case 4: 
        if (y_arg < 4) { CALL_CC(cc_args[y_arg]); current_opcode = "Call " + cc_args[y_arg] + ", nn"; return M_CYCLE; }
      case 5:
        if (q_arg == 0) { PUSH(rp2_args[p_arg]); current_opcode = "PUSH " + rp2_args[p_arg]; return M_CYCLE; }
        else { CALL(); current_opcode = "CALL nn"; return M_CYCLE; }
      case 6: AluImMap.get(alu_args[y_arg]).invoke(); current_opcode = Alu_t.values()[y_arg] + " A, nn"; return M_CYCLE;
      case 7: RST(y_arg*8); current_opcode = "RST " + String.format("%x", y_arg*8); return M_CYCLE;
    }
    return 0;
  }

  /**
   * <p>Decodes and executes the opcode specified in the argument.</p>
   * Slightly Based on:
   * http://www.codeslinger.co.uk/pages/projects/gameboy/opcodes.html
   * 
   * Mostly Based on:
   * https://gb-archive.github.io/salvage/decoding_gbz80_opcodes/Decoding%20Gamboy%20Z80%20Opcodes.html
   * 
   * @param opcode - Opcode Command
   * @return Clock cycles needed for the operation
   */
  public int executeOpcode(short opcode) {
    // CB-prefixed opcodes
    if (opcode == 0xCB) {
      //opcode = ReadMemory(regSet.getPC());
      regSet.setPC(regSet.getPC() + 1);
      return executeExtendedOpcode(opcode);
    }

    int x_arg = (opcode & X_MASK) >> 6;
    int y_arg = (opcode & Y_MASK) >> 3;
    int z_arg = (opcode & Z_MASK);
    int p_arg = (opcode & P_MASK) >> 4;
    int q_arg = (opcode & Q_MASK) >> 3;

    current_opcode = String.format("x: %d, y: %d, z: %d, p: %d, q: %d", x_arg, y_arg, z_arg, p_arg, q_arg);

    switch(x_arg) {
      case 0: return x0_Opcodes(y_arg, z_arg, p_arg, q_arg);
      case 1: return x1_Opcodes(y_arg, z_arg, p_arg, q_arg);
      case 2: return x2_Opcodes(y_arg, z_arg, p_arg, q_arg);
      case 3: return x3_Opcodes(y_arg, z_arg, p_arg, q_arg);
      default:    // Undefined
        System.out.println("Opcode is undefined!");
        break;
    }

    return 0;
  }

  /**
   * Code from http://www.codeslinger.co.uk/pages/projects/gameboy/opcodes.html
   * @return
   */
  int executeExtendedOpcode(short opcode) {
    int x_arg = (opcode & X_MASK) >> 6;
    int y_arg = (opcode & Y_MASK) >> 3;
    int z_arg = (opcode & Z_MASK);

    current_opcode = String.format("Extended Opcode... x: %d, y: %d, z: %d", x_arg, y_arg, z_arg);

    switch(x_arg) {
      case 0 :
        RotMap.get(rot_args[y_arg]).invoke(r_args[z_arg]);
        current_opcode = Rot_t.values()[y_arg] + " " + ((r_args[z_arg] != null) ? r_args[z_arg].toString() : "(HL)");
        return M_CYCLE*2;
      case 1: BIT(y_arg, r_args[z_arg]); current_opcode = "BIT " + y_arg + ", " + ((r_args[z_arg] != null) ? r_args[z_arg].toString() : "(HL)"); return M_CYCLE*2;
      case 2: RES(y_arg, r_args[z_arg]); current_opcode = "RES " + y_arg + ", " + ((r_args[z_arg] != null) ? r_args[z_arg].toString() : "(HL)"); return M_CYCLE*2;
      case 3: SET(y_arg, r_args[z_arg]); current_opcode = "SET " + y_arg + ", " + ((r_args[z_arg] != null) ? r_args[z_arg].toString() : "(HL)"); return M_CYCLE*2;

      default:
        //assert(false);
        return 0; // unhandled extended opcode
      }
  }
  
  /**
   * Code from http://www.codeslinger.co.uk/pages/projects/gameboy/opcodes.html
   * @return
   */
  int executeNextOpcode() {
    int res = 0;
    short opcode = 0;//ReadMemory(regSet.getPC());
    regSet.setPC(regSet.getPC() + 1);
    res =	executeOpcode(opcode);
    return res ;
  }

  //#endregion

//#region ---- ---- ---- ---- ----  Opcode Tester Programs  ---- ---- ---- ---- ----

  /**
   * Tests 8-bit ALU commands on every register.
   * @param ALU_opcode r_args value of the opcode
   */
  public void alu_tests(int ALU_opcode) {
    
    System.out.println("\nRegisters before instruction:");
    System.out.println(regSet);

    // Loops through every 8-bit register that can be an argument
    for (int j = 0; j < 7; j++) {
      // Encodes the instruction to the proper format: alu[y], r[z]
      int instruction = 0b10000000 + (Y_MASK & (ALU_opcode << 3)) + (Z_MASK & j);

      // Runs the opcode and returns how many M-cycles it took
      int clockCycles = executeOpcode((short)instruction);
      
      System.out.print("Current instruction: " + current_opcode);
      System.out.printf("\tResults in A: 0x%02x", regSet.getByte(Reg_8.A));
      System.out.print("   " + regSet.getFlagsShort());
      System.out.println("   Cycles: " + (clockCycles/4) + " M");
    }

    System.out.println("\nRegisters after instructions:");
    System.out.println(regSet);
    System.out.println("--------------------------------------------------------------------");
  }

  /**
   * Tests opcodes with x_arg = 0.
   * @param z_num value of the z_arg bits
   * @param is_Q set to 1 or 0, sets the q_arg flag
   */
  public void x0_tests(int z_num, int y, int is_Q) {

    System.out.println("--------------------------------------------------------------------");

    if (y == -1) {
      System.out.println("\nRegisters before instruction:");
      System.out.println(regSet);
    }
    // Loops though every 16-bit register that can be an argument.
    // If the instruction uses 8-bit registers, is_Q is used to get
    // either even or odd indexed registers
    for (int j = 0; j < 4; j++) {
      if (y != -1) { j = y; System.out.println("\nRegisters before instruction:"); System.out.print(regSet); }

      // Encodes the instruction to the proper format: alu[y] a, r[z]
      int instruction = (P_MASK & (j << 4)) + (Q_MASK * is_Q) + (Z_MASK & z_num);

      // Runs the opcode and returns how many M-cycles it took
      int clockCycles = executeOpcode((short)instruction);
      
      System.out.print("Current instruction: " + current_opcode);

      // This is so the proper destination register is printed out for the results
      if (z_num == 1) { System.out.printf("\tResults in HL: 0x%04x", regSet.getWord(Reg_16.HL)); }
      else if (z_num == 3) { System.out.printf("\tResults in %s: 0x%04x", rp_args[j], regSet.getWord(rp_args[j])); }
      else if (z_num == 7) { System.out.printf("\tResults in A: 0x%02x", regSet.getA()); }
      else if (((j << 1) + is_Q) != 6) { System.out.printf("\tResults in %s: 0x%04x", r_args[(j << 1) + is_Q], regSet.getByte(r_args[(j << 1) + is_Q])); }

      System.out.print("   " + regSet.getFlagsShort());
      System.out.println("   Cycles " + (clockCycles/4) + " M");
      if (y != -1) { break; }
    }
    
    System.out.println("\nRegisters after instructions:");
    System.out.println(regSet);
  }

  /**
   * Tests 8-bit ROT commands on every register.
   * @param ROT_opcode r_args value of the opcode
   */
  public void rot_tests(int ROT_opcode, boolean setCarry) {
    
    System.out.println("\nRegisters before instructions:");
    System.out.println(regSet);

    // Loops through every 8-bit register that can be an argument
    for (int j = 0; j < 8; j++) {
      if (setCarry) { regSet.setCarryFlag(); }
      else { regSet.clearCarryFlag(); }

      System.out.println(regSet.getFlagsShort());

      // Encodes the instruction to the proper format: rot[y], r[z]
      int instruction = (Y_MASK & (ROT_opcode << 3)) + (Z_MASK & j);

      // Runs the opcode and returns how many M-cycles it took
      int clockCycles = executeExtendedOpcode((short)instruction);
      
      System.out.print("Current instruction (extended): " + current_opcode);
      if (j != 6) { System.out.printf("\tResults in %s: 0x%02x", r_args[j], regSet.getByte(r_args[j])); }
      else { System.out.printf("\tResults in (HL): 0x%02x", 0); }
      System.out.print("   " + regSet.getFlagsShort());
      System.out.print("   Cycles: " + (clockCycles/4) + " M\n");
    }

    System.out.println("\nRegisters after instructions:");
    System.out.println(regSet);
    System.out.println(regSet.getFlags());
    System.out.println("--------------------------------------------------------------------");
  }

  /**
   * Bit operation tests
   * @param BIT_opcode r_args value of the opcode
   */
  public void BIT_tests(int BIT_opcode) {
    
    System.out.println("\nRegisters before instructions:");
    System.out.println(regSet);

    // Loops through every 8-bit register that can be an argument
    for (int j = 0; j < 8; j++) {
      for (int i = 0; i < 8; i++) {
        System.out.println(regSet.getFlagsShort());

        // Encodes the instruction to the proper format: BIT/RES/SET y, r[z]
        int instruction = (BIT_opcode << 6) + (Y_MASK & (i << 3)) + (Z_MASK & j);

        // Runs the opcode and returns how many M-cycles it took
        int clockCycles = executeExtendedOpcode((short)instruction);
        
        System.out.print("Current instruction (extended): " + current_opcode);
        if (j != 6) { System.out.printf("\tResults in %s: 0x%02x", r_args[j], regSet.getByte(r_args[j])); }
        else { System.out.printf("\tResults in (HL): 0x%02x", 0); }
        System.out.print("   " + regSet.getFlagsShort());
        System.out.print("   Cycles: " + (clockCycles/4) + " M\n");
      }
    }

    System.out.println("\nRegisters after instructions:");
    System.out.println(regSet);
    System.out.println(regSet.getFlags());
    System.out.println("--------------------------------------------------------------------");
  }

  /**
   * CPU Tester Program
   * You compile this with this command:
   * javac -d compiled CPU.java
   * 
   * run with this command:
   * java -cp compiled CPU
   * @param args
   */
  public static void main(String[] args) {
    CPU cpu = new CPU();
    cpu.regSet = new RegisterSet();

    System.out.println("CPU Tests....");

    //#region 8-bit ALU instructions

    cpu.alu_tests(Alu_t.ADD.index);
    cpu.alu_tests(Alu_t.ADC.index);
    cpu.alu_tests(Alu_t.SUB.index);
    cpu.alu_tests(Alu_t.SBC.index);
    cpu.alu_tests(Alu_t.AND.index);
    cpu.alu_tests(Alu_t.XOR.index);
    cpu.alu_tests(Alu_t.OR.index);
    cpu.alu_tests(Alu_t.CP.index);

    //#endregion
    //#region x=0 tests

    // Resets the register set
    cpu.regSet = new RegisterSet();

    // 16-bit ADD HL
    cpu.x0_tests(1, -1, 1);

    // 16-bit INC
    cpu.x0_tests(3, -1, 0);

    // 16-bit DEC
    cpu.x0_tests(3, -1, 1);

    // 8-bit INC
    cpu.x0_tests(4, -1, 0);
    cpu.x0_tests(4, -1, 1);

    // 8-bit DEC
    cpu.x0_tests(5, -1, 0);
    cpu.x0_tests(5, -1, 1);

    //#endregion
    //#region Rotation and other x=0 opcodes

    // RLCA
    cpu.regSet = new RegisterSet();
    cpu.regSet.setA(0xF0);
    cpu.regSet.clearCarryFlag();
    cpu.x0_tests(7, 0, 0);
    System.out.println("Without carry Expected: 0xe1");
    cpu.regSet.setA(0x70);
    cpu.regSet.setCarryFlag();
    cpu.x0_tests(7, 0, 0);
    System.out.println("With Carry Expected: 0xe0");

    // RLA
    cpu.regSet.setA(0xF0);
    cpu.regSet.clearCarryFlag();
    cpu.x0_tests(7, 1, 0);
    System.out.println("w/ carry Expected: 0xe0");
    cpu.regSet.setA(0x70);
    cpu.regSet.setCarryFlag();
    cpu.x0_tests(7, 1, 0);
    System.out.println("w/out Expected: 0xe1");

    // RRCA
    cpu.regSet.setA(0x0F);
    cpu.regSet.clearCarryFlag();
    cpu.x0_tests(7, 0, 1);
    System.out.println("w/ Expected: 0x87");
    cpu.regSet.setA(0x0E);
    cpu.regSet.setCarryFlag();
    cpu.x0_tests(7, 0, 1);
    System.out.println("w/out Expected: 0x07");

    // RRA
    cpu.regSet.setA(0x0F);
    cpu.regSet.clearCarryFlag();
    cpu.x0_tests(7, 1, 1);
    System.out.println("w/ Expected: 0x07");

    cpu.regSet.setA(0x0E);
    cpu.regSet.setCarryFlag();
    cpu.x0_tests(7, 1, 1);
    System.out.println("w/out Expected: 0x87");

    cpu.regSet = new RegisterSet();

    cpu.x0_tests(7, 2, 0);  // DAA
    cpu.x0_tests(7, 2, 1);  // CPL
    cpu.x0_tests(7, 3, 1);  // SCF
    cpu.x0_tests(7, 3, 1);  // CCF

    //#endregion
    //#region Jump instruction tests

    // JP HL
    cpu.regSet.setWord(Reg_16.HL, 0x156f);
    System.out.print(cpu.regSet);
    cpu.executeOpcode((short)0xE9);
    System.out.println("Current instruction: " + cpu.current_opcode);
    System.out.println(cpu.regSet);

    //#endregion
    //#region Extended opcodes:
    cpu.regSet = new RegisterSet();

    // Rotational/Shift operations
    cpu.rot_tests(Rot_t.SWAP.index, false);
    cpu.rot_tests(Rot_t.RLC.index, false);
    cpu.rot_tests(Rot_t.RLC.index, true);
    cpu.rot_tests(Rot_t.RRC.index, false);
    cpu.rot_tests(Rot_t.RRC.index, true);
    cpu.rot_tests(Rot_t.RL.index, false);
    cpu.rot_tests(Rot_t.RL.index, true);
    cpu.rot_tests(Rot_t.RR.index, false);
    cpu.rot_tests(Rot_t.RR.index, true);
    cpu.rot_tests(Rot_t.SLA.index, false);
    cpu.rot_tests(Rot_t.SLA.index, true);
    cpu.rot_tests(Rot_t.SRA.index, false);
    cpu.rot_tests(Rot_t.SRA.index, true);
    cpu.rot_tests(Rot_t.SRL.index, false);
    cpu.rot_tests(Rot_t.SRL.index, true);

    // Bit tests
    cpu.BIT_tests(1);

    // Bit resets
    cpu.regSet = new RegisterSet(0xFF00, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFE, 0x100);
    cpu.BIT_tests(2);

    // Bit sets
    cpu.BIT_tests(3);

    //#endregion
  }

//#endregion
}
