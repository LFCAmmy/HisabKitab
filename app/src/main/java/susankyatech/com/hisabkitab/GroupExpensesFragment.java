package susankyatech.com.hisabkitab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GroupExpensesFragment extends Fragment {

    private DatabaseReference userReference;

    private String currentUserId;

    public GroupExpensesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_group_expenses, container, false);

        Button clearDueBtn = mView.findViewById(R.id.group_expenses_clear_due_btn);
        RecyclerView recyclerView = mView.findViewById(R.id.group_expenses_recycler_view);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        clearDueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Due cleared!", Toast.LENGTH_SHORT).show();
            }
        });

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<UserDataModel, UserDataViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<UserDataModel, UserDataViewHolder>
                (UserDataViewHolder.class, R.layout.manage_groups_update_group_members_recycler_view_layout, UserDataModel.class) {
            @Override
            protected void onBindViewHolder(@NonNull UserDataViewHolder holder, int position, @NonNull UserDataModel model) {

            }

            @NonNull
            @Override
            public UserDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return null;
            }
        };
    }

    public static class UserDataViewHolder extends RecyclerView.ViewHolder {


        public UserDataViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
