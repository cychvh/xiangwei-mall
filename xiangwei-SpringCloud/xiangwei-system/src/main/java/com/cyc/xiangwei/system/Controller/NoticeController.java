package com.cyc.xiangwei.system.Controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyc.xiangwei.common.utils.Result;
import com.cyc.xiangwei.common.utils.ResultCodeEnum;
import com.cyc.xiangwei.system.entity.Notice;
import com.cyc.xiangwei.system.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 辅助方法：安全提取 Header 参数
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


    // 1. 统一获取公告列表
    // 原来的 /notice/list 变成了 /list (配合类上的 @RequestMapping)
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "5") Integer pageSize,
                          @RequestParam(defaultValue = "") String search,
                          HttpServletRequest request) {

        Integer type = getIntegerHeader(request, "type");
        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();

        if (type == 2) {
            wrapper.and(w -> w.eq(Notice::getType, "User").or().eq(Notice::getType, "All"));
        } else if (type == 1) {
            wrapper.and(w -> w.eq(Notice::getType, "Merchant").or().eq(Notice::getType, "All"));
        }

        if (StringUtils.hasText(search)) {
            wrapper.and(w -> w.like(Notice::getTitle, search).or().like(Notice::getContent, search));
        }

        wrapper.orderByDesc(Notice::getCreateTime);

        Page<Notice> page = noticeService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    // 2. 管理员添加公告
    @PostMapping("/addNotice")
    public Result<?> addNotice(@Validated @RequestBody Notice notice, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");
        Integer userId = getIntegerHeader(request, "userId");

        if (type == null || userId == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 0) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        notice.setId(null);

        boolean save = noticeService.save(notice);
        if (save) {
            return Result.success();
        }

        return Result.error(ResultCodeEnum.ERROR, "添加失败");
    }

    // 3. 管理员删除公告
    @DeleteMapping("/deleteNotice")
    public Result<?> deleteNotice(@RequestParam @NotNull(message = "公告ID不能为空") Integer id, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");

        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 0) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        boolean b = noticeService.removeById(id);
        if (b) {
            return Result.success();
        }

        return Result.error(ResultCodeEnum.ERROR, "删除失败");
    }

    // 4. 管理员更新公告
    @PutMapping("/updateNotice")
    public Result<?> updateNotice(@Validated @RequestBody Notice notice, HttpServletRequest request) {
        Integer type = getIntegerHeader(request, "type");

        if (type == null) {
            return Result.error(ResultCodeEnum.UNAUTHORIZED);
        }
        if (type != 0) {
            return Result.error(ResultCodeEnum.FORBIDDEN);
        }

        if (notice.getId() == null) {

            return Result.error(ResultCodeEnum.PARAM_ERROR, "更新时公告ID不能为空");
        }

        boolean b = noticeService.updateById(notice);
        if (b) {
            return Result.success();
        }

        return Result.error(ResultCodeEnum.ERROR, "修改失败，可能数据不存在");
    }
}