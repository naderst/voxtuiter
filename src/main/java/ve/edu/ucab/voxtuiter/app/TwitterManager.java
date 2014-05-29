/*
    Vox Tuiter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtuiter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtuiter.app;

import twitter4j.DirectMessage;
import twitter4j.QueryResult;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Trend;
import twitter4j.Query;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Abstracción para manejar la API de Twitter
 */
public class TwitterManager {
    private TwitterFactory tf;
    private Twitter twitter;
    private MainActivity mainActivity;
    private AccessToken accessToken = null;

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
        if (!mainActivity.read("accessToken").isEmpty() && !mainActivity.read("accessSecret").isEmpty()) {
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

            mainActivity.speak("Será enviado a la página de Twitter, inicie sesión y diga el número de PIN.");
            mainActivity.openURL(requestToken.getAuthorizationURL());

            try {
                String pin = mainActivity.listenSpeech(15000).get(0).replace(" ", "");
                System.out.println(pin);
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            } catch (TwitterException te) {
                if (te.getStatusCode() == 401) {
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
            if (text.length() <= 140) {
                twitter.updateStatus(text);
                mainActivity.speak("Tweet publicado con éxito.");
            } else
                mainActivity.speak("Error, el tweet debe contener máximo 140 caracteres");
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

    /**
     * Elimina un tweet de favoritos
     *
     * @param tweetId ID del tweet
     */
    public void removeFav(long tweetId) {
        try {
            twitter.destroyFavorite(tweetId);
            mainActivity.speak("Se removió el tweet de sus favoritos.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo remover el tweet de sus favoritos.");
        }
    }

    /**
     * Envía una respuesta a un tweet
     *
     * @param tweetId     ID del tweet
     * @param reply       mensaje de respuesta
     * @param screen_name nombre de usuario dal que se le hace una respuesta
     */
    public void reply(long tweetId, String reply, String screen_name) {
        try {
            StatusUpdate update = new StatusUpdate("@" + screen_name + " " + reply);
            update.setInReplyToStatusId(tweetId);
            twitter.updateStatus(update);
            mainActivity.speak("Se ha respondido el tweet selecccionado con éxito.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo responder el tweet seleccionado.");
        }
    }

    /**
     * Indica más información acerca de un tweet
     *
     * @param tweetId ID del tweet
     */
    public void moreInformation(long tweetId) {
        try {
            Status tweet = twitter.showStatus(tweetId);
            mainActivity.speak("Este tuit fue creado en el: " + tweet.getCreatedAt().toString());
            if (tweet.getGeoLocation() != null && !tweet.getGeoLocation().toString().matches(".*\\d+.*"))
                mainActivity.speak("Fue publicado desde: " + tweet.getGeoLocation().toString());
            if (tweet.getPlace() != null && !tweet.getPlace().getFullName().isEmpty())
                mainActivity.speak("Se adjunto la ubicación: " + tweet.getPlace().getFullName() + " en este tweet");
            if (tweet.getRetweetCount() > 0)
                mainActivity.speak("Ha sido retuiteado " + tweet.getRetweetCount() + " veces");
            else
                mainActivity.speak("Todavía no ha sido retuiteado");
            if (tweet.isRetweetedByMe())
                mainActivity.speak("Y usted lo ha retuiteado");
            if (tweet.getFavoriteCount() > 0)
                mainActivity.speak("Ha sido marcado como favorito " + tweet.getFavoriteCount() + " veces");
            else
                mainActivity.speak("Todavía no ha sido marcado como favorito");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo obtener más información del tweet indicado.");
        }
    }

    /**
     * Muestra la información del perfil de un usuario
     *
     * @param userId ID del usuario
     */
    public void profile(long userId) {
        try {
            User user = twitter.showUser(userId);
            AccessToken accessToken = twitter.getOAuthAccessToken();
            long myUserId = accessToken.getUserId();
            Relationship relationship = twitter.showFriendship(myUserId, userId);
            String description = user.getDescription();
            String location = user.getLocation();
            mainActivity.speak("Usted está visitando el perfil de: " + user.getName());
            if (!description.isEmpty())
                mainActivity.speak("La descripción del usuario es: " + description);
            if (!location.isEmpty() && !location.matches(".*\\d+.*"))
                mainActivity.speak("Se ubica en: " + location);
            if (user.isVerified())
                mainActivity.speak("Este es un usuario verificado");
            if (relationship.isSourceFollowingTarget() && twitter.showFriendship(myUserId, userId).isSourceFollowedByTarget())
                mainActivity.speak("Usted sigue a este usuario y él también lo sigue");
            else if (relationship.isSourceFollowingTarget())
                mainActivity.speak("Usted sigue a este usuario pero el no lo sigue");
            else if (relationship.isSourceFollowedByTarget())
                mainActivity.speak("Este usuario lo sigue pero usted no");
            else if (relationship.isSourceBlockingTarget())
                mainActivity.speak("Usted ha bloqueado a este usuario");
            mainActivity.speak("El usuario ha publicado: " + user.getStatusesCount() + " tweets");
            mainActivity.speak("Ha marcado: " + user.getFavouritesCount() + " tweets como favorito");
            mainActivity.speak("Tiene: " + user.getFollowersCount() + " seguidores");
            mainActivity.speak("Y sigue a: " + user.getFriendsCount() + " usuarios");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo obtener el perfil del usuario indicado.");
        }
    }

    /**
     * Indica la información de perfil del usuario con sesión iniciada en la aplicación
     */
    public void myProfile() {
        try {
            AccessToken accessToken = twitter.getOAuthAccessToken();
            User user = twitter.showUser(accessToken.getUserId());
            String description = user.getDescription();
            String location = user.getLocation();
            mainActivity.speak("El nombre de su perfil es: " + user.getName());
            if (!description.isEmpty())
                mainActivity.speak("Su descripción es: " + description);
            if (!location.isEmpty() && !location.matches(".*\\d+.*"))
                mainActivity.speak("Se ubica en: " + location);
            if (user.isVerified())
                mainActivity.speak("Usted es un usuario verificado");
            mainActivity.speak("Usted ha publicado: " + user.getStatusesCount() + " tweets");
            mainActivity.speak("Ha marcado: " + user.getFavouritesCount() + " tweets como favorito");
            mainActivity.speak("Tiene: " + user.getFollowersCount() + " seguidores");
            mainActivity.speak("Y sigue a: " + user.getFriendsCount() + " usuarios");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo obtener su perfil.");
        }
    }

    /**
     * Genera una solicitud de seguimiento a un usuario
     *
     * @param userId ID del usuario
     */
    public void follow(long userId) {
        try {
            if (!twitter.showFriendship(twitter.getOAuthAccessToken().getUserId(), userId).isSourceFollowingTarget()) {
                twitter.createFriendship(userId, true);
                mainActivity.speak("Ahora sigue a este usuario.");
            } else
                mainActivity.speak("Usted ya sigue a este usuario desde antes.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo seguir al usuario indicado.");
        }
    }

    /**
     * Genera una solicitud para dejar de seguir a un usuario
     *
     * @param userId ID del usuario
     */
    public void unfollow(long userId) {
        try {
            if (twitter.showFriendship(twitter.getOAuthAccessToken().getUserId(), userId).isSourceFollowingTarget()) {
                twitter.destroyFriendship(userId);
                mainActivity.speak("Ya no sigue a este usuario.");
            } else
                mainActivity.speak("Usted no sigue a este usuario desde antes.");
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo deshacer el seguimiento al usuario indicado.");
        }
    }

    /**
     * Obtiene el timeline con los N tweets más recientes de un usuario
     *
     * @param userId ID del usuario
     * @return lista con los N tweets más recientes de un usuario o null en caso de error
     */
    public ResponseList<Status> userTimeLine(long userId) {
        try {
            return twitter.getUserTimeline(userId);
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener los tweets del usuario indicado.");
        }
        return null;
    }

    /**
     * Obtiene el timeline con los N tweets más recientes del usuario cuya sessión
     * esta iniciada en la aplicación
     *
     * @return lista con los N tweets más recientes de un usuario o null en caso de error
     */
    public ResponseList<Status> myTimeLine() {
        try {
            return twitter.getUserTimeline(twitter.getOAuthAccessToken().getUserId());
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener sus tweets.");
        }
        return null;
    }

    /**
     * Obtiene las N menciones más recientes del usuario cuya sessión
     * esta iniciada en la aplicación
     *
     * @return lista con las N menciones más recientes  o null en caso de error
     */
    public ResponseList<Status> getMentions() {
        try {
            return twitter.getMentionsTimeline();
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener sus notificaciones.");
        }
        return null;
    }

    /**
     * Obtiene los mensajes directos recibidos, del usuario cuya sessión
     * esta iniciada en la aplicación
     *
     * @return lista con los N mensajes directos más recientes recibidos o null en caso de error
     */
    public ResponseList<DirectMessage> getDirectMessages() {
        ResponseList<DirectMessage> directMessages;
        try {
            if ((directMessages = twitter.getDirectMessages()).size() == 0) {
                mainActivity.speak("No tiene mensajes directos recibidos.");
                return null;
            } else
                return directMessages;
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener sus mensajes directos recibidos.");
        }
        return null;
    }

    /**
     * Obtiene los mensajes directos enviados, del usuario cuya sessión
     * esta iniciada en la aplicación
     *
     * @return lista con los N mensajes directos más reciente enviados o null en caso de error
     */
    public ResponseList<DirectMessage> getSentDirectMessages() {
        ResponseList<DirectMessage> sentDirectMessages;
        try {
            if ((sentDirectMessages = twitter.getSentDirectMessages()).size() == 0) {
                mainActivity.speak("No tiene mensajes directos enviados.");
                return null;
            } else
                return sentDirectMessages;
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener sus mensajes directos enviados.");
        }
        return null;
    }

    /**
     * Envía a un usuario un mensaje directo,
     * utilizando el ID de la cuenta de twitter del usuario
     *
     * @param userRecipientId ID del usuario
     * @param reply           contenido del mensaje directo
     */
    public void sendDirectMessage(long userRecipientId, String reply) {
        String userName = new String(" a: ");
        try {
            userName = userName + twitter.showUser(userRecipientId).getName();
            twitter.sendDirectMessage(userRecipientId, reply);
            mainActivity.speak("Su mensaje directo a sido enviado con éxito" + userName);
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo enviar su mensaje directo" + ((!userName.equals(" a: ")) ? userName : "."));
        }
    }

    /**
     * Envía a un usuario un mensaje directo,
     * utilizando el nombre de la cuenta de twitter del usuario
     *
     * @param userName ID del usuario
     * @param reply    contenido del mensaje directo
     */
    public void sendDirectMessage(String userName, String reply) {
        try {
            twitter.sendDirectMessage(userName, reply);
            mainActivity.speak("Su mensaje directo a sido enviado con éxito a: @" + userName);
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo enviar su mensaje directo a: @" + userName);
        }
    }

    /**
     * Obtiene el TimeLine principal de la cuenta de Twitter del usuario
     * cuya sesión está iniciada en la aplicación
     *
     * @return Lista con los N tweets más recientes del timeline o null en caso de error
     */
    public ResponseList<Status> getTimeLine() {
        try {
            return twitter.getHomeTimeline();
        } catch (TwitterException e) {
            mainActivity.speak("No se pudo obtener su time line.");
        }
        return null;
    }

    /**
     * Cierra la sesión del usuario en la aplicación
     */
    public void signOut() {
        mainActivity.save("accessToken", "");
        mainActivity.save("accessSecret", "");
    }

    /**
     * Obtiene los trending topics en Venezuela
     *
     * @return Lista con los primeros 10 trending topics o null en caso de error
     */
    public Trend[] getTrendsTitles() {
        try {
            return twitter.getPlaceTrends(23424982).getTrends();
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener de twitter las 10 tendencias de Venezuela.");
        }
        return null;
    }

    /**
     * Realiza una búsqueda en twitter
     *
     * @param text texto que contiene la búsqueda a realizar en twitter
     * @return Lista con los N primeros resultados encontrados o null en caso de error
     */
    public ResponseList<User> search(String text) {
        try {
            return twitter.searchUsers(text, 1);
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener los resultados de su búsqueda");
        }
        return null;
    }

    /**
     * Obtiene los tweets de una tendencia indicada
     *
     * @param text texto que contiene la búsqueda a realizar en twitter de una tendencia indicada
     * @param name nombre de la tendencia a buscar en twitter
     * @return Lista con los N primeros tweets de la tendencia o null en caso de error
     */
    public QueryResult trendTweets(String text, String name) {
        try {
            Query query = new Query(text);
            return twitter.search(query);
        } catch (TwitterException e) {
            mainActivity.speak("No se pudieron obtener los tweets de la tendencia " + name);
        }
        return null;
    }
}