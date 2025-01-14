package com.gridle.localdelivery.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gridle.localdelivery.Interface.ImageClickListener;
import com.gridle.localdelivery.R;
import com.gridle.localdelivery.adapter.ReviewAdapter;
import com.gridle.localdelivery.fragment.ReviewFragment;
import com.gridle.localdelivery.fragment.StocksFragment;
import com.gridle.localdelivery.local.Entity.ShopsEntity;
import com.gridle.localdelivery.model.NearbyShopsResponse;
import com.gridle.localdelivery.utils.PrefUtils;
import com.gridle.localdelivery.viewModel.NearbyShopsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShopDetailActivity extends AppCompatActivity implements ImageClickListener {

    private ImageView imageViewFavLike;
    private ImageView imageViewFavUnlike;
    private ImageView imageViewShop;
    private ConstraintLayout constraintLayoutCall;
    private ConstraintLayout constraintLayoutReview;
    private TextView textViewLocation;
    private ImageView imageViewDelivering;
    private TextView textViewPreparing;
    private ImageView imageViewPreparing;
    private PrefUtils prefUtils;
    private ConstraintLayout blurr_screen;
    private ImageView order_type_screen;
    private ImageView imageViewVisitStore;
    private ConstraintLayout constraint_layout_order_type;
    private TextView textViewPickup;
    private TextView textViewDelivery;
    private TextView textViewReadMoreReviews;
    private ImageView imageViewBackButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private int flag = 0;
    public static final int position = 0;
    private NearbyShopsViewModel viewModel;
    private ShopsEntity shop;
    private TextView textViewShopName;
    private TextView textViewShopType;
    private TextView textViewShopAddress;
    private TextView textViewShopTiming;
    private TextView textViewShopDay;
    private TextView textViewRating;
    private TextView textViewNumberOfOrders;
    private CardView cardViewRating;
    private String phoneNumber;
    private int mRequestCode = 1;
    private ReviewAdapter reviewAdapter;
    private int pos;
    private List<NearbyShopsResponse.Result.NearbyShopsObject.ReviewObject> reviewList = new ArrayList<>();
    private String shopDay = "MTWTFSS";
    private boolean isDelivery = false;
    private boolean isPickup = true;
    private int deliveryPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        prefUtils = new PrefUtils(ShopDetailActivity.this);

        pos = getIntent().getIntExtra(String.valueOf(position),0);
        checkNetwork();
        setView();
        getShopDetails();
        setClickListeners();
        progressBar.setVisibility(View.GONE);
    }

    private void checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo datac = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi != null & datac != null)
                && (wifi.isConnected() || datac.isConnected())) {
            //connection is available
        }else{
            //no connection
            setAlertBox();
        }
    }

    private void setAlertBox() {
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setView() {
        imageViewFavLike = findViewById(R.id.imageViewFavLike);
        imageViewFavUnlike = findViewById(R.id.imageViewFavUnlike);
        imageViewShop = findViewById(R.id.imageViewShopDetail);
        constraintLayoutCall = findViewById(R.id.constraint_layout_call);
        constraintLayoutReview = findViewById(R.id.constraint_layout_review);
        textViewLocation = findViewById(R.id.textViewLocation);
        imageViewDelivering = findViewById(R.id.imageViewDelivering);
        textViewPreparing = findViewById(R.id.textViewPreparing);
        imageViewPreparing = findViewById(R.id.imageViewPreparing);
        blurr_screen = findViewById(R.id.blurr_screen);
        order_type_screen = findViewById(R.id.order_type_screen);
        imageViewVisitStore = findViewById(R.id.imageViewVisitStore);
        constraint_layout_order_type = findViewById(R.id.constraint_layout_order_type);
        textViewPickup = findViewById(R.id.textViewPickup);
        textViewDelivery = findViewById(R.id.textViewDelivery);
        textViewShopName = findViewById(R.id.textViewShopNameDetail);
        textViewShopType = findViewById(R.id.textViewShopTypeDetail);
        textViewShopAddress = findViewById(R.id.textViewShopAddressDetail);
        textViewShopTiming = findViewById(R.id.textViewShopTimingDetail);
        textViewShopDay = findViewById(R.id.textViewShopDayDetail);
        textViewRating = findViewById(R.id.textViewShopRatingDetail);
        textViewNumberOfOrders = findViewById(R.id.textViewNumberOfOrders);
        textViewReadMoreReviews = findViewById(R.id.textViewReadMoreReviews);
        cardViewRating = findViewById(R.id.card_view_rating_box_shop_detail);
        imageViewBackButton = findViewById(R.id.imageViewBackButton);
        recyclerView = findViewById(R.id.recycler_view_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recyclerView.setHasFixedSize(true);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getShopDetails() {
        viewModel = ViewModelProviders.of(ShopDetailActivity.this).get(NearbyShopsViewModel.class);
        viewModel.getShopsList().observe(ShopDetailActivity.this, new Observer<List<ShopsEntity>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(List<ShopsEntity> shopsEntities) {
                if(shopsEntities.size()!=0) {
                    shop = shopsEntities.get(pos);
                    if(shop.getImage()!=null) {
                        imageViewShop.setImageURI(Uri.parse(shop.getImage()));
                    }
                    textViewShopName.setText(getShopName());
                    textViewShopType.setText("Shop Type : " + shop.getShopType());
                    textViewShopAddress.setText("Shop Address : " + shop.getAddress());
                    if(shop.getTimings()!=null) {
                        String openTime = shop.getTimings().split("!")[0].split("-")[0];
                        String closeTime = shop.getTimings().split("!")[0].split("-")[1];
                        textViewShopTiming.setText("Shop Timing : " + convertInto12Hour(Integer.parseInt(openTime.split(":")[0]),
                                Integer.parseInt(openTime.split(":")[1])) + " - " +
                                convertInto12Hour(Integer.parseInt(closeTime.split(":")[0]),
                                        Integer.parseInt(closeTime.split(":")[1])));
                        if(shop.getTimings().length()>11) {
                            shopDay = shop.getTimings().split("!")[1];
                            setDay();
                        }
                    }
                    textViewNumberOfOrders.setText(shop.getNumberOfOrders() + " orders completed");
                    setDelivery(shop);
                    setPreparing(shop);
                    phoneNumber = shop.getPhoneNumber();
                    if(shop!=null) {
                        if(shop.isFavourite()) {
                            imageViewFavLike.setVisibility(View.VISIBLE);
                            imageViewFavUnlike.setVisibility(View.GONE);
                        }
                        else {
                            imageViewFavLike.setVisibility(View.GONE);
                            imageViewFavUnlike.setVisibility(View.VISIBLE);
                        }
                        reviewList = shop.getReviewList();
//                        Collections.reverse(reviewList);
                        reviewAdapter = new ReviewAdapter(reviewList, false);
                        recyclerView.setAdapter(reviewAdapter);
                        if(shop.getReviewList().size()<4) {
                            textViewReadMoreReviews.setVisibility(View.GONE);
                        }
                        reviewAdapter.setOnItemClickListener(new ReviewAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                String name = shop.getReviewList().get(position).getUserId().getUsername();
                                int rating = shop.getReviewList().get(position).getRating();
                                String comment = shop.getReviewList().get(position).getComment();
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_visit_store,
                                        new ReviewFragment(name, rating, comment)).addToBackStack(null).commit();
                            }
                        });
                        textViewRating.setText(calculateRating(shop.getReviewList()));
                        if(Double.parseDouble(calculateRating(shop.getReviewList())) >= 4.0) {
                            cardViewRating.setCardBackgroundColor(Color.argb(255, 17, 168, 0));
                        }
                        else if(Double.parseDouble(calculateRating(shop.getReviewList())) >= 3.0) {
                            cardViewRating.setCardBackgroundColor(Color.argb(255, 255, 230, 0));
                        }
                        else {
                            cardViewRating.setCardBackgroundColor(Color.argb(255, 255, 0, 0));
                        }
                    }
                }
            }
        });
    }

    private void setClickListeners() {
        imageViewFavUnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.fav(pos, 1);
                imageViewFavLike.setVisibility(View.VISIBLE);
                imageViewFavUnlike.setVisibility(View.GONE);
            }
        });

        imageViewFavLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.fav(pos, 0);
                imageViewFavLike.setVisibility(View.GONE);
                imageViewFavUnlike.setVisibility(View.VISIBLE);
            }
        });

        constraintLayoutCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call();
            }
        });

        constraintLayoutReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_visit_store,
                        new ReviewFragment(shop.get_id(), reviewList)).addToBackStack(null).commit();
            }
        });

        textViewReadMoreReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_visit_store,
                        new ReviewFragment(shop.get_id(), reviewList)).addToBackStack(null).commit();
            }
        });

        imageViewVisitStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraint_layout_order_type.setVisibility(View.VISIBLE);
                textViewPickup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isPickup) {
                            Toast.makeText(ShopDetailActivity.this, "Pickup",
                                    Toast.LENGTH_LONG).show();
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_visit_store,
                                    new StocksFragment(shop.getStock(), shop.get_id(), getShopName(), true,
                                            shop.isFavourite(), pos, isPickup, isDelivery && isPickup,
                                            deliveryPrice))
                                    .addToBackStack(null).commit();
                        }
                        else {
                            Toast.makeText(ShopDetailActivity.this, "Pickup Not Available",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });

                textViewDelivery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isDelivery && isPickup) {
                            Toast.makeText(ShopDetailActivity.this, "Delivery",
                                    Toast.LENGTH_LONG).show();
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_visit_store,
                                    new StocksFragment(shop.getStock(), shop.get_id(), getShopName(), false,
                                            shop.isFavourite(), pos, isPickup, isDelivery && isPickup,
                                            deliveryPrice))
                                    .addToBackStack(null).commit();
                        }
                        else {
                            Toast.makeText(ShopDetailActivity.this, "Delivery Not Available",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        blurr_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 0;
                order_type_screen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        flag = 1;
                    }
                });
                if(flag==0) {
                    constraint_layout_order_type.setVisibility(View.GONE);
                }
            }
        });

        imageViewBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @SuppressLint({"SetTextI18n"})
    private void setDelivery(ShopsEntity shop) {
        double distance = Math.round (shop.getDistance() * 100.0) / 100.0;
        if(shop.isDelivery() && Double.parseDouble(shop.getDeliveryRadius()) >= distance) {
            textViewLocation.setText("Delivering to : " + prefUtils.getAddress());
            imageViewDelivering.setImageResource(R.drawable.tick);
            textViewDelivery.setTextColor(getResources().getColor(R.color.black));
            isDelivery = true;
            deliveryPrice = Integer.parseInt(shop.getDeliveryRate());
        }
        else {
            textViewLocation.setText("Not Delivering to : " + prefUtils.getAddress());
            imageViewDelivering.setImageResource(R.drawable.cross);
            textViewDelivery.setTextColor(getResources().getColor(R.color.colorGrey));
            isDelivery = false;
        }
    }

    @SuppressLint("SetTextI18n")
    private void setPreparing(ShopsEntity shop) {

        if(shop.getTimings().length() > 11) {
            final Calendar c = Calendar.getInstance();
            int mHour = c.get(Calendar.HOUR_OF_DAY);
            int mMinute = c.get(Calendar.MINUTE);
            int day = c.get(Calendar.DAY_OF_WEEK);
            String openTime = shop.getTimings().split("!")[0].split("-")[0];
            String closeTime = shop.getTimings().split("!")[0].split("-")[1];
            String shopDay = shop.getTimings().split("!")[1];
            boolean isTimeInRange;
            boolean isPreparing = false;

            if(mHour > Integer.parseInt(openTime.split(":")[0]) &&
                    mHour < Integer.parseInt(closeTime.split(":")[0])) {
                isTimeInRange = true;
            }
            else if(mHour == Integer.parseInt(openTime.split(":")[0]) &&
                    mMinute >= Integer.parseInt(openTime.split(":")[1])) {
                isTimeInRange = true;
            }
            else if(mHour == Integer.parseInt(closeTime.split(":")[0]) &&
                    mMinute <= Integer.parseInt(closeTime.split(":")[1])) {
                isTimeInRange = true;
            }
            else {
                isTimeInRange = false;
            }

            if(isTimeInRange) {
                switch (day) {
                    case Calendar.MONDAY :
                        isPreparing = shopDay.charAt(0) == 'M';
                        break;

                    case Calendar.TUESDAY:
                        isPreparing = shopDay.charAt(1) == 'T';
                        break;

                    case Calendar.WEDNESDAY:
                        isPreparing = shopDay.charAt(2) == 'W';
                        break;

                    case Calendar.THURSDAY:
                        isPreparing = shopDay.charAt(3) == 'T';
                        break;

                    case Calendar.FRIDAY:
                        isPreparing = shopDay.charAt(4) == 'F';
                        break;

                    case Calendar.SATURDAY:
                        isPreparing = shopDay.charAt(5) == 'S';
                        break;

                    case Calendar.SUNDAY:
                        isPreparing = shopDay.charAt(6) == 'S';
                        break;

                }
            }

            if(isTimeInRange && isPreparing) {
                textViewPreparing.setText("Currently preparing order for Pickup");
                imageViewPreparing.setImageResource(R.drawable.tick);
                isPickup = true;
            }
            else {
                textViewPreparing.setText("Not preparing order for Pickup");
                imageViewPreparing.setImageResource(R.drawable.cross);
                isPickup = false;
            }
        }
    }

    private String getShopName() {
        String name = shop.getShopName();
        String shopName = "";
        int firstAscii = (int)name.charAt(0);
        if(firstAscii >= 97 && firstAscii <= 122) {
            shopName = (char)(firstAscii - 32) + name.substring(1);
        }
        else {
            shopName = name;
        }
        return shopName;
    }

    private String convertInto12Hour(int hourOfDay, int minute) {
        String format;
        if (hourOfDay == 0) {
            hourOfDay += 12;
            format = "AM";
        }
        else if (hourOfDay == 12) {
            format = "PM";
        }
        else if (hourOfDay > 12) {
            hourOfDay -= 12;
            format = "PM";
        }
        else {
            format = "AM";
        }

        return hourOfDay + ":" + minute + " " + format;
    }

    private void setDay() {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString redSpannable= new SpannableString("M T W T F S S");
        boolean[] dayArray = new boolean[]{shopDay.charAt(0) == 'M', shopDay.charAt(1) == 'T',
                shopDay.charAt(2) == 'W', shopDay.charAt(3) == 'T', shopDay.charAt(4) == 'F',
                shopDay.charAt(5) == 'S', shopDay.charAt(6) == 'S'};
        for(int i=0; i<dayArray.length; i++) {
            if(dayArray[i]) {
                redSpannable.setSpan(new ForegroundColorSpan(Color.RED), i*2, (i*2)+1, 0);
            }
        }
        builder.append(redSpannable);

        textViewShopDay.setText(builder, TextView.BufferType.SPANNABLE);
    }

    private void call() {
        String uri = "tel:" + phoneNumber.trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    mRequestCode);
            return;
        }
        startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private String calculateRating(List<NearbyShopsResponse.Result.NearbyShopsObject.ReviewObject> reviewList) {
        int ratingSum = 0;
        double ratingAverage;
        for(NearbyShopsResponse.Result.NearbyShopsObject.ReviewObject reviewObject : reviewList) {
            ratingSum += reviewObject.getRating();
        }
        ratingAverage = ((double) ratingSum)/reviewList.size();
        return String.format("%.1f", ratingAverage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == mRequestCode) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call();
            }
            else {
                Toast.makeText(ShopDetailActivity.this, "Call Permission Not Granted !",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void setImageClick(String imageUrl, String itemName) {
        StocksFragment stocksFragment = (StocksFragment) getSupportFragmentManager().
                findFragmentById(R.id.frame_layout_visit_store);

        assert stocksFragment != null;
        stocksFragment.setImageClick(imageUrl, itemName);
    }
}
