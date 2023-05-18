import battleship.*;

import java.awt.Point;
import java.util.*;


public class PrimeShooters implements BattleShipBot {
    private int gameSize;
    private BattleShip2 battleShip;
    private Random random;
    private int shipSunk;
    private CellState[][] board;
    private Queue<Point> targets;

    private Point lastHit = null;

    private Point secondLastHit = null;

    private int chainLength = 0;

    @Override
    public void initialize(BattleShip2 b) {
        battleShip = b;
        gameSize = b.BOARD_SIZE;

        shipSunk = 0;
        board = new CellState[15][15];

        for (int row = 0; row < gameSize; row++) {
            for (int col = 0; col < gameSize; col++) {

                board[row][col] = CellState.Empty;
            }
        }


        random = new Random(0xAAAAAAAA);   // Needed for random shooter - not required for more systematic approaches
        targets = new LinkedList<>();
    }

    private void targetNeighbours(Point hit) {
        if (secondLastHit != null) {
            targets.clear();

            if (secondLastHit.x == lastHit.x && secondLastHit.y + 1 == lastHit.y && lastHit.y + 1 <= 14 && board[lastHit.x][lastHit.y + 1] == CellState.Empty) {
                targets.add(new Point(lastHit.x, lastHit.y + 1));

            } else if (secondLastHit.x == lastHit.x && secondLastHit.y - 1 == lastHit.y && lastHit.y - 1 >= 0 && board[lastHit.x][lastHit.y - 1] == CellState.Empty) {
                targets.add(new Point(lastHit.x, lastHit.y - 1));

            } else if (secondLastHit.x + 1 == lastHit.x && secondLastHit.y == lastHit.y && lastHit.x + 1 <= 14 && board[lastHit.x + 1][lastHit.y] == CellState.Empty) {
                targets.add(new Point(lastHit.x + 1, lastHit.y));
            } else if (secondLastHit.x - 1 == lastHit.x && secondLastHit.y == lastHit.y && lastHit.x - 1 >= 0 && board[lastHit.x - 1][lastHit.y] == CellState.Empty ) {
                targets.add(new Point(lastHit.x - 1, lastHit.y));
            }
        } else {
            int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            for (int[] direction : directions) {
                int x = hit.x + direction[0];
                int y = hit.y + direction[1];
                if(x >= 0 && x <= 14 && y >= 0 && y <= 14 && board[x][y] == CellState.Empty)
                {
                    targets.add(new Point(x,y));
                }
            }
        }
    }

    private Point optimizeShots() {
        if (!targets.isEmpty()) {
            return targets.remove();
        }

        for (int row = 0; row < gameSize; row++) {
            for (int col = row % 2; col < gameSize; col += 2) {
                if (board[row][col] == CellState.Empty) {
                    return new Point(row, col);
                }
            }
        }

        for (int row = 0; row < gameSize; row++) {
            for (int col = (row + 1) % 2; col < gameSize; col += 2) {
                if (board[row][col] == CellState.Empty) {
                    return new Point(row, col);
                }
            }
        }
        return null;
    }

    @Override
    public void fireShot() {
        Point shot = optimizeShots();

        if (battleShip.shoot(shot)) {
            board[shot.x][shot.y] = CellState.Hit;
            if (battleShip.numberOfShipsSunk() > shipSunk) {
                shipSunk++;
                targets.clear();
                secondLastHit = null;
                lastHit = null;
                chainLength = 0;
            }
            else {
                //update the lastHit which keeps track of the last ship part being hit that does not result in a sinking
                if(lastHit != null){
                    secondLastHit = lastHit;
                    //System.out.println("Second last hit activated! The second to last shot is at " + secondLastHit);
                }
                chainLength += 1;
                lastHit = shot;
                //System.out.println("Partial hit! at " + lastHit);
                targetNeighbours(shot);
            }
        }
        else {
            board[shot.x][shot.y] = CellState.Miss;
            if(secondLastHit!= null){
                //Point point = null;
                if (secondLastHit.x == lastHit.x && secondLastHit.y + 1 == lastHit.y && lastHit.y - chainLength >= 0 && board[lastHit.x][lastHit.y - chainLength] == CellState.Empty) {
                    targets.add(new Point(lastHit.x, lastHit.y - chainLength));
                } else if (secondLastHit.x == lastHit.x && secondLastHit.y - 1 == lastHit.y && lastHit.y + chainLength <= 14 && board[lastHit.x][lastHit.y + chainLength] == CellState.Empty) {
                    targets.add(new Point(lastHit.x, lastHit.y + chainLength));
                } else if (secondLastHit.x + 1 == lastHit.x && secondLastHit.y == lastHit.y && lastHit.x - chainLength >= 0 && board[lastHit.x - chainLength][lastHit.y] == CellState.Empty) {
                    targets.add(new Point(lastHit.x - chainLength, lastHit.y));
                } else if (secondLastHit.x - 1 == lastHit.x && secondLastHit.y == lastHit.y && lastHit.x + chainLength <= 14 && board[lastHit.x + chainLength][lastHit.y] == CellState.Empty) {
                    targets.add(new Point(lastHit.x + chainLength, lastHit.y));

                }
            }
        }
    }

    @Override
    public String getAuthors() {
        return "Nick Milanovic: 000292701\nMarko Vockic: 000350323\nHans Zuriel: 000862028";
    }
}
