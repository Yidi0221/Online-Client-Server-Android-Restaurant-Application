package com.example.yidixu.androidclientserverexample;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import contacts.ClientOrder;
import contacts.OrderCommand;


public class MainActivity extends AppCompatActivity {

    TextView textResponse,textInfo,textInfoip,textMsg;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear;
    int orderindex= 1;
    String message =" ";
    ServerSocket serverSocket;
    TextView txtSlogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSlogan = (TextView)findViewById(R.id.txtSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Nabila.ttf");
        txtSlogan.setTypeface(face);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar){
            actionBar.hide();
        }
        Button startButton = (Button)findViewById(R.id.start_app);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startAppIntent = new Intent(MainActivity.this, Main3Activity.class);
                startActivity(startAppIntent);
            }
        });




        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        textResponse = (TextView)findViewById(R.id.response);

        textInfo= (TextView)findViewById(R.id.info);
        textInfoip=(TextView)findViewById(R.id.infoip);
        textMsg =(TextView)findViewById(R.id.msg);

        textInfoip.setText(getIpAddress());

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        buttonClear.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                textResponse.setText("");
                message=" ";
                textMsg.setText(message);
            }});

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
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
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textInfo.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                        //+ serverSocket.getLocalPort());
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

                        message  += "ClientID:"+cod.getClientID()+",OrderID:"+cod.getOrderID()+",OrderResponse:"+cod.getServerOrderRespond()+" received"+ "\n";

                        System.out.println("received: client id, "+cod.getOrderID());
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
                            //  msg.setText(message);
                            textMsg.setText(message);
                        }
                    });

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();

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

                  message += "replayed: " + msgReply + "\n";

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // msg.setText(message);
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                  message += "Something wrong! " + e.toString() + "\n";
            }

            MainActivity.this.runOnUiThread(
                    new Runnable() {

                        @Override
                        public void run() {
                            // msg.setText(message);
                        }
                    });
        }

    }

    private void ProcessOrder(ClientOrder cod){}

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    MyClientTask myClientTask = new MyClientTask(
                            editTextAddress.getText().toString(),
                            Integer.parseInt(editTextPort.getText().toString()));
                    myClientTask.execute();
                }};

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        MyClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                // DataOutputStream dout=new DataOutputStream(socket.getOutputStream());

                // dout.writeUTF("Hello Server");

                ///  dout.flush();

                ClientOrder cod = new ClientOrder(orderindex,orderindex,orderindex,orderindex,orderindex,orderindex);
                cod.setCustmerOrderCmd(OrderCommand.Submit);
                ObjectOutputStream dout= new ObjectOutputStream(socket.getOutputStream());
                orderindex++;
                dout.writeObject(cod);
                dout.flush();
                message += "ClientID:"+cod.getClientID()+",OrderID:"+cod.getOrderID()+",OrderCmd:"+cod.getCustmerOrderCmd()+" sent"+ "\n";
                System.out.println("ClientID:"+cod.getClientID()+",OrderID:"+cod.getOrderID()+",OrderCmd:"+cod.getCustmerOrderCmd()+" sent");
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
            textResponse.setText(response);
            textMsg.setText(message);
            super.onPostExecute(result);
        }

    }
}