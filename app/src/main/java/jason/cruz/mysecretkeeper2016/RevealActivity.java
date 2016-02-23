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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class RevealActivity extends AppCompatActivity {

    ImageView revealThumb, stegoThumb;
    EditText et_StegoFile;
    int SELECT_FILE_FROM_FOLDER = 0, SELECT_FILE = 1;
    int fileChooserCounter = 0;
    long coverTime, secretTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reveal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        stegoThumb = (ImageView) findViewById(R.id.stegoThumb);
        revealThumb = (ImageView) findViewById(R.id.revealThumb);
        et_StegoFile = (EditText) findViewById(R.id.et_StegoFile);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Hide Button
    public void revealSecret(View view) {
        File file1 = new File(et_StegoFile.getText().toString());
        File file2 = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/MySecretKeeper2016", System.currentTimeMillis() + "-Revealed.jpg");
        try {
            RandomAccessFile file = new RandomAccessFile(file1, "rw");

            file.seek(file.length() - 8);
            long pointer = file.getFilePointer();
            BufferedOutputStream bos = null;
            //---display file saved message---

            int bytesRead = 0;
            int totalRead = 0;
            int bytesSecretRead = 0;
            int secretRead = 0;
            byte[] buffer = new byte[4]; // 128k buffer
            while(totalRead < 4) { // go on reading while total bytes read is
                // less than 1mb
                bytesRead = file.read(buffer);
                totalRead += bytesRead;

            }

            //
            int bytesRead2 = 0;
            int totalRead2 = 0;
            byte[] buffer2 = new byte[4]; // 128k buffer
            while(totalRead2 < 4) { // go on reading while total bytes read is
                // less than 1mb
                bytesRead2 = file.read(buffer2);
                totalRead2 += bytesRead2;

            }

            int mark_size = buffer[0] << 24 | (buffer[1] & 0xff) << 16 | (buffer[2] & 0xff) << 8
                    | (buffer[3] & 0xff);
            et_StegoFile.setText("MSK " + mark_size);

            //


            int secret_size = buffer[0] << 24 | (buffer[1] & 0xff) << 16 | (buffer[2] & 0xff) << 8
                    | (buffer[3] & 0xff);

            file.seek(file.length() - 8 - Long.valueOf(secret_size));
            pointer = file.getFilePointer();

            FileOutputStream out = new FileOutputStream(file2);
            byte[] secretbuffer = new byte[secret_size];
            while(totalRead < secret_size) { // go on reading while total bytes read is
                // less than 1mb
                bytesSecretRead = file.read(secretbuffer);
                totalRead += bytesSecretRead;
                out.write(secretbuffer, 0, bytesSecretRead);
            }

            file.close();

            Bitmap bitmap = decodeSampledBitmapFromFile(file2.getAbsolutePath(), 1000, 700);
            revealThumb.setImageBitmap(bitmap);
            Toast.makeText(getBaseContext(), "File saved successfully!",
                    Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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



    //Browse Cover File Button
    public void fileBrowserStego(View view) {
        selectImage();
    }

    private void selectImage() {
        final CharSequence[] items = {"My Secret Keeper Folder", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(RevealActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("My Secret Keeper Folder")) {
                    fileChooserCounter = 1;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    Uri uri = Uri.parse(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/MySecretKeeper2016/");
                    intent.setDataAndType(uri, "image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE_FROM_FOLDER);
                } else if (items[item].equals("Choose from Library")) {
                    fileChooserCounter = 2;
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
            if ((requestCode == SELECT_FILE) || (requestCode == SELECT_FILE_FROM_FOLDER)) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                        null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);
                Bitmap bm;

                    stegoThumb.setImageURI(selectedImageUri);
                    et_StegoFile.setText(selectedImagePath);
                }
            }

        }
    }
