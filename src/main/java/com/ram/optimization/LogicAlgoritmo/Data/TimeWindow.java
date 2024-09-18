/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Data;

/**
 *
 * @author Othmane
 */
public class TimeWindow {
    int Earliest, Latest;

    TimeWindow(int earliest, int latest) {
        if(earliest >= latest)
            throw new IllegalArgumentException("Earliest time should be less than Latest time");
        if (earliest < 0 || latest < 0)
            throw new IllegalArgumentException("Times should be between 0 and 24*60 (excluded)");
        if(earliest >= VisitTime.MINUTES_IN_DAY || latest >= VisitTime.MINUTES_IN_DAY)
            throw new IllegalArgumentException("Times should be between 0 and 24*60 (excluded)");
        this.Earliest = earliest;
        this.Latest = latest;
    }

    TimeWindow() {
        this(0, VisitTime.MINUTES_IN_DAY - 1);
    }

    public int getEarliest() {
        return this.Earliest;
    }

    public int getLatest() {
        return this.Latest;
    }

//    public int getRange() {
//        return this.Latest - this.Earliest;
//    }

    @Override
    public String toString() {
        return "Earliest = " + this.Earliest + " "
                + "Latest = " + this.Latest + " ";
    }
}
