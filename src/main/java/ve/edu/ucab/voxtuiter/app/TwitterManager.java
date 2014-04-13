/*
    Vox Tuiter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtuiter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtuiter.app;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Interfaz para manejar el API de Twitter
 */
public class TwitterManager {
    private TwitterFactory tf;
    private Twitter twitter;

    TwitterManager(MainActivity mainActivity) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey(Configuration.consumerKey)
                .setOAuthConsumerSecret(Configuration.consumerSecret);

        if(!mainActivity.read("accessToken").isEmpty() && !mainActivity.read("accessSecret").isEmpty()) {
            cb.setOAuthAccessToken(mainActivity.read("accessToken")).setOAuthAccessTokenSecret(mainActivity.read("accessSecret"));
            tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();
        } else {
            tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();

            RequestToken requestToken = null;

            try {
                requestToken = twitter.getOAuthRequestToken();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            AccessToken accessToken = null;

            mainActivity.speak("Será enviado a la página de Twitter, inicie sesión y diga el número de PIN");
            mainActivity.openURL(requestToken.getAuthorizationURL());

            try {
                String pin = mainActivity.listenSpeech().get(0).replace(" ", "");
                System.out.println(pin);
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            } catch (TwitterException te) {
                if(te.getStatusCode() == 401) {
                    mainActivity.speak("PIN incorrecto");
                    System.exit(1);
                } else {
                    te.printStackTrace();
                }
            }

            mainActivity.save("accessToken", accessToken.getToken());
            mainActivity.save("accessSecret", accessToken.getTokenSecret());
        }
    }

    /**
     * Publica un tweet
     *
     * @param text Texto a publicar en el tweet
     */
    public void tweet(String text) {
        try {
            twitter.updateStatus(text);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retuitea un tweet
     *
     * @param tweetId ID del tweet
     */
    public void retweet(long tweetId) {
        try {
            twitter.retweetStatus(tweetId);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Añade un tweet a favoritos
     *
     * @param tweetId ID del tweet
     */
    public void fav(long tweetId) {
        try {
            twitter.createFavorite(tweetId);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el TimeLine principal de la cuenta de Twitter del usuario
     * @return Lista con los tweets del timeline o null en caso de error
     */
    public ResponseList<Status> getTimeLine() {
        try {
            return twitter.getHomeTimeline();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
