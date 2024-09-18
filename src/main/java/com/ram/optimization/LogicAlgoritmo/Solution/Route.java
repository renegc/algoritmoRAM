package com.ram.optimization.LogicAlgoritmo.Solution;


import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Data.Vehicle;
import com.ram.optimization.LogicAlgoritmo.Data.Order;
import com.ram.optimization.LogicAlgoritmo.Data.VisitTime;
import com.ram.optimization.LogicAlgoritmo.Solution.LSM.LocalSearchMotion;
import com.ram.optimization.LogicAlgoritmo.Solution.LSM.Insertion;
import com.ram.optimization.LogicAlgoritmo.Solution.LSM.InverseInsertion;
import com.ram.optimization.LogicAlgoritmo.Solution.LSM.Swap;
import com.ram.optimization.LogicAlgoritmo.Solution.LSM._2Opt;
import java.util.Map;
import java.util.HashMap;
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
public class Route implements AutoCloseable {
    final static double Probability = 0.05d;
    final int Length;
    final Vehicle AssignedVehicle;
    int[] Sequence;
    double RouteTraveledDistance;
    Map<Integer, VisitTime> VisitTimes;
    VisitTime TripEndTime;
    VisitTime TripDepartureTime;
    VisitTime TripLoadingStartTime;
    int RouteCost;
    int SumDemandPieces;
    double SumDemandWeight;
    double SumDemandVolume;
    
   public Route(int[] seq, Vehicle vehicle, VisitTime ready_time){
       this.Sequence = seq;
       this.Length = this.Sequence.length;
       this.AssignedVehicle = vehicle;
       this.TripLoadingStartTime = ready_time;
   }
    
   public boolean isFeasible(InputData data, double cumulative_traveled_distance) {
       if(this.VisitTimes == null)
            this.VisitTimes = new HashMap<>(this.Length, 1f);
       int day = this.TripLoadingStartTime.getDay();
       double current_time = this.TripLoadingStartTime.getTimeStamp();
       boolean c = new VisitTime(this.AssignedVehicle.getShift().getEarliest()).equals(this.TripLoadingStartTime);
       current_time += c ? 0d : this.AssignedVehicle.getRefillTime();
       this.TripDepartureTime = new VisitTime(day, (int) Math.ceil(current_time));
       this.RouteTraveledDistance = 0d;
       this.SumDemandPieces = 0;
       this.SumDemandWeight = 0;
       this.SumDemandVolume = 0;
       int previous_stop = data.getDepotIndex(this.AssignedVehicle.getStartingLocation());
       double travel_time;
       double variable_cost = 0d;
       double sum_weight = IntStream.of(this.Sequence)
                                .mapToObj(data.Orders::get)
                                .mapToDouble(Order::getWeight)
                                .sum();
       if(this.AssignedVehicle.getMaxWeight() < sum_weight)
            return false;
       sum_weight += 1000d * this.AssignedVehicle.getVehicleInitialWeight();
       double distance;
       for (int stop : this.Sequence){
          Order order = data.Orders.get(stop);
          this.SumDemandPieces += order.getPieces();
          this.SumDemandWeight += order.getWeight();
          this.SumDemandWeight += order.getVolume();
          if(this.AssignedVehicle.getMaxPieces() < this.SumDemandPieces
                || this.AssignedVehicle.getMaxWeight() < this.SumDemandWeight
                || this.AssignedVehicle.getMaxVolume() < this.SumDemandVolume)
                return false;
          distance = data.getDistance(previous_stop, stop);
          this.RouteTraveledDistance += distance;
          if(this.RouteTraveledDistance + cumulative_traveled_distance > this.AssignedVehicle.getMaxDistance())
                return false;
          variable_cost += this.AssignedVehicle.getVariableCost() * distance * sum_weight / 1000d;
          sum_weight -= order.getWeight();
          travel_time = data.getTravelTime(previous_stop, stop);
          while(current_time + travel_time > this.AssignedVehicle.getShift().getLatest()){
                day++;
                travel_time -= this.AssignedVehicle.getShift().getLatest() - current_time;
                current_time = this.AssignedVehicle.getShift().getEarliest();
          }
          current_time += travel_time;
          if (current_time < order.getTimeWindow().getEarliest())
                current_time = order.getTimeWindow().getEarliest();
          if (current_time > order.getTimeWindow().getLatest())
                return false;
          this.VisitTimes.put(stop, new VisitTime(day, (int)current_time));
          current_time += order.getServiceTime();
          previous_stop = stop;
       }
       if(this.AssignedVehicle.getRouteType() < 3) {
          int depot_index = data.getDepotIndex(this.AssignedVehicle.getEndingLocation());
          distance = data.getDistance(previous_stop, depot_index);
          this.RouteTraveledDistance += distance;
          if(this.RouteTraveledDistance + cumulative_traveled_distance > this.AssignedVehicle.getMaxDistance())
                return false;
          variable_cost += this.AssignedVehicle.getVariableCost() * distance * sum_weight / 1000d;
          travel_time = data.getTravelTime(previous_stop, depot_index);
          while(current_time + travel_time > this.AssignedVehicle.getShift().getLatest()){
                day++;
                travel_time -= this.AssignedVehicle.getShift().getLatest() - current_time;
                current_time = this.AssignedVehicle.getShift().getEarliest();
          }
          current_time += travel_time;
       }
       this.TripEndTime = new VisitTime(day, (int) Math.ceil(current_time));
       this.RouteCost = (int) Math.ceil(variable_cost);
       this.RouteCost += c ? this.AssignedVehicle.getFixedCost() : 0;
       this.RouteCost += this.AssignedVehicle.getFixedCost() * (this.TripEndTime.getDay() - this.TripLoadingStartTime.getDay());
       return true;
   }
    
   boolean Improve(InputData data, double traveled_distance, VisitTime ready_time, boolean c) throws Exception {
       LocalSearchMotion lsm;
       int cost = c ? this.RouteCost : Integer.MAX_VALUE;
//       int initial_cost = cost;
       for(int k = 0; k < this.Length; k++)
          for(int l = this.Length - 1; l > k ; l--) {
              lsm = new _2Opt(data, this.AssignedVehicle, k, l, this.Sequence.clone());
              if((cost == Integer.MAX_VALUE || Math.random() < Route.Probability || lsm.getGain() < 0d)
                && lsm.isFeasible(traveled_distance, ready_time)
                && cost > lsm.getRoute1().RouteCost) {
                    this.Sequence = lsm.getRoute1().Sequence;
                    cost = lsm.getRoute1().RouteCost;
              }
              lsm.close();
              if(l > k + 1) {
                    lsm = new Swap(data, this.AssignedVehicle, k, l, this.Sequence.clone());
                    if((cost == Integer.MAX_VALUE || Math.random() < Route.Probability || lsm.getGain() < 0d)
                        && lsm.isFeasible(traveled_distance, ready_time)
                        && cost > lsm.getRoute1().RouteCost) {
                        this.Sequence = lsm.getRoute1().Sequence;
                        cost = lsm.getRoute1().RouteCost;
                    } 
                    lsm.close();
              }
             for(int n = l == k + 1 ? 1 : 0; l + n < this.Length; n++) {
                lsm = new Insertion(data, this.AssignedVehicle, true, n, k, l, this.Sequence.clone());
                if((cost == Integer.MAX_VALUE || Math.random() < Route.Probability || lsm.getGain() < 0d)
                    && lsm.isFeasible(traveled_distance, ready_time)
                    && cost > lsm.getRoute1().RouteCost) {
                    this.Sequence = lsm.getRoute1().Sequence;
                    cost = lsm.getRoute1().RouteCost;
                    lsm.close();
                    break;
                }
                else 
                    lsm.close();
                if(n == 0)
                    continue;
                lsm = new Insertion(data, this.AssignedVehicle, false, n, k, l, this.Sequence.clone());
                if((cost == Integer.MAX_VALUE || Math.random() < Route.Probability || lsm.getGain() < 0d)
                    && lsm.isFeasible(traveled_distance, ready_time)
                    && cost > lsm.getRoute1().RouteCost) {
                    this.Sequence = lsm.getRoute1().Sequence;
                    cost = lsm.getRoute1().RouteCost;
                    lsm.close();
                    break;
                }
                else 
                    lsm.close();
              }
              for(int n = l == k + 1 ? 1 : 0; k - n >= 0; n++) {
                lsm = new InverseInsertion(data, this.AssignedVehicle, true, n, k, l, this.Sequence.clone());
                if((cost == Integer.MAX_VALUE || Math.random() < Route.Probability || lsm.getGain() < 0d)
                    && lsm.isFeasible(traveled_distance, ready_time)
                    && cost > lsm.getRoute1().RouteCost) {
                    this.Sequence = lsm.getRoute1().Sequence;
                    cost = lsm.getRoute1().RouteCost;
                    lsm.close();
                    break;
                }
                else 
                    lsm.close();
                if(n == 0)
                    continue;
                lsm = new InverseInsertion(data, this.AssignedVehicle, false, n, k, l, this.Sequence.clone());
                if((cost == Integer.MAX_VALUE || Math.random() < Route.Probability || lsm.getGain() < 0d)
                    && lsm.isFeasible(traveled_distance, ready_time)
                    && cost > lsm.getRoute1().RouteCost) {
                    this.Sequence = lsm.getRoute1().Sequence;
                    cost = lsm.getRoute1().RouteCost;
                    lsm.close();
                    break;
                }
                else 
                    lsm.close();
              }
          }
//       if(cost < initial_cost) {
//            this.isFeasible(data, traveled_distance);
//            return this.Improve(data, traveled_distance, ready_time, true);
//       }
       if(cost < Integer.MAX_VALUE)
            this.isFeasible(data, traveled_distance);
       return cost < Integer.MAX_VALUE;
    }
    
    LocalSearchMotion getLSM(InputData data, Route route, double traveled_distance, boolean c) throws Exception { 
        if(route == null)
            return null;
        LocalSearchMotion lsm;
        int cost = this.RouteCost + (c ? route.RouteCost : Integer.MAX_VALUE);
        for(int k = 0; k < this.Length; k++){
            for(int l = 0; l < route.Length; l++){
                lsm = new _2Opt(data, this.AssignedVehicle, k, l, this.Sequence, route.Sequence);
                if((!c || Math.random() < Route.Probability || lsm.getGain() < 0d)
                    && lsm.isFeasible(traveled_distance, this.TripLoadingStartTime)
                    && lsm.getTraveledDistance() + traveled_distance <= this.AssignedVehicle.getMaxDistance()
                    && lsm.getCost() < cost) {
                    LocalSearchMotion new_lsm = lsm.getRoute1().getLSM(data, lsm.getRoute2(), traveled_distance, true);
                    if(new_lsm == null)
                        return lsm;
                    lsm.close();
                    return new_lsm;
                }
                else 
                    lsm.close();
                lsm = new Swap(data, this.AssignedVehicle, k, l, this.Sequence.clone(), route.Sequence.clone());
                if((!c || Math.random() < Route.Probability || lsm.getGain() < 0d)
                    && lsm.isFeasible(traveled_distance, this.TripLoadingStartTime)
                    && lsm.getTraveledDistance() + traveled_distance <= this.AssignedVehicle.getMaxDistance()
                    && lsm.getCost() < cost) {
                    LocalSearchMotion new_lsm = lsm.getRoute1().getLSM(data, lsm.getRoute2(), traveled_distance, true);
                    if(new_lsm == null)
                        return lsm;
                    lsm.close();
                    return new_lsm;
                }
                else 
                    lsm.close();
                for(int n = 0; l + n < route.Length; n++){
                    lsm = new Insertion(data, this.AssignedVehicle, true, n, k, l, this.Sequence, route.Sequence);  
                    if((!c || Math.random() < Route.Probability || lsm.getGain() < 0d)
                        && lsm.isFeasible(traveled_distance, this.TripLoadingStartTime)
                        && lsm.getTraveledDistance() + traveled_distance <= this.AssignedVehicle.getMaxDistance()
                        && lsm.getCost() < cost) {
                        LocalSearchMotion new_lsm = lsm.getRoute1().getLSM(data, lsm.getRoute2(), traveled_distance, true);
                        if(new_lsm == null || lsm.getRoute2() == null)
                            return lsm;
                        lsm.close();
                        return new_lsm;
                    }
                    else 
                        lsm.close();
                    if(n == 0)
                        continue;
                    lsm = new Insertion(data, this.AssignedVehicle, false, n, k, l, this.Sequence, route.Sequence); 
                    if((!c || Math.random() < Route.Probability || lsm.getGain() < 0d)
                        && lsm.isFeasible(traveled_distance, this.TripLoadingStartTime)
                        && lsm.getTraveledDistance() + traveled_distance <= this.AssignedVehicle.getMaxDistance()
                        && lsm.getCost() < cost) {
                        LocalSearchMotion new_lsm = lsm.getRoute1().getLSM(data, lsm.getRoute2(), traveled_distance, true);
                        if(new_lsm == null || lsm.getRoute2() == null)
                            return lsm;
                        lsm.close();
                        return new_lsm;
                    }
                    else 
                        lsm.close();
                }
                for(int n = 0; k - n >= 0; n++){
                    lsm = new InverseInsertion(data, this.AssignedVehicle, true, n, k, l, this.Sequence, route.Sequence); 
                    if((!c || Math.random() < Route.Probability || lsm.getGain() < 0d)
                        && lsm.isFeasible(traveled_distance, this.TripLoadingStartTime)
                        && lsm.getTraveledDistance() + traveled_distance <= this.AssignedVehicle.getMaxDistance()
                        && lsm.getCost() < cost) {
                        LocalSearchMotion new_lsm = lsm.getRoute1().getLSM(data, lsm.getRoute2(), traveled_distance, true);
                        if(new_lsm == null || lsm.getRoute2() == null)
                            return lsm;
                        lsm.close();
                        return new_lsm;
                    }
                    else 
                        lsm.close();
                    if(n == 0)
                        continue;
                    lsm = new InverseInsertion(data, this.AssignedVehicle, false, n, k, l, this.Sequence, route.Sequence); 
                    if((!c || Math.random() < Route.Probability || lsm.getGain() < 0d)
                        && lsm.isFeasible(traveled_distance, this.TripLoadingStartTime)
                        && lsm.getTraveledDistance() + traveled_distance <= this.AssignedVehicle.getMaxDistance()
                        && lsm.getCost() < cost) {
                        LocalSearchMotion new_lsm = lsm.getRoute1().getLSM(data, lsm.getRoute2(), traveled_distance, true);
                        if(new_lsm == null || lsm.getRoute2() == null)
                            return lsm;
                        lsm.close();
                        return new_lsm;
                    }
                    else 
                        lsm.close();
                }
            }
        }
        return null;
    }

    public VisitTime getTripEndTime() {
        return TripEndTime;
    }

    public int getRouteCost() {
        return RouteCost;
    }

    public double getRouteTraveledDistance() {
        return RouteTraveledDistance;
    }
    
    int compare(Route route) {
        if(this.AssignedVehicle.getId() == route.AssignedVehicle.getId()) 
            return this.TripLoadingStartTime.compare(route.TripLoadingStartTime);
        return this.AssignedVehicle.getId() - route.AssignedVehicle.getId();
    }
    
    String toString(InputData data){
        String str = "";
        for(int stop : this.Sequence) {
            str += "Order by " + data.Orders.get(stop).getAlias() + " is visitied at ";
            str += this.VisitTimes.get(stop) + " " + data.Orders.get(stop).getSkills() + "\n";
        }
        return str + "\n";
    }

   @Override
   public void close() {
        this.Sequence = null;
        this.VisitTimes = null;
        if(this.VisitTimes != null)
            this.VisitTimes.clear();
   }
}