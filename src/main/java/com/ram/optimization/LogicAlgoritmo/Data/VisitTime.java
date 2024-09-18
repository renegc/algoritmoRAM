/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Data;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Othmane
 */
public class VisitTime {
    final static int MINUTES_IN_DAY = 24 * 60;
    int TimeStamp; // visit time in minutes
    int Day;

    public VisitTime(int day, int time_stamp) {
        if (time_stamp < 0 || time_stamp >= VisitTime.MINUTES_IN_DAY)
            throw new IllegalArgumentException("Time should be between 0 and 24*60 (excluded)");
        this.TimeStamp = time_stamp;
        if (day < 0)
            throw new IllegalArgumentException("Day should be positive");
        this.Day = day;
    }

    public VisitTime(int TimeStamp) {
        this(0, TimeStamp);
    }

    public int getTimeStamp() {
        return this.TimeStamp;
    }

    public int getDay() {
        return this.Day;
    }

    public int compare(VisitTime other) {
        if(other == null)
            return this.TimeStamp;
        return MINUTES_IN_DAY * (this.Day - other.Day) + this.TimeStamp - other.TimeStamp;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VisitTime other = (VisitTime) obj;
        return this.TimeStamp == other.TimeStamp && this.Day == other.Day;
    }
    
    

    @Override
    public String toString() {
        int minutes = this.TimeStamp;
        LocalTime time = LocalTime.ofSecondOfDay(0);
        try{
            time = LocalTime.ofSecondOfDay(minutes * 60);
        }catch(Exception e){
//            System.out.println(minutes);
        }
        return this.Day + "-" + time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}