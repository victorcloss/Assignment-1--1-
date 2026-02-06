/*
 * Copyright (c) 2019: Gustav Björdal, gustav.bjordal@it.uu.se.
 *
 * This file is part of course 1DL481 at Uppsala University, Sweden.
 *
 * Permission is hereby granted only to the registered students of that course to use this file, for
 * a homework assignment.
 *
 * The copyright notice and permission notice above shall be included in all copies and extensions
 * of this file, and those are not allowed to appear publicly on the internet, both during a course
 * instance and forever after.
 *
 */


/**
 * Used to store the Cost in case the cost is represented by something more complex than an integer.
 *
 * Feel free to change anything in this file if necessary.
 */
class Cost implements Comparable<Cost> {
    public int value;

    public Cost(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public boolean isEqualTo(Cost c) {
        return this.value == c.value;
    }

    public boolean isBetterThan(Cost c) {
        return this.value < c.value; // Menor é melhor
    }

    @Override
    public int compareTo(Cost o) {
        return Integer.compare(this.value, o.value);
    }
}
