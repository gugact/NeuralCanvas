package com.gugact.neuralcanvas.models;

import com.gugact.neuralcanvas.R;
import com.gugact.neuralcanvas.ui.CanvasMainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Gustavo on 07/06/2017.
 */

public class TestList {

    public ArrayList<double[]> inputList;
    public CanvasMainActivity mainActivity;

    public TestList(CanvasMainActivity activity){
        this.mainActivity = activity;
        this.inputList = new ArrayList<>();
        getTrainingSetFromFile();

    }





    public final double[][] saidaDesejada =
                {
                        {1,0,0,0,0,0,0,0,0,0}, //ZERO
                        {1,0,0,0,0,0,0,0,0,0}, //ZERO
                        {0,1,0,0,0,0,0,0,0,0}, //UM
                        {0,1,0,0,0,0,0,0,0,0}, //UM
                        {0,0,1,0,0,0,0,0,0,0}, //DOIS
                        {0,0,1,0,0,0,0,0,0,0}, //DOIS
                        {0,0,0,1,0,0,0,0,0,0}, //TRES
                        {0,0,0,1,0,0,0,0,0,0}, //TRES
                        {0,0,0,0,1,0,0,0,0,0}, //QUATRO
                        {0,0,0,0,1,0,0,0,0,0}, //QUATRO
                        {0,0,0,0,0,1,0,0,0,0}, //CINCO
                        {0,0,0,0,0,1,0,0,0,0}, //CINCO
                        {0,0,0,0,0,0,1,0,0,0}, //SEIS
                        {0,0,0,0,0,0,1,0,0,0}, //SEIS
                        {0,0,0,0,0,0,0,1,0,0}, //SETE
                        {0,0,0,0,0,0,0,1,0,0}, //SETE
                        {0,0,0,0,0,0,0,0,1,0}, //OITO
                        {0,0,0,0,0,0,0,0,1,0}, //OITO
                        {0,0,0,0,0,0,0,0,0,1},  //NOVE
                        {0,0,0,0,0,0,0,0,0,1}  //NOVE
                };


    public void getTrainingSetFromFile(){

        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(mainActivity.getResources().openRawResource(R.raw.learning_digits)));




            for(int digits=0;digits <20;digits++){

                ArrayList<String> zerosAndOnes = new ArrayList<>();

                for(int lines=0;lines<30;lines++){


                    zerosAndOnes.addAll(Arrays.asList(bReader.readLine().split(" ")));


                }
                bReader.readLine();
                bReader.readLine();


                int counter =0;
                double[] digit_pattern = new double[900];
                for(String digit: zerosAndOnes){
                    digit_pattern[counter] =Long.parseLong(digit);
                    counter++;
                }

                inputList.add(digit_pattern);



            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
