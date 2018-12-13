package com.example.yidixu.androidclientserverexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Chicken extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chicken);

        Button order = (Button) findViewById(R.id.order);

        Button burgerbuttonminus = (Button) findViewById(R.id.burgerbuttonminus);
        Button burgerbuttonplus = (Button) findViewById(R.id.burgerbuttonplus);


        final EditText burgersnumber = (EditText) findViewById(R.id.burgersnumber);

        burgerbuttonminus.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View view) {
                int num = Integer.valueOf(burgersnumber.getText().toString());
                if(num>0){
                    num--;
                    burgersnumber.setText(Integer.toString(num));
                }
            }
        });
        burgerbuttonplus.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View view) {
                int num = Integer.valueOf(burgersnumber.getText().toString());
                num++;
                burgersnumber.setText(Integer.toString(num));
            }
        });

        order.setOnClickListener(new View.OnClickListener()

        {

            @Override
            public void onClick(View view) {

                Intent FrenchfrisIntent = new Intent(Chicken.this, Main3Activity.class);
                startActivity(FrenchfrisIntent);
            }
        });
    }
}
