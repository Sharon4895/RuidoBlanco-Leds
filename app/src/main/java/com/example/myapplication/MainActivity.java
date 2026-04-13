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
                // Guardar usuario [cite: 13]
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues reg = new ContentValues();
                reg.put("alias", user);
                reg.put("password", pass);
                db.replace("usuarios", null, reg);
                db.close();

                // Ir a la segunda pantalla
                startActivity(new Intent(this, ControlActivity.class));
            } else {
                Toast.makeText(this, "Ingrese datos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}