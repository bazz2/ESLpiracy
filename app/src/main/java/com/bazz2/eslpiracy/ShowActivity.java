package com.bazz2.eslpiracy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;

/**
 * Created by chenjt on 2015/12/7.
 */
public class ShowActivity extends Activity {
    private ImageButton ib_play;
    private EditText et_title;
    private ScrollView sv_content;

    private static boolean is_ib_play = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        ib_play = (ImageButton) findViewById(R.id.ib_play);
        et_title = (EditText) findViewById(R.id.et_title);
        sv_content = (ScrollView) findViewById(R.id.sv_content);

        ib_play.setOnClickListener(switch_play_pause);
    }

    private View.OnClickListener switch_play_pause = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (is_ib_play == true) {
                ib_play.setImageResource(R.drawable.pause);
                is_ib_play = false;
            } else {
                ib_play.setImageResource(R.drawable.play);
                is_ib_play = true;
            }
        }
    };
}
