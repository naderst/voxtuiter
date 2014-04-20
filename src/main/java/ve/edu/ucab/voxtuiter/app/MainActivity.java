/*
    Vox Tuiter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtuiter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtuiter.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
    private static final int REQUEST_CODE = 1234;
    /**
     * Objeto para llevar un texto a voz
     */
    private TextToSpeech vox;
    /**
     * Clase que se encarga de manejar el flujo de la App
     */
    private AppMain appMain;
    /**
     * Intent asociado a la actividad de reconocimiento de la voz
     */
    private Intent intent;
    /**
     * Lista con todas las frases escuchadas con el reconocedor de voz de android
     */
    private ArrayList<String> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vox = new TextToSpeech(this, this);
    }

    /**
     * Escucha al usuario para luego procesar su voz y llevarla a texto.
     * La ejecución se hace de manera bloqueante y es procesada por el método onActivityResult
     *
     * @return Lista de frases escuchadas
     */
    public ArrayList<String> listenSpeech() {
        while(true) {
            startActivityForResult(intent, REQUEST_CODE);

            synchronized (matches) {
                matches.clear();

                try {
                    matches.wait(15000); // El hilo se bloquea hasta que el usuario termine de hablar
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(matches.isEmpty()) {
                    /* Ha pasado mucho tiempo y el usuario no ha dicho nada? */
                    speak("Lo siento, no escuché lo que dijo, vuelva a intentarlo");
                } else {
                    return matches;
                }
            }
        }
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

        text = text.replace("#", "hash tag");
        text = text.replaceAll("http:\\/\\/t\\.co\\/(\\w)*", "U R L");
        vox.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);

        synchronized (this) {
            try {
                wait(); // El hilo se bloquea hasta que la App termine de narrar el texto
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            matches = new ArrayList<String>();

            final MainActivity mainActivity = this;

            new Thread() {
                @Override
                public void run() {
                    appMain = new AppMain(mainActivity);
                    appMain.onInit();
                }
            }.start();

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
            synchronized (matches) {
                // La variable matches contiene las distintas frases que se detectaron a través del micrófono
                matches.addAll(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
                matches.notify();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        synchronized (this) {
            notify(); // Envía la señal de que el motor tts terminó de hablar
        }
    }

    /**
     * Almacena un par clave/valor en un archivo
     *
     * @param key Clave del valor
     * @param val Valor que se desea almacenar
     */
    public void save(String key, String val) {
        SharedPreferences settings = getSharedPreferences("tokens", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, val);
        editor.commit();
    }

    /**
     * Obtiene un valor dado una clave
     *
     * @param key Clave del valor a obtener
     * @return Valor de clave, si no existe la clave retorna vacío
     */
    public String read(String key) {
        SharedPreferences settings = getSharedPreferences("tokens", 0);
        return settings.getString(key, "");
    }

}
