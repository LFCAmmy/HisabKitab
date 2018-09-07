package susankyatech.com.hisabkitab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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

import java.security.SecureRandom;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class CreateGroupFragment extends Fragment {

    @BindView(R.id.group_image)
    CircleImageView groupImage;
    @BindView(R.id.group_name)
    EditText groupName;
    @BindView(R.id.max_no_of_member)
    EditText maxMember;
    @BindView(R.id.join_automatically)
    CheckBox autoJoin;
    @BindView(R.id.create_group_btn)
    Button createGroup;

    private ProgressDialog loadingBar;
    private String joinAuto, userName, currentUserId, token, downloadUrl="";
    final static int gallery_pick = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference groupRef, userRef;
    private StorageReference groupImageRef;


    public CreateGroupFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        ButterKnife.bind(this,view);

        init();

        return view;
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        groupRef = FirebaseDatabase.getInstance().getReference().child("Group");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");

        loadingBar = new ProgressDialog(getContext());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userName = dataSnapshot.child("user_name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
        RandomString tickets = new RandomString(23, new SecureRandom(), easy);

        token = tickets.nextString();

        if (autoJoin.isChecked()){
            joinAuto = "true";
        } else {
            joinAuto = "false";
        }

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallery_pick);
            }
        });

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = groupName.getText().toString();
                String max = maxMember.getText().toString();


                if (downloadUrl.equals("")) {
                    downloadUrl = "none";
                }else if (TextUtils.isEmpty(name)){
                    groupName.setError("Enter Group Name");
                    groupName.requestFocus();
                } else if (TextUtils.isEmpty(name)){
                    maxMember.setError("Enter Group Name");
                    maxMember.requestFocus();
                } else {
                    HashMap groupMap = new HashMap();
                    groupMap.put("name",name);
                    groupMap.put("max_members",max);
                    groupMap.put("join_automatically",joinAuto);
                    groupMap.put("group_image",downloadUrl);
                    groupMap.put("token",token);
                   groupRef.child(token).updateChildren(groupMap).addOnCompleteListener(new OnCompleteListener() {
                       @Override
                       public void onComplete(@NonNull Task task) {
                           if (task.isSuccessful()){
                               HashMap memberMap = new HashMap();
                               memberMap.put("name", userName);
                               memberMap.put("role","admin");
                               groupRef.child(token).child("members").child(currentUserId).updateChildren(memberMap)
                                       .addOnCompleteListener(new OnCompleteListener() {
                                           @Override
                                           public void onComplete(@NonNull Task task) {
                                               HashMap userMap = new HashMap();
                                               userMap.put("group_id",token);
                                               userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                                   @Override
                                                   public void onComplete(@NonNull Task task) {
                                                       Intent intent = new Intent(getActivity(), MainActivity.class);
                                                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                       startActivity(intent);
                                                   }
                                               });

                                           }
                                       });

                           } else{
                               String message = task.getException().getMessage();
                               Toast.makeText(getContext(), "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == gallery_pick && data!=null && resultCode==RESULT_OK){
            loadingBar.setTitle("Profile Picture");
            loadingBar.setMessage("Please Wait, while we are uploading your profile picture");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            Uri imageUri = data.getData();

            final StorageReference filePath = groupImageRef.child(token + ".jpg");

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshot) {
                    if (taskSnapshot.isSuccessful()){
                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                downloadUrl = task.getResult().toString();
                                Picasso.get().load(downloadUrl).placeholder(R.drawable.ic_photo_camera).into(groupImage);
                            }
                        });

                        loadingBar.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Error Occured: Images upload failed", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }
}
