package com.jean704.calculadoracivilbyjean704;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalculoCaladoSeccionCircular extends AppCompatActivity {
    //Definición de variables globales, además de listas y demás elementos necesarios para la exportación de archivos
    //Se utilizan valores de cración de archivos para importar listas grandes de valores en una sola corrida del programa.
    //Los valores finales pueden ser exportados mediante un excel y enviados a WhatsApp.
    private EditText inputQd, inputDd, inputN, inputS;
    private TextView outputResult;

    double thetha, areaCalc, perimetroCalc, radioHidraulicoCalc, velocidad, qCalc, qd, dd, n, s, y, radio;
    double T, Fr, D;
    double porcentajeLlenado;
    double factorFriccion = 0.001;
    private ArrayList<double[]> tuberiasGuardadas = new ArrayList<>();
    private int contadorTuberias = 0; // Contador para asignar identificadores únicos
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String sheetName = "Resultados_" + timeStamp;
    private static final int REQUEST_CODE_EXCEL = 1;

    //Funciones de inicialización y lectura de datos.
    private ActivityResultLauncher<Intent> saveFileLauncher;

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
        Button limpiarButton = findViewById(R.id.clearButton);
        Button guardarButton = findViewById(R.id.saveButton);
        Button mostrarButton = findViewById(R.id.showButton);
        Button whatsappButton = findViewById(R.id.sendWhatsAppButton);
        Button importButton = findViewById(R.id.importButton);
        buttonBackToMain.setOnClickListener(v -> {
            finish();
        });
        // Configuramos la acción del botón de cálculo
        calculateButton.setOnClickListener(v -> calculateResult());
        calculateButtonDarcy.setOnClickListener(v -> calculateDarcy());
        limpiarButton.setOnClickListener(v -> limpiarAccion());
        guardarButton.setOnClickListener(v -> guardarAccion());
        mostrarButton.setOnClickListener(v -> exportarDatosAExcel());
        whatsappButton.setOnClickListener(v -> compartirArchivoWhatsApp());
        importButton.setOnClickListener(v -> seleccionarArchivoExcel());
        saveFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                saveFileToUri(uri);
            }
        });

    }

    //Funcion de guardado de acciones en caso de querer calcular de uno en uno las tuberías.
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

    //Función de exportacion de datos a excel, se guarda en el dispositivo.
    public void exportarDatosAExcel() {
        if (tuberiasGuardadas.isEmpty()) {
            Toast.makeText(this, "No hay datos guardados para exportar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, "ResultadosTuberias_" + System.currentTimeMillis() + ".xlsx");
        saveFileLauncher.launch(intent);
    }

    //Funcion de creación de archivo de excel, con obtención de parámetros.
    private void saveFileToUri(Uri uri) {
        try {
            // Crea un nuevo workbook para garantizar que no se reutilicen datos antiguos
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(sheetName);

            // Encabezados
            String[] headers = {"Tubería", "Calado", "Área", "Perímetro", "Velocidad", "Caudal", "Espejo de Agua", "Froude", "Porcentaje de llenado", "Factor de Fricción"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Escribe los datos actualizados en la hoja de cálculo
            for (int i = 0; i < tuberiasGuardadas.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue("Tubería #" + (i + 1));
                for (int j = 0; j < tuberiasGuardadas.get(i).length; j++) {
                    row.createCell(j + 1).setCellValue(tuberiasGuardadas.get(i)[j]);
                }
            }

            // Guarda el archivo
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                workbook.write(outputStream);
                workbook.close();
                Toast.makeText(this, "Archivo Excel guardado correctamente.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el archivo Excel.", Toast.LENGTH_SHORT).show();
        }
    }

    //Función de envio de archivo de whatsApp, se guarda como nombre de archivo usando año, mes, dia y hora
    //para evitar duplicaciones de nombre.
    public void compartirArchivoWhatsApp() {
        // Crea el archivo primero y guarda el URI
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(getExternalFilesDir(null), "ResultadosTuberias_" + timestamp + ".xlsx");
        Uri fileUri = Uri.fromFile(file);

        // Llamamos al método para guardar el archivo
        saveFileToUri(fileUri);

        // Verificar si el archivo se creó correctamente
        if (!file.exists()) {
            Toast.makeText(this, "Primero exporta el archivo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear y configurar el Intent para compartir
        Uri fileUriShare = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_STREAM, fileUriShare);
        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp no está instalado.", Toast.LENGTH_SHORT).show();
        }
    }

    //Funcion para cargar un archivo de excel, se utiliza al momento de cargar un archivo y generar todos los resultados
    //Esta función solo calcula mediante Darcy.
    public void seleccionarArchivoExcel() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo Excel"), REQUEST_CODE_EXCEL);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el selector de archivos.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EXCEL && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                // Obtener la ruta del archivo desde la URI
                String filePath = getFilePathFromUri(uri);
                if (filePath != null) {
                    procesarArchivoExcel(filePath);
                } else {
                    Toast.makeText(this, "No se pudo obtener la ruta del archivo.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar el archivo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columnIndex >= 0) {
                    String displayName = cursor.getString(columnIndex);
                    File file = new File(getExternalFilesDir(null), displayName);
                    try (InputStream inputStream = getContentResolver().openInputStream(uri);
                         FileOutputStream outputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        filePath = file.getAbsolutePath();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }
    //Función de carga de archivo excel a iteracion de Darcy, para calcular varias tuberías
    //al mismo tiempo, luego del cálculo se envia mediante WhatsApp.
    public void procesarArchivoExcel(String filePath) {
        try {
            // Abrir el archivo Excel
            FileInputStream file = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0); // Leer la primera hoja
            ArrayList<double[]> resultados = new ArrayList<>();

            // Iterar sobre las filas del Excel
            for (Row row : sheet) {
                // Leer los valores de las columnas A a D con formato correcto
                double qd = Double.parseDouble(row.getCell(0).toString().replace(",", "."));
                double dd = Double.parseDouble(row.getCell(1).toString().replace(",", "."));
                double n = Double.parseDouble(row.getCell(2).toString().replace(",", "."));
                double s = Double.parseDouble(row.getCell(3).toString().replace(",", "."));


                // Asignar los valores a las entradas de calculateDarcy
                inputQd.setText(String.valueOf(qd));
                inputDd.setText(String.valueOf(dd));
                inputN.setText(String.valueOf(n));
                inputS.setText(String.valueOf(s));

                // Ejecutar los métodos existentes
                calculateDarcy();
                guardarAccion();
                //showResult(); // Generar resultados en el TextView de salida

                // Obtener los resultados del TextView
                double[] filaResultado = {
                        y, areaCalc, perimetroCalc, velocidad, qCalc,
                        T, Fr, porcentajeLlenado, factorFriccion
                };
                resultados.add(filaResultado);
            }

            // Exportar resultados a un archivo Excel
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File archivoExcel = new File(getExternalFilesDir(null), "ResultadosTuberias_" + timestamp + ".xlsx");
            guardarResultadosExcel(resultados, archivoExcel);

            // Compartir el archivo Excel directamente
            compartirArchivoExcel(archivoExcel);

            // Cerrar el archivo Excel
            workbook.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarResultadosExcel(ArrayList<double[]> resultados, File archivoExcel) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Resultados");

            // Crear encabezados en la primera fila
            Row headerRow = sheet.createRow(0);
            String[] encabezados = {"Calado (cm)", "Área (cm²)", "Perímetro (cm)", "Velocidad (m/s)", "Caudal (l/s)",
                    "Espejo de agua (m)", "Froude", "Porcentaje de llenado (%)", "Factor de fricción"};
            for (int i = 0; i < encabezados.length; i++) {
                headerRow.createCell(i).setCellValue(encabezados[i]);
            }

            // Llenar los resultados
            for (int i = 0; i < resultados.size(); i++) {
                Row row = sheet.createRow(i + 1);
                double[] filaResultado = resultados.get(i);
                for (int j = 0; j < filaResultado.length; j++) {
                    row.createCell(j).setCellValue(filaResultado[j]);
                }
            }

            // Guardar el archivo Excel
            FileOutputStream fileOut = new FileOutputStream(archivoExcel);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            Toast.makeText(this, "Archivo Excel guardado en: " + archivoExcel.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el archivo Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void compartirArchivoExcel(File archivoExcel) {
        if (archivoExcel == null || !archivoExcel.exists()) {
            Toast.makeText(this, "Primero exporta el archivo Excel.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Obtener el URI del archivo utilizando FileProvider
            Uri fileUriShare = FileProvider.getUriForFile(this, getPackageName() + ".provider", archivoExcel);

            // Crear el Intent para compartir
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.putExtra(Intent.EXTRA_STREAM, fileUriShare);

            // Iniciar la actividad para compartir
            startActivity(Intent.createChooser(intent, "Compartir archivo Excel"));
        } catch (Exception e) {
            Toast.makeText(this, "Error al compartir el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void limpiarAccion() {
        inputQd.setText("");
        inputDd.setText("");
        //inputN.setText("");
        inputS.setText("");
    }

    //Funcion que muestra los resultados en la sección de texto de abajo, dandoles formato.
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

    //Función de Iteración del cálculo de Manning, parámetro de entada el calado.
    public void repetirCalculo(double yCalculo) {
        //Los valores de funciones se utilizan en radianes, y las unidades se calculan en metros.
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

    //Función de calculo iterativo usando Manning.
    public void calculateResult() {
        try {
            // Obtener y convertir los valores de entrada
            qd = Double.parseDouble(inputQd.getText().toString());
            dd = Double.parseDouble(inputDd.getText().toString());
            n = Double.parseDouble(inputN.getText().toString());
            s = Double.parseDouble(inputS.getText().toString());

            radio = (dd / 2) / 100; // Radio en metros
            //definir un calado inicial bajo
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
            //No permitir iteraciones infinitas
            if (iteraciones >= iteracionesMax) {
                throw new ArithmeticException("No se encontró solución dentro del máximo de iteraciones.");
            }
            //definir un factor de friccion de 0 para evitar errores NA
            factorFriccion = 0;
            showResult();
            //Mostrar mensajes de error en caso de datos ingresados inválidos.
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa todos los valores correctamente.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Función para realizar el cálculo iterativo de darcy.
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
            //Función iterativa que verifica tolerancia y exceso de iteraciones
            while (Math.abs(qCalc - qd) > tolerancia && iteraciones < iteracionesMax) {
                repetirCalculoYDarcy(y); // Calcula el caudal qCalc para el valor actual de y

                //Función que asegura una tolerancia baja, realiza divisiones del incremento de ser necesario
                if (qCalc < qd) {
                    y += incremento; // Incrementa y si qCalc es menor que qd
                } else {
                    incremento /= 2; // Reduce el incremento para mayor precisión
                    y -= incremento; // Retrocede si excedemos el caudal objetivo
                }
                //Seleccionar el valor mínimo en caso de errores.
                y = Math.min(y, dd);
                iteraciones++;
            }
            //Llamar a la función de muestra de resultados
            showResult();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa todos los valores correctamente.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //Función de cálculo del factor de fricción, recibe como parámetro el Dh.

    public void calcularFactorDeFriccion(double Dh) {
        double U = 1.5e-6; // Viscosidad cinemática en m^2/s, utiliza flotantes.
        double f = 0.02; // Factor de fricción inicial
        //Definicion de tolerancia e iteraciones máximas
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
            //Incremento de iteraciones
            iteraciones++;
        }
        // Si se alcanzó el límite de iteraciones sin converger se muestra un mensaje de error
        Log.w("Darcy", "El cálculo de f no convergió después de " + iteracionesMax + " iteraciones");
        factorFriccion = Double.NaN;
    }

    //Función de iteacion del cálculo de Darcy
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

    }


    public void showResult() {
        y = y * 100;// (cm)
        areaCalc = areaCalc * 100 * 100;
        perimetroCalc = perimetroCalc * 100;
        String tipodeFlujo = "";
        dd = Double.parseDouble(inputDd.getText().toString());
        porcentajeLlenado = (y / D)*100;
        if (Fr > 1) {
            tipodeFlujo = "Flujo supercrítico";
        }
        if (Fr == 1) {
            tipodeFlujo = "Flujo en transición";
        }
        if (Fr < 1) {
            tipodeFlujo = "Flujo subcrítico";
        }
        //creación de matrices de números y correspondientes descripciones y unidades para muestra de resultados.
        double[] resultados = {y, areaCalc, perimetroCalc, velocidad, qCalc, T, Fr, porcentajeLlenado, factorFriccion};
        String[] descripciones = {"Calado", "Área", "Perímetro", "Velocidad", "Caudal", "Espejo de agua", "Froude", "y/D", "Factor de Fricción"};
        String[] unidades = {"(cm)", "(cm^2)", "(cm)", "(m/s)", "(l/s)", "(m)", "  " + tipodeFlujo, "%", "-"};
        mostrarResultados(resultados, descripciones, unidades);
    }
}