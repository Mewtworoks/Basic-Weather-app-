package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView cityNameText, weatherInfoText;
    Button displayButton;

    public class DownloadTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e){
                e.printStackTrace();
                return null; // Changed from "Failed" to null for better error handling
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect(); // Ensure connection is closed
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                Toast.makeText(getApplicationContext(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                weatherInfoText.setText("");
                return; // Added to prevent further processing if data retrieval failed
            }

            try {
                String description = null, temperature = null, humidity = null;
                JSONObject jsonObject = new JSONObject(s);

                JSONArray weatherInfoArray = jsonObject.getJSONArray("weather");
                if (weatherInfoArray.length() > 0) {
                    description = weatherInfoArray.getJSONObject(0).getString("description");
                }

                JSONObject mainInfoObject = jsonObject.getJSONObject("main");
                temperature = mainInfoObject.getString("temp");
                humidity = mainInfoObject.getString("humidity");

                // Capitalize the first letter of the description
                if (description != null && !description.isEmpty()) {
                    description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
                }

                String infoText = description + ".\nTemperature: " + temperature + "°C\nHumidity: " + humidity + "%";
                weatherInfoText.setText(infoText);

                Log.i("Weather Info", infoText);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Enter a proper city name", Toast.LENGTH_SHORT).show();
                weatherInfoText.setText("");
                e.printStackTrace(); // Added to log the stack trace for debugging
            }
        }
    }

    public void buttonClicked(View view){
        String cityName = cityNameText.getText().toString();
        if (cityName.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadTask task = new DownloadTask();
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Added to log the stack trace for debugging
        }

        task.execute("https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=YOUR_API_KEY"); // Use correct OpenWeatherMap API endpoint
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameText = findViewById(R.id.cityNameText);
        weatherInfoText = findViewById(R.id.weatherInfoText);
        displayButton = findViewById(R.id.displayButton);

        displayButton.setOnClickListener(this::buttonClicked); // Set onClickListener for the button
    }
}
