/**
 * Memory Map:
	0000-3FFF 16KB ROM Bank 00 (in cartridge, fixed at bank 00)
	4000-7FFF 16KB ROM Bank 01..NN (in cartridge, switchable bank number)
	8000-9FFF 8KB Video RAM (VRAM) (switchable bank 0-1 in CGB Mode)
	A000-BFFF 8KB External RAM (in cartridge, switchable bank, if any)
	C000-CFFF 4KB Work RAM Bank 0 (WRAM)
	D000-DFFF 4KB Work RAM Bank 1 (WRAM) (switchable bank 1-7 in CGB Mode)
	E000-FDFF Same as C000-DDFF (ECHO) (typically not used)
	FE00-FE9F Sprite Attribute Table (OAM)
	FEA0-FEFF Not Usable
	FF00-FF7F I/O Ports
	FF80-FFFE High RAM (HRAM)
	FFFF Interrupt Enable Register
 */
public class MemoryMap {

	// public byte[] cartridgeMemory = new byte[0x200000]; useless until other cartridge types are implemented
	private char[] romData = new char[0x8000];
	private char[] vRAM = new char[0x2000];
	private char[] sRAM = new char[0x2000];
	private char[] wRAM = new char[0x2000];
	private char[] OAM = new char[0xA0];
	private char[] io = new char[0x4C];
	private char[] hRAM = new char[0x80];

	private RegisterSet _registerSet;

	/**
	 * Constructor for memory map. Uses static method #Program.loadCartridge().
	 */
	public MemoryMap(RegisterSet registerSet, String catridgeName) {
		_registerSet = registerSet;

		byte[] cartridge = Program.loadCartridge(catridgeName);

		for (int i = 0; i < cartridge.length; i++) {
			this.romData[i] = (char)(cartridge[i]  & 0xFF);
		}

		initializeMemory();
		System.out.println("This is a new Memory Map!");
	}

	/**
	 * Constructor for memory map. Uses static method #Program.loadCartridge().
	 */
	public MemoryMap(RegisterSet registerSet) {
		_registerSet = registerSet;

		byte[] cartridge = Program.loadCartridge("Tetris (Japan) (En).gb");

		for (int i = 0; i < cartridge.length; i++) {
			this.romData[i] = (char)(cartridge[i]  & 0xFF);
		}

		initializeMemory();
		System.out.println("This is a new Memory Map!");
	}

	public void initializeMemory() {
		writeMemory(0xFF05, (char) 0x00);
		writeMemory(0xFF06, (char) 0x00);
		writeMemory(0xFF07, (char) 0x00);
		writeMemory(0xFF10, (char) 0x80);
		writeMemory(0xFF11, (char) 0xBF);
		writeMemory(0xFF12, (char) 0xF3);
		writeMemory(0xFF14, (char) 0xBF);
		writeMemory(0xFF16, (char) 0x3F);
		writeMemory(0xFF17, (char) 0x00);
		writeMemory(0xFF19, (char) 0xBF);
		writeMemory(0xFF1A, (char) 0x7f);
		writeMemory(0xFF1B, (char) 0xFF);
		writeMemory(0xFF1C, (char) 0x9F);
		writeMemory(0xFF1E, (char) 0xBF);
		writeMemory(0xFF20, (char) 0xFF);
		writeMemory(0xFF21, (char) 0x00);
		writeMemory(0xFF22, (char) 0x00);
		writeMemory(0xFF23, (char) 0xBF);
		writeMemory(0xFF24, (char) 0x77);
		writeMemory(0xFF25, (char) 0xF3);
		writeMemory(0xFF26, (char) 0xF1);
		writeMemory(0xFF40, (char) 0x91);
		writeMemory(0xFF42, (char) 0x00);
		writeMemory(0xFF43, (char) 0x00);
		writeMemory(0xFF45, (char) 0x00);
		writeMemory(0xFF47, (char) 0xFC);
		writeMemory(0xFF48, (char) 0xFF);
		writeMemory(0xFF49, (char) 0xFF);
		writeMemory(0xFF4A, (char) 0x00);
		writeMemory(0xFF4B, (char) 0x00);
		writeMemory(0xFFFF, (char) 0x00);
	}

	/**
	 * Retrieves the byte from a space in memory
	 * @param address
	 * @return Unsigned byte from designated space in memory 
	 */
	public char readMemory(int address) {
		try {
			if 		(address < 0x8000) 						return romData[address];
			else if (0x8000 <= address && address < 0xA000)	return vRAM[address - 0x8000];
			else if (0xA000 <= address && address < 0xC000) return sRAM[address - 0xA000];
			else if (0xC000 <= address && address < 0xE000) return wRAM[address - 0xC000];
			else if (0xE000 <= address && address < 0xFE00) return wRAM[address - 0xE000];
			else if (0xFE00 <= address && address < 0xFEA0) return OAM[address - 0xFE00];
			else if (0xFF00 <= address && address < 0xFF4C) return io[address - 0xFF00];
			else if (0xFF80 <= address && address <= 0xFFFF) return hRAM[address - 0xFF80];
			else throw new IndexOutOfBoundsException(); 
		}
		catch (IndexOutOfBoundsException e) {
			System.out.println("Invalid memory read at " + String.format("%04x", address));
			return 0;
		}
	}

	/**
	 * Writes a char (unsigned byte) to memory
	 * @param address
	 * @param data
	 */
	public void writeMemory(int address, char data) {
		try {
			if (address < 0x8000) throw new IndexOutOfBoundsException();
			else if (0x8000 <= address && address < 0xA000)	vRAM[address - 0x8000] = data;
			else if (0xA000 <= address && address < 0xC000) sRAM[address - 0xA000] = data;
			else if (0xC000 <= address && address < 0xE000) wRAM[address - 0xC000] = data;
			else if (0xE000 <= address && address < 0xFE00) wRAM[address - 0xE000] = data;
			else if (0xFE00 <= address && address < 0xFEA0) OAM[address - 0xFE00] = data;
			else if (0xFF00 <= address && address < 0xFF4C) io[address - 0xFF00] = data;
			else if (0xFF80 <= address && address <= 0xFFFF) hRAM[address - 0xFF80] = data;
			else throw new IndexOutOfBoundsException(); 
		}
		catch (IndexOutOfBoundsException e) {
			// System.out.println("Invalid memory write at " + String.format("%04x", address));
		}
	}

	/**
	 * Pushes a register to the stack.
	 */
	public void pushToStack(Reg_16 source) {
		int value = _registerSet.getWord(source);

		_registerSet.setSP(_registerSet.getSP() - 2);
		writeMemory(_registerSet.getSP(), (char)(value >> 8));
		writeMemory(_registerSet.getSP() + 1, (char)(value & 0xff));
	}

	/**
	 * Pops a short value from the stack.
	 */
	public short popFromStack() {
		short operand = (short)(((readMemory(_registerSet.getSP()) + 1) << 8) + (readMemory(_registerSet.getSP())));
		_registerSet.setSP(_registerSet.getSP() + 2);
		return operand;
	}

	public char[] getVRAM() { return vRAM; }
	public char[] getSRAM() { return sRAM; }
	public char[] getWRAM() { return wRAM; }
	public char[] getOAM() { return OAM; }
	public char[] getIO() { return io; }
	public char[] getHRAM() { return hRAM; }
}
