package com.example.recipe_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkingService {
    private final String recipeURL1 = "https://api.edamam.com/api/recipes/v2?type=public&q=";
    private final String recipeURL2 = "&app_id=3c909633&app_key=4fba14d24673b86dee4f890ced2fe370";

    //provides multi-threading
    public static ExecutorService networkExecutorService = Executors.newFixedThreadPool(4);

    //needed to return data from background thread to main thread
    public static Handler networkHandler = new Handler(Looper.getMainLooper());

    interface NetworkingListener {
        void dataListener(String jsonRecipeString);

        void imageListener(Bitmap image);
    }

    NetworkingListener listener;

    public void getRecipeData(String recipeName) {
        searchForRecipes(recipeName);
    }

    public void searchForRecipes(String recipeChars) {
        networkExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection;

                try {
                    StringBuilder jsonData = new StringBuilder();

                    //complete URL for API
                    String completeURL = recipeURL1 + recipeChars + recipeURL2;

                    //create a URL object from the completeURL String
                    URL urlObj = new URL(completeURL);

                    httpURLConnection = (HttpURLConnection) urlObj.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
                    httpURLConnection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                    httpURLConnection.setRequestProperty("authority", "api.edamam.com");

                    InputStream in = httpURLConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(reader);

                    //read the data
                    String theLine;
                    while ((theLine = br.readLine()) != null) {
                        jsonData.append(theLine);
                    }

                    final String finalData = jsonData.toString();

                    //return data from background thread to main thread
                    networkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.dataListener(finalData);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getImageData(String imageUrl) {
        networkExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL urlObj = new URL(imageUrl);

                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) urlObj.getContent());

                    networkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.imageListener(bitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}