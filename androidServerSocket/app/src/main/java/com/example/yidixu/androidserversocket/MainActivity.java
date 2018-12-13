package com.example.yidixu.androidserversocket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.yidixu.androidserversocket.Management.DataInventory;
import com.example.yidixu.androidserversocket.Management.DataInventoryThread;
import com.example.yidixu.androidserversocket.Management.OrderPackageThread;
import com.example.yidixu.androidserversocket.Management.OrderPrepareThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingDeque;

import contacts.ClientOrder;
import contacts.OrderRespond;


public class MainActivity extends AppCompatActivity {

    TextView info, infoip, textMessage;
    String message = "";
    ServerSocket serverSocket;

    LinkedBlockingDeque<ClientOrder> _PreparingQueue;

    LinkedBlockingDeque<ClientOrder> _PackagingQueue;
    LinkedBlockingDeque<DataInventory> _Inventory;

    private Handler mMainHandler;

    float _BurgerPrice = 7;
    float _ChikenPrice =5;
    double _OnionPrice =3.5;
    double _FriesPrice =2.5;


    //create an handler
    private final Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            ClientOrder cod = (ClientOrder)msg.obj;



            SendResponse(cod);

        }};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        textMessage = (TextView) findViewById(R.id.msg);

        infoip.setText(getIpAddress());




        _PreparingQueue = new LinkedBlockingDeque<ClientOrder>();
        _Inventory = new LinkedBlockingDeque<DataInventory>();
        _PackagingQueue = new LinkedBlockingDeque<ClientOrder>();
        Thread inventoryThread = new Thread(new DataInventoryThread(_Inventory));
        inventoryThread.start();

        Thread orderPrepare = new Thread( new OrderPrepareThread(myHandler,_PreparingQueue, _PackagingQueue));
        orderPrepare.start();

        Thread orderPackage = new Thread(new OrderPackageThread(myHandler,_PackagingQueue));
        orderPackage.start();

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;


                    // DataInputStream dis=new DataInputStream(socket.getInputStream());
                    // String  str=(String)dis.readUTF();
                    // System.out.println("message= "+str);

                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    try {
                        ClientOrder cod = (ClientOrder) ois.readObject();
                        System.out.println("received: client id, "+cod.getOrderID());

                        message += "ClientID:"+cod.getClientID()+",OrderID:"+cod.getOrderID()+",OrderCMD:"+cod.getCustmerOrderCmd()+" received"+ "\n";
                        ProcessOrder(cod);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    //To be done generate Client Order

                    // ClientOrder cod = new ClientOrder(count,count+2,count+3,count+4,count+5,count+6);
                    //cod.setCustmerOrderCmd(OrderCommand.Submit);



                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            textMessage.setText(message);
                        }
                    });

//                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
//                            socket, count);
//                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public final void SendResponse(ClientOrder cod){
        //Todo response to user request

        final TextView myTextView =  (TextView) findViewById(R.id.msg);

        if(cod.getServerOrderRespond()== OrderRespond.Done){
           double totalcost = (cod.getClientOrderDetail().getBurgerNum()*_BurgerPrice +cod.getClientOrderDetail().getChickenNum()*_ChikenPrice+cod.getClientOrderDetail().getOnionRingNum()*_OnionPrice+cod.getClientOrderDetail().getFrenchFryNum()*_FriesPrice)*1.3;
            cod.getClientOrderDetail().setTotalCost((float) totalcost);

        }

        message +=" ClientID:"+cod.getClientID()+",OrderID:"+cod.getOrderID()+",OrderResponse:"+cod.getServerOrderRespond()+" sent"+ "\n";

        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                textMessage.setText(message);
            }
        });





        MyServerTask myservertask = new MyServerTask(
                "192.168.200.2",
                8081,cod);
        myservertask.execute();

    }

    private void ProcessOrder(ClientOrder cod){

        switch (cod.getCustmerOrderCmd()){
            case Submit:

                try {
                    DataInventory data =  _Inventory.take();
                    if(cod.getClientOrderDetail().getBurgerNum()<= data.getBurgerNum()
                            & cod.getClientOrderDetail().getChickenNum() <= data.getChickenNum()
                            & cod.getClientOrderDetail().getFrenchFryNum()<= data.getFrenchFryNum()
                            & cod.getClientOrderDetail().getOnionRingNum()<=data.getOnionRingNum()){

                        //All kind of food available, confirm client order


                        ClientOrder tmpcod = new ClientOrder(cod.getClientID(),cod.getOrderID(),cod.getClientOrderDetail().getBurgerNum(),cod.getClientOrderDetail().getChickenNum(),cod.getClientOrderDetail().getOnionRingNum(),cod.getClientOrderDetail().getFrenchFryNum());
                        tmpcod.setServerOrderRespond(OrderRespond.Confirmed);
                        SendResponse(tmpcod); //Notify user

                        _PreparingQueue.add(cod); //Add to preparing queue

                        System.out.println("Order Confirm");

                    }else if(cod.getClientOrderDetail().getBurgerNum()<= data.getBurgerNum()
                            | cod.getClientOrderDetail().getChickenNum() <= data.getChickenNum()
                            | cod.getClientOrderDetail().getFrenchFryNum()<= data.getFrenchFryNum()
                            | cod.getClientOrderDetail().getOnionRingNum()<=data.getOnionRingNum()){
                        //Order Partially available, need customer confirm, update order with available food
                        cod.setServerOrderRespond(OrderRespond.IfContinue);
                        cod.getClientOrderDetail().setBurgerNum( cod.getClientOrderDetail().getBurgerNum()> data.getBurgerNum()? data.getBurgerNum():cod.getClientOrderDetail().getBurgerNum());
                        cod.getClientOrderDetail().setChickenNum( cod.getClientOrderDetail().getChickenNum()> data.getChickenNum()? data.getChickenNum():cod.getClientOrderDetail().getChickenNum());
                        cod.getClientOrderDetail().setFrenchFryNum( cod.getClientOrderDetail().getFrenchFryNum()> data.getFrenchFryNum()? data.getFrenchFryNum():cod.getClientOrderDetail().getFrenchFryNum());
                        cod.getClientOrderDetail().setOnionRingNum( cod.getClientOrderDetail().getOnionRingNum()> data.getOnionRingNum()? data.getOnionRingNum():cod.getClientOrderDetail().getOnionRingNum());

                        SendResponse(cod);//Notify user

                        System.out.println("Order Partially Available.Requires user feedback");

                    }else{
                        //Not available , order gets rejected
                        cod.setServerOrderRespond(OrderRespond.Declined);
                        SendResponse(cod);
                        System.out.println("Order Not Available.Declined");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            case Cancel:
                //Do Nothing
                break;
            case SummitPartially:

                ClientOrder tmpcod = new ClientOrder(cod.getClientID(),cod.getOrderID(),cod.getClientOrderDetail().getBurgerNum(),cod.getClientOrderDetail().getChickenNum(),cod.getClientOrderDetail().getOnionRingNum(),cod.getClientOrderDetail().getFrenchFryNum());
                tmpcod.setServerOrderRespond(OrderRespond.Confirmed);
                SendResponse(tmpcod); //Notify user

                _PreparingQueue.addFirst(cod); //Customer agree to order partially food, move this to first priority
                break;

        }
    }



    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;


        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            //String msgReply = "Hello from Android, you are #" + cnt;

            try {

                //To be done generate Client Order

                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                // printStream.print(msgReply);
                printStream.close();



                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textMessage.setText(message);
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    textMessage.setText(message);
                }
            });
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public class MyServerTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        ClientOrder _Cod;

        MyServerTask(String addr, int port,ClientOrder cod){
            dstAddress = addr;
            dstPort = port;
            _Cod = cod;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                // DataOutputStream dout=new DataOutputStream(socket.getOutputStream());

                // dout.writeUTF("Hello Server");

                ///  dout.flush();


                ObjectOutputStream dout= new ObjectOutputStream(socket.getOutputStream());

                dout.writeObject(_Cod);
                dout.flush();





            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {

                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            /// textResponse.setText(response);
            super.onPostExecute(result);
        }

    }

}

