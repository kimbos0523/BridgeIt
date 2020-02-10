import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


//represents a boolean-valued question over values of Type X, Y, and Z
interface IPred<X, Y, Z> {
  boolean apply(X x, Y y, Z z);
}

//a class that checking the given x and y integer is inside of given cell area
class FindColor implements IPred<Cell, Integer, Integer> {
  // returns true if the given x and y value is inside of given cell area
  public boolean apply(Cell c, Integer x, Integer y) {
    return ((c.x - (Constant.CELL_SIZE / 2)) <= x) 
        && ((c.x + (Constant.CELL_SIZE / 2)) > x )
        && ((c.y - (Constant.CELL_SIZE / 2)) <= y) 
        && ((c.y + (Constant.CELL_SIZE / 2)) > y);
  }
}


// a class for representing Cells in the game board
class Cell {
  int x;
  int y;
  Color color = Color.white;
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  
  
  // constructor for test
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }
  
  // default constructor
  Cell() {
    this.x = 0;
    this.y = 0;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }  
  
  // draw the cell in the game board
  WorldScene draw(WorldScene img) {
    img.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE, 
        "solid", this.color), this.x, this.y);
    return img;
  }
  
  // adds the neighbor cells that are not null and same color to temp arrayList
  ArrayList<Cell> getNeighbor() {
    ArrayList<Cell> temp = new ArrayList<Cell>();
    if (this.left != null && this.color.equals(this.left.color)) {
      temp.add(this.left);
    }
    if (this.top != null && this.color.equals(this.top.color)) {
      temp.add(this.top);
    }
    if (this.right != null && this.color.equals(this.right.color)) {
      temp.add(this.right);
    }
    if (this.bottom != null && this.color.equals(this.bottom.color)) {
      temp.add(this.bottom);
    }
    return temp;
  }
  
  // traverse the cells and determines whether the graph go through the
  // given cell or not
  boolean traverse(Cell end, ArrayList<Cell> alreadySeen) {
    if (this.equals(end)) {
      return true;
    }
    ArrayList<Cell> workList = new ArrayList<Cell>();
    alreadySeen.add(this);
    workList.addAll(this.getNeighbor());
    boolean any = false;
    for (int i = 0; i < workList.size(); i++) {
      if (!alreadySeen.contains(workList.get(i))) {
        any = workList.get(i).traverse(end, alreadySeen) || any;
      }
    }
    return any;
  }  
}


// a class for 'bridge it' game
class BridgeIt extends World {
  // all the cells of the game
  ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
  int boardSize;
  // a data for representing player, true = player1, false = player2
  boolean player = true;

  
  // default constructor
  BridgeIt(int boardSize) {
    this.boardSize = new Utils().checkBoardSize(boardSize);
    this.initBoard();
    this.makeBoard();
    this.makeLink();
  }
 
  // EFFECT: initiates the board by using the given int
  public void initBoard() {
    for (int i = 0; i < boardSize; i++) {
      ArrayList<Cell> col = new ArrayList<Cell>();
      for (int j = 0; j < boardSize; j++) {
        col.add(new Cell());
      }
      board.add(col);
    }
  }
  
  // EFFECT: color the game board and locate cells in the board properly
  public void makeBoard() {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        Utils.getCell(board, i, j).color = Utils.colorBoard(i, j);
        Utils.getCell(board, i, j).x = 
            (Constant.CELL_SIZE / 2) + (Constant.CELL_SIZE * i);
        Utils.getCell(board, i, j).y = 
            (Constant.CELL_SIZE / 2) + + (Constant.CELL_SIZE * j);
      }
    }
  }
  
  // EFFECT: makes link between each cells (there are 9 cases for link)
  void makeLink() {
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.size(); j++) {
        if (i > 0) {
          Utils.linkLeft(board, i, j);
        }
        if (i < board.size() - 1) {
          Utils.linkRight(board, i, j);
        }
        if (j > 0) {
          Utils.linkTop(board, i, j);
        }
        if (j < board.size() - 1) {
          Utils.linkBottom(board, i, j);
        }
      }
    }
  }

  // draws the game state
  public WorldScene makeScene() {
    int gameSize = Constant.CELL_SIZE * boardSize;
    WorldScene result = new WorldScene(gameSize, gameSize);
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.size(); j++) {
        result = board.get(i).get(j).draw(result);        
      }
    }
    return result;
  }
  
  // draws the end game state
  public WorldScene lastScene(String msg) {
    int gameSize = boardSize * Constant.CELL_SIZE;
    if (msg.equals("player1")) {
      WorldScene scene = this.makeScene();
      scene.placeImageXY(new TextImage("Player one has won!", 50, Color.black), 
          gameSize / 2, gameSize / 2);
      return scene;
    }
    else {
      WorldScene scene = this.makeScene();
      scene.placeImageXY(new TextImage("Player two has won!", 50, Color.black), 
          gameSize / 2, gameSize / 2);
      return scene;
    }
  }
  
  // EFFECT: if the player press the right area of game board and the cells color
  //         is white, then the cell's color must be changed to pink or magenta
  //         depending on player
  public void onMouseClicked(Posn pos) {
    if (buttomColor(pos).equals(Color.white)) {
      this.changeColor(pos.x / 50, pos.y / 50, player);
      if (this.checkWin() && this.player) {
        this.endOfWorld("player1");
      }
      else if (this.checkWin() && !this.player) {
        this.endOfWorld("player2");
      }
      player = !player;
    }
  }
  
  // finds the proper color from the given posn
  public Color buttomColor(Posn pos) {
    for (int i = 1; i < boardSize - 1; i++) {
      for (int j = 1; j < boardSize - 1; j++) {
        if (new FindColor().apply(Utils.getCell(board, i, j), pos.x, pos.y)) {
          return Utils.getCell(board, i, j).color;
        }
      }
    }
    return Color.black;
  }
  
  // EFFECT: changes color depending on which player presses the buttom
  public void changeColor(int i, int j, boolean player) {
    if (player) {
      Utils.getCell(board, i, j).color = Color.pink;
    }
    else {
      Utils.getCell(board, i, j).color = Color.magenta;
    }
  }
  
  // checks the player satisfied win condition
  public boolean checkWin() {
    boolean result = false;
    for (int i = 0; i < boardSize / 2; i++) {
      for (int j = 0; j < boardSize / 2; j++) {
        if (this.player) {
          result =  Utils.getCell(board, 0, 2 * i + 1).traverse(
              Utils.getCell(board, boardSize - 1, 2 * j + 1), new ArrayList<Cell>());
        }
        else {
          result = Utils.getCell(board, 2 * i + 1, 0).traverse(
              Utils.getCell(board, 2 * j + 1, boardSize - 1), new ArrayList<Cell>());
        }
        if (result) {
          return true;
        }
      }
    }
    return result; 
  }
}

// a class for constant 
class Constant { 
  static int CELL_SIZE = 50;
}

// a class for Utils
class Utils {
  // check the input boardSize is proper number or not, if proper number, returns
  // itself, if not, throw the error msg
  int checkBoardSize(int boardSize) {
    if (boardSize >= 3 && boardSize % 2 == 1) {
      return boardSize;
    }
    else {
      throw new IllegalArgumentException("Input is not proper number");
    }
  }
  
  // get cells from the multi-dimentional array list of Cell
  static Cell getCell(ArrayList<ArrayList<Cell>> arlist, int i, int j) {
    return arlist.get(i).get(j);
  } 
  
  // color the cell properly
  static Color colorBoard(int i, int j) {
    if (i % 2 == 1 && j % 2 == 0) {
      return Color.magenta;
    }
    else if (i % 2 == 0 && j % 2 == 1) {
      return Color.pink;
    }
    else {
      return Color.white;
    }
  }
  
  //makes a link between left and this cell
  static void linkLeft(ArrayList<ArrayList<Cell>> arlist, int i, int j) {
    Utils.getCell(arlist, i, j).left = Utils.getCell(arlist, i - 1, j);
    Utils.getCell(arlist, i - 1, j).right = Utils.getCell(arlist, i, j);
  }
  
  //makes a link between top and this cell
  static void linkTop(ArrayList<ArrayList<Cell>> arlist, int i, int j) {
    Utils.getCell(arlist, i, j).top = Utils.getCell(arlist, i, j - 1);
    Utils.getCell(arlist, i, j - 1).bottom = Utils.getCell(arlist, i, j);
  }
  
  // makes a link between right and this cell
  static void linkRight(ArrayList<ArrayList<Cell>> arlist, int i, int j) {
    Utils.getCell(arlist, i, j).right = Utils.getCell(arlist, i + 1, j);
    Utils.getCell(arlist, i + 1, j).left = Utils.getCell(arlist, i, j);
  }
  
  // makes a link between bottom and this cell
  static void linkBottom(ArrayList<ArrayList<Cell>> arlist, int i, int j) {
    Utils.getCell(arlist, i, j).bottom = Utils.getCell(arlist, i, j + 1);
    Utils.getCell(arlist, i, j + 1).top = Utils.getCell(arlist, i, j);
  }
}


// a class for testing methods
class ExamplesBridgeIt {
  // Examples of Cell
  Cell c1;
  Cell c2;
  
  // Examples of BridgeItWorld
  BridgeIt testWorld;
  BridgeIt w;
  
  // EFFECT: initialize data
  void initData() {
    this.c1 = new Cell(75, 25, Color.magenta);
    this.c2 = new Cell(25, 75, Color.pink);
    
    this.testWorld = new BridgeIt(3);
    this.w = new BridgeIt(11); 
  }
  
  // EFFECT: initialize data without the board invoking makeLink() method
  //         to test the other methods
  void exceptLink() {
    this.c1 = new Cell(75, 25, Color.magenta);
    this.c2 = new Cell(25, 75, Color.pink);
    
    this.testWorld = new BridgeIt(3);
    this.w = new BridgeIt(11);
    
    // set the board before invoking makeLink() method to test
    testWorld.board = new ArrayList<ArrayList<Cell>>();
    testWorld.initBoard();
    testWorld.makeBoard();
  }
  
  // EFFECT: test class FindColor
  void testFindColor(Tester t) {
    this.initData();
    t.checkExpect(new FindColor().apply(c1, 60, 30), true);
    t.checkExpect(new FindColor().apply(c1, 160, 30), false);
    t.checkExpect(new FindColor().apply(c1, 60, 130), false);
    t.checkExpect(new FindColor().apply(c1, 160, 130), false);
  }
  
  // EFFECT: test draw(WorldScene) method
  void testDraw(Tester t) {
    this.initData();
    WorldScene w1 = new WorldScene(550, 550);
    w1.placeImageXY(
        new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE, 
        "solid", c1.color), c1.x, c1.y);
    t.checkExpect(c1.draw(new WorldScene(550, 550)), w1);
    WorldScene w2 = new WorldScene(550, 550);
    w2.placeImageXY(
        new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE, 
        "solid", c2.color), c2.x, c2.y);
    t.checkExpect(c2.draw(new WorldScene(550, 550)), w2);
  }
  
  // EFFECT: test getNeighbor() method
  void testGetNeighbor(Tester t) {
    this.initData();
    // check the default condition
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).getNeighbor(),
        new ArrayList<Cell>());
    // if user pressed (1, 1) cell in board, the cell changes its color to pink
    // and if user invoked getNeighbor() to (0, 1) cell, it returns (1, 1) cell
    testWorld.onMouseClicked(new Posn(75, 75));
    ArrayList<Cell> result = new ArrayList<Cell>();
    Cell cell = Utils.getCell(testWorld.board, 1, 1);
    result.add(cell);
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).getNeighbor(),
        result); 
  }
  
  // EFFECT: test traverse(Cell, ArrayList<Cell>) method
  void testTraverse(Tester t) {
    // to test traverse method, we used 3 x 3 board which is testWorld
    this.initData();
    Cell end = Utils.getCell(testWorld.board, 2, 1); 
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).traverse(end, 
        new ArrayList<Cell>()), false);
    // if user 1 pressed (1, 1) cell in board, then traverse must return true
    testWorld.onMouseClicked(new Posn(75, 75));
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).traverse(end, 
        new ArrayList<Cell>()), true);
  }
  
  // EFFECT: testing the method using for making proper board
  // Method: initBoard(), makeBoard(), makeLink()
  void testConstructorMethod(Tester t) {
    this.initData();
    // check initial board size of testWorld
    t.checkExpect(testWorld.board.size(), 3);
    t.checkExpect(testWorld.board.get(1).size(), 3);
    // set testWorld board empty and check
    testWorld.board = new ArrayList<ArrayList<Cell>>();
    t.checkExpect(testWorld.board.size(), 0);
    // invoke the method initBoard(), and check the board has proper size
    testWorld.initBoard();
    t.checkExpect(testWorld.board.size(), 3);
    t.checkExpect(testWorld.board.get(2).size(), 3);
    // check cells in board do not have proper x, y, color yet
    t.checkExpect(testWorld.board.get(1).get(0).x, 0);
    t.checkExpect(testWorld.board.get(1).get(0).y, 0);
    t.checkExpect(testWorld.board.get(1).get(0).color, Color.white);
    t.checkExpect(testWorld.board.get(2).get(1).x, 0);
    t.checkExpect(testWorld.board.get(2).get(1).y, 0);
    t.checkExpect(testWorld.board.get(2).get(1).color, Color.white);
    // invoke the method makeBoard() and check cells have proper x, y, color
    testWorld.makeBoard();
    t.checkExpect(testWorld.board.get(1).get(0).x, 75);
    t.checkExpect(testWorld.board.get(1).get(0).y, 25);
    t.checkExpect(testWorld.board.get(1).get(0).color, Color.magenta);
    t.checkExpect(testWorld.board.get(2).get(1).x, 125);
    t.checkExpect(testWorld.board.get(2).get(1).y, 75);
    t.checkExpect(testWorld.board.get(2).get(1).color, Color.pink);
    // check cells in board do not have proper neighbor yet
    t.checkExpect(testWorld.board.get(0).get(0).left, null);
    t.checkExpect(testWorld.board.get(0).get(0).top, null);
    t.checkExpect(testWorld.board.get(0).get(0).right, null);
    t.checkExpect(testWorld.board.get(0).get(0).bottom, null);
    t.checkExpect(testWorld.board.get(1).get(1).left, null);
    t.checkExpect(testWorld.board.get(1).get(1).top, null);
    t.checkExpect(testWorld.board.get(1).get(1).right, null);
    t.checkExpect(testWorld.board.get(1).get(1).bottom, null);
    // invoke the method makeLink() and check cells have proper neighbor
    testWorld.makeLink();
    t.checkExpect(testWorld.board.get(0).get(0).left, null);
    t.checkExpect(testWorld.board.get(0).get(0).top, null);
    t.checkExpect(testWorld.board.get(0).get(0).right, 
        testWorld.board.get(1).get(0));
    t.checkExpect(testWorld.board.get(0).get(0).bottom, 
        testWorld.board.get(0).get(1));
    t.checkExpect(testWorld.board.get(1).get(1).left, 
        testWorld.board.get(0).get(1));
    t.checkExpect(testWorld.board.get(1).get(1).top, 
        testWorld.board.get(1).get(0));
    t.checkExpect(testWorld.board.get(1).get(1).right, 
        testWorld.board.get(2).get(1));
    t.checkExpect(testWorld.board.get(1).get(1).bottom, 
        testWorld.board.get(1).get(2));    
  }
  
  // EFFECT: test makeScene() method
  void testMakeScene(Tester t) {
    this.initData(); 
    // test the board size 3 x 3
    WorldScene w = new WorldScene(150, 150);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 25, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 75, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 125, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 125);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 125);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 125);
    t.checkExpect(testWorld.makeScene(), w);
  }
  
  // EFFECT: test lastScene(String) method
  void testLastScene(Tester t) { 
    this.initData();
    // test initial condition of the game board
    WorldScene w = new WorldScene(150, 150);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 25, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 75, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 125, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 125);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 125);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 125);
    t.checkExpect(testWorld.makeScene(), w);
    // pressed the cell (1, 1), the cell(1, 1) will be changed to pink and 
    // player 1 must be win 
    testWorld.onMouseClicked(new Posn(75, 75));
    WorldScene w2 = new WorldScene(150, 150);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 25);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 25);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 25);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 25, 75);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 75, 75);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 125, 75);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 125);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 125);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 125);
    w2.placeImageXY(new TextImage("Player one has won!", 50, Color.black), 75, 75);
    t.checkExpect(testWorld.lastScene("player1"), w2);
    // make a case for player2 win this game
    this.initData();
    testWorld.player = false;
    testWorld.onMouseClicked(new Posn(75, 75));
    WorldScene w3 = new WorldScene(150, 150);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 25);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 25);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 25);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 25, 75);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 75);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 125, 75);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 125);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 125);
    w3.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 125);
    w3.placeImageXY(new TextImage("Player two has won!", 50, Color.black), 75, 75);
    t.checkExpect(testWorld.lastScene("player2"), w3);   
  }
  
  // EFFECT: test onMouseClicked(Posn) method 
  void testOnMouseClicked(Tester t) {
    this.initData();
    // test initial condition of the game board
    WorldScene w = new WorldScene(150, 150);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 25);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 25, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 75, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 125, 75);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 125);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 125);
    w.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 125);
    t.checkExpect(testWorld.makeScene(), w);
    t.checkExpect(testWorld.player, true);
    // if player one pressed Posn(75, 75) in the game board
    testWorld.onMouseClicked(new Posn(75, 75));
    WorldScene w2 = new WorldScene(150, 150);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 25);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 25);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 25);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 25, 75);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 75, 75);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.pink), 125, 75);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 25, 125);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.magenta), 75, 125);
    w2.placeImageXY(new RectangleImage(Constant.CELL_SIZE, Constant.CELL_SIZE,
        "Solid", Color.white), 125, 125);
    w2.placeImageXY(new TextImage("Player one has won!", 50, Color.black), 75, 75);
    t.checkExpect(testWorld.lastScene("player1"), w2);
    t.checkExpect(testWorld.player, false);
  }
  
  // EFFECT: test buttomColor(Posn) method
  void testButtomColor(Tester t) {
    this.initData();
    t.checkExpect(w.buttomColor(new Posn(25, 25)), Color.black);
    t.checkExpect(w.buttomColor(new Posn(75, 75)), Color.white);
    t.checkExpect(w.buttomColor(new Posn(75, 125)), Color.magenta);
    t.checkExpect(w.buttomColor(new Posn(125, 75)), Color.pink);
  }
  
  // EFFECT: test changeColor(int, int, boolean) method
  void testChangeColor(Tester t) {
    this.initData();
    t.checkExpect(Utils.getCell(testWorld.board, 1, 1).color, Color.white);
    testWorld.changeColor(1, 1, true);
    t.checkExpect(Utils.getCell(testWorld.board, 1, 1).color, Color.pink);
    testWorld.changeColor(1, 1, false);
    t.checkExpect(Utils.getCell(testWorld.board, 1, 1).color, Color.magenta);
  }
  
  // EFFECT: test checkWin() method
  void testCheckWin(Tester t) {
    this.initData();
    t.checkExpect(testWorld.checkWin(), false);
    testWorld.onMouseClicked(new Posn(75, 75));
    testWorld.player = true;
    t.checkExpect(testWorld.checkWin(), true);
  }
  
  // EFFECT: test checkBoardSize(int) method in Utils class
  void testCheckBoardSize(Tester t) {
    t.checkConstructorException(
        new IllegalArgumentException("Input is not proper number"), "BridgeIt", 0);
    t.checkConstructorException(
        new IllegalArgumentException("Input is not proper number"), "BridgeIt", 6);
    t.checkExpect(new Utils().checkBoardSize(3), 3);
    t.checkExpect(new Utils().checkBoardSize(5), 5);
  }
  
  // EFFECT: test getCell(ArrayList<ArrayList<Cell>>, int, int) method
  void testGetCell(Tester t) {
    this.exceptLink();
    t.checkExpect(Utils.getCell(testWorld.board, 0, 0),
        new Cell(25, 25, Color.white));
    t.checkExpect(Utils.getCell(testWorld.board, 1, 0),
        new Cell(75, 25, Color.magenta));
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1),
        new Cell(25, 75, Color.pink));
  }
  
  // EFFECT: test colorBoard(int, int) method
  void testColorBoard(Tester t) {
    t.checkExpect(Utils.colorBoard(0, 0), Color.white);
    t.checkExpect(Utils.colorBoard(1, 0), Color.magenta);
    t.checkExpect(Utils.colorBoard(2, 0), Color.white);
    t.checkExpect(Utils.colorBoard(0, 1), Color.pink);
    t.checkExpect(Utils.colorBoard(1, 1), Color.white);
    t.checkExpect(Utils.colorBoard(2, 1), Color.pink);
    t.checkExpect(Utils.colorBoard(0, 2), Color.white);
    t.checkExpect(Utils.colorBoard(1, 2), Color.magenta);
    t.checkExpect(Utils.colorBoard(2, 2), Color.white);
  }
  
  // EFFECT: test linkLeft(ArrayList<ArrayList<Cell>>, int, int) method
  void testLinkLeft(Tester t) {
    this.exceptLink();
    Utils.linkLeft(testWorld.board, 1, 0);
    t.checkExpect(Utils.getCell(testWorld.board, 1, 0).left,
        Utils.getCell(testWorld.board, 0, 0));
    t.checkExpect(Utils.getCell(testWorld.board, 0, 0).right,
        Utils.getCell(testWorld.board, 1, 0));
    Utils.linkLeft(testWorld.board, 1, 1);
    t.checkExpect(Utils.getCell(testWorld.board, 1, 1).left,
        Utils.getCell(testWorld.board, 0, 1));
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).right,
        Utils.getCell(testWorld.board, 1, 1));
  }
  
  // EFFECT: test linkTop(ArrayList<ArrayList<Cell>>, int, int) method
  void testLinkTop(Tester t) {
    this.exceptLink();
    Utils.linkTop(testWorld.board, 0, 1);
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).top,
        Utils.getCell(testWorld.board, 0, 0));
    t.checkExpect(Utils.getCell(testWorld.board, 0, 0).bottom,
        Utils.getCell(testWorld.board, 0, 1));
    Utils.linkTop(testWorld.board, 1, 1);
    t.checkExpect(Utils.getCell(testWorld.board, 1, 1).top,
        Utils.getCell(testWorld.board, 1, 0));
    t.checkExpect(Utils.getCell(testWorld.board, 1, 0).bottom,
        Utils.getCell(testWorld.board, 1, 1));
  }
  
  // EFFECT: test linkRight(ArrayList<ArrayList<Cell>>, int, int) method
  void testLinkRight(Tester t) {
    this.exceptLink();
    Utils.linkRight(testWorld.board, 0, 0);
    t.checkExpect(Utils.getCell(testWorld.board, 0, 0).right,
        Utils.getCell(testWorld.board, 1, 0));
    t.checkExpect(Utils.getCell(testWorld.board, 1, 0).left,
        Utils.getCell(testWorld.board, 0, 0));
    Utils.linkRight(testWorld.board, 0, 1);
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).right,
        Utils.getCell(testWorld.board, 1, 1));
    t.checkExpect(Utils.getCell(testWorld.board, 1, 1).left,
        Utils.getCell(testWorld.board, 0, 1));
  }
  
  // EFFECT: test linkBottom(ArrayList<ArrayList<Cell>>, int, int) method
  void testLinkBottom(Tester t) {
    this.exceptLink();
    Utils.linkBottom(testWorld.board, 0, 0);
    t.checkExpect(Utils.getCell(testWorld.board, 0, 0).bottom,
        Utils.getCell(testWorld.board, 0, 1));
    t.checkExpect(Utils.getCell(testWorld.board, 0, 1).top,
        Utils.getCell(testWorld.board, 0, 0));
    Utils.linkBottom(testWorld.board, 1, 0);
    t.checkExpect(Utils.getCell(testWorld.board, 1, 0).bottom,
        Utils.getCell(testWorld.board, 1, 1));
    t.checkExpect(Utils.getCell(testWorld.board, 1, 1).top,
        Utils.getCell(testWorld.board, 1, 0));
  }
   
  // EFFECT: run this game
  void testBigBang(Tester t) {
    this.initData();
    w.bigBang(550, 550, 0);
  }
}