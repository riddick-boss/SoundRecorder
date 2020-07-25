package abandonnedstudio.app.soundrecorder;

import android.content.Context;
import android.media.MediaPlayer;
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
import java.io.File;

public class SendFragment extends Fragment implements SendRecyclerViewAdapter.OnItemTrackClick {

    private File[] allFiles;
    private SendRecyclerViewAdapter sendRecyclerViewAdapter;
    private RecyclerView recyclerView;
    private MediaPlayer pl = null;
    private File fileToPlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.send_fragment_layout, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        pl = new MediaPlayer();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.sendRecyclerView);

        String trackPath = getActivity().getExternalFilesDir("/").getAbsolutePath()+"/Uploaded";
        File directory = new File(trackPath);
        allFiles = directory.listFiles();
        sendRecyclerViewAdapter = new SendRecyclerViewAdapter(allFiles, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(sendRecyclerViewAdapter);
    }

    @Override
    public void onClickListen(File file, int position) {
        fileToPlay = file;
        playAudio(fileToPlay);
    }

    private void playAudio(File track){
        try {
            pl.setDataSource("file://" + track.getAbsolutePath());
            pl.prepare();
            pl.start();
            Toast.makeText(getContext(), "Playing track", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            pl.release();
            pl = new MediaPlayer();
        }
    }
}
