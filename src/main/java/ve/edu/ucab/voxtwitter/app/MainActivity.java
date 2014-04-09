package ve.edu.ucab.voxtwitter.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech vox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vox = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            vox.setLanguage(Locale.getDefault());
            vox.speak("Texto de prueba para vox twitter", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            vox = null;
            Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
        }
    }
}
