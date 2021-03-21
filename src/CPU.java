/**
 * CPU class.
 * Has all the registers and opcodes and stuff.
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
  }

//#region ---- ---- ---- ---- ---- Registers ---- ---- ---- ---- ---- ---- ---- ----
  // Code goes here!
  //#endregion

//#region ---- ---- ---- ---- ----  Opcodes  ---- ---- ---- ---- ---- ---- ---- ----

  //#region ---- ---- ---- ---- ---- Load Opcodes

  /**
   * 
   */
  void LD() {

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
  void ADC() {

  }

  /**
   * 
   */
  void ADD() {

  }

  /**
   * 
   */
  void SUB() {

  }

  /**
   * 
   */
  void SBC() {

  }
  
  /**
   * 
   */
  void AND() {

  }

  /**
   * 
   */
  void OR() {

  }

  /**
   * 
   */
  void XOR() {

  }

  /**
   * 
   */
  void CP() {

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

  /**
   * Code from http://www.codeslinger.co.uk/pages/projects/gameboy/opcodes.html
   * @param opcode
   * @return
   */
  public int executeOpcode(short opcode) {
    switch(opcode) {
      case 0x06:
        //CPU_8BIT_LOAD() ;
        return 8;

      case 0x80:
        //CPU_8BIT_ADD() ;
        return 4;

      case 0x90:
        //CPU_8BIT_SUB() ;
        return 4 ;

      case 0xAF:
        //CPU_8BIT_XOR() ;
        return 4;

      case 0x20 :
        //CPU_JUMP_IMMEDIATE() ;
        return 8;

      case 0xCC :
        //CPU_CALL();
      case 0xD0:
        //CPU_RETURN();
        return 8;

      case 0xCB:
        return executeExtendedOpcode();

      default:
        //assert(false);
        return 0; // unhandled opcode
    }
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
        return 8;

      case 0x73 :
        //CPU_TEST_BIT( m_RegisterDE.lo, 6 ) ;
        return 8;

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
