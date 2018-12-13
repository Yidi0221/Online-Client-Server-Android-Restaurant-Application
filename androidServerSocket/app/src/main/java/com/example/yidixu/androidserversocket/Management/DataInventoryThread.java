package com.example.yidixu.androidserversocket.Management;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by yidixu on 4/10/18.
 */

public class DataInventoryThread implements Runnable{

    private LinkedBlockingDeque<DataInventory> _Inventory;

    public DataInventoryThread (LinkedBlockingDeque<DataInventory> inventory){
        _Inventory = inventory;

    }



                @Override
    public void run() {
        while (true){
            try {
                _Inventory.clear();
                for (int i = 0; i< 50; i ++){
                    // where to read inventory data file and update inventory

                    DataInventory data = new DataInventory(2*i, 3*i, 4*i,5*i);
                    _Inventory.add(data);
                    System.out.println("data inventory added");
                }
                Thread.sleep(3600 * 1000); // One hour update
            } catch (InterruptedException ex){
                ex.printStackTrace();

            }
        }
    }
}
