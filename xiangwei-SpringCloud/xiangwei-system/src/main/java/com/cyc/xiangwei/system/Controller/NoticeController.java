package com.cyc.xiangwei.system.Controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.system.entity.Notice;
import com.cyc.xiangwei.system.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    //  辅助方法：安全地从 Header 提取 Integer 类型的参数，防止网关传参为空导致的报错
    private Integer getIntegerHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    // 统一获取公告列表接口 (管理员/用户/商家 通用)
    @GetMapping("/notice/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "5") Integer pageSize,
                          @RequestParam(defaultValue = "") String search,
                          HttpServletRequest request) {

        // 1. 获取并校验登录身份
        Integer type = getIntegerHeader(request, "type");
        if (type == null) {
            return Result.error("401", "未登录或权限异常");
        }

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();


        if (type == 2) {
            wrapper.and(w -> w.eq(Notice::getType, "User").or().eq(Notice::getType, "All"));
        } else if (type == 1) {
            wrapper.and(w -> w.eq(Notice::getType, "Merchant").or().eq(Notice::getType, "All"));
        }
        // 注：如果 type == 0 (管理员)，则不添加任何 type 过滤条件，即可以查询所有记录

        // 3. 关键字模糊搜索 (核心修复：同样必须用 and 嵌套)
        if (StringUtils.hasText(search)) {
            wrapper.and(w -> w.like(Notice::getTitle, search).or().like(Notice::getContent, search));
        }

        // 4. 按创建时间倒序排列 (最新的在最前面)
        wrapper.orderByDesc(Notice::getCreateTime);

        // 5. 执行分页查询并返回
        Page<Notice> page = noticeService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    // 3. 管理员添加公告
    @PostMapping("/notice/addNotice")
    public Result<?> addNotice(@Validated @RequestBody Notice notice, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");
        Integer userId = getIntegerHeader(request, "userId");

        // 防空指针安全拦截
        if (type == null || type != 0) {
            return Result.error("405", "权限不够");
        }
        if (userId == null) {
            return Result.error("401", "未登录或获取不到发布人信息");
        }

        notice.setId(null);

        boolean save = noticeService.save(notice);
        if (save) {
            return Result.success();
        }
        return Result.error("500", "添加失败");
    }

    // 4. 管理员删除公告
    @DeleteMapping("/notice/deleteNotice")
    public Result<?> deleteNotice(@RequestParam @NotNull(message = "公告ID不能为空")Integer id, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");

        if (type == null || type != 0) {
            return Result.error("405", "权限不够");
        }

        boolean b = noticeService.removeById(id);
        if (b) {
            return Result.success();
        }
        return Result.error("500", "删除失败");
    }

    // 5. 管理员更新公告
    @PutMapping("/notice/updateNotice")
    public Result<?> updateNotice(@Validated @RequestBody Notice notice, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");

        if (type == null || type != 0) {
            return Result.error("405", "权限不够");
        }

        if (notice.getId() == null) {
            return Result.error("400", "更新时公告ID不能为空");
        }

        boolean b = noticeService.updateById(notice);
        if (b) {
            return Result.success();
        }
        return Result.error("500", "修改失败，可能数据不存在");
    }
}