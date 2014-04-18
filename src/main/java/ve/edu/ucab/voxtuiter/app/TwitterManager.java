/*
    Vox Tuiter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtuiter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtuiter.app;

import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Trend;
import twitter4j.Query;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Interfaz para manejar la API de Twitter
 */
public class TwitterManager {
    private TwitterFactory tf;
    private Twitter twitter;
    private MainActivity mainActivity;

    TwitterManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        ConfigurationBuilder cb = new ConfigurationBuilder();
        /* Configuramos oAuth con las claves otorgadas por Twitter */
        cb.setDebugEnabled(true).setOAuthConsumerKey(Configuration.consumerKey)
                .setOAuthConsumerSecret(Configuration.consumerSecret);

        /*
            Verificamos si hay tokens de acceso almacenados, si los hay los utilizamos para
            autenticar...
         */
        if(!mainActivity.read("accessToken").isEmpty() && !mainActivity.read("accessSecret").isEmpty()) {
            cb.setOAuthAccessToken(mainActivity.read("accessToken")).setOAuthAccessTokenSecret(mainActivity.read("accessSecret"));
            tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();
        } else {
            /*
            ...de lo contrario debemos enviar al usuario a la página de Twitter
            para autorizar la App, el usuario debe dictar el PIN generado por Twitter, de manera
            que la App pueda completar la autenticación, por último guardamos las credenciales
            (tokens de acceso) para que el usuario no vuelva a pasar por este proceso.
             */
            tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();

            RequestToken requestToken = null;

            try {
                requestToken = twitter.getOAuthRequestToken();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            AccessToken accessToken = null;

            mainActivity.speak("Será enviado a la página de Twitter, inicie sesión y diga el número de PIN.");
            mainActivity.openURL(requestToken.getAuthorizationURL());

            try {
                String pin = mainActivity.listenSpeech().get(0).replace(" ", "");
                System.out.println(pin);
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            } catch (TwitterException te) {
                if(te.getStatusCode() == 401) {
                    mainActivity.speak("PIN incorrecto.");
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
            mainActivity.speak("Tweet publicado con éxito.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo escribir el tweet indicado.");
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
            mainActivity.speak("Retweet exitoso.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo hacer retweet en el tweet seleccionado.");
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
            mainActivity.speak("Se marcó el tweet como favorito.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo marcar como favorito el tweet seleccionado.");
        }
    }

    public void reply(long tweetId, String reply){
        try {
            twitter.updateStatus(new StatusUpdate(reply).inReplyToStatusId(tweetId));
            mainActivity.speak("Se ha respondido el tweet selecccionado con éxito.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo responder el tweet seleccionado.");
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
            mainActivity.speak("No se pudo obtener el time line de twitter.");
        }
        return null;
    }

    public void signOut(){
        mainActivity.save("accessToken", "");
        mainActivity.save("accessSecret", "");
    }

    public  Trend[] getTrendsTitles(){
        try {
            return twitter.getPlaceTrends(23424982).getTrends();
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener las tendencias venezolanas de twitter.");
        }
        return null;
    }

    public QueryResult search(String text, String name){
        try {
            Query query = new Query(text);
            return twitter.search(query);
        } catch (TwitterException e){
            mainActivity.speak("No se pudieron obtener los tweets de la tendencia" + name);
        }
        return null;
    }
}
