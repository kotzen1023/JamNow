package com.seventhmoon.jamnow;

import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.RemoteServerItem;
import com.seventhmoon.jamnow.Data.RemoteServerItemArrayAdapter;


import java.util.ArrayList;



public class RemoteActivity extends AppCompatActivity {
    private static final String TAG = RemoteActivity.class.getName();
    public static ArrayList<RemoteServerItem> remotServerList = new ArrayList<>();

    public static RemoteServerItemArrayAdapter remoteServerItemArrayAdapter;

    private static BroadcastReceiver mReceiver = null;
    private static boolean isRegister = false;

    private GridView myGridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.remote_choose_activity);



        remotServerList.clear();



        myGridview = findViewById(R.id.gridViewRemote);

        myGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RemoteServerItem item = (RemoteServerItem) parent.getItemAtPosition(position);

                if (item.isSelected()) {
                    Log.i(TAG, "selected -> unselected");
                    //selected[position] = false;
                    item.setSelected(false);
                    //if (selected_count > 0)
                    //    selected_count--;

                } else {
                    Log.i(TAG, "unselected -> selected");
                    //selected[position] = true;
                    item.setSelected(true);
                    //selected_count++;
                }

                myGridview.invalidateViews();
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
                    }
                }


            }
        };



        if (!isRegister) {
            filter = new IntentFilter();
            filter.addAction(Constants.ACTION.ADD_REMOTE_COMPLETE);
            filter.addAction(Constants.ACTION.DELETE_REMOTE_COMPLETE);
            filter.addAction(Constants.ACTION.MODIFY_REMOTE_COMPLETE);


            registerReceiver(mReceiver, filter);
            isRegister = true;
            Log.d(TAG, "registerReceiver mReceiver");
        }
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

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remote_menu, menu);





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
                showInputDialog();
                break;

            case R.id.action_remove:




                break;

            case R.id.action_modify:




                break;


        }



        return true;
    }

    protected void showInputDialog() {

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



        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //resultText.setText("Hello, " + editText.getText());
                //Log.e(TAG, "input password = " + editText.getText());
                RemoteServerItem item = new RemoteServerItem(name.getText().toString(), address.getText().toString(), port.getText().toString(), account.getText().toString(), password.getText().toString());


                remotServerList.add(item);

                Intent intent = new Intent(Constants.ACTION.ADD_REMOTE_COMPLETE);
                sendBroadcast(intent);



            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.show();
    }


}
