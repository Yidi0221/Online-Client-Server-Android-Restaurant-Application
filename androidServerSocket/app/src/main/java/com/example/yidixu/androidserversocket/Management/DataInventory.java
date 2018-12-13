package com.example.yidixu.androidserversocket.Management;

import java.io.DataInputStream;



public class DataInventory {

    private int BurgerNum = 0;
    private int ChickenNum = 0;
    private int OnionRingNum = 0;
    private int FrenchFryNum = 0;

    public DataInventory (int burger, int chicken, int onion, int fry){
        BurgerNum = burger;
        ChickenNum = chicken;
        OnionRingNum = onion;
        FrenchFryNum = fry;
    }
    public int getBurgerNum() {
        return BurgerNum;
    }

    public void setBurgerNum(int burgerNum) {
        BurgerNum = burgerNum;
    }

    public int getChickenNum() {
        return ChickenNum;
    }

    public void setChickenNum(int chickenNum) {
        ChickenNum = chickenNum;
    }

    public int getOnionRingNum() {
        return OnionRingNum;
    }

    public void setOnionRingNum(int onionRingNum) {
        OnionRingNum = onionRingNum;
    }

    public int getFrenchFryNum() {
        return FrenchFryNum;
    }

    public void setFrenchFryNum(int frenchFryNum) {
        FrenchFryNum = frenchFryNum;
    }


}
