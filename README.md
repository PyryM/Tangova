# Tangova
Project Tango plugin for Cordova.
You probably don't want to actually use this at the moment.

Usage:
```javascript
Tangova.startTango(callback, errorcallback);

// note: these callbacks won't be called at the moment (but the function does work)
Tangova.stopTango(successCallback, errorCallback);

// note: these callbacks won't be called at the moment (but the function does work)
Tangova.requestDepthFrame(successCallback, errorCallback);
```

The callback passed into startTango will receive both pose updates and depth frames (if requested). For example,
```javascript
function tangoCallback(data) {
  if(data.msgtype == "pose") {
    console.log(data.rotation);
    console.log(data.translation);
  } else if(data.msgtype == "depthmap") {
    console.log(data.rows);
    console.log(data.cols);
    console.log(data.b64data);
  }
}
```
