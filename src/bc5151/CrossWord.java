/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bc5151;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author peter
 */
public class CrossWord extends javax.swing.JPanel {

    int width;
    int height;
    SquareItem square[][];
    int number[][];
    char letter[][];
    char autoLetter[][];
    int gridSize = 36;
    boolean mirror = true;
    ArrayList<CWWord> wordList;
    CWSquarePosition selected;
    int[] wordCount;
    int wordMax;
    CWDictionary dict;
    boolean horizontal = true;
    Random rand;
    TransformChar charMap;
    CWState state = CWState.BLACK;

    /**
     * Creates new form CrossWord
     */
    public CrossWord() {
        initComponents();
        rand = new Random();
        charMap = new TransformChar();
        selected = new CWSquarePosition(-1, -1);
        setGridSize(15, 15);
    }

    public void setState(CWState state) {
        this.state = state;
    }

    public void setDict(CWDictionary dict) {
        this.dict = dict;
    }

    public void clearGrid() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                letter[i][j] = '.';
                autoLetter[i][j] = '.';
                square[i][j] = null;
                number[i][j] = 0;
            }
        }
        setNumbers();
    }

    public void setGridSize(int width, int height) {
        wordMax = Math.max(width, height);
        wordCount = new int[wordMax + 1];
        this.width = width;
        this.height = height;
        letter = new char[width][height];
        autoLetter = new char[width][height];
        square = new SquareItem[width][height];
        number = new int[width][height];
        clearGrid();
    }

    public void saveToFile(File name) {

        try {
            FileWriter fileWriter = new FileWriter(name.getAbsolutePath());
            try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                for (int i = 0; i < height; i++) {
                    String str = "";
                    for (int j = 0; j < width; j++) {
                        if (square[j][i] != null) {
                            str += '+';
                        } else {
                            str += Character.toString(letter[j][i]);
                        }
                    }
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                }
                for (CWWord w : wordList) {
                    if (!w.clue.equals("")) {
                        bufferedWriter.write("clue:"
                                + Integer.toString(w.x) + ":"
                                + Integer.toString(w.y) + ":"
                                + (w.horizontal ? "h:" : "v:")
                                + w.clue + "\n");
                    }
                }
            }
        } catch (IOException ex) {
            System.out
                    .println("Error writing to file '" + name.getName() + "'");
        }
    }

    public void readFromFile(File name) {
        try {
            FileReader fileReader = new FileReader(name);
            String total;
            int w;
            int h;
            ArrayList<String> strList = new ArrayList<>();
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                total = "";
                w = 0;
                h = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("clue:")) {
                        strList.add(line);
                    } else if (!line.equals("")) {
                        if (w == 0) {
                            w = line.length();
                        }
                        h++;
                        total += line;
                    }
                }
            }
            width = w;
            height = h;
            setGridSize(w, h);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    char c = total.charAt(i * w + j);
                    if (c == '+') {
                        letter[j][i] = '.';
                        square[j][i] = new BlackSquare(i, j, 1, 1);
                    } else {
                        letter[j][i] = c;
                        square[j][i] = null;
                    }
                    autoLetter[j][i] = '.';
                    number[j][i] = 0;
                }
            }
            setNumbers();
            for (String line : strList) {
                String[] str = line.split("[:]");
                int x = Integer.parseInt(str[1]);
                int y = Integer.parseInt(str[2]);
                for (CWWord wrd : wordList) {
                    if (wrd.x == x && wrd.y == y && wrd.horizontal == str[3].equals("h")) {
                        wrd.clue = str[4];
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + name.getName() + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + name.getName() + "'");
        }
    }

    public void selectSquare() {
        Point p = this.getMousePosition();
        if (p != null) {
            selected.x = p.x / gridSize;
            selected.y = p.y / gridSize;
            if (square[selected.x][selected.y] != null) {
                selected.unset();
                selected.y = -1;
            } else {
                horizontal = p.x % gridSize > p.y % gridSize;
            }
        } else {
            selected.unset();
        }
        this.firePropertyChange("selected", null, selected);
        //return selectedWord();
    }

    public CWWord selectedWord() {
        for (CWWord w : wordList) {
            if (horizontal) {
                if (selected.y == w.y && selected.x >= w.x && selected.x < w.x + w.length
                        && w.horizontal) {
                    return w;
                }
            } else if (selected.x == w.x && selected.y >= w.y && selected.y < w.y + w.length
                    && !w.horizontal) {
                return w;
            }
        }
        return null;
    }

    public String selectedWordString() {
        for (int i = 0; i < wordList.size(); i++) {
            CWWord w = wordList.get(i);
            if (horizontal) {
                if (selected.y == w.y && selected.x >= w.x && selected.x < w.x + w.length
                        && w.horizontal) {
                    return getWordString(i, letter);
                }
            } else if (selected.x == w.x && selected.y >= w.y && selected.y < w.y + w.length
                    && !w.horizontal) {
                return getWordString(i, letter);
            }
        }
        return null;
    }

    public int selectedWordIndex() {
        for (int i = 0; i < wordList.size(); i++) {
            CWWord w = wordList.get(i);
            if (horizontal) {
                if (selected.y == w.y && selected.x >= w.x && selected.x < w.x + w.length
                        && w.horizontal) {
                    return i;
                }
            } else if (selected.x == w.x && selected.y >= w.y && selected.y < w.y + w.length
                    && !w.horizontal) {
                return i;
            }
        }
        return -1;
    }

    public void selectNext() {
        if (horizontal) {
            selected.x++;
        } else {
            selected.y++;
        }
        if (!selected.legalSquare(width, height) || square[selected.x][selected.y] != null) {
            selected.unset();
        }
        this.firePropertyChange("selected", null, selected);
    }

    public void toggleSquare() {
        Point p = this.getMousePosition();
        int x = p.x / gridSize;
        int y = p.y / gridSize;
        toggle(x, y);
        if (selected.x == x && selected.y == y && square[x][y] != null) {
            selected.unset();
        }
        if (mirror && (x != width - x - 1 || y != height - y - 1)) {
            toggle(width - x - 1, height - y - 1);
        }
        setNumbers();
    }

    public void toggleSquare(int x, int y) {
        toggle(x, y);
        if (selected.x == x && selected.y == y && square[x][y] != null) {
            selected.unset();
        }
        if (mirror && (x != width - x - 1 || y != height - y - 1)) {
            toggle(width - x - 1, height - y - 1);
        }
        setNumbers();
    }

    void toggle(int x, int y) {
        if (square[x][y] == null) {
            square[x][y] = new BlackSquare(x, y, 1, 1);
        } else {
            square[x][y] = null;
        }
    }

    public void setLetter(char pos) {
       pos=Character.toUpperCase(charMap.getChar(pos));
//        UndoableEditEvent undoableEditEvent = new UndoableEditEvent(this,
//                new CWUndoLetter(this,letter[selected.x][selected.y],pos,
//                selected.x, selected.y));
//        
        if (selected.legalSquare(width, height) && square[selected.x][selected.y] == null) {
            letter[selected.x][selected.y] = pos;
            selectNext();
        }
    }

    CWWord wordStart(int x, int y, boolean horizontal) {
        CWWord w = null;
        if (horizontal) {
            if (x == 0 || square[x - 1][y] != null) {
                int cross = 0;
                int i;
                for (i = x; i < width && square[i][y] == null; i++) {
                    if (y > 0 && square[i][y - 1] == null) {
                        cross++;
                    } else if (y < height - 1 && square[i][y + 1] == null) {
                        cross++;
                    }
                }
                if (i - x > 1) {
                    w = new CWWord(x, y, i - x, cross, true);
                }
            }
        } else {
            if (y == 0 || square[x][y - 1] != null) {
                int cross = 0;
                int i;
                for (i = y; i < height && square[x][i] == null; i++) {
                    if (x > 0 && square[x - 1][i] == null) {
                        cross++;
                    } else if (x < width - 1 && square[x + 1][i] == null) {
                        cross++;
                    }
                }
                if (i - y > 1) {
                    w = new CWWord(x, y, i - y, cross, false);
                }
            }
        }
        return w;
    }

    void setNumbers() {
        wordList = new ArrayList<>();
        int num = 1;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                CWWord wh = wordStart(j, i, true);
                CWWord wv = wordStart(j, i, false);
                number[j][i] = 0;
                if (wh != null) {
                    wordList.add(wh);
                    number[j][i] = num++;
                }
                if (wv != null) {
                    wordList.add(wv);
                    if (number[j][i] == 0) {
                        number[j][i] = num++;
                    }
                }
            }
        }
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                CWWord w = wordStart(i, j, false);
//                if (w != null) {
//                    int k;
//                    for (k = 0; k < wordList.size(); k++) {
//                        if (wordList.get(k).x + wordList.get(k).y > i + j) {
//                            break;
//                        }
//                    }
//                    wordList.add(k, w);
//                    if (number[i][j] == 0) {
//                        number[i][j] = num++;
//                    }
//                }
//            }
//        }
        //Collections.sort(wordList);
        for (int i = 0; i <= wordMax; i++) {
            wordCount[i] = 0;
        }
        for (CWWord wrd : wordList) {
            wordCount[wrd.length]++;
        }
    }

    public void autoSquare(int min, int max) {
        boolean ok = false;
        int oldMax = 0;
        int oldMin = 1000;
        while (!ok) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            toggleSquare(x, y);
            for (int i = 2; i < min; i++) {
                if (wordCount[i] != 0) {
                    toggleSquare(x, y);
                    break;
                }
            }
            ok = true;
            for (int i = max + 1; i <= wordMax; i++) {
                if (wordCount[i] != 0) {
                    ok = false;
                    break;
                }
            }
            int maxC = 0;
            int minC = 1000;
            for (int i = min; i <= max; i++) {
                maxC = Math.max(wordCount[i], maxC);
                minC = Math.min(wordCount[i], minC);
            }
            if (ok) {
                if (maxC > minC + 3) {
                    ok = false;
                }
                if (oldMin > minC || oldMax < maxC) {
                    toggleSquare(x, y);
                }
            }
            oldMin = minC;
            oldMax = maxC;
            this.paintImmediately(0, 0, width * gridSize + 2 * gridSize, height * gridSize);
        }
        System.out.print("OK");
    }

    public void autoFill() {
        autoLetter = new char[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(letter[i], 0, autoLetter[i], 0, height);
        }
        int w = -1;
        for (int i = 0; i < 6; i++) {
            w = fillWord(0, 1);
            if (w >= 0) {
                break;
            }
        }
        if (w < 0) {
            System.out.print("CANNOT FIND SOLUTION");
        }
    }

    int fillWord(int idx, int tries) {
        int pos = wordList.size();
        if (idx >= pos) {
            return 0;
        }
        if (idx + 1 < pos
                && getDictionaryWord(getWordString(idx + 1, autoLetter)).equals("")) {
            CWWord w = wordList.get(idx + 1);
            wordList.remove(idx + 1);
            wordList.add(idx, w);
            return -1;
        }
        for (int i = idx + 1; i < pos; i++) {
            if (wordList.get(idx).isCrossing(wordList.get(i))) {
                if (i > idx + 1) {
                    CWWord w = wordList.get(i);
                    wordList.remove(i);
                    wordList.add(idx + 1, w);
                }
                break;
            }
        }
        String origStr = getWordString(idx, autoLetter);
        boolean ok = false;
        String newStr;
        int tryit = 0;
        while (!ok) {
            newStr = getDictionaryWord(origStr);
            if (newStr.equals("")) {
                return -1;
            }
            setWordString(idx, newStr, autoLetter);
            if (fillWord(idx + 1, tries) >= 0) {
                ok = true;
            } else {
                tryit++;
                setWordString(idx, origStr, autoLetter);
                if (tryit > tries) {
                    CWWord w = wordList.get(idx + 1);
                    wordList.remove(idx + 1);
                    wordList.add(idx, w);
                    return -1;
                }
            }
            this.paintImmediately(0, 0, width * gridSize, height * gridSize);
        }
        return 0;
    }

    String getWordString(int idx, char[][] grid) {
        String str = "";
        CWWord w = wordList.get(idx);
        if (w.horizontal) {
            for (int x = w.x; x < w.x + w.length; x++) {
                str += Character.toString(grid[x][w.y]);
            }
        } else {
            for (int y = w.y; y < w.y + w.length; y++) {
                str += Character.toString(grid[w.x][y]);
            }
        }
        return str;
    }

    public CWWord getWord(int x, int y, boolean horiz) {
        for (CWWord w : wordList) {
            if (horiz) {
                if (y == w.y && x >= w.x && x < w.x + w.length
                        && w.horizontal) {
                    return w;
                }
            } else if (x == w.x && y >= w.y && y < w.y + w.length
                    && !w.horizontal) {
                return w;
            }
        }
        return null;
    }

    void setWordString(int idx, String str, char[][] grid) {
        CWWord w = wordList.get(idx);
        if (w.horizontal) {
            for (int i = 0; i < w.length; i++) {
                grid[w.x + i][w.y] = str.charAt(i);
            }
        } else {
            for (int i = 0; i < w.length; i++) {
                grid[w.x][w.y + i] = str.charAt(i);
            }
        }
    }

    String getDictionaryWord(String search) {
        String[] list = dict.dictPart[search.length()];
        int stop = rand.nextInt(list.length);
        int start = stop;
        do {
            start++;
            if (start >= list.length) {
                start = 0;
            }
            if (list[start].matches("^" + search + "$")) {
                return list[start];
            }
        } while (start != stop);
        return "";
    }

    Polygon getTriangle(Rectangle rect, boolean horizontal) {
        if (horizontal) {
            int a[] = {rect.x + rect.width / 3, rect.x + rect.width / 3,
                rect.x + rect.width};
            int b[] = {rect.y, rect.y + rect.height, rect.y + rect.height / 2};
            return new Polygon(a, b, 3);
        } else {
            int a[] = {rect.x, rect.x + rect.width, rect.x + rect.width / 2};
            int b[] = {rect.y + rect.height / 3, rect.y + rect.height / 3,
                rect.y + rect.height};
            return new Polygon(a, b, 3);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gridSize = Math.min(this.getWidth() / width, this.getHeight() / height);
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(Color.white);
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());
        drawCanvas(g2, true);
        drawNumberStat(g2);
        g2.dispose();
    }

    public void export(File file) {
        BufferedImage im = new BufferedImage(800, 1500, BufferedImage.TYPE_BYTE_GRAY);
        gridSize = 600 / width;
        Graphics g = im.getGraphics();
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(Color.white);
        g2.clearRect(0, 0, 800, 1500);
        g2.translate(50, 50);
        drawCanvas(g2, false);
        drawClues(g2);
        g2.dispose();

        try {
            ImageIO.write((BufferedImage) im, "png", file);
        } catch (IOException ex) {
            Logger.getLogger(CrossWord.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void drawCanvas(Graphics2D g2, boolean drawLetters) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Rectangle rect = new Rectangle(i * gridSize, j * gridSize,
                        gridSize, gridSize);
                if (drawLetters) {
                    CWWord w1 = getWord(i, j, true);
                    CWWord w2 = getWord(i, j, false);
                    if ((w1 != null && !w1.clue.equals(""))
                            || (w2 != null && !w2.clue.equals(""))) {
                        g2.setColor(Color.CYAN);
                        g2.fill(rect);
                    }
                    Point p = this.getMousePosition();
                    if (p != null && rect.contains(p)) {
                        g2.setColor(Color.pink);
                        g2.fill(rect);
                    }
                    if (i == selected.x && j == selected.y) {
                        g2.setColor(Color.green);
                        g2.fill(getTriangle(rect, horizontal));
                    }
                }
                if (number[i][j] > 0) {
                    g2.setColor(Color.black);
                    g2.setFont(new Font("FreeSans", Font.PLAIN, gridSize / 4));
                    g2.drawString(Integer.toString(number[i][j]), (int) (i
                            * gridSize + gridSize / 16), (int) (j
                            * gridSize + gridSize / 4));
                    if (!drawLetters) {
                        System.out.print(Integer.toString(number[i][j]) + "\n");
                    }
                }
                if (square[i][j] == null) {
                    if (drawLetters) {
                        g2.setColor(Color.black);
                        g2.setFont(new Font("FreeSans", Font.BOLD, gridSize * 3 / 4));
                        if (letter[i][j] != '.') {
                            g2.drawString(Character.toString(letter[i][j]),
                                    (int) (i * gridSize + gridSize / 4), (int) (j
                                    * gridSize + gridSize * 4 / 5));
                        } else if (autoLetter != null && autoLetter[i][j] != '.') {
                            g2.setColor(Color.blue);
                            g2.drawString(Character.toString(autoLetter[i][j]),
                                    (int) (i * gridSize + gridSize / 4), (int) (j
                                    * gridSize + gridSize * 4 / 5));
                        }
                    }
                    g2.setColor(Color.black);
                    g2.draw(rect);
                } else {
                    if (square[i][j].getClass() == BlackSquare.class) {
                        g2.setColor(Color.black);
                        g2.fill(rect);
                    }
                    // if (square[i][j].getClass() == ClueSquare.class) {
                    // g2.setColor(Color.cyan);
                    // g2.fill(rect);
                    // g2.setColor(Color.black);
                    // g2.draw(rect);
                    // }
                }
            }
        }
    }

    private void drawNumberStat(Graphics2D g2) {
        g2.setFont(new Font("FreeSans", Font.PLAIN, gridSize / 3));
        for (int i = 2; i <= wordMax; i++) {
            g2.drawString(
                    Integer.toString(i) + " : "
                    + Integer.toString(wordCount[i]), gridSize / 2
                    + gridSize * width + gridSize / 2, i * gridSize - gridSize + gridSize / 2);
        }
    }

    private void drawClues(Graphics2D g2) {
        g2.setFont(new Font("FreeSans", Font.PLAIN, gridSize / 3));
        int num = 0;
        int pos = gridSize * height + gridSize;
        do {
            pos += gridSize / 2;
            num = drawNextClue(g2, num + 1, true, pos);
        } while (num > 0);
        num = 0;
        do {
            pos += gridSize / 2;
            num = drawNextClue(g2, num + 1, false, pos);
        } while (num > 0);
    }

    private int drawNextClue(Graphics2D g2, int num, boolean horizontal, int pos) {
        int x = 0;
        int y = 0;
        boolean hit = false;
        for (x = 0; x < width; x++) {

            for (y = 0; y < height; y++) {
                if (num == number[x][y]) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                break;
            }
        }
        if (!hit) {
            return 0;
        }
        hit = false;
        for (CWWord w : wordList) {
            if (w.horizontal == horizontal && w.x == x && w.y == y) {
                g2.drawString(
                        Integer.toString(num)
                        + (horizontal ? ". vågrätt: " : ". lodrätt: ")
                        + w.getClue(), 10, pos);
                hit = true;
                break;
            }
        }
        if (!hit) {
            return drawNextClue(g2, num + 1, horizontal, pos);
        }
        return num;
    }

    private boolean isFilled(CWWord word) {
        boolean filled = true;
        if (word == null) {
            return false;
        }
        if (word.horizontal) {
            for (int i = 0; i < word.length; i++) {
                if (letter[word.x + i][word.y] == '.') {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < word.length; i++) {
                if (letter[word.x][word.y + i] == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isFilledWord(int x, int y, boolean horizontal) {
        CWWord word = getWord(x, y, horizontal);
        return isFilled(word);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                formKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        this.requestFocus();
        switch (evt.getButton()) {
            case MouseEvent.BUTTON1:
                switch (state) {
                    case BLACK:
                        toggleSquare();
                        break;
                    case EDIT:
                        selectSquare();
                        break;
                    case NOEDIT:
                        selectSquare();
                        break;
                }
                //selectSquare();
                repaint();
                break;
            case MouseEvent.BUTTON2:
                break;
            case MouseEvent.BUTTON3:
                //toggleSquare();
                //repaint();
                break;
            default:
                break;
        }
    }//GEN-LAST:event_formMouseReleased

    private void formKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyTyped
        if (evt.getKeyChar() == '\n') {
            switch (state) {
                case BLACK:
                    toggleSquare();
                    break;
                case EDIT:
                    selectSquare();
                    break;
                case NOEDIT:
                    selectSquare();
                    break;
            }
        } else if (state == CWState.EDIT) {
            if (evt.getKeyChar() == '+') {
                CWWord w = selectedWord();
                if (w != null) {
                    if (w.horizontal) {
                        for (int i = 0; i < w.length; i++) {
                            letter[w.x + i][w.y] = autoLetter[w.x + i][w.y];
                        }
                    } else {
                        for (int i = 0; i < w.length; i++) {
                            letter[w.x][w.y + i] = autoLetter[w.x][w.y + i];
                        }
                    }
                }
            } else if (evt.getKeyChar() == '-') {
                CWWord w = selectedWord();
                if (w != null) {
                    if (w.horizontal) {
                        for (int i = 0; i < w.length; i++) {
                            if (!isFilledWord(w.x + i, w.y, !w.horizontal)) {
                                letter[w.x + i][w.y] = '.';
                            }
                        }
                    } else {
                        for (int i = 0; i < w.length; i++) {
                            if (!isFilledWord(w.x, w.y + i, !w.horizontal)) {
                                letter[w.x][w.y + i] = '.';
                            }
                        }
                    }
                }
            } else {
                setLetter(evt.getKeyChar());
            }  
        }
        repaint();
    }//GEN-LAST:event_formKeyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
