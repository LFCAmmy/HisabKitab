package com.susankya.abhinav.myrealmapplication.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.susankya.abhinav.myrealmapplication.activity.AdminActivity;
import com.susankya.abhinav.myrealmapplication.R;

import java.util.HashMap;

public class QRCode extends Fragment {

    private DatabaseReference userReference, groupReference, expendituresReference;

    private CodeScanner mCodeScanner;

    private String currentUserId, userName;

    public QRCode() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrcode, container, false);

        CodeScannerView scannerView = view.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(getActivity(), scannerView);

        showCamera();

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCodeScanner.startPreview();
            }
        });

        return view;
    }

    private void showCamera() {

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
                        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
                        expendituresReference = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");

                        userReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    userName = dataSnapshot.child("user_name").getValue().toString();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        userReference.child("group_id").setValue(result.getText()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    HashMap memberMap = new HashMap();
                                    memberMap.put("user_id", currentUserId);
                                    memberMap.put("name", userName);
                                    memberMap.put("role", "member");
                                    groupReference.child(result.getText()).child("members").child(currentUserId).updateChildren(memberMap)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if (task.isSuccessful()) {
                                                        HashMap expendituresMap = new HashMap();
                                                        expendituresMap.put("name", userName);
                                                        expendituresMap.put("total_amount", 0);
                                                        expendituresMap.put("user_id", currentUserId);
                                                        expendituresReference.child(result.getText()).child(currentUserId).updateChildren(expendituresMap)
                                                                .addOnCompleteListener(new OnCompleteListener() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task task) {

                                                                        if (task.isSuccessful()) {
                                                                            Intent intent = new Intent(getActivity(), AdminActivity.class);
                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}