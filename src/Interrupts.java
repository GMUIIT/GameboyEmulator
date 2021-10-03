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
    public void enableInterrupts() { interruptMasterEnableFlag = true; }
    public void disableInterrupts() { interruptMasterEnableFlag = true; }

    public void requestInterrupt(InterruptTypes type) { interruptFlag |= (1 << type.ordinal()); }
    public void clearInterrupt(InterruptTypes type) { interruptFlag &= ~(1 << type.ordinal()); }

    /**
     * Called when opcodes are finished executing.
     */
    public void doInterrupts() {
        if (interruptMasterEnableFlag) {
            interruptEnabled = _memoryMap.readMemory(0xFFFF);
            interruptFlag = _memoryMap.readMemory(0xFF0F);

            if (testIfInterruptSet(InterruptTypes.VBANK)) {
                handleInterrupt(InterruptTypes.VBANK);
            } else if (testIfInterruptSet(InterruptTypes.LCDC)) {
                handleInterrupt(InterruptTypes.LCDC);
            } else if (testIfInterruptSet(InterruptTypes.TIMER)) {
                handleInterrupt(InterruptTypes.TIMER);
            } else if (testIfInterruptSet(InterruptTypes.SERIAL)) {
                handleInterrupt(InterruptTypes.SERIAL);
            } else if (testIfInterruptSet(InterruptTypes.P10_13)) {
                handleInterrupt(InterruptTypes.P10_13);
            }
        }
    }

    private void handleInterrupt(InterruptTypes interruptType) {
        disableInterrupts();
        clearInterrupt(interruptType);
        _memoryMap.pushToStack(Reg_16.PC);
        _registerSet.setPC(interruptType.getHandlerAddress());
    }

    private boolean testIfInterruptSet(InterruptTypes interruptTypes) {
        boolean isEnabled = (interruptEnabled & (1 << interruptTypes.ordinal())) > 0;
        boolean isSet = (interruptFlag & (1 << interruptTypes.ordinal())) > 0;

        return isEnabled && isSet;
    }
}