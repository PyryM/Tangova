var exec = require("cordova/exec");

/**
 * Constructor.
 *
 * @returns {Tangova}
 */
function Tangova() { }

/**
 * Requests tango permission: 0 = motion, 1 = ADF
 */
Tangova.prototype.requestPermission = function(successCallback, errorCallback, ptype) {
    exec(successCallback, errorCallback, 'Tangova', 'request_permissions', [ptype]);
}

/**
 * Convenience function to request both motion and ADF with one call
 */
Tangova.prototype.requestAllPermissions = function(successCallback, errorCallback) {
    var newthis = this;
    newthis.requestPermission(function(v) {
        newthis.requestPermission(successCallback, errorCallback, 1);
    }, errorCallback, 0);    
}

/**
 * Starts the tango motion tracking and depth camera
 */
Tangova.prototype.startTango = function (successCallback, errorCallback, useDepth) {
    if (errorCallback == null) {
        errorCallback = function () {
        };
    }

    if (typeof errorCallback != "function") {
        console.log("Tangova.startTango: failure parameter not a function");
        return;
    }

    if (typeof successCallback != "function") {
        console.log("Tangova.startTango: success callback parameter must be a function");
        return;
    }

    var dparam = useDepth || false;
    exec(successCallback, errorCallback, 'Tangova', 'start_tango', [dparam]);
}

/**
 * Stops the tango motion tracking and depth camera
 */
Tangova.prototype.stopTango = function () {
    var errorCallback = function (e) {
        console.log(e);
    };
    var successCallback = function(e) {
        console.log(e);
    };
    exec(successCallback, errorCallback, 'Tangova', 'stop_tango', []);
}

/**
 * Requests a depth frame
 */
Tangova.prototype.requestDepthFrame = function (depthmode) {
    var depthmode = mode || 1;
    var errorCallback = function (e) {
        console.log(e);
    };
    var successCallback = function(e) {
        console.log(e);
    };
    exec(successCallback, errorCallback, 'Tangova', 'request_depth_frame', [depthmode]);
}

/**
 * Set the point cloud --> depthmap conversion parameters
 */
Tangova.prototype.setDepthParams = function(options) {
    var errorCallback = function (e) {
        console.log(e);
    };
    var successCallback = function(e) {
        console.log(e);
    };
    var gridArgs = [ 
                     options.rows       || 32,
                     options.cols       || 32,
                     options.width      || 0.625,
                     options.height     || 0.3125,
                     options.minDepth   || 0.1,
                     options.maxDepth   || 2.0
                   ];
    exec(successCallback, errorCallback, 'Tangova', 'set_grid_params', gridArgs);
};

/**
 * Get a list of ADF files
 */
Tangova.prototype.getADFList = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'Tangova', 'get_adf_list', []);
}

/**
 * Load an ADF
 */
Tangova.prototype.loadADF = function (filename, successCallback, errorCallback) {
    if (errorCallback == null) {
        errorCallback = function (e) {console.log(e)};
    }
    if (successCallback == null) {
        successCallback = function (e) {console.log(e)};
    }
    exec(successCallback, errorCallback, 'Tangova', 'load_adf', [filename]);
}

module.exports = new Tangova();
