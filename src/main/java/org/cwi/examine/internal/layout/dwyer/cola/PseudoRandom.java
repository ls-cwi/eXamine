package org.cwi.examine.internal.layout.dwyer.cola;


import java.util.Random;

/**
 * Linear congruential pseudo random number generator.
 */
public class PseudoRandom {
    private static final double a = 214013;
    private static final double c = 2531011;
    private static final double m = Double.MAX_VALUE;   // Replacement of 2147483648, see original source below.
    private static final double range = 32767;
    
    private Random random;

    public PseudoRandom() {
        this(1);
    }
    
    public PseudoRandom(long seed) {
        this.random = new Random();
        this.random.setSeed(seed);
    }

    /**
     * Random real between 0 and 1.
     */
    public double getNext() {
        return random.nextDouble();
    }

    /**
     * Random real between min and max.
     */
    public double getNextBetween(double min, double max) {
        return min + getNext() * (max - min);
    }
    
}

/*
export class PseudoRandom {
    private a: number = 214013;
    private c: number = 2531011;
    private m: number = 2147483648;
    private range: number = 32767;

    constructor(public seed: number = 1) { }

    // random real between 0 and 1
    getNext(): number {
        this.seed = (this.seed * this.a + this.c) % this.m;
        return (this.seed >> 16) / this.range;
    }

    // random real between min and max
    getNextBetween(min: number, max: number) {
        return min + this.getNext() * (max - min);
    }
}
*/