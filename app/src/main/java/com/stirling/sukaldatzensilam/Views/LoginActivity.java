package com.stirling.sukaldatzensilam.Views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.stirling.sukaldatzensilam.R;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;
    private SignInButton btnLoginGoogle;
    private String TAG = "Tag verificación Email";
    private FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Instanciamos las preferencias compartidas y el editor
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",
                0); // 0 - modo privad
        SharedPreferences.Editor editor = pref.edit();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
            finish();
        }

        // set the view now
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.nombreReg);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);
        btnLoginGoogle = (SignInButton ) findViewById(R.id.btn_loginGoogle);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Listener del botón registrarse
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        //Listener del botón "olvidaste tu contraseña?"
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,
                        ResetPasswordActivity.class));
            }
        });

        //Listener del botón Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                //Autenticar usuario
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this,
                                new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Si el logueo falla, enseña un mensaje al usuario
                                //Si el logueo es correcto, el auth state listener será notificado
                                //y la lógica manejará el usuario logueado en el listener
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) { //I: puesto try catch's
                                    // there was an error
                                    try {
                                        throw task.getException();
                                    }// if user enters wrong email.
                                    catch (FirebaseAuthWeakPasswordException weakPassword) {
                                        Toast.makeText(LoginActivity.this,
                                                "Introduzca password con mínimo 6 caracteres",
                                                Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onComplete: weak_password");
                                    }// if user enters wrong password.
                                    catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                        Log.d(TAG, "onComplete: malformed_email");
                                        Toast.makeText(LoginActivity.this,
                                                "Correo electrónico o contraseña incorrectos",
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.d(TAG, "onComplete: " + e.getMessage());
                                        Toast.makeText(LoginActivity.this,
                                                "Excepción: "+ e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    //todo  que no se cierre la sesión
       //                             editor.putLong("user", )
                                    Intent intent = new Intent(LoginActivity.this,
                                            MainUserActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }

        });
        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);

                //todo: Obtener correo e información y si falta algo solicitarla para meterlo a BD

            }
        });
    }
}
