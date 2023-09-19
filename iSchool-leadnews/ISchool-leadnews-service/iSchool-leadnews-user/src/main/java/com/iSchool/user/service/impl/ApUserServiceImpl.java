package com.iSchool.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iSchool.apis.wemedia.IWemediaClient;
import com.iSchool.model.common.dtos.ResponseResult;
import com.iSchool.model.common.enums.AppHttpCodeEnum;
import com.iSchool.model.user.dtos.LoginDto;
import com.iSchool.model.user.pojos.ApUser;
import com.iSchool.model.wemedia.pojos.WmUser;
import com.iSchool.user.mapper.ApUserMapper;
import com.iSchool.user.service.ApUserService;
import com.iSchool.utils.common.AppJwtUtil;
import com.iSchool.utils.common.MD5Utils;
import com.iSchool.utils.common.RandomSaltUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Autowired
    private IWemediaClient wemediaClient;

    @Autowired
    private ApUserMapper userMapper;

    /**
     * app端登录功能
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        //如果是使用SpringSecurity的过程
//        //在service里面要获取认证的方法
//        //1.AuthenticationManager authenticate认证方法
//        //1.1使用用户名密码的方式认证(参数：主要信息和凭证 即用户名和密码)
//        UsernamePasswordAuthenticationToken authenticationToken =new UsernamePasswordAuthenticationToken(user.getUserName(),user.getPassword());
//        //1.2传入认证
//        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
//        //2.认证没有通过，抛出对应异常
//        if(Objects.isNull(authenticate)){
//            throw new RuntimeException("用户名或密码错误");
//        }
        //1.正常登录 用户名和密码
        if(StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())){
            //1.1 根据手机号查询用户信息
            ApUser dbUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if(dbUser == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户信息不存在");
            }

            //1.2 比对密码
            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String pswd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if(!pswd.equals(dbUser.getPassword())){
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            //1.3 返回数据  jwt  user
            String token = AppJwtUtil.getToken(dbUser.getId().longValue());
            Map<String,Object> map = new HashMap<>();
            map.put("token",token);
            dbUser.setSalt("");
            dbUser.setPassword("");
            map.put("user",dbUser);

            return ResponseResult.okResult(map);
        }else {
            //2.游客登录
            Map<String,Object> map = new HashMap<>();
            map.put("token",AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @Override
    public ResponseResult register(ApUser user) {
        //传入用户名、手机号、密码进行注册
        String phone = user.getPhone();
        String password = user.getPassword();
        String name = user.getName();
        //判断用户是否已经存在(手机号判断)
        ApUser apUser = userMapper.selectOne(new LambdaQueryWrapper<ApUser>().eq(ApUser::getPhone, phone));
        if(apUser != null){
            return new ResponseResult<>(403,"该手机号已注册!");
        }
        //对password进行加盐加密
        //生成三位数的盐
        String salt = RandomSaltUtil.generateRandomString(3);
        user.setSalt(salt);
        //md5加密
        String pwd = MD5Utils.encodeWithSalt(password, salt);
        //存入对象
        user.setPassword(pwd);
        user.setFlag((short)1);
        user.setStatus(0);
        user.setCreatedTime(new Date());
        user.setPoints(0L);
        //保存用户
        this.save(user);
        //同时自动注册自媒体账号
        WmUser wmUser = new WmUser();
        wmUser.setPhone(phone);
        wmUser.setName(name);
        wmUser.setSalt(salt);
        wmUser.setPassword(pwd);
        wmUser.setCreatedTime(user.getCreatedTime());
        wmUser.setStatus(9);//初始化状态为正常使用
        //远程调用进行注册
        wemediaClient.register(wmUser);
        return new ResponseResult<>(200,"注册成功");
    }
}
