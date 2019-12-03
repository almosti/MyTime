package com.almosti.mytime;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class TimeListOperator {
    private String file="TimeList.dat";

    ArrayList<TimePage> load(Context context){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (ArrayList<TimePage>) ois.readObject();
        } catch ( FileNotFoundException e ) {
            return null;
        } catch ( Exception e ) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if ( e instanceof InvalidClassException) {
                File data = context.getFileStreamPath(file);
                data.delete();
            }
        }
        return null;
    }

    boolean save(Context context, Serializable ser)
    {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(file, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
    }
}
