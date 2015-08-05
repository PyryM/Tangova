# Tangova
Project Tango plugin for Cordova.

Installation:
```cordova plugin add https://github.com/PyryM/Tangova.git```
(For general installation of Cordova+Crosswalk [see this documentation](https://crosswalk-project.org/documentation/cordova/cordova_4.html))

Usage:
```javascript
// simplest way to start motion tracking on an ADF with one call
var adfName = "herblab"; //note: you use the *name* and not the *uuid*
                         //leave adfName as null or "" to not load an adf
Tangova.start(tangoCallback, onErrorCallback, adfName);

// to stop the motion tracking
// note: these callbacks won't be called at the moment (but the function does work)
Tangova.stopTango(successCallback, errorCallback);

// to set the maximum pose update rate (starts at 30hz)
Tangova.setMaxUpdateRate(15.5); // 15.5 hz
```

The callback passed into start will receive pose updates:
```javascript
function tangoCallback(data) {
  if(data.baseFrame === "AREA_DESCRIPTION") {
    // localized against the loaded area description
  } else if(data.baseFrame === "START_OF_SERVICE") {
    // localized against where the tango was when the service started
  }
  console.log(data.rotation);
  console.log(data.translation);
}
```
