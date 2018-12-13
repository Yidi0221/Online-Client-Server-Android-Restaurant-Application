package com.example.yidixu.androidserversocket.Management;

import android.os.Handler;
import android.os.Message;

import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

import contacts.ClientOrder;
import contacts.OrderRespond;


public class OrderPackageThread extends Thread {

    private LinkedBlockingDeque<ClientOrder> _Orders;

    private Handler mHandler;
    public OrderPackageThread( Handler handler,LinkedBlockingDeque lbd){

        _Orders = lbd;
        mHandler = handler;
    }
    @Override
    public void run() {
        while (true){
            try{
                ClientOrder cod = _Orders.take();
                System.out.println("Order Id packaging: "+cod.getClientID());
                cod.setServerOrderRespond(OrderRespond.Delievering);

                Message msg = mHandler.obtainMessage();
                msg.obj= cod;
                mHandler.sendMessage(msg);

                Random random = new Random();

                Thread.sleep( random.nextInt(5000));  //20-30sec

                cod.setServerOrderRespond(OrderRespond.Done);
                Message msg2 = mHandler.obtainMessage();
                msg2.obj= cod;
                mHandler.sendMessage(msg2);

            }catch (InterruptedException ex){

            }
        }


    }
}
