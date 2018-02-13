package com.seventhmoon.jamnow.Data;


import android.os.Environment;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class FileOperation {

    private static final String TAG = FileOperation.class.getName();

    private static File RootDirectory = new File("/");


    public static boolean init_folder_and_files() {
        Log.i(TAG, "init_folder_and_files() --- start ---");
        boolean ret = true;
        //RootDirectory = null;

        //path = new File("/");
        //RootDirectory = new File("/");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File folder_jam = new File(RootDirectory.getAbsolutePath() + "/.jamNow/");
        File folder_server = new File(RootDirectory.getAbsolutePath() + "/.jamNow/servers");

        if(!folder_jam.exists()) {
            Log.i(TAG, "folder_jam folder not exist");
            ret = folder_jam.mkdirs();
            if (!ret)
                Log.e(TAG, "init_folder_and_files: failed to mkdir hidden");
            try {
                ret = folder_jam.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!ret)
                Log.e(TAG, "init_info: failed to create hidden file");
        }

        if(!folder_server.exists()) {
            Log.i(TAG, "folder_server folder not exist");
            ret = folder_server.mkdirs();
            if (!ret)
                Log.e(TAG, "init_folder_and_files: failed to mkdir hidden");
            try {
                ret = folder_server.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!ret)
                Log.e(TAG, "init_info: failed to create hidden file");
        }

        while(true) {
            if(folder_jam.exists() && folder_server.exists())
                break;
        }

        Log.i(TAG, "init_folder_and_files() ---  end  ---");
        return ret;
    }

    public static void removeAll() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File folder_tennis = new File(RootDirectory.getAbsolutePath() + "/.jamNow/");

        if (folder_tennis.exists()) {
            for (File file : folder_tennis.listFiles()) {
                if (!file.delete())
                    Log.e(TAG, "delete error, can't delete " + file.getName());
                else
                    Log.d(TAG, "delete "+file.getName()+ " success!");
            }
        }


    }

    public static boolean remove_file(String fileName) {
        boolean ret = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File file = new File(RootDirectory.getAbsolutePath() + "/.jamNow/"+fileName);

        if (file.exists()) {
            ret = file.delete();
        } else {
            Log.d(TAG, "file "+file.getName()+ " is not exist");
        }

        return ret;
    }

    public static boolean check_record_exist(String fileName) {
        Log.i(TAG, "check_record_exist --- start ---");
        boolean ret = false;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File file = new File(RootDirectory.getAbsolutePath() + "/.jamNow/"+fileName);

        if(file.exists()) {
            Log.i(TAG, "file exist");
            ret = true;
        }

        Log.i(TAG, "check_record_exist --- end ---");

        return ret;
    }

    public static boolean check_file_exist(String filePath) {
        Log.i(TAG, "check_file_exist --- start ---");
        boolean ret = false;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File file = new File(filePath);

        if(file.exists()) {
            Log.i(TAG, "file exist");
            ret = true;
        }
        Log.i(TAG, "check_file_exist --- end ---");

        return ret;
    }

    public static boolean clear_record(String fileName) {
        boolean ret = true;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        //check folder
        File folder = new File(RootDirectory.getAbsolutePath() + "/.jamNow");

        if (folder.exists()) {
            File matchRecord = new File(folder+"/"+fileName);


            if (!matchRecord.exists()) {
                try {
                    ret = matchRecord.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!ret)
                    Log.e(TAG, "create "+matchRecord.getName()+" failed!");
            }

            //if exist, write empty string
            try {
                FileWriter fw = new FileWriter(matchRecord.getAbsolutePath());
                fw.write("");
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                ret = false;
            }



        } else {
            Log.e(TAG, "inside_folder not exits!");
            ret = false;
        }



        return ret;
    }

    public static boolean append_record(String message, String fileName) {
        Log.i(TAG, "append_record --- start ---");
        boolean ret = true;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }
        //check folder
        File folder = new File(RootDirectory.getAbsolutePath() + "/.jamNow");

        if(!folder.exists()) {
            Log.i(TAG, "folder not exist");
            ret = folder.mkdirs();
            if (!ret)
                Log.e(TAG, "append_message: failed to mkdir ");
        }

        //File file_txt = new File(folder+"/"+date_file_name);
        File file_txt = new File(folder+"/"+fileName);
        //if file is not exist, create!
        if(!file_txt.exists()) {
            Log.i(TAG, "file not exist");

            try {
                ret = file_txt.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!ret)
                Log.e(TAG, "append_record: failed to create file "+file_txt.getAbsolutePath());

        }

        try {
            FileWriter fw = new FileWriter(file_txt.getAbsolutePath(), true);
            fw.write(message);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }


        Log.i(TAG, "append_record --- end (success) ---");

        return ret;
    }

    public static String read_record(String fileName) {


        Log.i(TAG, "read_record() --- start ---");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File file = new File(RootDirectory.getAbsolutePath() + "/.jamNow/"+fileName);
        String message = "";

        //photo
        if (!file.exists())
        {
            Log.i(TAG, "read_record() "+file.getAbsolutePath()+ " not exist");

            return "";
        }
        else {
            try {

                FileReader fr = new FileReader(file.getAbsolutePath());
                BufferedReader br = new BufferedReader(fr);
                while (br.ready()) {

                    message = br.readLine();

                }
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "message = "+message);

            Log.i(TAG, "read_record() --- end ---");
        }


        return message;
    }

    public static String get_absolute_path(String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File file = new File(RootDirectory.getAbsolutePath() + "/.jamNow/"+fileName);

        return file.getAbsolutePath();
    }

    public static String copy_file(String pathName) {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File src_file = new File(pathName);
        File dest_file = new File(RootDirectory.getAbsolutePath() + "/.jamNow/" + src_file.getName());

        try {

            InputStream in = new FileInputStream(src_file);
            OutputStream out = new FileOutputStream(dest_file);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dest_file.getName();
    }

    public static boolean append_server(String fileName, String ipAddress, String port, String authName, String password) {
        Log.i(TAG, "append_server --- start ---");
        boolean ret = true;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }
        //check folder
        File folder = new File(RootDirectory.getAbsolutePath() + "/.jamNow/servers");

        if(!folder.exists()) {
            Log.i(TAG, "folder not exist");
            ret = folder.mkdirs();
            if (!ret)
                Log.e(TAG, "append_message: failed to mkdir ");
        }

        File file_txt;
        String message;
        if (fileName.equals("")) {
            file_txt = new File(folder+"/"+ipAddress);
            message = ipAddress+";"+fileName+";"+ipAddress+";"+port+";"+authName+";"+password;
        } else {
            file_txt = new File(folder+"/"+fileName);
            message = fileName+";"+fileName+";"+ipAddress+";"+port+";"+authName+";"+password;
        }


        //String message = fileName+";"+ipAddress+";"+port+";"+authName+";"+password;

        //if file is not exist, create!
        if(!file_txt.exists()) {
            Log.i(TAG, "file not exist");

            try {
                ret = file_txt.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!ret)
                Log.e(TAG, "append_record: failed to create file "+file_txt.getAbsolutePath());

        }



        try {
            FileWriter fw = new FileWriter(file_txt.getAbsolutePath(), false);
            fw.write(message);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }


        Log.i(TAG, "append_server --- end (success) ---");

        return ret;
    }

    public static RemoteServerItem load_file(String fileName) {
        RemoteServerItem item = null;

        if (fileName.length() > 0) {

            File folder = new File(RootDirectory.getAbsolutePath() + "/.jamNow/servers");

            File file_txt = new File(folder + "/" + fileName);

            String message = "";

            if (!file_txt.exists()) {
                Log.i(TAG, "read_last_message() " + file_txt.getAbsolutePath() + " not exist");
                return item;
            } else {
                try {

                    FileReader fr = new FileReader(file_txt.getAbsolutePath());
                    BufferedReader br = new BufferedReader(fr);
                    while (br.ready()) {

                        message = br.readLine();

                    }
                    fr.close();
                    Log.i(TAG, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "read_last_message() --- end ---");
            }


            if (!message.equals("")) {
                String msg_split[] = message.split(";");

                item = new RemoteServerItem(msg_split[0], msg_split[1], msg_split[2], msg_split[3], msg_split[4], msg_split[5]);
                /*item.setName(msg_split[0]);
                item.setUrlAddress(msg_split[1]);
                item.setPort(msg_split[2]);
                item.setAuthName(msg_split[3]);
                item.setPassword(msg_split[4]);*/
            }
        }

        return item;
    }

    public static boolean remove_server_file(String fileName) {
        boolean ret = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        File file = new File(RootDirectory.getAbsolutePath() + "/.jamNow/servers/"+fileName);

        if (file.exists()) {
            ret = file.delete();
        } else {
            Log.d(TAG, "file "+file.getName()+ " is not exist");
        }

        return ret;
    }

    public static ArrayList<RemoteServerItem> load_folder() {
        ArrayList<RemoteServerItem> remotServerList = new ArrayList<>();
        File file = new File(RootDirectory.getAbsolutePath() + "/.jamNow/servers");

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                RemoteServerItem item = load_file(child.getName());
                if (item != null) {
                    remotServerList.add(item);
                }
            }
        }


        return remotServerList;
    }
}
