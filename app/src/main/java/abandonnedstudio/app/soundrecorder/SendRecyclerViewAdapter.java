package abandonnedstudio.app.soundrecorder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;


public class SendRecyclerViewAdapter extends RecyclerView.Adapter<SendRecyclerViewAdapter.MyViewHolder>{

    private File[] filesArray;
    private OnItemTrackClick onItemTrackClick;

    public SendRecyclerViewAdapter(File[] allTracks, OnItemTrackClick onItemTrackClick){
        this.filesArray = allTracks;
        this.onItemTrackClick = onItemTrackClick;
        //sorting tracks - the newest on top
        Arrays.sort(filesArray, Collections.<File>reverseOrder());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_recyclerview_layout, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.listTrackName.setText(filesArray[position].getName());
    }

    @Override
    public int getItemCount() {
        return filesArray.length;
    }

    //inner class
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView listTrackName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ImageButton listPlayTrack = itemView.findViewById(R.id.listPlayTrackButtonDownload);
            listTrackName = itemView.findViewById(R.id.listTrackNameDownloadTextView);

            listPlayTrack.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemTrackClick.onClickListen(filesArray[getAdapterPosition()], getAdapterPosition());
        }
    }

    //interface
    public interface OnItemTrackClick{
        void onClickListen(File file, int position);
    }
}
