package com.example.pgarcia.camerafilemanageroldversionas;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by pgarcia on 06/12/2016.
 */

public class Patient implements Serializable {
    private String name="";
    private String surname="";
    private String ipp ="";
    private String datebirth="";
    private ArrayList<String> plaies;
    private Sexe sex;

    // Constructeur de la classe
    public Patient(){
        ArrayList<String> plaies=new ArrayList<>();
        this.plaies = plaies;
    }

    // Getters et setters de tous les elements de la classe
    public String getName() {
        name = name.toUpperCase();
        return name;
    }
    public String getSex() {
        return sex.getSexe();
    }
    public void setSex(String sex) {
        this.sex = new Sexe(sex);
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) { this.surname = surname; }
    public String getIpp() {
        return ipp;
    }
    public void setIpp(String ipp) {
        this.ipp = ipp;
    }
    public String getUriPatient() { return name + "_" + surname + "_" + ipp; }
    public String getDatebirth() {
        return datebirth;
    }
    public void setDatebirth(String datebirth) {
        this.datebirth = datebirth;
    }
    public ArrayList<String> getPlaies() {
        return plaies;
    }

    public class Sexe implements Serializable {
        Boolean sex;

        Sexe (String s) {
            switch (s.toLowerCase()) {
                case "homme" :
                    sex = true;
                    break;
                case "femme" :
                    sex = false;
                    break;
                default:

            }
        }

        public String getSexe() {
            if (sex) return "Homme";
            else return "Femme";
        }
    }
}

