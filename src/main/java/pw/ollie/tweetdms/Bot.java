package pw.ollie.tweetdms;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.api.DirectMessagesResources;
import twitter4j.api.TweetsResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main Bot class. God object because I can't be arsed to do it properly right now
 */
public final class Bot implements Runnable {
    private final File directory = new File("./");
    private final DateFormat dateFormat = new SimpleDateFormat("dd/mm/yy HH:mm:ss");
    private final Twitter twitter;

    private Bot() {
        File configFile = new File(directory, "botconfiguration.conf");
        File accessTokenFile = new File(directory, "accesstoken.conf");

        if (!configFile.exists()) {
            log("Bot configuration file not found, attempting to create default...");

            try {
                configFile.createNewFile();
                log("Created config file, please fill it in then restart app");
                System.exit(0);
            } catch (IOException e) {
                err("Could not create default bot configuration file");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        if (!accessTokenFile.exists()) {
            log("Access token file not found, attempting to create...");

            try {
                accessTokenFile.createNewFile();
            } catch (IOException e) {
                err("Could not create access token file");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        BotConfig botConfig = new BotConfig(configFile, accessTokenFile);

        Twitter twitter = null;
        if (botConfig.getAccessToken() == null && botConfig.getAccessTokenSecret() == null) {
            try {
                twitter = TwitterFactory.getSingleton();
                twitter.setOAuthConsumer(botConfig.getConsumerKey(), botConfig.getConsumerSecret());
                RequestToken requestToken = twitter.getOAuthRequestToken();
                AccessToken accessToken = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while (accessToken == null) {
                    System.out.println("Open the following URL and grant access to your account:");
                    System.out.println(requestToken.getAuthorizationURL());
                    System.out.print("Enter the PIN (if available) or just hit enter. [PIN]:");
                    String pin = br.readLine();
                    try {
                        if (pin.length() > 0) {
                            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } else {
                            accessToken = twitter.getOAuthAccessToken();
                        }
                    } catch (TwitterException e) {
                        if (401 == e.getStatusCode()) {
                            System.out.println("Unable to get the access token");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }

                twitter.verifyCredentials();
                BufferedWriter writer = new BufferedWriter(new FileWriter(accessTokenFile));
                writer.write("accesstoken=" + accessToken.getToken());
                writer.newLine();
                writer.write("accesstokensecret=" + accessToken.getTokenSecret());
                writer.close();
            } catch (TwitterException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (twitter != null) {
            this.twitter = twitter;
        } else {
            TwitterFactory factory = new TwitterFactory();
            this.twitter = factory.getInstance();
            AccessToken token = new AccessToken(botConfig.getAccessToken(), botConfig.getAccessTokenSecret());
            this.twitter.setOAuthConsumer(botConfig.getConsumerKey(), botConfig.getConsumerSecret());
            this.twitter.setOAuthAccessToken(token);
        }
    }

    @Override
    public void run() {
        try {
            DirectMessagesResources dms = twitter.directMessages();
            TweetsResources tweets = twitter.tweets();
            ResponseList<DirectMessage> messages = dms.getDirectMessages();

            for (DirectMessage message : messages) {
                tweets.updateStatus(message.getText());
                dms.destroyDirectMessage(message.getId());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        System.out.println("[" + dateFormat.format(new Date()) + "] " + message);
    }

    private void err(String message) {
        System.err.println("[" + dateFormat.format(new Date()) + "] " + message);
    }

    public static void main(String[] args) {
        Bot bot = new Bot();
        bot.run();
    }
}
