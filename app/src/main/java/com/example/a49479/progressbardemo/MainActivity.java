package com.example.a49479.progressbardemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    MyProgressBarView custom_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        custom_progress = (MyProgressBarView)findViewById(R.id.custom_progress);
        custom_progress .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(custom_progress.getState() == MyProgressBarView.PROGRESSBAR_STATE_UN_START){
                    custom_progress.setState(MyProgressBarView.PROGRESSBAR_STATE_DOWNLOADING);
                    custom_progress.setPercentage(custom_progress.getPercentage()+1);
                }
                else if(custom_progress.getState() == MyProgressBarView.PROGRESSBAR_STATE_DOWNLOADING){
                    if(custom_progress.getPercentage()!=100) {
                        custom_progress.setPercentage(custom_progress.getPercentage() + 1);
                    }
                    else{
                        custom_progress.setState(MyProgressBarView.PROGRESSBAR_STATE_DONE
                        );
                    }
                }
            }
        });
    }


}
