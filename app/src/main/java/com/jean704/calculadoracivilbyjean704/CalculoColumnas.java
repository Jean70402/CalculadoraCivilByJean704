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
        // Limpiar cualquier vista anterior
        varillaGrid.removeAllViews();
        filasCirculos.clear();
        buttonLayout.removeAllViews();  // Limpiar botones previos
        numCirculosPorFila.clear();  // Limpiar el contador de círculos

        // Generar filas con círculos en los extremos
        for (int i = 0; i < numFilas; i++) {
            // Crear un LinearLayout con orientación horizontal para la fila de círculos
            LinearLayout filaCirculosLayout = new LinearLayout(this);
            filaCirculosLayout.setOrientation(LinearLayout.HORIZONTAL);
            filaCirculosLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // Añadir la fila al contenedor de varillas
            varillaGrid.addView(filaCirculosLayout);
            filasCirculos.add(filaCirculosLayout);

            // Inicializar el contador de círculos por fila
            numCirculosPorFila.add(2); // Empieza con 2 círculos

            // Crear los círculos iniciales (uno en cada extremo)
            for (int j = 0; j < maxCircles; j++) {
                TextView space = new TextView(this);
                LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(80, 80);
                circleParams.setMargins(8, 8, 8, 8);
                space.setLayoutParams(circleParams);
                space.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape));

                // Los círculos en los extremos siempre están visibles
                if (j == 0 || j == maxCircles - 1) {
                    space.setVisibility(View.VISIBLE);
                    space.setTag("circle");
                } else {
                    space.setVisibility(View.INVISIBLE); // Invisible para los círculos intermedios
                }
                filaCirculosLayout.addView(space);
            }
        }

        // Ahora, generar los botones al final, fuera del ciclo de filas
        for (int i = 0; i < numFilas; i++) {
            final int index = i;
            // Crear un LinearLayout para los botones "+" y "-"
            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
            buttonRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            buttonRow.setGravity(Gravity.CENTER);

            // Crear el botón "+"
            Button botonMas = new Button(this);
            botonMas.setText("+");
            botonMas.setOnClickListener(v -> agregarCirculo(index));

            // Crear el botón "-"
            Button botonMenos = new Button(this);
            botonMenos.setText("-");
            botonMenos.setOnClickListener(v -> eliminarCirculo(index));

            // Añadir los botones al LinearLayout
            buttonRow.addView(botonMas);
            buttonRow.addView(botonMenos);

            // Añadir la fila de botones al final
            buttonLayout.addView(buttonRow);
        }
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
        LinearLayout filaLayout = filasCirculos.get(fila);
        filaLayout.removeAllViews();  // Limpiar la fila de círculos

        // Número actual de círculos
        int numCirculos = numCirculosPorFila.get(fila);

        // Crear el LinearLayout con orientación horizontal para contener los círculos
        LinearLayout contenedorCirculos = new LinearLayout(this);
        contenedorCirculos.setOrientation(LinearLayout.HORIZONTAL);
        contenedorCirculos.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        filaLayout.addView(contenedorCirculos);

        // Tamaño fijo de los círculos
        int tamanoCirculo = 80;  // Tamaño fijo para los círculos
        int margen = 16;         // Márgenes entre los círculos

        // Crear el primer círculo visible (extremo izquierdo)
        TextView circuloIzq = new TextView(this);
        LinearLayout.LayoutParams paramsIzq = new LinearLayout.LayoutParams(tamanoCirculo, tamanoCirculo);
        paramsIzq.setMargins(margen, 8, margen, 8);  // Márgenes para el círculo izquierdo
        circuloIzq.setLayoutParams(paramsIzq);
        circuloIzq.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape));
        circuloIzq.setVisibility(View.VISIBLE);
        contenedorCirculos.addView(circuloIzq);

        // Crear los círculos intermedios si es necesario
        if (numCirculos > 2) {
            // Crear un LinearLayout para los círculos intermedios
            LinearLayout contenedorIntermedios = new LinearLayout(this);
            contenedorIntermedios.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsIntermediosContainer = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            contenedorIntermedios.setLayoutParams(paramsIntermediosContainer);
            contenedorCirculos.addView(contenedorIntermedios);

            // Calcular el número de espacios a distribuir entre los círculos intermedios
            int numEspacios = numCirculos - 2; // Espacios entre círculos intermedios

            // Espacio adicional que se distribuirá entre los círculos intermedios
            int espacioEntreCirculos = (int) ((float) (contenedorCirculos.getWidth() - 2 * tamanoCirculo - 2 * margen) / numEspacios);

            // Si el espacio calculado es menor que el margen mínimo, ajustamos el valor
            if (espacioEntreCirculos < margen) {
                espacioEntreCirculos = margen;
            }

            // Crear los círculos intermedios
            for (int j = 0; j < numEspacios; j++) {
                TextView circuloIntermedio = new TextView(this);
                LinearLayout.LayoutParams paramsIntermedio = new LinearLayout.LayoutParams(tamanoCirculo, tamanoCirculo);
                paramsIntermedio.setMargins(espacioEntreCirculos, 8, espacioEntreCirculos, 8);  // Márgenes dinámicos
                circuloIntermedio.setLayoutParams(paramsIntermedio);
                circuloIntermedio.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape));
                circuloIntermedio.setVisibility(View.VISIBLE);
                contenedorIntermedios.addView(circuloIntermedio);
            }
        }

        // Crear el último círculo visible (extremo derecho)
        TextView circuloDer = new TextView(this);
        LinearLayout.LayoutParams paramsDer = new LinearLayout.LayoutParams(tamanoCirculo, tamanoCirculo);
        paramsDer.setMargins(margen, 8, margen, 8);  // Márgenes para el círculo derecho
        circuloDer.setLayoutParams(paramsDer);
        circuloDer.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape));
        circuloDer.setVisibility(View.VISIBLE);
        contenedorCirculos.addView(circuloDer);
    }


}
