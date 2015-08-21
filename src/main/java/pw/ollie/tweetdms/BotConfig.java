package pw.ollie.tweetdms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public final class BotConfig {
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;

    protected BotConfig(File configFile, File accessTokenFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("consumerkey")) {
                    this.consumerKey = line.split("=")[1];
                } else if (line.startsWith("consumersecret")) {
                    this.consumerSecret = line.split("=")[1];
                }
            }
            reader.close(); // should do this properly but nah

            reader = new BufferedReader(new FileReader(accessTokenFile));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("accesstokensecret")) {
                    this.accessTokenSecret = line.split("=")[1];
                } else if (line.startsWith("accesstoken")) {
                    this.accessToken = line.split("=")[1];
                }
            }
            reader.close(); // again, nah
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
}
