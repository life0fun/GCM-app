# Android GCM demo

This application demos the basic feature  of GCM, including:

* Register device to GCM upon App started or phone power recycle.
* Listen to GCM RECEIVE intent and REGISTRATION intent.
* Customized Broadcast IntentService only starts running to handle GCM intent upon receiving GCM intent.
* Notification bar shows the newly received message.
* Store gcm message into local content provider.
* display stored gcm messages in a IM style list fragment.
* user can acknowledge gcm message and acke message also stored in the same content provider.

## Architecture

* ActionTab and Fragment UI.
* Two fragments, one is list of gcm messages, the other is GCM control panel.
* Standalone service started upon power up, or when app get started.
* Local Content Provider maps Uri to local SQLite database.
* Basic skeleton of a typical App, service, looper, content provider, fragmented UI, message json object.
* Design strictly follows clean code principle, testable components.

## Configuration

* update App's SENDER_ID with your GCM application project.
* note down the registration id from the log tag GCM and add the device reg id to sender's client list on server app side.


## Server App

Server App is a node app. Authorization to your GCM project is done through basic http Authorization header.

* Post json object msg to https endpoint at android.googleapis.com/gcm/send
* Http header Authorization using the API key from your GCM app project page. 
    https://code.google.com/apis/console/#project:686172024995:access
* Cache a list of devices GCM registration ids for msg to be sent to each of them.
* device GCM registration id is obtained when device registered to GCM on Android phone.
* http authorization headers and http request options are encapsulated into requestwrapper.coffee.


## Parser cloud backend integration

We have integrated Parser cloud backend into the project. The Parser project name is called Listen in the backend.
https://parse.com/apps/listen/push_notifications

Please refer to README-Parser.md
