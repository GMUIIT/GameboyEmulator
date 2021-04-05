
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

	public byte[] cartridgeMemory = new byte[0x200000];
	public byte[][][] screenData = new byte[160][144][3];
	public byte[] romData = new byte[0x10000];

	public MemoryMap() {
		for (int i = 0; i < cartridgeMemory.length; i++) { cartridgeMemory[i] = 0; }
		System.out.println("This is a new Memory Map!");
	}

	public void writeMemory(short address, byte data) {
		
	}

	public byte readMemory(short address) {
		return 0;
	}
}
