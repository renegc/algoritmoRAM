/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Data;

/**
 *
 * @author othmane
 */
public class Location {
    double Abscisse, Ordinate;

    Location(double x, double y) {
        if (x < -90 || x > 90 || y < -180 || y > 180) 
            throw new IllegalArgumentException("Invalid coordinates. x and y must be within the valid range for earth's coordinates.");
        this.Abscisse = x;
        this.Ordinate = y;
    }

    public double getAbscisse() {
        return Abscisse;
    }

    public double getOrdinate() {
        return Ordinate;
    }

    public double getDistance(Location location) {
        // Haversine formula
        final double R = 6371; // Radius of the earth in km
        double lat1 = Math.toRadians(this.Abscisse);
        double lat2 = Math.toRadians(location.Abscisse);
        double dlat = Math.toRadians(location.Abscisse - this.Abscisse);
        double dlong = Math.toRadians(location.Ordinate - this.Ordinate);
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dlong / 2) * Math.sin(dlong / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
////         OSRM API base URL
//        String baseURL = "http://router.project-osrm.org/route/v1/driving/";
//
//        // Coordinates for this and the location location
//        String coordinates = this.Abscissa + "," + this.Ordinate + ";" + location.Abscissa + "," + location.Ordinate;
//
//        // Full URL for the request
//        String requestURL = baseURL + coordinates + "?overview=false";
//
//        // Open a connection to the OSRM API
//        URL url = new URL(requestURL);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//
//        // Read the response
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String inputLine;
//        StringBuilder response = new StringBuilder();
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//
//        // Parse the JSON response to get the distance
//        double distance = parseDistanceFromResponse(response.toString());
//
//        return distance;
    }
    
//    private double parseDistanceFromResponse(String jsonResponse) {
//        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
//        double distance = jsonObject.getAsJsonArray("routes")
//                .get(0).getAsJsonObject()
//                .get("distance").getAsDouble();
//        return distance;
//    }
    
    @Override
    public String toString() {
        return this.Abscisse + "," + this.Ordinate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) 
            return true;
        if (!(obj instanceof Location))
            return false;
        Location location = (Location) obj;
        return this.Abscisse == location.Abscisse && this.Ordinate == location.Ordinate;
    }

    public boolean notEquals(Object obj) {
        return !this.equals(obj);
    }
}
