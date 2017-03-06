package com.leadeon.zxing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class TestScanActivity extends AppCompatActivity implements QRCodeView.Delegate, View.OnClickListener {
    private static final String TAG = TestScanActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;

    private QRCodeView mQRCodeView;
    private TextView toolbar;
    private ImageView scan;
    private ImageView auth;
    private ImageView orc;
    private RelativeLayout head;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scan);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mQRCodeView = (ZXingView) findViewById(R.id.zxingview);
        mQRCodeView.setDelegate(this);
        head = (RelativeLayout) findViewById(R.id.textscan);
        scan = (ImageView) findViewById(R.id.scan);
        auth = (ImageView) findViewById(R.id.auth);
        orc = (ImageView) findViewById(R.id.orc);
        toolbar = ((TextView) findViewById(R.id.toolbar_title));

        findViewById(R.id.finish).setOnClickListener(this);
        findViewById(R.id.file).setOnClickListener(this);
        scan.setOnClickListener(this);
        auth.setOnClickListener(this);
        orc.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
//        mQRCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);

        mQRCodeView.showScanRect();
        mQRCodeView.startSpot();


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.e(TAG, "onScanQRCodeSuccess: "+result);
//        vibrate();
//        mQRCodeView.startSpot();
        if (result!=null&&result.startsWith("http")){

            View inflate = getLayoutInflater().inflate(R.layout.activity_main2, null);

            head.addView(inflate);
//            Toolbar title = (Toolbar) inflate.findViewById(R.id.main_toolabar);
            WebView webView = (WebView) inflate.findViewById(R.id.web);
//            result = "http://www.baidu.com";
            webView.getSettings().setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    view.loadUrl(request.getUrl().toString());
                    return true;
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    Log.e(TAG, "onReceivedError: "+error.toString());
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            webView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    toolbar.setText(title);
                    //a textview
                }
            });
            webView.loadUrl(result);
//            Intent intent = new Intent(this,Main2Activity.class);
//            intent.putExtra("result",result);
//            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
//            startActivity(intent);
        }else{
            Toast.makeText(this, "非http链接", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }
    /**
     * 打开图库页面进行选择图片
     */
    private void openFile() {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 1);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish:
                this.finish();
                break;
            case R.id.file:
                Toast.makeText(this, "文件", Toast.LENGTH_SHORT).show();
                openFile();
                break;
            case R.id.scan:
                toolbar.setText("扫一扫");
                scan.setImageResource(R.mipmap.scan_scan_select);
                auth.setImageResource(R.mipmap.scan_auth);
                break;
            case R.id.auth:
                scan.setImageResource(R.mipmap.scan_scan);
                auth.setImageResource(R.mipmap.scan_auth_select);
                toolbar.setText("授权网点查验");
                break;
            case R.id.orc:
                scan.setImageResource(R.mipmap.scan_scan);
                auth.setImageResource(R.mipmap.scan_auth);
                Toast.makeText(this, "充值卡充值", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== Activity.RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();
                        if (null != picturePath) {
                            new DecodeTask().execute(picturePath);
                        }
                    }
                    break;
            }
        }
    }
    class DecodeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Bitmap smallBitmap = getSmallBitmap(params[0]);
            return QRCodeDecoder.syncDecodeQRCode(smallBitmap);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (TextUtils.isEmpty(s)) {
                Toast.makeText(TestScanActivity.this, "shibai", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TestScanActivity.this, s, Toast.LENGTH_SHORT).show();
                onScanQRCodeSuccess(s);
            }
        }
    }
    private Bitmap getSmallBitmap(String filePath) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, 240, 360);
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        if (bm == null) {
            return null;
        }
        int degree = readPictureDegree(filePath);
        bm = rotateBitmap(bm, degree);
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }

        return inSampleSize;
    }

    private int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

}
