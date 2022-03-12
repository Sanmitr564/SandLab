import java.awt.*;

public class SandLab {
    public static void main(String[] args) {
        SandLab lab = new SandLab(120, 80);
        lab.run();
    }

    //add constants for particle types here
    public static final int EMPTY = 0;
    public static final int METAL = 1;
    public static final int FALLING_SAND = 2;
    public static final int WATER = 3;
    public static final int GRASS = 4;

    public static final int SAND = 5;


    public static final Color[] colors = new Color[]{
            new Color(0, 0, 0),
            new Color(192, 192, 192),
            new Color(76, 70, 50),
            new Color(156, 211, 219),
            new Color(0, 154, 23),

            new Color(76, 70, 50),
    };
    //do not add any more fields
    private int[][] grid;
    private SandDisplay display;

    public SandLab(int numRows, int numCols) {
        String[] names;
        names = new String[5];
        names[EMPTY] = "Empty";
        names[METAL] = "Metal";
        names[FALLING_SAND] = "Sand";
        names[WATER] = "Water";
        names[GRASS] = "Grass";
        display = new SandDisplay("Falling Sand", numRows, numCols, names);
        grid = new int[numRows][numCols];
    }

    //called when the user clicks on a location using the given tool
    private void locationClicked(int row, int col, int tool) {
        grid[row][col] = tool;
    }

    //copies each element of grid into the display
    public void updateDisplay() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                display.setColor(row, col, colors[grid[row][col]]);
            }
        }
    }

    //called repeatedly.
    //causes one random particle to maybe do something.
    public void step() {
        int row = (int) (Math.random() * grid.length);
        int col = (int) (Math.random() * grid[row].length);
        if (grid[row][col] == FALLING_SAND) {
            if (row == grid.length - 1 || grid[row + 1][col] == METAL) {
                grid[row][col] = SAND;
            }else if (grid[row + 1][col] == EMPTY) {
                move(row, col, 1, 0, FALLING_SAND, EMPTY);
            } else if (grid[row + 1][col] == WATER) {
                move(row, col, 1, 0, FALLING_SAND, WATER);
            } else if (grid[row + 1][col] == SAND || grid[row+1][col] == GRASS) {
                Runnable[] moves = new Runnable[4];
                int numMoves = 0;
                //moves[0] = () -> move(row, col, 0, 0, SAND, SAND);
                if (col < grid[row].length - 1 && (grid[row + 1][col + 1] == EMPTY || grid[row + 1][col + 1] == WATER)) {
                    moves[numMoves] = () -> move(row, col, 1, 1, FALLING_SAND, grid[row + 1][col + 1]);
                    numMoves++;
                }
                if (col > 0 && (grid[row + 1][col - 1] == EMPTY || grid[row + 1][col - 1] == WATER)) {
                    moves[numMoves] = () -> move(row, col, 1, -1, FALLING_SAND, grid[row + 1][col - 1]);
                    numMoves++;
                }
                if (col > 0 && col < grid[row].length - 1 && Math.random() < 2 / 3f) {
                    if (grid[row + 1][col - 1] == SAND && (grid[row][col - 1] == EMPTY || grid[row][col - 1] == WATER) && grid[row][col + 1] == SAND) {
                        moves[numMoves] = () -> move(row, col, 0, -1, FALLING_SAND, grid[row][col - 1]);
                        numMoves++;
                    }
                    if (grid[row + 1][col + 1] == SAND && (grid[row][col + 1] == EMPTY || grid[row][col + 1] == WATER) && grid[row][col - 1] == SAND) {
                        moves[numMoves] = () -> move(row, col, 0, 1, FALLING_SAND, grid[row][col + 1]);
                        numMoves++;
                    }
                }
                if (Math.random() < 1 / 25f || numMoves == 0) {
                    move(row, col, 0, 0, SAND, SAND);
                } else {
                    moves[(int) (Math.random() * numMoves)].run();
                }
            }
        }

        if (grid[row][col] == SAND) {
            if (row + 1 < grid.length && (grid[row + 1][col] == EMPTY || grid[row + 1][col] == WATER)) {
                move(row, col, 1, 0, FALLING_SAND, grid[row + 1][col]);
            } else {
                boolean isExposedToAir = false;
                if ((row + 1 < grid.length && grid[row + 1][col] == EMPTY)) {
                    isExposedToAir = true;
                } else if (row > 0 && grid[row - 1][col] == EMPTY) {
                    isExposedToAir = true;
                } else if (col + 1 < grid[row].length && grid[row][col + 1] == EMPTY) {
                    isExposedToAir = true;
                } else if (col > 0 && grid[row][col - 1] == EMPTY) {
                    isExposedToAir = true;
                }

                if (isExposedToAir) {
                    for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
                        for (int colOffset = -1; colOffset < 2; colOffset++) {
                            try {
                                if (rowOffset == 0 && colOffset == 0) {
                                    continue;
                                }
                                if (grid[row + rowOffset][col + colOffset] == WATER) {
                                    grid[row + rowOffset][col + colOffset] = EMPTY;
                                    grid[row][col] = GRASS;
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                }

                if (isExposedToAir && grid[row][col] != GRASS && Math.random() < .9) {
                    grid[row][col] = GRASS;
                    if (!hasWaterAccess(row, col)) {
                        grid[row][col] = SAND;
                    }
                }
            }
        }

        if (grid[row][col] == WATER) {
            Runnable[] moves = new Runnable[3];
            int numMoves = 0;
            if (row + 1 < grid.length && grid[row + 1][col] == EMPTY) {
                moves[numMoves] = () -> move(row, col, 1, 0, WATER, EMPTY);
                numMoves++;
            }
            if (col < grid[row].length - 1 && grid[row][col + 1] == EMPTY) {
                moves[numMoves] = () -> move(row, col, 0, 1, WATER, EMPTY);
                numMoves++;
            }
            if (col > 0 && grid[row][col - 1] == EMPTY) {
                moves[numMoves] = () -> move(row, col, 0, -1, WATER, EMPTY);
                numMoves++;
            }
            if (numMoves != 0)
                moves[(int) (Math.random() * numMoves)].run();
        }

    }

    private void move(int row, int col, int rowOffset, int colOffset, int type1, int type2) {
        grid[row][col] = type2;
        grid[row + rowOffset][col + colOffset] = type1;
    }

    private boolean hasWaterAccess(int row, int col) {
        boolean[][] spaces = new boolean[grid.length][grid[0].length];
        return checkAccess(row, col, spaces);
    }

    private boolean checkAccess(int row, int col, boolean[][] spaces) {
        if (grid[row][col] == WATER) {
            return true;
        }
        if ((grid[row][col] != GRASS && grid[row][col] != WATER) || spaces[row][col]) {
            return false;
        }
        spaces[row][col] = true;
        for (int rowOffset = -1; rowOffset < 2; rowOffset++) {
            for (int colOffset = -1; colOffset < 2; colOffset++) {
                if (rowOffset == 0 && colOffset == 0) {
                    continue;
                }
                try {
                    if (grid[row + rowOffset][col + colOffset] == GRASS) {
                        if (checkAccess(row + rowOffset, col + colOffset, spaces)) {
                            return true;
                        }
                    }else if(grid[row+rowOffset][col+colOffset] == WATER){
                        grid[row+rowOffset][col+colOffset] = EMPTY;
                        return true;
                    }
                } catch (Exception e) {
                }
            }
        }
        return false;
    }

    //do not modify
    public void run() {
        while (true) {
            for (int i = 0; i < display.getSpeed(); i++)
                step();
            updateDisplay();
            display.repaint();
            display.pause(1);  //wait for redrawing and for mouse
            int[] mouseLoc = display.getMouseLocation();
            if (mouseLoc != null)  //test if mouse clicked
                locationClicked(mouseLoc[0], mouseLoc[1], display.getTool());
        }
    }
}
