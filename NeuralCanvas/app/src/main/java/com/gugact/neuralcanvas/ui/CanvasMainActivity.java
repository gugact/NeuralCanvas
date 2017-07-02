package com.gugact.neuralcanvas.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gugact.neuralcanvas.R;
import com.gugact.neuralcanvas.models.RedeNeural;
import com.gugact.neuralcanvas.models.TestList;
import com.squareup.seismic.ShakeDetector;

import java.text.NumberFormat;
import java.util.ArrayList;

public class CanvasMainActivity extends AppCompatActivity implements ShakeDetector.Listener {

    RelativeLayout relativeLayout;
    Paint paint;
    SketchSheetView view;
    Path path2;
    Bitmap bitmap;
    Bitmap reducedBitmap;
    Canvas canvas;
    ImageView previewImg;
    int size;
    private boolean trained = false;
    private RedeNeural digitNeuralNetwork;
    private Handler updateBarHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_main);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativelayout1);

        updateBarHandler = new Handler();

        previewImg = (ImageView) findViewById(R.id.preview_20px_image);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        size = displayMetrics.widthPixels;


        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);

        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(size,size));



        view = new SketchSheetView(CanvasMainActivity.this);

        paint = new Paint();

        path2 = new Path();




        relativeLayout.addView(view, new LayoutParams(
                size,
                size));

        paint.setDither(true);

        paint.setColor(Color.parseColor("#000000"));

        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeJoin(Paint.Join.ROUND);

        paint.setStrokeCap(Paint.Cap.ROUND);

//        paint.setStrokeWidth(50);
        paint.setStrokeWidth(150);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_recognize_menu) {

            if(!trained){
                displayTrainingAlert();
            }else{

                transferAndResizeBitmap();
                displayResult(treatResult(digitNeuralNetwork.computeOutputs(getInput())));

            }
            return true;
        }
        if(id == R.id.action_train_menu){
            setUpNetwork();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpNetwork(){


        final Dialog dialog = constructDialog();

        final TestList test = new TestList(this);

        digitNeuralNetwork = new RedeNeural(900,90,10,0.35);

        final NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMinimumFractionDigits(4);


//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        pd.setMessage("Treinando...");
//        pd.setMax(200);
//        pd.setCancelable(false);
//        pd.show();
        Thread mThread = new Thread() {
            @Override
            public void run() {

                for (int i=0;i<200;i++) {
                    for (int j=0;j<test.inputList.size();j++) {
                        digitNeuralNetwork.computeOutputs(test.inputList.get(j));
                        digitNeuralNetwork.calcError(test.saidaDesejada[j]);
                        digitNeuralNetwork.learn();
                    }
//                    updateBarHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            pd.incrementProgressBy(1);
//                            if(pd.getMax()== pd.getProgress()){
//                                pd.dismiss();
//                            }
//                        }
//                    });

                    Log.d("Epoch ", i + " ,Error: " + percentFormat .format(digitNeuralNetwork.getError(test.inputList.size())) );
                }

                dialog.dismiss();

//                pd.dismiss();
            }
        };
        mThread.start();
        trained = true;
    }


    @Override
    public void hearShake() {
        path2.reset();
        view.invalidate();
        view.resetSketch();
    }

    class SketchSheetView extends View {

        public void resetSketch(){
            bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);

            canvas = new Canvas(bitmap);

            this.setBackgroundColor(Color.WHITE);
        }

        public SketchSheetView(Context context) {

            super(context);

            bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);

            canvas = new Canvas(bitmap);

            this.setBackgroundColor(Color.WHITE);
        }

        private ArrayList<DrawingClass> DrawingClassArrayList = new ArrayList<DrawingClass>();




        @Override
        public boolean onTouchEvent(MotionEvent event) {

            DrawingClass pathWithPaint = new DrawingClass();

            canvas.drawPath(path2, paint);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                path2.moveTo(event.getX(), event.getY());

                path2.lineTo(event.getX(), event.getY());
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                path2.lineTo(event.getX(), event.getY());

                pathWithPaint.setPath(path2);

                pathWithPaint.setPaint(paint);

                DrawingClassArrayList.add(pathWithPaint);
            }

            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (DrawingClassArrayList.size() > 0) {

                canvas.drawPath(
                        DrawingClassArrayList.get(DrawingClassArrayList.size() - 1).getPath(),

                        DrawingClassArrayList.get(DrawingClassArrayList.size() - 1).getPaint());
            }
        }
    }

    public class DrawingClass {

        Path DrawingClassPath;
        Paint DrawingClassPaint;

        public Path getPath() {
            return DrawingClassPath;
        }

        public void setPath(Path path) {
            this.DrawingClassPath = path;
        }


        public Paint getPaint() {
            return DrawingClassPaint;
        }

        public void setPaint(Paint paint) {
            this.DrawingClassPaint = paint;
        }
    }

    public int treatResult(double[] result){

        double returned_result = result[0];
        int chosen_index = 0;

        for(int j=0;j<result.length;j++){
//            System.out.println(result[j] + " : ");
            Log.d("perceptron " + j + ": ",""+ result[j]);
        }

        for(int i=0; i<result.length;i++){
            if(returned_result < result[i]){
                returned_result = result[i];
                chosen_index = i;
            }
        }
        if(result[chosen_index] > 0.01){
            return chosen_index;
        }else{
            return -1;
        }
    }



    public void displayResult(int result){

        if(result != -1){
            AlertDialog alertDialog = new AlertDialog.Builder(CanvasMainActivity.this).create();
            alertDialog.setMessage(String.valueOf(result));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }else{
            AlertDialog alertDialog = new AlertDialog.Builder(CanvasMainActivity.this).create();
            alertDialog.setMessage(getString(R.string.invalid_digit));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }


    public void displayTrainingAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(CanvasMainActivity.this).create();
        alertDialog.setMessage(getString(R.string.train_before));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    public void transferAndResizeBitmap(){

        reducedBitmap = bitmap.copy(bitmap.getConfig() , true);
        reducedBitmap = resizeImage(reducedBitmap , 30);
        int height = reducedBitmap.getHeight();
        int widht = reducedBitmap.getWidth();
        previewImg.setImageBitmap(reducedBitmap);
//        getInput();

    }


    public double[] getInput(){

//        String final_result = "";

        int positionResult = 0;
        double[] result = new double[900];

        for (int y = 0; y < 30; y++) {

            for(int x = 0 ; x < 30; x ++){

                if(reducedBitmap.getPixel(x,y) == 0){
                    result[positionResult] = 0;
//                    final_result = final_result.concat(" 0");
                }else{
                    result[positionResult] = 1;
//                    final_result = final_result.concat(" 1");
                }

//                final_result = final_result.concat(", " + String.valueOf(result[positionResult]));
                positionResult++;
            }
        }
//        Log.d("matrix: " + positionResult + " : ", final_result);
        return result;

    }

    private Bitmap resizeImage(Bitmap bitmap, int newSize){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = 0;
        int newHeight = 0;

        if(width > height){
            newWidth = newSize;
            newHeight = (newSize * height)/width;
        } else if(width < height){
            newHeight = newSize;
            newWidth = (newSize * width)/height;
        } else if (width == height){
            newHeight = newSize;
            newWidth = newSize;
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                width, height, matrix, true);

        return resizedBitmap;
    }

    private Dialog constructDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_training_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        return dialog;
    }


}