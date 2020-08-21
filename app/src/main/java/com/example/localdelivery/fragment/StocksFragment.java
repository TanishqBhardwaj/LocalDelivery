package com.example.localdelivery.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.localdelivery.R;
import com.example.localdelivery.adapter.StocksTabLayoutAdapter;
import com.example.localdelivery.local.Entity.ShopsEntity;
import com.example.localdelivery.model.StocksData;
import com.example.localdelivery.viewModel.NearbyShopsViewModel;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class StocksFragment extends Fragment {

    private EditText searchView;
    private Context mContext;
    private Activity mActivity;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static List<StocksData> shop;
    private List<StocksData> filteredList;
    private ImageView imageViewViewCart;
    private ImageView imageViewLike;
    private ImageView imageViewUnlike;
    private ImageView imageViewBackButton;
    private List<StocksData> cartList;
    private String shopId;
    private String shopName;
    private TextView textViewShopName;
    private boolean isPickup;
    private boolean isFav;
    private int pos;
    private NearbyShopsViewModel viewModel;
    private StocksTabLayoutAdapter mainPagerAdapter;
    private int tabPos;

    public StocksFragment() {
        // Required empty public constructor
    }

    public StocksFragment(List<StocksData> shop, String shopId, String shopName, boolean isPickup,
                          boolean isFav, int pos) {
        cartList = new ArrayList<>();
        StocksFragment.shop = shop;
        this.shopId = shopId;
        this.shopName = shopName;
        this.isPickup = isPickup;
        this.isFav = isFav;
        this.pos = pos;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        if(context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stocks, container, false);
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setView(view);
        setTabLayout();
        setClickListeners();
        return view;
    }

    private void setView(View view) {
        searchView = view.findViewById(R.id.searchViewStocks);
        tabLayout = view.findViewById(R.id.tabLayoutStocks);
        viewPager = view.findViewById(R.id.viewPager);
        imageViewViewCart = view.findViewById(R.id.imageViewViewCart);
        imageViewLike = view.findViewById(R.id.imageViewFavLikeStocks);
        imageViewUnlike = view.findViewById(R.id.imageViewFavUnlikeStocks);
        imageViewBackButton = view.findViewById(R.id.imageViewBackButtonStocks);
        textViewShopName = view.findViewById(R.id.textViewShopNameTitle);

        textViewShopName.setText(shopName);

        if(isFav) {
            imageViewLike.setVisibility(View.VISIBLE);
            imageViewUnlike.setVisibility(View.GONE);
        }
        else {
            imageViewLike.setVisibility(View.GONE);
            imageViewUnlike.setVisibility(View.VISIBLE);
        }
    }

    public static void changeQuantity(int quantity, String id) {
        int i=0;
        for(StocksData stocksData : shop) {
            if(stocksData.get_id().equals(id)) {
                shop.get(i).setQuantity(quantity);
            }
            ++i;
        }
    }

    private void setTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Grocery"));
        tabLayout.addTab(tabLayout.newTab().setText("Skincare"));
        tabLayout.addTab(tabLayout.newTab().setText("Beverages"));
        tabLayout.addTab(tabLayout.newTab().setText("Bakery"));
        tabLayout.addTab(tabLayout.newTab().setText("Organic Food"));
        tabLayout.addTab(tabLayout.newTab().setText("Packed Food"));
        tabLayout.addTab(tabLayout.newTab().setText("Home & Dining"));
        tabLayout.addTab(tabLayout.newTab().setText("Bathroom & Cleaning"));
        tabLayout.addTab(tabLayout.newTab().setText("Electricals and Electronics"));

        mainPagerAdapter = new StocksTabLayoutAdapter(getParentFragmentManager(),
                tabLayout.getTabCount(), mContext, shop);
        viewPager.setAdapter(mainPagerAdapter);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    private void setClickListeners() {
        viewModel = ViewModelProviders.of(StocksFragment.this).get(NearbyShopsViewModel.class);

        imageViewUnlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.fav(pos, 1);
                imageViewLike.setVisibility(View.VISIBLE);
                imageViewUnlike.setVisibility(View.GONE);
            }
        });

        imageViewLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.fav(pos, 0);
                imageViewLike.setVisibility(View.GONE);
                imageViewUnlike.setVisibility(View.VISIBLE);
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        imageViewViewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(StocksData stocksData : shop) {
                    if(stocksData.getQuantity()!=0) {
                        cartList.add(stocksData);
                    }
                }
                if(cartList.size()>0) {
                    getParentFragmentManager().beginTransaction().replace(R.id.frame_layout_visit_store,
                            new OrderFragment(shop, cartList, shopId, shopName, isPickup, isFav, pos)).commit();
                }
                else {
                    Toast.makeText(mContext, "You haven't selected any Item !", Toast.LENGTH_LONG).show();
                }
            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPos = tab.getPosition();
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        imageViewBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onBackPressed();
            }
        });
    }

    private void filter(String text) {
        filteredList = new ArrayList<>();

        for(StocksData stocksData : shop) {
            if(stocksData.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(stocksData);
            }
        }
        mainPagerAdapter.filterList(filteredList, tabPos);
        viewPager.setAdapter(mainPagerAdapter);
    }
}
