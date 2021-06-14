import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

import javax.swing.*;

import java.io.*;
import java.util.ArrayList;
import java.lang.*;

//Courtesy of my boi Stack overflow :)
class Squares extends JPanel
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

public class TileMapper extends JFrame {

    Squares squares = new Squares();

    public TileMapper() {
        super("Tile Mapper");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(squares);
    }

    /**
     * 
     * @param palette
     */
    public void changePalette(int[][] palette) {
        squares.changePalette(palette);
    }

    /**
     * 
     * @param TileCode
     */
    public void paintTile(String TileCode) {
        int w = 10;
        int h = 10;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char currentChar = TileCode.charAt(i*8 + j);

                int x = i*10;
                int y = j*10;

                squares.addSquare(x, y, w, h, (int)currentChar - 48);
            }
        }
    }

    /**
     * 
     * @param hex
     * @param arg
     * @return
     */
    public static String hexToBin(String hex,boolean arg) {
        hex = hex.replaceAll("0", "0000");
        hex = hex.replaceAll("1", "0001");
        hex = hex.replaceAll("2", "0010");
        hex = hex.replaceAll("3", "0011");
        hex = hex.replaceAll("4", "0100");
        hex = hex.replaceAll("5", "0101");
        hex = hex.replaceAll("6", "0110");
        hex = hex.replaceAll("7", "0111");
        hex = hex.replaceAll("8", "1000");
        hex = hex.replaceAll("9", "1001");
        hex = hex.replaceAll("a", "1010");
        hex = hex.replaceAll("b", "1011");
        hex = hex.replaceAll("c", "1100");
        hex = hex.replaceAll("d", "1101");
        hex = hex.replaceAll("e", "1110");
        hex = hex.replaceAll("f", "1111");

        if (arg) {
            hex = hex.replaceAll("1","2");
        }

        return hex;
    }

    /**
     * 
     * @param l
     * @return
     */
    public static String addUp(String l) {
        String a = "";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                a += (Character.getNumericValue(l.charAt(i*8 + j)) + Character.getNumericValue(l.charAt(i*8+j+8)));
            }
        }
        return a;
    }

    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public static String TileMaker(ArrayList<String> x,int y) {
        String ans = "";
        boolean z;
        String answer;

        for (int i = 0; i < 16; i++) {
            z = (i % 2 == 0);
            ans += hexToBin(x.get(y*16 + i),z);
        }

        answer = addUp(ans);
        return answer;
    }

    public static void main(String[] args) {

        String inputFile = "Tetris (Japan) (En).gb";

        ArrayList<String> mem = new ArrayList<String>();

        try (InputStream inputStream = new FileInputStream(inputFile);) {

            // Bit is used here to read the individual byte that is being read. Yes 1 at a time although this could
            // Probably be readuced down to a faster speed
            int bit;
            String temp = "";
            int log = 0;

            // String temp is used to grab those bytes and collect them into a single string
            while ((bit = inputStream.read()) != -1) {

                // If the bit is below a certain number it messes with the amount of 0 and 1s in the sequence so I need to make sure to add them in later.
                if (bit < 16) {
                    temp += "0";
                }

                // This is the clearing and keeping track of the bits one after the other and group them
                // Control words for Gameboys are 8 bytes long and are used for everything so it's important to keep them
                // In this grouping to read instructions and much more
                temp += Integer.toHexString(bit);
                log += 1;

                // Adding the memory 8 bytes at a time
                if (log == 1) {
                    mem.add(temp);
                    temp = "";
                    log = 0;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Change to 884 when testing is done
        for (int j = 500; j < 505; j++)
            System.out.println(TileMaker(mem,j));

        String OneTile = TileMaker(mem,500);

        int[][] warm_palette =  new int[][] {
            {124,63,88},
            {235,107,111},
            {249,168,117},
            {255,246,211}
        };
        
        int[][] cool_palette =  new int[][] {
            {98,46,76},
            {117, 80, 232},
            {96, 143, 207},
            {139, 229, 255}
        };

        System.out.println("Before JFrame");
        TileMapper Til = new TileMapper();

        Til.changePalette(cool_palette);

        Til.paintTile(OneTile);

        Til.pack();
        Til.setVisible(true);

        System.out.println("After JFrame");
    }
}
