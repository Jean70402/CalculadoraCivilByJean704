package com.jean704.calculadoracivilbyjean704;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class CalculoColumnas extends AppCompatActivity {

    private EditText inputEs, inputFc, inputFy, inputB, inputH;
    private Button buttonCalculate, buttonBackToMain;
    private TextView resultTextView;

    private GridLayout varillaGrid;
    private LinearLayout buttonLayout;
    private Spinner spinnerFilasAcero;
    private ArrayList<LinearLayout> filasCirculos; // Para almacenar las filas de círculos
    private ArrayList<Integer> circulosPorFila;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculo_columnas);

        // Inicializar vistas
        inputEs = findViewById(R.id.input_es);
        inputFc = findViewById(R.id.input_fc);
        inputFy = findViewById(R.id.input_fy);
        inputB = findViewById(R.id.input_b);
        inputH = findViewById(R.id.input_h);
        buttonCalculate = findViewById(R.id.button_calculate);
        buttonBackToMain = findViewById(R.id.button_back_to_main);

        // Configurar el botón de cálculo
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateProduct();
            }
        });

        // Configurar el botón para regresar al menú principal
        buttonBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra la actividad actual para regresar a la anterior (menú principal)
            }
        });

        varillaGrid = findViewById(R.id.varillaGrid);
        buttonLayout = findViewById(R.id.buttonLayout);
        spinnerFilasAcero = findViewById(R.id.spinner_filas_acero);

        filasCirculos = new ArrayList<>();
        circulosPorFila = new ArrayList<>(); // Inicializamos el contador de círculos por fila

        // Configurar el Listener del Spinner para generar filas y botones dinámicamente
        spinnerFilasAcero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int numFilas = Integer.parseInt(spinnerFilasAcero.getSelectedItem().toString());
                generarFilasConBotones(numFilas); // Llamar al método para generar las filas y botones
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Llamar al método inicial para crear las filas y botones
        generarFilasConBotones(Integer.parseInt(spinnerFilasAcero.getSelectedItem().toString()));
    }



    private void calculateProduct() {
        try {
            // Leer valores de los campos de entrada y convertirlos a double
            double es = Double.parseDouble(inputEs.getText().toString());
            double fc = Double.parseDouble(inputFc.getText().toString());
            double fy = Double.parseDouble(inputFy.getText().toString());
            double b = Double.parseDouble(inputB.getText().toString());
            double h = Double.parseDouble(inputH.getText().toString());

            // Calcular el producto de todos los valores
            double result = es * fc * fy * b * h;

            // Mostrar el resultado
            resultTextView.setText("Resultado: " + result);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor ingrese todos los valores correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    private void generarFilasConBotones(int numFilas) {
        // Limpiar cualquier vista anterior
        varillaGrid.removeAllViews();
        buttonLayout.removeAllViews();
        filasCirculos.clear();
        circulosPorFila.clear();

        // Generar las filas y los botones "+" y "-" correspondientes
        for (int i = 0; i < numFilas; i++) {
            final int filaIndex = i; // Índice de la fila actual

            // Crear una fila para los círculos
            LinearLayout filaCirculosLayout = new LinearLayout(this);
            filaCirculosLayout.setOrientation(LinearLayout.HORIZONTAL);
            filaCirculosLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // Añadir la fila de círculos al GridLayout
            varillaGrid.addView(filaCirculosLayout);

            // Crear una fila de botones "+"
            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
            buttonRow.setGravity(Gravity.CENTER);

            // Crear el botón "+"
            Button botonMas = new Button(this);
            botonMas.setText("+");
            botonMas.setOnClickListener(v -> agregarCirculo(filaIndex));

            // Crear el botón "-"
            Button botonMenos = new Button(this);
            botonMenos.setText("-");
            botonMenos.setOnClickListener(v -> quitarCirculo(filaIndex));

            // Añadir los botones a la fila de botones
            buttonRow.addView(botonMas);
            buttonRow.addView(botonMenos);

            // Añadir la fila de botones al layout de botones
            buttonLayout.addView(buttonRow);

            // Guardar la referencia de la fila de círculos y el contador de círculos
            filasCirculos.add(filaCirculosLayout);
            circulosPorFila.add(0); // Inicializamos el contador de círculos en 0
        }
    }

    private void agregarCirculo(int filaIndex) {
        // Crear un nuevo círculo (usaremos TextView para los círculos)
        TextView circle = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
        params.setMargins(8, 8, 8, 8);
        circle.setLayoutParams(params);
        circle.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape)); // Estilo de círculo

        // Añadir el círculo a la fila correspondiente
        filasCirculos.get(filaIndex).addView(circle);

        // Actualizar el contador de círculos para esa fila
        int count = circulosPorFila.get(filaIndex);
        circulosPorFila.set(filaIndex, count + 1);
    }

    private void quitarCirculo(int filaIndex) {
        // Eliminar el último círculo de la fila si existe
        LinearLayout filaCirculosLayout = filasCirculos.get(filaIndex);
        if (filaCirculosLayout.getChildCount() > 0) {
            filaCirculosLayout.removeViewAt(filaCirculosLayout.getChildCount() - 1);
        }

        // Actualizar el contador de círculos para esa fila
        int count = circulosPorFila.get(filaIndex);
        circulosPorFila.set(filaIndex, Math.max(count - 1, 0));
    }

}

