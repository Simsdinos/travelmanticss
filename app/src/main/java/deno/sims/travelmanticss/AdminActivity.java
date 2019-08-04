package deno.sims.travelmanticss;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AdminActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    TravelDeal deal;
    public final int PICTURE_RESULT=42;
    ImageView imageView;
    Uri uris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

            mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
            mDatabaseReference = FirebaseUtil.mDatabaseReference;
            txtTitle =  findViewById(R.id.txtTitle);
            txtDescription =  findViewById(R.id.txtDescription);
            txtPrice =  findViewById(R.id.txtPrice);
             imageView=findViewById(R.id.imageview);


            Intent intent = getIntent();
            TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
            if (deal==null) {
                deal = new TravelDeal();
            }
            this.deal = deal;
            txtTitle.setText(deal.getTitle());
            txtDescription.setText(deal.getDescription());
            txtPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());


        Button btn_image=findViewById(R.id.btn_image);
         btn_image.setOnClickListener(new View.OnClickListener() {
             
                 @Override
                     public void onClick(View view){
                         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                         intent.setType("image/jpeg");
                         intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                         startActivityForResult(intent.createChooser(intent,
                                 "Insert Picture"), PICTURE_RESULT);
                     }
         });

        }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.save_menu:
                    saveDeal();
                    Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                    clean();
                    backToList();
                    return true;
                case R.id.delete_menu:
                    deleteDeal();
                    Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                    backToList();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);

            }

        }

  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICTURE_RESULT && resultCode==RESULT_OK)
        {
            Uri imageUri=data.getData();

           final StorageReference ref = FirebaseUtil.mStoragerefence.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageName(pictureName);
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri task) {
                            deal.setImageUrl(task.toString());
                            showImage(task.toString());

                        }
                    });


                }
            });



        }
    }


    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.save_menu, menu);
            if (FirebaseUtil.isAdmin) {
                menu.findItem(R.id.delete_menu).setVisible(true);
                menu.findItem(R.id.save_menu).setVisible(true);
                enableEditTexts(true);
            }
            else {
                menu.findItem(R.id.delete_menu).setVisible(false);
                menu.findItem(R.id.save_menu).setVisible(false);
                enableEditTexts(false);
            }


            return true;
        }
        private void saveDeal() {
            deal.setTitle(txtTitle.getText().toString());
            deal.setDescription(txtDescription.getText().toString());
            deal.setPrice(txtPrice.getText().toString());
            if(deal.getId()==null) {
                mDatabaseReference.push().setValue(deal);
            }
            else {
                mDatabaseReference.child(deal.getId()).setValue(deal);
            }
        }
        private void showImage(String url)
        {
            if (url != null && url.isEmpty() == false) {
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                Picasso.with(this)
                        .load(url)
                        .resize(width,width)
                        .centerCrop()
                        .into(imageView);
            }
        }
        private void deleteDeal() {
            if (deal == null) {
                Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
                return;
            }
            mDatabaseReference.child(deal.getId()).removeValue();

        }
        private void backToList() {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
        }
        private void clean() {
            txtTitle.setText("");
            txtPrice.setText("");
            txtDescription.setText("");
            txtTitle.requestFocus();
        }
        private void enableEditTexts(boolean isEnabled) {
            txtTitle.setEnabled(isEnabled);
            txtDescription.setEnabled(isEnabled);
            txtPrice.setEnabled(isEnabled);
        }

    }


