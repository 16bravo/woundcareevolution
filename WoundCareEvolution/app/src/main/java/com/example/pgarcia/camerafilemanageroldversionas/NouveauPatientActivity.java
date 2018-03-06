package com.example.pgarcia.camerafilemanageroldversionas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Created by ahassani on 17/01/2017.
 */
public class NouveauPatientActivity extends AppCompatActivity {

    EditText ipnView;
    EditText nameView;
    EditText surnameView;
    EditText datebirthView;
    Button btn_add;
    RadioGroup radioGroup;
    RadioButton radio1,radio2;

    String sex=null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nouveau_patient);

        ipnView = (EditText) findViewById(R.id.et_ipn);
        nameView = (EditText) findViewById(R.id.et_name);
        surnameView = (EditText) findViewById(R.id.et_surname);
        datebirthView = (EditText) findViewById(R.id.et_birthdate);
        btn_add = (Button) findViewById(R.id.btn_add);
        radio1=(RadioButton) findViewById(R.id.radio1);
        radio2=(RadioButton) findViewById(R.id.radio2);
        radioGroup=(RadioGroup)findViewById(R.id.radioGroup);

        btn_add.setOnClickListener(new View.OnClickListener(){
            //récupération des données inscritent dans les edits --> méthode to String
            public void onClick(View arg0){
                if (sex == null || nameView.getText().toString().length() < 1  ||
                        surnameView.getText().toString().length() < 1) {
                    Toast.makeText(getApplicationContext(), "Les champs nom, prénom et sexe sont obligatoires",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Quand on click sur le button d'ajouter, il cheque si l'utilisateur a introduit toutes les
                // informations obligatoires. Si c'est le cas, on crée un nouveau patient sur la liste de
                // patients et on finalise l'activity de creation de patient.

                Patient contact = new Patient();
                contact.setName(nameView.getText().toString());
                contact.setSurname(surnameView.getText().toString());
                contact.setIpp(ipnView.getText().toString());
                contact.setDatebirth(datebirthView.getText().toString());
                contact.setSex(sex);

                Patients.setPatient(contact);

                finish();
            }
        });

    }


    public void onRadioButtonClicked(View view) {
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio1:
                sex = "Homme";
                break;
            case R.id.radio2:
                sex = "Femme";
                break;
        }
    }

}