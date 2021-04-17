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

  /**
   * 
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

    AluImMap.put(Alu_t.ADC, () -> ADC_I());
    AluImMap.put(Alu_t.ADD, () -> ADD_I());
    AluImMap.put(Alu_t.AND, () -> AND_I());
    AluImMap.put(Alu_t.CP,  () -> CP_I());
    AluImMap.put(Alu_t.OR,  () -> OR_I());
    AluImMap.put(Alu_t.SBC, () -> SBC_I());
    AluImMap.put(Alu_t.SUB, () -> SUB_I());
    AluImMap.put(Alu_t.XOR, () -> XOR_I());

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
   * @param source
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
   * @param source
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
   * @param source
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
   * @param source
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
   * @param source
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
   * @param source
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
   * @param source
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
   * @param source
   */
  void INC(Reg_8 source) {
    
  }

  /**
   * 
   * @param source
   */
  void INC_16(Reg_16 source) {
    
  }

  /**
   * 
   * @param source
   */
  void DEC(Reg_8 source) {

  }

  /**
   * 
   * @param source
   */
  void DEC_16(Reg_16 source) {

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Misc Opcodes

  /**
   * 
   * @param source
   */
  void SWAP(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
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
   * @param source
   */
  void RLC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);

  }
  
  /**
   * 
   * @param source
   */
  void RL(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);
    
  }
  
  /**
   * 
   * @param source
   */
  void RRC(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);

  }
  
  /**
   * 
   * @param source
   */
  void RR(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);

  }

  /**
   * 
   * @param source
   */
  void SLA(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);

  }
  
  /**
   * 
   * @param source
   */
  void SRA(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);

  }
  
  /**
   * 
   * @param source
   */
  void SRL(Reg_8 source) {
    int value = (source != null) ? regSet.getByte(source) : regSet.getWord(Reg_16.HL);

  }

  //#endregion

  //#region ---- ---- ---- ---- ---- Bit Opcodes

  /**
   * 
   * @param b
   * @param source
   */
  void BIT(int b, Reg_8 source) {

  }

  /**
   * 
   * @param b
   * @param source
   */
  void SET(int b, Reg_8 source) {

  }

  /**
   * 
   * @param b
   * @param source
   */
  void RES(int b, Reg_8 source) {

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
    if (opcode == 0xCB) { return executeExtendedOpcode(); }

    int x_arg = (opcode & X_MASK) >> 6;
    int y_arg = (opcode & Y_MASK) >> 3;
    int z_arg = opcode & Z_MASK;
    int p_arg = (opcode & P_MASK) >> 5;
    int q_arg = (opcode & Q_MASK) >> 4;

    switch(x_arg) {
      case 0:
        switch(z_arg) {
          case 0:
            // NOP
            // LD (nn),SP
            // STOP
            // JR d
            // JR cc[y-4],d
            break;
          case 1:
            // Q = 0, LD rp[p],nn
            // Q = 1, ADD HL, rp[p]
            break;
          case 2:
            // Q = 0, LD (??),A
            // Q = 1, LD A,(??)
            break;
          case 3:
            // Q = 0  INC rp[p]
            // Q = 1  DEC rp[p]
            break;
          case 4:
            INC(r_args[y_arg]);
            break;
          case 5:
            DEC(r_args[y_arg]);
            break;
          case 6:
            // LD r[y],n
            break;
          case 7:
            Z7Map.get(z7_args[y_arg]).invoke();
            break;
        }
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
        switch(z_arg) {
          case 0:
            // RET cc[0],nn
            // RET cc[1],nn
            // RET cc[2],nn
            // RET cc[3],nn
            // LD (0xFF00+n),A
            // ADD SP,d
            // LD A,(0xFF00+n)
            // LD HL,SP+ d
            break;
          case 1:
            // Q = 0  POP rp2[p]
            // Q = 1, 
              // P0 = RET
              // P1 = RETI
              // P2 = JP HL
              // P3 = LD SP, HL
            break;
          case 2:
            // JP cc[0],nn
            // JP cc[1],nn
            // JP cc[2],nn
            // JP cc[3],nn
            // LD (0xFF00+C),A
            // LD (nn),A
            // LD A,(0xFF00+C)
            // LD A,(nn)
            break;
          case 3:
            // y=0 JP
            // CB?
            // n/a, n/a, n/a, n/a
            // DI
            // EI
            break;
          case 4:
            // Call cc[y], nn
            break;
          case 5:
            // Q = 0  PUSH rp2[p]
            // Q = 1, Call nn
            break;
          case 6:
            AluImMap.get(alu_args[y_arg]).invoke();
            break;
          case 7:
            // RST y*8
            break;
        }
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
    short opcode = 0;

    //short opcode = ReadMemory(m_ProgramCounter) ;
    //m_ProgramCounter++;

    int x_arg = (opcode & X_MASK) >> 6;
    int y_arg = (opcode & Y_MASK) >> 3;
    int z_arg = opcode & Z_MASK;

    switch(x_arg) {
      case 0 :
        RotMap.get(rot_args[y_arg]).invoke(r_args[z_arg]);
        return M_CYCLE*2;

      case 1 :
        BIT(y_arg, r_args[z_arg]);
        return M_CYCLE*2;

      case 2:
        RES(y_arg, r_args[z_arg]);
        return M_CYCLE*2;

      case 3:
        SET(y_arg, r_args[z_arg]);
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
