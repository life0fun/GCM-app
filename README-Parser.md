# This is a demo from Parse project.

Parse platform provides cloud distributed key value store for mobile app.
This is similar to AWS Dynamo, or cloud data structure store, cloud Redis.

https://parse.com/apps/quickstart?app_id=listen#android/blank

## Usage

Each app has app id and client key.

    `Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);`

Each ParseObject contains key-value JSON data to be stored in the cloud.

``` java
    ParseObject gameScore = new ParseObject("GameScore");
    gameScore.put("score", 1337);
    gameScore.put("playerName", "Sean Plott");
    gameScore.put("cheatMode", false);
    gameScore.saveInBackground();
    gameScore.saveEventually();  <-- save the object offline, eventual consistent!!!

    // to update an object, just set the new data inside save callback.
    gameScore.saveInBackground(new SaveCallback() {
      public void done(ParseException e) {
        // Now let's update it with some new data. In this case, only cheatMode and score
        // will get sent to the server. playerName hasn't changed.
        gameScore.put("score", 1338);
        gameScore.put("cheatMode", true);
        gameScore.saveInBackground();
      }
    });
```

Look at Data Browser in your app on Parse to check the object stored in the cloud.

```
    objectId: "xWMyZ4YEGZ", score: 1337, playerName: "Sean Plott", cheatMode: false,
    createdAt:"2011-06-10T18:33:42Z", updatedAt:"2011-06-10T18:33:42Z"
```

Use ParseQuery to retrieve objects using objectId.

```java
    ParseQuery query = new ParseQuery("GameScore");
    query.getInBackground("xWMyZ4YEGZ", new GetCallback() {
      public void done(ParseObject object, ParseException e) {
        if (e == null) {
            // object will be your game score
            (E)object.get('key');
            object.getInt('score');
            object.getString('playerName');
            object.getBoolean('cheatMode');
        } else {
            // something went wrong
        }
      }
    });
```

Auto incr Counter key and Complex data types, e.g, Array, Set, Map ?.
```java
    gameScore.increment("score");
    gameScore.increment("score", step);
    gameScore.saveInBackground();

    gameScore.addAllUnique("skills", Arrays.asList("flying", "kungfu"));
    gameScore.saveInBackground();
```

## To send msg to application, you can do web UI, curl post, or send msg from within app.

* send a POST request to https://api.parse.com/1/push with the Content-Type application/json.

    curl -X POST \
    -H "X-Parse-Application-Id: 2qF1TwYOVeCRCQn0UclQKlknNJWvrCk3E8JUNIFm" \
    -H "X-Parse-REST-API-Key: m7sQeLD0SEuMf5TGazKevUQJ0H4UGTfGR6ZEmrtp" \
    -H "Content-Type: application/json" \
    -d "channel": "", "type" : "android", "data": { "action": "com.parser.starter.locationchange", "msg": "hello world" } }' \
    https://api.parse.com/1/push

* To avoid bash quote mess, please use urllib2 to post json data to the site. Please refer to curl.py.

