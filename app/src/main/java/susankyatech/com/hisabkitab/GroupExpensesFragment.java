package susankyatech.com.hisabkitab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class GroupExpensesFragment extends Fragment {

    private DatabaseReference userReference;

    private RecyclerView recyclerView;

    private String currentUserId;

    public GroupExpensesFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_expenses, container, false);

        Button clearDueBtn = view.findViewById(R.id.group_expenses_clear_due_btn);
        recyclerView = view.findViewById(R.id.group_expenses_recycler_view);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        clearDueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Due cleared!", Toast.LENGTH_SHORT).show();
            }
        });

        getAllUserName();
        return view;
    }

    private void getAllUserName() {
        Query query = FirebaseDatabase.getInstance().getReference().child("Users").limitToLast(50);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addChildEventListener(childEventListener);


        FirebaseRecyclerOptions<UserDataModel> options = new FirebaseRecyclerOptions.Builder<UserDataModel>().setQuery(query, UserDataModel.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<UserDataModel, GroupManageViewHolder>(options) {

            @NonNull
            @Override
            public GroupManageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_expenses_recycler_view_layout, viewGroup, false);
                return new GroupManageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull GroupManageViewHolder holder, int position, @NonNull UserDataModel model) {
                Log.d("ARA", "onBindViewHolder: "+model.getUser_name());
                holder.setUser_name(model.getUser_name());
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    public static class GroupManageViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public GroupManageViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUser_name(String user_name) {
            TextView displayUserName_TV = mView.findViewById(R.id.group_expenses_user_name_tv);
            displayUserName_TV.setText(user_name);
        }
    }
}


