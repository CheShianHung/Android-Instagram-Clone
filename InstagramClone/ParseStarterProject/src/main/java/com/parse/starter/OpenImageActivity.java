package com.parse.starter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class OpenImageActivity extends AppCompatActivity {

    TextView userText;
    ImageView image;
    String username;
    String fromWhere;
    int imageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_image);

        userText = (TextView) findViewById(R.id.openImageText);
        image = (ImageView) findViewById(R.id.selectedImage);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        imageIndex = intent.getIntExtra("imageIndex", -1);
        fromWhere = intent.getStringExtra("from");

        userText.setText(username + "'s photo:");


        if (imageIndex == -1) {
            Toast.makeText(getApplicationContext(), "Warning: Index passing failed. Unable to load photo. Please return and try again.", Toast.LENGTH_SHORT).show();
        } else {
            if(fromWhere.equals("user")) {
                image.setImageBitmap(Main2Activity.bitmapArray.get(imageIndex));
            }
            else if(fromWhere.equals("friend")) {
                getBitmap();
            }
        }
    }

    private void getBitmap() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        query.whereEqualTo("username", username);
        query.whereEqualTo("index", imageIndex);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size() > 0 && e == null) {
                    ParseObject object = objects.get(0);
                    ParseFile parseImage = (ParseFile) object.get("image");
                    parseImage.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if (e == null && data != null) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                image.setImageBitmap(bmp);
                            } else {
                                Log.i("test", "There was a problem downloading the data. Please return and try again.");
                            }
                        }
                    });
                }
            }
        });
    }
}
