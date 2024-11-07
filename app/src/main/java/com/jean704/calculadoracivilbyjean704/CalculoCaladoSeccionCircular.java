package com.jean704.calculadoracivilbyjean704;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CalculoCaladoSeccionCircular extends AppCompatActivity {

    private EditText inputQd, inputDd, inputN, inputS;
    private TextView outputResult;

    double thetha,areaCalc,perimetroCalc,radioHidraulicoCalc,velocidad,qCalc,qd,dd,n,s,y,radio;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calado_tuberia_circular);

        // Inicializamos los campos de entrada y el campo de salida
        inputQd = findViewById(R.id.inputQd);
        inputDd = findViewById(R.id.inputDd);
        inputN = findViewById(R.id.inputN);
        inputS = findViewById(R.id.inputS);
        outputResult = findViewById(R.id.outputResult);
        Button calculateButton = findViewById(R.id.calculateButton);

        // Configuramos la acción del botón de cálculo
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateResult();
            }
        });

        Button backButton = findViewById(R.id.button_back_to_main);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalculoCaladoSeccionCircular.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }
    public void repetirCalculo(double yCalculo){
        y = yCalculo;
        thetha= 2*Math.acos(1-(y/radio));
        areaCalc=0.5*(radio*radio)*(thetha-Math.sin(thetha));
        perimetroCalc=radio*thetha;
        radioHidraulicoCalc=Math.pow((areaCalc/perimetroCalc),((double) 2 /3));
        velocidad= (1/n)*radioHidraulicoCalc*Math.pow(s,0.5);
        qCalc= velocidad*areaCalc*1000;
    }

    public void mostrarResultado(double result) {
        // Formatea el resultado a 5 decimales
        String resultText = String.format("%.5f", result);
        // Crea el texto completo con el valor formateado
        String fullText = "Resultado: " + resultText + " [l/s]";
        // Crea un SpannableString con el texto completo
        SpannableString spannable = new SpannableString(fullText);

        // Aplica el color solo al valor de "result"
        int color = getResources().getColor(R.color.teal_700); // Usa el color definido en colors.xml o uno de la clase Color
        spannable.setSpan(new ForegroundColorSpan(color),
                "Resultado: ".length(),
                "Resultado: ".length() + resultText.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Configura el texto en el TextView
        outputResult.setText(spannable);
    }
    public void calculateResult() {
        try {
            // Obtener y convertir los valores de entrada
            qd = Double.parseDouble(inputQd.getText().toString());
            dd = Double.parseDouble(inputDd.getText().toString());
            n = Double.parseDouble(inputN.getText().toString());
            s = Double.parseDouble(inputS.getText().toString());

            radio= (dd/2)/100;
            y=0.0001;
            double iterac=0;
            qCalc=0;
            while (qCalc<qd && iterac < Math.pow(10,10)) {
                y += 0.00001; // Incrementa y
                repetirCalculo(y); // Calcula qCalc
                iterac++;
            }
            //Log.d("calculo", "Resultado final: qCalc=" + qCalc);
            mostrarResultado(y);
        } catch (NumberFormatException e) {
            // Mostrar un mensaje si algún campo no está lleno o contiene valores no numéricos
            Toast.makeText(this, "Por favor, ingresa todos los valores correctamente.", Toast.LENGTH_SHORT).show();
        }
    }

}
