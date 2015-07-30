package com.tacc.eddie.smartlab;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

// used for execution of linux commands remotely via SSH

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class VislabActivity extends Activity {

    private CardScrollView mCardScroller;
    private View mView;

    JSch jsch;
    Session session;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        mView = buildView();

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return mView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mView;
            }

            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        });
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.DISALLOWED);
            }
        });
        setContentView(mCardScroller);

        new AsyncTask<Integer, Void, Void>(){
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    createSshSession("user", "ip", "password", 22);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearHistory();
        super.onDestroy();
    }

    private void clearHistory() {
        DataManager dataManager = new DataManager(getBaseContext());
        List<String> storedStrings = dataManager.getStoredStrings();
        storedStrings.clear();
        dataManager.setStoredStrings(new ArrayList<>(storedStrings));
        mView = buildView();
        mCardScroller.getAdapter().notifyDataSetChanged();
    }

    private View buildView() {
        // create a new CardBuilder with a Text layout
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);

        // get the stored strings from the DataManager
        DataManager dataManager = new DataManager(getBaseContext());
        ArrayList<String> strings = dataManager.getStoredStrings();

        // instantiate a new StringBuilder instance to create the card’s text
        StringBuilder builder = new StringBuilder();
        if (strings.size() == 0){
            // nothing has been done so just wait nicely
            builder.append("How can I help you?");
            //  create a bulleted list comprised of each string in the command history list by adding a hyphen, the item and then a new line.
        } else {
            builder.append("Command History:\n");
            for (String s : strings) {
                builder.append("- ").append(s).append("\n");
            }
        }

        //  set the CardBuilder’s text to the string you just created, and return the CardBuilder’s view
        card.setText(builder.toString());
        return card.getView();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS){
            getMenuInflater().inflate(R.menu.activity_menu, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        // check to make sure that the feature ID is for voice commands
        // this means the menu item was selected from the “OK Glass” menu
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            DataManager dataManager = new DataManager(getBaseContext());
            ArrayList<String> storedStrings = dataManager.getStoredStrings();

            switch (item.getItemId()) {
                case R.id.stallion_on:
                    new AsyncTask<Integer, Void, Void>(){
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {
                                List<String> commands = new ArrayList<String>();
                                commands.add("stallion_on");
                                executeRemoteCommand(commands);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute(1);

                    // add recently executed command to history list
                    storedStrings.add("Turn On Stallion");
                    dataManager.setStoredStrings(storedStrings);

                    break;

                case R.id.stallion_off:
                    new AsyncTask<Integer, Void, Void>(){
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {
                                List<String> commands = new ArrayList<String>();
                                commands.add("ps aux | grep stallion_on | grep -v grep | awk '{ print $2 }' | xargs kill -9");
                                commands.add("stallion_off");
                                executeRemoteCommand(commands);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute(1);

                    // add recently executed command to history list
                    storedStrings.add("Turn Off Stallion");
                    dataManager.setStoredStrings(storedStrings);

                    break;

                case R.id.start_dc:
                    new AsyncTask<Integer, Void, Void>(){
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {
                                List<String> commands = new ArrayList<String>();
                                commands.add("export DISPLAY=:0.0");
                                commands.add("startdisplaycluster");
                                executeRemoteCommand(commands);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute(1);

                    // add recently executed command to history list
                    storedStrings.add("Start DisplayCluster");
                    dataManager.setStoredStrings(storedStrings);

                    break;

                case R.id.start_vnc:
                    new AsyncTask<Integer, Void, Void>(){
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {
                                List<String> commands = new ArrayList<String>();
                                commands.add("displayclusterVNC");
                                executeRemoteCommand(commands);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute(1);

                    // add recently executed command to history list
                    storedStrings.add("Start VNC Server");
                    dataManager.setStoredStrings(storedStrings);

                    break;
                default:
                    return true;
            }

            // update the view to show the new history
            mView = buildView();
            mCardScroller.getAdapter().notifyDataSetChanged();

            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void createSshSession(String username, String hostname, String password, int port) {
        try {
            jsch = new JSch();
            session = jsch.getSession(username, hostname, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // Avoid asking for key confirmation
        } catch (Exception e) {
            System.err.println("ERROR: Connecting via shell to " + hostname);
            e.printStackTrace();
        }
    }

    public void executeRemoteCommand(List<String> commands) {

        try {
            session.connect();

            // SSH Channel
            Channel channel = session.openChannel("shell");
            OutputStream ops = channel.getOutputStream();
            PrintStream ps = new PrintStream(ops, true);
            channel.connect();

            // run commands
            for(String command : commands) {
                ps.println(command);
            }
            ps.close();

            // print command output to terminal
            InputStream in=channel.getInputStream();
            byte[] tmp=new byte[1024];
            while(true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
