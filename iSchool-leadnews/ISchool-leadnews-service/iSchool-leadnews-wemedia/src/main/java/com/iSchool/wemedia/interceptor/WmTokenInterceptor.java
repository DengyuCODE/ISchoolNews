package com.iSchool.wemedia.interceptor;

import com.iSchool.model.wemedia.pojos.WmUser;
import com.iSchool.utils.thread.WmThreadLocalUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WmTokenInterceptor implements HandlerInterceptor {
    /**
     * 用于获取请求中的信息
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头中的userId
        String userId = request.getHeader("userId");
        if(userId!=null){
            //将其存入线程中(ThreadLocal)
            //在util中创建使用ThreadLocal的工具类
            WmUser wmUser=new WmUser();
            wmUser.setId(Integer.parseInt(userId));
            WmThreadLocalUtil.setUser(wmUser);
        }

        return true;
    }

    /**
     * 清理线程中的信息
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        WmThreadLocalUtil.clear();
    }
}


//以下方法是使用SpringSecurity的情况，使用过滤器将
/**
 * 自定义认证过滤器
 * 这个过滤器会会获取请求头中的token并对token进行解析取出其中的userid。
 * 使用userid去redis中获取对应的LoginUser对象,然后封装Authentication对象存入SecurityContextHolder
 */

/*
//OncePerRequestFilter方法是Spring框架中的一个过滤器类，如同其名字一样，确保过滤器只被每个请求执行一次
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        //获取token
        String token = httpServletRequest.getHeader("token");
        //判断token是否为空字符串
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return ;
        }
        //解析token
        String userId = null;
        try {
            //token创建是根据userId创建的
            Claims claims = JwtUtil.parseJWT(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("token非法");
        }
        //将对象存入SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId,null,null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        //执行完之后放行
        filterChain.doFilter(httpServletRequest,httpServletResponse);




        //这里只需要将userId存到SecurityContextHolder，以下操作忽略
        //从redis中获取用户信息
        String redisKey = "login"+userId;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);
        if(Objects.isNull(loginUser)){
            throw new RuntimeException("用户未登录");
        }
        //将对象存入SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser,null,null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        //执行完之后放行
        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
}
*/

