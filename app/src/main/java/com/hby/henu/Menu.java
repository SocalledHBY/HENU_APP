package com.hby.henu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class Menu extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Menu";
    private EditText yearET;
    private RadioGroup termRG;
    private Button menuScoreQueryBt;
    private Button menuLogoutBt;
    private String term = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        yearET = (EditText) findViewById(R.id.yearET);
        termRG = (RadioGroup) findViewById(R.id.termRG);
        menuScoreQueryBt = (Button) findViewById(R.id.menuScoreQueryBt);
        menuLogoutBt = (Button) findViewById(R.id.menuLogoutBt);
        menuScoreQueryBt.setOnClickListener(this);
        menuLogoutBt.setOnClickListener(this);

        termRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.termFirst:
                        term = "0";
                        break;
                    case R.id.termSecond:
                        term = "1";
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menuScoreQueryBt:
                Intent ScoreQuery = new Intent(this, ScoreQuery.class);
                ScoreQuery.putExtra("year", yearET.getText().toString());
                ScoreQuery.putExtra("term", term);
                startActivity(ScoreQuery);
                break;
            case R.id.menuLogoutBt:
                Intent login = new Intent(this, Login.class);
                startActivity(login);
                finish();
                break;
        }
    }
}
