package susankyatech.com.hisabkitab.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import susankyatech.com.hisabkitab.Activity.MemberMain;
import susankyatech.com.hisabkitab.R;

public class GettingStarted extends Fragment {

    private EditText groupCodeET;
    private Button joinGroupBtn;
    private TextView createGroupTV;

    private DatabaseReference userReference, groupReference;

    private String currentUserId, userName;
    private int maxMembersAllowed;
    private long groupMembersCount;

    public GettingStarted() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_getting_started, container, false);

        groupCodeET = view.findViewById(R.id.group_code_et);
        joinGroupBtn = view.findViewById(R.id.join_group_btn);
        createGroupTV = view.findViewById(R.id.create_group_tv);

        init();

        return view;
    }

    private void init() {

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userName = dataSnapshot.child("user_name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        joinGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String code = groupCodeET.getText().toString();

                if (TextUtils.isEmpty(code)){
                    groupCodeET.setError("Please enter group code to join!");
                    groupCodeET.requestFocus();
                }
                else {
                    groupReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(code)) {

                                groupMembersCount = dataSnapshot.child(code).child("members").getChildrenCount();
                                maxMembersAllowed = Integer.valueOf(dataSnapshot.child(code).child("max_members").getValue().toString());

                                if (maxMembersAllowed >= groupMembersCount) {

                                    HashMap memberMap = new HashMap();
                                    memberMap.put("name", userName);
                                    memberMap.put("role","member");
                                    memberMap.put("status", "active");
                                    groupReference.child(code).child("members").child(currentUserId).updateChildren(memberMap)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {

                                                    if (task.isSuccessful()) {
                                                        userReference.child("group_id").setValue(code).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {
                                                                    Intent intent = new Intent(getActivity(), MemberMain.class);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                }
                                else {
                                    Toast.makeText(getActivity(), "Sorry, this group has already its maximum members joined in the group!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(getActivity(), "No group found. Please enter a valid group code to join!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        createGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new CreateGroup();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.content_welcome_frame, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}
