/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Othmane
 */
public class Order implements AutoCloseable{
    String Alias;
    int Id;
    int Pieces;
    double Weight;
    double Volume;
    double ServiceTime;
    Location Location;
    TimeWindow TW;
    Set<String> Skills = new HashSet<String>(2, 1f);

    Order(int id, StringTokenizer st) {
        this.Id = id;
        this.Alias = st.nextToken();
        try{
            double x = Double.parseDouble(st.nextToken());
            double y = Double.parseDouble(st.nextToken());
            this.Location = new Location(x, y);
        }
        catch (NumberFormatException e) {
            System.out.println("No location for order " + this.Alias);
            System.exit(0);
        }
        int shiftStart = Integer.parseInt(st.nextToken()); // shift start time in minutes
        int shiftEnd = Integer.parseInt(st.nextToken());
        this.TW = new TimeWindow(shiftStart, shiftEnd);
        this.ServiceTime = Double.parseDouble(st.nextToken());
        this.Pieces = Integer.parseInt(st.nextToken());
        this.Weight = Double.parseDouble(st.nextToken());
        this.Volume = Double.parseDouble(st.nextToken());
        this.setSkills(st.nextToken());
    }

    public void setSkills(String skills) {
        if(skills == ""){
            this.Skills.add("");
            return;
        }
        StringTokenizer st = new StringTokenizer(skills, "-");
        while (st.hasMoreTokens())
            this.Skills.add(st.nextToken());
    }

    public double getVolume() {
        return this.Volume;
    }

    public double getServiceTime() {
        return this.ServiceTime;
    }

    public Location getLocation() {
        return this.Location;
    }

    public TimeWindow getTimeWindow() {
        return this.TW;
    }

    public int getPieces() {
        return this.Pieces;
    }

    public double getWeight() {
        return this.Weight;
    }

    public String getAlias() {
        return this.Alias;
    }

    public int getId() {
        return this.Id;
    }
    
    public boolean getSkillsFit(Vehicle vehicle){
        return this.Skills.stream()
                        .allMatch(order_skill -> vehicle.getSkills().stream().anyMatch(vehicle_skill -> order_skill.compareTo(vehicle_skill) == 0));
    }

    public Set<String> getSkills() {
        return this.Skills;
    }

    @Override
    public String toString() {
        return "Location = " + this.Location + " "
                + "Shift = " + this.TW + " "
                + "ServiceTime = " + this.ServiceTime + " "
                + "Pieces = " + this.Pieces + " "
                + "Weight = " + this.Weight + " "
                + "Skills = " + this.Skills;
    }

    @Override
    public void close() throws Exception {
        this.Skills.clear();
    }
}
