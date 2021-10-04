//import java.util.Timer;

public class Timers {
    int DIV;
    int TIMA;
    int TMA;
    int TAC;
    int cycleDiv;   // is the cpu clock rate divider ripped from TAC
    int cycleCnt;   // counts the number of cycles before the timer updates
    int divCycles;  // counts the number of cycles before DIV updates

    MemoryMap _memoryMap;
    Interrupts _interrupts;

    public Timers(MemoryMap memoryMap, Interrupts interrupts) {
        _memoryMap = memoryMap;
        _interrupts = interrupts;
    }

    public void setClockFrequency() {
        switch(TAC & 0x03) {
            case 0: cycleDiv = 1024; break;
            case 1: cycleDiv = 16; break;
            case 2: cycleDiv = 64; break;
            case 3: cycleDiv = 256; break;
            default: cycleDiv = 1024; break;
        }
    }

    public void updateTimers(int cycles) {
        divCycles += cycles;
        if (divCycles >= 255) {
            divCycles = 0;
            DIV += 1;

            if(DIV >= 255) { DIV = 0; }
        }

        if ((TAC & 0x06) > 0) {
            cycleCnt += 1;
            if (cycleCnt >= cycleDiv) {
                _interrupts.requestInterrupt(Interrupts.InterruptTypes.TIMER);
                cycleCnt = 0;

                TIMA += 1;
                if(TIMA >= 255)  {
                    TIMA = TMA;
                }
            }
        }

        _memoryMap.getIO()[0xFF04 - 0xFF00] = (char)DIV;
        _memoryMap.getIO()[0xFF04 - 0xFF00] = (char)DIV;
        _memoryMap.getIO()[0xFF05 - 0xFF00] = (char)TIMA;
        _memoryMap.getIO()[0xFF06 - 0xFF00] = (char)TMA;
        _memoryMap.getIO()[0xFF07 - 0xFF00] = (char)TAC;
    }
}