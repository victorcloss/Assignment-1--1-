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
 * We here assume that tabu search is used. While it is highly recommended that you use tabu search
 * for this assignment, you are allowed to implement simulated annealing instead.
 *
 */
/*
 * Copyright (c) 2019: Gustav Björdal, gustav.bjordal@it.uu.se.
 */

import java.util.Random;

class LocalSearch {
    private final Design design;
    private final int v, b, r, lb;
    private final Tabu tabu;
    private int it; // Iteração atual
    private int bestLambda; // Melhor custo encontrado
    private int alpha, beta;
    private Random random = new Random();

    public LocalSearch(int v, int b, int r, int alpha, int beta) {
        this.v = v;
        this.b = b;
        this.r = r;
        this.lb = calculateLb();
        this.design = new Design(v, b, r);
        // Tenure fixo de 10 iterações (simples e funcional)
        this.tabu = new Tabu(10, v, b, r); 
        this.it = 0;
        this.alpha = alpha;
        this.beta = beta;
    }

    // Fórmula matemática do Assignment PDF
    private int calculateLb() {
        long num = (long) (r * v % b) * (long) Math.pow((r * v / b) + 1, 2) 
                 + (long) (b - (r * v % b)) * (long) Math.pow(r * v / b, 2) 
                 - (long)r * v;
        long den = (long) v * (v - 1);
        return (int) Math.ceil((double) num / den);
    }

    void run() {
        design.init();
        design.saveDesign(); // Salva o inicial como o "melhor" por enquanto
        bestLambda = design.getCurrentCostValue();
        
        Cost bestCost = new Cost(bestLambda);
        long startTime = System.currentTimeMillis();

        // Roda por 300 segundos (conforme enunciado) ou até achar o ótimo
        while (bestLambda > lb && (System.currentTimeMillis() - startTime) < 290000) {
            it++;
            
            // Estratégia: Pegar o MELHOR vizinho (Best Improvement)
            // Se for muito lento para instâncias grandes, pode trocar por First Improvement
            Move bestMove = null;
            Cost bestMoveCost = new Cost(Integer.MAX_VALUE);
            
            // Tenta 100 movimentos aleatórios por iteração (Stochastic)
            // Para ser exaustivo, teríamos que iterar todos v*b*(b-r), o que é lento.
            // Amostragem aleatória funciona bem para SLS.
            for(int k=0; k<200; k++) {
                Move m = getRandomNeighbour();
                if(m == null) continue;
                
                // Checa Tabu
                // Se for Tabu, só aceita se melhorar o melhor global (Aspiration Criteria)
                boolean isTabu = tabu.isTabu(m.row, m.colIn, it) || tabu.isTabu(m.row, m.colOut, it);
                Cost c = design.probeMove(m);
                
                if (!isTabu || c.value < bestLambda) {
                    if (c.isBetterThan(bestMoveCost)) {
                        bestMoveCost = c;
                        bestMove = m;
                    }
                }
            }
            
            // Se achou algum movimento válido
            if (bestMove != null) {
                design.commitMove(bestMove);
                tabu.makeTabu(bestMove.row, bestMove.colOut, it); // Proíbe desfazer (voltar o 1 pra colOut)
                tabu.makeTabu(bestMove.row, bestMove.colIn, it);  // Proíbe mexer no novo 1
                
                if (bestMoveCost.value < bestLambda) {
                    bestLambda = bestMoveCost.value;
                    design.saveDesign();
                    // System.err.println("New Best: " + bestLambda + " at iter " + it);
                }
            }
        }

        // Fim da busca: restaura o melhor e imprime
        design.restoreSavedDesign();
        System.out.println(v + " " + b + " " + r + " " + lb + " " + bestLambda);
        System.out.print(design.toString());
    }

    private Move getRandomNeighbour() {
        // Escolhe uma linha aleatória
        int row = random.nextInt(v);
        
        // Procura um 1 aleatório e um 0 aleatório na mesma linha
        int colOut = -1;
        int colIn = -1;
        
        // Tenta achar colunas válidas (segurança contra loop infinito)
        for(int k=0; k<100; k++) {
            int c = random.nextInt(b);
            if(design.getMatrixValue(row, c) == 1) colOut = c;
        }
        for(int k=0; k<100; k++) {
            int c = random.nextInt(b);
            if(design.getMatrixValue(row, c) == 0) colIn = c;
        }
        
        if(colOut != -1 && colIn != -1) {
            return new Move(row, colOut, colIn);
        }
        return null;
    }
    
    // Métodos opcionais que não usamos mas o esqueleto pedia
    private Move getBestNeighbour() { return null; }
    private Move getFirstImprovingNeighbour() { return null; }

    public String getOutput() {
        design.restoreSavedDesign();
        return v + " " + b + " " + r + " " + lb + " " + bestLambda + "\n" + design.toString();
    }
}