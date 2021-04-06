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
  public RegisterSet regSet;

  // private RegisterSet save0 = new RegisterSet(0, 0, 0, 0, 0, 0);
  // private RegisterSet save1 = new RegisterSet(0, 0, 0, 0, 0, 0);
  // private RegisterSet save2 = new RegisterSet(0, 0, 0, 0, 0, 0);
  // private RegisterSet save3 = new RegisterSet(0, 0, 0, 0, 0, 0);

//#endregion

//#region ---- ---- ---- ---- ---- Dissassembly Decode Table ---- ---- ---- ---- ---- ----
  
  /**
   * Argument modifiers for Jump Instructions specifying certain conditions to jump.
   * NZ = no zero
   * Z = zero
   * NC = no Carry
   * C = Carry
   */
  enum CC_t {
    NZ, Z, NC, C;

    public int index;

    CC_t() { this.index = this.ordinal(); }
  }

  /**
   * ALU operation types for 8-bit ALU operations.
   * ADD = Add instruction
   * ADC = Add instruction with carry
   * SUB = Subtraction instruction
   * SBC = Subtraction instruction with carry
   * AND = And instruction
   * XOR = XOR instruction
   * OR  = Or instruction
   * CP  = Compare instruction
   */
  enum Alu_t {
    ADD, ADC, SUB, SBC, AND, XOR, OR, CP;

    public int index;

    Alu_t() { this.index = this.ordinal(); }
  }

  /**
   * 
   */
  enum Rot_t {
    RLC, RRC, RL, RR, SLA, SRA, SWAP, SRL;

    public int index;

    Rot_t() { this.index = this.ordinal(); }
  }

  // Opcode Argument Types
  final Reg_8[] r_args = { Reg_8.B, Reg_8.C, Reg_8.D, Reg_8.E, Reg_8.H, Reg_8.L, null, Reg_8.A };
  final Reg_16[] rp_args = { Reg_16.BC, Reg_16.DE, Reg_16.HL, Reg_16.SP };
  final Reg_16[] rp2_args = { Reg_16.BC, Reg_16.DE, Reg_16.HL, Reg_16.AF };
  final CC_t[] cc_args = CC_t.values();
  final Alu_t[] alu_args = Alu_t.values();
  final Rot_t[] rot_args = Rot_t.values();

  @FunctionalInterface
  interface AluCommand { public void invoke(Reg_8 source); }
  
  HashMap<Alu_t, AluCommand> AluMap = new HashMap<Alu_t, AluCommand>();

  public void initializeHashmaps() {
    AluMap.put(Alu_t.ADC, (source) -> ADC(source));
    AluMap.put(Alu_t.ADD, (source) -> ADD(source));
    AluMap.put(Alu_t.AND, (source) -> AND(source));
    AluMap.put(Alu_t.CP,  (source) -> CP(source));
    AluMap.put(Alu_t.OR,  (source) -> OR(source));
    AluMap.put(Alu_t.SBC, (source) -> SBC(source));
    AluMap.put(Alu_t.SUB, (source) -> SUB(source));
    AluMap.put(Alu_t.XOR, (source) -> XOR(source));
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
    regSet.setByte(destination, regSet.getByte(source));
  }
  
  /**
   * 
   */
  void LDD() {

  }
  
  /**
   * 
   */
  void LDH() {

  }
  
  /**
   * 
   */
  void LDI() {

  }

  /**
   * 
   */
  void LDHL() {

  }

  /**
   * 
   */
  void POP() {

  }
  
  /**
   * 
   */
  void PUSH() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- ALU Opcodes
  /**
   * 
   */
  void ADC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    // Code goes here!
  }

  /**
   * 
   */
  void ADC_I() {

  }

  /**
   * 
   * @param destination
   * @param source
   */
  void ADD(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    int result = regSet.getA() + value;

    regSet.setA(result & 0xFF);

    if ((result & 0xFF00) > 0) regSet.setCarryFlag();
    else regSet.clearCarryFlag();

    if (result > 0) regSet.clearZeroFlag();
    else regSet.setZeroFlag();

    if (((result & 0x0F) + (value & 0x0F)) > 0x0F) regSet.setHalfCarryFlag();
    else regSet.clearHalfCarryFlag();

    regSet.clearNegativeFlag();
  }

  /**
   * 
   */
  void ADD_I() {

  }

  /**
   * 
   */
  void ADD_I_16() {

  }

  /**
   * 
   */
  void ADD_16() {

  }

  /**
   * 
   */
  void SUB(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    // Code goes here!
  }

  /**
   * 
   */
  void SUB_I() {

  }

  /**
   * 
   */
  void SBC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    // Code goes here!
  }

  /**
   * 
   */
  void SBC_I() {

  }
  
  /**
   * 
   */
  void AND(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    // Code goes here!
  }

  /**
   * 
   */
  void AND_I() {

  }

  /**
   * 
   */
  void OR(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    // Code goes here!
  }

  /**
   * 
   */
  void OR_I() {

  }

  /**
   * 
   */
  void XOR(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    // Code goes here!
  }

  /**
   * 
   */
  void XOR_I() {

  }

  /**
   * 
   */
  void CP(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
    // Code goes here!
  }

  /**
   * 
   */
  void CP_I() {

  }

  /**
   * 
   */
  void INC() {

  }

  /**
   * 
   */
  void DEC() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Misc Opcodes

  /**
   * 
   */
  void SWAP() {

  }

  /**
   * 
   */
  void DAA() {

  }

  /**
   * 
   */
  void CPL() {

  }

  /**
   * 
   */
  void CCF() {

  }

  /**
   * 
   */
  void SCF() {

  }

  /**
   * 
   */
  void NOP() {

  }

  /**
   * 
   */
  void HALT() {

  }
  
  /**
   * 
   */
  void STOP() {

  }

  /**
   * 
   */
  void DI() {

  }
  
  /**
   * 
   */
  void EI() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Rotates and Shifts 

  /**
   * 
   */
  void RLCA() {

  }

  /**
   * 
   */
  void RLA() {

  }

  /**
   * 
   */
  void RRCA() {

  }

  /**
   * 
   */
  void RRA() {

  }

  /**
   * 
   */
  void RLC() {

  }
  
  /**
   * 
   */
  void RL() {

  }
  
  /**
   * 
   */
  void RRC() {

  }
  
  /**
   * 
   */
  void RR() {

  }

  /**
   * 
   */
  void SLA() {

  }
  
  /**
   * 
   */
  void SRA() {

  }
  
  /**
   * 
   */
  void SRL() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Bit Opcodes

  /**
   * 
   */
  void BIT() {

  }

  /**
   * 
   */
  void SET() {

  }

  /**
   * 
   */
  void RES() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Jump Opcodes

  /**
   * 
   */
  void JP() {

  }
  
  /**
   * 
   */
  void JR() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Call Opcodes

  /**
   * 
   */
  void CALL() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Restart Opcodes

  /**
   * 
   */
  void RST() {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Return Opcodes

  /**
   * 
   */
  void RET() {

  }
  
  /**
   * 
   */
  void RETI() {

  }

  //#endregion
//#endregion

//#region ---- ---- ---- ---- ---- Misc. CPU Functions ---- ---- ---- ---- ---- ----

  final int M_CYCLE = 4;          // Clock cycle for a single-byte operation in the Gameboy
  final int X_MASK = 0b11000000;  // Mask for the X portion of the arguments in the Opcode.
  final int Y_MASK = 0b00111000;  // Mask for the Y portion of the arguments in the Opcode.
  final int Z_MASK = 0b00000111;  // Mask for the Z portion of the arguments in the Opcode.
  final int P_MASK = 0b00110000;  // Mask for the P portion of the arguments in the Opcode.
  final int Q_MASK = 0b00001000;  // Mask for the Q portion of the arguments in the Opcode.

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
    if (opcode == 0xCB) { executeExtendedOpcode(); }

    int x_arg = (opcode & X_MASK) >> 6;
    int y_arg = (opcode & Y_MASK) >> 3;
    int z_arg = opcode & Z_MASK;
    int p_arg = (opcode & P_MASK) >> 5;
    int q_arg = (opcode & Q_MASK) >> 4;

    switch(x_arg) {
      case 0:
        // Opcodes
        break;
      case 1:
        if ((y_arg == 6) && (z_arg == 6)) {
          HALT();
        }
        else {
          LD(r_args[y_arg], r_args[z_arg]);
        }
        return M_CYCLE;
      case 2:
        AluMap.get(alu_args[y_arg]).invoke(r_args[z_arg]);
        return M_CYCLE;
      case 3:
        // Opcodes
        break;
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
  int executeExtendedOpcode() {
    //short opcode = ReadMemory(m_ProgramCounter) ;
    //m_ProgramCounter++;

    short opcode = 0;

    switch(opcode) {
      case 0xB :
        //CPU_RRC(m_RegisterDE.lo) ;
        return M_CYCLE*2;

      case 0x73 :
        //CPU_TEST_BIT( m_RegisterDE.lo, 6 ) ;
        return M_CYCLE*2;

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
    //short opcode = ReadMemory(m_ProgramCounter);
    //m_ProgramCounter++;
    //res = ExecuteOpcode(opcode);
    return res ;
  }

  //#endregion
}
