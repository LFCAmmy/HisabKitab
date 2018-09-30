package susankyatech.com.hisabkitab.Fragments;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import susankyatech.com.hisabkitab.Activity.AdminMain;
import susankyatech.com.hisabkitab.R;

import static android.app.Activity.RESULT_OK;

public class CreateGroup extends Fragment {

    private final static int GALLERY_PICK = 1;
    private static final String string = "0123456789";
    private static final Random random = new Random();

    private CircleImageView groupImage;
    private EditText groupNameET, groupMaxMembersET;
    private Button createGroupBtn;

    private ProgressDialog loadingBar;
    private Uri imageUri;

    private DatabaseReference userReference, groupReference, totalExpenditureRef;
    private StorageReference groupImageStoreReference;

    private String currentUserId, userName, groupToken, downloadGroupImageUrl = "none";

    public CreateGroup() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        groupImage = view.findViewById(R.id.group_image);
        groupNameET = view.findViewById(R.id.group_name_et);
        groupMaxMembersET = view.findViewById(R.id.group_max_members_et);
        createGroupBtn = view.findViewById(R.id.create_group_btn);

        loadingBar = new ProgressDialog(getContext());

        init();

        return view;
    }

    private void init() {

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        groupReference = FirebaseDatabase.getInstance().getReference().child("Group");
        totalExpenditureRef = FirebaseDatabase.getInstance().getReference().child("Total_Expenditures");

        groupImageStoreReference = FirebaseStorage.getInstance().getReference().child("Group Images");


        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userName = dataSnapshot.child("user_name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(groupNameET.getText().toString())) {
                    groupNameET.setError("Please enter a new group name!");
                    groupNameET.requestFocus();
                } else if (TextUtils.isEmpty(groupMaxMembersET.getText().toString())) {
                    groupMaxMembersET.setError("Please enter max members!");
                    groupMaxMembersET.requestFocus();
                } else {
                    loadingBar.setTitle("Creating Group");
                    loadingBar.setMessage("Please wait while we are creating a group for you.");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    String name = groupNameET.getText().toString();
                    Integer max = Integer.valueOf(groupMaxMembersET.getText().toString());

                    groupToken = generateGroupToken();

                    Calendar callForDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                    String date = currentDate.format(callForDate.getTime());

                    Calendar calForTime = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                    String time = currentTime.format(calForTime.getTime());

                    final HashMap groupMap = new HashMap();
                    groupMap.put("group_name", name);
                    groupMap.put("max_members",max);
                    groupMap.put("group_image", downloadGroupImageUrl);
                    groupMap.put("group_created_date", date);
                    groupMap.put("group_created_time", time);
                    groupMap.put("group_token", groupToken);
                   groupReference.child(groupToken).updateChildren(groupMap).addOnCompleteListener(new OnCompleteListener() {
                       @Override
                       public void onComplete(@NonNull Task task) {

                           if (task.isSuccessful()) {
                               HashMap membersMap = new HashMap();
                               membersMap.put("user_id", currentUserId);
                               membersMap.put("name", userName);
                               membersMap.put("role","admin");
                               groupReference.child(groupToken).child("members").child(currentUserId).updateChildren(membersMap)
                                       .addOnCompleteListener(new OnCompleteListener() {
                                           @Override
                                           public void onComplete(@NonNull Task task) {

                                               if (task.isSuccessful()) {
                                                   HashMap userMap = new HashMap();
                                                   userMap.put("group_id", groupToken);
                                                   userReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                                       @Override
                                                       public void onComplete(@NonNull Task task) {

                                                           if (task.isSuccessful()) {
                                                               userReference.child("group_id").setValue(groupToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<Void> task) {

                                                                       if (task.isSuccessful()) {
                                                                           HashMap totalExp = new HashMap();
                                                                           totalExp.put("user_id", currentUserId);
                                                                           totalExp.put("name", userName);
                                                                           totalExp.put("total_amount",0);
                                                                           totalExpenditureRef.child(groupToken).child(currentUserId).updateChildren(totalExp)
                                                                                   .addOnCompleteListener(new OnCompleteListener() {
                                                                                       @Override
                                                                                       public void onComplete(@NonNull Task task) {
                                                                                           if (task.isSuccessful()){
                                                                                               if (imageUri != null) {
                                                                                                   final StorageReference imagePath = groupImageStoreReference.child(groupToken + ".jpg");
                                                                                                   imagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                                       @Override
                                                                                                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshot) {

                                                                                                           if (taskSnapshot.isSuccessful()) {
                                                                                                               imagePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                                                                   @Override
                                                                                                                   public void onComplete(@NonNull Task<Uri> task) {

                                                                                                                       downloadGroupImageUrl = task.getResult().toString();
                                                                                                                       groupReference.child(groupToken).child("group_image")
                                                                                                                               .setValue(downloadGroupImageUrl)
                                                                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                   @Override
                                                                                                                                   public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                   }
                                                                                                                               });
                                                                                                                   }
                                                                                                               });
                                                                                                           } else {
                                                                                                               Toast.makeText(getContext(), "Error Occurred, please try again!", Toast.LENGTH_SHORT).show();
                                                                                                           }
                                                                                                       }
                                                                                                   });
                                                                                               }

                                                                                               Intent intent = new Intent(getActivity(), AdminMain.class);
                                                                                               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                               startActivity(intent);

                                                                                               loadingBar.dismiss();
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
                                           }
                                       });
                           } else{
                               Toast.makeText(getContext(), "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
                }
            }
        });
    }

    public String generateGroupToken() {

        StringBuilder token = new StringBuilder(6);
        for (int i=0; i<6; i++) {
            token.append(string.charAt(random.nextInt(string.length())));
        }
        return token.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();

            Picasso.get().load(imageUri).into(groupImage);
        }
    }
}
