package com.jean704.calculadoracivilbyjean704;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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

    private Spinner spinnerFilasAcero;
    private LinearLayout varillaGrid;
    private LinearLayout buttonLayout;
    private ArrayList<LinearLayout> filasCirculos;
    private ArrayList<Integer> numCirculosPorFila; // Lista para almacenar el número de círculos por fila

    int maxCircles = 9; // Número máximo de círculos en una fila
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
        filasCirculos = new ArrayList<>();
        numCirculosPorFila = new ArrayList<>();

        // Configurar el Listener del Spinner para generar filas
        Spinner spinnerFilasAcero = findViewById(R.id.spinner_filas_acero);
        spinnerFilasAcero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int numFilas = Integer.parseInt(spinnerFilasAcero.getSelectedItem().toString());
                generarFilasConBotones(numFilas);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
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
        varillaGrid.removeAllViews();
        filasCirculos.clear();
        buttonLayout.removeAllViews();
        numCirculosPorFila.clear();

        for (int i = 0; i < numFilas; i++) {
            LinearLayout filaCirculosLayout = new LinearLayout(this);
            filaCirculosLayout.setOrientation(LinearLayout.HORIZONTAL);
            filaCirculosLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            varillaGrid.addView(filaCirculosLayout);
            filasCirculos.add(filaCirculosLayout);
            numCirculosPorFila.add(2);

            // Crear círculo izquierdo estático
            TextView circuloIzq = crearCirculo();
            filaCirculosLayout.addView(circuloIzq);

            // Contenedor central para círculos intermedios
            LinearLayout contenedorIntermedios = new LinearLayout(this);
            contenedorIntermedios.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams paramsIntermediosContainer = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            paramsIntermediosContainer.gravity = Gravity.CENTER;
            contenedorIntermedios.setLayoutParams(paramsIntermediosContainer);
            contenedorIntermedios.setBackground(ContextCompat.getDrawable(this, R.drawable.debug_border));
            filaCirculosLayout.addView(contenedorIntermedios);


            // Crear círculo derecho estático
            TextView circuloDer = crearCirculo();
            filaCirculosLayout.addView(circuloDer);

            // Añadir el contenedor intermedio a filasCirculos
            filasCirculos.set(i, contenedorIntermedios);
        }

        // Generación de botones fuera del ciclo de filas
        for (int i = 0; i < numFilas; i++) {
            final int index = i;
            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
            buttonRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            buttonRow.setGravity(Gravity.CENTER);

            Button botonMas = new Button(this);
            botonMas.setText("+");
            botonMas.setOnClickListener(v -> agregarCirculo(index));

            Button botonMenos = new Button(this);
            botonMenos.setText("-");
            botonMenos.setOnClickListener(v -> eliminarCirculo(index));

            buttonRow.addView(botonMas);
            buttonRow.addView(botonMenos);
            buttonLayout.addView(buttonRow);
        }
    }

    private TextView crearCirculo() {
        TextView circle = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
        params.setMargins(8, 8, 8, 8);
        circle.setLayoutParams(params);
        circle.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape));
        return circle;
    }


    private void agregarCirculo(int fila) {
        int numCirculos = numCirculosPorFila.get(fila);

        // Solo se pueden agregar círculos intermedios, sin afectar los extremos
        if (numCirculos < maxCircles) {
            numCirculosPorFila.set(fila, numCirculos + 1);
            actualizarCirculos(fila);
            Log.d("CalculoColumnas", "Número de círculos en fila " + fila + ": " + numCirculosPorFila.get(fila));
        }
    }

    private void eliminarCirculo(int fila) {
        int numCirculos = numCirculosPorFila.get(fila);

        // Asegurarse de que no haya menos de 2 círculos (uno izquierdo y uno derecho)
        if (numCirculos > 2) {
            // Si hay más de 2 círculos, eliminar uno intermedio
            numCirculosPorFila.set(fila, numCirculos - 1);  // Disminuir el número de círculos
            actualizarCirculos(fila);
            Log.d("CalculoColumnas", "Número de círculos en fila " + fila + ": " + numCirculosPorFila.get(fila));
        }
    }


    private void actualizarCirculos(int fila) {
        LinearLayout contenedorIntermedios = filasCirculos.get(fila);
        contenedorIntermedios.removeAllViews();

        int numCirculos = numCirculosPorFila.get(fila);
        int numEspacios = numCirculos - 2;

        for (int j = 0; j < numEspacios; j++) {
            TextView circuloIntermedio = crearCirculo();
            contenedorIntermedios.addView(circuloIntermedio);
        }
    }



}
