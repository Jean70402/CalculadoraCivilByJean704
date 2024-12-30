package com.jean704.calculadoracivilbyjean704;

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

import androidx.appcompat.app.AppCompatActivity;

public class CalculoCaladoSeccionCircular extends AppCompatActivity {

    private EditText inputQd, inputDd, inputN, inputS;
    private TextView outputResult;

    double thetha, areaCalc, perimetroCalc, radioHidraulicoCalc, velocidad, qCalc, qd, dd, n, s, y, radio;
    double T, Fr, D;

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
        Button buttonBackToMain = findViewById(R.id.button_back_to_main);
        Button calculateButtonDarcy= findViewById(R.id.calculateButtonDarcy);
        buttonBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra la actividad actual para regresar a la anterior (menú principal)
            }
        });

        // Configuramos la acción del botón de cálculo
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateResult();
            }
        });
        calculateButtonDarcy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDarcy();
            }
        });
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

    public void repetirCalculo(double yCalculo) {
        y = yCalculo;
        thetha = 2 * Math.acos(1 - (y / radio));
        areaCalc = 0.5 * (radio * radio) * (thetha - Math.sin(thetha));
        perimetroCalc = radio * thetha;
        radioHidraulicoCalc = Math.pow((areaCalc / perimetroCalc), ((double) 2 / 3));
        velocidad = (1 / n) * radioHidraulicoCalc * Math.pow(s, 0.5);
        T = Math.sin(thetha / 2) * dd / 100;
        D = areaCalc / T;
        Fr = velocidad / Math.pow(9.81 * D, 0.5);
        qCalc = velocidad * areaCalc * 1000;
    }

    public void calculateResult() {
        try {
            // Obtener y convertir los valores de entrada
            qd = Double.parseDouble(inputQd.getText().toString());
            dd = Double.parseDouble(inputDd.getText().toString());
            n = Double.parseDouble(inputN.getText().toString());
            s = Double.parseDouble(inputS.getText().toString());

            radio = (dd / 2) / 100; // Radio en metros

            y = 0.0001;
            qCalc = 0;
            double incremento = 0.001;
            double tolerancia = 1e-6;
            double iteracionesMax = 1e6;
            double iteraciones = 0;

            while (Math.abs(qCalc - qd) > tolerancia && iteraciones < iteracionesMax) {
                repetirCalculo(y); // Calcula el caudal qCalc para el valor actual de y

                if (qCalc < qd) {
                    y += incremento; // Incrementa y si qCalc es menor que qd
                } else {
                    incremento /= 2; // Reduce el incremento para mayor precisión
                    y -= incremento; // Retrocede si excedemos el caudal objetivo
                }
                y = Math.min(y, dd);
                iteraciones++;
            }

            if (iteraciones >= iteracionesMax) {
                throw new ArithmeticException("No se encontró solución dentro del máximo de iteraciones.");
            }
            showResult();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa todos los valores correctamente.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    double factorFriccion = 0.001;

    public void calcularFactorDeFriccion() {
        qd = Double.parseDouble(inputQd.getText().toString());
        dd = Double.parseDouble(inputDd.getText().toString());
        n = Double.parseDouble(inputN.getText().toString());
        s = Double.parseDouble(inputS.getText().toString());

        dd = dd / 100; // Diámetro en metros
        double U = 1.004e-6; // Viscosidad cinemática en m^2/s
        double Dh = dd;      // Diámetro hidráulico
        double tolerancia = 1e-10;
        double iteracionesMax = 100;
        double iteraciones = 0;

        while (iteraciones < iteracionesMax) {
            double velocidad = Math.sqrt(s * dd * 2 * 9.81 / factorFriccion);
            double Reynolds = velocidad * Dh / U;
            double ladoIzquierdo = 1 / Math.sqrt(factorFriccion);
            double ladoDerecho = -2 * Math.log10((n / 1000 / (3.7 * Dh)) + 2.51 / (Reynolds * Math.sqrt(factorFriccion)));
            double diferencia = ladoIzquierdo - ladoDerecho;
            if (Math.abs(diferencia) < tolerancia) {
                break;
            }
            // Derivada del lado izquierdo con respecto al factor de fricción
            double g = (n / 1000 / (3.7 * Dh)) + (2.51 / (Reynolds * Math.sqrt(factorFriccion)));
            double derivada = -0.5 / Math.pow(factorFriccion, 1.5)
                    - (2 / (Math.log(10) * g)) * (-2.51 / (2 * Reynolds * Math.pow(factorFriccion, 1.5)));
            factorFriccion = factorFriccion - (diferencia / derivada);
            iteraciones++;
        }
        Log.d("Darcy", "El factor encontrado es: " + factorFriccion+"en "+ iteraciones+" iteraciones");
    }
    public void calculateDarcy(){
        calcularFactorDeFriccion();
        qd = Double.parseDouble(inputQd.getText().toString());
        dd = Double.parseDouble(inputDd.getText().toString());
        n = Double.parseDouble(inputN.getText().toString());
        s = Double.parseDouble(inputS.getText().toString());
        Log.d("Darcy", "La velocidad es: "+velocidad);
        radio = (dd / 2) / 100; // Radio en metros
        y = 0.0001;
        qCalc=0;
        double incremento = 0.001;
        double tolerancia = 1e-6;
        double iteracionesMax = 1e6;
        double iteraciones = 0;

        while (Math.abs(qCalc - qd) > tolerancia && iteraciones < iteracionesMax) {
            repetirCalculoYDarcy(y); // Calcula el caudal qCalc para el valor actual de y

            if (qCalc < qd) {
                y += incremento; // Incrementa y si qCalc es menor que qd
            } else {
                incremento /= 2; // Reduce el incremento para mayor precisión
                y -= incremento; // Retrocede si excedemos el caudal objetivo
            }
            y = Math.min(y, dd);
            iteraciones++;
        }
        showResult();
    }
    public void repetirCalculoYDarcy(double yParametro){
        y = yParametro;
        thetha = 2 * Math.acos(1 - (y / radio));
        areaCalc = 0.5 * (radio * radio) * (thetha - Math.sin(thetha));
        perimetroCalc = radio * thetha;
        radioHidraulicoCalc = Math.pow((areaCalc / perimetroCalc), ((double) 2 / 3));
        //velocidad = (1 / n) * radioHidraulicoCalc * Math.pow(s, 0.5);
        T = Math.sin(thetha / 2) * dd / 100;
        D = areaCalc / T;
        Fr = velocidad / Math.pow(9.81 * D, 0.5);
        qCalc = velocidad * areaCalc * 1000;
    }

    public void showResult(){
        y = y * 100;// (cm)
        areaCalc = areaCalc * 100 * 100;
        perimetroCalc = perimetroCalc * 100;
        String tipodeFlujo = "";

        if (Fr > 1) {
            tipodeFlujo = "Flujo supercrítico";
        }
        if (Fr == 1) {
            tipodeFlujo = "Flujo en transición";
        }
        if (Fr < 1) {
            tipodeFlujo = "Flujo subcrítico";
        }

        double[] resultados = {y, areaCalc, perimetroCalc, velocidad, qCalc, T, Fr};
        String[] descripciones = {"Calado", "Área", "Perímetro", "Velocidad", "Caudal", "Espejo de agua", "Froude"};
        String[] unidades = {"(cm)", "(cm^2)", "(cm)", "(m/s)", "(l/s)", "(m)", "  "+tipodeFlujo};
        mostrarResultados(resultados, descripciones, unidades);
    }
    /*
        while  (Math.abs((ladoIzquierdo-ladoDerecho)/ladoDerecho)> tolerancia && iteraciones<iteracionesMax) {
            thetha = 2 * Math.acos(1 - (y / radio));
            areaCalc = 0.5 * (radio * radio) * (thetha - Math.sin(thetha));
            perimetroCalc = radio * thetha;
            radioHidraulicoCalc = (areaCalc / perimetroCalc);
            Dh=4*radioHidraulicoCalc;
            velocidad = Math.sqrt(s*dd*2*9.81/factorFriccion);
            Reynolds=velocidad*Dh/U;
            T = Math.sin(thetha / 2) * dd / 100;
            D = areaCalc / T;
            Fr = velocidad / Math.sqrt(9.81 * D);
            qCalc = velocidad * areaCalc * 1000;
            ladoIzquierdo= 1/(Math.sqrt(factorFriccion));
            ladoDerecho=-2*Math.log10((n/1000/(3.7*Dh))+2.51/(Reynolds*(Math.sqrt(factorFriccion))));
            if (qCalc < qd) {
                y += incremento; // Incrementa y si qCalc es menor que qd
            } else {
                incremento /= 2; // Reduce el incremento para mayor precisión
                y -= incremento; // Retrocede si excedemos el caudal objetivo
            }
            y = Math.min(y, dd);
            iteraciones++;
        }
        showResult();
        Log.d("Darcy","El factor hallado es: "+factorFriccion);
        */
}