package com.example.flaya.buttontest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends AppCompatActivity {

    EditText editInput = null;
    TextView textResult = null;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK )
        {
            if(event.getAction() == KeyEvent.ACTION_DOWN)
            {
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
