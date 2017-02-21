package com.parse.starter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    static ArrayList<Bitmap> bitmapArray;
    TextView welcomeText;
    TextView noImageText;
    ListView friendList;
    LinearLayout imageLinearLayout;
    ArrayList<String> friendArray;
    ArrayAdapter<String> arrayAdapter;
    String username;

    int imageSize;
    int imageMargin;
    int imageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageSize = (int) convertDpToPixel(50,getApplicationContext());
        imageMargin = (int) convertDpToPixel(5,getApplicationContext());
        imageCount = ParseUser.getCurrentUser().getInt("imageSize");

        welcomeText = (TextView) findViewById(R.id.welcomeText);
        friendList = (ListView) findViewById(R.id.friendList);
        imageLinearLayout = (LinearLayout) findViewById(R.id.image_linear_layout);
        noImageText = (TextView) findViewById(R.id.no_image_text);
        username = ParseUser.getCurrentUser().getUsername();

        friendArray = new ArrayList<String>();
        bitmapArray = new ArrayList<Bitmap>();
        initializeBitmapArray();
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,friendArray);

        friendList.setAdapter(arrayAdapter);
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Main2Activity.this, FriendActivity.class);
                intent.putExtra("friendName", friendArray.get(i));
                startActivity(intent);
            }
        });


        welcomeText.setText("Welcome, user " + username + "!");

        getFriendData();
        getImageData();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_logout){
            ParseUser.logOut();
            finish();

            return true;
        }
        else if(item.getItemId() == R.id.add_image) {

            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else {
                getPhoto();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getPhoto();
            }
        }
    }

    //After selecting image from phone
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            Toast.makeText(getApplicationContext(),"Adding the photo...", Toast.LENGTH_SHORT).show();

            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                ParseFile file = new ParseFile("image.png", byteArray);
                ParseObject object = new ParseObject("Image");
                object.put("username", username);
                object.put("image", file);
                object.put("index", imageCount);

                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null) {
                            if(bitmapArray.size() == 0) {
                                hideNoImageText();
                            }
                            imageCount++;
                            ParseUser.getCurrentUser().put("imageSize", imageCount);
                            try {
                                ParseUser.getCurrentUser().save();
                                bitmapArray.add(bitmap);
                                showImage();
                                Toast.makeText(getApplicationContext(),"Photo added successfully!", Toast.LENGTH_SHORT).show();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                                imageCount = ParseUser.getCurrentUser().getInt("imageSize");
                                Toast.makeText(getApplicationContext(),"Unable to update photo count. Upload Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }catch(Exception e) {
                Toast.makeText(getApplicationContext(),"Failed: The photo has to be less than 10MB", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }

    //Get image data while loading
    private void getImageData() {
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery("Image");
        imageQuery.whereEqualTo("username", username);
        //imageQuery.orderByDescending("createdAt");
        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if(objects.size() > 0 && e == null){
                    hideNoImageText();
                    for(ParseObject object : objects) {
                        final int imageIndex = object.getInt("index");
                        ParseFile image = (ParseFile) object.get("image");
                        image.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    bitmapArray.set(imageIndex, bmp);
                                    showImage();
                                } else {
                                    Log.i("test", "There was a problem downloading the data.");
                                }
                            }
                        });
                    }
                }else {
                    showNoImageText();
                }
            }
        });
    }

    private void showImage(){
        LinearLayout imageRow = new LinearLayout(getApplicationContext());

        imageLinearLayout.removeAllViews();
        for(int i = 0; i < bitmapArray.size(); i++) {
            final int imageIndex = bitmapArray.size() - 1 - i;

            if(i % 3 == 0) {
                imageRow = new LinearLayout(getApplicationContext());
                imageRow.setOrientation(LinearLayout.HORIZONTAL);
                imageRow.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                imageLinearLayout.addView(imageRow);
            }

            ImageView image = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageSize, imageSize);
            params.setMargins(imageMargin, imageMargin, imageMargin, imageMargin);
            image.setLayoutParams(params);
            image.setBackgroundColor(Color.parseColor("#eeeeee"));
            image.setImageBitmap(bitmapArray.get(imageIndex));
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openImage(imageIndex);
                }
            });
            image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(Main2Activity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Do you wish to delete this photo?")
                            .setMessage("The deleted photo will not be able to retrieve again.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteImageOnServer(imageIndex);
                                }
                            }).setNegativeButton("No", null)
                            .show();
                    return true;
                }
            });
            imageRow.addView(image);
        }
    }

    private void getFriendData() {
        ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();
        friendQuery.whereNotEqualTo("username", username);
        friendQuery.addAscendingOrder("username");

        friendQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(objects.size() > 0 && e == null){
                    for(ParseUser user : objects) {
                        friendArray.add(user.getUsername());
                    }
                    arrayAdapter.notifyDataSetChanged();
                }else{
                    welcomeText.setText("Welcome, user " + username + "!\nYou have no friend. Add new friends and share your photo with them!");
                }
            }
        });
    }

    private void openImage(int index) {
        Intent intent = new Intent(this,OpenImageActivity.class);
        intent.putExtra("imageIndex", index);
        intent.putExtra("username", username);
        intent.putExtra("from", "user");
        startActivity(intent);
    }

    private void deleteImageOnServer(final int index) {
        //Delete image and reset counter in User class
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery("Image");
        imageQuery.whereEqualTo("username", username);
        imageQuery.whereEqualTo("index", index);
        imageQuery.setLimit(1);
        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size() > 0 && e == null) {
                    for(ParseObject object : objects) {
                        object.deleteInBackground();

                        imageCount--;
                        ParseUser.getCurrentUser().put("imageSize", imageCount);
                        try {
                            ParseUser.getCurrentUser().save();
                            bitmapArray.remove(index);
                            showImage();
                            Toast.makeText(getApplicationContext(),"The photo is deleted.", Toast.LENGTH_SHORT).show();

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                            imageCount = ParseUser.getCurrentUser().getInt("imageSize");
                            Toast.makeText(getApplicationContext(), "Cannot access User class on parse server. Delete photo failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Photo not found in parse server.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Reset image order
        imageQuery = ParseQuery.getQuery("Image");
        imageQuery.whereEqualTo("username", username);
        imageQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size() > 0 && e == null) {
                    for(ParseObject object : objects) {
                        if(object.getInt("index") > index) {
                            int newIndex = object.getInt("index") - 1;
                            object.put("index", newIndex);
                            try {
                                object.save();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                                Toast.makeText(getApplicationContext(),"Warning: Unable to update photo index. Please check the server.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });
    }

    private void initializeBitmapArray(){
        for(int i = 0; i < imageCount; i++) {
            bitmapArray.add(null);
        }
    }

    private void hideNoImageText(){
        noImageText.setText("");
        noImageText.setVisibility(View.INVISIBLE);
    }

    private void showNoImageText(){
        noImageText.setText("You have no photo. Click Add Photo to upload your first photo.");
        noImageText.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.friendlist_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void getPhoto () {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"Please click the Logout button from the right top menu to logout.", Toast.LENGTH_SHORT).show();
    }

}
