package com.example.pgarcia.camerafilemanageroldversionas;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by pgarcia on 07/03/2017.
 */
public class GalerieActivity extends AppCompatActivity {
    // View qui va nous premettre afficher les photos consécutivement
    ViewFlipper imageFlipper;
    // Objet qui va nous premetre dettecter les mouvements qui fait
    // l'utilisateur pour changer de photo
    private GestureDetector mGestureDetector;

    RelativeLayout warning;
    ImageButton delete;
    TextView infoPhoto;

    // Liste de photos qui vont s'affichier sur le flipper
    ArrayList<File> photos;
    File uriPlaiesPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galerie_layout);

        imageFlipper = (ViewFlipper) findViewById(R.id.imageFlipper);
        warning = (RelativeLayout) findViewById(R.id.warning2);
        delete = (ImageButton) findViewById(R.id.delete);
        infoPhoto = (TextView) findViewById(R.id.infoPhoto);

        // On obtient la URI qui indique où est stocké l'album de plaies qu'il faut afficher
        Bundle bundle = getIntent().getExtras();
        uriPlaiesPatient = (File) bundle.getSerializable("uriPlaiesPatient");

        CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        mGestureDetector = new GestureDetector(this, customGestureDetector);

        imageFlipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        // Situer le cursor à la darniere image
        imageFlipper.setDisplayedChild(imageFlipper.getChildCount()-1);

        //Bouton pour ajouter une photo à la galerie
        ImageButton btn_add_new_plaie = (ImageButton) findViewById(R.id.btn_add_photo);
        btn_add_new_plaie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = CameraActivity.dispatchTakePictureIntent(GalerieActivity.this,uriPlaiesPatient);
                if (i != null) startActivity(i);
                else Toast.makeText(getApplicationContext(),"Probleme avec la camera",Toast.LENGTH_SHORT).show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // On genere un dialog pour confirmer la suppression de la photo
                // Si la reponse est affirmative, on le supprime

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(GalerieActivity.this);
                alertDialog.setTitle("Suppression de l'image");
                alertDialog.setMessage("Vous êtes sures que vous voulez la supprimer ?");
                alertDialog.setIcon(R.drawable.iconapp);

                alertDialog.setPositiveButton("Supprimer",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int i = imageFlipper.getDisplayedChild();

                                // Avec la fonction delete on supprime la photo du dispositif
                                if (photos.get(i).delete()) {
                                    // Si la suppression a bien marché :
                                    Toast.makeText(v.getContext(), "La photo a été bien supprimée",Toast.LENGTH_SHORT).show();

                                    // On supprime la photo du Flipper
                                    imageFlipper.removeViewAt(i);

                                    // On supprime la photo de la liste de photos
                                    photos.remove(i);

                                    // On cheque si le dossier est empty
                                    dossierEmpty(photos.isEmpty());
                                }
                                else  Toast.makeText(v.getContext(), "La photo ne a pu être supprimée",Toast.LENGTH_SHORT).show();

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
    protected void onResume() {
        super.onResume();

        // Il faut remplir le flipper car on sait pas si les photos ont changé
        photos = getPhotos(uriPlaiesPatient);
        remplirFlipper();
        dossierEmpty(photos.isEmpty());

        imageFlipper.setDisplayedChild(imageFlipper.getChildCount()-1);
        setInfoPhoto(imageFlipper.getDisplayedChild());

    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Fonction qui deplace les photos vers la droit où la gauche en fonction
            // du mouvement du doigt de l'utilisateur

            // Swipe right (next)
            if (e1.getX() > e2.getX()) {
                if (imageFlipper.getDisplayedChild() < imageFlipper.getChildCount()-1) {
                    imageFlipper.setInAnimation(GalerieActivity.this, R.anim.left_in);
                    imageFlipper.setOutAnimation(GalerieActivity.this, R.anim.left_out);
                    imageFlipper.showNext();
                    setInfoPhoto(imageFlipper.getDisplayedChild());
                }
            }

            // Swipe left (previous)
            if (e1.getX() < e2.getX()) {
                if (imageFlipper.getDisplayedChild() != 0) {
                    imageFlipper.setInAnimation(GalerieActivity.this, R.anim.right_in);
                    imageFlipper.setOutAnimation(GalerieActivity.this, R.anim.right_out);
                    imageFlipper.showPrevious();
                    setInfoPhoto(imageFlipper.getDisplayedChild());
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public ArrayList<File> getPhotos(File dir) {
        // On lit toutes les photos du dossier

        ArrayList<File> res = new ArrayList<>();

        if (dir.isDirectory())
            for (File f : dir.listFiles())
                res.add(f);

        // On reordene les Files par rapport à la date de creation
        Collections.sort(res, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.lastModified() > f2.lastModified() ? 1 : -1;
            }
        });

        return res;
    }

    private void remplirFlipper() {
        // On vide le flipper et on le remplit avec les photos qu'il y a sur l'ArrayList
        imageFlipper.removeAllViews();
        for (File f : photos)
            setFlipperImage(f);
    }

    private void setFlipperImage(File f) {
        // Fonction qui ajoute une photo sur le flipper

        ImageView image = new ImageView(getApplicationContext());
        Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
        image.setImageBitmap(Bitmap.createScaledBitmap(bm, bm.getWidth()/6,bm.getHeight()/6, false));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageFlipper.addView(image);
    }

    private void dossierEmpty(Boolean b) {
        // Fonction qui cheque s'il y a des photos sur le dossier
        // Si l'album est vide, on affiche le message qui dis que l'album est vide

        if (b) {
            warning.setVisibility(View.VISIBLE);
            delete.setVisibility(View.INVISIBLE);
            infoPhoto.setVisibility(View.INVISIBLE);
        } else {
            warning.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.VISIBLE);
            infoPhoto.setVisibility(View.VISIBLE);
        }
    }

    private void setInfoPhoto(int i) {
        // Fonction qui affiche la date (info) de la photo qu'on est en train d'afficher

        if (i < 0 || i >= photos.size()) {
            infoPhoto.setText("");
            return;
        }

        File f = photos.get(i);
        String date = (String) android.text.format.DateFormat.format("dd/MM/yyyy (HH:mm)", f.lastModified());
        infoPhoto.setText((i+1) + ". " + date);
    }

}
