import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

import javax.swing.*;

class Pixels extends JPanel
{
    private static final int PREF_W = 500;
    private static final int PREF_H = PREF_W;

    int[][] defaultPalette = new int[][] {
        {8, 24, 32},
        {52, 104, 86},
        {136, 192, 112},
        {224, 248, 208}
    };

    int[][] currentpalette = defaultPalette;

    private List<Color> colors = new ArrayList<Color>();
    private List<Rectangle> squares = new ArrayList<Rectangle>();

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
    public void addSquare(int x, int y, int width, int height, int color) {
        Color c = new Color(currentpalette[color][0], currentpalette[color][1], currentpalette[color][2]);
        Rectangle rect = new Rectangle(x, y, width, height);

        squares.add(rect);
        colors.add(c);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREF_W, PREF_H);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        for(int i = 0; i < squares.size(); i++) {
            g2.setColor(colors.get(i));
            g2.fill(squares.get(i));
        }
    }
}


public class LCD extends JFrame {

    Pixels pixels = new Pixels();
    MemoryMap _memoryMap;
    Interrupts _interrupts;

    int lcd_status;
    int total_screen;
    int viewable_screen;

    int scrollX;
    int scrollY;
    int windowX;
    int windowY;
    int controlReg;

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

    public LCD(MemoryMap memoryMap, Interrupts interrupts) {
        super("LCD");

        _memoryMap = memoryMap;
        _interrupts = interrupts;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(pixels);
    }

    public void changePalette(int[][] palette) { pixels.changePalette(palette); }

    public void updateGraphics() {
        //Reading the current values of the scrolling here!
        scrollX = _memoryMap.readMemory(0xFF42);
        scrollY = _memoryMap.readMemory(0xFF43);
        windowY = _memoryMap.readMemory(0xFF4A);
        windowX = _memoryMap.readMemory(0xFF4B) - 7;
        controlReg = _memoryMap.readMemory(0xFF41);

        LCDEnable = (controlReg & 0b10000000) > 0;

        if ((controlReg & 0b0100000) > 0) {
            // Window Tile map display 9C00-9FFF
            // 9C00-9FFF = 1024. 1024 / 128 = 8 rows. Tile IDs
            for (int i = 0x9c00; i <= 0x9FFF; i++) {
                currentTileMap[i - 0x9c00] = _memoryMap.readMemory(i);
            }
        } else {
            // Window Tile map display 9800-9BFF
            // 9800-9BFF = 1024. 1024 / 128 = 8 rows. Tile IDs
            for (int i = 0x9800; i <= 0x9BFF; i++) {
                currentTileMap[i - 0x9800] = _memoryMap.readMemory(i);
            }
        }

        windowDisplay = (controlReg & 0b0010000) > 0;

        if ((controlReg & 0b0001000) > 0) {
            // BG & Window Tile Data Select 8000-8FFF
            // 4096 rows 4096 / 32 = 128 types of tiles
            for (int i = 0x8000; i <= 0x8FFF; i++) {
                currentBGWindow[i - 0x8000] = _memoryMap.readMemory(i);
            }
        } else {
            // BG & Window Tile Data Select 8800-97FF
            // 4096 rows 4096 / 32 = 128 types of tiles
            for (int i = 0x8800; i <= 0x97FF; i++) {
                currentBGWindow[i - 0x8800] = _memoryMap.readMemory(i);
            }
        }

        spriteSize = (controlReg & 0b0000100) > 0;
        spriteEnabled = (controlReg & 0b0000010) > 0;
        bgDisplay = (controlReg & 0b0000001) > 0;

        /*
        taken from http://www.codeslinger.co.uk/pages/projects/gameboy/graphics.html
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

        if (LCDEnable) {
            //Draw background here
            if (bgDisplay) { drawBackgrounds(); }

            //Draw sprites here
            if (spriteEnabled) { drawSprites(); }

            //Draw windows 
            if (windowDisplay) { drawWindows(); }
        }
    }

    /**
     * 
     */
    public int[] tileFormatter(int tileId, boolean bigOrNah)
    {
        /*int startingAddress = tileId * 4;
        int[] tilePixels = new int[64];

        // Each tile stores a 2-bit color.
        // 0
        // TileID = 0
        // 0 + (0)
        // 0 + 2

        for (int x = 0; x < 8; x++) {
            int currentPixel_H = currentBGWindow[startingAddress + (x*2)];
            int currentPixel_L = currentBGWindow[startingAddress + (x*2) + 1];

            for (int n = 0; n < 8; n++) {
                tilePixels[x * n] = (((currentPixel_H & (0x80 >> n)) > 0) ? 1 : 0) + (((currentPixel_L & (0x80 >> n)) > 0) ? 2 : 0);
            }
        }*/

        // 0x8000 - 0x8010 = 16 addresses

        // FF 00 7E FF 85 81 89 83 93 85 A5 8B C9 97 7E FF

        int[][] tilePixels = new int[8][8];
        int[] tilePixels_2 = new int[64];

        for (int i = 0; i < 8; i++)
        {
            int someVal1 = _memoryMap.readMemory(0x9000 + (i*2));
            int someVal2 = _memoryMap.readMemory(0x9000 + (i*2) + 1);
            
            for (int j = 0; j < 8; j++)
            {
                tilePixels[i][j] = (((someVal1 & (0x01 << j)) > 0) ? 0 : 2) + (((someVal2 & (0x01 << j)) > 0) ? 0 : 1);
            }
        }
        
        for (int k = 0; k < 64; k++)
        {
            tilePixels_2[k] = tilePixels[k/8][k%8];
        }

        return tilePixels_2;
    }

    public void drawBackgrounds() {
        
        int[][] tempScreen = new int[256][256];
        int[] currentTile = new int[64];

        // 32 x 32 = 256

        // i == tile position
        // j == tile pixel position

        for (int i = 0; i < currentTileMap.length; i++) // 32 x 32 times
        {
            currentTile = tileFormatter(currentTileMap[i], false);

            for (int j = 0; j < currentTile.length; j++) // 8 x 8 times
            {
                int x = ((j % 8) + ((i * 8) % 32)) % 255;
                int y = ((j / 8) + ((i * 8) / 32)) % 255;
                tempScreen[x][y] = currentTile[j];
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

    public void drawSprites() {
        
        
    }

    public void drawWindows() {


    }

    public void renderGraphics() {
        int height = 5;
        int width = 5;

        //pixels.clear();
        // for (int x = 0; x < screenData.length; x++) {
        //     for (int y = 0; y < screenData[x].length; y++) {
        //         pixels.addSquare(x * height, y * width, width, height, screenData[x][y]);
        //     }
        // }

        for (int i = 0; i < screenData.length; i++) {
            for (int j = 0; j < screenData[i].length; j++) {
                int currentChar = screenData[i][j];

                int x = j * 10;
                int y = i * 10;

                pixels.addSquare(x, y, 10, 10, currentChar);
            }
        }

        repaint();
    }
}