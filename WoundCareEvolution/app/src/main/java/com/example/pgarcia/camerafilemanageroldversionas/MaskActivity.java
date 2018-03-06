package com.example.pgarcia.camerafilemanageroldversionas;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;

/**
 * Created by pgarcia on 17/01/2017.
 */
public abstract class MaskActivity extends Activity {

    protected ImageView maskView;
    protected SeekBar opacity,taille;
    protected ImageButton tailleB,opacityB;

    // k indique la taille du carré transparent au centre du Mask
    protected Double k=0.25;

    protected File uriPhotoDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);

        maskView = (ImageView) findViewById(R.id.mask);
        opacity = (SeekBar) findViewById(R.id.opacity);
        taille = (SeekBar) findViewById(R.id.taille);
        opacityB = (ImageButton) findViewById(R.id.opacityB);
        tailleB = (ImageButton) findViewById(R.id.tailleB);

        maskView.setAlpha(128);

        Bundle bundle = getIntent().getExtras();
        uriPhotoDestination = (File) bundle.getSerializable("uriPhotoDestination");

        final Bitmap img = getDerniereImage(uriPhotoDestination.getParentFile());
        if (img != null) {
            // S'il y a au moins une photo sur l'album, on utilise le darniere comme mask
            // In faut appeller la fonction editMask pour changer la taille du mask
            // et lui faire incorporé le carré transparent au centre
            Bitmap mask = editMask(img);
            if (mask != null) {
                maskView.setImageBitmap(mask);
            }
        } else {
            // Si l'album est vide, on n'utilise pas de masque
            taille.setVisibility(View.INVISIBLE);
            opacity.setVisibility(View.INVISIBLE);
            opacityB.setVisibility(View.INVISIBLE);
            tailleB.setVisibility(View.INVISIBLE);
        }

        opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // On change l'opacité du mask en fonction de l'intencité que l'utilisateur a
                // indique avec la seekBar de transparence
                maskView.setAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        taille.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Si l'utilisateur change la taille du carré, il faut le changer dans le mask

                k = progress/1000.0;

                if (img != null) {
                    Bitmap mask = editMask(img);
                    if (mask != null) {
                        maskView.setImageBitmap(mask);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        opacityB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opacity.setVisibility(opacity.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
            }
        });

        tailleB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taille.setVisibility(taille.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    private Bitmap getDerniereImage(File url) {
        //  Il cherche l'image avec la date de modification la plus recente

        long neuf = 0;
        File ret = null;

        for (File f : url.listFiles()) {
            if (       f.lastModified() > neuf
                    && f.lastModified() != uriPhotoDestination.lastModified()
                    && f.getName().endsWith(".jpg")) {

                neuf = f.lastModified();
                ret = f;
            }
        }
        // S'il a pas trouve acune image dans le dossier, elle va returner null
        return (ret != null) ? BitmapFactory.decodeFile(ret.getAbsolutePath()) : null;
    }

    private Bitmap editMask(Bitmap mask) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // get Taille écran
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // C'EST NECESSAIRE FAIRE ÇA, sinon l'app crash. On considere que si k (la taille du carré) est
        // plus petite que 0.02, le carré est de la taille de l'image, alors on supprime la mask
        if (k<0.02)
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mask = Bitmap.createScaledBitmap(mask, mask.getWidth()/8,mask.getHeight()/8, false);

        if (mask != null) {
            // On calcule les percentages à partir de k et la taille de l'écran
            // De cette facon on obien les poins du carré
            int x1 = (int) (k*mask.getWidth());
            int y1 = (int) (k*mask.getHeight());
            int x2 = (int) ((1-k)*mask.getWidth());
            int y2 = (int) ((1-k)*mask.getHeight());

            // Avec cette fonction, on déssine un carré à l'image qui commence au point (x1, y1) et qui finis au point (x2-x1,y2-y1)
            mask.setPixels(new int[mask.getWidth()*mask.getHeight()],0,mask.getWidth(),x1, y1, x2-x1, y2-y1);

            return mask;
        } else {
            Toast.makeText(getApplicationContext(), "Il y a eu un problème avec le mask",Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}
