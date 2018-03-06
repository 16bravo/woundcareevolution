package com.example.pgarcia.camerafilemanageroldversionas;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by pgarcia on 08/11/2016.
 */
public abstract class GiroscopeActivity extends MaskActivity {
    // Left, Right, Top, Buttom
    private TextView l;
    private TextView r;
    private TextView t;
    private TextView b;

    protected ImageButton method;
    protected ImageButton calibr;
    protected ImageButton photo;

    // Si true, la methode de transparency (transp-rouge)
    // Si false, la methode vert-jaune
    // ATENTTION : Il doit commencer à FALSE. Si vous voulez commencer pour le method
    // de transparency, il faut changer des lignes de code
    private boolean transparency=false;

    // Il est true si on est en train de prende la photo (le countdown est demarré)
    private boolean takingPhoto=false;

    // Calibrer est true si l'utilisateur a clické sur le button calibrage. Une fois les variables cy, et cz
    // mise a jour il devient faux une autre fois
    private boolean calibrer=false;

    // Indique si le telephone a été deja calibré
    protected boolean calibrated1=false;

    // Indique si l'utilisateur est pret pour prende la photo
    protected boolean calibrated2 =false;

    //Valeurs de calibrage
    int cy, cz;

    // Meilleur de ne pas toucher : elles sont des variables qu'on va utiliser pour enregistrer
    // les valeurs captées du gyroscope
    final float[] mValuesMagnet      = new float[3];
    final float[] mValuesAccel       = new float[3];
    final float[] mValuesOrientation = new float[3];
    final float[] mRotationMatrix    = new float[9];

    SensorEventListener mEventListener;
    SensorManager sensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        l = (TextView) findViewById(R.id.l);
        r = (TextView) findViewById(R.id.r);
        t = (TextView) findViewById(R.id.t);
        b = (TextView) findViewById(R.id.b);

        method = (ImageButton) findViewById(R.id.method);
        calibr = (ImageButton) findViewById(R.id.calibrer);

        // Au debut le carré est transparent
        r.setAlpha(0);
        l.setAlpha(0);
        t.setAlpha(0);
        b.setAlpha(0);

        // Mais les elements sont verts
        r.setBackgroundColor(Color.rgb(60, 255, 100));
        l.setBackgroundColor(Color.rgb(60, 255, 100));
        t.setBackgroundColor(Color.rgb(60, 255, 100));
        b.setBackgroundColor(Color.rgb(60, 255, 100));

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        mEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                // On recupere les valeurs chaque fois que le gyroscope de l'appareil a des nouvelles informations

                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
                        break;
                }

                // Avec cettes nouvelles infos, on appelle notre fonction qui va travailler avec ces nouveaux valeurs
                if (event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD)
                    captureValeurs();
            }
        };

        method.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // On change le methode utilisé
                transparency = !transparency;

                if (transparency) {
                    // Tous les cotées rouges
                    r.setBackgroundColor(Color.rgb(255, 60, 100));
                    l.setBackgroundColor(Color.rgb(255, 60, 100));
                    t.setBackgroundColor(Color.rgb(255, 60, 100));
                    b.setBackgroundColor(Color.rgb(255, 60, 100));
                } else if (calibrated1) {
                    // Tous les cotées opaques
                    t.setAlpha(1);
                    b.setAlpha(1);
                    l.setAlpha(1);
                    r.setAlpha(1);
                }
            }
        });

        calibr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // On indique qu'il faut mettre à jour les valeurs de cy et cz
                calibrer = true;
                // Il indique que l'utilisateur a deja calibré
                calibrated1=true;

                // On affiche le carré de la methode en fonction de la methode selectioné dans cet instant
                if (transparency) {
                    r.setBackgroundColor(Color.rgb(255, 60, 100));
                    l.setBackgroundColor(Color.rgb(255, 60, 100));
                    t.setBackgroundColor(Color.rgb(255, 60, 100));
                    b.setBackgroundColor(Color.rgb(255, 60, 100));
                } else {
                    t.setAlpha(1);
                    b.setAlpha(1);
                    l.setAlpha(1);
                    r.setAlpha(1);
                }

            }
        });

        // You have set the event lisetner up, now just need to register this with the
        // sensor manager along with the sensor wanted.
        setListners();
    }

    private void captureValeurs() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet);
        SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);

        //Factor multiplicatif (meilleur ne pas modifier)
        final int s = 1000;

        int yy = (int) (mValuesOrientation[1] * s);
        int zz = (int) (mValuesOrientation[2] * s);

        // Si on a clické sur le button de calibrer, ce boolean va etre true
        // Donc on actualise les variables cy et cz et on met le boolean a faux car on a deja calibré
        if (calibrer) {
            cy = yy;
            cz = zz;
            calibrer = false;
            Toast.makeText(getApplicationContext(), "Vous venez de calibrer",Toast.LENGTH_SHORT).show();

            method.setVisibility(View.VISIBLE);
            photo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00AD63")));
            calibr.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7B807C")));
        }

        // Si l'utilisateur a bien calibré l'appareil en fonction de la methode selectioné on fait une chose ou une autre
        if (calibrated1) {
            if (transparency) {
                // ********************** METHOD TRANSPARENCY **********************

                double ey;
                double ez;

                // Au debut tous les cotés sont transparents
                t.setAlpha(0);
                b.setAlpha(0);
                l.setAlpha(0);
                r.setAlpha(0);

                // Cette precision c'est seulement pour modifier le degrée de transparence du carré (pas la precision pour prendre la photo)
                // Si la valeur est plus haut, il y a moins de precision; plus bas, plus de precision
                // (pour pouvoir voir les differences de precision il faut changer la valeur en minumum 5 unitées)
                // La valeur par default est 30.0 (c'est meilleur de ne pas modifier cette valeur!!)
                final double precision = 30.0;

                // On calcule le niveau de transparence qu'il faut appliquer
                // La valeur calculé est entre 0 et 1 où
                // --> 0 est transparence absolue car il y a pas d'erreur
                // --> 1 est opaque car il y a le maximum d'erreur
                if (yy - cy == 0) ey = 0;
                else ey = 1.0 - (precision / ((double) Math.abs(yy - cy)));

                if (zz - cz == 0) ez = 0;
                else ez = 1.0 - (precision / ((double) Math.abs(zz - cz)));

                // On applique cette transparence sur le coté qui a l'erreur
                // (*** voir plus de détail sur ça dans le methode de COULEURS)

                // Si l'erreur se trouve dans y cela signifie qu'il faut modifier soit le coté supérieur, soit le coté inferieur
                // --> S'il est positif il faut modifier le coté inferieur
                // --> S'il est negatif il faut modifier le coté superieur

                if (yy > cy) b.setAlpha((float) ey);
                else t.setAlpha((float) ey);

                // Si l'erreur se trouve dans z cela signifie qu'il faut modifier soit le coté droit, soit le coté gauche
                // --> S'il est positif il faut modifier le coté gauche
                // --> S'il est negatif il faut modifier le coté droit

                if (zz > this.cz) l.setAlpha((float) ez);
                else r.setAlpha((float) ez);

            } else {
                // ********************** METHOD COLEURS **********************

                // On fait le calcul de l'erreur entre le valeur de calibrage  et le valeur  de la postion actuel du téléphone
                int ez = Math.abs(zz - cz);
                int ey = Math.abs(yy - cy);

                //La couleur ne peut pas avoir une valeur superieur à 255.
                if (ez > 255) ez = 255;
                if (ey > 255) ey = 255;

                // On met tous les cotés à vert au début
                r.setBackgroundColor(Color.rgb(60, 255, 100));
                l.setBackgroundColor(Color.rgb(60, 255, 100));
                t.setBackgroundColor(Color.rgb(60, 255, 100));
                b.setBackgroundColor(Color.rgb(60, 255, 100));

                // EXPLICATION DU FONCTIONNEMENT DU CHANGEMENT De la COULEUR DU CARRÉ //

                // cy et cz indiquent la position de calibrage
                // yy et zz indiquent la position actuelle du telephone
                // ey et ez indiquent l'erreur. C'est-à-dire, la difference en valeur absolue entre cy et yy et entre cz et zz

                // Il faut voir qu'on part de tous les cotés verts. Donc on va modifier seulement les cotés où il y a une erreur de position
                // Si l'erreur est 0 (il n'en y a pas) --> le coté sera vert car sa couleur sera RGB(err=0,255,100) = VERT
                // Si l'erreur est le maximmum (255) --> le coté sera jaune car sa couleur sera RGB(err=255,255,100) = JAUNE
                // Si l'erreur est entre 0 et 255 --> le coté aura une couleur entre Jaune et Vert, s'il est tres proche de 0
                //                                    il sera tres proche  d'etre vert. S'il est tres proche de 255, il sera
                //                                    tres proche d'être jaune.

                // En fonction de si l'erreur se retreuve dans y ou dans z, on change un coté où un autre :

                // Si l'erreur se trouve dans y cela signifie qu'il faut modifier soit le coté supérieur, soit le coté inferieur
                // --> S'il est positif il faut modifier le coté inferieur
                // --> S'il est negatif il faut modifier le coté superieur

                if (yy > cy) b.setBackgroundColor(Color.rgb(ey, 255, 100));
                else t.setBackgroundColor(Color.rgb(ey, 255, 100));

                // Si l'erreur se trouve dans z cela signifie qu'il faut modifier soit le coté droit, soit le coté gauche
                // --> S'il est positif il faut modifier le coté gauche
                // --> S'il est negatif il faut modifier le coté droit

                if (zz > cz) l.setBackgroundColor(Color.rgb(ez, 255, 100));
                else r.setBackgroundColor(Color.rgb(ez, 255, 100));
            }

            // Si l'utilisateur est pret pour prendre la photo (calibrated2==true), on verifie si le telephone
            // est dans la bonne position pour commencer à prendre la photo
            if (calibrated2) checkIfTakePhoto(Math.abs(yy - cy), Math.abs(zz - cz));
        }

    }

    private void checkIfTakePhoto(int y, int z) {
        if (y <= Constants.PRECISION_TO_TAKE_PHOTO && z <= Constants.PRECISION_TO_TAKE_PHOTO) {
            // On verifie si l'erreur est plus petit que la precision qu'on a indiqué
            // S'il est plus petit, on commence a prendre la photo

            if (!takingPhoto) {
                takingPhoto=true;
                takePhoto();
            }
        } else if (takingPhoto) {
            // S'il y a plus d'erreur que la precision indiqué, et on est en train de prendre la photo
            // on indique qu'il faut arreter la prise de photo

            stopTakingPhoto();
            takingPhoto=false;
        }
    }

    protected void onResume() {
        super.onResume();
        setListners();
    }

    protected void onPause() {
        super.onPause();
        unsetListners();
    }

    // Register the event listener and sensor type.
    public void setListners()    {
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(mEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unsetListners()  {
        // On supprime les senseurs
        sensorManager.unregisterListener(mEventListener);

        // On fait disparaitre le carré (il devient transparent)
        t.setAlpha(0);
        b.setAlpha(0);
        l.setAlpha(0);
        r.setAlpha(0);
    }

    // On declare cettes fonctions ici pour pouvoir les utiliser, mais elles sont implementées
    // dans CameraActvitiy
    protected abstract void takePhoto();
    protected abstract void stopTakingPhoto();

}
