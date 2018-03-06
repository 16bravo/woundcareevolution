package com.example.pgarcia.camerafilemanageroldversionas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Casa on 24/10/2016.
 */

public class PatientListAdapter extends BaseAdapter {
    Context context;

    public PatientListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return Patients.size();
    }

    @Override
    public Object getItem(int i) {
        return Patients.getPatient(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // On met l'info du patient qui est à la position "i" de la liste

        View convertView = view;
        if(convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.patient_element_in_list,null);

        TextView t = (TextView) convertView.findViewById(R.id.patient);
        t.setText(Patients.getPatient(i).getName() + " " + Patients.getPatient(i).getSurname());

        return convertView;
    }
}
