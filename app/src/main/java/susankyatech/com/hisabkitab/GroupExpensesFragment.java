package susankyatech.com.hisabkitab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class GroupExpensesFragment extends Fragment {

    private RecyclerView recyclerView;

    private Query userReference;

    private String currentUserId;

    public GroupExpensesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_group_expenses, container, false);

        Button clearDueBtn = mView.findViewById(R.id.group_expenses_clear_due_btn);
        recyclerView = mView.findViewById(R.id.group_expenses_recycler_view);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        displayUserList();

        clearDueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Due cleared!", Toast.LENGTH_SHORT).show();
            }
        });

        return mView;
    }

//    private void displayUserList() {
//
//
//        FirebaseRecyclerAdapter<UserDataModel, UserListHolder> adapter = new FirebaseRecyclerAdapter<UserDataModel, UserListHolder>
//                (UserDataModel.class, R.layout.group_expenses_recycler_view_layout, UserListHolder.class, userReference) {
//
//            @Override
//            protected void populateViewHolder(UserListHolder viewHolder, UserDataModel model, int position) {
//
//                Log.d("TAG", "populateViewHolder: " + model.toString());
//                viewHolder.setUser_name(model.getUser_name());
//            }
//        };
//
//        recyclerView.setAdapter(adapter);
//    }

    public static class UserListHolder extends RecyclerView.ViewHolder {

        View view;

        public UserListHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setUser_name(String user_name) {
            Log.d("qwerty", "masd" + user_name);

            TextView name = view.findViewById(R.id.group_expenses_user_name_tv);
            name.setText(user_name);

        }
    }
}