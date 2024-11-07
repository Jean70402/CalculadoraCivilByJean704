package com.jean704.calculadoracivilbyjean704;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
    public void mostrarResultados(double[] resultados, String[] descripciones, String[] unidades) {
        // Asegura que todos los arreglos tengan el mismo tamaño
        if (resultados.length != descripciones.length || descripciones.length != unidades.length) {
            Toast.makeText(this, "Los parámetros no coinciden en tamaño.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Genera el texto completo con descripciones, resultados y unidades
        StringBuilder fullText = new StringBuilder();
        for (int i = 0; i < resultados.length; i++) {
            String resultText = String.format("%s: %.5f %s\n", descripciones[i], resultados[i], unidades[i]);
            fullText.append(resultText);
        }

        // Crea el SpannableString y aplica el color solo a los valores
        SpannableString spannable = new SpannableString(fullText.toString());
        int color = getResources().getColor(R.color.teal_700);

        // Aplicar el color a cada resultado
        int index = 0;

        for (int i = 0; i < resultados.length; i++) {
            // Construir cada línea de texto
            String descripcion = descripciones[i] + ": ";
            String resultText = String.format("%.5f", resultados[i]);
            String unidad = " " + unidades[i] + "\n";

            // Encontrar el índice de inicio y fin para el valor a colorear
            int start = index + descripcion.length();
            int end = start + resultText.length();

            // Aplicar el color solo al valor del resultado
            spannable.setSpan(
                    new ForegroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // Actualizar el índice para la siguiente línea
            index += descripcion.length() + resultText.length() + unidad.length();
        }
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
            double[] resultados = { y, areaCalc, perimetroCalc, velocidad, qCalc };
            String[] descripciones = {"Calado", "Área", "Perímetro","Velocidad","Caudal"};
            String[] unidades = {"(m)", "(m^2)", "(m)","(m/s)","(m^3/s)"};
            mostrarResultados(resultados,descripciones,unidades);
        } catch (NumberFormatException e) {
            // Mostrar un mensaje si algún campo no está lleno o contiene valores no numéricos
            Toast.makeText(this, "Por favor, ingresa todos los valores correctamente.", Toast.LENGTH_SHORT).show();
        }
    }

}