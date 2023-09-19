package com.iSchool.common.constants;

//定义文章的参数
public class ArticleConstants {
    public static final Short LOADTYPE_LOAD_MORE = 1;// type:加载更多
    public static final Short LOADTYPE_LOAD_NEW = 2;// type:加载最新
    public static final String DEFAULT_TAG = "__all__"; //默认频道(推荐)

    public static final String ARTICLE_ES_SYNC_TOPIC = "article.es.sync.topic";//文章传到es的kafka主题

    public static final Integer HOT_ARTICLE_LIKE_WEIGHT = 3;
    public static final Integer HOT_ARTICLE_COMMENT_WEIGHT = 5;
    public static final Integer HOT_ARTICLE_COLLECTION_WEIGHT = 8;

    public static final String HOT_ARTICLE_FIRST_PAGE = "hot_article_first_page_";

    public static final String TTL_EXCHANGE = "ttl.direct";//延迟交换机

    public static final String TTL_QUEUE = "ttl.queue";//延迟队列

    public static final String DLE_TTL_EXCHANGE = "dl.ttl.direct";//死信交换机

    public static final String DLE_TTL_QUEUE = "dl.ttl.queue";//死信队列

    public static final String TTL_ROUTINGKEY = "ttl.#";//routingKey

    public static final String DLE_ROUTINGKEY = "dle.#";//死信交换机routingKey

    public static final String POINT_SUFFIX = "apPoint:"; //缓存积分信息后缀

    public static final String READ_TIME = "readTime:"; //阅读获取积分上限前缀

    public static final Integer LIMIT_TIMES = 10; //通过阅读文章获取积分的最大上限次数

    public static final Integer READING_TIME = 15;//指定阅读时长为15秒


}