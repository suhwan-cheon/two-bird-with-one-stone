package com.example.twobirdwithonestone.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.twobirdwithonestone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SubShopListViewActivity extends AppCompatActivity {
    private ListView mListView;
    private SubShopListViewAdapter mAdapter;
    private FirebaseFirestore db;
    private String currentTime;
    private String currentUID;
    private long userPoint = 0;
    private String itemPrice;
    private DataBase database;
    private Coupon selectedItemasCoupon;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sublistshop);
        mListView = (ListView)findViewById(R.id.SubShopListview);
        progressBar = findViewById(R.id.sublistshop_progressbar);
        mAdapter = new SubShopListViewAdapter();
        mListView.setAdapter(mAdapter);

        //ShopListViewActivity에서 intent로 전달하는 배열 받기
        Intent intent = getIntent();
        //ArrayList 를 받아 addItem
        ArrayList<ParcelableItems> list = intent.getParcelableArrayListExtra("coffee_list");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
        Calendar cal = Calendar.getInstance();
        currentTime = sdf.format(cal.getTime());
        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //new Coupon(currentTime.toString(), "쿠폰", currentUID, false, currentUID)

        final int i = 0;
        int couponImgIndex = -1;
        Bitmap bitmap = BitmapFactory.decodeByteArray(list.get(i).image, 0, list.get(i).image.length);
        //선택한 아이템의 Detail한 설명을 보여주기 위해 listview에 하나의 아이템만 add. 굳이 listView를 쓸 필요는 없지만 개발의 편의를 위해 수정하지 않고 진행함.
        mAdapter.addItem(list.get(i).category, list.get(i).name, bitmap, list.get(i).price, list.get(i).explanation);
        if(list.get(i).name.equals("경복궁 야간개장 입장권")){
            couponImgIndex = 101;
        }else if(list.get(i).name.equals("따릉이")){
            couponImgIndex = 102;
        }else if(list.get(i).name.equals("아쿠아리움")){
            couponImgIndex = 103;
        }else if(list.get(i).name.equals("서울대공원 동물원")){
            couponImgIndex = 104;
        }else if(list.get(i).name.equals("롯데월드 타워 전망대")){
            couponImgIndex = 105;
        }else if(list.get(i).name.equals("개발도상국 어린이 지원")){
            couponImgIndex = 201;
        }else if(list.get(i).name.equals("도심 숲 조성과 보존")){
            couponImgIndex = 202;
        }else if(list.get(i).name.equals("기부금품 모집")){
            couponImgIndex = 203;
        }else if(list.get(i).name.equals("동화책선물")){
            couponImgIndex = 204;
        }else if(list.get(i).name.equals("국내 사업현장의 아동권리 보호")){
            couponImgIndex = 205;
        }else if(list.get(i).name.equals("아메리카노")){
            couponImgIndex = 301;
        }else if(list.get(i).name.equals("카푸치노")){
            couponImgIndex = 302;
        }else if(list.get(i).name.equals("프라푸치노")){
            couponImgIndex = 303;
        }else if(list.get(i).name.equals("난지 캠핑장")){
            couponImgIndex = 401;
        }else if(list.get(i).name.equals("노을 캠핑장")){
            couponImgIndex = 402;
        }else if(list.get(i).name.equals("서울대공원 캠핑장")){
            couponImgIndex = 403;
        }else if(list.get(i).name.equals("중랑 캠핑장")){
            couponImgIndex = 404;
        }else if(list.get(i).name.equals("강동 그린웨이 캠핑장")){
            couponImgIndex = 405;
        }else if(list.get(i).name.equals("딸기 생크림 케이크")){
            couponImgIndex = 501;
        }else if(list.get(i).name.equals("딸기 조각 케이크")){
            couponImgIndex = 502;
        }else if(list.get(i).name.equals("아이스크림 콘")){
            couponImgIndex = 601;
        }else{
            couponImgIndex = -1;
        }

        Button btn_buy_item = findViewById(R.id.btn_buy_item);
        itemPrice = list.get(0).getprice();
        Log.d("SubShopListViewActivity", "햐햐 아이템가격"+itemPrice);
        String buyString = itemPrice+"Point로 구매";
        btn_buy_item.setText(buyString);
        Log.d("123", btn_buy_item.getText().toString());
        db = FirebaseFirestore.getInstance();
        DocumentReference couponDocRef= db.collection("Coupons").document();
        String couponUID = couponDocRef.getId();
        final DocumentReference couponRef = db.collection("Coupons").document(couponUID);
        Log.d("SubShopSubShop", couponDocRef.getId());
        selectedItemasCoupon = new Coupon(currentTime, list.get(i).name, couponUID, false, currentUID, couponImgIndex );
        //여기에 CUID 생성해야하는데 TODO
        btn_buy_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                //유저의 포인트 가져오기
                final DocumentReference docRef = db.collection("Users").document(currentUID);
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserData userData = documentSnapshot.toObject(UserData.class);
                        userPoint =  userData.getPoint();
                        //Toast.makeText(getApplicationContext(), "햐햐 보유 포인트는 "+userPoint, Toast.LENGTH_SHORT).show();
                        //아이템 구매하기
                        if(userPoint != 0){
                            int itemPriceInt = Integer.parseInt(itemPrice);
                            if(userPoint >= itemPriceInt){
                                //유저 Point Update
                                db.collection("Users").document(currentUID)
                                        .update("point", userPoint - itemPriceInt)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //Toast.makeText(getApplicationContext(), "햐햐 유저 포인트 업데이트 성공", Toast.LENGTH_SHORT).show();
                                                //쿠폰 추가하기!
                                                SubShopListViewItem bitem = (SubShopListViewItem)mListView.getItemAtPosition(0);
                                                Log.d("SubShopListview", "SUSU"+ bitem.getTitle());
                                                couponRef
                                                        .set(selectedItemasCoupon)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getApplicationContext(), "쿠폰을 구매했습니다.", Toast.LENGTH_SHORT).show();
                                                                Log.d("SubShopListViewActivity", "햐햐 쿠폰 저장 성공");
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d("SubShopListViewActivity", "햐햐 쿠폰 저장 실패");
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                finish();
                                                            }
                                                        });
//                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                                            @Override
//                                                            public void onSuccess(DocumentReference documentReference) {
//
//                                                            }
//                                                        })
//                                                        .addOnFailureListener(new OnFailureListener() {
//                                                            @Override
//                                                            public void onFailure(@NonNull Exception e) {
//
//                                                            }
//                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "유저 포인트 업데이트 실패", Toast.LENGTH_SHORT).show();
                                                Log.w(TAG, "Error updating document", e);
                                                progressBar.setVisibility(View.INVISIBLE);
                                                finish();
                                            }
                                        });
                            }else{
                                Toast.makeText(getApplicationContext(), "포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                finish();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("SubShopListViewActivity", "O point");
                            finish();
                        }
                    }
                });
            }
        });
    }
}
