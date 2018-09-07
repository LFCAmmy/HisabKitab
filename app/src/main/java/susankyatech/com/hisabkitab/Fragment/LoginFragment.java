package susankyatech.com.hisabkitab.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import susankyatech.com.hisabkitab.Activity.WelcomeActivity;
import susankyatech.com.hisabkitab.R;

public class LoginFragment extends Fragment {

    private EditText userEmail, userPassword;
    private Button login;

    public LoginFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        userEmail = view.findViewById(R.id.email_field);
        userPassword = view.findViewById(R.id.password_field);
        login = view.findViewById(R.id.login_btn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();

                logInUserAccount(email, password);
            }
        });
        return view;
    }

    private void logInUserAccount(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Email field is empty!", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Password field is empty!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
