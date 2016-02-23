package jason.cruz.mysecretkeeper2016;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class HideActivity extends AppCompatActivity {

    ImageView coverThumb, secretThumb, stegoThumb;
    EditText et_CoverFile, et_SecretFile;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    int fileChooserCounter = 0;
    long coverTime, secretTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coverThumb = (ImageView) findViewById(R.id.coverThumb);
        secretThumb = (ImageView) findViewById(R.id.secretThumb);
        stegoThumb = (ImageView) findViewById(R.id.stegoThumb);
        et_CoverFile = (EditText) findViewById(R.id.et_CoverFile);
        et_SecretFile = (EditText) findViewById(R.id.et_SecretFile);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//CHECK IF STORAGE IS READABLE AND WRITABLE
/*        boolean mExternalStorageAvailable = false;
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
*/
    }

    //Browse Cover File Button
    public void fileBrowserCover(View view) {
        selectImage();
        fileChooserCounter = 1;
    }

    //Browse Secret File Button
    public void fileBrowserSecret(View view) {
        selectImage();
        fileChooserCounter = 2;
    }

    //Hide Button
    public void hideSecret(View view) {
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

            int mark = 675;
            byte[] c = new byte[4];
            for (int i = 0; i < 4; i++) {
                int offset = (c.length - 1 - i) * 8;
                c[i] = (byte) ((mark >>> offset) & 0xFF);
            }
            bos.write(c);
            bos.flush();
            bos.close();

            Bitmap bitmap = decodeSampledBitmapFromFile(file3.getAbsolutePath(), 1000, 700);
            stegoThumb.setImageBitmap(bitmap);
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(HideActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (fileChooserCounter == 1) {
                        coverTime = System.currentTimeMillis();
                        File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                                "/MySecretKeeper2016", coverTime + ".jpg");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        startActivityForResult(intent, REQUEST_CAMERA);
                    } else {
                        secretTime = System.currentTimeMillis();
                        File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                                "/MySecretKeeper2016", secretTime + ".jpg");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
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
        if (fileChooserCounter == 1) {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/MySecretKeeper2016", coverTime + ".jpg");
            Bitmap bitmap = decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);
            coverThumb.setImageBitmap(bitmap);
            et_CoverFile.setText(file.toString());
        } else {
            File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/MySecretKeeper2016", secretTime + ".jpg");
            Bitmap bitmap = decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);
            secretThumb.setImageBitmap(bitmap);
            et_SecretFile.setText(file.toString());
        }
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

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);
        Bitmap bm;

        if (fileChooserCounter == 1) {
            coverThumb.setImageURI(selectedImageUri);
            et_CoverFile.setText(selectedImagePath);
        } else {
            secretThumb.setImageURI(selectedImageUri);
            et_SecretFile.setText(selectedImagePath);
        }
    }
}