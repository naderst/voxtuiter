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
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    private TextToSpeech vox;
    private ResponseList<Status> list;
    private static final int REQUEST_CODE = 1234;
    private EventsManager eventsManager;
    private Intent intent;
    private Semaphore wait4speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vox = new TextToSpeech(this, this);
    }

    /**
     * Escucha al usuario para luego procesar su voz y llevarla a texto.
     * La ejecución se hace de manera asíncrona y es procesada por el método onActivityResult
     */
    public void listenSpeech() {
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * Procesa un texto y lo lleva a voz. El hilo que lo ejecute se bloqueará hasta que
     * el texto se reproduzca
     *
     * @param text Texto para ser llevado a voz
     */
    public void speak(String text) {
        HashMap<String, String> myHashAlarm = new HashMap();

        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FIN");

        vox.speak(text, TextToSpeech.QUEUE_ADD, myHashAlarm);

        try {
            wait4speak.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
        Evento que se dispara cuando el motor de la API TextToSpeech está listo
     */
    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            vox.setLanguage(Locale.getDefault());
            vox.setOnUtteranceCompletedListener(this);
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 0);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 0);
            wait4speak = new Semaphore(0); // Semáforo para bloquear el método speak hasta que el motor tts termine de hablar
            eventsManager = new EventsManager(this);
            eventsManager.onInit();
        } else {
            vox = null;
            Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
        Evento que procesará cuando el usuario hable a través del micrófono una vez puesto a la escucha.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // La variable matches contiene las distintas frases que se detectaron a traves del micrófono
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            eventsManager.onSpeak(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        wait4speak.release(); // Envía la señal de que el motor tts terminó de hablar
    }
}
