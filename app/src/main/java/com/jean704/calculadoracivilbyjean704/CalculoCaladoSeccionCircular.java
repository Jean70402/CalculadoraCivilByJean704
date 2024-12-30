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

import java.util.ArrayList;

public class CalculoCaladoSeccionCircular extends AppCompatActivity {

    private EditText inputQd, inputDd, inputN, inputS;
    private TextView outputResult;

    double thetha, areaCalc, perimetroCalc, radioHidraulicoCalc, velocidad, qCalc, qd, dd, n, s, y, radio;
    double T, Fr, D;
    double factorFriccion = 0.001;
    private ArrayList<double[]> tuberiasGuardadas = new ArrayList<>();
    private int contadorTuberias = 0; // Contador para asignar identificadores únicos


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
        Button calculateButtonDarcy = findViewById(R.id.calculateButtonDarcy);
        Button limpiarButton=findViewById(R.id.clearButton);
        Button guardarButton=findViewById(R.id.saveButton);
        Button mostrarButton=findViewById(R.id.showButton);
        buttonBackToMain.setOnClickListener(v -> {
            finish();
        });
        // Configuramos la acción del botón de cálculo
        calculateButton.setOnClickListener(v -> calculateResult());
        calculateButtonDarcy.setOnClickListener(v -> calculateDarcy());
        limpiarButton.setOnClickListener(v -> limpiarAccion());
        guardarButton.setOnClickListener(v -> guardarAccion());
        mostrarButton.setOnClickListener(v -> mostrarGuardados());
    }
    public void guardarAccion() {
        try {
            if (Double.isNaN(qCalc)) {
                Toast.makeText(this, "Realiza un cálculo antes de guardar.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Agregar resultados al array global
            double[] resultados = {y, areaCalc, perimetroCalc, velocidad, qCalc, T, Fr, porcentajeLlenado, factorFriccion};
            tuberiasGuardadas.add(resultados);
            contadorTuberias++; // Incrementar el contador de tuberías

            // Mostrar mensaje de confirmación
            Toast.makeText(this, "Resultados guardados como Tubería #" + contadorTuberias, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void mostrarGuardados() {
        StringBuilder savedData = new StringBuilder("Resultados Guardados:\n");
        for (int i = 0; i < tuberiasGuardadas.size(); i++) {
            savedData.append("Tubería #").append(i + 1).append(": ");
            for (double valor : tuberiasGuardadas.get(i)) {
                savedData.append(String.format("%.4f", valor)).append(" ");
            }
            savedData.append("\n");
        }
        Toast.makeText(this, savedData.toString(), Toast.LENGTH_LONG).show();
    }

    public void limpiarAccion(){
        inputQd.setText("");
        inputDd.setText("");
        //inputN.setText("");
        inputS.setText("");
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
            String resultText = String.format("%s: %.4f %s\n", descripciones[i], resultados[i], unidades[i]);
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
            String resultText = String.format("%.4f", resultados[i]);
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
            factorFriccion = 0;
            showResult();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa todos los valores correctamente.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void calculateDarcy() {
        try {
            qd = Double.parseDouble(inputQd.getText().toString());
            dd = Double.parseDouble(inputDd.getText().toString());
            n = Double.parseDouble(inputN.getText().toString());
            s = Double.parseDouble(inputS.getText().toString());

            dd = dd / 100; // Diámetro en metros
            radio = (dd / 2); // Radio en metros
            y = 0.001;
            qCalc = 0;
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
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa todos los valores correctamente.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void calcularFactorDeFriccion(double Dh) {
        double U = 1.004e-6; // Viscosidad cinemática en m^2/s
        double f = 0.02; // Factor de fricción inicial
        double tolerancia = 1e-10;
        double iteracionesMax = 100;
        double iteraciones = 0;

        while (iteraciones < iteracionesMax) {
            // Velocidad basada en el factor de fricción actual
            double velocidad = Math.sqrt(s * Dh * 2 * 9.81 / f);

            // Número de Reynolds
            double Reynolds = velocidad * Dh / U;

            // Validar que Reynolds sea positivo
            if (Reynolds <= 0) {
                Log.e("Darcy", "Reynolds inválido: " + Reynolds);
                factorFriccion = Double.NaN;
                return;
            }

            // Ecuación de Colebrook-White
            double ladoIzquierdo = 1 / Math.sqrt(f);
            double ladoDerecho = -2 * Math.log10((n / 1000 / (3.7 * Dh)) + 2.51 / (Reynolds * Math.sqrt(f)));

            // Diferencia entre ambos lados
            double diferencia = ladoIzquierdo - ladoDerecho;

            // Validar convergencia
            if (Math.abs(diferencia) < tolerancia) {
                factorFriccion = f;
                Log.d("Darcy", "El factor hallado es: " + f + " en " + iteraciones + " iteraciones");
                return;
            }

            // Derivada del lado derecho con respecto a f
            double g = (n / 1000 / (3.7 * Dh)) + (2.51 / (Reynolds * Math.sqrt(f)));
            double derivada = -0.5 / Math.pow(f, 1.5)
                    - (2 / (Math.log(10) * g)) * (-2.51 / (2 * Reynolds * Math.pow(f, 1.5)));

            // Validar derivada para evitar divisiones por cero
            if (derivada == 0) {
                Log.e("Darcy", "Derivada inválida (división por cero)");
                factorFriccion = Double.NaN;
                return;
            }

            // Actualizar f usando Newton-Raphson
            f = f - (diferencia / derivada);

            // Validar que f sea positivo
            if (f <= 0) {
                Log.e("Darcy", "Factor de fricción inválido: " + f);
                factorFriccion = Double.NaN;
                return;
            }

            iteraciones++;
        }

        // Si se alcanzó el límite de iteraciones sin converger
        Log.w("Darcy", "El cálculo de f no convergió después de " + iteracionesMax + " iteraciones");
        factorFriccion = Double.NaN;
    }

    public void repetirCalculoYDarcy(double yParametro) {
        y = yParametro;

        // Calcular geometría
        thetha = 2 * Math.acos(1 - (y / radio));
        areaCalc = 0.5 * (radio * radio) * (thetha - Math.sin(thetha)); // Área mojada
        perimetroCalc = radio * thetha; // Perímetro mojado
        double Dh = 4 * (areaCalc / perimetroCalc); // Diámetro hidráulico

        // Validar que el diámetro hidráulico sea positivo
        if (Dh <= 0) {
            Log.e("Darcy", "Diámetro hidráulico inválido: " + Dh);
            return;
        }

        // Recalcular factor de fricción con el nuevo diámetro hidráulico
        calcularFactorDeFriccion(Dh);

        // Validar que el factor de fricción sea válido
        if (Double.isNaN(factorFriccion)) {
            Log.e("Darcy", "Factor de fricción inválido durante el cálculo con y = " + y);
            return;
        }

        // Calcular velocidad y caudal
        velocidad = Math.sqrt(s * Dh * 2 * 9.81 / factorFriccion); // Velocidad con el nuevo factor de fricción
        T = Math.sin(thetha / 2) * dd; // Tirante hidráulico
        D = areaCalc / T;
        Fr = velocidad / Math.sqrt(9.81 * D);
        qCalc = velocidad * areaCalc * 1000; // Caudal en L/s

        Log.d("Darcy", "Calado (y): " + y + ", Factor de fricción: " + factorFriccion + ", Velocidad: " + velocidad + ", Caudal: " + qCalc);
    }

    double porcentajeLlenado;

    public void showResult() {
        y = y * 100;// (cm)
        areaCalc = areaCalc * 100 * 100;
        perimetroCalc = perimetroCalc * 100;
        String tipodeFlujo = "";
        dd = Double.parseDouble(inputDd.getText().toString());
        porcentajeLlenado = areaCalc / (Math.PI * dd * dd / 4) * 100;
        if (Fr > 1) {
            tipodeFlujo = "Flujo supercrítico";
        }
        if (Fr == 1) {
            tipodeFlujo = "Flujo en transición";
        }
        if (Fr < 1) {
            tipodeFlujo = "Flujo subcrítico";
        }

        double[] resultados = {y, areaCalc, perimetroCalc, velocidad, qCalc, T, Fr, porcentajeLlenado, factorFriccion};
        String[] descripciones = {"Calado", "Área", "Perímetro", "Velocidad", "Caudal", "Espejo de agua", "Froude", "Porcentaje de llenado", "Factor de Fricción"};
        String[] unidades = {"(cm)", "(cm^2)", "(cm)", "(m/s)", "(l/s)", "(m)", "  " + tipodeFlujo, "%", "-"};
        mostrarResultados(resultados, descripciones, unidades);
    }
}