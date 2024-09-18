/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Solution.LSM;

import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Data.Vehicle;
import com.ram.optimization.LogicAlgoritmo.Solution.Motion;
import java.util.stream.IntStream;

/**
 *
 * @author Othmane
 */
public class _2Opt extends LocalSearchMotion {

    public _2Opt(InputData data, Vehicle vehicle, int i, int j, int[]... portions) {
        super(data, vehicle, i, j, portions);
        this.setGain();
        this.MotionName = "2Opt";
    }

    @Override
    public void Perform() {
        if (this.OnePortion) 
            new Motion(this.I, this.J)._2opt(this.GiantTourPortion1);
        else {
            int[] array1 = new int[this.I + this.J + 1];
            IntStream.range(0, this.I).forEach(i -> array1[i] = this.GiantTourPortion1[i]);
            IntStream.range(0, this.J + 1).forEach(i -> array1[i + this.I] = this.GiantTourPortion2[this.J - i]);
            int[] array2 = new int[this.GiantTourPortion2.length + this.GiantTourPortion1.length - array1.length];
            int k = 0;
            for (int i = this.GiantTourPortion1.length - 1; i >= this.I; i--) {
                array2[k] = this.GiantTourPortion1[i];
                k++;
            }
            for (int i = this.J + 1; i < this.GiantTourPortion2.length; i++) {
                array2[k] = this.GiantTourPortion2[i];
                k++;
            }
            this.GiantTourPortion1 = array1;
            this.GiantTourPortion2 = array2;
        }
    }

    @Override
    public void setGain() {
        if (this.I == 0) {
            this.Gain += this.getTraveledDistanceFromDepot(this.GiantTourPortion2[this.J]);
            this.Gain -= this.getTraveledDistanceFromDepot(this.GiantTourPortion1[this.I]);
        }
        else {
            this.Gain += this.getTraveledDistance(this.GiantTourPortion1[this.I - 1], this.GiantTourPortion2[this.J]);
            this.Gain -= this.getTraveledDistance(this.GiantTourPortion1[this.I - 1], this.GiantTourPortion1[this.I]);
        }
        if (this.J + 1 < this.Border) {
            this.Gain += this.getTraveledDistance(this.GiantTourPortion1[this.I], this.GiantTourPortion2[this.J + 1]);
            this.Gain -= this.getTraveledDistance(this.GiantTourPortion2[this.J], this.GiantTourPortion2[this.J + 1]);
        }
        else {
            this.Gain += this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I]);
            this.Gain -= this.getTraveledDistanceToDepot(this.GiantTourPortion2[this.J]);
        }
    }

    @Override
    public String toString() {
        return this.MotionName + " (" + this.I + ";" + this.J + ")";
    }
}
