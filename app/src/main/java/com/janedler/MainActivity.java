package com.janedler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Toast测试类
 * Created by janedler on 2016/11/23.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.toast_v1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.janedler.V1.JToast.makeText(MainActivity.this,"V1---Toast", com.janedler.V1.JToast.LENGTH_LONG).show();

            }
        });

        ((Button) findViewById(R.id.toast_v2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.janedler.V2.JToast.make(MainActivity.this.getWindow().getDecorView(),"V2---Toast", com.janedler.V2.JToast.LENGTH_LONG).show();
            }
        });

    }
}
