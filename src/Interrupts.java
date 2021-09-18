/**
 * Interrupts Class
 */

public class Interrupts {

    /** interrupts controller starting addresses */
    public enum InterruptTypes {
        VBANK(0x0040), LCDC(0x0048), TIMER(0x0050), SERIAL(0x0058), P10_13(0x0060);

        private int value;

        InterruptTypes(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final boolean isGameboyColor;

    // Interupt Master Enable Flag
    private boolean interruptMasterEnableFlag;

    // address where interrupt flag starts
    private int interruptFlag = 0xe1;

    // if interrupt is enabled
    private int interruptEnabled;

    // if pending any interrupts to enabled.
    private int pendingEnableInterrupts = -1;

    // if pending any interrupts to disable.
    private int pendingDisableInterrupts = -1;

    /**
     * Constructor for the Interrupts class
     * @param gbc
     */
    public Interrupts(boolean gbc) {
        this.isGameboyColor = gbc;
    }

    /**
     * Enable interrupts with a delay
     * @param withDelay
     */
    public void enableInterrupts(boolean withDelay) {
        pendingDisableInterrupts = -1;
        
        if (withDelay) {
            if (pendingEnableInterrupts == -1) {
                pendingEnableInterrupts = 1;// sets delay
            }
        } else {
            pendingEnableInterrupts = -1;// no longer pending
            interruptMasterEnableFlag = true;// flag enabled
        }
    }

    /**
     * Disable interrupts with a delay.
     * @param withDelay
     */
    public void disableInterrupts(boolean withDelay) {
        pendingEnableInterrupts = -1;
        if (withDelay && isGameboyColor) {
            if (pendingDisableInterrupts == -1) {
                pendingDisableInterrupts = 1;// sets delay
            }
        } else {
            pendingDisableInterrupts = -1;// no longer pending
            interruptMasterEnableFlag = false;// flag disabled
        }
    }

    /**
     * Request interrupts.
     * @param type
     */
    public void requestInterrupt(InterruptTypes type) {
        interruptFlag = interruptFlag | (1 << type.ordinal());// interrupt or shifts interrupt type address to the left
                                                              // 2^1 bis.
    }

    /**
     * Clear interrupts.
     * @param type
     */
    public void clearInterrupt(InterruptTypes type) {
        interruptFlag = interruptFlag & ~(1 << type.ordinal()); // clears interrupt and shift interrupt type address to
                                                                // the left 2^1
                                                                // bits.
    }

    /**
     * Called when opcodes are finished executing.
     */
    public void onInstructionFinished() {
        if (pendingEnableInterrupts != -1) {
            if (pendingEnableInterrupts-- == 0) {
                enableInterrupts(false);
            }
        }
        if (pendingDisableInterrupts != -1) {
            if (pendingDisableInterrupts-- == 0) {
                disableInterrupts(false);
            }
        }

        // Handle the interrupts using the switch case statements.
    }

    /**
     * Get status of IME.
     * @return
     */
    public boolean getIMEStatus() {
        return interruptMasterEnableFlag;
    }

    /**
     * Get if interrupt is requested or not.
     * @return
     */
    public boolean isInterruptRequested() {
        return (interruptFlag & interruptEnabled) != 0;
    }

    /**
     * Accepts specified addresses.
     * 
     * @param address
     * @return
     */
    public boolean isValidAddress(int address) {
        return address == 0xff0f || address == 0xffff;
    }

    /**
     * Called by MemoryMapper to set the interrupt flag and interrupt enabled registers.
     * 
     * (Angel): Could potentially be seperate functions perhaps? ex. setInterruptFlag, setInterruptEnabled?
     * 
     * @param address
     * @param value
     */
    public void setByte(int address, int value) {
        switch (address) {
            case 0xff0f:
                interruptFlag = value | 0xe0;
                break;

            case 0xffff:
                interruptEnabled = value;
                break;
        }
    }

    /**
     * Called by MemoryMapper to get the interrupt flag and interrupt enabled registers values.
     * 
     * (Angel): Could potentially be seperate functions perhaps? ex. getInterruptFlag, getInterruptEnabled?
     * 
     * @param address
     * @return
     */
    public int getByte(int address) {
        switch (address) {
            case 0xff0f:
                return interruptFlag;

            case 0xffff:
                return interruptEnabled;

            default:
                return 0xff;
        }
    }

}