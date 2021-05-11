package com.luck.picture.lib;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.decoration.WrapContentLinearLayoutManager;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.AttrsUtils;

/**
 * @Author: YuChuan
 * @CreateDate: 2020/12/11 16:00
 * @Description:
 */
public class PictureSelectorPreviewMotobandStyleActivity extends PicturePreviewActivity {
    /**
     * alpha duration
     */
    private final static int ALPHA_DURATION = 300;
    private View bottomLine;
    private TextView mTvSelected;

    @Override
    public int getResourceId() {
        return R.layout.picture_motoband_style_preview;
    }

    private void goneParent() {
        if (tvMediaNum.getVisibility() == View.VISIBLE) {
            tvMediaNum.setVisibility(View.GONE);
        }
        if (mTvPictureOk.getVisibility() == View.VISIBLE) {
            mTvPictureOk.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(check.getText())) {
            check.setText("");
        }
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        goneParent();
        bottomLine = findViewById(R.id.bottomLine);
        mTvPictureRight.setVisibility(View.VISIBLE);
        mTvPictureRight.setText(getString(R.string.picture_send));
        mCbOriginal.setTextSize(14);
        mTvSelected = findViewById(R.id.tv_selected);
        mTvPictureRight.setOnClickListener(this);
        WrapContentLinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(getContext());
        layoutManager.setOrientation(WrapContentLinearLayoutManager.HORIZONTAL);
        if (isBottomPreview) {
            if (selectData != null && selectData.size() > position) {
                int size = selectData.size();
                for (int i = 0; i < size; i++) {
                    LocalMedia media = selectData.get(i);
                    media.setChecked(false);
                }
                LocalMedia media = selectData.get(position);
                media.setChecked(true);
            }
        } else {
            int size = selectData != null ? selectData.size() : 0;
            for (int i = 0; i < size; i++) {
                LocalMedia media = selectData.get(i);
                if (isEqualsDirectory(media.getParentFolderName(), currentDirectory)) {
                    media.setChecked(isShowCamera ? media.position - 1 == position : media.position == position);
                }
            }
        }
    }

    /**
     * Is it the same directory
     *
     * @param parentFolderName
     * @param currentDirectory
     * @return
     */
    private boolean isEqualsDirectory(String parentFolderName, String currentDirectory) {
        return isBottomPreview
                || TextUtils.isEmpty(parentFolderName)
                || TextUtils.isEmpty(currentDirectory)
                || currentDirectory.equals(getString(R.string.picture_camera_roll))
                || parentFolderName.equals(currentDirectory);
    }

    @Override
    public void initPictureSelectorStyle() {
        super.initPictureSelectorStyle();
        if (PictureSelectionConfig.uiStyle != null) {
            if (!TextUtils.isEmpty(PictureSelectionConfig.uiStyle.picture_top_titleRightDefaultText)) {
                mTvPictureRight.setText(PictureSelectionConfig.uiStyle.picture_top_titleRightDefaultText);
            }
            if (PictureSelectionConfig.uiStyle.picture_top_titleRightTextNormalBackground != 0) {
                mTvPictureRight.setBackgroundResource(PictureSelectionConfig.uiStyle.picture_top_titleRightTextNormalBackground);
            } else {
                mTvPictureRight.setBackgroundResource(R.drawable.picture_send_button_bg);
            }
            if (PictureSelectionConfig.uiStyle.picture_top_titleRightTextSize != 0) {
                mTvPictureRight.setTextSize(PictureSelectionConfig.uiStyle.picture_top_titleRightTextSize);
            }
            if (!TextUtils.isEmpty(PictureSelectionConfig.uiStyle.picture_bottom_selectedText)) {
                mTvSelected.setText(PictureSelectionConfig.uiStyle.picture_bottom_selectedText);
            }
            if (PictureSelectionConfig.uiStyle.picture_bottom_selectedTextSize != 0) {
                mTvSelected.setTextSize(PictureSelectionConfig.uiStyle.picture_bottom_selectedTextSize);
            }
            if (PictureSelectionConfig.uiStyle.picture_bottom_selectedTextColor != 0) {
                mTvSelected.setTextColor(PictureSelectionConfig.uiStyle.picture_bottom_selectedTextColor);
            }
            if (PictureSelectionConfig.uiStyle.picture_bottom_barBackgroundColor != 0) {
                selectBarLayout.setBackgroundColor(PictureSelectionConfig.uiStyle.picture_bottom_barBackgroundColor);
            } else {
                selectBarLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.picture_color_half_grey));
            }

            ColorStateList colorStateList = AttrsUtils.getColorStateList(PictureSelectionConfig.uiStyle.picture_top_titleRightTextColor);
            if (colorStateList != null) {
                mTvPictureRight.setTextColor(colorStateList);
            }

            if (PictureSelectionConfig.uiStyle.picture_bottom_selectedCheckStyle != 0) {
                check.setBackgroundResource(PictureSelectionConfig.uiStyle.picture_bottom_selectedCheckStyle);
            } else {
                check.setBackgroundResource(R.drawable.picture_wechat_select_cb);
            }

            if (PictureSelectionConfig.uiStyle.picture_top_leftBack != 0) {
                pictureLeftBack.setImageResource(PictureSelectionConfig.uiStyle.picture_top_leftBack);
            } else {
                pictureLeftBack.setImageResource(R.drawable.picture_icon_back);
            }

            if (config.isOriginalControl) {
                if (!TextUtils.isEmpty(PictureSelectionConfig.uiStyle.picture_bottom_originalPictureText)) {
                    mCbOriginal.setText(PictureSelectionConfig.uiStyle.picture_bottom_originalPictureText);
                } else {
                    mCbOriginal.setText(getString(R.string.picture_original_image));
                }
                if (PictureSelectionConfig.uiStyle.picture_bottom_originalPictureTextSize != 0) {
                    mCbOriginal.setTextSize(PictureSelectionConfig.uiStyle.picture_bottom_originalPictureTextSize);
                } else {
                    mCbOriginal.setTextSize(14);
                }
                if (PictureSelectionConfig.uiStyle.picture_bottom_originalPictureTextColor != 0) {
                    mCbOriginal.setTextColor(PictureSelectionConfig.uiStyle.picture_bottom_originalPictureTextColor);
                } else {
                    mCbOriginal.setTextColor(Color.parseColor("#FFFFFF"));
                }
                if (PictureSelectionConfig.uiStyle.picture_bottom_originalPictureCheckStyle != 0) {
                    mCbOriginal.setButtonDrawable(PictureSelectionConfig.uiStyle.picture_bottom_originalPictureCheckStyle);
                } else {
                    mCbOriginal.setButtonDrawable(R.drawable.picture_original_wechat_checkbox);
                }
            }
        } else if (PictureSelectionConfig.style != null) {
            if (PictureSelectionConfig.style.pictureCompleteBackgroundStyle != 0) {
                mTvPictureRight.setBackgroundResource(PictureSelectionConfig.style.pictureCompleteBackgroundStyle);
            } else {
                mTvPictureRight.setBackgroundResource(R.drawable.picture_send_button_bg);
            }
            if (PictureSelectionConfig.style.pictureRightTextSize != 0) {
                mTvPictureRight.setTextSize(PictureSelectionConfig.style.pictureRightTextSize);
            }
            if (!TextUtils.isEmpty(PictureSelectionConfig.style.pictureWeChatPreviewSelectedText)) {
                mTvSelected.setText(PictureSelectionConfig.style.pictureWeChatPreviewSelectedText);
            }
            if (PictureSelectionConfig.style.pictureWeChatPreviewSelectedTextSize != 0) {
                mTvSelected.setTextSize(PictureSelectionConfig.style.pictureWeChatPreviewSelectedTextSize);
            }
            if (PictureSelectionConfig.style.picturePreviewBottomBgColor != 0) {
                selectBarLayout.setBackgroundColor(PictureSelectionConfig.style.picturePreviewBottomBgColor);
            } else {
                selectBarLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.picture_color_half_grey));
            }
            if (PictureSelectionConfig.style.pictureCompleteTextColor != 0) {
                mTvPictureRight.setTextColor(PictureSelectionConfig.style.pictureCompleteTextColor);
            } else {
                if (PictureSelectionConfig.style.pictureCancelTextColor != 0) {
                    mTvPictureRight.setTextColor(PictureSelectionConfig.style.pictureCancelTextColor);
                } else {
                    mTvPictureRight.setTextColor(ContextCompat.getColor(getContext(), R.color.picture_color_white));
                }
            }
            if (PictureSelectionConfig.style.pictureOriginalFontColor == 0) {
                mCbOriginal.setTextColor(ContextCompat
                        .getColor(this, R.color.picture_color_white));
            }
            if (PictureSelectionConfig.style.pictureWeChatChooseStyle != 0) {
                check.setBackgroundResource(PictureSelectionConfig.style.pictureWeChatChooseStyle);
            } else {
                check.setBackgroundResource(R.drawable.picture_wechat_select_cb);
            }
            if (config.isOriginalControl) {
                if (PictureSelectionConfig.style.pictureOriginalControlStyle == 0) {
                    mCbOriginal.setButtonDrawable(ContextCompat
                            .getDrawable(this, R.drawable.picture_original_wechat_checkbox));
                }
            }
            if (PictureSelectionConfig.style.pictureWeChatLeftBackStyle != 0) {
                pictureLeftBack.setImageResource(PictureSelectionConfig.style.pictureWeChatLeftBackStyle);
            } else {
                pictureLeftBack.setImageResource(R.drawable.picture_icon_back);
            }
            if (!TextUtils.isEmpty(PictureSelectionConfig.style.pictureUnCompleteText)) {
                mTvPictureRight.setText(PictureSelectionConfig.style.pictureUnCompleteText);
            }
        } else {
            mTvPictureRight.setBackgroundResource(R.drawable.picture_send_button_bg);
            mTvPictureRight.setTextColor(ContextCompat.getColor(getContext(), R.color.picture_color_white));
            selectBarLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.picture_color_half_grey));
            check.setBackgroundResource(R.drawable.picture_wechat_select_cb);
            pictureLeftBack.setImageResource(R.drawable.picture_icon_back);
            mCbOriginal.setTextColor(ContextCompat
                    .getColor(this, R.color.picture_color_white));
            if (config.isOriginalControl) {
                mCbOriginal.setButtonDrawable(ContextCompat
                        .getDrawable(this, R.drawable.picture_original_wechat_checkbox));
            }
        }

        onSelectNumChange(false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.picture_right) {
            boolean enable = selectData.size() != 0;
            if (enable) {
                mTvPictureOk.performClick();
            } else {
                btnCheck.performClick();
                boolean isNewEnableStatus = selectData.size() != 0;
                if (isNewEnableStatus) {
                    mTvPictureOk.performClick();
                }
            }
        }
    }

    @Override
    protected void onPageSelectedChange(LocalMedia media) {
        super.onPageSelectedChange(media);
        goneParent();
    }

    @Override
    protected void onSelectNumChange(boolean isRefresh) {
        goneParent();
        boolean enable = selectData != null && selectData.size() != 0;
        mTvPictureRight.setEnabled(enable);
        if (enable) {
            initCompleteText(selectData.size());
            if (PictureSelectionConfig.uiStyle != null) {
                if (PictureSelectionConfig.uiStyle.picture_top_titleRightTextNormalBackground != 0) {
                    mTvPictureRight.setBackgroundResource(PictureSelectionConfig.uiStyle.picture_top_titleRightTextNormalBackground);
                } else {
                    mTvPictureRight.setBackgroundResource(R.drawable.picture_send_button_bg);
                }
            } else if (PictureSelectionConfig.style != null) {
                if (PictureSelectionConfig.style.pictureCompleteTextColor != 0) {
                    mTvPictureRight.setTextColor(PictureSelectionConfig.style.pictureCompleteTextColor);
                }
                if (PictureSelectionConfig.style.pictureCompleteBackgroundStyle != 0) {
                    mTvPictureRight.setBackgroundResource(PictureSelectionConfig.style.pictureCompleteBackgroundStyle);
                }
            } else {
                mTvPictureRight.setTextColor(ContextCompat.getColor(getContext(), R.color.picture_color_white));
                mTvPictureRight.setBackgroundResource(R.drawable.picture_send_button_bg);
            }
        } else {
            if (PictureSelectionConfig.uiStyle != null) {
                if (PictureSelectionConfig.uiStyle.picture_top_titleRightTextDefaultBackground != 0) {
                    mTvPictureRight.setBackgroundResource(PictureSelectionConfig.uiStyle.picture_top_titleRightTextDefaultBackground);
                } else {
                    mTvPictureRight.setBackgroundResource(R.drawable.picture_send_button_default_bg);
                }
                if (!TextUtils.isEmpty(PictureSelectionConfig.uiStyle.picture_top_titleRightDefaultText)) {
                    mTvPictureRight.setText(PictureSelectionConfig.uiStyle.picture_top_titleRightDefaultText);
                } else {
                    mTvPictureRight.setText(getString(R.string.picture_send));
                }
            } else if (PictureSelectionConfig.style != null && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureUnCompleteText)) {
                mTvPictureRight.setText(PictureSelectionConfig.style.pictureUnCompleteText);
            } else {
                mTvPictureRight.setText(getString(R.string.picture_send));
            }
            bottomLine.animate().alpha(0).setDuration(ALPHA_DURATION).setInterpolator(new AccelerateInterpolator());
            bottomLine.setVisibility(View.GONE);
        }
    }


    /**
     * initCompleteText
     */
    @Override
    protected void initCompleteText(int startCount) {
        boolean isNotEmptyStyle = PictureSelectionConfig.style != null;
        if (config.isWithVideoImage) {
            // 混选模式
            if (config.selectionMode == PictureConfig.SINGLE) {
                if (startCount <= 0) {
                    mTvPictureRight.setText(isNotEmptyStyle && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureUnCompleteText)
                            ? PictureSelectionConfig.style.pictureUnCompleteText : getString(R.string.picture_send));
                } else {
                    boolean isCompleteReplaceNum = isNotEmptyStyle && PictureSelectionConfig.style.isCompleteReplaceNum;
                    if (isCompleteReplaceNum && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureCompleteText)) {
                        mTvPictureRight.setText(String.format(PictureSelectionConfig.style.pictureCompleteText, selectData.size(), 1));
                    } else {
                        mTvPictureRight.setText(isNotEmptyStyle && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureCompleteText)
                                ? PictureSelectionConfig.style.pictureCompleteText : getString(R.string.picture_send));
                    }
                }
            } else {
                boolean isCompleteReplaceNum = isNotEmptyStyle && PictureSelectionConfig.style.isCompleteReplaceNum;
                if (isCompleteReplaceNum && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureCompleteText)) {
                    mTvPictureRight.setText(String.format(PictureSelectionConfig.style.pictureCompleteText,
                            selectData.size(), config.maxSelectNum));
                } else {
                    mTvPictureRight.setText(isNotEmptyStyle && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureUnCompleteText)
                            ? PictureSelectionConfig.style.pictureUnCompleteText : getString(R.string.picture_send_num, selectData.size(),
                            config.maxSelectNum));
                }
            }
        } else {
            String mimeType = selectData.get(0).getMimeType();
            int maxSize = PictureMimeType.isHasVideo(mimeType) && config.maxVideoSelectNum > 0 ? config.maxVideoSelectNum : config.maxSelectNum;
            if (config.selectionMode == PictureConfig.SINGLE) {
                if (startCount <= 0) {
                    mTvPictureRight.setText(isNotEmptyStyle && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureUnCompleteText)
                            ? PictureSelectionConfig.style.pictureUnCompleteText : getString(R.string.picture_send));
                } else {
                    boolean isCompleteReplaceNum = isNotEmptyStyle && PictureSelectionConfig.style.isCompleteReplaceNum;
                    if (isCompleteReplaceNum && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureCompleteText)) {
                        mTvPictureRight.setText(String.format(PictureSelectionConfig.style.pictureCompleteText, selectData.size(),
                                1));
                    } else {
                        mTvPictureRight.setText(isNotEmptyStyle && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureCompleteText)
                                ? PictureSelectionConfig.style.pictureCompleteText : getString(R.string.picture_send));
                    }
                }
            } else {
                boolean isCompleteReplaceNum = isNotEmptyStyle && PictureSelectionConfig.style.isCompleteReplaceNum;
                if (isCompleteReplaceNum && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureCompleteText)) {
                    mTvPictureRight.setText(String.format(PictureSelectionConfig.style.pictureCompleteText, selectData.size(), maxSize));
                } else {
                    mTvPictureRight.setText(isNotEmptyStyle && !TextUtils.isEmpty(PictureSelectionConfig.style.pictureUnCompleteText)
                            ? PictureSelectionConfig.style.pictureUnCompleteText
                            : getString(R.string.picture_send_num, selectData.size(), maxSize));
                }
            }
        }
    }
}
