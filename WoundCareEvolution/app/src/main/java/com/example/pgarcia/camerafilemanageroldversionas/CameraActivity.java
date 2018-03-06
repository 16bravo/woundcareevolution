package com.example.pgarcia.camerafilemanageroldversionas;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// Elle hérite de la classe gyroscopeActivity et maskactivity
public class CameraActivity extends GiroscopeActivity {

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button confirmation;
    private ImageButton delete;

    private TextView timer;
    private LinearLayout flash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timer = (TextView) findViewById(R.id.timer);
        photo = (ImageButton) findViewById(R.id.button);
        flash = (LinearLayout) findViewById(R.id.flash);
        surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        confirmation = (Button) findViewById(R.id.confirmPhoto);
        delete = (ImageButton) findViewById(R.id.deletePhoto);

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!calibrated2) {
                    calibrated2 =true;
                    Toast.makeText(getApplicationContext(), "Vous êtes prêt pour prendre la photo",
                            Toast.LENGTH_SHORT).show();
                }
                else temps.start();
            }
        });

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        confirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si on appuie sur le button delete
                // on supprime la photo, on finalise l'activity et
                // on la recommence pour pouvoir prendre une nouvelle photo

                if (uriPhotoDestination.exists()) uriPhotoDestination.delete();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    @Override // Quand cette fonciton est appellé pour MaskActivity,
    // CameraActivity est responsable de demarrer le countdown
    protected void takePhoto() {
        temps.start();
    }

    @Override // Si MaskActivity appelle cette fonction, cela signifie que nous devons arreter
    // le countdwon car l'utilisateur n'est pas dans la bonne position
    protected void stopTakingPhoto() {
        temps.cancel();
        timer.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open();
        // Il y a un bug ici : il ne redémarre pas la camera
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        // Normalement il faut pas finaliser l'activity ici mais on le fait car il y a un bug
        // avec la camera : quand on bloque le telephone, la camera s'arrete aussi et on ne peut
        // pas  redémarrer la caméra
        finish();
    }

    //Retardateur : on ajoute 2 seconds au temps qui l'utilsateur indique pour les  raisons suivantes :
    // On suppose que le temps indiquée sur COUNDOWN_TIME est 3
    // (seconds 1,2,3) L'appareil va compter 3 seconds
    // (second  4) Quand il les a comptés, il va prendre la photo (et on va afficher un effet de flash)
    // (second  5) Un second apres de prendre la photo, il faut effaçer le flash, donc on utilise le encore le compteur.
    CountDownTimer temps = new CountDownTimer(Constants.COUNTDOWN_TIME*1000+2000, 1000) {
        public void onTick(long millisUntilFinished) {
            int time = (int) (millisUntilFinished / 1000) - 1;

            if (time == 0) {
                // Si le timer est fini, on prend la photo
                camera.takePicture(shutterCallback, null, null, jpegCallBack);
                unsetListners();

                // On montre l'effet flash
                flash.setBackgroundColor(0x88ffffff);
                timer.setText("");
            }
            // sinon, on montre combien de temps il manque pour prendre la photo
            else timer.setText(""+time);
        }
        public void onFinish() {
            // On efface tous les objets qui sont utilisées pour prendre la photo
            // et on affiche les objets qui sont utilises pour confirmer la prise de photo ou la supprimer
            flash.setBackgroundColor(0x00ffffff);
            timer.setText("");
            photo.setVisibility(View.INVISIBLE);
            calibr.setVisibility(View.INVISIBLE);
            method.setVisibility(View.INVISIBLE);
            maskView.setVisibility(View.INVISIBLE);
            opacity.setVisibility(View.INVISIBLE);
            opacityB.setVisibility(View.INVISIBLE);
            taille.setVisibility(View.INVISIBLE);
            tailleB.setVisibility(View.INVISIBLE);
            confirmation.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }
    };


    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        //Implémente la surface de la caméra
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (Throwable t) {
                Log.e("TEST1", "Exception in setPreviewDisplay()", t);
                Toast.makeText(CameraActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        //Configuration camera (meilleur de ne toucher pas)
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
            parameters.setRotation(90);

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
            Camera.Size size = sizes.get(0);

            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i).width > size.width)
                    size = sizes.get(i);
            }

            parameters.setPictureSize(size.width, size.height);
            camera.setParameters(parameters);
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    Camera.PictureCallback jpegCallBack = new Camera.PictureCallback() {
        public void onPictureTaken(final byte[] data, Camera camera) {
            try {
                // On genere le bitmap avec l'image prise
                Bitmap userImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                FileOutputStream out = new FileOutputStream(uriPhotoDestination);
                userImage.compress(Bitmap.CompressFormat.JPEG, 90, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    };

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };


    public static Intent dispatchTakePictureIntent(Context activityQuiAppelle, File uriPhoto) {
        // TOUJOURS lorsque vous voulez appeller la CameraActivity, il faut utiliser cette fonction
        // pour generer son intent. Si on n'indique pas où il faut stocker la photo, l'application
        // va crasher

        // PARAMETRES:
        // activityQuiAppelle : c'est l'objet de l'activity qui va appeller à CameraActivity
        // uriPhoto : c'est le File (vide) où la photo va se stoker

        File photoFile = null;
        try {
            // Avec cette fonction on genere le File vide qui sera remplit pour l'activity CameraActivity
            photoFile = createImageFile(uriPhoto);
        } catch (IOException ex) {
            Toast.makeText(activityQuiAppelle, "Erreur à la creation du fichier pour enregistrer l'image: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            // On cree l'intent qui va vers CameraActivity et on ajoute la destination de la photo
            Intent i = new Intent(activityQuiAppelle,CameraActivity.class);
            i.putExtra("uriPhotoDestination",photoFile);

            // Si la photo prise est vide, on la supprime
            if (photoFile.length() == 0L) {
                photoFile.delete();
            }

            return i;
        } else return null;
    }

    private static File createImageFile(File uri) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // On genere le dossier s'il n'existe pas sur le systeme de fichiers
        if (!uri.isDirectory())
            uri.mkdirs();

        // On genere le File avec un nom concret
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",             /* suffix */
                uri                 /* directory */
        );

        return image;
    }
}
