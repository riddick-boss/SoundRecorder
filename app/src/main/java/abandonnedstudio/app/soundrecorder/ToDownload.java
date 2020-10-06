package abandonnedstudio.app.soundrecorder;

public class ToDownload {

    //class used by firebase api
    //necessary to make firebase recyclerview adapter work properly

    private String title;
    private String DownloadUrl;

    public ToDownload(){
    }

    public ToDownload(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDownloadUrl() {
        return DownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        DownloadUrl = downloadUrl;
    }
}
