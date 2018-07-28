package com.example.jinny.rowing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by jinny on 2018-02-13.
 */

public class MainActivity extends Activity{
    protected void onCreate(Bundle savedIstanceState){
        super.onCreate(savedIstanceState);
        setContentView(R.layout.activity_main);

        Button startinghutButton=(Button)findViewById(R.id.startinghutButton);
        Button timinghut_500Button=(Button)findViewById(R.id.timinghut_500Button);
        Button timinghut_1000Button=(Button)findViewById(R.id.timinghut_1000Button);
        Button timinghut_1500Button=(Button)findViewById(R.id.timinghut_1500Button);
        Button finalhutButton=(Button)findViewById(R.id.finalhutButton);

        startinghutButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent startinghutIntent=new Intent(MainActivity.this, StartingHutActivity.class);
                MainActivity.this.startActivity(startinghutIntent);
            }
        });

        timinghut_500Button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent timinghut_500Intent=new Intent(MainActivity.this, TimingHut_500Activity.class);
                MainActivity.this.startActivity(timinghut_500Intent);
            }
        });

        timinghut_1000Button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent timinghut_1000Intent=new Intent(MainActivity.this, TimingHut_1000Activity.class);
                MainActivity.this.startActivity(timinghut_1000Intent);
            }
        });

        timinghut_1500Button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent timinghut_1500Intent=new Intent(MainActivity.this, TimingHut_1500Activity.class);
                MainActivity.this.startActivity(timinghut_1500Intent);
            }
        });

        finalhutButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent finalhutIntent=new Intent(MainActivity.this, FinalHutActivity.class);
                MainActivity.this.startActivity(finalhutIntent);
            }
        });

        /*
        try{
            Thread.sleep(4000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        startActivity(new Intent(this, StartingHutActivity.class));
        finish();
        */
    }
}
