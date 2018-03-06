package com.example.pgarcia.camerafilemanageroldversionas;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Casa on 25/10/2016.
 */

public class Patients  {
    private static ArrayList<Patient> patients;

    // TOUJOURS il faut passer le context de l'application a cette class car sinon elle ne
    // pourra ni stoker ni lires les donnees sur le dispositif .
    static Context context=null;

    // Constructor, getters et setters
    public Patients(Context context) {
        Patients.context = context;
    }
    public static Patient getPatient(int i) {
        return patients.get(i);
    }
    public static int size() {
        return patients.size();
    }
    public static void setPatient(Patient p) {
        patients.add(p);
    }
    public static void supprimerPatient(int i) {
        patients.remove(i);
    }

    public static void saveData() {
        if (context==null) {
            // TOUJOURS il faut passer le context de l'application a cette class car sinon elle ne
            // pourra ni stoker ni lires les donnees sur le dispositif .
            Toast.makeText(context, "Context is null, il faut appeler constructeur (save)",
                    Toast.LENGTH_LONG).show();
            return;
        }
        try {
            // Stockage des données (array list de patients de cette classe) sur l'album indiqué sur la classe CONSTANTS

            FileOutputStream fos = context.openFileOutput("patients", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(patients);
            os.close();
        } catch (Exception e) {
            Toast.makeText(context, "Error in saving data",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public static void loadData() {
        if (context==null) {
            // TOUJOURS il faut passer le context de l'application a cette class car sinon elle ne
            // pourra ni stoker ni lires les donnees sur le dispositif .

            Toast.makeText(context, "Context is null, il faut appeler constructeur (load)",
                    Toast.LENGTH_LONG).show();
            return;
        }
        try {
            // On lis les données du dossier indiqué sur Constants et on les enregistre sur l'ArrayList de cette classe

            FileInputStream fis = context.openFileInput("patients");
            ObjectInputStream is = new ObjectInputStream(fis);
            patients = (ArrayList<Patient>) is.readObject();
            is.close();
        } catch (Exception e) {
            Toast.makeText(context, "Bonjour, c'est la premiere fois que vous utilisez l'application",
                    Toast.LENGTH_LONG).show();
            patients = new ArrayList<>();
        }
    }

    @Nullable
    public static Patient getPatientByIpp(String ipp) {
        for(Patient p : patients) {
            if (ipp.equals(p.getIpp())){
                return p;
            }
        }
           return null;
    }
}
