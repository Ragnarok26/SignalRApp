package gtm.com.signalrapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;

import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;

public class MainActivity extends AppCompatActivity {
    private SignalR manager;
    private TextView txt;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                //MainActivity.this.updateClientesSource();
            } catch (Exception ex) { }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = (TextView)findViewById(R.id.txt);
        txt.setText("");
        Platform.loadPlatformComponent(new AndroidPlatformComponent());
        String host = "http://www.star911.com.mx/IOProject";
        HubConnection connection = new HubConnection(host);
        HubProxy hub = connection.createHubProxy("at5");
        /*hub.on("updatePulsos",
                new SubscriptionHandler() {
                    @Override
                    public void run() {

                    }
                }
        );*/
        hub.subscribe(this);
        /*hub.on("updatePulsos",
                new SubscriptionHandler1<Object>() {
                    @Override
                    public void run(final Object msg) {
                        final String finalMsg;
                        Gson gson = new Gson();
                        Object object = gson.fromJson(msg.toString(), AT5DataSource.class);
                        Field[] fields = object.getClass().getDeclaredFields();
                        for (int i = 0; i < fields.length; i++) {
                            try {
                                System.out.println("Value = " + fields[i].get(object));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                , Object.class);*/
        manager = new SignalR(this, connection);
        manager.addHub(hub);
        manager.initialize(
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    try {
                        String message = (String)msg.obj;
                        MainActivity.this.txt.setText(message);
                    } catch (Exception ex) { }
                }
            }
        );
    }

    public void updatePulsos(Object msg) {
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        txt.setText("Clientes Actualizados " + sdf.format(new Date()) + "!");*/
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(msg.toString()));
        reader.setLenient(true);
        Type type = new TypeToken<AT5Pulsos[]>() { }.getType();
        AT5Pulsos[] datos;
        try {
            datos = gson.fromJson(reader, type);
        } catch (Exception ex) {
            datos = null;
        } finally {
            gson = null;
            reader = null;
            type = null;
            msg = null;
        }
    }

    @Override
    public void finish() {
        try {
            manager.finalize();
        } catch (Exception ex) {
        } finally {
            manager = null;
            super.finish();
        }
    }
}
