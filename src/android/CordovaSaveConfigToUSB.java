package cordova.plugin.write.rt160.config.usb;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
// import android.util.Log;
// import android.view.View;
// import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaSaveConfigToUSB extends CordovaPlugin {
    private final String filenameExternal = "emmt.cfg";
    // private static final int CREATE_FILE = 1;
    // private static final int UPDATE_CFG_FILE = 69;
    private static final int SELECT_USB_DRIVE = 69;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("saveCfgToUsb")) {
            JSONObject cfgSettings = args.getString(0);
            this.saveCfgToUsb(cfgSettings, callbackContext);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == SELECT_USB_DRIVE
                && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            Boolean hasFile = false;
            if (resultData != null) {
                String encodedFileName = Uri.encode("/emmt.cfg").toString();
                uri = resultData.getData();
                DocumentFile usbDrive = DocumentFile.fromTreeUri(this.getApplicationContext(), uri);
                DocumentFile foundCfgFile = usbDrive.findFile("emmt.cfg");
                if (foundCfgFile != null) {
                    hasFile = foundCfgFile.exists();
                }
                if (hasFile) {
                    foundCfgFile.delete();
                    DocumentFile newCfgFile = usbDrive.createFile("application/cfg", "emmt.cfg");
                    try {
                        writeCfgAtUri(newCfgFile, "I am configured so fuck off");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    DocumentFile newCfgFile = usbDrive.createFile("application/cfg", "emmt.cfg");
                    try {
                        writeCfgAtUri(newCfgFile, "I am not configured fuck off anyway");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void saveCfgToUsb(JSONObject cfgSettings, CallbackContext callbackContext) {
        if (cfgSettings != null ) {
            setUSBDirectory();
            callbackContext.success(cfgSettings);
        } else {
            callbackContext.error("Expected one non-empty argument.");
        }
    }

    private void setUSBDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, SELECT_USB_DRIVE);
    }

    private Boolean writeCfgAtUri(DocumentFile cfgFile, String cfgContents) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            if (cfgFile.canWrite()) {
                String isWriteable = Boolean.toString(cfgFile.canWrite());
                OutputStream outputStream = getContentResolver().openOutputStream(cfgFile.getUri());
                outputStream.write(cfgContents.getBytes());
                outputStream.flush();
                outputStream.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
