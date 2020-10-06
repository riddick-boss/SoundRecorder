package abandonnedstudio.app.soundrecorder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ToDownloadViewHolder extends RecyclerView.ViewHolder{

    public TextView nameTextView;
    public ImageButton downloadButton;

    public ToDownloadViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTextView = (TextView) itemView.findViewById(R.id.listTrackNameDownloadTextView);
        downloadButton = (ImageButton) itemView.findViewById(R.id.DownloadTrackButton);
    }
}
