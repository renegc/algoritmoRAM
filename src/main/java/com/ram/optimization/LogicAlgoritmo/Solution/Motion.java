package com.ram.optimization.LogicAlgoritmo.Solution;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class Motion {
    private final int Index1, Index2;
    
    public Motion(int i, int j){
        this.Index1 = i;
        this.Index2 = j;
    }
    
    public void Swap(int[] array){
        if(this.Index1 == this.Index2)
           return;
        int aux = array[this.Index1];
        array[this.Index1] = array[this.Index2];
        array[this.Index2] = aux;
    }  
    
    public void _2opt(int[] array){
        if(this.Index1 < this.Index2)
            for(int k = this.Index1, l = this.Index2; k < l; k++, l--)
                new Motion(k, l).Swap(array);
    }
    
    public void Insertion(int[] array){
        if(this.Index1 < this.Index2){
            int aux = array[this.Index2];
            for(int k = this.Index2; k > this.Index1; k--)
                array[k] = array[k-1];        
            array[this.Index1] = aux;
        }
    }
    
    public void InverseInsertion(int[] array){
        if(this.Index1 < this.Index2){
            int aux = array[this.Index1];
            for(int k = this.Index1; k < this.Index2; k++)
                array[k] = array[k+1];
            array[this.Index2] = aux;
        }
    }
}