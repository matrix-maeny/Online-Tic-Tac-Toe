package com.matrix_maeny.onlinetictactoe;

import java.util.ArrayList;

public class Moves {

    private String firstMove = "";
    private String secondMove = "";
    private String turn = "";
    private int touchCount = -1;
    private String winner = "DRAW";

    private ArrayList<Integer> gameMoves = new ArrayList<>();
//    private int move1,move2,move3, move4,move5,move6,move7,move8,move9;

    public Moves() {

//        move1 = move2 = move3 = move4 = move5 = move6 = move7 = move8 = move9 = -1;

        for (int i = 0; i < 9; i++) {

            gameMoves.add(-1);
        }

    }

    public String getSecondMove() {
        return secondMove;
    }

    public void setSecondMove(String secondMove) {
        this.secondMove = secondMove;
    }

    public void setGameMoves(ArrayList<Integer> gameMoves) {
        this.gameMoves = gameMoves;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public int getTouchCount() {
        return touchCount;
    }

    public void setTouchCount(int touchCount) {
        this.touchCount = touchCount;
    }

    public ArrayList<Integer> getGameMoves() {
        return gameMoves;
    }

    public void setGameMoves(int index, int element) {
        gameMoves.set(index,element);

    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public String getFirstMove() {
        return firstMove;
    }

    public void setFirstMove(String firstMove) {
        this.firstMove = firstMove;
    }

//    public int getMove1() {
//        return move1;
//    }
//
//    public void setMove1(int move1) {
//        this.move1 = move1;
//    }
//
//    public int getMove2() {
//        return move2;
//    }
//
//    public void setMove2(int move2) {
//        this.move2 = move2;
//    }
//
//    public int getMove3() {
//        return move3;
//    }
//
//    public void setMove3(int move3) {
//        this.move3 = move3;
//    }
//
//    public int getMove4() {
//        return move4;
//    }
//
//    public void setMove4(int move4) {
//        this.move4 = move4;
//    }
//
//    public int getMove5() {
//        return move5;
//    }
//
//    public void setMove5(int move5) {
//        this.move5 = move5;
//    }
//
//    public int getMove6() {
//        return move6;
//    }
//
//    public void setMove6(int move6) {
//        this.move6 = move6;
//    }
//
//    public int getMove7() {
//        return move7;
//    }
//
//    public void setMove7(int move7) {
//        this.move7 = move7;
//    }
//
//    public int getMove8() {
//        return move8;
//    }
//
//    public void setMove8(int move8) {
//        this.move8 = move8;
//    }
//
//    public int getMove9() {
//        return move9;
//    }
//
//    public void setMove9(int move9) {
//        this.move9 = move9;
//    }
}
