package com.luck.picture.lib.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.dialog.PictureCustomDialog;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnPhotoSelectChangedListener;
import com.luck.picture.lib.tools.AnimUtils;
import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.MediaUtils;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.luck.picture.lib.tools.StringUtils;
import com.luck.picture.lib.tools.ToastUtils;
import com.luck.picture.lib.tools.VoiceUtils;
import com.luck.picture.lib.widget.SquareRelativeLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author：luck
 * @date：2016-12-30 12:02
 * @describe：图片列表
 */
public class PictureImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private boolean showCamera;
    private OnPhotoSelectChangedListener imageSelectChangedListener;
    private List<LocalMedia> data = new ArrayList<>();
    private List<LocalMedia> selectData = new ArrayList<>();
    private PictureSelectionConfig config;

    public PictureImageGridAdapter(Context context, PictureSelectionConfig config) {
        this.context = context;
        this.config = config;
        this.showCamera = config.isCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    /**
     * 全量刷新
     *
     * @param data
     */
    public void bindData(List<LocalMedia> data) {
        this.data = data == null ? new ArrayList<>() : data;
        this.notifyDataSetChanged();
    }

    public void bindSelectData(List<LocalMedia> images) {
        // 这里重新构构造一个新集合，不然会产生已选集合一变，结果集合也会添加的问题
        List<LocalMedia> selection = new ArrayList<>();
        int size = images.size();
        for (int i = 0; i < size; i++) {
            LocalMedia media = images.get(i);
            selection.add(media);
        }
        this.selectData = selection;
        if (!config.isSingleDirectReturn) {
            subSelectPosition();
            if (imageSelectChangedListener != null) {
                imageSelectChangedListener.onChange(selectData);
            }
        }
    }

    public List<LocalMedia> getSelectedData() {
        return selectData == null ? new ArrayList<>() : selectData;
    }

    public int getSelectedSize() {
        return selectData == null ? 0 : selectData.size();
    }

    public List<LocalMedia> getData() {
        return data == null ? new ArrayList<>() : data;
    }

    public boolean isDataEmpty() {
        return data == null || data.size() == 0;
    }


    public void clear() {
        if (getSize() > 0) {
            data.clear();
        }
    }

    public int getSize() {
        return data == null ? 0 : data.size();
    }

    public LocalMedia getItem(int position) {
        return getSize() > 0 ? data.get(position) : null;
    }


    @Override
    public int getItemViewType(int position) {
        if (showCamera && position == 0) {
            return PictureConfig.TYPE_CAMERA;
        } else {
            return PictureConfig.TYPE_PICTURE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == PictureConfig.TYPE_CAMERA) {
            View view = LayoutInflater.from(context).inflate(R.layout.picture_item_camera, parent, false);
            return new CameraViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.picture_image_grid_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == PictureConfig.TYPE_CAMERA) {
            CameraViewHolder headerHolder = (CameraViewHolder) holder;
            headerHolder.headerView.setOnClickListener(v -> {
                if (imageSelectChangedListener != null) {
                    imageSelectChangedListener.onTakePhoto();
                }
            });
        } else {
            final ViewHolder contentHolder = (ViewHolder) holder;
            final LocalMedia image = data.get(showCamera ? position - 1 : position);
            image.position = contentHolder.getAdapterPosition();
            final String path = image.getPath();
            final String mimeType = image.getMimeType();
            if (config.checkNumMode) {
                notifyCheckChanged(contentHolder, image);
            }
            if (config.isSingleDirectReturn) {
                contentHolder.tvCheck.setVisibility(View.GONE);
                contentHolder.btnCheck.setVisibility(View.GONE);
            } else {
                selectImage(contentHolder, isSelected(image));
                contentHolder.tvCheck.setVisibility(View.VISIBLE);
                contentHolder.btnCheck.setVisibility(View.VISIBLE);
                // 启用了蒙层效果
                if (config.isMaxSelectEnabledMask) {
                    dispatchHandleMask(contentHolder, image);
                }
            }
            contentHolder.tvIsGif.setVisibility(PictureMimeType.isGif(mimeType) ? View.VISIBLE : View.GONE);
            if (PictureMimeType.isHasImage(image.getMimeType())) {
                if (image.loadLongImageStatus == PictureConfig.NORMAL) {
                    image.isLongImage = MediaUtils.isLongImg(image);
                    image.loadLongImageStatus = PictureConfig.LOADED;
                }
                contentHolder.tvLongChart.setVisibility(image.isLongImage ? View.VISIBLE : View.GONE);
            } else {
                image.loadLongImageStatus = PictureConfig.NORMAL;
                contentHolder.tvLongChart.setVisibility(View.GONE);
            }
            boolean isHasVideo = PictureMimeType.isHasVideo(mimeType);
            if (isHasVideo || PictureMimeType.isHasAudio(mimeType)) {
                contentHolder.tvDuration.setVisibility(View.VISIBLE);
                contentHolder.tvDuration.setText(DateUtils.formatDurationTime(image.getDuration()));
                contentHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds
                        (isHasVideo ? R.drawable.picture_icon_video : R.drawable.picture_icon_audio,
                                0, 0, 0);
            } else {
                contentHolder.tvDuration.setVisibility(View.GONE);
            }
            if (config.chooseMode == PictureMimeType.ofAudio()) {
                contentHolder.ivPicture.setImageResource(R.drawable.picture_audio_placeholder);
            } else {
                if (PictureSelectionConfig.imageEngine != null) {
                    PictureSelectionConfig.imageEngine.loadGridImage(context, path, contentHolder.ivPicture);
                }
            }

            if (config.enablePreview || config.enPreviewVideo || config.enablePreviewAudio) {
                contentHolder.btnCheck.setOnClickListener(v -> {
                    if (config.isMaxSelectEnabledMask) {
                        if (!contentHolder.tvCheck.isSelected() && getSelectedSize() >= config.maxSelectNum) {
                            String msg = StringUtils.getMsg(context, config.chooseMode == PictureMimeType.ofAll() ? null : image.getMimeType(), config.maxSelectNum);
                            showPromptDialog(msg);
                            return;
                        }
                    }
                    // 如原图路径不存在或者路径存在但文件不存在
                    String newPath = image.getRealPath();
                    if (!TextUtils.isEmpty(newPath) && !new File(newPath).exists()) {
                        ToastUtils.s(context, PictureMimeType.s(context, mimeType));
                        return;
                    }
                    // 如果有旋转信息图片宽高则是相反
                    MediaUtils.setOrientationAsynchronous(context, image, config.isAndroidQChangeWH, null);
                    changeCheckboxState(contentHolder, image);
                });
            }
            contentHolder.contentView.setOnClickListener(v -> {
                if (config.isMaxSelectEnabledMask) {
                    if (image.isMaxSelectEnabledMask()) {
                        return;
                    }
                }
                // 如原图路径不存在或者路径存在但文件不存在
                String newPath = image.getRealPath();
                if (!TextUtils.isEmpty(newPath) && !new File(newPath).exists()) {
                    ToastUtils.s(context, PictureMimeType.s(context, mimeType));
                    return;
                }
                int index = showCamera ? position - 1 : position;
                if (index == -1) {
                    return;
                }
                // 如果有旋转信息图片宽高则是相反
                MediaUtils.setOrientationAsynchronous(context, image, config.isAndroidQChangeWH, null);
                boolean eqResult =
                        PictureMimeType.isHasImage(mimeType) && config.enablePreview
                                || PictureMimeType.isHasVideo(mimeType) && (config.enPreviewVideo
                                || config.selectionMode == PictureConfig.SINGLE)
                                || PictureMimeType.isHasAudio(mimeType) && (config.enablePreviewAudio
                                || config.selectionMode == PictureConfig.SINGLE);
                if (eqResult) {
                    if (PictureMimeType.isHasVideo(image.getMimeType())) {
                        if (config.videoMinSecond > 0 && image.getDuration() < config.videoMinSecond) {
                            // 视频小于最低指定的长度
                            showPromptDialog(context.getString(R.string.picture_choose_min_seconds, config.videoMinSecond / 1000));
                            return;
                        }
                        if (config.videoMaxSecond > 0 && image.getDuration() > config.videoMaxSecond) {
                            // 视频时长超过了指定的长度
                            showPromptDialog(context.getString(R.string.picture_choose_max_seconds, config.videoMaxSecond / 1000));
                            return;
                        }
                    }
                    imageSelectChangedListener.onPictureClick(image, index);
                } else {
                    changeCheckboxState(contentHolder, image);
                }
            });
        }
    }

    /**
     * 处理蒙版效果
     *
     * @param contentHolder
     * @param item
     */
    private void dispatchHandleMask(ViewHolder contentHolder, LocalMedia item) {
        if (config.isWithVideoImage && config.maxVideoSelectNum > 0) {
            if (getSelectedSize() >= config.maxSelectNum) {
                boolean isSelected = contentHolder.tvCheck.isSelected();
                contentHolder.ivPicture.setColorFilter(ContextCompat.getColor
                        (context, isSelected ? R.color.picture_color_80 : R.color.picture_color_half_white), PorterDuff.Mode.SRC_ATOP);
                item.setMaxSelectEnabledMask(!isSelected);
            } else {
                item.setMaxSelectEnabledMask(false);
            }
        } else {
            LocalMedia media = selectData.size() > 0 ? selectData.get(0) : null;
            if (media != null) {
                boolean isSelected = contentHolder.tvCheck.isSelected();
                if (config.chooseMode == PictureMimeType.ofAll()) {
                    if (PictureMimeType.isHasImage(media.getMimeType())) {
                        // 所有视频不可选
                        if (!isSelected && !PictureMimeType.isHasImage(item.getMimeType())) {
                            contentHolder.ivPicture.setColorFilter(ContextCompat.getColor
                                    (context, PictureMimeType.isHasVideo(item.getMimeType()) ? R.color.picture_color_half_white : R.color.picture_color_20), PorterDuff.Mode.SRC_ATOP);
                        }
                        item.setMaxSelectEnabledMask(PictureMimeType.isHasVideo(item.getMimeType()));
                    } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                        // 所有图片不可选
                        if (!isSelected && !PictureMimeType.isHasVideo(item.getMimeType())) {
                            contentHolder.ivPicture.setColorFilter(ContextCompat.getColor
                                    (context, PictureMimeType.isHasImage(item.getMimeType()) ? R.color.picture_color_half_white : R.color.picture_color_20), PorterDuff.Mode.SRC_ATOP);
                        }
                        item.setMaxSelectEnabledMask(PictureMimeType.isHasImage(item.getMimeType()));
                    }
                } else {
                    if (config.chooseMode == PictureMimeType.ofVideo() && config.maxVideoSelectNum > 0) {
                        if (!isSelected && getSelectedSize() == config.maxVideoSelectNum) {
                            contentHolder.ivPicture.setColorFilter(ContextCompat.getColor
                                    (context, R.color.picture_color_half_white), PorterDuff.Mode.SRC_ATOP);
                        }
                        item.setMaxSelectEnabledMask(!isSelected && getSelectedSize() == config.maxVideoSelectNum);
                    } else {
                        if (!isSelected && getSelectedSize() == config.maxSelectNum) {
                            contentHolder.ivPicture.setColorFilter(ContextCompat.getColor
                                    (context, R.color.picture_color_half_white), PorterDuff.Mode.SRC_ATOP);
                        }
                        item.setMaxSelectEnabledMask(!isSelected && getSelectedSize() == config.maxSelectNum);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return showCamera ? data.size() + 1 : data.size();
    }

    public class CameraViewHolder extends RecyclerView.ViewHolder {
        View headerView;
        TextView tvCamera;
        ImageView iv_camera;
        SquareRelativeLayout relativelayout;

        public CameraViewHolder(View itemView) {
            super(itemView);
            headerView = itemView;
            relativelayout = itemView.findViewById(R.id.relativelayout);
            iv_camera = itemView.findViewById(R.id.iv_camera);
            tvCamera = itemView.findViewById(R.id.tvCamera);
            String title = config.chooseMode == PictureMimeType.ofAudio() ?
                    context.getString(R.string.picture_tape)
                    : context.getString(R.string.picture_take_picture);
            tvCamera.setText(title);

            if (config.style != null) {
                if (config.style.pictureFirstIcon != 0) {
                    iv_camera.setImageResource(config.style.pictureFirstIcon);
                }

                if (config.style.pictureFirstTextColor != 0) {
                    tvCamera.setTextColor(config.style.pictureFirstTextColor);
                }

                 if (config.style.pictureFirstBgColor != 0) {
                     relativelayout.setBackgroundColor(config.style.pictureFirstBgColor);
                }


            }

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPicture;
        TextView tvCheck;
        TextView tvDuration, tvIsGif, tvLongChart;
        View contentView;
        View btnCheck;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            ivPicture = itemView.findViewById(R.id.ivPicture);
            tvCheck = itemView.findViewById(R.id.tvCheck);
            btnCheck = itemView.findViewById(R.id.btnCheck);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvIsGif = itemView.findViewById(R.id.tv_isGif);
            tvLongChart = itemView.findViewById(R.id.tv_long_chart);
            if (config.style != null) {
                if (config.style.pictureCheckedStyle != 0) {
                    tvCheck.setBackgroundResource(config.style.pictureCheckedStyle);
                }

                if (config.style.pictureSelectNumberTextColor != 0) {
                    tvCheck.setTextColor(config.style.pictureSelectNumberTextColor);
                }
            }
        }
    }

    public boolean isSelected(LocalMedia image) {
        int size = selectData.size();
        for (int i = 0; i < size; i++) {
            LocalMedia media = selectData.get(i);
            if (media == null || TextUtils.isEmpty(media.getPath())) {
                continue;
            }
            if (media.getPath()
                    .equals(image.getPath())
                    || media.getId() == image.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 选择按钮更新
     */
    private void notifyCheckChanged(ViewHolder viewHolder, LocalMedia imageBean) {
        viewHolder.tvCheck.setText("");
        int size = selectData.size();
        for (int i = 0; i < size; i++) {
            LocalMedia media = selectData.get(i);
            if (media.getPath().equals(imageBean.getPath())
                    || media.getId() == imageBean.getId()) {
                imageBean.setNum(media.getNum());
                media.setPosition(imageBean.getPosition());
                viewHolder.tvCheck.setText(String.valueOf(imageBean.getNum()));
            }
        }
    }

    /**
     * 改变图片选中状态
     *
     * @param contentHolder
     * @param image
     */

    @SuppressLint("StringFormatMatches")
    private void changeCheckboxState(ViewHolder contentHolder, LocalMedia image) {
        boolean isChecked = contentHolder.tvCheck.isSelected();
        int count = selectData.size();
        String mimeType = count > 0 ? selectData.get(0).getMimeType() : "";

        if (config.isWithVideoImage) {
            // 混选模式
            int videoSize = 0;
            int imageSize = 0;
            for (int i = 0; i < count; i++) {
                LocalMedia media = selectData.get(i);
                if (PictureMimeType.isHasVideo(media.getMimeType())) {
                    videoSize++;
                }
            }

            if (PictureMimeType.isHasVideo(image.getMimeType())) {
                if (config.maxVideoSelectNum <= 0) {
                    // 如果视频可选数量是0
                    showPromptDialog(context.getString(R.string.picture_rule));
                    return;
                }

                if (getSelectedSize() >= config.maxSelectNum && !isChecked) {
                    showPromptDialog(context.getString(R.string.picture_message_max_num, config.maxSelectNum));
                    return;
                }

                if (videoSize >= config.maxVideoSelectNum && !isChecked) {
                    // 如果选择的是视频
                    showPromptDialog(StringUtils.getMsg(context, image.getMimeType(), config.maxVideoSelectNum));
                    return;
                }

                if (!isChecked && config.videoMinSecond > 0 && image.getDuration() < config.videoMinSecond) {
                    // 视频小于最低指定的长度
                    showPromptDialog(context.getString(R.string.picture_choose_min_seconds, config.videoMinSecond / 1000));
                    return;
                }

                if (!isChecked && config.videoMaxSecond > 0 && image.getDuration() > config.videoMaxSecond) {
                    // 视频时长超过了指定的长度
                    showPromptDialog(context.getString(R.string.picture_choose_max_seconds, config.videoMaxSecond / 1000));
                    return;
                }
            }

            if (PictureMimeType.isHasImage(image.getMimeType())) {
                if (getSelectedSize() >= config.maxSelectNum && !isChecked) {
                    showPromptDialog(context.getString(R.string.picture_message_max_num, config.maxSelectNum));
                    return;
                }
            }

        } else {
            // 非混选模式
            if (!TextUtils.isEmpty(mimeType)) {
                boolean mimeTypeSame = PictureMimeType.isMimeTypeSame(mimeType, image.getMimeType());
                if (!mimeTypeSame) {
                    showPromptDialog(context.getString(R.string.picture_rule));
                    return;
                }
            }
            if (PictureMimeType.isHasVideo(mimeType) && config.maxVideoSelectNum > 0) {
                if (count >= config.maxVideoSelectNum && !isChecked) {
                    // 如果先选择的是视频
                    showPromptDialog(StringUtils.getMsg(context, mimeType, config.maxVideoSelectNum));
                    return;
                }
                if (!isChecked && config.videoMinSecond > 0 && image.getDuration() < config.videoMinSecond) {
                    // 视频小于最低指定的长度
                    showPromptDialog(context.getString(R.string.picture_choose_min_seconds, config.videoMinSecond / 1000));
                    return;
                }

                if (!isChecked && config.videoMaxSecond > 0 && image.getDuration() > config.videoMaxSecond) {
                    // 视频时长超过了指定的长度
                    showPromptDialog(context.getString(R.string.picture_choose_max_seconds, config.videoMaxSecond / 1000));
                    return;
                }
            } else {
                if (count >= config.maxSelectNum && !isChecked) {
                    ToastUtils.s(context, StringUtils.getMsg(context, mimeType, config.maxSelectNum));
                    return;
                }
                if (PictureMimeType.isHasVideo(image.getMimeType())) {
                    if (!isChecked && config.videoMinSecond > 0 && image.getDuration() < config.videoMinSecond) {
                        // 视频小于最低指定的长度
                        showPromptDialog(context.getString(R.string.picture_choose_min_seconds, config.videoMinSecond / 1000));
                        return;
                    }

                    if (!isChecked && config.videoMaxSecond > 0 && image.getDuration() > config.videoMaxSecond) {
                        // 视频时长超过了指定的长度
                        showPromptDialog(context.getString(R.string.picture_choose_max_seconds, config.videoMaxSecond / 1000));
                        return;
                    }
                }
            }
        }

        if (isChecked) {
            for (int i = 0; i < count; i++) {
                LocalMedia media = selectData.get(i);
                if (media == null || TextUtils.isEmpty(media.getPath())) {
                    continue;
                }
                if (media.getPath().equals(image.getPath())
                        || media.getId() == image.getId()) {
                    selectData.remove(media);
                    subSelectPosition();
                    AnimUtils.disZoom(contentHolder.ivPicture, config.zoomAnim);
                    break;
                }
            }
        } else {
            // 如果是单选，则清空已选中的并刷新列表(作单一选择)
            if (config.selectionMode == PictureConfig.SINGLE) {
                singleRadioMediaImage();
            }

            // 如果宽高为0，重新获取宽高
            if (image.getWidth() == 0 || image.getHeight() == 0) {
                int width = 0, height = 0;
                image.setOrientation(-1);
                if (PictureMimeType.isContent(image.getPath())) {
                    if (PictureMimeType.isHasVideo(image.getMimeType())) {
                        int[] size = MediaUtils.getVideoSizeForUri(context, Uri.parse(image.getPath()));
                        width = size[0];
                        height = size[1];
                    } else if (PictureMimeType.isHasImage(image.getMimeType())) {
                        int[] size = MediaUtils.getImageSizeForUri(context, Uri.parse(image.getPath()));
                        width = size[0];
                        height = size[1];
                    }
                } else {
                    if (PictureMimeType.isHasVideo(image.getMimeType())) {
                        int[] size = MediaUtils.getVideoSizeForUrl(image.getPath());
                        width = size[0];
                        height = size[1];
                    } else if (PictureMimeType.isHasImage(image.getMimeType())) {
                        int[] size = MediaUtils.getImageSizeForUrl(image.getPath());
                        width = size[0];
                        height = size[1];
                    }
                }
                image.setWidth(width);
                image.setHeight(height);
            }

            selectData.add(image);
            image.setNum(selectData.size());
            VoiceUtils.getInstance().play();
            AnimUtils.zoom(contentHolder.ivPicture, config.zoomAnim);
            contentHolder.tvCheck.startAnimation(AnimationUtils.loadAnimation(context, R.anim.picture_anim_modal_in));
        }

        //通知点击项发生了改变
        boolean isRefreshAll = false;
        if (config.isMaxSelectEnabledMask) {
            if (config.chooseMode == PictureMimeType.ofAll()) {
                // ofAll模式
                if (config.isWithVideoImage && config.maxVideoSelectNum > 0) {
                    if (getSelectedSize() >= config.maxSelectNum) {
                        isRefreshAll = true;
                    }
                    if (isChecked) {
                        // 删除
                        if (getSelectedSize() == config.maxSelectNum - 1) {
                            isRefreshAll = true;
                        }
                    }
                } else {
                    if (!isChecked && getSelectedSize() == 1) {
                        // 添加
                        isRefreshAll = true;
                    }
                    if (isChecked && getSelectedSize() == 0) {
                        // 删除
                        isRefreshAll = true;
                    }
                }
            } else {
                // ofImage or ofVideo or ofAudio
                if (config.chooseMode == PictureMimeType.ofVideo() && config.maxVideoSelectNum > 0) {
                    if (!isChecked && getSelectedSize() == config.maxVideoSelectNum) {
                        // 添加
                        isRefreshAll = true;
                    }
                    if (isChecked && getSelectedSize() == config.maxVideoSelectNum - 1) {
                        // 删除
                        isRefreshAll = true;
                    }
                } else {
                    if (!isChecked && getSelectedSize() == config.maxSelectNum) {
                        // 添加
                        isRefreshAll = true;
                    }
                    if (isChecked && getSelectedSize() == config.maxSelectNum - 1) {
                        // 删除
                        isRefreshAll = true;
                    }
                }
            }
        }

        if (isRefreshAll) {
            notifyDataSetChanged();
        } else {
            notifyItemChanged(contentHolder.getAdapterPosition());
        }

        selectImage(contentHolder, !isChecked);
        if (imageSelectChangedListener != null) {
            imageSelectChangedListener.onChange(selectData);
        }
    }

    /**
     * 单选模式
     */
    private void singleRadioMediaImage() {
        if (selectData != null
                && selectData.size() > 0) {
            LocalMedia media = selectData.get(0);
            notifyItemChanged(media.position);
            selectData.clear();
        }
    }

    /**
     * 更新选择的顺序
     */
    private void subSelectPosition() {
        if (config.checkNumMode) {
            int size = selectData.size();
            for (int index = 0; index < size; index++) {
                LocalMedia media = selectData.get(index);
                media.setNum(index + 1);
                notifyItemChanged(media.position);
            }
        }
    }

    /**
     * 选中的图片并执行动画
     *
     * @param holder
     * @param isChecked
     */
    public void selectImage(ViewHolder holder, boolean isChecked) {
        holder.tvCheck.setSelected(isChecked);
        if (isChecked) {
            holder.ivPicture.setColorFilter(ContextCompat.getColor
                    (context, R.color.picture_color_80), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.ivPicture.setColorFilter(ContextCompat.getColor
                    (context, R.color.picture_color_20), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void setOnPhotoSelectChangedListener(OnPhotoSelectChangedListener
                                                        imageSelectChangedListener) {
        this.imageSelectChangedListener = imageSelectChangedListener;
    }

    /**
     * 提示
     */
    private void showPromptDialog(String content) {
        PictureCustomDialog dialog = new PictureCustomDialog(context, R.layout.picture_prompt_dialog);
        TextView btnOk = dialog.findViewById(R.id.btnOk);
        TextView tvContent = dialog.findViewById(R.id.tv_content);
        tvContent.setText(content);
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

}
