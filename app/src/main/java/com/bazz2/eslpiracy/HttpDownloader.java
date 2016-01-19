package com.bazz2.eslpiracy;
import android.app.AlertDialog;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chenjt on 2015/11/24.
 */
public class HttpDownloader {
    private URL url = null;

    /*
    public String download(String urlStr) {
        StringBuffer stringbuffer = new StringBuffer();
        String line;
        BufferedReader bufferedReader = null;

        try {
            url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                stringbuffer.append(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return stringbuffer.toString();
    }*/

    public int download(String urlStr, String path, String fileName) {
        InputStream inputStream = null;
        FileUtils fileUtils = new FileUtils();

        String fileNameTmp = fileName.concat(".part");
        if (fileUtils.isExist(path + "/" + fileNameTmp)) {
            try {
                fileUtils.deleteFile(path + "/" + fileNameTmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fileUtils.isExist(path + "/" + fileName))
            return 1;

        try {
            inputStream = getFromUrl(urlStr);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        File file = fileUtils.writeToSDPATHFromInput(path, fileNameTmp, inputStream);
        if (file != null) {
            fileUtils.rename(path+"/"+fileNameTmp, path+"/"+fileName);
            return 0;
        } else {
            return -1;
        }
    }

    public InputStream  getFromUrl(String urlStr) throws IOException {
        url = new URL(urlStr);
        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        InputStream input = httpUrlConnection.getInputStream();
        return input;
    }
}

class FileUtils {
    FileUtils() {
        //SDPATH = Environment.getExternalStorageDirectory() + "/";
    }

    void deleteFile(String fileName) throws IOException {
        File file = new File(fileName);
        file.delete();
    }

    File createFile(String fileName) throws IOException {
        File file = new File(fileName);
        file.createNewFile();
        return file;
    }

    File createDir(String path) throws IOException {
        File dir = new File(path);
        dir.mkdir();
        return dir;
    }

    boolean isExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    void rename(String from, String to) {
        File fromf = new File(from);
        File tof = new File(to);
        fromf.renameTo(tof);
    }

    File writeToSDPATHFromInput(String path, String fileName, InputStream inputStream) {
        File file = null;
        OutputStream outputStream = null;

        try {
            createDir(path);
            file = createFile(path + "/" + fileName);
            outputStream = new FileOutputStream(file);
            byte buffer[] = new byte[1024];
            while ((inputStream.read(buffer)) != -1) {
                outputStream.write(buffer);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                outputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
