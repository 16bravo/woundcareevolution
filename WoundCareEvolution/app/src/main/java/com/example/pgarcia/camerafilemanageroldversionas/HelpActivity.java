package com.example.pgarcia.camerafilemanageroldversionas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // L'app remplit avec le layout qui est indiqué
        Bundle bundle = getIntent().getExtras();
        switch (bundle.getInt("id",1)) {
            case 1:
                setContentView(R.layout.help_layout);
                break;
            case 2:
                setContentView(R.layout.apropos_layout);
                break;
        }
    }
}
