/**
 * Interrupts Class
 */

public class Interrupts {

    /** interrupts controller starting addresses */
    public enum InterruptTypes {
        VBANK(0x0040), LCDC(0x0048), TIMER(0x0050), SERIAL(0x0058), P10_13(0x0060);

        private int handler;

        InterruptTypes(int handler) {
            this.handler = handler;
        }

        public int getHandler() {
            return handler;
        }
    }

    private final boolean gbc;

    private boolean ime; // Interupt Master Enable Flag

    private int interruptFlag = 0xe1;// address where interrupt flag starts

    private int interruptEnabled;// if interrupt is enabled

    private int pendingEnableInterrupts = -1; // if pending any interrupts to enabled.

    private int pendingDisableInterrupts = -1; // if pending any interrupts to disable.

    /**
     * 
     * @param gbc
     */
    public Interrupts(boolean gbc) {
        this.gbc = gbc;
    }

    /**
     * Enable interrupts with a delay
     * 
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
            ime = true;// flag enabled
        }
    }

    /**
     * Disable interrupts with a delay.
     * 
     * @param withDelay
     */
    public void disableInterrupts(boolean withDelay) {
        pendingEnableInterrupts = -1;
        if (withDelay && gbc) {
            if (pendingDisableInterrupts == -1) {
                pendingDisableInterrupts = 1;// sets delay
            }
        } else {
            pendingDisableInterrupts = -1;// no longer pending
            ime = false;// flag disabled
        }
    }

    /**
     * Request interrupts.
     * 
     * @param type
     */
    public void requestInterrupt(InterruptTypes type) {
        interruptFlag = interruptFlag | (1 << type.ordinal());// interrupt or shifts interrupt type address to the left
                                                              // 2^1 bis.
    }

    /**
     * Clear interrupts.
     * 
     * @param type
     */
    public void clearInterrupt(InterruptTypes type) {
        interruptFlag = interruptFlag & ~(1 << type.ordinal()); // clears interrupt and shift interrupt type address to
                                                                // the left 2^1
                                                                // bits.
    }

    /**
     * 
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
    }

    /**
     * Get status of IME.
     * 
     * @return
     */
    public boolean isIme() {
        return ime;
    }

    /**
     * Get if interrupt is requested or not.
     * 
     * @return
     */
    public boolean isInterruptRequested() {
        return (interruptFlag & interruptEnabled) != 0;
    }

    /**
     * Finds any halt bugs in system clock
     * 
     * @return
     */
    public boolean isHaltBug() {
        // returns the interrupt flag enabled at the specified address and is not set to
        // 0 and the ime.
        return (interruptFlag & interruptEnabled & 0x1f) != 0 && !ime;
    }

    /**
     * Accepts specified addresses.
     * 
     * @param address
     * @return
     */
    public boolean accepts(int address) {
        return address == 0xff0f || address == 0xffff;
    }

    /**
     * Set bytes.
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
     * Get bytes.
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