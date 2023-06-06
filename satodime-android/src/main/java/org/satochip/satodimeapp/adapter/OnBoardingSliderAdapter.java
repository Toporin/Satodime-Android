package org.satochip.satodimeapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.ui.activity.OnBoardingActivity;


public class OnBoardingSliderAdapter extends PagerAdapter {

    Activity context;
    LayoutInflater layoutInflater;
    public String[] slideHeadings;
    public String[] slideDescriptions;
    public int[] slideImages;

    public OnBoardingSliderAdapter(OnBoardingActivity context, String[] slideDescriptions, String[] slideHeadings, int[] slideImages) {
        this.context=context;
        this.slideDescriptions=slideDescriptions;
        this.slideHeadings=slideHeadings;
        this.slideImages=slideImages;
    }

//    public int[] slideImages = {
//            R.drawable.ic_onboarding_1,
//            R.drawable.ic_onboarding_2,
//            R.drawable.ic_onboarding_3,
//            R.drawable.onboarding_4,
//    };

    @Override
    public int getCount() {
        return slideHeadings.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view ==  object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.onboarding_slide_layout, container, false);

        ImageView logoImageView = (ImageView) view.findViewById(R.id.iv_app_logo);
        ImageView slideImageView = (ImageView) view.findViewById(R.id.iv_image_icon);
        TextView slideHeading = (TextView) view.findViewById(R.id.tv_heading);
        TextView slideDescription = (TextView) view.findViewById(R.id.tv_description);

        logoImageView.setImageResource(R.drawable.app_logo_horizontal);
        slideImageView.setImageResource(slideImages[position]);
        slideHeading.setText(slideHeadings[position]);
        slideDescription.setText(slideDescriptions[position]);

        container.addView(view);

        return view;

    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

}
