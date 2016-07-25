package com.localz.spotz.sdk.app.model;

import android.content.Context;
import android.util.Log;

import com.localz.spotz.sdk.models.Spot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of HashMap that backs up data to a local file.
 *
 * @author Localz
 */

public class SpotzMap extends ConcurrentHashMap<String, Spot> {

    private static final long serialVersionUID = -7604275359152984786L;
    private File file;


    public SpotzMap(Context context) {
        super();

        ObjectInputStream inputStream = null;
        try {
            if (!getFile(context).exists()) {
                Log.d("SpotzMap", "SpotzMap file not exists - trying to create");
                getFile(context).createNewFile();
            } else {
                inputStream = new ObjectInputStream(new FileInputStream(getFile(context)));
                Map<String, Spot> spotzFromFile = (Map<String, Spot>) inputStream.readObject();
                if (spotzFromFile != null && !spotzFromFile.isEmpty()) {
                    super.putAll(spotzFromFile);
                    writeToFile();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Spot put(String key, Spot value) {
        Spot spot = super.put(key, value);
        writeToFile();
        return spot;
    }

    @Override
    public Spot remove(Object key) {
        Spot spot = super.remove(key);
        writeToFile();
        return spot;
    }

    @Override
    public void clear() {
        super.clear();
        writeToFile();
    }

    private void writeToFile() {
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(getFile(null)));
            HashMap<String, Spot> copyMap = new HashMap<>(this.size());
            copyMap.putAll(this);
            outputStream.writeObject(copyMap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File getFile(Context context) {
        if (file == null && context != null) {
            file = new File(context.getFilesDir(), "SpotzMap");
        }
        return file;
    }
}