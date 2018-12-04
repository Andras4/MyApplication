package com.example.andras.myapplication;

import android.content.DialogInterface;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = RegisterActivity.class.getSimpleName();
    EditText editTextFirstName, editTextLastName, editTextPhoneNumber;

    FirebaseAuth mAuth;

    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        editTextFirstName = findViewById(R.id.register_first_name);
        editTextLastName = findViewById(R.id.register_last_name);
        editTextPhoneNumber = findViewById(R.id.register_phone_number);
        Button mShowAlertDialogSignUp = findViewById(R.id.button_sign_up);

        mShowAlertDialogSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = editTextFirstName.getText().toString();
                String lastName = editTextLastName.getText().toString();
                String phone = editTextPhoneNumber.getText().toString();
                if(firstName.isEmpty()) {
                    editTextFirstName.setError("Please fill out this field.");
                    editTextFirstName.requestFocus();
                    return;
                }
                if(lastName.isEmpty()) {
                    editTextLastName.setError("Last name is required");
                    editTextLastName.requestFocus();
                    return;
                }

                if(phone.isEmpty()) {
                    editTextPhoneNumber.setError("Phone number is required");
                    editTextPhoneNumber.requestFocus();
                    return;
                }
                if(phone.length() < 10) {
                    editTextPhoneNumber.setError("Please enter a valid phone number");
                    editTextPhoneNumber.requestFocus();
                    return;
                }

                sendVerificationCode();

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(RegisterActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_enter_code_received, null);
                final EditText editTextCode = mView.findViewById(R.id.editTextEnterCode);
                Button buttonVerify = mView.findViewById(R.id.button_verify);
                //Button buttonCancel = mView.findViewById(R.id.button_cancel);

                mBuilder.setView(mView);

                buttonVerify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String code = editTextCode.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
                        signInWithPhoneAuthCredential(credential);
                    }
                });
                mBuilder.show();
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "Verification Successfull", Toast.LENGTH_LONG).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {
        String phone = editTextPhoneNumber.getText().toString();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.w(TAG, "onVerificationFailed", e);
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            Log.d(TAG, "onCodeSent:" + s);
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
        }
    };

    @Override
    public void onClick(View view) {

    }
}
