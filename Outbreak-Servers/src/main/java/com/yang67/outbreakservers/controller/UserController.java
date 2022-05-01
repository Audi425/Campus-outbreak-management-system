package com.yang67.outbreakservers.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yang67.outbreakservers.common.Constants;
import com.yang67.outbreakservers.common.Result;
import com.yang67.outbreakservers.controller.entityDto.UserDto;
import com.yang67.outbreakservers.entity.User;
import com.yang67.outbreakservers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@CrossOrigin
@ResponseBody
@RequestMapping("/user")
public class UserController {
    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Resource
    private UserService userService;

    //登录
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        if(StrUtil.isBlank(user.getUserPwd())){
            return Result.error(Constants.CODE_400,"参数错误");
        }
        UserDto userDto = userService.login(user);
        return Result.success(userDto);
    }

    //根据id查个人信息
    @GetMapping("/selectUserInfo")
    public Result selectAdminInfo(@RequestParam(value = "userId") String userId){
        return Result.success(userService.getById(userId));
    }

    //修改人员信息
    @PostMapping("/updUserInfo")
    public Result updUserInfo(@RequestBody User user){
        System.out.println(user.toString());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getUserId());
        if(userService.update(user,queryWrapper)) {
            return Result.success();
        }
        else{
            return Result.error();
        }
    }

    //头像上传
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        // 定义一个文件唯一的标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;
        File uploadFile = new File(fileUploadPath + fileUUID);
        // 判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
        File parentFile = uploadFile.getParentFile();
        if(!parentFile.exists()) {
            parentFile.mkdirs();
        }
        String url;
        // 上传文件到磁盘
        file.transferTo(uploadFile);
        // 数据库若不存在重复文件，则不删除刚才上传的文件
        url = "http://localhost:8081/user/file/" + fileUUID;
        return url;
    }

    @GetMapping("/file/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        // 根据文件的唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    //修改密码
    @GetMapping("/updUserPwd")
    public Result updUserPwd(@RequestParam(value = "userId")String userId,@RequestParam(value = "userPwd")String userPwd){
        System.out.println(userId+","+userPwd);
        return Result.success();
    }
}
