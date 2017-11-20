package ivan.trgooglemapsapi;

/**
 * Created by ivan_ on 05/11/2017.
 */

public class Lugares {
    public String lugar;
    public double lat,lon;

    public Lugares(String lugar, double latitud,double longitud){
        this.lugar=lugar;
        this.lat=latitud;
        this.lon=longitud;
    }
}
