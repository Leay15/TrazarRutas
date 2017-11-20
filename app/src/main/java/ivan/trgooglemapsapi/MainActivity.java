package ivan.trgooglemapsapi;

import android.Manifest;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LocationListener{

    LocationManager locationManager;
    public ArrayList<Lugares> listaLugares = new ArrayList<>();
    public ArrayList<String> nombreLugares = new ArrayList<>();
    public TextView txViewLocalizacion;
    public ListView listViewLugares;
    public double latActual,lonActual;
    public String destino;
    public double latDestino,lonDestino;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txViewLocalizacion=(TextView)findViewById(R.id.txUbicacionActual);
        listViewLugares=(ListView)findViewById(R.id.listViewLugares);

        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        cargarLugares();
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,nombreLugares);
        listViewLugares.setAdapter(adaptador);
        obtenerLocalizacion();

        listViewLugares.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                abrirMapa(i);
            }
        });
        abrirMapa(0);


    }

    private void abrirMapa(int i) {
        Lugares l = listaLugares.get(i);
        destino = l.lugar;
        latDestino=l.lat;
        lonDestino=l.lon;

        Intent intent = new Intent(MainActivity.this,MapaActivity.class);
        intent.putExtra("Destino",destino);
        intent.putExtra("Latitud",latActual);
        intent.putExtra("Longitud",lonActual);
        intent.putExtra("latDestino",latDestino);
        intent.putExtra("lonDestino",lonDestino);

        startActivity(intent);
    }

    private void cargarLugares() {
        Lugares l = new Lugares("Tec. Cd. Hidalgo",19.7090368,-100.5174843);
        Lugares l1 = new Lugares("Cinepolis Cd. Hidalgo",19.682987,-100.546428);
        Lugares l2 = new Lugares("Tacos Kikis",19.686047, -100.5596730);
        Lugares l3 = new Lugares("Bancomer Cd Hidalgo",19.6902836,-100.5574211);
        listaLugares.add(l);
        nombreLugares.add(l.lugar);
        listaLugares.add(l1);
        listaLugares.add(l2);
        listaLugares.add(l3);
        nombreLugares.add(l1.lugar);
        nombreLugares.add(l2.lugar);
        nombreLugares.add(l3.lugar);



    }

    private void obtenerLocalizacion() {
        if(gpsStatus()){
            long time=500;
            float minDistance=10;

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,time,minDistance,this);
        }else{
            AlertDialog.Builder alerta = new AlertDialog.Builder(this);
            alerta.setTitle("GPS Desactivado");
            alerta.setCancelable(false);
            alerta.setPositiveButton("Activar GPS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialogInterface.cancel();

                }
            });
            alerta.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alerta.create();
            alerta.show();
        }
    }

    public boolean gpsStatus(){
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gps = Settings.Secure.isLocationProviderEnabled(contentResolver,LocationManager.GPS_PROVIDER);
        return gps;
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        txViewLocalizacion.setText("");
        latActual=location.getLatitude();
        lonActual=location.getLongitude();
        txViewLocalizacion.setText("Lat: " +String.valueOf(location.getLatitude()) + " Lon: " +
                String.valueOf(location.getLongitude()));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

