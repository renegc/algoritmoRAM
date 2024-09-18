/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Solution.LSM;


import com.ram.optimization.LogicAlgoritmo.Solution.Motion;
import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Data.Vehicle;


/**
 *
 * @author pc
 */
public class Swap extends LocalSearchMotion {

    private final int FirstBorder;

    public Swap(InputData data, Vehicle vehicle, int i, int j, int[]... portions) {
        super(data, vehicle, i, j, portions);
        this.FirstBorder = this.GiantTourPortion1.length;
        this.setGain();
        this.MotionName = "Swap";
    }

    @Override
    public void Perform() {
        if (this.OnePortion)
            new Motion(this.I, this.J).Swap(this.GiantTourPortion1);
        else {
            int aux = this.GiantTourPortion1[this.I];
            this.GiantTourPortion1[this.I] = this.GiantTourPortion2[this.J];
            this.GiantTourPortion2[this.J] = aux;
        }
    }

    @Override
    public void setGain() {
        this.Gain = 0d;
        if (this.I == 0) {
            this.Gain += this.getTraveledDistanceFromDepot(this.GiantTourPortion2[this.J]);
            this.Gain -= this.getTraveledDistanceFromDepot(this.GiantTourPortion1[this.I]);
        }
        else {
            this.Gain += this.getTraveledDistance(this.GiantTourPortion1[this.I - 1], this.GiantTourPortion2[this.J]);
            this.Gain -= this.getTraveledDistance(this.GiantTourPortion1[this.I - 1], this.GiantTourPortion1[this.I]);
        }
        if (this.I + 1 < this.J && this.OnePortion) {
            this.Gain += this.getTraveledDistance(this.GiantTourPortion2[this.J - 1], this.GiantTourPortion1[this.I]);
            this.Gain -= this.getTraveledDistance(this.GiantTourPortion2[this.J - 1], this.GiantTourPortion2[this.J]);
            this.Gain += this.getTraveledDistance(this.GiantTourPortion2[this.J], this.GiantTourPortion1[this.I + 1]);
            this.Gain -= this.getTraveledDistance(this.GiantTourPortion1[this.I], this.GiantTourPortion1[this.I + 1]);
        }
        else if (!this.OnePortion) {
            if (this.J > 0) {
                this.Gain += this.getTraveledDistance(this.GiantTourPortion2[this.J - 1], this.GiantTourPortion1[this.I]);
                this.Gain -= this.getTraveledDistance(this.GiantTourPortion2[this.J - 1], this.GiantTourPortion2[this.J]);
            }
            else {
                this.Gain += this.getTraveledDistanceFromDepot(this.GiantTourPortion1[this.I]);
                this.Gain -= this.getTraveledDistanceFromDepot(this.GiantTourPortion2[this.J]);
            }
            if (this.I + 1 < this.FirstBorder) {
                this.Gain += this.getTraveledDistance(this.GiantTourPortion2[this.J], this.GiantTourPortion1[this.I + 1]);
                this.Gain -= this.getTraveledDistance(this.GiantTourPortion1[this.I], this.GiantTourPortion1[this.I + 1]);
            }
            else {
                this.Gain += this.getTraveledDistanceToDepot(this.GiantTourPortion2[this.J]);
                this.Gain -= this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I]);
            }
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
