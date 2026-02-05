#!/usr/bin/env python3


## To run this file, you may need to make it executable:
## on Linux and macOS, this can be done with: chmod +x invDesChecker

## Copyright: ChatGPT, by translation on 5 Dec 2024 of Pierre Flener's
## version in Prolog of 12 Jan 2017, with subsequent tweaks by human
## intelligence.

## Refactoring, or adding the missing check whether the input matrix
## does not have too many rows, is welcome!

import math

# Helper function for reading space/tab-separated integers
def parse_row(line):
    # Split the line into components (handle both spaces and tabs)
    row = line.split()
    int_row = [int(x) for x in row]
    return int_row

# Function to calculate row sums
def row_sums(matrix, expected_sum):
    for i, row in enumerate(matrix):
        if sum(row) != expected_sum:
            print(f"Matrix row {i+1} does not add up to {expected_sum}!")
            return False
    return True

# Function to calculate the scalar product of two rows
def scalar_product(row1, row2):
    return sum([a * b for a, b in zip(row1, row2)])

# Function to calculate the lambda value (maximum dot product)
def lambda_value(matrix):
    max_dot_product = float('-inf')
    for i in range(len(matrix)):
        for j in range(i+1, len(matrix)):
            dot_product = scalar_product(matrix[i], matrix[j])
            max_dot_product = max(max_dot_product, dot_product)
    return max_dot_product

# Function to read the incidence matrix
def read_matrix(num_rows, num_cols):
    matrix = []
    for _ in range(num_rows):
        line = input()
        row = parse_row(line)
        if len(row) != num_cols or any((i not in {0,1} for i in row)):
            print(f"At least one matrix row does not have {num_cols} zeros and ones!")
            return None
        matrix.append(row)
    return matrix  # human intelligence: this doesn't catch superfluous rows!

# Main function that encapsulates the Prolog code
def go():
    try:
        # Read the first line and parse it (expecting 5 integers)
        first_line = input()
        first_values = parse_row(first_line)
        if len(first_values) != 5:
            print("There are not five integers in the first line!")
            return False
        
        V, B, R, LowerBound, Lambda = first_values

        # Check lower bound calculation
        RVdivB = (R * V) / B
        RVmodB = (R * V) % B
        Numerator1 = (math.ceil(RVdivB) ** 2) * RVmodB
        Numerator2 = (math.floor(RVdivB) ** 2) * (B - RVmodB)
        Denominator = V * (V - 1)
        ActualBound = math.ceil((Numerator1 + Numerator2 - R * V) / Denominator)
        
        if LowerBound != ActualBound:
            print(f"The expected lower bound is {ActualBound}, not the claimed {LowerBound}!")
            return False
        
        # Read the incidence matrix
        matrix = read_matrix(V, B)
        if matrix is None:
            return False
        
        # Check row sums
        if not row_sums(matrix, R):
            return False

        # Calculate lambda and compare
        ActualLambda = lambda_value(matrix)
        if Lambda != ActualLambda:
            print(f"The largest observed dot product is {ActualLambda}, not the claimed {Lambda}!")
            return False

        # Final confirmation
        print("It looks like all is fine for this design!")
        if Lambda == LowerBound:
            print("Since the largest dot product equals its lower bound, this design is actually optimal!")
        else:
            print("Beware, no comment is made on the optimality of this design!")
        return True

    except Exception as e:
        print(f"Error: {e}")
    
    return False

# Run the main function
if __name__ == "__main__":
    success = go()
    if success:
        exit(0)
    exit(1)
