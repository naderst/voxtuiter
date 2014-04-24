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

enum Sitios {
    MENU, TIMELINE, TRENDSTITLES, TRENDS, PROFILE, PROFILE_TWEETS
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
        boolean flagTimeline = false;
        boolean flagTrends = false;
        boolean flagTrendsTitles = false;
        String command = "";
        int i = 0, j = 0;
        long userId = 0;

        while(true) {
            if(!flagTimeline && !flagTrendsTitles && !flagTrends){
                mainActivity.speak("Diga un comando");
                matches = mainActivity.listenSpeech();
                command = matches.get(0);
            }else
                if(flagTimeline)
                    if(ubicacion == Sitios.TIMELINE)
                        command = "leer";
                    else
                        command = "historial de mensajes";
                else if(flagTrendsTitles)
                    command = "tendencias";
                else
                    command = "entrar";

            if(command.equals("leer")) {
                if(ubicacion != Sitios.TIMELINE){
                    ubicacion = Sitios.TIMELINE;
                    i = 0;
                    if((timeline = twitter.getTimeLine()) == null)
                        continue;
                }

                mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                flagTimeline = false;
                continue;
            }

            if(command.equals("tendencias")) {
                if(ubicacion != Sitios.TRENDSTITLES && ubicacion != Sitios.TRENDS)
                    i = 0;
                if(ubicacion != Sitios.TRENDSTITLES){
                    ubicacion = Sitios.TRENDSTITLES;
                    if((trendsTitles = twitter.getTrendsTitles()) == null)
                        continue;
                }

                mainActivity.speak("La tendencia número" + (i+1) + "es la siguiente:" + trendsTitles[i].getName());
                flagTrendsTitles = false;
                continue;
            }

            if(command.equals("siguiente")){
                switch (ubicacion){
                    case TRENDSTITLES:
                        if(trendsTitles.length > (i + 1)){
                            i++;
                            flagTrendsTitles = true;
                        }else
                            mainActivity.speak("Usted está en la última tendencia cargada.");
                        break;
                    case TRENDS:
                        if(trends.size() > (j + 1)){
                            j++;
                            flagTrends = true;
                        }else
                            mainActivity.speak("Usted está en el último tweet cargado de la tendencia actual.");
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
                            if(timeline.size() > (i + 1)){
                                i++;
                                flagTimeline = true;
                            }else
                                mainActivity.speak("Usted está en el último tweet cargado.");
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
                            flagTrendsTitles = true;
                        }else
                            mainActivity.speak("Usted está en la tendencia cargada más reciente.");
                        break;
                    case TRENDS:
                        if(j > 0){
                            j--;
                            flagTrends = true;
                        }else
                            mainActivity.speak("Usted está en el tweet cargado más reciente de la tendencia actual.");
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
                            if(i > 0){
                                i--;
                                flagTimeline = true;
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
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
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
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
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
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
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
                if(!(reply = mainActivity.listenSpeech().get(0)).equals("cancelar"))
                    switch (ubicacion){
                        case TRENDS:
                            twitter.reply(trends.get(j).getId(), reply, trends.get(j).getUser().getScreenName());
                            break;
                        default:
                            if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
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
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
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
                    if((result = twitter.search(trendsTitles[i].getQuery(), trendsTitles[i].getName())) != null){
                        trends = result.getTweets();
                        j = 0;
                    }else
                        continue;
                }
                if(ubicacion == Sitios.TRENDS){
                    mainActivity.speak(trends.get(j).getUser().getName() + " dijo, " + trends.get(j).getText());
                    flagTrends = false;
                }else
                    mainActivity.speak("Comando no disponible.");
                continue;
            }

            if(command.equals("twittear")) {
                String text;
                mainActivity.speak("Diga su tweet");
                if(!(text = mainActivity.listenSpeech().get(0)).equals("cancelar"))
                    twitter.tweet(text);
                else
                    mainActivity.speak("El tweet se ha cancelado.");
                continue;
            }

            if(command.equals("perfil")) {
                switch (ubicacion){
                    case TRENDS:
                        twitter.profile(userId = trends.get(j).getUser().getId());
                        break;
                    case PROFILE:
                        twitter.profile(userId);
                        break;
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
                            twitter.profile(userId = timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                ubicacion = Sitios.PROFILE;
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
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
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
                    default:
                        if(ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS)
                            twitter.unfollow(timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("historial de mensajes")){
                switch (ubicacion){
                    case TIMELINE:
                        if((timeline = twitter.userTimeLine(timeline.get(i).getUser().getId())) != null){
                            i = 0;
                            ubicacion = Sitios.PROFILE_TWEETS;
                        }else
                            continue;
                        break;
                    case TRENDS:
                        if((timeline = twitter.userTimeLine(trends.get(j).getUser().getId())) != null){
                            i = 0;
                            ubicacion = Sitios.PROFILE_TWEETS;
                        }else
                            continue;
                        break;
                    case PROFILE:
                        if((timeline = twitter.userTimeLine(userId)) != null){
                            i = 0;
                            ubicacion = Sitios.PROFILE_TWEETS;
                        }else
                            continue;
                        break;
                    default:
                        if(ubicacion != Sitios.PROFILE_TWEETS){
                            mainActivity.speak("Comando no disponible.");
                            continue;
                        }
                        break;
                }
                mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                flagTimeline = false;
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