package com.example.pgarcia.camerafilemanageroldversionas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.io.File;

/**
 * Created by Maxime on 05/03/2018.
 */

public class FichePatientCodeActivity extends AppCompatActivity {
    TextView ipp;
    TextView namePatient;
    TextView surname;
    TextView datebirth;
    TextView sex;
    ListView plaies;
    ImageButton addPlaie;
    RelativeLayout warning;

    File uriPlaiesPatient =null;
    Patient p = new Patient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On recupere le numéro du patient qui a été selectionné
        Bundle bundle = getIntent().getExtras();


        String ippRecup = getIntent().getStringExtra("IPP");
        System.out.println("CODE" + ippRecup +"a");

        setContentView(R.layout.fiche_patient);

        ipp = (TextView) findViewById(R.id.ipp);
        namePatient = (TextView) findViewById(R.id.name);
        surname = (TextView) findViewById(R.id.surname);
        datebirth = (TextView) findViewById(R.id.datebirth);
        sex = (TextView) findViewById(R.id.sex);
        addPlaie = (ImageButton) findViewById(R.id.btn_add_plaie);
        warning = (RelativeLayout) findViewById(R.id.warning1);

        plaies = (ListView) findViewById(R.id.list_plaies);



        // On obtient le patient
        p = Patients.getPatientByIpp(ippRecup);

        // On remplit la fiche de patient avec ses informaions
        ipp.setText(p.getIpp());
        namePatient.setText(p.getName()+" ");
        surname.setText(p.getSurname());
        datebirth.setText(p.getDatebirth());
        sex.setText(p.getSex()+"");

        // S'il y a pas de plaies on affiche le message "pas de plaies"
        if (p.getPlaies().isEmpty()) warning.setVisibility(View.VISIBLE);

        plaies.setAdapter(new PlaiesListAdapter(this,p.getPlaies()));
        plaies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int k, long l) {
                // Si on click sur un dossier de plaies on appelle la galerie
                Intent i = new Intent(FichePatientCodeActivity.this,GalerieActivity.class);
                uriPlaiesPatient = getUriPlaiesPatient(p.getUriPatient(), p.getPlaies().get(k));
                i.putExtra("uriPlaiesPatient", uriPlaiesPatient);
                startActivity(i);
            }
        });

        registerForContextMenu(plaies);

        addPlaie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si on clique sur le button d'ajouter un album de plaies, il  montre
                // le dialogue pour introduir le nom de l'album. Finalement il appelle
                // la galerieActivity pour prendre la premiere photo de l'album

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FichePatientCodeActivity.this);
                alertDialog.setTitle("Nouvelle plaie");
                alertDialog.setMessage("Entrer le nom");

                final EditText input = new EditText(FichePatientCodeActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setIcon(R.drawable.iconapp);

                alertDialog.setPositiveButton("Valider",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String nomPlaie = input.getText().toString();
                                if (nomPlaie.compareTo("") == 0) {
                                    Toast.makeText(getApplicationContext(),"Le nom de la plaie ne peut pas être vide", Toast.LENGTH_SHORT).show();
                                } else {
                                    p.getPlaies().add(nomPlaie);
                                    plaies.deferNotifyDataSetChanged();

                                    // On obtient la URI où est stocké l'album de photos du patient
                                    uriPlaiesPatient = getUriPlaiesPatient(p.getUriPatient(), p.getPlaies().get(p.getPlaies().size()-1));

                                    warning.setVisibility(View.INVISIBLE);

                                    // Avec cette fonction, on obtient l'intent qui conteindra un "extra"
                                    // Cet "extra" va indiquer à l'activity camera où elle doit stocker l'image qu'elle va prendre
                                    Intent i = CameraActivity.dispatchTakePictureIntent(FichePatientCodeActivity.this,uriPlaiesPatient);
                                    if (i != null) startActivity(i);
                                    else Toast.makeText(getApplicationContext(),"Probleme avec la camera",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                alertDialog.setNegativeButton("Annuler",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list_plaies) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_plaies_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.modifier:
                // On genere un dialogue pour demander a l'utilisateur de changer le nom de l'album de plaies

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FichePatientCodeActivity.this);
                alertDialog.setTitle("Modifier plaie");
                alertDialog.setMessage("Entrer le nom");

                final EditText input = new EditText(FichePatientCodeActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setText(p.getPlaies().get((int) info.id));
                alertDialog.setView(input);
                alertDialog.setIcon(R.drawable.iconapp);

                alertDialog.setPositiveButton("Valider",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String nomPlaie = input.getText().toString();
                                if (nomPlaie.compareTo("") == 0) {
                                    Toast.makeText(getApplicationContext(),"Le nom de la plaie ne peut pas être vide", Toast.LENGTH_SHORT).show();
                                } else {
                                    File f = getUriPlaiesPatient(p.getUriPatient(), p.getPlaies().get((int) info.id));
                                    p.getPlaies().set((int) info.id,nomPlaie);
                                    f.renameTo(getUriPlaiesPatient(p.getUriPatient(), p.getPlaies().get((int) info.id)));
                                }
                            }
                        });

                alertDialog.setNegativeButton("Annuler",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public File getUriPlaiesPatient(String name, String selectedPlaie) {
        // On genere la URI vers l'album de plaies dans le systeme de fichiers du dispositif

        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                Constants.DOSSIER_INTERNAL_STORAGE + Constants.DOSSIER_APPLICATION + "/" + name + "/" + selectedPlaie);
    }
}
