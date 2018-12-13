package com.example.yidixu.androidserversocket.Management;

import android.os.Handler;

import android.os.Message;


import java.util.Random;

import java.util.concurrent.LinkedBlockingDeque;

import contacts.ClientOrder;
import contacts.OrderRespond;



public class OrderPrepareThread extends Thread {


    private LinkedBlockingDeque<ClientOrder> _PrepareQueue;
    private LinkedBlockingDeque<ClientOrder> _PackageQueue;
    private  Handler mHandler;

    public OrderPrepareThread( Handler handler,LinkedBlockingDeque prepareq, LinkedBlockingDeque<ClientOrder> packageq){

        mHandler = handler;
        _PrepareQueue = prepareq;
        _PackageQueue = packageq;

    }
    @Override
    public void run() {



        while (true){
            try{


                ClientOrder cod = _PrepareQueue.take();


                System.out.println("Order Id Preparing: "+cod.getClientID());
                Random  random = new Random();

                cod.setServerOrderRespond(OrderRespond.Preparing);
                Message msg = mHandler.obtainMessage();
                msg.obj= cod;
                mHandler.sendMessage(msg);

                Thread.sleep( random.nextInt(10000)); //180-300sec

                //Bundle bundle = new Bundle();
                // bundle.putString("msg","order done");
                //  msg.setData(bundle);





                _PackageQueue.add(cod); //pass to package queue
            }catch (InterruptedException ex){

            }
        }


    }
}
