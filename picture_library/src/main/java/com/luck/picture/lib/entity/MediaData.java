package com.luck.picture.lib.entity;

import java.util.List;

/**
 * @Author: RenZhengWei
 * @CreateDate: 2020/5/7 16:21
 * @Description: MediaData
 */
public class MediaData {
    /**
     * Is there more
     */
    public boolean isHasNextMore;

    /**
     * data
     */
    public List<LocalMedia> data;


    public MediaData() {
        super();
    }

    public MediaData(boolean isHasNextMore, List<LocalMedia> data) {
        super();
        this.isHasNextMore = isHasNextMore;
        this.data = data;
    }
}
