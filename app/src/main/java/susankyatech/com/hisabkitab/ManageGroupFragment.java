package susankyatech.com.hisabkitab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import static android.app.Activity.RESULT_OK;

public class ManageGroupFragment extends Fragment {

    private static final int GALLERY_PICK = 1;

    private CircleImageView displayGroupImage;
    private TextView displayUserName_TV, displayGroupName_TV, displayGroupToken_TV;
    private EditText changeGroupName_ET, changeMaxGroupMembers_ET;
    private Button changeGroupName_Btn, changeGroupImage_Btn, changeGroupMaxMembers_Btn, updateGroupMembersBtn, displayGroupIdToken_Btn;
    private ProgressDialog loadingBar;

    private DatabaseReference userReference, groupReference;
    private StorageReference groupImageReference;

    String currentUserId, currentGroupId, currentUserName, downloadUrl;

    public ManageGroupFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_manage_group, container, false);

        displayGroupImage = view.findViewById(R.id.group_manage_display_group_image);
        displayUserName_TV = view.findViewById(R.id.group_manage_display_user_name_et);
        displayGroupName_TV = view.findViewById(R.id.group_manage_display_group_name_tv);
        changeGroupName_Btn = view.findViewById(R.id.group_manage_change_group_name_btn);
        changeGroupImage_Btn = view.findViewById(R.id.group_manage_change_group_image_btn);
        changeGroupMaxMembers_Btn = view.findViewById(R.id.group_manage_change_group_max_members_btn);
        updateGroupMembersBtn = view.findViewById(R.id.group_manage_update_group_members);
        displayGroupIdToken_Btn = view.findViewById(R.id.group_manage_show_group_id_token_btn);

        loadingBar = new ProgressDialog(getContext());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

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

                if (dataSnapshot.exists()) {
                    currentGroupId = dataSnapshot.child("group_id").getValue().toString();
                    currentUserName = dataSnapshot.child("user_name").getValue().toString();
                    displayUserName_TV.setText(currentUserName);

                    groupReference = groupReference.child(currentGroupId);
                    groupReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String currentGroupName = dataSnapshot.child("name").getValue().toString();
                            String currentGroupImageUrl = dataSnapshot.child("group_image").getValue().toString();
                            displayGroupName_TV.setText(currentGroupName);
                            Picasso.get().load(currentGroupImageUrl).placeholder(R.drawable.ic_photo_camera).into(displayGroupImage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        changeGroupName_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                        .title("Change Group Name")
                        .customView(R.layout.manage_group_change_group_name_dialog_layout, true)
                        .positiveText("Change")
                        .negativeText("Cancel")
                        .positiveColor(getResources().getColor(R.color.green))
                        .negativeColor(getResources().getColor(R.color.red))
                        .show();

                changeGroupName_ET = materialDialog.getCustomView().findViewById(R.id.manage_group_change_group_name_dialog_et);

                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String currentGroupName = changeGroupName_ET.getText().toString();

                        groupReference.child("name").setValue(currentGroupName)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            groupReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    String newGroupName = dataSnapshot.child("name").getValue().toString();
                                                    changeGroupName_ET.setText(newGroupName);
                                                    Toast.makeText(getContext(), "Group name changed successfully!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                    }
                                });

                        materialDialog.dismiss();
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

        changeGroupImage_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        changeGroupMaxMembers_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                        .title("Change Max Members")
                        .customView(R.layout.manage_group_change_max_members_dialog_layout, true)
                        .positiveText("Change")
                        .negativeText("Cancel")
                        .positiveColor(getResources().getColor(R.color.green))
                        .negativeColor(getResources().getColor(R.color.red))
                        .show();

                changeMaxGroupMembers_ET = materialDialog.getCustomView().findViewById(R.id.manage_group_change_max_members_dialog_et);

                materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String currentGroupMaxMembers = changeMaxGroupMembers_ET.getText().toString();

                        groupReference.child("max_members").setValue(currentGroupMaxMembers)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            groupReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    String newGroupMaxMembers = dataSnapshot.child("max_members").getValue().toString();
                                                    changeMaxGroupMembers_ET.setText(newGroupMaxMembers);
                                                    Toast.makeText(getActivity(), "Max members changed successfully!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                    }
                                });

                        materialDialog.dismiss();
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
                Fragment fragment = new UpdateGroupMembersFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.content_main_frame, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        displayGroupIdToken_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                        .title("Group Token")
                        .customView(R.layout.manage_group_display_group_token_dialog_layout, true)
                        .positiveText("Dismiss")
                        .positiveColor(getResources().getColor(R.color.green))
                        .show();

                 displayGroupToken_TV = materialDialog.getCustomView().findViewById(R.id.manage_group_display_group_token_dialog_tv);

                 groupReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String token = dataSnapshot.child("groupToken").getValue().toString();
                        displayGroupToken_TV.setText(token);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

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
            loadingBar.setCanceledOnTouchOutside(true);

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
                                    downloadUrl = task.getResult().toString();
                                    groupReference.child("group_image").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.ic_photo_camera).into(displayGroupImage);
                                                    }
                                                }
                                            });

                                }

                            }
                        });

                        loadingBar.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }
}
