package com.iSchool.common.constants;

//用户行为的常量
public class BehaviorConstants {
    public static final String LIKES_BEHAVIOR = "likes_behavior_"; //点赞的key前缀 //拼接方式:前缀+userId+"_"+ArticleId+"_"+type
    public static final Short LIKE = 0;//点赞
    public static final Short DIS_LIKE = 1;//取消点赞



    //用户行为的key的前缀
    public static final String LIKE_BEHAVIOR="LIKE-BEHAVIOR-";
    public static final String UN_LIKE_BEHAVIOR="UNLIKE-BEHAVIOR-";
    public static final String COLLECTION_BEHAVIOR="COLLECTION-BEHAVIOR-";
    public static final String READ_BEHAVIOR="READ-BEHAVIOR-";
    public static final String APUSER_FOLLOW_RELATION="APUSER-FOLLOW-";
    public static final String APUSER_FANS_RELATION="APUSER-FANS-";
}
