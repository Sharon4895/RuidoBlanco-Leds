package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText etUsuario, etPassword;
    private AdminSQLiteOpenHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String user = etUsuario.getText().toString();
            String pass = etPassword.getText().toString();

            if (!user.isEmpty() && !pass.isEmpty()) {
                try {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues reg = new ContentValues();
                    reg.put("alias", user);
                    reg.put("password", pass);
                    // Usamos insertWithOnConflict o replace para evitar errores de clave primaria
                    db.insertWithOnConflict("usuarios", null, reg, SQLiteDatabase.CONFLICT_REPLACE);
                    db.close();

                    // Pasamos el usuario a la siguiente actividad
                    Intent intent = new Intent(this, ControlActivity.class);
                    intent.putExtra("USUARIO_ALIAS", user);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Ingrese datos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}