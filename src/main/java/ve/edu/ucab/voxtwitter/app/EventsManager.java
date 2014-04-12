/*
    Vox Twitter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtwitter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtwitter.app;

import java.util.ArrayList;

/**
 * La clase EventsManager es una abstracción para manejar todos los eventos relevantes de la app
 */
public class EventsManager {
    private MainActivity mainActivity;

    EventsManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Evento que se ejecuta cuando la aplicación está lista para empezar
     */
    public void onInit() {
        mainActivity.listenSpeech();
    }

    /**
     * Evento que se ejecuta cuando el usuario habla por el micrófono una vez que el mismo esté
     * a la escucha
     * @param matches Lista de frases detectadas en el microfono
     */
    public void onSpeak(ArrayList<String> matches) {
        System.out.println(matches.toString());

        mainActivity.speak(matches.get(0));

        if(!matches.get(0).equals("salir"))
            mainActivity.listenSpeech();
    }
}
