/*
 * Copyright (C) 2016 Olmo Gallegos Hernández.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.voghdev.pdfviewpager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import es.voghdev.pdfviewpager.library.PDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.BasePDFPagerAdapter;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import es.voghdev.pdfviewpager.library.asset.CopyAsset;
import es.voghdev.pdfviewpager.library.asset.CopyAssetThreadImpl;

public class AssetOnSDActivity extends BaseSampleActivity {
    final String[] sampleAssets = {"Itron.pdf","adobe.pdf", "sample.pdf"};

    PDFViewPager pdfViewPager;
    File pdfFolder;

    Button folderButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.asset_on_sd);
        setContentView(R.layout.activity_asset_on_sd);
        pdfViewPager = (PDFViewPager) findViewById(R.id.pdfviewfpager);
        pdfFolder = Environment.getExternalStorageDirectory();
        copyAssetsOnSDCard();
        folderButton = findViewById(R.id.folder_button);

        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pickpdfstorage();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 10:
                // Get the Uri of the selected file
                if (resultCode == RESULT_OK) {

                    if (null != data.getData()) {

                        Uri uri = data.getData();
                        File file;

                        if (uri.getScheme().equals("content")) {

                            file = new File(getCacheDir(), data.getData().getLastPathSegment());

                            try {
                                InputStream iStream = getContentResolver().openInputStream(uri);
                                FileOutputStream output = null;
                                output = new FileOutputStream(file);
                                final byte[] buffer = new byte[1024];
                                int size;
                                while ((size = iStream.read(buffer)) != -1) {
                                    output.write(buffer, 0, size);
                                }
                                iStream.close();
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else
                            file = new File(uri.getPath());

                        PagerAdapter adapter = new PDFPagerAdapter.Builder(this)
                                .setPdfPath(file.getAbsolutePath())
                                .setOffScreenSize(pdfViewPager.getOffscreenPageLimit())
                                .create();
                        pdfViewPager.setAdapter(adapter);
//                        OpenPdfActivity(file.getAbsolutePath());
                    }
                }
                break;
        }

    }

    private void Pickpdfstorage() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, 10);
    }

    protected void copyAssetsOnSDCard() {
        final Context context = this;
        CopyAsset copyAsset = new CopyAssetThreadImpl(getApplicationContext(), new Handler(), new CopyAsset.Listener() {
            @Override
            public void success(String assetName, String destinationPath) {
//                pdfViewPager = new PDFViewPager(context, getPdfPathOnSDCard());
//                setContentView(pdfViewPager);

                PagerAdapter adapter = new PDFPagerAdapter.Builder(context)
                        .setPdfPath(getPdfPathOnSDCard())
                        .setOffScreenSize(pdfViewPager.getOffscreenPageLimit())
                        .create();

                pdfViewPager.setAdapter(adapter);
//                pdfViewPager.addView(pdfViewPager);
//                pdfViewPager.init
            }

            @Override
            public void failure(Exception e) {
                e.printStackTrace();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        for (String asset : sampleAssets) {
            copyAsset.copy(asset, new File(pdfFolder, asset).getAbsolutePath());
        }
    }

    protected String getPdfPathOnSDCard() {
        File f = new File(pdfFolder, "adobe.pdf");
        return f.getAbsolutePath();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (pdfViewPager != null) {
            ((BasePDFPagerAdapter) pdfViewPager.getAdapter()).close();
        }
    }
}