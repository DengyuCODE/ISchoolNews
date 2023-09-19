package com.iSchool.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

//用户积分表
@Data
@TableName("ap_points")
public class ApPoint implements Serializable{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.ID_WORKER)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 文章id
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 积分获取状态(0 无效 ， 1 有效)
     */
    @TableField("status")
    private Integer status;

    /**
     *  获取积分数量积分
     */
    @TableField("points")
    private Long points;

    /**
     * 修改时间时间
     */
    @TableField("updated_time")
    private Date updatedTime;

}
