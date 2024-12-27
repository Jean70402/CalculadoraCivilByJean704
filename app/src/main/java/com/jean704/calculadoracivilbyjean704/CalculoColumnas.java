package com.jean704.calculadoracivilbyjean704;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class CalculoColumnas extends AppCompatActivity {

    private EditText inputEs, inputFc, inputFy, inputB, inputH,inputR;
    private Button buttonCalculate, buttonBackToMain;
    private TextView resultTextView;
    private LinearLayout varillaGrid;
    private LinearLayout buttonLayout;
    private ArrayList<LinearLayout> filasCirculos;
    private ArrayList<Integer> numCirculosPorFila; // Lista para almacenar el número de círculos por fila

    int maxCircles = 9; // Número máximo de círculos en una fila
    int tamanoCircles = (Math.min(80, maxCircles*10));
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
        inputR = findViewById(R.id.input_r);
        buttonCalculate = findViewById(R.id.button_calculate);
        buttonBackToMain = findViewById(R.id.button_back_to_main);

        // Configurar el botón de cálculo
        buttonCalculate.setOnClickListener(v -> calculateProduct());

        buttonBackToMain.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual para regresar a la anterior (menú principal)
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
            contenedorIntermedios.setGravity(Gravity.START);
            LinearLayout.LayoutParams paramsIntermediosContainer = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            contenedorIntermedios.setLayoutParams(paramsIntermediosContainer);
            filaCirculosLayout.addView(contenedorIntermedios);

            // Listener para obtener el ancho del contenedor central
            int finalI = i;
            contenedorIntermedios.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    contenedorIntermedios.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int anchoContenedor = contenedorIntermedios.getWidth();
                    agregarCirculosDesdeCentro(finalI, anchoContenedor);
                }
            });

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

            // Crear un TextView con la palabra "As:"
            TextView textoAs = new TextView(this);
            textoAs.setText("phi:");
            textoAs.setPadding(0, 0, 10, 0);  // Opcional, para añadir algo de espacio entre el texto y el spinner

            // Crear el Spinner con valores de As_string
            Spinner spinnerAs = new Spinner(this);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.As_string, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAs.setAdapter(adapter);
            // Añadir los botones y el spinner a la fila
            buttonRow.addView(botonMas);
            buttonRow.addView(botonMenos);
            buttonRow.addView(textoAs);
            buttonRow.addView(spinnerAs);
            buttonLayout.addView(buttonRow);
        }
    }


    private TextView crearCirculo() {
        TextView circle = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tamanoCircles, tamanoCircles);
        params.setMargins(0, 0, 0, 0);
        circle.setLayoutParams(params);
        circle.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_shape));
        return circle;
    }

    private void agregarCirculosDesdeCentro(int fila, int anchoContenedor) {
        LinearLayout contenedorIntermedios = filasCirculos.get(fila);
        contenedorIntermedios.removeAllViews();

        // Solo círculos intermedios (sin contar los extremos)
        int numCirculos = numCirculosPorFila.get(fila) - 2;
        if (numCirculos <= 0) return;

        // Tamaño de los círculos

        // Verificar si los círculos caben en el contenedor
        int totalCirculosAncho = tamanoCircles * numCirculos + (maxCircles-2) * numCirculos; // Tamaño de los círculos + márgenes
        if (totalCirculosAncho > anchoContenedor) {
            // Si no caben, limitamos el número de círculos
            numCirculos = (anchoContenedor - 8 * numCirculos) / tamanoCircles;
        }

        // Calcular el espacio entre círculos
        int espacioEntreCirculos = (anchoContenedor - tamanoCircles * numCirculos - (maxCircles-2) * numCirculos) / (numCirculos + 1);

        // Eliminar la gravedad centrada
        contenedorIntermedios.setGravity(Gravity.START);  // Establece la alineación al inicio (izquierda)

        // Aplicar el mismo margen a todos los círculos
        for (int j = 0; j < numCirculos; j++) {
            TextView circuloIntermedio = crearCirculo();

            // Ajustar margen izquierdo para espaciar los círculos
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tamanoCircles, tamanoCircles);
            params.leftMargin = espacioEntreCirculos+(tamanoCircles/(maxCircles));  // Aplica el mismo margen a todos los círculos
            circuloIntermedio.setLayoutParams(params);

            contenedorIntermedios.addView(circuloIntermedio);

            // Log para depuración
            Log.d("CalculoColumnas", "Círculo intermedio " + j + " en fila " + fila + ": posición x = " + params.leftMargin);
        }
    }


    private void agregarCirculo(int fila) {
        int numCirculos = numCirculosPorFila.get(fila);

        // Solo se pueden agregar círculos intermedios, sin afectar los extremos
        if (numCirculos < maxCircles) {
            numCirculosPorFila.set(fila, numCirculos + 1);

            // Usar el ancho actual del contenedor para reposicionar los círculos
            LinearLayout contenedorIntermedios = filasCirculos.get(fila);
            int anchoContenedor = contenedorIntermedios.getWidth();
            agregarCirculosDesdeCentro(fila, anchoContenedor);
        }
    }

    private void eliminarCirculo(int fila) {
        int numCirculos = numCirculosPorFila.get(fila);

        // Asegurarse de que no haya menos de 2 círculos (uno izquierdo y uno derecho)
        if (numCirculos > 2) {
            numCirculosPorFila.set(fila, numCirculos - 1);

            // Usar el ancho actual del contenedor para reposicionar los círculos
            LinearLayout contenedorIntermedios = filasCirculos.get(fila);
            int anchoContenedor = contenedorIntermedios.getWidth();
            agregarCirculosDesdeCentro(fila, anchoContenedor);
        }
    }

    private void calculateProduct() {
        try {
            // Leer valores de los campos de entrada y convertirlos a double
            double es = Double.parseDouble(inputEs.getText().toString());
            double fc = Double.parseDouble(inputFc.getText().toString());
            double fy = Double.parseDouble(inputFy.getText().toString());
            double b = Double.parseDouble(inputB.getText().toString());
            double h = Double.parseDouble(inputH.getText().toString());
            double r = Double.parseDouble(inputR.getText().toString());
            // Calcular el producto de todos los valores
            double result = es * fc * fy * b * h;

            // Mostrar el resultado
           // resultTextView.setText("Resultado: " + result);
            ObtenerAs();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor ingrese todos los valores correctamente", Toast.LENGTH_SHORT).show();
        }
    }
    private void ObtenerAs() {

        // Obtener el valor seleccionado en el Spinner
        Spinner spinnerFilasAcero = findViewById(R.id.spinner_filas_acero);

        String valorSeleccionado = spinnerFilasAcero.getSelectedItem().toString();

        // Obtener el número de filas
        int numFilas = Integer.parseInt((valorSeleccionado));
        for (int i = 0; i < numFilas; i++) {
            LinearLayout buttonRow = (LinearLayout) buttonLayout.getChildAt(i); // Obtener la fila
            Spinner spinnerAs = (Spinner) buttonRow.getChildAt(1); // El Spinner está en la segunda posición (índice 1)
            Log.d("ObtenerAs", "Fila " + i + ": Valor seleccionado en Spinner As: " + valorSeleccionado);
        }
        // Puedes hacer algo con los valores obtenidos, por ejemplo:
        Log.d("ObtenerAs", "Valor seleccionado: " + valorSeleccionado);
        Log.d("ObtenerAs", "Número de filas: " + numFilas);

        // Aquí podrías realizar más operaciones según tus necesidades
    }

}
