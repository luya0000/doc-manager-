package com.manage.source.controller;

import com.manage.common.APIResponse;
import com.manage.common.BaseController;
import com.manage.common.Constants;
import com.manage.common.UrlConstants;
import com.manage.exception.impl.BizExceptionStatusEnum;
import com.manage.source.bean.FileDetail;
import com.manage.source.service.FileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luya on 2018/6/11.
 */
@RestController
@RequestMapping(value = UrlConstants.URL_FILE_MODEL)
public class FileController extends BaseController {
    private Log logger = LogFactory.getLog(FileController.class);
    // 系统文件根目录
    @Value("${file.fileRootPath}")
    private String fileRootPath;

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/getBasePath")
    public APIResponse getRootpath() {
        Integer userId = getUserId();
        List<String> rootPatn = fileService.getUserRolePath(userId, fileRootPath);
        fileService.makeDirs(fileRootPath);
        return APIResponse.toOkResponse(fileRootPath);
    }

    @RequestMapping(value = "/fileList")
    public APIResponse fileList(@RequestParam("path") String path) {
        String roles = getRoles();
        File listPath = new File(path);
        // 如果是根目录不进行操作
        if (fileRootPath.contains(listPath.getPath())) {
            return APIResponse.toExceptionResponse(BizExceptionStatusEnum.FILE_NOT_EXEC_ERROR);
        }
        // TODO 如果是文件想办法打开它
        if (listPath.isFile()) {
            String erro = path + " is a file";
            logger.error(erro);
            return APIResponse.toExceptionResponse(BizExceptionStatusEnum.FILE_NOT_DIR_ERROR);
        }
        if (!listPath.exists()) {
            String erro = path + " no exist";
            logger.error(erro);
            return APIResponse.toExceptionResponse(BizExceptionStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        List<String> ownerDirs = new ArrayList<>();
        if (roles.contains(Constants.SYSTEM_TYPE)) {
            ownerDirs.add(this.fileRootPath);
        } else {
            ownerDirs = fileService.getUserRolePath(getUserId(), this.fileRootPath);
        }
        List<FileDetail> reFiles = fileService.listAdminFiles(listPath, ownerDirs);
        return APIResponse.toOkResponse(reFiles);
    }

    /*按类型查找文件*/
    public void fileListByType() {

    }

    /**
     * 上传镜像文件
     *
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/upload-file")
    public APIResponse uploadFile(HttpServletRequest request) throws IOException {
        String filename = request.getParameter("name");
        String path = request.getParameter("path");

        if (getRoles().contains(Constants.SYSTEM_TYPE)) {
            fileService.uploadFile(request, path, filename);
        } else {
            List<String> dirs = fileService.getAdminUserDirs(getUserId(), this.fileRootPath);
            for (String dir : dirs) {
                if (path.contains(dir)) {
                    fileService.uploadFile(request, path, filename);
                } else {
                    return APIResponse.toExceptionResponse(BizExceptionStatusEnum.FILE_NOT_EXEC_ERROR);
                }
            }
        }
        return APIResponse.toOkResponse();
    }

    public void viewFile() {

    }

    public void downloadFile() {

    }

    @RequestMapping(value = "/delete")
    public APIResponse deleteFile(String paths) {
        if (StringUtils.isEmpty(paths)) {
            return APIResponse.toOkResponse();
        }

        String[] dirs = paths.split(":");
        List<String> errorFileList = new ArrayList<>();
        // 超管直接删除
        if (getRoles().contains(Constants.SYSTEM_TYPE)) {
            for (String dir : dirs) {
                if (!fileService.deleteFile(dir)) {
                    errorFileList.add(dir.substring(dir.lastIndexOf(File.separator) + 1));
                }
            }
        } else {
            List<String> dirList = fileService.getAdminUserDirs(getUserId(), this.fileRootPath);
            for (String dir : dirList) {
                // 转换成文件地址格式
                dir = dir.replace("/","\\");
                for (String delDir : dirs) {
                    if (delDir.contains(dir)) {
                        if (!fileService.deleteFile(delDir)) {
                            errorFileList.add(dir.substring(dir.lastIndexOf(File.separator) + 1));
                        }
                    } else {
                        return APIResponse.toExceptionResponse(BizExceptionStatusEnum.FILE_NOT_EXEC_ERROR);
                    }
                }
            }
        }
        return APIResponse.toOkResponse(errorFileList);
    }

    public void changeFolder() {

    }

    @RequestMapping(value = "/newdir")
    public APIResponse newFolder(String path, String dir) {
        try {
            if (!StringUtils.isEmpty(path) && !StringUtils.isEmpty(dir)) {
                if (getRoles().contains(Constants.SYSTEM_TYPE)) {
                    fileService.makeDirs(path + "/" + dir);
                } else {
                    List<String> dirs = fileService.getAdminUserDirs(getUserId(), this.fileRootPath);
                    for (String d : dirs) {
                        if (path.contains(d)) {
                            fileService.makeDirs(path + "/" + dir);
                        } else {
                            return APIResponse.toExceptionResponse(BizExceptionStatusEnum.FILE_NOT_EXEC_ERROR);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return APIResponse.toExceptionResponse(BizExceptionStatusEnum.FILE_CREATE_ERROR);
        }
        return APIResponse.toOkResponse();
    }
}
