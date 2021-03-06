package abandonnedstudio.app.soundrecorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //Layout Bars
    Button submitButton;
    EditText nameEditText;

    public String name=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checking if name is already entered
        if(loadName() != null){
            startActivity(new Intent(MainActivity.this, RecordingActivity.class));
            finish();
        }

        setContentView(R.layout.opening_layout);

        //initialize layout elements
        submitButton = (Button) findViewById(R.id.openingButton);
        nameEditText = (EditText) findViewById(R.id.openingEditText);

        //set actions for button
        //save name and start recording activity
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameEditText.getText().toString();
                SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_id), Context.MODE_PRIVATE);
                prefs.edit().putString("Name", name).apply();
                Toast.makeText(getApplicationContext(), "Name saved!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, RecordingActivity.class));
                finish();
            }
        });

    }

    //loading saved name
    private String loadName(){
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.prefs_id), Context.MODE_PRIVATE);
        return prefs.getString("Name", null);
    }
}
