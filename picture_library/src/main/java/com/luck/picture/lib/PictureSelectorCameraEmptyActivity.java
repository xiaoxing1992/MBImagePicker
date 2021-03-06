package com.luck.picture.lib;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.immersive.ImmersiveManage;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.thread.PictureThreadUtils;
import com.luck.picture.lib.tools.BitmapUtils;
import com.luck.picture.lib.tools.MediaUtils;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.luck.picture.lib.tools.ToastUtils;
import com.luck.picture.lib.tools.ValueOf;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author：luck
 * @date：2019-11-15 21:41
 * @describe：单独拍照承载空Activity
 */
public class PictureSelectorCameraEmptyActivity extends PictureBaseActivity {

    @Override
    public void immersive() {
        ImmersiveManage.immersiveAboveAPI23(this
                , ContextCompat.getColor(this, R.color.picture_color_transparent)
                , ContextCompat.getColor(this, R.color.picture_color_transparent)
                , openWhiteStatusBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (config != null && !config.isUseCustomCamera && PictureSelectionConfig.onPictureSelectorInterfaceListener == null) {
            if (savedInstanceState == null) {
                if (PermissionChecker
                        .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                        PermissionChecker
                                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    onTakePhoto();
                } else {
                    PermissionChecker.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
                }
            }
            setTheme(R.style.Picture_Theme_Translucent);
        }
    }


    @Override
    public int getResourceId() {
        return R.layout.picture_empty;
    }


    /**
     * 启动相机
     */
    private void onTakePhoto() {
        // 启动相机拍照,先判断手机是否有拍照权限
        if (PermissionChecker
                .checkSelfPermission(this, Manifest.permission.CAMERA)) {
            boolean isPermissionChecker = true;
            if (config != null && config.isUseCustomCamera) {
                isPermissionChecker = PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            }
            if (isPermissionChecker) {
                startCamera();
            } else {
                PermissionChecker
                        .requestPermissions(this,
                                new String[]{Manifest.permission.RECORD_AUDIO}, PictureConfig.APPLY_RECORD_AUDIO_PERMISSIONS_CODE);
            }
        } else {
            PermissionChecker.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PictureConfig.APPLY_CAMERA_PERMISSIONS_CODE);
        }
    }

    /**
     * 根据类型启动相应相机
     */
    private void startCamera() {
        switch (config.chooseMode) {
            case PictureConfig.TYPE_ALL:
            case PictureConfig.TYPE_IMAGE:
                // 拍照
                startOpenCamera();
                break;
            case PictureConfig.TYPE_VIDEO:
                // 录视频
                startOpenCameraVideo();
                break;
            case PictureConfig.TYPE_AUDIO:
                // 录音
                startOpenCameraAudio();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case UCrop.REQUEST_CROP:
                    singleCropHandleResult(data);
                    break;
                case PictureConfig.REQUEST_CAMERA:
                    dispatchHandleCamera(data);
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (config != null && PictureSelectionConfig.listener != null) {
                PictureSelectionConfig.listener.onCancel();
            }
            closeActivity();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            if (data == null) {
                return;
            }
            Throwable throwable = (Throwable) data.getSerializableExtra(UCrop.EXTRA_ERROR);
            ToastUtils.s(getContext(), throwable.getMessage());
        }
    }

    /**
     * 单张图片裁剪
     *
     * @param data
     */
    protected void singleCropHandleResult(Intent data) {
        if (data == null) {
            return;
        }
        List<LocalMedia> medias = new ArrayList<>();
        Uri resultUri = UCrop.getOutput(data);
        if (resultUri == null) {
            return;
        }
        String cutPath = resultUri.getPath();
        // 单独拍照
        boolean isCutEmpty = TextUtils.isEmpty(cutPath);
        LocalMedia media = new LocalMedia(config.cameraPath, 0, false,
                config.isCamera ? 1 : 0, 0, config.chooseMode);
        if (SdkVersionUtils.checkedAndroid_Q()) {
            int lastIndexOf = config.cameraPath.lastIndexOf("/") + 1;
            media.setId(lastIndexOf > 0 ? ValueOf.toLong(config.cameraPath.substring(lastIndexOf)) : -1);
            media.setAndroidQToPath(cutPath);
            if (isCutEmpty) {
                if (PictureMimeType.isContent(config.cameraPath)) {
                    String path = PictureFileUtils.getPath(this, Uri.parse(config.cameraPath));
                    media.setSize(!TextUtils.isEmpty(path) ? new File(path).length() : 0);
                } else {
                    media.setSize(new File(config.cameraPath).length());
                }
            } else {
                media.setSize(new File(cutPath).length());
            }
        } else {
            // 拍照产生一个临时id
            media.setId(System.currentTimeMillis());
            media.setSize(new File(isCutEmpty ? media.getPath() : cutPath).length());
        }
        media.setCut(!isCutEmpty);
        media.setCutPath(cutPath);
        String mimeType = PictureMimeType.getImageMimeType(cutPath);
        media.setMimeType(mimeType);
        int width = 0, height = 0;
        media.setOrientation(-1);
        if (PictureMimeType.isContent(media.getPath())) {
            if (PictureMimeType.isHasVideo(media.getMimeType())) {
                int[] size = MediaUtils.getVideoSizeForUri(getContext(), Uri.parse(media.getPath()));
                width = size[0];
                height = size[1];
            } else if (PictureMimeType.isHasImage(media.getMimeType())) {
                int[] size = MediaUtils.getImageSizeForUri(getContext(), Uri.parse(media.getPath()));
                width = size[0];
                height = size[1];
            }
        } else {
            if (PictureMimeType.isHasVideo(media.getMimeType())) {
                int[] size = MediaUtils.getVideoSizeForUrl(media.getPath());
                width = size[0];
                height = size[1];
            } else if (PictureMimeType.isHasImage(media.getMimeType())) {
                int[] size = MediaUtils.getImageSizeForUrl(media.getPath());
                width = size[0];
                height = size[1];
            }
        }
        media.setWidth(width);
        media.setHeight(height);
        // 如果有旋转信息图片宽高则是相反
        MediaUtils.setOrientationAsynchronous(getContext(), media,
                item -> {
                    medias.add(item);
                    handlerResult(medias);
                });
    }


    /**
     * 拍照后处理结果
     *
     * @param intent
     */

    protected void dispatchHandleCamera(Intent intent) {
        boolean isAudio = config.chooseMode == PictureMimeType.ofAudio();
        config.cameraPath = isAudio ? getAudioPath(intent) : config.cameraPath;
        if (TextUtils.isEmpty(config.cameraPath)) {
            return;
        }
        // 开启异步线程进行处理
        showPleaseDialog();
        PictureThreadUtils.executeBySingle(new PictureThreadUtils.SimpleTask<LocalMedia>() {
            @Override
            public LocalMedia doInBackground() {
                // 创建一个LocalMedia对象
                LocalMedia media = new LocalMedia();
                String mimeType = isAudio ? PictureMimeType.MIME_TYPE_AUDIO : "";
                int[] newSize = new int[2];
                long duration = 0;
                if (!isAudio) {
                    // 图片和视频的处理逻辑
                    if (PictureMimeType.isContent(config.cameraPath)) {
                        // content://类型处理规则
                        String path = PictureFileUtils.getPath(getContext(), Uri.parse(config.cameraPath));
                        if (!TextUtils.isEmpty(path)) {
                            File cameraFile = new File(path);
                            mimeType = PictureMimeType.getMimeType(config.cameraMimeType);
                            media.setSize(cameraFile.length());
                        }
                        if (PictureMimeType.isHasImage(mimeType)) {
                            // 图片
                            newSize = MediaUtils.getImageSizeForUrlToAndroidQ(getContext(), config.cameraPath);
                        } else if (PictureMimeType.isHasVideo(mimeType)) {
                            // 视频
                            newSize = MediaUtils.getVideoSizeForUri(getContext(), Uri.parse(config.cameraPath));
                        }
                        int lastIndexOf = config.cameraPath.lastIndexOf("/") + 1;
                        media.setId(lastIndexOf > 0 ? ValueOf.toLong(config.cameraPath.substring(lastIndexOf)) : -1);
                        media.setRealPath(path);
                        // 自定义拍照时已经在应用沙盒内生成了文件
                        String mediaPath = intent != null ? intent.getStringExtra(PictureConfig.EXTRA_MEDIA_PATH) : null;
                        media.setAndroidQToPath(mediaPath);
                    } else {
                        // 普通类型处理规则
                        File cameraFile = new File(config.cameraPath);
                        mimeType = PictureMimeType.getMimeType(config.cameraMimeType);
                        media.setSize(cameraFile.length());
                        if (PictureMimeType.isHasImage(mimeType)) {
                            // 图片
                            int degree = PictureFileUtils.readPictureDegree(getContext(), config.cameraPath);
                            BitmapUtils.rotateImage(degree, config.cameraPath);
                            newSize = MediaUtils.getImageSizeForUrl(config.cameraPath);
                        } else if (PictureMimeType.isHasVideo(mimeType)) {
                            // 视频
                            newSize = MediaUtils.getVideoSizeForUrl(config.cameraPath);
                        }
                        // 拍照产生一个临时id
                        media.setId(System.currentTimeMillis());
                    }
                    // 给LocalMedia对象赋值
                    media.setPath(config.cameraPath);
                    media.setDuration(duration);
                    media.setMimeType(mimeType);
                    media.setWidth(newSize[0]);
                    media.setHeight(newSize[1]);
                    if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isHasVideo(media.getMimeType())) {
                        media.setParentFolderName(Environment.DIRECTORY_MOVIES);
                    } else {
                        media.setParentFolderName(PictureMimeType.CAMERA);
                    }
                    media.setChooseModel(config.chooseMode);
                    long bucketId = MediaUtils.getCameraFirstBucketId(getContext());
                    media.setBucketId(bucketId);
                    // 如果有旋转信息图片宽高则是相反
                    MediaUtils.setOrientationSynchronous(getContext(), media);
                }
                return media;
            }
            @Override
            public void onSuccess(LocalMedia result) {
                // 刷新系统相册
                dismissDialog();
                if (!SdkVersionUtils.checkedAndroid_Q()) {
                    if (config.isFallbackVersion3) {
                        new PictureMediaScannerConnection(getContext(), config.cameraPath);
                    } else {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(config.cameraPath))));
                    }
                }
                dispatchCameraHandleResult(result);
                // 这里主要解决极个别手机拍照会在DCIM目录重复生成一张照片问题
                if (!SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isHasImage(result.getMimeType())) {
                    int lastImageId = MediaUtils.getDCIMLastImageId(getContext());
                    if (lastImageId != -1) {
                        MediaUtils.removeMedia(getContext(), lastImageId);
                    }
                }

            }
        });
    }

    /**
     * 摄像头后处理方式
     *
     * @param media
     */
    private void dispatchCameraHandleResult(LocalMedia media) {
        // 如果是单选 拍照后直接返回
        boolean isHasImage = PictureMimeType.isHasImage(media.getMimeType());
        if (config.enableCrop && isHasImage) {
            // 去裁剪
            config.originalPath = config.cameraPath;
            startCrop(config.cameraPath, media.getMimeType());
        } else if (config.isCompress && isHasImage && !config.isCheckOriginalImage) {
            // 去压缩
            List<LocalMedia> result = new ArrayList<>();
            result.add(media);
            compressImage(result);
        } else {
            // 不裁剪 不压缩 直接返回结果
            List<LocalMedia> result = new ArrayList<>();
            result.add(media);
            onResult(result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE:
                // 存储权限
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PermissionChecker.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA}, PictureConfig.APPLY_CAMERA_PERMISSIONS_CODE);
                } else {
                    ToastUtils.s(getContext(), getString(R.string.picture_jurisdiction));
                    closeActivity();
                }
                break;
            case PictureConfig.APPLY_CAMERA_PERMISSIONS_CODE:
                // 相机权限
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onTakePhoto();
                } else {
                    closeActivity();
                    ToastUtils.s(getContext(), getString(R.string.picture_camera));
                }
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity();
    }
}
