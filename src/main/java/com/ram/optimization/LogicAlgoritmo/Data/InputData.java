package com.ram.optimization.LogicAlgoritmo.Data;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.ram.optimization.Google.GoogleCloudStorageService;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
/**
 * This class represents the input data for the MDVRP problem.
 *
 * @author othmane
 */
public class InputData implements AutoCloseable {

    final double VehicleSpeed = 1d; // km/min
    public int OrdersCount;
    public int VehiclesCount;
    public int AllStopsCount;
    public double[][] DistanceMatrix;
    public List<Location> Depots;
    public Map<Integer, Order> Orders;
    public Map<Integer, Vehicle> Vehicles = new HashMap<>();
//    public Map<String, Boolean> Constraints = new HashMap<>();

    /**
     * Returns a string representation of the InputData object.
     *
     * @return The string representation.
     */
    /**
     * Constructs an InputData object and initializes it with data from the
     * input files.
     */
    public InputData(int max_stops) throws IOException {
        this.Orders = new HashMap<>(max_stops, 1f);
        try {
            // Aquí leemos el objeto que viene en el cuerpo de la petición
            String projectId = "routesandmaps";
            String bucketName = "routesandmaps-files";

            GoogleCloudStorageService gcsService = new GoogleCloudStorageService(projectId, bucketName);
            Bucket bucket = gcsService.getBucket();

            if (bucket != null) {
                System.out.println("Conectado al bucket: " + bucket.getName());
            } else {
                System.out.println("El bucket no fue encontrado.");
            }

            StringTokenizer st;
            // Read vehicles data from file
            // Leer datos de vehículos desde Google Cloud Storage
            Blob vehiclesFileBlob = gcsService.getFile("Vehicles.csv-1726779117273.csv");
            if (vehiclesFileBlob == null) {
                System.out.println("Vehicles.csv-1726779117273.csv no fue encontrado en el bucket.");
                return;
            }

            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(vehiclesFileBlob.getContent())))) {
                String line = bufferedReader.readLine(); // Saltar la primera línea
                int vehicle_id = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.matches(",+")) {
                         st = new StringTokenizer(line, ",");
                    this.Vehicles.put(vehicle_id, new Vehicle(vehicle_id, st));
                    vehicle_id++; 
                    }
                  
                }
            } catch (IOException e) {
                System.out.println("Error al leer el archivo Vehicles.csv: " + e.getMessage());
                e.printStackTrace();
            }

            this.VehiclesCount = this.Vehicles.size();

            // Leer datos de órdenes desde Google Cloud Storage
            Blob ordersFileBlob = gcsService.getFile("Orders.csv-1726779054669.csv");
            if (ordersFileBlob == null) {
                System.out.println("Orders.csv-1726779054669.csv no fue encontrado en el bucket.");
                return;
            }

            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(ordersFileBlob.getContent())))) {
                String line = bufferedReader.readLine(); // Saltar la primera línea
                int order_id = 0;
                while ((line = bufferedReader.readLine()) != null && order_id < max_stops) {
                    st = new StringTokenizer(line, ",");
                    this.Orders.put(order_id, new Order(order_id, st));
                    order_id++;
                }
            } catch (IOException e) {
                System.out.println("Error al leer el archivo Orders.csv: " + e.getMessage());
                e.printStackTrace();
            }

            this.OrdersCount = this.Orders.size();
            /*  File trucksFile = new File("Instances\\Vehicles.csv");
            Scanner VehiclesScanner = new Scanner(trucksFile);
            String line = VehiclesScanner.nextLine(); // Skip the first line
            int vehicle_id = 0;
            while (VehiclesScanner.hasNextLine()) {
                line = VehiclesScanner.nextLine();
                st = new StringTokenizer(line, ",");
                this.Vehicles.put(vehicle_id, new Vehicle(vehicle_id, st));
                vehicle_id++;
            }
            VehiclesScanner.close();
            this.VehiclesCount = this.Vehicles.size();

            File orderFile = new File("Instances\\Orders.csv");
            Scanner orderScanner = new Scanner(orderFile);
            orderScanner.nextLine(); // Skip the first line
            int order_id = 0;
            while (orderScanner.hasNextLine() && order_id < max_stops) {
                line = orderScanner.nextLine();
                st = new StringTokenizer(line, ",");
                this.Orders.put(order_id, new Order(order_id, st));
                order_id++;
            }
            orderScanner.close();

            this.OrdersCount = this.Orders.size();*/
        } catch (Exception e) {
            System.out.println("File not found");
            System.exit(0);
        }
        this.setDistanceMatrix();
    }

    private void setDistanceMatrix() {
        this.Depots = new ArrayList<>(this.VehiclesCount);
        for (Vehicle vehicle : this.Vehicles.values()) {
            if (this.Depots.stream().allMatch(vehicle.getStartingLocation()::notEquals)) {
                this.Depots.add(vehicle.getStartingLocation());
            }
            if (this.Depots.stream().allMatch(vehicle.getEndingLocation()::notEquals)) {
                this.Depots.add(vehicle.getEndingLocation());
            }
        }
        this.AllStopsCount = this.OrdersCount + this.Depots.size();
        this.DistanceMatrix = new double[this.AllStopsCount][this.AllStopsCount];
    }

    public double getDistance(int i, int j) {
        if (i != j && this.DistanceMatrix[i][j] == 0) {
            Location stop1 = (i < this.OrdersCount) ? this.Orders.get(i).Location : this.Depots.get(i - this.OrdersCount);
            Location stop2 = (j < this.OrdersCount) ? this.Orders.get(j).Location : this.Depots.get(j - this.OrdersCount);
            this.DistanceMatrix[j][i] = this.DistanceMatrix[i][j] = stop1.getDistance(stop2);
        }
        return this.DistanceMatrix[i][j];
    }

    public double getTravelTime(int i, int j) {
        return this.getDistance(i, j) / this.VehicleSpeed;
    }

    public int getDepotIndex(Location depot) {
        int index = 0;
        for (Location location : this.Depots) {
            if (location.equals(depot)) {
                return index + this.OrdersCount;
            }
            index++;
            if (index >= this.Depots.size()) {
                System.out.print("error");
                System.exit(0);
            }
        }
        return index + this.OrdersCount;
    }

    @Override
    public String toString() {
        return "Stops Count = " + this.OrdersCount + "\n"
                + "Vehicles Count = " + this.VehiclesCount + "\n" //                + "Vehicles = " + this.Vehicles + "\n"
                //                + "Customers = " + this.Customers + "\n"
                ;
    }

    @Override
    public void close() throws Exception {
        this.Orders.values()
                .stream()
                .forEach(order -> {
                    try {
                        order.close();
                    } catch (Exception ex) {
                        Logger.getLogger(InputData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
        this.Orders.clear();
        this.Vehicles.values()
                .stream()
                .forEach(order -> {
                    try {
                        order.close();
                    } catch (Exception ex) {
                        Logger.getLogger(InputData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
        this.Vehicles.clear();
    }
}
