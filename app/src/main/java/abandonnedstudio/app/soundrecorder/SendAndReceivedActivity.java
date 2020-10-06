package abandonnedstudio.app.soundrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class SendAndReceivedActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton goToSendFragment, goToDownloadFragment, goToRecordingAct;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_and_recived_layout);

        loadFragment(new SendFragment());

        //initialize layout elements
        goToSendFragment = (ImageButton) findViewById(R.id.sendFragmentButton);
        goToDownloadFragment = (ImageButton) findViewById(R.id.downloadFragmentButton);
        goToRecordingAct = (ImageButton) findViewById(R.id.backToRecordingButton);

        //set on click actions for buttons
        goToRecordingAct.setOnClickListener(this);
        goToDownloadFragment.setOnClickListener(this);
        goToSendFragment.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backToRecordingButton:
                startActivity(new Intent(SendAndReceivedActivity.this, RecordingActivity.class));
                finish();
                break;
            case R.id.sendFragmentButton:
                loadFragment(new SendFragment());
                break;
            case R.id.downloadFragmentButton:
                loadFragment(new DownloadFragment());
                break;
        }
    }

    //loading fragment
    private void loadFragment(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_placeholder, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SendAndReceivedActivity.this, RecordingActivity.class));
        finish();
    }
}
