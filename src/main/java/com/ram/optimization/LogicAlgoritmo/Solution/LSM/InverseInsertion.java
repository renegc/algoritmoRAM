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
 * @author pc
 */
public class InverseInsertion extends LocalSearchMotion {

    private final int FirstBorder;
    private final int N;
    private final boolean With2Opt;

    public InverseInsertion(InputData data, Vehicle vehicle, boolean with2opt, int n, int i, int j, int[]... portions) {
        super(data, vehicle, i, j, portions);
        this.With2Opt = with2opt;
        this.N = n;
        this.FirstBorder = this.GiantTourPortion1.length;
        this.setGain();
        this.MotionName = "InverseInsertion";
    }

    @Override
    public void Perform() {
        if (this.OnePortion)
            IntStream.range(0, this.N + 1).forEach(i -> new Motion(this.I - i, (this.With2Opt) ? this.J : this.J - i).InverseInsertion(this.GiantTourPortion1));
        else {
            int[] array1 = new int[this.GiantTourPortion1.length - this.N - 1];
            IntStream.range(0, this.I - this.N).forEach(i -> array1[i] = this.GiantTourPortion1[i]);
            IntStream.range(this.I + 1, this.GiantTourPortion1.length).forEach(i -> array1[i - this.N - 1] = this.GiantTourPortion1[i]);
            int[] array2 = new int[this.GiantTourPortion2.length + this.N + 1];
            if (this.GiantTourPortion2.length > 0) {
                IntStream.range(0, this.J + 1).forEach(i -> array2[i] = this.GiantTourPortion2[i]);
                IntStream.range(this.J + 1, this.GiantTourPortion2.length).forEach(i -> array2[i + this.N + 1] = this.GiantTourPortion2[i]);
            }
            IntStream.range(0, this.N + 1).forEach(i -> array2[(this.GiantTourPortion2.length > 0) ? this.J + 1 + i : i] = this.GiantTourPortion1[(this.With2Opt) ? this.I - i : this.I - this.N + i]);
            this.GiantTourPortion1 = array1;
            this.GiantTourPortion2 = array2;
            if(this.GiantTourPortion1.length == 0) {
                this.OnePortion = true;
                this.GiantTourPortion1 = this.GiantTourPortion2;
            }
        }
    }

    @Override
    public void setGain() {
        if (this.Border == 0) {
            if (this.With2Opt) {
                this.Gain += this.getTraveledDistanceFromDepot(this.GiantTourPortion1[this.I]);
                this.Gain += this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I - this.N]);
            } 
            else {
                this.Gain += this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I]);
                this.Gain += this.getTraveledDistanceFromDepot(this.GiantTourPortion1[this.I - this.N]);
            }
        } 
        else if (this.With2Opt) {
            this.Gain += this.getTraveledDistance(this.GiantTourPortion2[this.J], this.GiantTourPortion1[this.I]);
            if (this.J + 1 < this.Border) {
                this.Gain += this.getTraveledDistance(this.GiantTourPortion1[this.I - this.N], this.GiantTourPortion2[this.J + 1]);
                this.Gain -= this.getTraveledDistance(this.GiantTourPortion2[this.J], this.GiantTourPortion2[this.J + 1]);
            } 
            else {
                this.Gain += this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I - this.N]);
                this.Gain -= this.getTraveledDistanceToDepot(this.GiantTourPortion2[this.J]);
            }
        } 
        else {
            this.Gain += this.getTraveledDistance(this.GiantTourPortion2[this.J], this.GiantTourPortion1[this.I - this.N]);
            if (this.J + 1 < this.Border) {
                this.Gain += this.getTraveledDistance(this.GiantTourPortion1[this.I], this.GiantTourPortion2[this.J + 1]);
                this.Gain -= this.getTraveledDistance(this.GiantTourPortion2[this.J], this.GiantTourPortion2[this.J + 1]);
            } 
            else {
                this.Gain += this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I]);
                this.Gain -= this.getTraveledDistanceToDepot(this.GiantTourPortion2[this.J]);
            }
        }
        if (this.I + 1 < this.FirstBorder || this.OnePortion)
            this.Gain -= this.getTraveledDistance(this.GiantTourPortion1[this.I], this.GiantTourPortion1[this.I + 1]);
        else
            this.Gain -= this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I]);
        if (this.I - this.N == 0) {
            if (this.I + 1 < this.FirstBorder || this.OnePortion)
                this.Gain += this.getTraveledDistanceFromDepot(this.GiantTourPortion1[this.I + 1]);
            this.Gain -= this.getTraveledDistanceFromDepot(this.GiantTourPortion1[this.I - this.N]);
        } 
        else {
            if (this.I + 1 < this.FirstBorder || this.OnePortion)
                this.Gain += this.getTraveledDistance(this.GiantTourPortion1[this.I - this.N - 1], this.GiantTourPortion1[this.I + 1]);
            else
                this.Gain += this.getTraveledDistanceToDepot(this.GiantTourPortion1[this.I - this.N - 1]);
            this.Gain -= this.getTraveledDistance(this.GiantTourPortion1[this.I - this.N - 1], this.GiantTourPortion1[this.I - this.N]);
        }
    }

    @Override
    public String toString() {
        if (this.N == 0)
            return this.MotionName + " (" + this.I + ";" + this.J + ")";
        else if (this.With2Opt)
            return this.MotionName + " (" + this.I + ";" + this.J + ") " + -this.N;
        return this.MotionName + " (" + this.I + ";" + this.J + ") " + this.N;
    }
}
