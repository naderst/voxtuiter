/*
    Vox Twitter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtwitter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtwitter.app;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    private TextToSpeech vox;
    private static final int REQUEST_CODE = 1234;
    private AppMain appMain;
    private Intent intent;
    private ArrayList<String> matches;
    private Semaphore wait4speech;
    /**
     * Semáforo para bloquear el método speak hasta que el motor tts termine de hablar
     */
    private Semaphore wait4speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vox = new TextToSpeech(this, this);
    }

    /**
     * Escucha al usuario para luego procesar su voz y llevarla a texto.
     * La ejecución se hace de manera bloqueante y es procesada por el método onActivityResult
     */
    public ArrayList<String> listenSpeech() {
        startActivityForResult(intent, REQUEST_CODE);

        try {
            wait4speech.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return matches;
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

    /**
     * Abre una url en el navegador de Android
     * @param url URL a mostrar en el navegador
     */
    public void openURL(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
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
            wait4speak = new Semaphore(0);
            wait4speech = new Semaphore(0);

            final MainActivity mainActivity = this;

            Thread thread = new Thread() {
                @Override
                public void run() {
                    appMain = new AppMain(mainActivity);
                    appMain.onInit();
                }
            };

            thread.start();

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
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            wait4speech.release();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        wait4speak.release(); // Envía la señal de que el motor tts terminó de hablar
    }
}
