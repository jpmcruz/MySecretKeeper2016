package jason.cruz.mysecretkeeper2016;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class HideActivity extends AppCompatActivity {

    ImageView coverThumb, secretThumb;
    EditText et_CoverFile, et_SecretFile;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    int fileChooserCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coverThumb = (ImageView) findViewById(R.id.coverThumb);
        secretThumb = (ImageView) findViewById(R.id.secretThumb);
        et_CoverFile = (EditText) findViewById(R.id.et_CoverFile);
        et_SecretFile  = (EditText) findViewById(R.id.et_SecretFile);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWriteable = false;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // Can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // Can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                // Can't read or write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }
            et_CoverFile.setText("\n\nExternal Media: readable="
                    + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable);

        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/MySecretKeeper2016");
        directory.mkdirs();

    }

    //Browse Cover File Button
    public void fileBrowserCover (View view)
    {
        selectImage();
        fileChooserCounter = 1;
    }

    //Browse Secret File Button
    public void fileBrowserSecret (View view)
    {
        selectImage();
        fileChooserCounter = 2;
    }

    //Hide Button
    public void hideSecret (View view)
    {
        File file1 = new File(et_CoverFile.getText().toString());
        File file2 = new File(et_SecretFile.getText().toString());
        File file3 = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/MySecretKeeper2016", System.currentTimeMillis() + "-MSK.jpg");
        // convert File to byte[]



        try {
            FileInputStream inStream = new FileInputStream(file1);
            FileInputStream inStream2 = new FileInputStream(file2);
            FileOutputStream outStream = new FileOutputStream(file3);
            BufferedOutputStream bos = null;
            FileChannel inChannel = inStream.getChannel();
            FileChannel inChannel2 = inStream2.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inChannel2.transferTo(0, inChannel2.size(), outChannel);

            inStream.close();
            inStream2.close();
            outStream.close();

            byte[] b = new byte[4];
            for (int i = 0; i < 4; i++) {
                int offset = (b.length - 1 - i) * 8;
                b[i] = (byte) ((file2.length() >>> offset) & 0xFF);
            }
            file3.createNewFile();
            FileOutputStream fos = new FileOutputStream(file3, true);
            bos = new BufferedOutputStream(fos);
            bos.write(b);
            bos.flush();
            bos.close();


            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(HideActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/MySecretKeeper2016", System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
            Toast.makeText(getBaseContext(), "Wrote the camera photo!",
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "1 Didnt write the camera photo!",
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "2 Didnt write the camera photo!",
                    Toast.LENGTH_LONG).show();
        }

        if (fileChooserCounter == 1)
        {
            coverThumb.setImageBitmap(thumbnail);
            et_CoverFile.setText(destination.toString());
        }
        else
        {
            secretThumb.setImageBitmap(thumbnail);
            et_SecretFile.setText(destination.toString());
        }

    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);
        Bitmap bm;

        if (fileChooserCounter == 1)
        {
            coverThumb.setImageURI(selectedImageUri);
            et_CoverFile.setText(selectedImagePath);
        }
        else
        {
           secretThumb.setImageURI(selectedImageUri);
            et_SecretFile.setText(selectedImagePath);
        }

//
//
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeFile(selectedImagePath, options);
//        final int REQUIRED_SIZE = 200;
//        int scale = 1;
//        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
//                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
//            scale *= 2;
//        options.inSampleSize = scale;
//        options.inJustDecodeBounds = false;
//
//        bm = BitmapFactory.decodeFile(selectedImagePath, options);
//        et_CoverFile.setText(selectedImagePath);
//        coverThumb.setImageBitmap(bm);
    }



//
//    // Storage Permissions
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//
//    /**
//     * Checks if the app has permission to write to device storage
//     *
//     * If the app does not has permission then the user will be prompted to grant permissions
//     *
//     * @param activity
//     */
//    public static void verifyStoragePermissions(Activity activity) {
//        // Check if we have write permission
//        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//    }

}
