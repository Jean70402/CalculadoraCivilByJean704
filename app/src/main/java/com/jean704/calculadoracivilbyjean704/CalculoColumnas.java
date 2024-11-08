package com.jean704.calculadoracivilbyjean704;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CalculoColumnas extends AppCompatActivity {

    private EditText inputEs, inputFc, inputFy, inputB, inputH;
    private Button buttonCalculate, buttonBackToMain;
    private TextView resultTextView;

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
        resultTextView = findViewById(R.id.resultTextView); // Crear un TextView para mostrar el resultado

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
}

