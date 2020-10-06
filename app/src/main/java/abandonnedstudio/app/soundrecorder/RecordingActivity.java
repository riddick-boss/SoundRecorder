package abandonnedstudio.app.soundrecorder;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RecordingActivity extends AppCompatActivity implements View.OnClickListener {

    //Layout Bars
    ImageButton StartRecordingButton, StopRecordingButton, PlayTr1Button, PlayTr2Button, StopTr1Button, StopTr2Button;
    Button  RestartAllButton, SendButton, GoToAllTracksButton;
    Chronometer chronometer;
    CheckBox checkBoxTr1, checkBoxTr2;

    final String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private boolean firstRecordingsSaved=false, isAlreadyRecording=false;
    private String trackPath1 = null, trackPath2 = null;
    private StorageReference storageref;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //pointing that tracks will be uploaded to folder named as name user inserted
        storageref = FirebaseStorage.getInstance().getReference("SoundRecorder_storage");

        //load layout
        setContentView(R.layout.recording_layout);

        //initialize layout elements
        StartRecordingButton = (ImageButton) findViewById(R.id.startRecordingButton);
        StopRecordingButton = (ImageButton) findViewById(R.id.stopRecordingButton);
        PlayTr1Button = (ImageButton) findViewById(R.id.playTr1Button);
        PlayTr2Button = (ImageButton) findViewById(R.id.playTr2Button);
        StopTr1Button = (ImageButton) findViewById(R.id.stopTr1Button);
        StopTr2Button = (ImageButton) findViewById(R.id.stopTr2Button);
        RestartAllButton = (Button) findViewById(R.id.recordingRestartAllButton);
        SendButton = (Button) findViewById(R.id.sendButton);
        GoToAllTracksButton = (Button) findViewById(R.id.recordingGoToAllTracksButton);
        chronometer = (Chronometer) findViewById(R.id.chronometerRecording);
        checkBoxTr1 = (CheckBox) findViewById(R.id.check_box_tr1);
        checkBoxTr2 = (CheckBox) findViewById(R.id.check_box_tr2);

        progressDialog = new ProgressDialog(this);

        //set on click actions for buttons
        RestartAllButton.setOnClickListener(this);
        SendButton.setOnClickListener(this);
        GoToAllTracksButton.setOnClickListener(this);

    }

    //* *** ACTIONS FOR BUTTONS *** */

    //on click actions
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.recordingRestartAllButton:
                deleteRecordedTracks();
                break;
            case R.id.sendButton:
                sendTracksToFirebase();
                break;
            case R.id.recordingGoToAllTracksButton:
                startActivity(new Intent(RecordingActivity.this, SendAndReceivedActivity.class));
                try{
                    player.release();
                }catch (Exception ignored){
                }
                finish();
        }
    }

    /* *** SENDING FILES TO FIREBASE ACTIONS *** */

    //sending tracks to firebase
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private void sendTracksToFirebase(){

        //checking internet connection
        if(!checkInternetConnection(this)){
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        final File track1, track2, dir;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.getDefault());
        StorageReference filePathInFirebase;
        Uri uriTrack1, uriTrack2;
        final String currentDateAndTime, uploadedFolderName= Objects.requireNonNull(this.getExternalFilesDir("/")).getAbsolutePath()+"/Uploaded";

        //checking if folder "Uploaded" exists, if not - creating it
        dir= new File(uploadedFolderName);
        if(!dir.exists()){
            if(!dir.mkdirs()){
                Toast.makeText(this, "Failed to create folder, try again", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //sending tracks which were chosen (ticked) to firebase and saving them on user's device

        //sending only first track
        if(checkBoxTr1.isChecked() && !(checkBoxTr2.isChecked())){
            try{
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
                track1 = new File(trackPath1);
                //getting current date and time (used to name file)
                currentDateAndTime = sdf.format((new Date()));
                filePathInFirebase = storageref.child(getName()).child("From_user").child(currentDateAndTime+"__audio.3gp");
                uriTrack1 = Uri.fromFile(track1);
                filePathInFirebase.putFile(uriTrack1)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordingActivity.this, "Track 1 uploaded successfully", Toast.LENGTH_SHORT).show();
                                //renaming and changing location of file which was uploaded
                                track1.renameTo(new File(RecordingActivity.this.getExternalFilesDir("/").getAbsolutePath()+"/Uploaded/"+currentDateAndTime+"__audio.3gp"));
                                firstRecordingsSaved=false;
                                CreateDatabaseReference();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordingActivity.this, "Uploading error... Try again", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (Exception e){
                progressDialog.dismiss();
                Toast.makeText(this, "Track not recorded yet", Toast.LENGTH_SHORT).show();
            }
        }
        //sending only second track
        else if((checkBoxTr2.isChecked()) && !(checkBoxTr1.isChecked())){
            try{
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
                track2 = new File(trackPath2);
                //getting current date and time (used to name file)
                currentDateAndTime = sdf.format((new Date()));
                filePathInFirebase = storageref.child(getName()).child("From_user").child(currentDateAndTime+"__audio.3gp");
                uriTrack2 = Uri.fromFile(track2);
                filePathInFirebase.putFile(uriTrack2)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordingActivity.this, "Track 2 uploaded successfully", Toast.LENGTH_SHORT).show();
                                //renaming and changing location of file which was uploaded
                                track2.renameTo(new File(RecordingActivity.this.getExternalFilesDir("/").getAbsolutePath()+"/Uploaded/"+currentDateAndTime+"__audio.3gp"));
                                CreateDatabaseReference();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordingActivity.this, "Uploading error... Try again", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (Exception e){
                progressDialog.dismiss();
                Toast.makeText(this, "Track not recorded yet", Toast.LENGTH_SHORT).show();
            }
        }
        //sending both tracks
        else if((checkBoxTr1.isChecked()) && (checkBoxTr2.isChecked())){
            try{
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
                track1 = new File(trackPath1);
                track2 = new File(trackPath2);
                //getting current date and time (used to name file)
                currentDateAndTime = sdf.format((new Date()));
                filePathInFirebase = storageref.child(getName()).child("From_user").child(currentDateAndTime+"__audio1.3gp");
                uriTrack1 = Uri.fromFile(track1);
                uriTrack2 = Uri.fromFile(track2);
                filePathInFirebase.putFile(uriTrack1)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //renaming and changing location of file which was uploaded
                                track1.renameTo(new File(RecordingActivity.this.getExternalFilesDir("/").getAbsolutePath()+"/Uploaded/"+currentDateAndTime+"__audio1.3gp"));
                                firstRecordingsSaved=false;
                                CreateDatabaseReference();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordingActivity.this, "Uploading error... Try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                filePathInFirebase = storageref.child(getName()).child("From_user").child(currentDateAndTime+"__audio2.3gp");
                filePathInFirebase.putFile(uriTrack2)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordingActivity.this, "Both tracks uploaded successfully", Toast.LENGTH_SHORT).show();
                                //renaming and changing location of file which was uploaded
                                track2.renameTo(new File(RecordingActivity.this.getExternalFilesDir("/").getAbsolutePath()+"/Uploaded/"+currentDateAndTime+"__audio2.3gp"));
                                CreateDatabaseReference();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(RecordingActivity.this, "Uploading error... Try again", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e){
                progressDialog.dismiss();
                Toast.makeText(this, "Track not recorded yet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //used to be able to download tracks in future easily
    //creating
    private void CreateDatabaseReference(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(getName()).child("To_User");
        DatabaseReference newDbRef = databaseReference.push();
        newDbRef.child("title").setValue("name");
        newDbRef.child("DownloadUrl").setValue("url");
    }

    public Boolean checkInternetConnection(Context ct){
        ConnectivityManager cm = (ConnectivityManager) ct.getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        try{
            if (networkCapabilities != null) {
                return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            else return false;
        }catch(Exception e){
            return false;
        }
    }

    //deleting recorded tracks which aren't sent to firebase
    private void deleteRecordedTracks() {
        File track1, track2;
        try {
            track1 = new File(trackPath1);
            track1.delete();
        } catch (Exception ignored) {
        }
        try {
            track2 = new File(trackPath2);
            track2.delete();
        } catch (Exception ignored) {
        }
        Toast.makeText(this, "Tracks deleted", Toast.LENGTH_SHORT).show();
        firstRecordingsSaved = false;
    }

    /* *** END OF SENDING FILES TO FIREBASE ACTIONS *** */

    /* *** *** */

    /* *** RECORDING ACTIONS *** */

    //setting actions for buttons responsible for recording audio track
    public void onRecordingButtonClicked(View v){
        switch (v.getId()){
            case R.id.startRecordingButton:
                startRecording();
                break;
            case R.id.stopRecordingButton:
                stopRecording();
                break;
        }
    }

    //actions to do on starting recording track
    private void startRecording() {
        //checking if track is already being recorded
        if(!isAlreadyRecording) {
            //checking if app has permissions to access audio
            if (checkAudioPermission()) {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setOutputFile(trackFileName());
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                try {
                    recorder.prepare();
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    recorder.start();
                    isAlreadyRecording=true;
                    Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Something went wrong... Try again", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Unalbe to access audio", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Recording has already started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording(){
        //checking if track is already being recorded
        if(isAlreadyRecording) {
            recorder.stop();
            chronometer.stop();
            recorder.release();
            recorder = null;
            firstRecordingsSaved = !firstRecordingsSaved;
            Toast.makeText(this, "Track saved", Toast.LENGTH_SHORT).show();
            isAlreadyRecording=false;
        } else{
            Toast.makeText(this, "Recording hasn't started yet", Toast.LENGTH_SHORT).show();
        }
    }

    /* *** END OF RECORDING ACTIONS *** */

    /* *** *** */

    /* *** PLAYING RECORDED TRACKS ACTIONS *** */

    //setting actions for buttons responsible for playing recorded tracks - audio1 and audio2
    public void playTrackButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.playTr1Button:
                playTrack(1);
                break;
            case R.id.stopTr1Button:
            case R.id.stopTr2Button:
                stopPlayingTrack();
                break;
            case R.id.playTr2Button:
                playTrack(2);
                break;
        }
    }

    private void playTrack(int nrTrack){
        player = new MediaPlayer();
        //if track which user wants to play is not recorded yet, a toast will appear
        try{
            if(nrTrack==1){
                player.setDataSource(trackPath1);
            } else{
                player.setDataSource(trackPath2);
            }
            player.prepare();
            player.start();
        } catch (Exception e){
            Toast.makeText(this, "Track not recorded yet...", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayingTrack() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    /* *** END OF PLAYING RECORDED TRACKS ACTIONS *** */

    /* *** *** */

    /* *** ACTIONS ON APP LEAVING WITHOUT ENDING RECORDING OR PLAYING TRACKS BEFORE *** */

    @Override
    public void onStop() {
        super.onStop();
        //releasing recorder if not done previously
        if(recorder!=null){
            recorder.release();
            recorder=null;
        }
        if(player!=null){
            player.release();
            player=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //deleting not uploaded (to Firebase) tracks
        if(trackPath1 != null || trackPath2 != null){
            deleteRecordedTracks();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //preventing leaving app when track is still being recorded
        if(isAlreadyRecording){
            Toast.makeText(this, "Track is still recording, pause it", Toast.LENGTH_SHORT).show();
        }
        else finish();
    }

    /* *** END OF ACTIONS ON APP LEAVING WITHOUT ENDING RECORDING OR PLAYING TRACKS BEFORE *** */

    //checking and asking for permission to record audio
    public boolean checkAudioPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            int ALL_PERMISSIONS = 1;
            ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
            return false;
        }
    }

    //deciding which track will be recorded - first or second; and returning track file name and path
    private String trackFileName() {
        String trackPath;
        if (!firstRecordingsSaved) {
            trackPath = this.getExternalFilesDir("/").getAbsolutePath();
            trackPath += "/" + getName() + "_audio1.3gp";
            trackPath1=trackPath;
        } else {
            trackPath = Objects.requireNonNull(this.getExternalFilesDir("/")).getAbsolutePath();
            trackPath += "/" + getName() + "_audio2.3gp";
            trackPath2=trackPath;
        }
        return trackPath;
    }

    //loading name saved in opening activity - is used to name file as "yourName_audioX.3gp"
    public String getName(){
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.prefs_id), Context.MODE_PRIVATE);
        return prefs.getString("Name", null);
    }
}
