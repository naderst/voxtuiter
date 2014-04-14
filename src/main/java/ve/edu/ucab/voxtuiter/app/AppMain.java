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

import twitter4j.ResponseList;
import twitter4j.Status;

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

    AppMain(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        twitter = new TwitterManager(mainActivity);
    }

    /**
     * Evento que se ejecuta cuando la aplicación está lista para empezar
     */
    public void onInit() {

        while(true) {
            mainActivity.speak("Diga un comando");
            ArrayList<String> matches = mainActivity.listenSpeech();
            String comando = matches.get(0);

            if(comando.equals("leer")) {
                ResponseList<Status> timeline = twitter.getTimeLine();

                for(Status e : timeline) {
                    mainActivity.speak(e.getUser().getName() + " dijo, " + e.getText());
                    comando = mainActivity.listenSpeech().get(0);
                    if(comando.equals("siguiente"))
                        continue;
                    if(comando.equals("retweet")) {
                        twitter.retweet(e.getId());
                        continue;
                    }
                    if(comando.equals("favorito")) {
                        twitter.fav(e.getId());
                        continue;
                    }
                    if(comando.equals("salir"))
                        break;
                    mainActivity.speak("Comando inválido");
                }

                continue;
            }

            if(comando.equals("twittear")) {
                mainActivity.speak("Diga su tweet");
                twitter.tweet(mainActivity.listenSpeech().get(0));
                mainActivity.speak("Tweet publicado con éxito");
                continue;
            }

            if(comando.equals("salir")) {
                System.exit(0);
            }

            mainActivity.speak("Comando inválido, vuelva a intentarlo");

            System.out.println(matches.toString());
        }
    }
}
