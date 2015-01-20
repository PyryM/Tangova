package com.mtknn.tangova;

import android.app.Activity;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import org.apache.cordova.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.annotation.SuppressLint;

import android.widget.Toast;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import java.io.FileInputStream;
import java.io.IOException;

public class Tangova extends CordovaPlugin  {
    private static final String LOG_TAG = "Tangova";
    protected CallbackContext tangoCallbackContext;
    protected boolean wantsDepth = false;

    private Tango mTango;
    private TangoConfig mConfig;
    private boolean mIsTangoServiceConnected = false;
    private Activity mActivity;
    private TangoDepthGridulator mGridulator;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.d(LOG_TAG, "Tangova.initialize");
        super.initialize(cordova, webView);

        // only the gridulator really needs to be initted
        mGridulator = new TangoDepthGridulator(32, 32);
        mGridulator.setGridParams(1.0f, 1.0f, // w, h
                                  0.1f, 2.0f, // min depth, max depth
                                  true);    // clamp depth

        initTango();
    }

	/**
	* Executes the request and returns PluginResult.
	*
	* @param action The action to execute.
	* @param args JSONArry of arguments for the plugin.
	* @param callbackContext The callback id used when calling back into JavaScript.
	* @return A PluginResult object with a status and message.
	*/
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start_tango")) {
        	tangoCallbackContext = callbackContext;
        	PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            startTango(args);
            return true;
        } else if(action.equals("stop_tango")) {
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            callbackContext.sendPluginResult(r);
            stopTango();
            return true;
        } else if(action.equals("request_depth_frame")) {
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            callbackContext.sendPluginResult(r);
            requestTangoDepth();
            return true;
        }

        return super.execute(action, args, callbackContext);
    }

    public JSONObject poseToJSON(TangoPoseData pose) {        
        JSONObject ret = new JSONObject();
        try {
            ret.put("msgtype", "pose");
            ret.put("translation", new JSONArray(pose.translation));
            ret.put("rotation", new JSONArray(pose.rotation));
            ret.put("timestamp", pose.timestamp);
            ret.put("statusCode", pose.statusCode);
            ret.put("confidence", pose.confidence);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "poseToJSON error (how???)",e);
        }
        return ret;
    }

    public JSONObject depthToJSON(TangoXyzIjData xyzIj) {
        byte[] buffer = new byte[xyzIj.xyzCount * 3 * 4];
        FileInputStream fileStream = new FileInputStream(
                 xyzIj.xyzParcelFileDescriptor.getFileDescriptor());
        try {
            fileStream.read(buffer,
                    xyzIj.xyzParcelFileDescriptorOffset, buffer.length);
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String gridulated = mGridulator.computeB64GridString(buffer);

        JSONObject ret = new JSONObject();
        try {
            ret.put("msgtype", "depthmap");
            ret.put("rows", mGridulator.rows());
            ret.put("cols", mGridulator.cols());
            ret.put("b64data", gridulated);
            ret.put("timestamp", xyzIj.timestamp);   
        } catch (JSONException e) {
            Log.e(LOG_TAG, "depthToJSON error (how???)", e);
        }
        return ret;
    }

    public void requestTangoDepth() {
    	wantsDepth = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Tango.TANGO_INTENT_ACTIVITYCODE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this.cordova.getActivity(),
                        "This app requires Motion Tracking permission!",
                        Toast.LENGTH_LONG).show();
                return;
            }
            try {
                setTangoListeners();
            } catch (TangoErrorException e) {
                Toast.makeText(this.cordova.getActivity(), "Tango Error! Restart the app!",
                        Toast.LENGTH_SHORT).show();
            }
            try {
                mTango.connect(mConfig);
                mIsTangoServiceConnected = true;
            } catch (TangoOutOfDateException e) {
                Toast.makeText(this.cordova.getActivity(),
                        "Tango Service out of date!", Toast.LENGTH_SHORT)
                        .show();
            } catch (TangoErrorException e) {
                Toast.makeText(this.cordova.getActivity(),
                        "Tango Error! Restart the app!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initTango() {
        // Instantiate Tango client
        mTango = new Tango(this.cordova.getActivity());

        // Set up Tango configuration for motion tracking
        // If you want to use other APIs, add more appropriate to the config
        // like: mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true)
        try {
            mConfig = new TangoConfig();
            mConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT);
            mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
            mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        } catch (TangoErrorException e) {
            Log.e(LOG_TAG, e.toString() + ": " + e.getMessage());
        }
    }

    public void startTango(JSONArray args) {
        if (!mIsTangoServiceConnected) {
            this.cordova.startActivityForResult((CordovaPlugin) this,
                    Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_MOTION_TRACKING),
                    Tango.TANGO_INTENT_ACTIVITYCODE);
        }
    }

    public void stopTango() {
        // When the app is pushed to the background, unlock the Tango
        // configuration and disconnect
        // from the service so that other apps will behave properly.
        try {
            mTango.disconnect();
            mIsTangoServiceConnected = false;
        } catch (TangoErrorException e) {
            Toast.makeText(this.cordova.getActivity(), "Tango Error!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendData(JSONObject data) {
        // Success return object
        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
        result.setKeepCallback(true);
        tangoCallbackContext.sendPluginResult(result);
    }

    private void setTangoListeners() {
        // Select coordinate frame pairs
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        // Add a listener for Tango pose data
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {

            @SuppressLint("DefaultLocale")
            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                sendData(poseToJSON(pose));
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                if(wantsDepth) {
                    sendData(depthToJSON(xyzIj));
                    wantsDepth = false;
                }
            }

            @Override
            public void onTangoEvent(TangoEvent arg0) {
                // Ignoring TangoEvents
            }

        });
    }
}
