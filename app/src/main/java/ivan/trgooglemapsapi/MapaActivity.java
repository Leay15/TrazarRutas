package ivan.trgooglemapsapi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerDragListener {

    private SupportMapFragment mapFragment;

    public double latActual,lonActual;
    public String destino;
    public double latDestino,lonDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        Bundle b = this.getIntent().getExtras();
        destino = b.getString("Destino");
        latActual = b.getDouble("Latitud");
        lonActual = b.getDouble("Longitud");
        latDestino = b.getDouble("latDestino");
        lonDestino = b.getDouble("lonDestino");


        mapFragment = MapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);
    }

    GoogleMap mGoogleMap;
    ArrayList<LatLng> coordenadas = new ArrayList<>();
    ArrayList<Marker> marcadores = new ArrayList<>();
    LatLng markInicial=null,markFinal=null;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mGoogleMap.clear();
                if(markFinal==null){
                    markFinal=latLng;
                    Marker m = mGoogleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                    coordenadas.add(latLng);
                }else{
                    markInicial=markFinal;
                    markFinal=latLng;
                    String url = obtenerDireccionesURL(markInicial,markFinal);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                }

            }
        });
        mGoogleMap.setOnMarkerDragListener(this);
        LatLng lugarActual = new LatLng(19.691661, -100.552225);

        //LatLng lugarDestino = new LatLng(latDestino,lonDestino);

        /*googleMap.addMarker(new MarkerOptions()
                .position(lugarActual)
                .title("Usted Esta Aqui"));
        */
        //googleMap.addMarker(new MarkerOptions()
            //    .position(lugarDestino)
            //    .title(destino));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(lugarActual)
                .zoom(13)
                .build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //Metodo de construccion del URL para el API
    private String obtenerDireccionesURL(LatLng origin,LatLng dest){

        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        String sensor = "sensor=false";

        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    Marker cordAux;
    @Override
    public void onMarkerDragStart(Marker marker) {
        cordAux= marker;
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng corUpdt = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        if(markInicial!=null) {
            int pos = marcadores.indexOf(marker);
            coordenadas.set(pos, corUpdt);

            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.addAll(coordenadas);
            mGoogleMap.clear();
            agregarMarcadores();
            mGoogleMap.addPolyline(lineOptions);
        }else{
            markFinal=corUpdt;
        }
    }

    private void agregarMarcadores() {
        marcadores.clear();
        for (LatLng l:coordenadas) {
            Marker m = mGoogleMap.addMarker(new MarkerOptions().position(l).draggable(true));
            marcadores.add(m);
        }
    }

    //Descarga de Datos de la API por proceso
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);
        }
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creamos una conexion http
            urlConnection = (HttpURLConnection) url.openConnection();

            // Conectamos
            urlConnection.connect();

            // Leemos desde URL
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){

        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    //Leer el JSON que nos regresa el API
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for(int i=0;i<result.size();i++){
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    coordenadas.add(position);
                }

                lineOptions.addAll(coordenadas);
                lineOptions.width(10);
                lineOptions.color(Color.rgb(0,0,255));
            }
            if(lineOptions!=null) {
                agregarMarcadores();
                mGoogleMap.addPolyline(lineOptions);
            }
        }
    }
}
