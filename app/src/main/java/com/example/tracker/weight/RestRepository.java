package com.example.tracker.weight;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class RestRepository {

    public List<WeightDto> loadWeights() {
        Gson gson = new Gson();

        String response = "";
        try {
            response = new HttpsGetRequest().execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // TODO: Fix Weight conversion, it always returns 0
        Weight[] weights = gson.fromJson(response, Weight[].class);
        List<WeightDto> weightDtos = new ArrayList<>();
        Arrays.asList(weights).forEach(weight -> weightDtos.add(weight.toWeightDto()));
        return weightDtos;
    }

    static class Weight {
        String id;
        long timeInMillis;
        double weightInKgs;

        WeightDto toWeightDto() {
            return new WeightDto(timeInMillis, weightInKgs);
        }
    }

    static class HttpsGetRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... urls) {

            try {
                // this is so we don't have to touch the hosts.txt file on the android device
                HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> "192.168.0.110".equals(s));

                URL url = new URL("https://192.168.0.110:5000");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                return content.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}
