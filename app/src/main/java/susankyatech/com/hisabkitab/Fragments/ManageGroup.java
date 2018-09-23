package susankyatech.com.hisabkitab.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import susankyatech.com.hisabkitab.R;

import static android.app.Activity.RESULT_OK;

public class ManageGroup extends Fragment {

    private static final int GALLERY_PICK = 1;

    private CircleImageView displayGroupImage;
    private TextView displayUserNameTV, displayGroupNameTV, displayGroupTokenTV;
    private EditText changeGroupNameET, changeMaxGroupMembersET;
    private Button changeGroupNameBtn, changeGroupImageBtn, changeGroupMaxMembersBtn, updateGroupMembersBtn, displayGroupTokenBtn;
    private ProgressDialog loadingBar;

    private DatabaseReference userReference, groupReference;
    private StorageReference groupImageReference;

    private String currentGroupId, currentUserName, downloadGroupImageUrl;

    public ManageGroup() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_manage_group, container, false);

        displayGroupImage = view.findViewById(R.id.display_group_image);
        displayGroupNameTV = view.findViewById(R.id.group_name_tv);
        displayUserNameTV = view.findViewById(R.id.user_name_tv);
        changeGroupNameBtn = view.findViewById(R.id.change_group_name_btn);
        changeGroupImageBtn = view.findViewById(R.id.change_group_image_btn);
        changeGroupMaxMembersBtn = view.findViewById(R.id.change_max_members_btn);
        updateGroupMembersBtn = view.findViewById(R.id.update_group_members_btn);
        displayGroupTokenBtn = view.findViewById(R.id.group_token_btn);

        loadingBar = new ProgressDialog(getContext());

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupImageReference = FirebaseStorage.getInstance().getReference().child("Group Images");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");

        init();

        return view;
    }

    private void init() {

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                currentUserName = dataSnapshot.child("user_name").getValue().toString();
                displayUserNameTV.setText(currentUserName);

                groupReference = groupReference.child(currentGroupId);
                groupReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String currentGroupName = dataSnapshot.child("group_name").getValue().toString();
                        String currentGroupImageUrl = dataSnapshot.child("group_image").getValue().toString();
                        displayGroupNameTV.setText(currentGroupName);
                        Picasso.get().load(currentGroupImageUrl).placeholder(R.drawable.ic_photo_camera).into(displayGroupImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        changeGroupNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                        .title("Change Group Name")
                        .customView(R.layout.change_group_name_dialog_layout, true)
                        .positiveText("Change")
                        .negativeText("Cancel")
                        .positiveColor(getResources().getColor(R.color.green))
                        .negativeColor(getResources().getColor(R.color.red))
                        .canceledOnTouchOutside(false)
                        .show();

                changeGroupNameET = materialDialog.getCustomView().findViewById(R.id.group_name_et);

                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String currentGroupName = changeGroupNameET.getText().toString();

                        if (!TextUtils.isEmpty(currentGroupName)) {

                            groupReference.child("group_name").setValue(currentGroupName)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            groupReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    String newGroupName = dataSnapshot.child("group_name").getValue().toString();
                                                    changeGroupNameET.setText(newGroupName);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });
                            materialDialog.dismiss();
                            Toast.makeText(getActivity(), "Group name changed successfully!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            changeGroupNameET.setError("Please enter a new group name!");
                            changeGroupNameET.requestFocus();
                        }
                    }
                });

                materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        materialDialog.dismiss();
                    }
                });
            }
        });

        changeGroupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        changeGroupMaxMembersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                        .title("Change Max Members")
                        .customView(R.layout.change_max_members_dialog_layout, true)
                        .positiveText("Change")
                        .negativeText("Cancel")
                        .positiveColor(getResources().getColor(R.color.green))
                        .negativeColor(getResources().getColor(R.color.red))
                        .canceledOnTouchOutside(false)
                        .show();

                changeMaxGroupMembersET = materialDialog.getCustomView().findViewById(R.id.max_members_et);

                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String currentGroupMaxMembers = changeMaxGroupMembersET.getText().toString();

                        if (!TextUtils.isEmpty(currentGroupMaxMembers)) {

                            groupReference.child("max_members").setValue(currentGroupMaxMembers)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            groupReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    String newGroupMaxMembers = dataSnapshot.child("max_members").getValue().toString();
                                                    changeMaxGroupMembersET.setText(newGroupMaxMembers);
                                                    Toast.makeText(getActivity(), "Max members changed successfully!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });

                            materialDialog.dismiss();
                        } else {
                            changeMaxGroupMembersET.setError("Please enter new max members!");
                            changeMaxGroupMembersET.requestFocus();
                        }
                    }
                });

                materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        materialDialog.dismiss();
                    }
                });
            }
        });

        updateGroupMembersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new UpdateGroupMembers();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.content_main_frame, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        displayGroupTokenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                        .title("Group Token")
                        .customView(R.layout.display_group_token_dialog_layout, true)
                        .positiveText("Dismiss")
                        .positiveColor(getResources().getColor(R.color.green))
                        .canceledOnTouchOutside(false)
                        .show();

                 displayGroupTokenTV = materialDialog.getCustomView().findViewById(R.id.group_token_tv);

                displayGroupTokenTV.setText(currentGroupId);

                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        materialDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            loadingBar.setTitle("Group Image");
            loadingBar.setMessage("Please wait, while we are setting your group image!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);

            final Uri imageUri = data.getData();

            final StorageReference newGroupImagePath = groupImageReference.child(currentGroupId + ".jpg");

            newGroupImagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshot) {

                    if (taskSnapshot.isSuccessful()) {
                        newGroupImagePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    downloadGroupImageUrl = task.getResult().toString();
                                    groupReference.child("group_image").setValue(downloadGroupImageUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Picasso.get().load(downloadGroupImageUrl).placeholder(R.drawable.ic_photo_camera).into(displayGroupImage);
                                                    }
                                                }
                                            });

                                }

                            }
                        });

                        loadingBar.dismiss();
                    } else {
                        loadingBar.dismiss();
                        Toast.makeText(getActivity(), "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
