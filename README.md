# SoundRecorder
#### Android app which enables user to record audio tracks, listen to them and send to Firebase Storage (which might be held by audio processing company) or store them locally. Firebase holder can send tracks to user, who is able to download them directly from app.

Used:
- Java
- Fragments
- Firebase Database
- Firebase Storage
- [FirebaseUI](https://github.com/firebase/FirebaseUI-Android/tree/master/database)
- Android Media API
- Local Storage
- Permissions
- Shared Preferences

## UI

#### 1. Name entering fragment

<img src="/Screenshots/open.png" width="200">

This fragment is shown only once, when user is opening app for the first time. User is entering his/company/etc name. It is used later to name audio track file. Name is stored in Shared Preferences. If it already exists, audio recording fragment is shown at app opening.

#### 2. Audio recording fragment

<img src="/Screenshots/recording.png" width="200">
<img src="/Screenshots/upload.png" width="200">

In this fragment user is able to record tracks, listen to them and decide which to send to Firebase Storage. There is option to start recording, stop it and continue recording later. Tracks which user sent to Storage are also saved locally, so user has his own copy of tracks. Tracks are deleted if user decides not to send them, for the reason of saving local memory - if track is not good enough to send it, there is no reason to keep it. App is creating folder in device memory only for tracks which user sent. Track files are named like "name_which_user_entered_current_time_in_miliseconds". That's why app is asking user to enter his name at first opening. This naming convention is convinient, because it is easy to check who sent file and when. User has no affect on naming tracks. However, for every user unique id is been created in Firebase to avoid situation when few users enter the same name and it is not possible to declare which track belongs to who. In app there are 2 fields "track 1" and "track 2". If user record first track, he will be able to listen to it under field "track 1", second track will be under "track 2", third track will replace first track, so it will be under field "track 1", and so on.

<img src="/Screenshots/permission.png" width="200">
App is checking for audio recording and storing permission and asking for it if necessary.

#### 3. Sent tracks fragment

<img src="/Screenshots/sent.png" width="200">

List of tracks which were send to Storage. There is option to listen to them directly from app.

#### 4. Tracks to download fragment

<img src="/Screenshots/to_download.png" width="200">

List of tracks which user can download. [FirebaseUI](https://github.com/firebase/FirebaseUI-Android/tree/master/database) is used to bind recyclerview.
Tracks are stored in Firebase Storage, but reference to them is stored in Database which is updated instantly, so app shows always actual list of tracks possible to download by user. App is creating specified folder in device memory only for tracks which are downloaded.

## Firebase

#### What happens in Firebase when user uploads track?

<img src="/Screenshots/firebase_storage.png" width="600">
When user uploads first track, in Firebase Storage his folder is created, named by name he provided in app and his unique Firebase id. Instantly 2 other folders in his folder are being created - "To_user", where Firebase holder can easily put tracks which he wants user to download and "From_user", where it is easy to find every track user has sent. Next tracks which user sends are stored in folder "From_user".

<img src="/Screenshots/firebase_database.png" width="600">
In Firebase Realtime Database there is being created a record "To_user" with 2 fields.

**App is creating all this stuff so, all what Firebase holder has to do is: in "DownloadUrl" put url to track in Storage and set it's title in "title" field :smile:.**
