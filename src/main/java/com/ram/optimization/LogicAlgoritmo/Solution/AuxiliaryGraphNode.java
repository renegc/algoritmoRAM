package com.ram.optimization.LogicAlgoritmo.Solution;



import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Data.Vehicle;
import com.ram.optimization.LogicAlgoritmo.Data.Order;
import com.ram.optimization.LogicAlgoritmo.Data.VisitTime;
import com.ram.optimization.LogicAlgoritmo.Solution.LSM.LocalSearchMotion;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class AuxiliaryGraphNode implements AutoCloseable{
    int NodeProcessingWith;
    Set<Route> Routes;
    AuxiliaryGraphNode Posterior;
    int Label;
    final int NodeIndex;
    final ReentrantLock Lock;
    
    AuxiliaryGraphNode(int NodeIndex){
        this.NodeIndex = NodeIndex;
        this.NodeProcessingWith = NodeIndex;
        this.Lock = new ReentrantLock();
        if(this.NodeIndex > 0)
            this.Label = Integer.MAX_VALUE;
        else{
            this.Routes = new HashSet<>();
            this.Posterior = null;
            this.Label = 0;
        }
    }
    
    void UpdateLabel(AuxiliaryGraphNode Posterior, Route route){
       if(route == null)
           return;
       this.Lock.lock();
       try{
          int label = Posterior.Label + route.RouteCost;
          if(label < this.Label) {
                this.Label = label;
                this.Posterior = Posterior;
                if(this.Routes != null)
                    this.Routes.clear();
                this.Routes = new HashSet<>(this.Posterior.Routes.size() + 1, 1f);
                this.Posterior.Routes.forEach(this.Routes::add);
                this.Routes.add(route);
          }
          else
                route.close();
       }
       finally {
            this.Lock.unlock();
       }
    }
    
   void UpdateLabel(AuxiliaryGraphNode Posterior, Route old_route, Route new_route){
       if(new_route == null)
          return;
       this.Lock.lock();
       try{
          int label = Posterior.Label - old_route.RouteCost + new_route.RouteCost;
          if(label < this.Label) {
                this.Label = label;
                this.Posterior = Posterior;
                if(this.Routes != null)
                    this.Routes.clear();
                this.Routes = new HashSet<>(this.Posterior.Routes.size() + 1, 1f);
                this.Posterior.getRoutes()
                            .stream()
                            .map(r -> r == old_route ? new_route : r)
                            .forEach(this.Routes::add);
          }
          else
                new_route.close();
       }
       finally {
           this.Lock.unlock();
       }
   }
    
   void UpdateLabel(AuxiliaryGraphNode Posterior, Route old_route, LocalSearchMotion lsm){
       if(lsm == null)
          return;
       this.Lock.lock();
       try{
          int label = Posterior.Label - old_route.RouteCost + lsm.getCost();
          if(label < this.Label) {
                this.Label = label;
                this.Posterior = Posterior;
                if(this.Routes != null)
                    this.Routes.clear();
                this.Routes = new HashSet<>(this.Posterior.Routes.size() + 1, 1f);
                this.Posterior.getRoutes()
                            .stream()
                            .filter(r -> r != old_route)
                            .forEach(this.Routes::add);
                if(lsm.getRoute1() != null)
                    this.Routes.add(lsm.getRoute1());
                if(lsm.getRoute2() != null)
                    this.Routes.add(lsm.getRoute2());
          }
          else
                lsm.close();
       }
       finally {
           this.Lock.unlock();
       }
   }
    
    public String toString(InputData data) {
       String str = "";
       Route[] array = this.Routes.stream()
                                .sorted(Route::compare)
                                .toArray(Route[]::new);
       for(Route r : array) {
            str += "This route contains " + r.Length + " stops and assigned to ";
            str += r.AssignedVehicle.getAlias() + " ";
            str += r.AssignedVehicle.getSkills() + " and costs " + r.RouteCost +" USD:\n";
            str += r.toString(data);
       }
       return str + "Total cost = " + this.Label + " USD";
    }

    public void toCSV(InputData data) {
        int customerCount = data.OrdersCount;
        int vehicleCount = data.VehiclesCount;
        String path = "output for " + customerCount + " customers and " + vehicleCount + " vehicles" + " with total cost = " + this.Label + " USD.csv";
        try (PrintWriter writer = new PrintWriter(new File(path))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Alias,Latitude,Longitude,Vehicle,Visit Time,Rout Cost, Vehicle Cost\n");
            this.getRoutes().stream()
                        .sorted(Route::compare)
                        .forEach(r -> {
                            double vehicle_cost = this.getRoutes().stream()
                                                            .filter(rr -> rr.AssignedVehicle.getId() == r.AssignedVehicle.getId())
                                                            .mapToDouble(Route::getRouteCost)
                                                            .sum();
                            sb.append("depot,").append(r.AssignedVehicle.getStartingLocation().getAbscisse()).append(",");
                            sb.append(r.AssignedVehicle.getStartingLocation().getOrdinate() + ",");
                            sb.append(r.AssignedVehicle.getAlias() + ",");
                            sb.append(r.TripDepartureTime + ",");
                            sb.append(r.RouteCost + ",");
                            sb.append(vehicle_cost + "\n");
                            for(int stop:r.Sequence) {
                                Order order = data.Orders.get(stop);
                                sb.append(data.Orders.get(stop).getAlias() + "," +order.getLocation().getAbscisse() + ",");
                                sb.append(order.getLocation().getOrdinate() + ",");
                                sb.append(r.AssignedVehicle.getAlias() + ",");
                                sb.append(r.VisitTimes.get(stop) + ",");
                                sb.append(r.RouteCost + ",");
                                sb.append(vehicle_cost + "\n");
                            }
                            if(r.AssignedVehicle.getRouteType() < 3) {
                                sb.append("depot,");
                                sb.append(r.AssignedVehicle.getEndingLocation().getAbscisse() + ",");
                                sb.append(r.AssignedVehicle.getEndingLocation().getOrdinate() + ",");
                                sb.append(r.AssignedVehicle.getAlias() + ",");
                                sb.append(r.TripEndTime + ",");
                                sb.append(r.RouteCost + ",");
                                sb.append(vehicle_cost + "\n");
                            }
                        });
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
    
    AuxiliaryGraphNode getPosterior(){
        return this.Posterior;
    }

    Set<Route> getRoutes(){
        return this.Routes;
    }

    int getRoutesCount(){
        return this.Routes.size();
    }
    
    boolean isFeasible(){
        return this.Posterior != null;
    }
    
    VisitTime getReadyTime(Vehicle vehicle){
        return this.Routes.stream()
                        .filter(r -> r.AssignedVehicle.getId() == vehicle.getId())
                        .map(r -> r.TripEndTime)
                        .reduce(new VisitTime(vehicle.getShift().getEarliest()), (vt1, vt2) -> vt1.compare(vt2) <= 0 ? vt2 : vt1);
    }
    
    double getTraveled_distance(Vehicle vehicle){
        return this.getTraveled_distance(vehicle, null);
    }
    
    double getTraveled_distance(Vehicle vehicle, Route route){
        return this.Routes.stream()
                        .filter(r -> r != route)
                        .filter(r -> r.AssignedVehicle.getId() == vehicle.getId())
                        .mapToDouble(r -> r.RouteTraveledDistance)
                        .sum();
    }
    
    int getSumStops(Vehicle vehicle) {
        return this.Routes.stream()
                        .filter(r -> r.AssignedVehicle.getId() == vehicle.getId())
                        .mapToInt(r -> r.Length)
                        .sum();
    }

    int[] getGiantTour(InputData data) {
        try {
            return this.getRoutes().stream()
                                .sorted((r1, r2) -> {
                                    if(r1.AssignedVehicle.getId() == r2.AssignedVehicle.getId()) 
                                        return r1.TripLoadingStartTime.compare(r2.TripLoadingStartTime);
                                    return (int) (Math.random() * 100);
                                })
                                .flatMapToInt(r -> IntStream.of(r.Sequence))
                                .toArray();
        } catch (Exception e) {
            return this.getRoutes().stream()
                                .sorted(Route::compare)
                                .flatMapToInt(r -> IntStream.of(r.Sequence))
                                .toArray();
        }
    }

    @Override
    public void close() throws Exception {
        if(this.Routes == null)
            return;
        for(Route r : this.Routes)
            r.close();
        this.Routes.clear();
    }
}