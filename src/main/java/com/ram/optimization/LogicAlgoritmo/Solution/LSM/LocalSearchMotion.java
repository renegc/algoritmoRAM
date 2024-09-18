package com.ram.optimization.LogicAlgoritmo.Solution.LSM;




import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Data.Vehicle;
import com.ram.optimization.LogicAlgoritmo.Data.VisitTime;
import com.ram.optimization.LogicAlgoritmo.Solution.Route;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public abstract class LocalSearchMotion implements AutoCloseable{
    InputData Data;
    String MotionName;
    boolean OnePortion;
    int I, J;
    int Border;
    int StartingDepotIndex, EndingDepotIndex;
    int[] GiantTourPortion1, GiantTourPortion2;
    double Gain;
    Route Route1 = null, Route2 = null;
    Vehicle AssignedVehicle;
    
    public abstract void setGain();
    public abstract void Perform();
    
    LocalSearchMotion(InputData data, Vehicle vehicle, int i, int j, int[] ... portions) {
        if(portions.length == 1 && i >= j)
            throw new IllegalArgumentException("i should be smaller than j in LSM");
        if(portions.length > 2)
            throw new IllegalArgumentException("routes number should be equals to 1 or 2 in LSM");
        this.Data = data;
        this.Gain = 0d;
        this.I = i;
        this.J = j;
        this.AssignedVehicle = vehicle;
        this.OnePortion = portions.length == 1;
        this.GiantTourPortion1 = portions[0];
        this.GiantTourPortion2 = this.OnePortion ? this.GiantTourPortion1 : portions[1];
        this.Border = this.OnePortion ? this.GiantTourPortion1.length : this.GiantTourPortion2.length;
        this.StartingDepotIndex = this.Data.getDepotIndex(this.AssignedVehicle.getStartingLocation());
        this.EndingDepotIndex = this.Data.getDepotIndex(this.AssignedVehicle.getEndingLocation());
    }
    
    public boolean isFeasible(double traveled_distance, VisitTime ready_time) {
        this.Perform();
        if (this.OnePortion) {
            this.Route1 = new Route(this.GiantTourPortion1, this.AssignedVehicle, ready_time);
            return this.Route1.isFeasible(this.Data, traveled_distance);
        }
        this.Route1 = new Route(this.GiantTourPortion1, this.AssignedVehicle, ready_time);
        if(this.Route1.isFeasible(this.Data, traveled_distance)) {
            this.Route2 = new Route(this.GiantTourPortion2, this.AssignedVehicle, this.Route1.getTripEndTime());
            return this.Route2.isFeasible(this.Data, traveled_distance);
        }
        return false;
    }

    @Override
    public void close() {
        this.GiantTourPortion1 = null;
        if(this.GiantTourPortion2 == null)
            this.GiantTourPortion2 = null;
        if(this.Route1 != null)
            this.Route1.close();
        if(this.Route2 != null)
            this.Route2.close();
    }
    
    public double getTraveledDistance() {
        double distance = 0d;
        if(this.Route1 != null)
            distance += this.Route1.getRouteTraveledDistance();
        if(this.Route2 != null)
            distance += this.Route2.getRouteTraveledDistance();
        return distance;
    }
    
    public int getCost() {
        int cost = 0;
        if(this.Route1 != null)
            cost += this.Route1.getRouteCost();
        if(this.Route2 != null)
            cost += this.Route2.getRouteCost();
        return cost;
    }

    public double getGain() {
        return this.Gain;
    }

    public Route getRoute1() {
        return this.Route1;
    }

    public Route getRoute2() {
        return this.Route2;
    }
    
    double getTraveledDistance(int i, int j){
        return this.Data.getDistance(i, j);
    }
    
    double getTraveledDistanceToDepot(int i){
        if(this.AssignedVehicle.getRouteType() == 3)
            return 0d;
        return this.Data.getDistance(i, this.EndingDepotIndex);
    }
    
    double getTraveledDistanceFromDepot(int i){
        return this.Data.getDistance(this.StartingDepotIndex, i);
    }
}
