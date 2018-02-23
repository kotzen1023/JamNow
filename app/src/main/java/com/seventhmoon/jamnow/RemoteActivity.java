package com.seventhmoon.jamnow;

import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.Toast;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.RemoteServerItem;
import com.seventhmoon.jamnow.Data.RemoteServerItemArrayAdapter;
import com.seventhmoon.jamnow.Data.SmbFileItem;
import com.seventhmoon.jamnow.Data.SmbFileItemArrayAdapter;
import com.seventhmoon.jamnow.Service.SearchSmbFileService;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;


import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

import static com.seventhmoon.jamnow.Data.FileOperation.append_server;
import static com.seventhmoon.jamnow.Data.FileOperation.load_file;
import static com.seventhmoon.jamnow.Data.FileOperation.load_folder;
import static com.seventhmoon.jamnow.Data.FileOperation.remove_server_file;
import static com.seventhmoon.jamnow.MainActivity.searchList;


public class RemoteActivity extends AppCompatActivity {
    private static final String TAG = RemoteActivity.class.getName();
    public static ArrayList<RemoteServerItem> remotServerList = new ArrayList<>();
    public static ArrayList<SmbFileItem> smbFileList = new ArrayList<>();

    public static RemoteServerItemArrayAdapter remoteServerItemArrayAdapter;
    private static SmbFileItemArrayAdapter smbFileItemArrayAdapter;

    private static BroadcastReceiver mReceiver = null;
    private static boolean isRegister = false;
    private static String current_select_filename="";
    private static int current_select_index = 0;
    private static String current_smb_root_path = "";
    private static String current_smb_path = "";
    private static String current_smb_folder = "";
    private static String current_auth_name = "";
    private static String current_auth_password = "";

    private GridView myGridview, smbGridView;
    MenuItem item_connect, menu_setting, menu_add_in;

    private static connectTask goodTask;
    private static boolean is_in_smb_folder = false;
    ActionBar actionBar;

    public static Deque<String> pathstack = new ArrayDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.remote_choose_activity);

        //for action bar
        actionBar = getSupportActionBar();



        remotServerList.clear();




        myGridview = findViewById(R.id.gridViewRemote);
        smbGridView= findViewById(R.id.gridViewSmhFiles);


        //load file
        remotServerList = new ArrayList<>(load_folder());
        remoteServerItemArrayAdapter = new RemoteServerItemArrayAdapter(this, R.layout.remote_choose_item, remotServerList);
        myGridview.setAdapter(remoteServerItemArrayAdapter);

        myGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RemoteServerItem item = (RemoteServerItem) parent.getItemAtPosition(position);

                if (item.isSelected()) {
                    Log.i(TAG, "selected -> unselected");
                    //selected[position] = false;
                    item.setSelected(false);
                    current_select_filename = "";
                    current_select_index = -1;
                    item_connect.setVisible(false);
                    //if (selected_count > 0)
                    //    selected_count--;

                } else {
                    Log.i(TAG, "unselected -> selected");
                    //selected[position] = true;
                    item.setSelected(true);
                    current_select_filename = item.getFilename();
                    current_select_index = position;
                    item_connect.setVisible(true);
                    /*if (item.getName() != null && !item.getName().equals("")) {
                        current_select_filename = item.getName();
                    } else {
                        current_select_filename = item.getUrlAddress();
                    }*/

                    Log.d(TAG, "current_select_filename = "+current_select_filename);
                    //selected_count++;
                }

                //deselect other
                for (int i=0; i<remotServerList.size(); i++) {

                    if (i != position) {
                        remotServerList.get(i).setSelected(false);

                    }
                }

                myGridview.invalidateViews();
            }
        });

        smbGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SmbFileItem item = (SmbFileItem) parent.getItemAtPosition(position);

                if (item.isSelected()) {
                    Log.i(TAG, "selected -> unselected");
                    item.setSelected(false);

                } else {
                    Log.i(TAG, "unselected -> selected");
                    item.setSelected(true);
                }

                if (item.getFileType() == 1) { //folder
                    current_smb_path = current_smb_path + item.getFileName();
                    Log.e(TAG, "current_smb_path = "+current_smb_path);

                    pathstack.push(current_smb_path);

                    Log.d(TAG, "=== stack start ===");
                    for (String s : pathstack) {
                        Log.e(TAG, "s = "+s);
                    }
                    Log.d(TAG, "=== stack  end  ===");

                    setTaskStart();
                }

                menu_add_in.setVisible(false);

                for (int i=0; i<smbFileList.size(); i++) {
                    if (smbFileList.get(i).isSelected() && smbFileList.get(i).getFileType() != 1) {
                        menu_add_in.setVisible(true);
                    }
                }


                smbGridView.invalidateViews();
            }
        });

        myGridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        IntentFilter filter;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() != null) {
                    if (intent.getAction().equalsIgnoreCase(Constants.ACTION.ADD_REMOTE_COMPLETE)) {
                        remoteServerItemArrayAdapter = new RemoteServerItemArrayAdapter(context, R.layout.remote_choose_item, remotServerList);
                        myGridview.setAdapter(remoteServerItemArrayAdapter);

                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.DELETE_REMOTE_COMPLETE)) {
                        remoteServerItemArrayAdapter.notifyDataSetChanged();
                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.MODIFY_REMOTE_COMPLETE)) {
                        remoteServerItemArrayAdapter.notifyDataSetChanged();
                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.GET_SMB_FILELIST_COMPLETE)) {
                        myGridview.setVisibility(View.GONE);
                        smbGridView.setVisibility(View.VISIBLE);

                        if (smbFileItemArrayAdapter == null) {

                            smbFileItemArrayAdapter = new SmbFileItemArrayAdapter(context, R.layout.smb_file_item, smbFileList);
                            smbGridView.setAdapter(smbFileItemArrayAdapter);
                        } else {
                            smbFileItemArrayAdapter.notifyDataSetChanged();
                        }

                        is_in_smb_folder = true;
                        item_connect.setVisible(false);
                        menu_setting.setVisible(false);

                        Log.e(TAG, "current path = "+remotServerList.get(current_select_index).getUrlAddress());

                        actionBar.setTitle(current_smb_path);

                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.SMB_CONNECT_FAILED)) {
                        toast(getResources().getString(R.string.smb_connect_failed));
                        item_connect.setVisible(true);
                        menu_add_in.setVisible(false);
                    } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.SMB_LIST_CLEAR)) {
                        if (smbFileItemArrayAdapter != null) {
                            smbFileItemArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }


            }
        };



        if (!isRegister) {
            filter = new IntentFilter();
            filter.addAction(Constants.ACTION.ADD_REMOTE_COMPLETE);
            filter.addAction(Constants.ACTION.DELETE_REMOTE_COMPLETE);
            filter.addAction(Constants.ACTION.MODIFY_REMOTE_COMPLETE);
            filter.addAction(Constants.ACTION.GET_SMB_FILELIST_COMPLETE);
            filter.addAction(Constants.ACTION.SMB_CONNECT_FAILED);

            registerReceiver(mReceiver, filter);
            isRegister = true;
            Log.d(TAG, "registerReceiver mReceiver");
        }

        //smb
        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        System.setProperty("jcifs.smb.client.soTimeout", "1000000");
        System.setProperty("jcifs.smb.client.responseTimeout", "30000");

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");

        if (isRegister && mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            isRegister = false;
            mReceiver = null;
        }

        remoteServerItemArrayAdapter = null;
        smbFileItemArrayAdapter = null;

        super.onDestroy();

    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }
    @Override
    public void onResume() {

        Log.i(TAG, "onResume");



        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if (is_in_smb_folder) {

            if (pathstack.isEmpty()) {
                smbGridView.setVisibility(View.GONE);
                myGridview.setVisibility(View.VISIBLE);
                is_in_smb_folder = false;
                item_connect.setVisible(true);
                menu_setting.setVisible(true);
                current_smb_path = "";
                pathstack.clear();
            } else {
                pathstack.pop();

                Log.d(TAG, "=== stack start ===");
                for (String s : pathstack) {
                    Log.e(TAG, "s = "+s);
                }
                Log.d(TAG, "=== stack  end  ===");

                if (pathstack.isEmpty()) {

                    smbGridView.setVisibility(View.GONE);
                    myGridview.setVisibility(View.VISIBLE);
                    is_in_smb_folder = false;
                    item_connect.setVisible(true);
                    menu_setting.setVisible(true);
                    current_smb_path = "";
                    pathstack.clear();
                    actionBar.setTitle(R.string.app_name);
                } else {
                    current_smb_path = pathstack.peek();
                    setTaskStart();
                }


            }


        } else {

            finish();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remote_menu, menu);


        item_connect = menu.findItem(R.id.action_link);
        menu_setting = menu.findItem(R.id.action_settings);
        menu_add_in = menu.findItem(R.id.action_add_in);

        item_connect.setVisible(false);
        menu_add_in.setVisible(false);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        switch (item.getItemId()) {
            case R.id.action_refresh:
                //showInputDialog();


                //intent = new Intent(MainActivity.this, FileChooseActivity.class);
                //startActivity(intent);
                break;

            //case R.id.action_link:
            //    showInputDialog();

            //    break;


            case R.id.action_add:
                showInputDialog(0); //add
                break;

            case R.id.action_remove:
                remove_server_file(current_select_filename);
                remotServerList.remove(current_select_index);

                Intent intent = new Intent(Constants.ACTION.DELETE_REMOTE_COMPLETE);
                sendBroadcast(intent);

                break;

            case R.id.action_modify:
                if (!current_select_filename.equals(""))
                    showInputDialog(1);



                break;
            case R.id.action_link:
                item_connect.setVisible(false);

                current_smb_path = remotServerList.get(current_select_index).getUrlAddress();
                pathstack.push(current_smb_path);

                Log.d(TAG, "=== stack start ===");
                for (String s : pathstack) {
                    Log.e(TAG, "s = "+s);
                }
                Log.d(TAG, "=== stack  end  ===");

                setTaskStart();

                break;
            case R.id.action_add_in:
                searchList.clear();



                for (int i=0; i<smbFileList.size(); i++) {
                    if (smbFileList.get(i).isSelected()) {
                        searchList.add(smbFileList.get(i).getFilePath());
                    }
                }

                Log.d(TAG, "search list:");
                for (int j =0; j<searchList.size(); j++) {
                    Log.e(TAG, "path = "+searchList.get(j));
                }

                Intent saveintent = new Intent(RemoteActivity.this, SearchSmbFileService.class);
                saveintent.setAction(Constants.ACTION.GET_SEARCHLIST_ACTION);
                saveintent.putExtra("AUTH", current_auth_name);
                saveintent.putExtra("PASSWORD", current_auth_password);
                startService(saveintent);

                //searchFiles();
                finish();

                break;


        }



        return true;
    }

    protected void showInputDialog(final int code) {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        View promptView = View.inflate(RemoteActivity.this, R.layout.remote_config_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RemoteActivity.this);
        alertDialogBuilder.setView(promptView);

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        //final EditText editUrlInput = promptView.findViewById(R.id.editUrlAddress);
        final EditText name = promptView.findViewById(R.id.serverName);
        final EditText address = promptView.findViewById(R.id.serverAddress);
        final EditText port = promptView.findViewById(R.id.serverPort);
        final EditText account = promptView.findViewById(R.id.serverAccount);
        final EditText password = promptView.findViewById(R.id.serverPwd);
        final EditText passwordRe = promptView.findViewById(R.id.serverPwdRe);
        //String original_file_name = "";



        if (code == 1) {
            Log.d(TAG, "modify");

            if (!current_select_filename.equals("")) {

                RemoteServerItem item = load_file(current_select_filename);

                //original_file_name = item.getFilename();

                if (item != null) {
                    name.setText(item.getName());
                    address.setText(item.getUrlAddress());
                    port.setText(item.getPort());
                    account.setText(item.getAuthName());
                    password.setText(item.getPassword());
                    passwordRe.setText(item.getPassword());
                }

            }

        } else {
            address.setText("smb://");
            port.setText("139");
            Log.d(TAG, "add");
        }

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //resultText.setText("Hello, " + editText.getText());
                //Log.e(TAG, "input password = " + editText.getText());




                if (password.getText().equals(passwordRe.getText())) {

                    if (code == 1) { //modify
                        remove_server_file(current_select_filename);
                        remotServerList.remove(current_select_index);
                    }

                    RemoteServerItem item;
                    append_server(name.getText().toString(), address.getText().toString(), port.getText().toString(), account.getText().toString(), password.getText().toString());

                    if (name.getText() != null && name.getText().length() > 0) {
                        item = new RemoteServerItem(name.getText().toString(), name.getText().toString(), address.getText().toString(), port.getText().toString(), account.getText().toString(), password.getText().toString());
                    } else {
                        item = new RemoteServerItem(address.getText().toString(), name.getText().toString(), address.getText().toString(), port.getText().toString(), account.getText().toString(), password.getText().toString());
                    }


                    remotServerList.add(item);

                    Intent intent = new Intent(Constants.ACTION.ADD_REMOTE_COMPLETE);
                    sendBroadcast(intent);
                } else {

                }



            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.show();
    }

    public void toast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private class connectTask extends AsyncTask<Integer, Integer, String>
    {
        @Override
        protected String doInBackground(Integer... countTo) {

            RemoteServerItem item1 = remotServerList.get(current_select_index);

            current_auth_name = item1.getAuthName();
            current_auth_password = item1.getPassword();

            try{
                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, item1.getAuthName(), item1.getPassword());

                Log.e(TAG, "connectTask path = "+current_smb_path);

                SmbFile dir = new SmbFile(current_smb_path, auth);
                /*if (pathstack.isEmpty()) {
                    Log.e(TAG, "path stack is empty");
                    dir = new SmbFile(item1.getUrlAddress(), auth);
                    pathstack.push(item1.getUrlAddress());
                } else {
                    //String new_path = pathstack.peek()+current_smb_folder;
                    Log.e(TAG, "current_smb_path = "+current_smb_path);
                    dir = new SmbFile(current_smb_path, auth);
                    //pathstack.push(dir.getPath());
                }*/


                //SmbFile dir = new SmbFile(item1.getUrlAddress(), auth);
                //UniAddress mDomain = UniAddress.getByName(item1.getUrlAddress());
                //NtlmPasswordAuthentication mAuthentication = new NtlmPasswordAuthentication(item1.getUrlAddress(), item1.getAuthName(), item1.getPassword());
                //SmbSession.logon(mDomain, mAuthentication);

                //String rootPath = "smb://" + item1.getUrlAddress() + "/";
                //SmbFile mRootFolder;

                //mRootFolder = new SmbFile(rootPath, mAuthentication);

                //SmbFile[] files;
                //files = mRootFolder.listFiles();
                smbFileList.clear();
                Intent intent = new Intent(Constants.ACTION.SMB_LIST_CLEAR);
                sendBroadcast(intent);


                for (SmbFile smbFile : dir.listFiles()) {
                    SmbFileItem item = new SmbFileItem();

                    Log.e(TAG, "file: "+smbFile.getName());
                    Log.e(TAG, "file: "+smbFile.getPath());

                    item.setFileName(smbFile.getName());
                    item.setFilePath(smbFile.getPath());

                    if (smbFile.isDirectory()) {
                        item.setFileType(1);
                    } else {
                        item.setFileType(0);
                    }

                    smbFileList.add(item);

                }

            } catch(Exception e){
                this.cancel(true);
                Intent intent = new Intent(Constants.ACTION.SMB_CONNECT_FAILED);
                sendBroadcast(intent);

                e.printStackTrace();
            }

            //while(current_state == STATE.Started) {
            /*while(isVideoPlayPress) {


                if (getPosition() > ab_loop_video_end) {
                    Log.d(TAG, "position = " + getPosition() + " ab_loop_video_start = " + ab_loop_video_start + " ab_loop_end = " + ab_loop_video_end);
                    //mediaPlayer.pause();
                    //current_state = Constants.STATE.Paused;
                    //Log.e(TAG, "==>0");
                    //if (mediaPlayer != null && current_state == Constants.STATE.Started) {
                    //    mediaPlayer.seekTo(ab_loop_start);
                    //}
                    //Log.e(TAG, "==>1");
                    setSeekTo(ab_loop_video_start);
                }


                try {

                    //long percent = 0;
                    //if (Data.current_file_size > 0)
                    //    percent = (Data.complete_file_size * 100)/Data.current_file_size;
                    if (videoView != null && isVideoviewPlaying()) {
                        int position = ((getPosition() * 1000) / current_video_duration);
                        publishProgress(position);
                    }


                    Thread.sleep(100);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }*/

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            Log.d(TAG, "=== connectTask onPreExecute");


        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);


            /*NumberFormat f = new DecimalFormat("00");
            NumberFormat f2 = new DecimalFormat("000");


            int minutes = (mediaPlayer.getCurrentPosition()/60000);

            int seconds = (mediaPlayer.getCurrentPosition()/1000) % 60;

            int minisec = (mediaPlayer.getCurrentPosition()%1000);

            songDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));*/

            //setActionBarTitle(mediaPlayer.getCurrentPosition());

            /*if (values[0] >= 1000) {
                seekBar.setProgress(1000);
                if (videoView != null && isVideoviewPlaying())
                    setVideoDuration(getPosition());
            } else {

                seekBar.setProgress(values[0]);
                if (videoView != null && isVideoviewPlaying())
                    setVideoDuration(getPosition());
            }*/

            // 背景工作處理"中"更新的事
            /*long percent = 0;
            if (Data.current_file_size > 0)
                percent = (Data.complete_file_size * 100)/Data.current_file_size;

            decryptDialog.setMessage(getResources().getString(R.string.photolist_decrypting_files) + "(" + values[0] + "/" + selected_names.size() + ") " + percent + "%\n" + selected_names.get(values[0] - 1));
            */
            /*if (Data.OnDecompressing) {
                loadDialog.setTitle(getResources().getString(R.string.decompressing_files_title) + " " + Data.CompressingFileName);
                loadDialog.setProgress(values[0]);
            } else if (Data.OnDecrypting) {
                loadDialog.setTitle(getResources().getString(R.string.decrypting_files_title) + " " + Data.EnryptingOrDecryptingFileName);
                loadDialog.setProgress(values[0]);
            } else {
                loadDialog.setMessage(getResources().getString(R.string.decrypting_files_title));
            }*/

        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            Log.d(TAG, "=== onPostExecute ===");


            //seekBar.setProgress(0);

            //taskDone = true;

            //loadDialog.dismiss();
            /*btnDecrypt.setVisibility(View.INVISIBLE);
            btnShare.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            selected_count = 0;*/

            //toast(getResources().getString(R.string.smb_connect_success));

            goodTask = null;
            Intent intent = new Intent(Constants.ACTION.GET_SMB_FILELIST_COMPLETE);
            sendBroadcast(intent);
        }

        @Override
        protected void onCancelled() {

            super.onCancelled();

            Log.d(TAG, "=== onCancelled ===");
            goodTask = null;
        }
    }

    public void setTaskStart() {
        Log.d(TAG, "setTaskStart");
        if (goodTask == null) {
            goodTask = new connectTask();
            goodTask.execute(10);
        }
    }

    public void setTaskStop() {
        Log.d(TAG, "setTaskStop");
        if (goodTask != null && !goodTask.isCancelled()) {
            goodTask.cancel(true);
            goodTask = null;
        }
    }
}
