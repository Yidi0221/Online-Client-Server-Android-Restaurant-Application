package com.example.yidixu.androidclientserverexample;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

import contacts.ClientOrder;
import contacts.OrderCommand;


public class Main3Activity extends AppCompatActivity {

    TextView textInfo,textInfoip,textMsg;
    TextView textChickenPrice, textBurgerPrice,textOnionPrice,textFriesPrice;
    Button buttonConnect, buttonClear;
    ImageButton Frylabel,chickenlabel,burgerlabel,Onionlabel;
    //int orderindex= 1;
    String message =" ";
    ServerSocket serverSocket;

    EditText _EditChicken, _EditBurger,_EditFries,_EditOnion;
    EditText _EditClient;


    HashMap<Integer, ClientOrderEntry> _OrderMap = new  HashMap<Integer, ClientOrderEntry>();

    float _BurgerPrice = 7;
    float _ChikenPrice = 5;
    double _OnionPrice = 3.5;
    double _FriesPrice = 2.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);


        textChickenPrice = (TextView)findViewById(R.id.ChickenPriceText);
        textBurgerPrice =(TextView)findViewById(R.id.BurgerPriceText);
        textOnionPrice = (TextView)findViewById(R.id.OnionPriceText);
        textFriesPrice = (TextView)findViewById(R.id.FryPriceText);

        textBurgerPrice.setText( "$"+Float.toString(_BurgerPrice));
        textChickenPrice.setText( "$"+Float.toString(_ChikenPrice));
        textFriesPrice.setText( "$"+Double.toString(_FriesPrice));
        textOnionPrice.setText( "$"+Double.toString(_OnionPrice));



        _EditBurger= (EditText)findViewById(R.id.BurgerEdit);
        _EditChicken = (EditText)findViewById(R.id.chickenEdit);
        _EditFries = (EditText)findViewById(R.id.FryEdit);
        _EditOnion = (EditText)findViewById(R.id.OnionEdit);
        _EditClient = (EditText)findViewById(R.id.ClientEdit);

        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);


        Frylabel =(ImageButton)findViewById(R.id.Frylabel);
        burgerlabel =(ImageButton)findViewById(R.id.burgerlabel);
        Onionlabel =(ImageButton)findViewById(R.id.Onionlabel);
        chickenlabel =(ImageButton)findViewById(R.id.chickenlabel);

        textInfo= (TextView)findViewById(R.id.info);
        textInfoip=(TextView)findViewById(R.id.infoip);
        textMsg =(TextView)findViewById(R.id.msg);

        textInfoip.setText(getIpAddress());


        buttonConnect.setOnClickListener(buttonConnectOnClickListener);



        buttonClear.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {

                message=" ";
                textMsg.setText(message);
            }});

        Frylabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent FrechFries = new Intent(Main3Activity.this, FrenchFries.class);
                startActivity(FrechFries);
            }
        });
        chickenlabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent chicken = new Intent(Main3Activity.this, Chicken.class);
                startActivity(chicken);

            }
        });
        burgerlabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent burger = new Intent(Main3Activity.this, Burger.class);
                startActivity(burger);

            }
        });
        Onionlabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent Onions = new Intent(Main3Activity.this, OnionRings.class);
                startActivity(Onions);

            }
        });


        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }



    public void AlertUser(final ClientOrder cod){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("ClientID:"+cod.getClientID()+" OrderID:"+cod.getOrderID()+" is partially available");
        alertDialogBuilder.setMessage("Chicken:"+cod.getClientOrderDetail().getChickenNum()+" Burger:"+cod.getClientOrderDetail().getBurgerNum()+" Fries:"+cod.getClientOrderDetail().getFrenchFryNum()+" Onion:"+cod.getClientOrderDetail().getOnionRingNum());
        alertDialogBuilder.setPositiveButton("Continue",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(Main3Activity.this,"Order has continue",Toast.LENGTH_LONG).show();
                        MyClientTask myClientTask = new MyClientTask(
                                _DstAddress,
                                _DstPort,
                                cod);
                        myClientTask.execute();

                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Main3Activity.this,"Order has been canceled",Toast.LENGTH_LONG).show();

            }
        });
        Main3Activity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

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
    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8081;


        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                Main3Activity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textInfo.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                        //+ serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    Socket socket = serverSocket.accept();



//                     DataInputStream dis=new DataInputStream(socket.getInputStream());
//                     String  str=(String)dis.readUTF();
//                     System.out.println("message= "+str);

                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    try {
                        ClientOrder cod = (ClientOrder) ois.readObject();

                        message  +=" ClientID:"+cod.getClientID()+",OrderID:"+cod.getOrderID()+",OrderResponse:"+cod.getServerOrderRespond()+" received"+ "\n";


                        ProcessOrder(cod);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    //To be done generate Client Order

                    // ClientOrder cod = new ClientOrder(count,count+2,count+3,count+4,count+5,count+6);
                    //cod.setCustmerOrderCmd(OrderCommand.Submit);



                    Main3Activity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //  msg.setText(message);
                            textMsg.setText(message);
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
            String msgReply = "Hello from Android, you are #" + cnt;

            try {

                //To be done generate Client Order

                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                //  message += "replayed: " + msgReply + "\n";

                Main3Activity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // msg.setText(message);
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //  message += "Something wrong! " + e.toString() + "\n";
            }

            Main3Activity.this.runOnUiThread(
                    new Runnable() {

                        @Override
                        public void run() {
                            //  msg.setText(message);
                        }
                    });
        }

    }

    private void ProcessOrder(ClientOrder cod){

        switch (cod.getServerOrderRespond()){
            case IfContinue:
                AlertUser(cod);

                break;
            case Done:

                message+= "Total cost:"+cod.getClientOrderDetail().getTotalCost();
                break;

        }

    }

    String _DstAddress = "192.168.200.2";
    int _DstPort=8080;

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    int clientid=  Integer.parseInt(_EditClient.getText().toString());
                    int chickenNum = Integer.parseInt(_EditChicken.getText().toString());
                    int burgerNum = Integer.parseInt(_EditBurger.getText().toString());
                    int fryNum = Integer.parseInt(_EditFries.getText().toString());
                    int onionNum = Integer.parseInt(_EditOnion.getText().toString());


                    if(_OrderMap.containsKey(clientid)){
                        ClientOrderEntry coe = _OrderMap.get(clientid);
                        int index = coe.GetNextOrderIndex();
                        ClientOrder  cod = new ClientOrder(clientid, index, burgerNum,chickenNum,onionNum,fryNum);
                        coe.AddNewOrder(cod);


                        MyClientTask myClientTask = new MyClientTask(
                                _DstAddress,
                                _DstPort,
                                cod);
                        myClientTask.execute();

                    }else{
                        //Create new Client
                        ClientOrderEntry coe = new ClientOrderEntry(clientid);
                        int index = coe.GetNextOrderIndex();
                        ClientOrder  cod = new ClientOrder(clientid, index, burgerNum,chickenNum,onionNum,fryNum);

                        coe.AddNewOrder(cod);
                        _OrderMap.put(clientid,coe);
                        MyClientTask myClientTask = new MyClientTask(
                                _DstAddress,
                                _DstPort,
                                cod);
                        myClientTask.execute();
                    }



                }};

    public class ClientOrderEntry{


        int _ClientID;
        int _OrderIndex;



        HashMap<Integer, ClientOrder> _OrderMap;
        public ClientOrderEntry(int clientID){
            _ClientID = clientID;
            _OrderMap = new HashMap<Integer, ClientOrder>();
            _OrderIndex=1;
        }

        public int get_ClientID() {
            return _ClientID;
        }

        public boolean ContainsOrderID(int index){
            return _OrderMap.containsKey(index);

        }

        public ClientOrder GetClientOrderByIndex(int index){
            return _OrderMap.get(index);

        }
        public int GetNextOrderIndex(){
            return _OrderIndex;

        }

        public void AddNewOrder(ClientOrder cod){

            _OrderMap.put(cod.getOrderID(),cod);
            _OrderIndex++;
        }
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        ClientOrder _Cod;

        MyClientTask(String addr, int port,ClientOrder cod){
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

                /// ClientOrder cod = new ClientOrder(orderindex,orderindex,orderindex,orderindex,orderindex,orderindex);
                //cod.setCustmerOrderCmd(OrderCommand.Submit);

                _Cod.setCustmerOrderCmd(OrderCommand.Submit);
                ObjectOutputStream dout= new ObjectOutputStream(socket.getOutputStream());
                ///orderindex++;
                //dout.writeObject(cod);
                dout.writeObject(_Cod);
                dout.flush();
                message += "ClientID:"+_Cod.getClientID()+",OrderID:"+_Cod.getOrderID()+",OrderCmd:"+_Cod.getCustmerOrderCmd()+" sent"+ "\n";

//                ByteArrayOutputStream byteArrayOutputStream =
//                        new ByteArrayOutputStream(1024);
//                byte[] buffer = new byte[1024];

//                int bytesRead;
//                InputStream inputStream = socket.getInputStream();

                /*
                 * notice:
                 * inputStream.read() will block if no data return
                 */
//                while ((bytesRead = inputStream.read(buffer)) != -1){
//                    byteArrayOutputStream.write(buffer, 0, bytesRead);
//                    response += byteArrayOutputStream.toString("UTF-8");
//                }

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

            textMsg.setText(message);
            super.onPostExecute(result);
        }

    }
}