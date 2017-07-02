package com.gugact.neuralcanvas.models;

/**
 * Created by Gustavo on 06/06/2017.
 */

public class RedeNeural {


    //numero de neurônios de entrada
    protected int qntd_entrada;

    //numero de neurônios total
    protected int qntd_neuronios;

    //numero de arestas de peso
    protected int qntd_arestas;

    //numeros neurônios camada escondida
    protected int qntd_escondida;

    //erro global
    protected double erroGlobal;

    //diferença entre proximo peso e peso atual (delta)
    protected double pesoDelta[];

    //numero de neurônios de saída
    protected int qntd_saida;

    //saídas dos neurônios
    protected double saidas_neuronios[];

    //pesos das arestas
    protected double pesos[];

    //diferença entre proximo bias e bias atual (delta)
    protected double biasDelta[];

    //erro direto do neurônio (esperado - atual) (Somatória peso x Erro)
    protected double erro[];

    //bias do neurônio
    protected double bias[];

    //taxa de aprendizado
    protected double aprendizado;

    //sigma x saída do neurônio (depois multiplica pelo aprendizado p/ descobrir delta do neurônio)
    protected double somatorioPesoDelta[];

    //sigma x saída do neurônio (depois multiplica pelo aprendizado p/ descobrir delta do bias)
    protected double somatorioBiasDelta[];

    //erro do neurônio (sigma)
    protected double sigma[];



    public RedeNeural(int nentradas,
                      int nescondida,
                      int nsaida,
                      double taprendizado) {

        this.aprendizado = taprendizado;

        this.qntd_entrada = nentradas;
        this.qntd_escondida = nescondida;
        this.qntd_saida = nsaida;
        qntd_neuronios = nentradas + nescondida + nsaida;
        qntd_arestas = (nentradas * nescondida) + (nescondida * nsaida);

        pesoDelta = new double[qntd_arestas];
        bias = new double[qntd_neuronios];
        somatorioBiasDelta = new double[qntd_neuronios];
        somatorioPesoDelta = new double[qntd_arestas];
        saidas_neuronios = new double[qntd_neuronios];
        pesos = new double[qntd_arestas];
        biasDelta = new double[qntd_neuronios];
        sigma = new double[qntd_neuronios];
        erro = new double[qntd_neuronios];

        resetar();
    }


    public double sigmoide(double sum) {
        return 1.0 / (1 + Math.exp(-1.0 * sum));
    }



    public double []computeOutputs(double entrada[]) {
        int i, j;
        final int cont_escondida = qntd_entrada;
        final int cont_saida = qntd_entrada + qntd_escondida;

        for (i = 0; i < qntd_entrada; i++) {
            saidas_neuronios[i] = entrada[i];
        }


        int contador = 0;

        for (i = cont_escondida; i < cont_saida; i++) {
            double somaNeuronio = bias[i];

            for (j = 0; j < qntd_entrada; j++) {
                somaNeuronio += saidas_neuronios[j] * pesos[contador++];
            }
            saidas_neuronios[i] = sigmoide(somaNeuronio);
        }


        double saidaRede[] = new double[qntd_saida];

        for (i = cont_saida; i < qntd_neuronios; i++) {
            double soma = bias[i];

            for (j = cont_escondida; j < cont_saida; j++) {
                soma += saidas_neuronios[j] * pesos[contador++];
            }
            saidas_neuronios[i] = sigmoide(soma);
            saidaRede[i-cont_saida] = saidas_neuronios[i];
        }

        return saidaRede;
    }



    //calcula os erros (sigmas)
    public void calcError(double saida_desejada[]) {
        int i, j;
        final int cont_escondida = qntd_entrada;
        final int cont_saida = qntd_entrada + qntd_escondida;


        for (i = qntd_entrada; i < qntd_neuronios; i++) {
            erro[i] = 0;
        }

        for (i = cont_saida; i < qntd_neuronios; i++) {
            erro[i] = saida_desejada[i - cont_saida] - saidas_neuronios[i];
            erroGlobal += erro[i] * erro[i];
            sigma[i] = erro[i] * saidas_neuronios[i] * (1 - saidas_neuronios[i]);
        }

        int contador = qntd_entrada * qntd_escondida;

        for (i = cont_saida; i < qntd_neuronios; i++) {
            for (j = cont_escondida; j < cont_saida; j++) {
                somatorioPesoDelta[contador] += sigma[i] * saidas_neuronios[j];
                erro[j] += pesos[contador] * sigma[i];
                contador++;
            }
            somatorioBiasDelta[i] += sigma[i];
        }


        for (i = cont_escondida; i < cont_saida; i++) {
            sigma[i] = erro[i] * saidas_neuronios[i] * (1 - saidas_neuronios[i]);
        }


        contador = 0;
        for (i = cont_escondida; i < cont_saida; i++) {
            for (j = 0; j < cont_escondida; j++) {
                somatorioPesoDelta[contador] += sigma[i] * saidas_neuronios[j];
                erro[j] += pesos[contador] * sigma[i];
                contador++;
            }
            somatorioBiasDelta[i] += sigma[i];
        }
    }



    //com os sigmas(erros) em mãos, podemos aplicar o aprendizado
    public void learn() {
        int i;

        //pesos correcao
        for (i = 0; i < pesos.length; i++) {
            pesoDelta[i] = (aprendizado * somatorioPesoDelta[i]);
            pesos[i] += pesoDelta[i];
            somatorioPesoDelta[i] = 0;
        }

        //bias correcao
        for (i = qntd_entrada; i < qntd_neuronios; i++) {
            biasDelta[i] = aprendizado * somatorioBiasDelta[i];
            bias[i] += biasDelta[i];
            somatorioBiasDelta[i] = 0;
        }
    }

    //reseta a rede
    public void resetar() {
        int i;

        for (i = 0; i < qntd_neuronios; i++) {
            bias[i] = 0.1d - (0.2d * (Math.random()));
            biasDelta[i] = 0;
            somatorioBiasDelta[i] = 0;
        }
        for (i = 0; i < pesos.length; i++) {
            pesos[i] = 0.1d - (0.2d * (Math.random()));
            pesoDelta[i] = 0;
            somatorioPesoDelta[i] = 0;
        }
    }

    public double getError(int tam) {
        double erro = Math.sqrt(erroGlobal / (tam * qntd_saida));
        erroGlobal = 0;
        return erro;
    }



}