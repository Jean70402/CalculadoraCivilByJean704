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
        // Limpiar cualquier vista anterior
        varillaGrid.removeAllViews();
        buttonLayout.removeAllViews();
        filasCirculos.clear();

        // Generar las filas y los botones "+" y "-" correspondientes
        for (int i = 0; i < numFilas; i++) {
            final int filaIndex = i;

            // Crear una fila para los círculos
            LinearLayout filaCirculosLayout = new LinearLayout(this);
            filaCirculosLayout.setOrientation(LinearLayout.HORIZONTAL);
            filaCirculosLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            varillaGrid.addView(filaCirculosLayout);
            filasCirculos.add(filaCirculosLayout);

            // Crear el botón "+"
            Button botonMas = new Button(this);
            botonMas.setText("+");

            // Crear el botón "-"
            Button botonMenos = new Button(this);
            botonMenos.setText("-");

            // Añadir los botones a la fila de botones
            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setGravity(Gravity.CENTER);
            buttonRow.addView(botonMas);
            buttonRow.addView(botonMenos);

            buttonLayout.addView(buttonRow);

            // Crear 2 círculos iniciales (uno a cada extremo)
            for (int j = 0; j < 9; j++) {
                TextView space = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
                params.setMargins(8, 8, 8, 8);
                space.setLayoutParams(params);
                space.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape));

                if (j == 0 || j == 8) {
                    space.setTag("circle");
                } else {
                    space.setTag("empty");
                    space.setVisibility(View.INVISIBLE);
                }
                filaCirculosLayout.addView(space);
            }

            botonMas.setOnClickListener(v -> {
                Log.d("CalculoColumnas", "Presionando botón '+' en fila: " + filaIndex);
                agregarCirculo(filaCirculosLayout);
            });

            botonMenos.setOnClickListener(v -> {
                Log.d("CalculoColumnas", "Presionando botón '-' en fila: " + filaIndex);
                eliminarUltimoCirculo(filaCirculosLayout);
            });
        }
    }

    private void agregarCirculo(LinearLayout filaCirculosLayout) {
        // Obtener las posiciones de los círculos fijos visibles (al inicio y al final)
        ArrayList<Integer> posiciones = new ArrayList<>();
        for (int i = 0; i < filaCirculosLayout.getChildCount(); i++) {
            TextView circle = (TextView) filaCirculosLayout.getChildAt(i);
            if (circle.getTag() != null && circle.getTag().equals("circle") && circle.getVisibility() == View.VISIBLE) {
                posiciones.add(i);
            }
        }

        // Si hay exactamente 2 círculos visibles (en los extremos), agregar un nuevo círculo intermedio
        if (posiciones.size() == 2) {
            int posInicio = posiciones.get(0);
            int posFin = posiciones.get(1);
            int nuevaPosicion = (posInicio + posFin) / 2;

            TextView space = (TextView) filaCirculosLayout.getChildAt(nuevaPosicion);
            space.setVisibility(View.VISIBLE);
            space.setTag("circle"); // Marcar como un círculo
            Log.d("AgregarCirculo", "Círculo añadido en la posición intermedia: " + nuevaPosicion);

        } else if (posiciones.size() > 2) {
            // Si ya hay círculos intermedios, encontrar la primera posición vacía entre ellos para añadir otro
            for (int i = 1; i < posiciones.size(); i++) {
                int posInicio = posiciones.get(i - 1);
                int posFin = posiciones.get(i);
                int nuevaPosicion = (posInicio + posFin) / 2;

                TextView space = (TextView) filaCirculosLayout.getChildAt(nuevaPosicion);
                if (space.getVisibility() == View.INVISIBLE) {
                    space.setVisibility(View.VISIBLE);
                    space.setTag("circle");
                    Log.d("AgregarCirculo", "Nuevo círculo añadido entre posiciones " + posInicio + " y " + posFin);
                    break; // Solo agregar un círculo
                }
            }
        }
    }

    private void eliminarUltimoCirculo(LinearLayout filaCirculosLayout) {
        // Recorrer desde el final para eliminar el último círculo intermedio añadido
        for (int i = filaCirculosLayout.getChildCount() - 2; i > 0; i--) {
            TextView circle = (TextView) filaCirculosLayout.getChildAt(i);
            if (circle.getTag() != null && circle.getTag().equals("circle") && circle.getVisibility() == View.VISIBLE) {
                circle.setVisibility(View.INVISIBLE); // Ocultar el último círculo visible
                Log.d("EliminarUltimoCirculo", "Círculo eliminado en la posición: " + i);
                break; // Detener el ciclo después de eliminar uno
            }
        }
    }

}

