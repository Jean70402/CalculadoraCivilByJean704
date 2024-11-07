package com.jean704.calculadoracivilbyjean704;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        // Configuración de botones
        setupButton(R.id.button_screen1, CalculoCaladoSeccionCircular.class);
        // setupButton(R.id.button_screen2, Screen2Activity.class);
        //setupButton(R.id.button_screen3, Screen3Activity.class);
        //setupButton(R.id.button_screen4, Screen4Activity.class);
        //setupButton(R.id.button_screen5, Screen5Activity.class);
        //setupButton(R.id.button_screen6, Screen6Activity.class);
        //setupButton(R.id.button_screen7, Screen7Activity.class);
        //setupButton(R.id.button_screen8, Screen8Activity.class);
    }

    // Método para configurar el cambio de actividad
    private void setupButton(int buttonId, final Class<?> targetActivity) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, targetActivity);
                startActivity(intent);
            }
        });
    }
}
