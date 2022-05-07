// Copyright 2017 Archos SA
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.archos.mediacenter.video.browser.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;

import androidx.preference.PreferenceManager;

import com.archos.mediacenter.utils.ThumbnailEngine;
import com.archos.mediacenter.video.R;
import com.archos.mediacenter.video.browser.adapters.AdapterDefaultValues;
import com.archos.mediacenter.video.browser.adapters.AdapterDefaultValuesGrid;
import com.archos.mediacenter.video.browser.adapters.object.Episode;
import com.archos.mediacenter.video.browser.adapters.object.Video;
import com.archos.mediacenter.video.player.PlayerActivity;
import com.archos.mediacenter.video.player.TextShadowSpan;
import com.archos.mediacenter.video.player.tvmenu.TVUtils;

import httpimage.HttpImageManager;

/**
 * Created by alexandre on 26/10/15.
 */
public class VideoGridPresenter extends VideoPresenter{
    private final ExtendedClickListener mOnExtendedClick;
    private final TextShadowSpan mTextNoShadowSpan;
    private static final String ITALIC = "</i>";
    private final boolean mIsTablet;
    private SpannableStringBuilder mSpannableStringBuilder;
    public VideoGridPresenter(Context context, ExtendedClickListener onExtendedClick, HttpImageManager imageManager) {
        this(context, AdapterDefaultValuesGrid.INSTANCE,  onExtendedClick, imageManager);

    }
    protected VideoGridPresenter(Context context, AdapterDefaultValues defaultValues, ExtendedClickListener onExtendedClick, HttpImageManager imageManager) {
        super(context, defaultValues,  onExtendedClick,imageManager);
        mOnExtendedClick = onExtendedClick;
        // Tablet is Nexus7 and larger
        mIsTablet = mContext.getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)|| TVUtils.isTV(mContext);
        mSpannableStringBuilder = new SpannableStringBuilder();
        mTextNoShadowSpan = new TextShadowSpan();
    }


    @Override
    public View bindView(View view, final Object object, ThumbnailEngine.Result thumbnailResult, int positionInAdapter) {
        super.bindView(view, object, thumbnailResult, positionInAdapter);
        ViewHolder holder = (ViewHolder) view.getTag();
        final Video video = (Video) object;

        String nameGrid =video.getName();

        if(video instanceof Episode){
            Episode episode = (Episode) video;
            nameGrid += " S"+episode.getSeasonNumber()+"E"+episode.getEpisodeNumber();
        }

        holder.name.setText(nameGrid);
        // Display "show name S##E##" and make sure S##E##
        // is visible.
        if (holder.name!=null&&video instanceof Episode) {
            holder.name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }

        //set gridview thumbnail Width & Height
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean drawerIsNull = prefs.getBoolean("drawerIsNull", true);
        boolean mIsLandscapeMode = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean mIsPortraitMode = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        //width subtraction when number of columns is 5 && mIsLandscapeMode && drawerIsNull
        int categoryWidth = (int) mContext.getResources().getDimension(R.dimen.categories_list_width);
        int TotalHorizontalSpacingLandscapeNullDrawer = (int) mContext.getResources().getDimension(R.dimen.total_horizontal_spacing_landscape_null_drawer);
        int subtraction = categoryWidth + TotalHorizontalSpacingLandscapeNullDrawer;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowWidth = displayMetrics.widthPixels;
        int TotalHorizontalSpacingPortrait = (int) mContext.getResources().getDimension(R.dimen.total_horizontal_spacing_portrait);
        int TotalHorizontalSpacingLandscape = (int) mContext.getResources().getDimension(R.dimen.total_horizontal_spacing_landscape);
        int width;
        if(mIsPortraitMode){
            width = windowWidth - TotalHorizontalSpacingPortrait;
        }else if(mIsLandscapeMode && drawerIsNull){
            width = windowWidth - subtraction;
        }else{
            width = windowWidth - TotalHorizontalSpacingLandscape;
        }
        int columnWidth;
        if(mIsPortraitMode){
            columnWidth = width / 3 ;
        }else if(mIsLandscapeMode && drawerIsNull){
            columnWidth = width / 5 ;
        }else{
            columnWidth = width / 6 ;
        }
        int height = columnWidth / 2;
        int columnHeight = height * 3;
        holder.thumbnail.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight));

        int resumePosition = video.getRemoteResumeMs()>0?video.getRemoteResumeMs():video.getResumeMs();
        boolean resume = resumePosition>0||resumePosition == PlayerActivity.LAST_POSITION_END;


        boolean hideGridviewInfo = prefs.getBoolean("hide_gridview_info", false);
        int bottomPadding = (int) mContext.getResources().getDimension(R.dimen.gridview_root_bottom_padding);
        if(hideGridviewInfo){
            holder.ItemViewRoot.setPadding(0,0,0,0);
            holder.secondLine.setVisibility(View.GONE);
            holder.name.setVisibility(View.GONE);
            holder.resume.setVisibility(View.GONE);
            holder.EmptyProgress.setVisibility(View.GONE);
        }else{
            holder.ItemViewRoot.setPadding(0,0,0,bottomPadding);
            holder.secondLine.setVisibility(View.VISIBLE);
            holder.name.setVisibility(View.VISIBLE);
            if (resume&&holder.resume!=null) {
                int duration = video.getDurationMs();
                duration = duration > 0 ? duration : resumePosition>0&&resumePosition<=100? 100 : 0;//resume can now be a percentage
                boolean displayProgressSlider = !mIsTablet&&(duration>0 ||resumePosition == PlayerActivity.LAST_POSITION_END); // Display the progress bar if we know the duration
                setResume(displayProgressSlider,duration > 0 ? duration : 100, resumePosition, holder.resume);

                holder.EmptyProgress.setVisibility(View.GONE);

            } else if(holder.resume!=null){
                // Show disabled video icon (there is no such disabled resume slider)
                holder.resume.setVisibility(View.GONE);
                holder.EmptyProgress.setVisibility(View.VISIBLE);

            }
        }

        return view;
    }


}
