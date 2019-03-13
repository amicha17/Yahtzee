//Arthur Micha
import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants 
{

    /* Private instance variables */
    private int numPlayers;
    private String[] playerNames;
    private YahtzeeDisplay display;
    private int[][] scores;
    private boolean[][] filled;

    public void run() 
    {
        /* You may need to change some of this */
        IODialog dialog = getDialog();
        numPlayers = dialog.readInt("Enter number of players");
        playerNames = new String[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            playerNames[i] = dialog.readLine("Enter name for player " + (i+1));
        }
        display = new YahtzeeDisplay(getGCanvas(), playerNames);
        scores = new int[numPlayers][N_CATEGORIES+1];
        filled = new boolean[numPlayers][N_CATEGORIES+1];
        playGame();
    }

    private void playGame() 
    {
        int currentPlayer = 0;
        for (int i=0; i<numPlayers*N_SCORING_CATEGORIES; i++)
        {
            takeTurn(currentPlayer);
            currentPlayer++;
            if (currentPlayer==numPlayers)
            {
                currentPlayer = 0;
            }
        }
        int winner = getWinner();
        display.printMessage(playerNames[winner] + " is the winner");
    }

    private int getWinner()
    {
        int champion = 0;
        for (int i=1; i<numPlayers; i++)
        {
            if (scores[i][TOTAL]>scores[champion][TOTAL])
            {
                champion = i;
            }
        }
        return champion;
    }

    private void takeTurn(int num)
    {
        display.printMessage(playerNames[num] + "'s turn. Click \"Roll dice \" button to roll the dice.");
        display.waitForPlayerToClickRoll(num+1);
        int[] dice = getDice();
        display.displayDice(dice);
        for (int i=0; i<2; i++)
        {
            handleOneReroll(dice);
        }
        display.printMessage("Select a category for this roll.");
        int category = display.waitForPlayerToSelectCategory();
        while (filled[num][category]==true)
        {
            category = display.waitForPlayerToSelectCategory();
        }
        int points = getPoints(category, dice);
        display.updateScorecard(category, num+1, points);
        filled[num][category]=true;
        scores[num][TOTAL] += points;
        display.updateScorecard(TOTAL, num+1, scores[num][TOTAL]);
        int topPoints = getTopPoints(dice, category);
        scores[num][UPPER_SCORE] += topPoints;
        display.updateScorecard(UPPER_SCORE, num+1, scores[num][UPPER_SCORE]);
        filled[num][category]=true;
        int lowerPoints = getLowerPoints(category, dice);
        scores[num][LOWER_SCORE] += lowerPoints;
        display.updateScorecard(LOWER_SCORE, num+1, scores[num][LOWER_SCORE]);
        filled[num][category]=true;
        if (scores[num][UPPER_SCORE]>=63)
        {
            display.updateScorecard(UPPER_BONUS, num+1, 35);
            display.updateScorecard(TOTAL, num+1, 35+scores[num][LOWER_SCORE]+scores[num][UPPER_SCORE]);
        }
    }

    private int getLowerPoints(int category, int[] dice)
    {
        if (category == THREE_OF_A_KIND || category == FOUR_OF_A_KIND)
        {
            return ofAKind(dice, category);
        }
        if (category == FULL_HOUSE)
        {
            return getFullHousePoints(dice);
        }
        if (category == SMALL_STRAIGHT)
        {
            return getSmallStraightPoints(category, dice);
        }
        if (category == LARGE_STRAIGHT)
        {
            return getLargeStraightPoints(dice);
        }
        if (category == CHANCE)
        {
            return getChancePoints(category, dice);
        }
        if (category == YAHTZEE)
        {
            return getYahtzeePoints(category, dice);
        }
        else
        {
            return 0;
        }
    }

    private int getPoints (int category, int[] dice)
    {
        if (category <= SIXES)
        {
            return getTopPoints(dice, category);
        }
        if (category >= 9 && category <= 15)
        {
            return getLowerPoints(category, dice);
        }
        else
        {
            return 0;
        }
    }

    private int getYahtzeePoints(int category, int[] dice)
    {
        int points = 0;

        for (int i=0; i<dice.length; i++)
        {
            if (dice[i]==dice[i+1] && dice[i+1]==dice[i+2] && dice[i+2]==dice[i+3] && dice[i+3]==dice[i+4])
            {
                points = 50;
            }
        }
        return points;
    }

    private int getChancePoints(int category, int[] dice)
    {
        int points = 0;
        if (category == CHANCE)
        {
            for (int i=0; i<dice.length; i++)
            {
                points+=dice[i];
            }
        }
        return points;
    }

    private int getLargeStraightPoints(int[] dice)
    {
        int[] numOfType = getNumOfType(dice);
        for (int i=1; i<numOfType.length-4; i++)
        {
            if (numOfType[i]==1 && numOfType[i+1]==1 && numOfType[i+2]==1 && numOfType[i+3]==1 && numOfType[i+4]==1)
            {
                return 40;
            }
        }
        return 0;
    }

    private int getSmallStraightPoints(int category, int[] dice)
    {
        int points = 0;
        int[] numOfType = getNumOfType(dice);
        for (int i=0; i<numOfType.length-3; i++)
        {
            if (category == SMALL_STRAIGHT)
            {
                if (numOfType[i]>=1 && numOfType[i+1]>=1 && numOfType[i+2]>=1 && numOfType[i+3]>=1)
                {
                    points = 30;
                }
            }
        }
        return points;
    }

    private int getFullHousePoints(int[] dice)
    {
        int counter1 = 0;
        int counter2 = 0;
        int[] numOfType = getNumOfType(dice);

        for (int i=0; i<numOfType.length; i++)
        {
            if (numOfType[i]==3)
            {
                counter1++;
            }
            if (numOfType[i]==2)
            {
                counter2++;
            }
        }
        if (counter1>0 && counter2>0)
        {
            return 25;
        }
        else
        {
            return 0;
        }
    }

    private int[] getNumOfType (int[] dice)
    {
        int[] numOfType = new int[6];
        for (int i=0; i<dice.length; i++)
        {
            if (dice[i] == 1)
            {
                numOfType[0]++;
            }
            if (dice[i] == 2)
            {
                numOfType[1]++;
            }
            if (dice[i] == 3)
            {
                numOfType[2]++;
            }
            if (dice[i] == 4)
            {
                numOfType[3]++;
            }
            if (dice[i] == 5)
            {
                numOfType[4]++;
            }
            if (dice[i] == 6)
            {
                numOfType[5]++;
            }
        }
        return numOfType;
    }

    private int ofAKind(int[] dice, int category)
    {
        int[] numOfType = getNumOfType(dice);
        int points = 0;
        for (int i=0; i<numOfType.length; i++)
        {
            if (category == THREE_OF_A_KIND)
            {
                if (numOfType[i]>=3)
                {
                    points+=dice[i]*3;
                }
            }
            if (category == FOUR_OF_A_KIND)
            {
                if (numOfType[i]>=4)
                {
                    points+=dice[i]*4;
                }
            }
        }
        return points;
    }

    private int getTopPoints(int[] dice, int category)
    {
        int score = 0;
        for (int i=0; i<dice.length; i++)
        {
            if (dice[i] == category)
            {
                score+=category;
            }
        }
        return score;
    }

    private void handleOneReroll(int[] dice)
    {
        display.printMessage("Select the dice you wish to re-roll and Click\"Roll again\".");
        display.waitForPlayerToSelectDice();
        dice = rerollSomeDice(dice);
        display.displayDice(dice);
    }

    private int[] rerollSomeDice(int[] dice)
    {
        for (int i=0; i<dice.length; i++)
        {
            if (display.isDieSelected(i))
            {
                dice[i] = (int)(Math.random()*6)+1;
            }
        }
        return dice;
    }

    private int[] getDice()
    {
        int[] temp = new int[N_DICE];
        for (int i=0; i<N_DICE; i++)
        {
            temp[i] = (int)(Math.random()*6)+1;
        }
        return temp;
    }

    /* Java main method to ensure that this program starts correctly */
    public static void main(String[] args) 
    {
        new Yahtzee().start(args);
    }

}
