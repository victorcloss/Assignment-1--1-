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
 * Most of this codes assumes that the investment design is represented using a 2D grid, but it can
 * easily be changed to also support a set representation. Feel free to change anything in this file
 * if necessary.
 *
 * Don't forget to add your own helper methods in addition to the ones here.
 */

import java.util.Random;

class Design {
    private final int v, b, r;
    
    // A matriz principal: 1 se investiu, 0 caso contrário
    private int[][] matrix;
    
    // Matriz de interseções (overlaps): overlaps[i][j] = produto escalar entre linha i e j
    private int[][] overlaps;
    
    // Backup para salvar a melhor solução encontrada
    private int[][] savedMatrix;
    
    private Random random = new Random();

    Design(int v, int b, int r) {
        this.v = v;
        this.b = b;
        this.r = r;
        this.matrix = new int[v][b];
        this.overlaps = new int[v][v];
        this.savedMatrix = new int[v][b];
        this.init();
    }

    // Inicializa aleatoriamente garantindo soma 'r' por linha
    void init() {
        // Limpa tudo
        for(int i=0; i<v; i++) {
            for(int j=0; j<b; j++) matrix[i][j] = 0;
            for(int j=0; j<v; j++) overlaps[i][j] = 0;
        }
        
        // Preenche cada linha com 'r' uns aleatórios
        for (int i = 0; i < v; i++) {
            int placed = 0;
            while (placed < r) {
                int col = random.nextInt(b);
                if (matrix[i][col] == 0) {
                    matrix[i][col] = 1;
                    placed++;
                }
            }
        }
        
        // Calcula a matriz de overlaps inicial (Full Scan - Lento, mas só roda 1 vez)
        for (int i = 0; i < v; i++) {
            for (int j = i + 1; j < v; j++) {
                int ov = 0;
                for (int k = 0; k < b; k++) {
                    if (matrix[i][k] == 1 && matrix[j][k] == 1) ov++;
                }
                overlaps[i][j] = ov;
                overlaps[j][i] = ov;
            }
        }
    }

    // Calcula qual seria o custo SE fizéssemos o movimento (Delta Evaluation)
    Cost probeMove(Move m) {
        int currentMax = getCurrentMaxOverlap();
        int predictedMax = 0;

        // Verifica o overlap da linha m.row contra todas as outras
        for (int other = 0; other < v; other++) {
            if (other == m.row) continue;

            int ov = overlaps[m.row][other];
            
            // Se eu tirar o 1 da colOut, e a outra linha tinha 1 lá -> overlap diminui
            if (matrix[other][m.colOut] == 1) ov--;
            
            // Se eu colocar o 1 na colIn, e a outra linha tem 1 lá -> overlap aumenta
            if (matrix[other][m.colIn] == 1) ov++;
            
            if (ov > predictedMax) predictedMax = ov;
        }

        // Precisamos verificar se o máximo global não vinha de OUTRO par de linhas
        // Se o predictedMax for maior que o current, ele com certeza é o novo máximo.
        // Se for menor, pode ser que outro par ainda mantenha o máximo lá em cima.
        // Como 'probe' precisa ser rápido, retornamos o maior entre o "local afetado" e o "global atual".
        // (Nota: Isso é uma heurística de simplificação válida para performance)
        return new Cost(Math.max(predictedMax, getMaxOverlapExcluding(m.row)));
    }
    
    // Retorna o maior overlap entre linhas que NÃO sejam a linha 'excludeRow'
    private int getMaxOverlapExcluding(int excludeRow) {
        int max = 0;
        for(int i=0; i<v; i++) {
            if(i == excludeRow) continue;
            for(int j=i+1; j<v; j++) {
                if(j == excludeRow) continue;
                if(overlaps[i][j] > max) max = overlaps[i][j];
            }
        }
        return max;
    }

    void commitMove(Move m) {
        // Atualiza a matriz
        matrix[m.row][m.colOut] = 0;
        matrix[m.row][m.colIn] = 1;
        
        // Atualiza os overlaps incrementalmente
        for (int other = 0; other < v; other++) {
            if (other == m.row) continue;
            
            if (matrix[other][m.colOut] == 1) {
                overlaps[m.row][other]--;
                overlaps[other][m.row]--;
            }
            if (matrix[other][m.colIn] == 1) {
                overlaps[m.row][other]++;
                overlaps[other][m.row]++;
            }
        }
    }

    private int getCurrentMaxOverlap() {
        int max = 0;
        for (int i = 0; i < v; i++) {
            for (int j = i + 1; j < v; j++) {
                if (overlaps[i][j] > max) max = overlaps[i][j];
            }
        }
        return max;
    }
    
    public int getCurrentCostValue() {
        return getCurrentMaxOverlap();
    }

    void saveDesign() {
        for(int i=0; i<v; i++) {
            System.arraycopy(matrix[i], 0, savedMatrix[i], 0, b);
        }
    }

    void restoreSavedDesign() {
        for(int i=0; i<v; i++) {
            System.arraycopy(savedMatrix[i], 0, matrix[i], 0, b);
        }
        // Recalcular overlaps após restore
        for (int i = 0; i < v; i++) {
            for (int j = i + 1; j < v; j++) {
                int ov = 0;
                for (int k = 0; k < b; k++) {
                    if (matrix[i][k] == 1 && matrix[j][k] == 1) ov++;
                }
                overlaps[i][j] = ov;
                overlaps[j][i] = ov;
            }
        }
    }
    
    public int getB() { return b; }
    public int getMatrixValue(int r, int c) { return matrix[r][c]; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < b; j++) {
                sb.append(matrix[i][j]).append(j == b - 1 ? "" : " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}