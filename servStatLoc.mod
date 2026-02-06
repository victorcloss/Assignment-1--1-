# Copyright: Pierre.Flener at it.uu.se and his TAs, 2025.
# This file is part of course 1DL481 at Uppsala University, Sweden.

# SEUS DADOS
# Team: Solo Team (Victor)
# Authors: Victor Closs

# --- PARÂMETROS E CONJUNTOS (Já vieram no arquivo) ---
param z;                         # número de zonas
param s;                         # número de estações a abrir
param v;                         # veículos por estação
param c;                         # veículos mais próximos para média

set Zones := 1..z;
param Demand {Zones} >= 0;       # Demanda da zona
param Time {Zones,Zones} >= 0;   # Tempo de viagem

# --- SUAS VARIÁVEIS (DECISÕES) ---

# 1. Variável Binária (0 ou 1): Onde abrimos as estações?
var Open {Zones} binary;

# 2. Variável Inteira: Quantos veículos da estação j atendem a zona i?
#    (Integer >= 0 garante que não seja negativo)
var Assign {Zones, Zones} integer >= 0;

# --- FUNÇÃO OBJETIVO ---
# Minimizar o tempo médio ponderado pela demanda.
# Fórmula: Para cada zona, Demanda * (Média dos Tempos dos Veículos Designados)
minimize TotalWeightedTime:
    sum {i in Zones} (
        Demand[i] * ( (1/c) * sum {j in Zones} (Assign[i,j] * Time[i,j]) )
    );

# --- RESTRIÇÕES (REGRAS) ---

# 1. Número exato de estações abertas deve ser 's'
subject to StationLimit:
    sum {j in Zones} Open[j] = s;

# 2. Cada zona 'i' deve ser atendida por exatos 'c' veículos
subject to ServiceRequirement {i in Zones}:
    sum {j in Zones} Assign[i,j] = c;

# 3. Capacidade da Estação:
#    A soma de todos os veículos saindo da estação 'j' (para qualquer zona)
#    não pode ultrapassar a capacidade 'v' se a estação estiver aberta.
#    Se Open[j] for 0, então 'v * 0' é 0, impedindo qualquer veículo de sair de lá.
subject to CapacityLimit {i in Zones, j in Zones}:
    Assign[i,j] <= v * Open[j];