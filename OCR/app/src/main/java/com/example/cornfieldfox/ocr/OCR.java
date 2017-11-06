package com.example.cornfieldfox.ocr;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OCR extends AppCompatActivity {
    private TessBaseAPI tessOCR;
    Bitmap image;
    String datapath = "";
    List<String> items;
    ListView lv;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        //tesseract initialization:
        datapath = getFilesDir()+"/tessdata/";
        String language = "eng";
        datapath = getFilesDir()+ "/tesseract/";
        tessOCR = new TessBaseAPI();
        checkFile(new File(datapath + "tessdata/"));
        tessOCR.init(datapath, language);
        items = new ArrayList<>();

        // default picture
        image = getBitmapFromAsset(getApplicationContext(),"wholefood2.JPG");
        ImageView iv = (ImageView)findViewById(R.id.ocrIMG);
        // receipt's image will take 1/3 of the whole screen
        iv.setMaxHeight(getWindowManager().getDefaultDisplay().getHeight()/3);
        iv.setImageBitmap(image);
        //extract button's reference
        Button extraOCR = (Button) findViewById(R.id.extractBTN);
        extraOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tessOCR.setImage(image);
                tessOCR.setImage(image);

                items.addAll(Arrays.asList(split(tessOCR.getUTF8Text())));
                ListView lv = (ListView) findViewById(R.id.itemLst);
                adapter.notifyDataSetChanged();
            }
        });
        lv = (ListView) findViewById(R.id.itemLst);
        adapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,items);
        lv.setAdapter(adapter);




    }


    private String[] split(String raw)
    {
        List<String> resList = new ArrayList<>();
        String[] temp = raw.split("\n");
        int start = -1;
        for(int i = 0; i < temp.length; i++)
        {
            if(temp[i].matches(".*[0-9]+\\.[0-9][0-9].*") && start==-1)
            {
                start = i;
                break;
            }

        }
        if(start == -1)
            start = 0;

        int end = temp.length;
        for(int i = temp.length-1; i >=0; i--)
        {
            if(temp[i].matches(".*[0-9]+\\.[0-9][0-9].*") && end==temp.length)
            {
                if(start<i)
                    end = i;
                else
                    end = temp.length-1;

                break;
            }
        }
        for(int i = start; i<=end;i++)
        {
            resList.add(temp[i]);
            Log.w("receipt",temp[i]);
        }

        return resList.toArray(new String[resList.size()]);
    }
//    // return the index contains "subtotal"
//    private int findSubtotal(String[] raw)
//    {
//
//    }


    private Bitmap getBitmapFromAsset(Context context, String name)
    {
        AssetManager am = context.getAssets();
        Bitmap bm = null;
        InputStream is;
        try
        {
            is = am.open(name);
            bm = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;

    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
