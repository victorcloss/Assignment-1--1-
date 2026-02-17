# Copyright: Pierre.Flener at it.uu.se and his TAs, 2025.
# This file is part of course 1DL481 at Uppsala University, Sweden.

Made by Victor Duarte - Solo Team

# --- PARAMETERS ---
param z;                         # number of zones
param s;                         # number of service stations to be located
param v;                         # number of vehicles per service station
param c;                         # number of closest vehicles for average

set Zones := 1..z;
param Demand {Zones} >= 0;       # Demand[i] = demand of zone i
param Time {Zones,Zones} >= 0;   # Time[i,j] = time from zone i to j

# --- DECISION VARIABLES ---
var Open {Zones} binary;         # 1 if a station is opened in zone j, 0 otherwise
var Assign {Zones, Zones} integer >= 0; # Number of vehicles from station j serving zone i

# --- OBJECTIVE FUNCTION ---
# Minimize the demand-weighted average travel time
minimize TotalWeightedTime:
    sum {i in Zones} (
        Demand[i] * ( (1/c) * sum {j in Zones} (Assign[i,j] * Time[i,j]) )
    );

# --- CONSTRAINTS ---

# 1. Exactly s service stations must be opened
subject to StationLimit:
    sum {j in Zones} Open[j] = s;

# 2. Each zone i must be served by exactly c vehicles
subject to ServiceRequirement {i in Zones}:
    sum {j in Zones} Assign[i,j] = c;

# 3. Capacity/Link Constraint:
#    Zone i can receive vehicles from station j only if j is open.
#    Limits the assignment per link to v (vehicles per station).
subject to CapacityLimit {i in Zones, j in Zones}:
    Assign[i,j] <= v * Open[j];