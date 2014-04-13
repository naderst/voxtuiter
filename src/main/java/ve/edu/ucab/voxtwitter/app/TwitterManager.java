/*
    Vox Twitter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtwitter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtwitter.app;

import android.content.Intent;
import android.speech.RecognizerIntent;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 */
public class TwitterManager {
    private TwitterFactory tf;
    private Twitter twitter;

    TwitterManager(final MainActivity mainActivity) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey(Configuration.consumerKey)
                .setOAuthConsumerSecret(Configuration.consumerSecret);
        tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();

        try {
            mainActivity.openURL(twitter.getOAuthRequestToken().getAuthorizationURL());
            System.out.println(mainActivity.listenSpeech().toString());
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publica un tweet
     * @param text Texto a publicar en el tweet
     */
    public void tweet(String text) {
        try {
            twitter.updateStatus(text);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
}
