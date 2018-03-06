package com.example.pgarcia.camerafilemanageroldversionas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    RelativeLayout warning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        // Nous allons charger les données sur la classe Patients
        new Patients(this);
        Patients.loadData();

        // On demande les permissions d'Android si on ne les a pas
        checkPermision(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA);

        // On check si l'appareil a une camera pour prendre photos
        if(!checkCameraHardware(this)) {
            Toast.makeText(this, "L'appareil photo ne fonctionne pas", Toast.LENGTH_LONG).show();
            // finish();
        }

        //Bouton pour ajouter un patient
        ImageButton btn_add_new = (ImageButton) findViewById(R.id.btn_add_patient);
        btn_add_new.setOnClickListener(new View.OnClickListener(){

            public void onClick(View arg0){
                // Si on click sur le button, l'application appelle l'activity NoveauPatientActivity
                Intent intent = new Intent(MainActivity.this, NouveauPatientActivity.class);
                startActivity(intent);
            }

        });

        warning = (RelativeLayout) findViewById(R.id.warning0);
        listView = (ListView) findViewById(R.id.patientList);
        listView.setAdapter(new PatientListAdapter(this));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int p, long l) {
                // Si on click sur un element de la liste de patients, on appelle l'activity FichePatientActivity
                Intent i = new Intent(MainActivity.this,FichePatientActivity.class);
                // C'est OBLIGATOIRE de lui passer le numero patient qui doit le faire apparaitre, sinon l'app va crasher (dans l'extra)
                i.putExtra("num",p);
                startActivity(i);
            }
        });

        // C'est pour ajouter un menu quand on fait un long clique sur les patients de la liste
        // Il va montrer l'option de supprimer le patient
        registerForContextMenu(listView);

        // C'est pour montrer le message qui dit "il y a pas de patients dans la liste" s'il en y a pas
        if (Patients.size() == 0) warning.setVisibility(View.VISIBLE);

        //Bouton pour scanner un Code-barre
        Button btn_scan = (Button) findViewById(R.id.scan_code_barre);
        btn_scan.setOnClickListener(new View.OnClickListener(){

            public void onClick(View arg0){
                // Si on click sur le button, l'application appelle l'activity NoveauPatientActivity
                Intent intent = new Intent(MainActivity.this, MenuCaptureActivity.class);
                startActivity(intent);
            }

        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.patientList) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_patients_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.supprimer:
                // Quand on appuie sur supprimer un patient, il faut le supprimer et actualiser la liste des patients qui est entrain de s'afficher
                Patients.supprimerPatient((int) info.id);
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // On actualise la liste des patients car elle peut etre changé (par exemple, on crée un nouveau patient)
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        if (Patients.size() > 0) warning.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        // Quand on arrete l'app, on enregistre les données
        Patients.saveData();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.help) {
            Intent i = new Intent(MainActivity.this,HelpActivity.class);
            // On utilise les extras pour indiquer a l'activity quel layout doit utiliser pour l'affichage
            i.putExtra("id",1);
            startActivity(i);
            return true;
        } else if (id == R.id.apropos) {
            Intent i = new Intent(MainActivity.this,HelpActivity.class);
            i.putExtra("id",2);
            startActivity(i);
            return true;
        }
        return false;
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void checkPermision(String p1, String p2) {
        if (ContextCompat.checkSelfPermission(this, p1) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,p1)) {
                ActivityCompat.requestPermissions(this, new String[]{p1,p2}, 1);
            }
        }
    }

}
