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
            String comand = matches.get(0);

            if(comand.equals("leer")) {
                ResponseList<Status> timeline = twitter.getTimeLine();
                int i = 0;
                boolean flag = true;

                while(!comand.equals("salir")){
                    Status e = timeline.get(i);
                    if(flag)
                        mainActivity.speak(e.getUser().getName() + " dijo, " + e.getText());
                    comand = mainActivity.listenSpeech().get(0);

                    if(comand.equals("siguiente")){
                        if(timeline.size() > (i + 1)){
                            i++;
                            flag = true;
                        }else{
                            mainActivity.speak("Usted está en el último tweet cargado");
                            flag = false;
                        }
                        continue;
                    }
                    if(comand.equals("anterior")){
                        if(i > 0){
                            i--;
                            flag = true;
                        }else{
                            mainActivity.speak("Usted está en el tweet cargado más reciente");
                            flag = false;
                        }
                        continue;
                    }
                    if(comand.equals("retweet")) {
                        twitter.retweet(e.getId());
                        flag = false;
                        continue;
                    }
                    if(comand.equals("favorito")) {
                        twitter.fav(e.getId());
                        flag = false;
                        continue;
                    }
                    mainActivity.speak("Comando inválido");
                }

                continue;
            }

            if(comand.equals("twittear")) {
                String text;
                mainActivity.speak("Diga su tweet");
                if((text = mainActivity.listenSpeech().get(0)) != "cancelar")
                    twitter.tweet(text);
                continue;
            }

            if(comand.equals("salir")) {
                System.exit(0);
            }

            if(comand.equals("cerrar sesión")) {
                twitter.signOut();
                System.exit(0);
            }

            mainActivity.speak("Comando inválido, vuelva a intentarlo");

            System.out.println(matches.toString());
        }
    }
}
