package abandonnedstudio.app.soundrecorder;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class DownloadFragment extends Fragment {

    private RecordingActivity recordingActivity;
    private RecyclerView myRecyclerView;
    private DatabaseReference dbDownRef;
    private FirebaseRecyclerOptions<ToDownload> downloadOptions;
    private FirebaseRecyclerAdapter<ToDownload, ToDownloadViewHolder> firebaseRecyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.download_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recordingActivity = new RecordingActivity();
        myRecyclerView = view.findViewById(R.id.downloadRecycylerView);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //checking internet connection
        if(!recordingActivity.checkInternetConnection(getContext())){
            Toast.makeText(getContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
        //getting right (user's) database reference
        dbDownRef = FirebaseDatabase.getInstance().getReference().child(getUserName()).child("To_User");
        dbDownRef.keepSynced(true);
        downloadOptions = new FirebaseRecyclerOptions.Builder<ToDownload>().setQuery(dbDownRef, ToDownload.class).build();
        //lot of stuff happening here to make recyclerview work properly
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ToDownload, ToDownloadViewHolder>(downloadOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ToDownloadViewHolder holder, int position, @NonNull final ToDownload model) {
                //if no track to download, then skipping this record in recyclerview
                if(model.getTitle().equals("name")){
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
                else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    holder.nameTextView.setText(model.getTitle());
                    holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadTrack(model.getDownloadUrl(), model.getTitle());
                        }
                    });
                }
            }

            @NonNull
            @Override
            public ToDownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_recyclerview_download_layout, parent, false);
                return new ToDownloadViewHolder(v);
            }
        };

        myRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //downloading track
    private void downloadTrack(String url, String trackName) {
        try {
            //checking internet connection, if not connected finishing function, otherwise tracks might be uploaded after connection multiple times
            if(!recordingActivity.checkInternetConnection(getContext())){
                Toast.makeText(getContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "Downloading started...", Toast.LENGTH_SHORT).show();
            File dir;
            String downloadFolderName = getContext().getExternalFilesDir("/").getAbsolutePath() + "/Downloaded";

            //checking if folder "Downloaded" exists, if not - creating it
            dir = new File(downloadFolderName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Toast.makeText(getContext(), "Failed to create folder, try again", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            DownloadManager dm = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url.trim());
            DownloadManager.Request request = new DownloadManager.Request(uri);

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(getContext(), "/Downloaded", trackName);

            dm.enqueue(request);
        }catch (IllegalArgumentException e){
            Toast.makeText(getContext(), "Error, contact administrator", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(getContext(), "Error, try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(firebaseRecyclerAdapter != null) firebaseRecyclerAdapter.stopListening();
    }

    //getting user name
    private String getUserName(){
        SharedPreferences prefs = getContext().getSharedPreferences(getString(R.string.prefs_id), Context.MODE_PRIVATE);
        return prefs.getString("Name", null);
    }
}
