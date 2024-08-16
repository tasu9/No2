package com.example.no2;
//ここら辺はchatgptにお願いしたら出てきた
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.no2.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private ActivityResultLauncher<Intent> _cameraLauncher;
    private ActivityResultLauncher<String> _pickImageLauncher;
    private ImageView imageView;
    private Button buttonClearImage;
    //CAMERA_PERMISSION_REQUEST_CODE はカメラのパーミッションを要求する際のリクエストコード
    //_cameraLauncher はカメラアクティビティの結果を受け取るための ActivityResultLauncher 。
    //imageView はカメラで撮影した画像を表示するための ImageView
    //buttonClearImage は ImageView に表示された画像をクリアするためのボタン

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI要素の取得と設定:
        buttonClearImage = findViewById(R.id.button_clear_image);
        imageView = findViewById(R.id.imageView);
        Button cameraButton = findViewById(R.id.button_take_picture);
        Button pickImageButton = findViewById(R.id.button_pick_image);


        //ContextCompat.checkSelfPermission を使ってカメラのパーミッションがあるか確認。
        //パーミッションはAndroidManifest.xmlに入力されている。
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
        // それぞれのボタンを押したときに行う処理の設定
        buttonClearImage.setOnClickListener(v -> clearImage());
        //クリアボタンは写真を表示する際にポンっと表示されるようにしてある
        cameraButton.setOnClickListener(v -> onCameraButtonClick());
        pickImageButton.setOnClickListener(v -> onPickImageButtonClick());

        // ActivityResultLauncherの初期化
        //registerForActivityResult でカメラアクティビティの結果を受け取るためのコールバックを設定。
        _cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Bitmap bitmap;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    bitmap = data.getParcelableExtra("data", Bitmap.class);
                                } else {
                                    bitmap = data.getParcelableExtra("data");
                                }
                                if (bitmap != null) {
                                    //カメラで撮影した画像を Bitmap として取得し、ImageView に表示
                                    imageView.setImageBitmap(bitmap);
                                    imageView.setVisibility(View.VISIBLE); // 画像が設定されたら表示する
                                    buttonClearImage.setVisibility(View.VISIBLE); // 画像が表示されたらボタンも表示する

                                }
                            }
                        }
                    }
                }
        );
        // 画像選択アクティビティの結果を受け取るための ActivityResultLauncher の初期化
        //ストレージからも写真選択ができるように機能を追加しました
        _pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            try {
                                // URI から Bitmap をデコードする
                                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                                if (bitmap != null) {
                                    imageView.setImageBitmap(bitmap);
                                    imageView.setVisibility(View.VISIBLE);
                                    buttonClearImage.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                Log.e("MainActivity", "Error loading image", e);
                            }
                        }
                    }
                }
        );



        // ViewPager2 と TabLayout を取得
        ViewPager2 viewPager = findViewById(R.id.my_view_pager);
        TabLayout tabLayout = findViewById(R.id.my_tab_layout);

        // ViewPager2 にアダプタを設定
        viewPager.setAdapter(new MyVPAdapter(this));

        // TabLayoutMediator を設定
        //TabLayoutMediator を使ってタブレイアウトと ViewPager2 をがっちゃんこ
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("メニュー");
                    break;
                case 1:
                    tab.setText("カレンダー");
                    break;
            }
        }).attach();
    }
    //カメラボタンを押すとここに飛んで処理が行われる
    private void onCameraButtonClick() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        _cameraLauncher.launch(cameraIntent);
    }
    //ストレージボタンを押すとここに飛ぶ
    private void onPickImageButtonClick() {
        _pickImageLauncher.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが許可された場合の処理
            } else {
                // パーミッションが拒否された場合の処理
                Log.d("MainActivity", "カメラのパーミッションが拒否されました。");
            }
        }
    }
    //クリアボタンを押すとここに飛んで処理が行われる
    private void clearImage() {
        imageView.setImageURI(null); // 画像をクリア
        imageView.setVisibility(View.GONE); // 画像ビューを非表示
        buttonClearImage.setVisibility(View.GONE); // 画像を消したらボタンも非表示
    }
}
