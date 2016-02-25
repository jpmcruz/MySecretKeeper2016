package jason.cruz.mysecretkeeper2016;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FacebookActivity extends AppCompatActivity {

    ImageView imageThumb;
    private CallbackManager callbackManager;
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "Should have been posted!",
                        Toast.LENGTH_LONG).show();

                FacebookSdk.sdkInitialize(FacebookActivity.this.getApplicationContext());

                callbackManager = CallbackManager.Factory.create();

//                LoginManager.getInstance().registerCallback(callbackManager,
//                        new FacebookCallback<LoginResult>() {
//                            @Override
//                            public void onSuccess(LoginResult loginResult) {
//                                // App code
//                                sharePhotoToFacebook();
//                                Toast.makeText(getBaseContext(), "1!",
//                                        Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onCancel() {
//                                // App code
//                            }
//
//                            @Override
//                            public void onError(FacebookException exception) {
//                                // App code
//                            }
//                        });



                callbackManager = CallbackManager.Factory.create();
                List<String> permissionNeeds = Arrays.asList("publish_actions");
                //this loginManager helps you eliminate adding a LoginButton to your UI
                loginManager = LoginManager.getInstance();
                loginManager.logInWithPublishPermissions(FacebookActivity.this, permissionNeeds);

                loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
                {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        sharePhotoToFacebook();
                        Toast.makeText(getBaseContext(), "1!",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel()
                    {
                        System.out.println("onCancel");
                        Toast.makeText(getBaseContext(), "2!",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception)
                    {
                        System.out.println("onError");
                        Toast.makeText(getBaseContext(), "3!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        imageThumb = (ImageView)findViewById(R.id.imageThumb);

    }

    private void sharePhotoToFacebook(){

        File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/MySecretKeeper2016", "fb.jpg");
        Bitmap bitmap = decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);
        imageThumb.setImageBitmap(bitmap);
      //  Bitmap image = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .setCaption("Sample test code!")
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        ShareApi.share(content, null);
        Toast.makeText(getBaseContext(), "Should have been posted!",
                Toast.LENGTH_LONG).show();

    }


    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }

        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }
}
