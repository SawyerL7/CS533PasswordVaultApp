package com.smd.passwordvault.helpers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GeneratePassword {

    public static void GeneratePassword() throws Exception {
        String apiURL = "https://randommer.io/api/Text/Password?length=12&hasDigits=true&hasUppercase=true&hasSpecial=true";
        String apiKey = "34d00eae19e04f6eb2123cdf2a59e1e1";

        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-Api-Key", apiKey);
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        int response = con.getResponseCode();

        if (response == 200) {
            InputStream is = con.getInputStream();
            Scanner s = new Scanner(is);
            String result = s.hasNext() ? s.next() : "";
            System.out.println(result);
        }
        else {
            throw new RuntimeException("HttpResponseCode: " + response);
        }
    }
}
