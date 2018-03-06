package com.example.pgarcia.camerafilemanageroldversionas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Casa on 24/10/2016.
 */

public class PlaiesListAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> plaies;

    public PlaiesListAdapter(Context context, ArrayList<String> plaies) {
        this.context = context;
        this.plaies = plaies;
    }

    @Override
    public int getCount() {
        return plaies.size();
    }

    @Override
    public Object getItem(int i) {
        return plaies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // On met l'info de la plaie qui est à la position "i" de la liste

        View convertView = view;
        if(convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.plaie_element_in_list,null);
        TextView t = (TextView) convertView.findViewById(R.id.plaie);
        t.setText(plaies.get(i));
        return convertView;
    }
}
