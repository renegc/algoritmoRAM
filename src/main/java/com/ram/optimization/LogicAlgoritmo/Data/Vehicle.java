/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Data;

import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 *
 * @author Othmane
 */
public class Vehicle  implements AutoCloseable{
    final String Alias;
    final int Id;
    final int RefillTime;
    final int MaxWeight;
    final int MaxPieces;
    final int MaxVolume;
    final int MaxStops;
    final int RouteType;
    final double InitialWeight; // Ton
    final double FixedCost; // USD/day
    final double VariableCost; // USD/Km/Ton
    final double MaxDistance; //Km
    final TimeWindow Shift;
    Location StartingLocation;
    Location EndingLocation;
    final Set<String> Skills = new HashSet<String>(2, 1f);

    Vehicle(int id, StringTokenizer st) {
        this.Id = id;
        this.Alias = st.nextToken();
        StringTokenizer st2;
        st2 = new StringTokenizer(st.nextToken(), ":");
        int shiftStartSec = Integer.parseInt(st2.nextToken()) * 60 + Integer.parseInt(st2.nextToken());
        st2 = new StringTokenizer(st.nextToken(), ":");
        int shiftEndSec = Integer.parseInt(st2.nextToken()) * 60 + Integer.parseInt(st2.nextToken());
        this.Shift = new TimeWindow(shiftStartSec, shiftEndSec);
        this.FixedCost = Double.parseDouble(st.nextToken());
        this.VariableCost = Double.parseDouble(st.nextToken());
        this.InitialWeight = Double.parseDouble(st.nextToken());
        this.RefillTime = Integer.parseInt(st.nextToken());
        this.MaxWeight = Integer.parseInt(st.nextToken());
        this.MaxPieces = Integer.parseInt(st.nextToken());
        this.MaxVolume = Integer.parseInt(st.nextToken());
        this.MaxDistance = Double.parseDouble(st.nextToken());
        this.MaxStops = Integer.parseInt(st.nextToken());
        double x, y;
        try {
            x = Double.parseDouble(st.nextToken());
            y = Double.parseDouble(st.nextToken());
            this.StartingLocation = new Location(x, y);
        }
        catch (NumberFormatException e) {
            System.out.println("No location for vehicle " + this.Alias);
            System.exit(0);
        }
        x = Double.parseDouble(st.nextToken());
        y = Double.parseDouble(st.nextToken());
        this.EndingLocation = new Location(x, y);
        this.RouteType = Integer.parseInt(st.nextToken());
        this.setSkills(st.nextToken());
    }

    public void setSkills(String skills) {
        if(skills == "" || skills == " "){
            this.Skills.add("");
            return;
        }
        StringTokenizer st = new StringTokenizer(skills, "-");
        while (st.hasMoreTokens()) 
            this.Skills.add(st.nextToken());
    }


    public int getRefillTime() {
        return RefillTime;
    }
    
    public int getRouteType() {
        return this.RouteType;
    }

    public String getAlias() {
        return this.Alias;
    }

    public int getId() {
        return Id;
    }
    
    public boolean getSkillsFit(Order order){
        return order.getSkillsFit(this);
    }

    public Set<String> getSkills() {
        return this.Skills;
    }

    public Location getStartingLocation() {
        return this.StartingLocation;
    }

    public Location getEndingLocation() {
        return this.EndingLocation;
    }

    public double getVehicleInitialWeight() {
        return this.InitialWeight;
    }

    public TimeWindow getShift() {
        return this.Shift;
    }

    public int getMaxWeight() {
        return this.MaxWeight;
    }

    public int getMaxPieces() {
        return this.MaxPieces;
    }

    public int getMaxVolume() {
        return this.MaxVolume;
    }

    public double getMaxDistance() {
        return this.MaxDistance;
    }

    public int getMaxStops() {
        return this.MaxStops;
    }

    public double getFixedCost() {
        return this.FixedCost;
    }

    public double getVariableCost() {
        return this.VariableCost;
    }

    @Override
    public String toString() {
        return "Alias = " + this.Alias + " "
                + "Starting Location = " + this.StartingLocation + " "
                + "Ending Location = " + this.EndingLocation + " "
                + "Shift = " + this.Shift + " "
                + "Max Weight = " + this.MaxWeight + " "
                + "Max Pieces = " + this.MaxPieces + " "
                + "Max Volume = " + this.MaxVolume + " "
                + "Max Distance = " + this.MaxDistance + " "
                + "Max Stops = " + this.MaxStops + " "
                + "Skills = " + this.Skills + " "
                + "Route Type = " + this.RouteType + " ";
    }

    @Override
    public void close() throws Exception {
        this.Skills.clear();
    }
}
