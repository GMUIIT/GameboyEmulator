/**
 * Interrupts Class
 */

public class Interrupts {

    /** interrupts controller starting addresses */
    public enum InterruptTypes {
        VBANK(0x0040), LCDC(0x0048), TIMER(0x0050), SERIAL(0x0058), P10_13(0x0060);

        InterruptTypes(int value) { this.handlerAddress = value; }

        // Address of the Handler in memory.
        private int handlerAddress;
        public int getHandlerAddress() { return handlerAddress; }
    }

    private boolean interruptMasterEnableFlag;
    private int interruptFlag = 0xe1;
    private int interruptEnabled;

    private MemoryMap _memoryMap;
    private RegisterSet _registerSet;

    /**
     * Constructor for the Interrupts class
     * @param gbc
     */
    public Interrupts(MemoryMap memoryMap, RegisterSet registerSet) {
        _memoryMap = memoryMap;
        _registerSet = registerSet;
    }

    public boolean getIMEStatus() { return interruptMasterEnableFlag; }
    public void enableInterrupts()
    {
        // System.out.println("Enabling Interrupts!");
        interruptMasterEnableFlag = true;
    }
    public void disableInterrupts()
    {
        // System.out.println("Disabling Interrupts!");
        interruptMasterEnableFlag = false;
    }

    public void requestInterrupt(InterruptTypes type)
    {
        // System.out.println("Requested Interrupt of type: " + type.toString());
        interruptFlag = _memoryMap.readMemory(0xFF0F);
        interruptFlag |= (1 << type.ordinal());
        _memoryMap.writeMemory(0xFF0F, (char)interruptFlag);
    }

    public void clearInterrupt(InterruptTypes type)
    {
        // System.out.println("Clearing Interrupt of type: " + type.toString());
        interruptFlag = _memoryMap.readMemory(0xFF0F);
        interruptFlag &= ~(1 << type.ordinal());
        _memoryMap.writeMemory(0xFF0F, (char)interruptFlag);
    }

    /**
     * Called when opcodes are finished executing.
     */
    public void doInterrupts() {
        if (interruptMasterEnableFlag) {
            interruptEnabled = _memoryMap.readMemory(0xFFFF);
            interruptFlag = _memoryMap.readMemory(0xFF0F);

            if (testIfInterruptSet(InterruptTypes.VBANK)) { handleInterrupt(InterruptTypes.VBANK); }
                else
            if (testIfInterruptSet(InterruptTypes.LCDC)) { handleInterrupt(InterruptTypes.LCDC); }
                else
            if (testIfInterruptSet(InterruptTypes.TIMER)) { handleInterrupt(InterruptTypes.TIMER); }
                else
            if (testIfInterruptSet(InterruptTypes.SERIAL)) { handleInterrupt(InterruptTypes.SERIAL); }
                else
            if (testIfInterruptSet(InterruptTypes.P10_13)) { handleInterrupt(InterruptTypes.P10_13); }

            _memoryMap.writeMemory(0xFF0F, (char)interruptFlag);
            // _memoryMap.writeMemory(0xFFFF, (char)interruptFlag);
        }
    }

    private void handleInterrupt(InterruptTypes interruptType) {
        // System.out.println("Handling interrupt: " + interruptType.name());
        disableInterrupts();
        clearInterrupt(interruptType);
        _memoryMap.pushToStack(Reg_16.PC);
        _registerSet.setPC(interruptType.getHandlerAddress());

        Program.cycles_count += 12;
    }

    private boolean testIfInterruptSet(InterruptTypes interruptTypes) {
        // boolean isEnabled = (interruptEnabled & (1 << interruptTypes.ordinal())) > 0;
        // boolean isSet = (interruptFlag & (1 << interruptTypes.ordinal())) > 0;

        // System.out.println("i enabled" + interruptEnabled);
        // System.out.println("i set" + interruptFlag);

        // return isEnabled && isSet;

        // if (interruptTypes == InterruptTypes.VBANK) {
        //     System.out.println("IE: " + (int)_memoryMap.readMemory(0xFFFF));
        //     System.out.println("IF: " + (int)_memoryMap.readMemory(0xFF0F));
        //     System.out.println("IT: " + (1 << interruptTypes.ordinal()));
        // }

        return (interruptEnabled & interruptFlag & (1 << interruptTypes.ordinal())) > 0;
    }
}