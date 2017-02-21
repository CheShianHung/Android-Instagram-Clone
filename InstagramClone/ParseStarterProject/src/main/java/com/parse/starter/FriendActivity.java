package com.parse.starter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends AppCompatActivity {

    static ArrayList<Bitmap> friendBitmapArray;
    String friendName;
    TextView friendText;
    TextView friendNoImageText;
    LinearLayout imageLinearLayout;
    int imageCount;
    int imageMargin;
    int imageSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        imageSize = (int) Main2Activity.convertDpToPixel(50,getApplicationContext());
        imageMargin = (int) Main2Activity.convertDpToPixel(5,getApplicationContext());

        Intent intent = getIntent();
        friendName = intent.getStringExtra("friendName");

        friendBitmapArray = new ArrayList<Bitmap>();
        friendText = (TextView) findViewById(R.id.friendText);
        friendNoImageText = (TextView) findViewById(R.id.friendNoImageText);
        imageLinearLayout = (LinearLayout) findViewById(R.id.friendImageLinearLayout);

        friendText.setText(friendName + "'s photos:");

        getImageCount();


    }

    private void getImageCount() {
        ParseQuery<ParseUser> user = ParseUser.getQuery();
        user.whereEqualTo("username", friendName);
        user.setLimit(1);

        user.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(objects.size() > 0 && e == null) {
                    ParseUser friend = objects.get(0);
                    imageCount = friend.getInt("imageSize");
                    initializeBitmapArray();
                    getImageData();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Error: Unable to retrieve the photo size. Load photo failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Get image data while loading
    private void getImageData() {
        ParseQuery<ParseObject> imageQuery = ParseQuery.getQuery("Image");
        imageQuery.whereEqualTo("username", friendName);
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
                                    friendBitmapArray.set(imageIndex, bmp);
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
        for(int i = 0; i < friendBitmapArray.size(); i++) {
            final int imageIndex = friendBitmapArray.size() - 1 - i;

            if(i % 4 == 0) {
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
            image.setImageBitmap(friendBitmapArray.get(imageIndex));
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openImage(imageIndex);
                }
            });
            imageRow.addView(image);
        }
    }

    private void openImage(int index) {
        Intent intent = new Intent(this,OpenImageActivity.class);
        intent.putExtra("imageIndex", index);
        intent.putExtra("username", friendName);
        intent.putExtra("from", "friend");
        startActivity(intent);
    }

    private void initializeBitmapArray(){
        for(int i = 0; i < imageCount; i++) {
            friendBitmapArray.add(null);
        }
    }

    private void hideNoImageText(){
        friendNoImageText.setText("");
        friendNoImageText.setVisibility(View.INVISIBLE);
    }

    private void showNoImageText(){
        friendNoImageText.setText("Your friend has no photo to show.");
        friendNoImageText.setVisibility(View.VISIBLE);
    }
}
