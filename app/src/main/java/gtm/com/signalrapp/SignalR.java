package gtm.com.signalrapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler;

/**
 * Created by SOPORTE on 22/12/2015.
 */
public class SignalR extends BroadcastReceiver {
    private Context context;
    private HubConnection connection;
    private ArrayList<HubProxy> hubs;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean timerEnabled = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.connect();
    }

    public SignalR(Context context) {
        this.context = context;
        this.connection = null;
        this.hubs = new ArrayList<>();
    }

    public SignalR(Context context, HubConnection connection) {
        this.context = context;
        this.connection = connection;
        this.hubs = new ArrayList<>();
    }

    public HubConnection getConnection() {
        return this.connection;
    }

    public void setConnection(HubConnection connection) {
        this.connection = connection;
    }

    public ArrayList<HubProxy> getHubs() {
        return this.hubs;
    }

    public boolean addHub(String hubName) {
        boolean flag = false;
        try {
            if (this.connection != null) {
                this.hubs.add(this.connection.createHubProxy(hubName));
                flag = true;
            }
        } catch (Exception ex) {
            flag = false;
        } finally {
            return flag;
        }
    }

    public boolean addHub(HubProxy hub) {
        boolean flag = false;
        try {
            this.hubs.add(hub);
            flag = true;
        } catch (Exception ex) {
            flag = false;
        } finally {
            return flag;
        }
    }

    public boolean addHub(String hubName, String eventName) {
        boolean flag = false;
        HubProxy hub;
        try {
            if (this.connection != null) {
                hub = this.connection.createHubProxy(hubName);
                hub.subscribe(eventName);
                this.hubs.add(hub);
                flag = true;
            }
        } catch (Exception ex) {
            flag = false;
        } finally {
            hub = null;
            return flag;
        }
    }

    public boolean addHub(HubProxy hub, String eventName) {
        boolean flag = false;
        try {
            hub.subscribe(eventName);
            this.hubs.add(hub);
            flag = true;
        } catch (Exception ex) {
            flag = false;
        } finally {
            return flag;
        }
    }

    public boolean addHub(String hubName, Object subscriberHandler) {
        boolean flag = false;
        HubProxy hub;
        try {
            if (this.connection != null) {
                hub = this.connection.createHubProxy(hubName);
                hub.subscribe(subscriberHandler);
                this.hubs.add(hub);
                flag = true;
            }
        } catch (Exception ex) {
            flag = false;
        } finally {
            hub = null;
            return flag;
        }
    }

    public boolean addHub(HubProxy hub, Object subscriberHandler) {
        boolean flag = false;
        try {
            hub.subscribe(subscriberHandler);
            this.hubs.add(hub);
            flag = true;
        } catch (Exception ex) {
            flag = false;
        } finally {
            return flag;
        }
    }

    public boolean addHub(String hubName, String eventName, SubscriptionHandler handler) {
        boolean flag = false;
        HubProxy hub;
        try {
            if (this.connection != null) {
                hub = this.connection.createHubProxy(hubName);
                hub.on(eventName, handler);
                this.hubs.add(hub);
                flag = true;
            }
        } catch (Exception ex) {
            flag = false;
        } finally {
            hub = null;
            return flag;
        }
    }

    public boolean addHub(HubProxy hub, String eventName, SubscriptionHandler handler) {
        boolean flag = false;
        try {
            hub.on(eventName, handler);
            this.hubs.add(hub);
            flag = true;
        } catch (Exception ex) {
            flag = false;
        } finally {
            return flag;
        }
    }

    public boolean initialize(final Handler handler) {
        if (this.connection != null) {
            this.connection.closed(
                    new Runnable() {
                        @Override
                        public void run() {
                            Message msg = handler.obtainMessage();
                            msg.obj = "|" + sdf.format(new Date()) + "| --> Conexión Cerrada.";
                            handler.sendMessage(msg);
                            timerEnabled = true;
                            SignalR.this.startTimer();
                        }
                    }
            );
            this.connection.connected(
                    new Runnable() {
                        @Override
                        public void run() {
                            Message msg = handler.obtainMessage();
                            msg.obj = "|" + sdf.format(new Date()) + "| --> Conexión Establecida.";
                            handler.sendMessage(msg);
                            timerEnabled = false;
                            SignalR.this.stopTimer();
                        }
                    }
            );
            this.connection.reconnecting(
                    new Runnable() {
                        @Override
                        public void run() {
                            Message msg = handler.obtainMessage();
                            msg.obj = "|" + sdf.format(new Date()) + "| --> Intentando Reestablecer la Conexión.";
                            handler.sendMessage(msg);
                        }
                    }
            );
            this.connection.reconnected(
                    new Runnable() {
                        @Override
                        public void run() {
                            Message msg = handler.obtainMessage();
                            msg.obj = "|" + sdf.format(new Date()) + "| --> Conexión Reestablecida.";
                            handler.sendMessage(msg);
                            timerEnabled = false;
                            SignalR.this.stopTimer();
                        }
                    }
            );
            this.connection.connectionSlow(
                    new Runnable() {
                        @Override
                        public void run() {
                            Message msg = handler.obtainMessage();
                            msg.obj = "|" + sdf.format(new Date()) + "| --> Conexión Lenta.";
                            handler.sendMessage(msg);
                        }
                    }
            );
        }
        return connect();
    }

    private boolean connect() {
        boolean flag;
        try {
            this.connection.start().get();
            flag = true;
        } catch (InterruptedException e) {
            flag = false;
        } catch (ExecutionException e) {
            flag = false;
        }
        return flag;
    }

    public void finalize() {
        try {
            this.connection.stop();
        } catch (Exception ex) {
        }
    }

    private void startTimer() {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, SignalR.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 10 * 1, pi); // Millisec * Second * Minute
    }

    private void stopTimer() {
        Intent intent = new Intent(context, SignalR.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
