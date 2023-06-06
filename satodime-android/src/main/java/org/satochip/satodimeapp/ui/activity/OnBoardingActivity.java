package org.satochip.satodimeapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.adapter.OnBoardingSliderAdapter;


public class OnBoardingActivity extends AppCompatActivity implements View.OnClickListener {
    private ViewPager mSlideViewPager;
    private LinearLayout mDotsLayout;

    private TextView[] mDots;

    private OnBoardingSliderAdapter onBoardingSliderAdapter;

    private ImageView buttonNext;
    private TextView btnSkip;


    private int mCurrentPage;
    public static boolean isDark = false;
    TextView privacy_policy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        mSlideViewPager = findViewById(R.id.slide_viewpager);
        mDotsLayout = findViewById(R.id.dots_layout);
        buttonNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);

        String[] slideDescriptions = {
                getString(R.string.text_onboarding_1),
                getString(R.string.text_onboarding_2),
                getString(R.string.text_onboarding_3),
                getString(R.string.text_onboarding_4)
        };

        String[] slideHeadings = {
                getString(R.string.title_onboarding_1),
                getString(R.string.title_onboarding_2),
                getString(R.string.title_onboarding_3),
                getString(R.string.title_onboarding_4)
        };

        int[] slideImages = {
                R.drawable.onboarding_1,
                R.drawable.onboarding_2,
                R.drawable.onboarding_3,
                R.drawable.onboarding_4_white,
        };

        //onBoardingSliderAdapter = new OnBoardingSliderAdapter(OnBoardingActivity.this,slideDescriptions,slideHeadings);
        onBoardingSliderAdapter = new OnBoardingSliderAdapter(OnBoardingActivity.this,slideDescriptions,slideHeadings,slideImages);

        mSlideViewPager.setAdapter(onBoardingSliderAdapter);

        addDotsIndicator(0);


        mSlideViewPager.addOnPageChangeListener(viewListener);

        buttonNext.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
    }

    public void addDotsIndicator(int position) {
        mDots = new TextView[4];
        mDotsLayout.removeAllViews(); //without this multiple number of dots will be created

        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;")); //code for the dot icon like thing
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.grey));

            mDotsLayout.addView(mDots[i]);
        }

        if (mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.gold_color)); //setting currently selected dot to white
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            addDotsIndicator(position);

            mCurrentPage = position;

            if (position == 0) {//we are on first page
//                buttonNext.setEnabled(true);
//                buttonNext.setText("Next");
            } else if (position == mDots.length - 1) { //last page
//                buttonNext.setEnabled(true);
            } else { //neither on first nor on last page
//                buttonNext.setEnabled(true);
//                buttonNext.setText("Next");
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                if (mCurrentPage != 3) {
                    mSlideViewPager.setCurrentItem(mCurrentPage + 1);
                } else {
                    startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
                    finish();
                }
                break;
            case R.id.btn_skip:
                startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
                finish();
                break;
            default:
                break;
        }
    }
}