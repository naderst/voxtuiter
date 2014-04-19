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

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.QueryResult;
import twitter4j.TwitterException;

enum Sitios {
    MENU, TIMELINE, TRENDSTITLES, TRENDS, PROFILE
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

        while(true) {
            if(!flagTimeline && !flagTrendsTitles && !flagTrends){
                mainActivity.speak("Diga un comando");
                matches = mainActivity.listenSpeech();
                command = matches.get(0);
            }else
                if(flagTimeline)
                    command = "leer";
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
                if(ubicacion != Sitios.TRENDSTITLES){
                    ubicacion = Sitios.TRENDSTITLES;
                    i = 0;
                    if((trendsTitles = twitter.getTrendsTitles()) == null)
                        continue;
                }

                mainActivity.speak("La tendencia número" + (i+1) + "es la siguiente:" + trendsTitles[i].getName());
                flagTrendsTitles = false;
                continue;
            }

            if(command.equals("siguiente")){
                switch (ubicacion){
                    case TIMELINE:
                        if(timeline.size() > (i + 1)){
                            i++;
                            flagTimeline = true;
                        }else
                            mainActivity.speak("Usted está en el último tweet cargado.");
                        break;
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
                        mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("anterior")){
                switch (ubicacion){
                    case TIMELINE:
                        if(i > 0){
                            i--;
                            flagTimeline = true;
                        }else
                            mainActivity.speak("Usted está en el tweet cargado más reciente.");
                        break;
                    case TRENDSTITLES:
                        if(i > 0){
                            i--;
                            flagTrendsTitles = true;
                        }else
                            mainActivity.speak("Usted está en la tendencia cargada más reciente.");
                        break;
                    case TRENDS:
                        if(trends.size() > (j + 1)){
                            j++;
                            flagTrends = true;
                        }else
                            mainActivity.speak("Usted está en el tweet cargado más reciente de la tendencia actual.");
                        break;
                    default:
                        mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }
            if(command.equals("retweet")) {
                switch (ubicacion){
                    case TIMELINE:
                        twitter.retweet(timeline.get(i).getId());
                        break;
                    case TRENDS:
                        twitter.retweet(trends.get(j).getId());
                        break;
                    default:
                        mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }
            if(command.equals("favorito")) {
                switch (ubicacion){
                    case TIMELINE:
                        twitter.fav(timeline.get(i).getId());
                        break;
                    case TRENDS:
                        twitter.fav(trends.get(j).getId());
                        break;
                    default:
                        mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("responder")) {
                mainActivity.speak("Indique su respuesta");
                String reply;
                if(!(reply = mainActivity.listenSpeech().get(0)).equals("cancelar")){
                    switch (ubicacion){
                        case TIMELINE:
                            twitter.reply(timeline.get(i).getId(), reply, timeline.get(i).getUser().getScreenName());
                            break;
                        case TRENDS:
                            twitter.reply(trends.get(j).getId(), reply, trends.get(j).getUser().getScreenName());
                            break;
                        default:
                            mainActivity.speak("Comando no disponible.");
                            break;
                    }
                }else
                    mainActivity.speak("La respuesta se ha cancelado.");
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

            if(command.equals("ver perfil")) {
                ubicacion = Sitios.PROFILE;
                switch (ubicacion){
                    case TIMELINE:
                        twitter.profile(timeline.get(i).getUser().getId());
                        break;
                    case TRENDS:
                        twitter.profile(trends.get(j).getUser().getId());
                        break;
                    default:
                        mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            if(command.equals("salir")) {
                mainActivity.speak("Gracias por su visita, vuelva pronto");
                System.exit(0);
            }

            if(command.equals("cerrar sesión")) {
                twitter.signOut();
                mainActivity.speak("Sesión finalizada con éxito. Cerrando aplicación");
                System.exit(0);
            }

            mainActivity.speak("Comando inválido, vuelva a intentarlo.");

            System.out.println(matches.toString());
        }
    }
}