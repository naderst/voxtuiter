/*
    Vox Tuiter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtuiter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtuiter.app;

import java.util.ArrayList;
import java.util.List;

import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.User;

enum Sitios {
    MENU, TIMELINE, TRENDSTITLES, TRENDS, MY_PROFILE, PROFILE, PROFILE_TWEETS, MENTIONS, SEARCH
}

/**
 * La clase AppMain es una abstracción para manejar el flujo de la App
 */
public class AppMain {
    /**
     * Objeto que se encarga de realizar el trabajo "sucio" de la App, brindando una interfaz
     * para los métodos básicos de la App.
     */
    private MainActivity mainActivity;
    /**
     * Objeto que permite utilizar las primitivas de Twitter de una manera sencilla.
     * No es más que un wrapper de twitter4j.
     */
    private TwitterManager twitter;

    private Sitios ubicacion;

    AppMain(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        twitter = new TwitterManager(mainActivity);
    }

    /**
     * Evento que se ejecuta cuando la aplicación está lista para empezar
     */
    public void onInit(){
        ubicacion = Sitios.MENU;
        ResponseList<Status> timeline = null;
        List<Status> trends = null;
        Trend[] trendsTitles = null;
        ArrayList<String> matches = null;
        ResponseList<User> users = null;
        boolean flagRepeat = false;
        boolean flagEnd = false;
        String command = "";
        int i = 0, j = 0;
        long userId = 0;

        while(true) {
            if(!flagRepeat){
                if((ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.TRENDSTITLES || ubicacion == Sitios.TRENDS) && !flagEnd) {
                    if((matches = mainActivity.listenSpeech(5000)) != null)
                        command = matches.get(0);
                    else
                        command = "siguiente";
                }else {
                    mainActivity.speak("Diga un comando");
                    matches = mainActivity.listenSpeech(15000);
                    command = matches.get(0);
                }
            }else
                if(flagRepeat)
                    command = "repetir";

            if(command.equals("repetir")){
                switch (ubicacion) {
                    case TRENDSTITLES:
                        mainActivity.speak("La tendencia número" + (i + 1) + "es la siguiente:" + trendsTitles[i].getName());
                        break;
                    case TRENDS:
                        mainActivity.speak(trends.get(j).getUser().getName() + " dijo, " + trends.get(j).getText());
                        break;
                    case PROFILE:
                        twitter.profile(userId);
                        break;
                    case SEARCH:
                        mainActivity.speak("Nombre: " + users.get(i).getName() + ". @" + users.get(i).getScreenName());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                        else{
                            mainActivity.speak("Comando no disponible.");
                            continue;
                        }
                        break;
                }
                if(ubicacion != Sitios.PROFILE)
                    flagRepeat = false;
                continue;
            }

            if(command.equals("leer")) {
                ubicacion = Sitios.TIMELINE;
                i = 0;
                if((timeline = twitter.getTimeLine()) != null) {
                    flagEnd = false;
                    mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                }
                continue;
            }

            if(command.equals("tendencias")) {
                if(ubicacion != Sitios.TRENDS)
                    i = 0;

                ubicacion = Sitios.TRENDSTITLES;
                if((trendsTitles = twitter.getTrendsTitles()) != null) {
                    flagEnd = false;
                    mainActivity.speak("La tendencia número" + (i + 1) + "es la siguiente:" + trendsTitles[i].getName());
                }
                continue;
            }

            if(command.equals("menciones")) {
                ubicacion = Sitios.MENTIONS;
                if((timeline = twitter.getMentions()) != null) {
                    flagEnd = false;
                    mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                }
                continue;
            }

            if(command.equals("mensajes")){
                continue;
            }

            if(command.equals("siguiente")){
                switch (ubicacion) {
                    case TRENDSTITLES:
                        if (trendsTitles.length > (i + 1)) {
                            i++;
                            flagRepeat = true;
                        } else {
                            mainActivity.speak("Usted está en la última tendencia cargada.");
                            flagEnd = true;
                        }
                        break;
                    case TRENDS:
                        if (trends.size() > (j + 1)) {
                            j++;
                            flagRepeat = true;
                        } else{
                            mainActivity.speak("Usted está en el último tweet cargado de la tendencia actual.");
                            flagEnd = true;
                        }
                        break;
                    case SEARCH:
                        if (users.size() > (i + 1)) {
                            i++;
                            flagRepeat = true;
                        } else {
                            mainActivity.speak("Usted está en el último usuario encontrado en su búsqueda.");
                            flagEnd = true;
                        }
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            if(timeline.size() > (i + 1)){
                                i++;
                                flagRepeat = true;
                            }else {
                                mainActivity.speak("Usted está en el último tweet cargado.");
                                flagEnd = true;
                            }
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("anterior")){
                switch (ubicacion){
                    case TRENDSTITLES:
                        if(i > 0){
                            i--;
                            flagRepeat = true;
                        }else
                            mainActivity.speak("Usted está en la tendencia cargada más reciente.");
                        break;
                    case TRENDS:
                        if(j > 0){
                            j--;
                            flagRepeat = true;
                        }else
                            mainActivity.speak("Usted está en el tweet cargado más reciente de la tendencia actual.");
                        break;
                    case SEARCH:
                        if(i > 0){
                            i--;
                            flagRepeat = true;
                        }else
                            mainActivity.speak("Usted está en el primer usuario encontrado en su búsqueda.");
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            if(i > 0){
                                i--;
                                flagRepeat = true;
                            }else
                                mainActivity.speak("Usted está en el tweet cargado más reciente.");
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }
            if(command.equals("retweet")) {
                switch (ubicacion){
                    case TRENDS:
                        twitter.retweet(trends.get(j).getId());
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.retweet(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }
            if(command.equals("favorito")) {
                switch (ubicacion){
                    case TRENDS:
                        twitter.fav(trends.get(j).getId());
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.fav(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }
            if(command.equals("quitar de favoritos")) {
                switch (ubicacion){
                    case TRENDS:
                        twitter.removeFav(trends.get(j).getId());
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.removeFav(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("responder")) {
                mainActivity.speak("Indique su respuesta");
                String reply;
                if(!(reply = mainActivity.listenSpeech(15000).get(0)).equals("cancelar"))
                    switch (ubicacion){
                        case TRENDS:
                            twitter.reply(trends.get(j).getId(), reply, trends.get(j).getUser().getScreenName());
                            break;
                        default:
                            if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                                twitter.reply(timeline.get(i).getId(), reply, timeline.get(i).getUser().getScreenName());
                            else
                                mainActivity.speak("Comando no disponible.");
                            break;
                    }
                else
                    mainActivity.speak("La respuesta se ha cancelado.");
                continue;
            }

            if(command.equals("más información")){
                switch (ubicacion){
                    case TRENDS:
                        twitter.moreInformation(trends.get(j).getId());
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.moreInformation(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("entrar")){
                if(ubicacion == Sitios.TRENDSTITLES){
                    ubicacion = Sitios.TRENDS;
                    QueryResult result;
                    if((result = twitter.search(trendsTitles[i].getQuery(), trendsTitles[i].getName())) != null) {
                        trends = result.getTweets();
                        j = 0;
                        flagEnd = false;
                        mainActivity.speak(trends.get(j).getUser().getName() + " dijo, " + trends.get(j).getText());
                    }
                }else
                    mainActivity.speak("Comando no disponible.");
                continue;
            }

            if(command.equals("twittear")) {
                String text;
                mainActivity.speak("Diga su tweet");
                if(!(text = mainActivity.listenSpeech(15000).get(0)).equals("cancelar"))
                    twitter.tweet(text);
                else
                    mainActivity.speak("El tweet se ha cancelado.");
                continue;
            }

            if(command.equals("buscar")) {
                ubicacion = Sitios.SEARCH;
                String text;
                i = 0;
                mainActivity.speak("Indique la búsqueda:");
                if(!(text = mainActivity.listenSpeech(15000).get(0)).equals("cancelar")) {
                    if((users = twitter.search(text)) != null) {
                        flagEnd = false;
                        mainActivity.speak("Nombre: " + users.get(i).getName() + ". @" + users.get(i).getScreenName());
                    }
                }else
                    mainActivity.speak("La búsqueda se ha cancelado.");
                continue;
            }

            if(command.equals("perfil")) {
                switch (ubicacion){
                    case TRENDS:
                        twitter.profile(userId = trends.get(j).getUser().getId());
                        break;
                    case SEARCH:
                        twitter.profile(userId = users.get(i).getId());
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.profile(userId = timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                ubicacion = Sitios.PROFILE;
                continue;
            }

            if(command.equals("mi perfil")) {
                ubicacion = Sitios.MY_PROFILE;
                twitter.myProfile();
                continue;
            }

            if(command.equals("seguir")){
                switch (ubicacion){
                    case TRENDS:
                        twitter.follow(trends.get(j).getUser().getId());
                        break;
                    case PROFILE:
                        twitter.follow(userId);
                        break;
                    case SEARCH:
                        twitter.follow(users.get(i).getId());
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.follow(timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("no seguir")){
                switch (ubicacion){
                    case TRENDS:
                        twitter.unfollow(trends.get(j).getUser().getId());
                        break;
                    case PROFILE:
                        twitter.unfollow(userId);
                        break;
                    case SEARCH:
                        twitter.unfollow(users.get(i).getId());
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.unfollow(timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("historial de mensajes")){
                switch (ubicacion){
                    case TRENDS:
                        if((timeline = twitter.userTimeLine(trends.get(j).getUser().getId())) == null)
                            continue;
                        break;
                    case PROFILE:
                        if((timeline = twitter.userTimeLine(userId)) == null)
                            continue;
                        break;
                    case MY_PROFILE:
                        if((timeline = twitter.myTimeLine()) == null)
                            continue;
                        break;
                    case SEARCH:
                        if((timeline = twitter.userTimeLine(users.get(i).getId())) == null)
                            continue;
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS) {
                            if ((timeline = twitter.userTimeLine(timeline.get(i).getUser().getId())) == null)
                                continue;
                        }else {
                            mainActivity.speak("Comando no disponible.");
                            continue;
                        }
                        break;
                }
                i = 0;
                flagEnd = false;
                ubicacion = Sitios.PROFILE_TWEETS;
                mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                continue;
            }

            if(command.equals("salir")) {
                mainActivity.speak("Gracias por su visita, vuelva pronto");
                System.exit(0);
            }

            if(command.equals("cerrar sesión")) {
                twitter.signOut();
                mainActivity.speak("Sesión finalizada con éxito. Cerrando aplicación, gracias por visitarnos vuelva pronto");
                System.exit(0);
            }

            mainActivity.speak("Comando inválido, vuelva a intentarlo.");

            System.out.println(matches.toString());
        }
    }
}