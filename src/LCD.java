import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.Color;
import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

class Screen extends JPanel
{
    private static final int PREF_ZOOM = 2;

    private static final int PREF_W = 160 * PREF_ZOOM;
    private static final int PREF_H = 144 * PREF_ZOOM;

    int[][] defaultPalette = new int[][] {
        {8, 24, 32},
        {52, 104, 86},
        {136, 192, 112},
        {224, 248, 208}
    };

    int[][] currentpalette = defaultPalette;

    // Image
    private BufferedImage image = new BufferedImage(160, 144, BufferedImage.TYPE_INT_RGB);

    /**
     *
     * @param palette
     */
    public void changePalette(int[][] palette) { currentpalette = palette; }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     */
    public void addSquare(int x, int y, int color) {
        Color c = new Color(currentpalette[3-color][0], currentpalette[3-color][1], currentpalette[3-color][2]);
        image.setRGB(x, y, c.getRGB());
    }

    @Override
    public Dimension getPreferredSize() { return new Dimension(PREF_W, PREF_H); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawScaledImage(image, this, g);
    }

    public void clearScreen() {
        for(int i = 0; i < 23040; i++) {
            image.setRGB(i % 144, i / 160, Color.black.getRGB());
        }
    }

    /**
     * Taken from here:
     * https://www.codejava.net/java-se/graphics/drawing-an-image-with-automatic-scaling
     * @param image
     * @param canvas
     * @param g
     */
    public static void drawScaledImage(Image image, Component canvas, Graphics g) {
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        double imgAspect = (double) imgHeight / imgWidth;

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        double canvasAspect = (double) canvasHeight / canvasWidth;

        int x1 = 0; // top left X position
        int y1 = 0; // top left Y position
        int x2 = 0; // bottom right X position
        int y2 = 0; // bottom right Y position

        if (canvasAspect > imgAspect) {
            y1 = canvasHeight;
            // keep image aspect ratio
            canvasHeight = (int) (canvasWidth * imgAspect);
            y1 = (y1 - canvasHeight) / 2;
        } else {
            x1 = canvasWidth;
            // keep image aspect ratio
            canvasWidth = (int) (canvasHeight / imgAspect);
            x1 = (x1 - canvasWidth) / 2;
        }
        x2 = canvasWidth + x1;
        y2 = canvasHeight + y1;

        g.drawImage(image, x1, y1, x2, y2, 0, 0, imgWidth, imgHeight, null);
    }
}

public class LCD {
    Screen _screen = new Screen();
    MemoryMap _memoryMap;
    Interrupts _interrupts;
    JFrame _jframe;

    enum LCD_Mode {
        HBLANK,
		VBLANK,
		OAM,
		VRAM,
    }

    LCD_Mode lcdMode = LCD_Mode.HBLANK;

    int lcdStatus;
    int totalScreen;
    int viewableScreen;

    int scrollX;
    int scrollY;
    int windowX;
    int windowY;
    int controlReg;

    int currentScanline = 0;

    //int currentBackground[] = ;
    int currentWindow[] = new int[1024];
    int currentTileMap[] = new int[1024];
    int currentBGWindow[] = new int[4096];

    boolean LCDEnable;
    boolean windowDisplay;
    boolean spriteSize;
    boolean spriteEnabled;
    boolean bgDisplay;

    public int[][] screenData = new int[160][144];

    public LCD(JFrame jframe, MemoryMap memoryMap, Interrupts interrupts) {
        // super("GMU IIT Gameboy Emulator");

        _memoryMap = memoryMap;
        _interrupts = interrupts;
        _jframe = jframe;

        _jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _jframe.getContentPane().add(_screen);
    }

    public void changePalette(int[][] palette) { _screen.changePalette(palette); }

    public void updateGraphics(int cycles) {
        /*
            From: http://www.codeslinger.co.uk/pages/projects/gameboy/graphics.html
            Taken from the pandocs:

            Bit 7 - LCD Display Enable (0=Off, 1=On)
            Bit 6 - Window Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
            Bit 5 - Window Display Enable (0=Off, 1=On)
            Bit 4 - BG & Window Tile Data Select (0=8800-97FF, 1=8000-8FFF)
            Bit 3 - BG Tile Map Display Select (0=9800-9BFF, 1=9C00-9FFF)
            Bit 2 - OBJ (Sprite) Size (0=8x8, 1=8x16)
            Bit 1 - OBJ (Sprite) Display Enable (0=Off, 1=On)
            Bit 0 - BG Display (for CGB see below) (0=Off, 1=On)
        */

        //Reading the current values of the scrolling here!
        scrollX = _memoryMap.readMemory(0xFF42);
        scrollY = _memoryMap.readMemory(0xFF43);
        windowY = _memoryMap.readMemory(0xFF4A);
        windowX = _memoryMap.readMemory(0xFF4B) - 7;
        controlReg = _memoryMap.readMemory(0xFF41);

        LCDEnable = (controlReg & 0b10000000) > 0;

        windowDisplay = (controlReg & 0b0010000) > 0;

        spriteSize = (controlReg & 0b0000100) > 0;
        spriteEnabled = (controlReg & 0b0000010) > 0;
        bgDisplay = (controlReg & 0b0000001) > 0;

        // if (LCDEnable) {
            // currentScanline -= cycles;
        // } else {
            // return;
        // }

        // if (currentScanline <= 0)
        // {
            // time to move onto next scanline
            // _memoryMap.writeMemory(0xff44, (char)(_memoryMap.readMemory(0xff44) + 1));
            // char currentline = _memoryMap.readMemory(0xFF44);

            // currentScanline = 456 ;

            // we have entered vertical blank period
            // if (currentline == 144)
                // _interrupts.requestInterrupt(Interrupts.InterruptTypes.VBANK);

            // if gone past scanline 153 reset to 0
            // else if (currentline > 153)
                // _memoryMap.writeMemory(0xff44, (char)0);

            // draw the current scanline
            // else if (currentline < 144)
            // {
                if (bgDisplay) {
                    char[] vram = _memoryMap.getVRAM();
                    getTileIDs(vram);
                    getTileData(vram);

                    drawBackgrounds();
                }
                if (spriteEnabled) { drawSprites(); }
                if (windowDisplay) { drawWindows(); }
            // }
        // }
    }

    private void getTileIDs(char[] vram) {
        if ((controlReg & 0b0100000) > 0) {
            // Window Tile map display 9C00-9FFF
            // 9C00-9FFF = 1024 = 32x32 Tile IDs
            for (int i = 0x9c00; i <= 0x9FFF; i++) {
                currentTileMap[i - 0x9C00] = vram[i - 0x8000]; //_memoryMap.readMemory(i);
            }
        } else {
            // Window Tile map display 9800-9BFF
            // 9800-9BFF = 1024 = 32x32 Tile IDs
            for (int i = 0x9800; i <= 0x9BFF; i++) {
                currentTileMap[i - 0x9800] = vram[i - 0x8000]; //_memoryMap.readMemory(i);
            }
        }
    }

    private void getTileData(char[] vram) {
        if ((controlReg & 0b0001000) > 0) {
            // BG & Window Tile Data Select 8000-8FFF
            // 4096 rows 4096 / 32 = 128 types of tiles
            for (int i = 0x8000; i <= 0x8FFF; i++) {
                currentBGWindow[i - 0x8000] = vram[i - 0x8000]; //_memoryMap.readMemory(i);
            }
        } else {
            // BG & Window Tile Data Select 8800-97FF
            // 4096 rows 4096 / 32 = 128 types of tiles
            for (int i = 0x8800; i <= 0x97FF; i++) {
                currentBGWindow[i - 0x8800] = vram[i - 0x8000]; //_memoryMap.readMemory(i);
            }
        }
    }

    public int[] tileFormatter(int tileId, boolean bigOrNah)
    {
        int startingAddress = tileId*16;
        int[] tilePixels = new int[64];

        // Each tile stores a 2-bit color.
        for (int i = 0; i < 8; i++) {
            int currentPixel_H = currentBGWindow[startingAddress + (i*2)];
            int currentPixel_L = currentBGWindow[startingAddress + (i*2) + 1];

            for (int j = 0; j < 8; j++) {
                int lowerBit = ((currentPixel_H & (0x80 >> j)) > 0) ? 1 : 0;
                int higherBit = ((currentPixel_L & (0x80 >> j)) > 0) ? 2 : 0;
                tilePixels[i + j*8] = lowerBit + higherBit;
            }
        }

        return tilePixels;
    }

    int[][] tempScreen = new int[256][256];
    int[] currentTile = new int[64];

    public void drawBackgrounds()
    {
        for (int i = 0; i < 1024; i++)
        {
            // TODO(Angel): Fix Lag
            currentTile = tileFormatter(currentTileMap[i], false);

            // TODO(Angel): Fix lag
            for (int j = 0; j < 64; j++)
            {
                int x = (j % 8) + 8*(i % 32);
                int y = (j / 8) + 8*(i / 32);

                tempScreen[y][x] = currentTile[j];
            }
        }

        // Would we use scrollX scrollY or would we use WindowX and WindowY?
        for (int i = 0; i < screenData.length; i++) //This would run 160 times
        {
            for(int j = 0; j < screenData[i].length; j++) //This would run 144 times.
            {
                int currentXPixel = (i) % 255;
                int currentYPixel = (j) % 255;
                screenData[i][j] = tempScreen[currentXPixel][currentYPixel];
            }
        }
    }

    int lastTicks = 0;
    int tick = 0;
    public void gpuStep(int cycles) {
	    // tick += cycles - lastTicks;
        // lastTicks = cycles;
        tick += cycles;

        // System.out.println("LCD Mode: " + lcdMode.toString());
        // System.out.println("Ticks: " + tick);

        switch(lcdMode) {
        case HBLANK:
            if (tick >= 204) {
                currentScanline++;

                if (currentScanline >= 143) {
                    _interrupts.requestInterrupt(Interrupts.InterruptTypes.VBANK);
                    lcdMode = LCD_Mode.VBLANK;
                } else {
                    lcdMode = LCD_Mode.OAM;
                }

                tick -= 204;
            }
            break;
        case OAM:
            if(tick >= 80) {
                lcdMode = LCD_Mode.VRAM;
                tick -= 80;
            }
            break;
        case VBLANK:
            if(tick >= 456) {
                currentScanline++;

                if(currentScanline > 153) {
                    currentScanline = 0;
                    lcdMode = LCD_Mode.OAM;
                }

                tick -= 456;
            }
            break;
        case VRAM:
            if(tick >= 172) {
                lcdMode = LCD_Mode.HBLANK;

                updateGraphics(cycles);

                tick -= 172;
            }
            break;
        default:
            break;
        }

        _memoryMap.writeMemory(0xff44, (char)currentScanline);
    }


    public void drawSprites() {
        // TODO(Angel): Implement

    }

    public void drawWindows() {
        // TODO(Angel): Implement

    }

    public void renderGraphics() {
        char[] vram = _memoryMap.getVRAM();
        getTileIDs(vram);
        getTileData(vram);

        drawBackgrounds();

        _screen.clearScreen();
        for (int i = 0; i < screenData.length; i++) {
            for (int j = 0; j < screenData[i].length; j++) {
                _screen.addSquare(i, j, screenData[i][j]);
            }
        }

        _jframe.setVisible(true);
        _screen.repaint();
    }
}