package com.iSchool.search.Interceptor;

import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.utils.thread.ApThreadLocalUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApTokenInterceptor implements HandlerInterceptor {
    /**
     * 获取请求头中的信息
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
        if(userId != null){
            ApUser apUser =new ApUser();
            apUser.setId(Integer.parseInt(userId));
            ApThreadLocalUtil.setUser(apUser);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
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
    }
}
*/


