package com.example.projecttemplate.utils.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.example.projecttemplate.utils.DateUtils;
import com.example.projecttemplate.utils.StringUtils;
import com.example.projecttemplate.utils.uuid.IdUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件处理工具类
 * 
 * @author 7bin
 */
public class FileUtils extends org.apache.commons.io.FileUtils
{
    public static String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";


    public static String concatenateDirAndFilename(String dir, String filename){
        return dir + File.separator + filename;
    }


    /**
     * 读取项目中的静态文本资源
     * @param resourcePath 资源路径, e.g. "static/jupyter_lab_config.py"
     * @return java.lang.String
     * @Author bin
     **/
    public static String readResourceTxtFile(String resourcePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(resourcePath);
        InputStream is = classPathResource.getInputStream();
        return readTxtFile(is);
    }

    /**
     * 根据输入路径读取文本字符串
     * @param filePath 文件路径
     * @return java.lang.String
     * @Author bin
     **/
    public static String readTxtFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }
        FileInputStream fis = new FileInputStream(file);
        return readTxtFile(fis);
    }

    /**
     * 根据输入流读取文本字符串
     * @param is
     * @return java.lang.String
     * @Author bin
     **/
    public static String readTxtFile(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        String s; //读取到的字符串
        char[] cbuf = new char[20];
        int len;
        StringBuilder sb = new StringBuilder(); //临时字符串
        while ((len = isr.read(cbuf)) != -1) {
            sb.append(new String(cbuf, 0 , len));
        }
        s = sb.toString();
        IOUtils.close(isr);//关流
        return s;
    }


    /**
     * 输出指定文件的byte数组
     * 
     * @param filePath 文件路径
     * @param os 输出流
     * @return
     */
    public static void writeBytes(String filePath, OutputStream os) throws IOException
    {
        FileInputStream fis = null;
        try
        {
            File file = new File(filePath);
            if (!file.exists())
            {
                throw new FileNotFoundException(filePath);
            }
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int length;
            while ((length = fis.read(b)) > 0)
            {
                os.write(b, 0, length);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            IOUtils.close(os);
            IOUtils.close(fis);
        }
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @return 目标文件
     * @throws IOException IO异常
     */
    public static String writeBytes(byte[] data, String uploadDir, String filename) throws IOException
    {
        FileOutputStream fos = null;
        try
        {
            File file = FileUploadUtils.getAbsoluteFile(uploadDir, filename);
            fos = new FileOutputStream(file);
            fos.write(data);
        }
        finally
        {
            IOUtils.close(fos);
        }
        return concatenateDirAndFilename(uploadDir, filename);
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @param uploadDir 目标文件
     * @return 目标文件
     * @throws IOException IO异常
     */
    public static String writeBytes(byte[] data, String uploadDir) throws IOException
    {
        FileOutputStream fos = null;
        String pathName = "";
        try
        {
            String extension = getFileExtendName(data);
            pathName = DateUtils.datePath() + "/" + IdUtils.fastUUID() + "." + extension;
            File file = FileUploadUtils.getAbsoluteFile(uploadDir, pathName);
            fos = new FileOutputStream(file);
            fos.write(data);
        }
        finally
        {
            IOUtils.close(fos);
        }
        // 返回相对地址
        // return FileUploadUtils.getPathFileName(uploadDir, pathName);
        return concatenateDirAndFilename(uploadDir, pathName);
    }

    /**
     * 删除文件
     * 
     * @param path 文件路径
     * @return 是否删除成功
     */
    public static boolean delete(String path)
    {
        return FileUtil.del(path);
    }

    /**
     * 文件名称验证
     * 
     * @param filename 文件名称
     * @return true 正常 false 非法
     */
    public static boolean isValidFilename(String filename)
    {
        return filename.matches(FILENAME_PATTERN);
    }

    /**
     * 检查文件是否可下载
     * 
     * @param resource 需要下载的文件
     * @return true 正常 false 非法
     */
    public static boolean checkAllowDownload(String resource)
    {
        // 禁止目录上跳级别
        if (StringUtils.contains(resource, ".."))
        {
            return false;
        }

        // 检查允许下载的文件规则
        if (ArrayUtils.contains(MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, FileTypeUtils.getFileType(resource)))
        {
            return true;
        }

        // 不在允许下载的文件规则
        return false;
    }

    /**
     * 下载文件名重新编码
     * 
     * @param request 请求对象
     * @param fileName 文件名
     * @return 编码后的文件名
     */
    public static String setFileDownloadHeader(HttpServletRequest request, String fileName) throws UnsupportedEncodingException
    {
        final String agent = request.getHeader("USER-AGENT");
        String filename = fileName;
        if (agent.contains("MSIE"))
        {
            // IE浏览器
            filename = URLEncoder.encode(filename, "utf-8");
            filename = filename.replace("+", " ");
        }
        else if (agent.contains("Firefox"))
        {
            // 火狐浏览器
            filename = new String(fileName.getBytes(), "ISO8859-1");
        }
        else if (agent.contains("Chrome"))
        {
            // google浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        else
        {
            // 其它浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        return filename;
    }

    /**
     * 下载文件名重新编码
     *
     * @param response 响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException
    {
        String percentEncodedFileName = percentEncode(realFileName);

        StringBuilder contentDispositionValue = new StringBuilder();
        contentDispositionValue.append("attachment; filename=")
                .append(percentEncodedFileName)
                .append(";")
                .append("filename*=")
                .append("utf-8''")
                .append(percentEncodedFileName);

        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue.toString());
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) throws UnsupportedEncodingException
    {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 获取图像后缀
     * 
     * @param photoByte 图像数据
     * @return 后缀名
     */
    public static String getFileExtendName(byte[] photoByte)
    {
        String strFileExtendName = "jpg";
        if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
                && ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97))
        {
            strFileExtendName = "gif";
        }
        else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70))
        {
            strFileExtendName = "jpg";
        }
        else if ((photoByte[0] == 66) && (photoByte[1] == 77))
        {
            strFileExtendName = "bmp";
        }
        else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71))
        {
            strFileExtendName = "png";
        }
        return strFileExtendName;
    }

    /**
     * 获取文件名称 /profile/upload/2022/04/16/7bin.png -- 7bin.png
     * 
     * @param fileName 路径名称
     * @return 没有文件路径的名称
     */
    public static String getName(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }
        int lastUnixPos = fileName.lastIndexOf('/');
        int lastWindowsPos = fileName.lastIndexOf('\\');
        int index = Math.max(lastUnixPos, lastWindowsPos);
        return fileName.substring(index + 1);
    }

    /**
     * 获取不带后缀文件名称 /profile/upload/2022/04/16/7bin.png -- 7bin
     * 
     * @param fileName 路径名称
     * @return 没有文件路径和后缀的名称
     */
    public static String getNameNotSuffix(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }
        String baseName = FilenameUtils.getBaseName(fileName);
        return baseName;
    }

    /**
     * 根据传入的字节大小对单位进行转换
     * @param size 字节
     * @return java.lang.String
     * @author bin
     **/
    public static String calcSize(Long size){
        //获取到的size为：1705230
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + "B";
        }
        return resultSize;
    }



    /**
     * 获得路径下的文件路径列表
     * @param path 
     * @return java.util.List<java.lang.String> 
     * @author 7bin
     **/
    public static List<String> getFilesPath(String path)
    {
        File file = new File(path);
        File[] files = file.listFiles();
        List<String> filesPath = new ArrayList<>();
        for (File f : files) {
            filesPath.add(f.getAbsolutePath());
        }
        return filesPath;
    }

    /**
     * 获得路径下的文件
     * @param path
     * @return java.util.List<java.lang.String>
     * @author 7bin
     **/
    public static List<File> ls(String path)
    {

        File[] ls = FileUtil.ls(path);
        // 不是文件夹会抛出异常

        return new ArrayList<>(Arrays.asList(ls));
    }

    /**
     * 获得路径下的文件列表(含子目录)
     * @param path 待查找的路径
     * @param fileList 存储结果列表
     * @return void 
     * @author 7bin
     **/
    public static void getFilesContainChild(String path, List<File> fileList)
    {
        File file = new File(path);
        // 如果这个路径是文件夹
        if (file.isDirectory()) {
            // 获取路径下的所有文件
            File[] files = file.listFiles();
            if (files == null){
                return;
            }
            for (int i = 0; i < files.length; i++) {
                // 如果还是文件夹 递归获取里面的文件 文件夹
                fileList.add(files[i]);
                if (files[i].isDirectory()) {
                    // System.out.println("目录：" + files[i].getAbsolutePath());
                    // filesPath.add(files[i].getAbsolutePath());
                    //继续读取文件夹里面的所有文件
                    getFilesContainChild(files[i].getAbsolutePath(), fileList);
                } else {
                    // filesPath.add(files[i].getAbsolutePath());
                    // System.out.println("文件：" + files[i].getAbsolutePath());
                }
            }
        } else {
            // System.out.println("文件：" + file.getAbsolutePath());
            fileList.add(file);
        }
    }


    /**
     * 根据传入的父目录得到文件的相对路径
     * @param file 待处理文件
     * @param parentPath 父目录
     * @return java.lang.String
     * @author 7bin
     **/
    public static String getFileRelativePath(File file, String parentPath) {
        String path = FileUtil.getCanonicalPath(file);
        parentPath = transPath(parentPath);
        path = transPath(path);
        if (!path.contains(parentPath)){
            return path;
        } else {
            return (path.split(parentPath))[1];
        }
    }


    /**
     * 转移路径分隔符 \ -> /
     * @param path
     * @return java.lang.String
     * @author 7bin
     **/
    public static String transPath(String path){
        return path.replaceAll("\\\\", "/");
    }



    /**
     * File类型文件转成MultipartFile
     * @param file 待转换文件
     * @return {@link MultipartFile}
     * @author 7bin
     **/
    public static MultipartFile file2MultipartFile(File file) {
        DiskFileItem item = new DiskFileItem("file"
            , MediaType.MULTIPART_FORM_DATA_VALUE
            , true
            , file.getName()
            , (int)file.length()
            , file.getParentFile());
        try {
            OutputStream os = item.getOutputStream();
            os.write(FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonsMultipartFile(item);
    }



    /**
     * 判断两个文件是否相同
     * @param file1Path file1路径
     * @param file2Path file2路径
     * @return {@link Boolean}
     * @author 7bin
     **/
    public static Boolean isSameFile(String file1Path, String file2Path) {
        FileInputStream fis1 = null;
        FileInputStream fis2 = null;
        try {
            fis1 = new FileInputStream(file1Path);
            fis2 = new FileInputStream(file2Path);
            String md5Hex1 = DigestUtils.md5Hex(fis1);
            // System.out.println("file1:" + md5Hex1);
            String md5Hex2 = DigestUtils.md5Hex(fis2);
            // System.out.println("file2:" + md5Hex2);
            return md5Hex1.equals(md5Hex2);
        } catch (IOException e){
            return false;
        } finally {
            // 关流
            try {
                if (fis1 != null){
                    fis1.close();
                }
                if (fis2 != null){
                    fis2.close();
                }
            } catch (IOException e){
                // System.out.println("IOException");
                e.printStackTrace();
            }
        }
    }


    /**
     * 递归删除文件夹
     * @param path 文件夹路径
     * @author 7bin
     **/
    public static void deleteDirectory(String path) throws IOException {
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });

        Files.walkFileTree(Paths.get(""), new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                return super.visitFile(file, attrs);
            }
        });
    }


    /**
     * 拷贝文件夹
     * @param source 源文件夹路径
     * @param target 目标文件夹路径
     * @author 7bin
     **/
    public static void copyDirectory(String source, String target) throws IOException {
        long start = System.currentTimeMillis();

        Files.walk(Paths.get(source)).forEach(path -> {
            try {
                String targetName = path.toString().replace(source, target);
                // 是目录
                if (Files.isDirectory(path)) {
                    Files.createDirectory(Paths.get(targetName));
                }
                // 是普通文件
                else if (Files.isRegularFile(path)) {
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        long end = System.currentTimeMillis();
        // System.out.println(end - start);
    }

    /**
     * 判断文件是否存在
     * @param path 文件路径
     * @return {@link Boolean} true 存在 false 不存在
     */
    public static Boolean exist(String path) {
        return FileUtil.exist(path);
    }


    /**
     * 写内容到制定路径，覆盖原文件
     * @param path 文件路径
     * @param content 内容
     * @return {@link Boolean} true 写入成功 false 写入失败
     */
    public static File write(String path, String content) {
        return FileUtil.writeUtf8String(content, path);
    }


    /**
     * 根据文件决定路径获取该文件的文件夹路径
     * @param path 文件路径
     * @return {@link String} 父目录
     */
    public static String getDirPath(String path) {
        // 截取最后一个/前面的内容
        return path.substring(0, path.lastIndexOf("/"));
    }


    /**
     * 根据文件路径创建父目录
     * @param path 文件路径
     */
    public static void createParentDir(String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }


    /**
     * 解压压缩包
     * @param file 压缩包路径
     * @return {@link String} 解压后文件夹路径
     */
    public static String unCompress(String file) {
        String target = file.substring(0, file.lastIndexOf("."));
        if (file.endsWith(".zip")) {
            ZipUtil.unzip(file, target);
        } else if (file.endsWith(".rar")) {
            // TODO
        } else if (file.endsWith(".7z")) {
            // TODO
        } else if (file.endsWith(".tar")) {
            // TODO
        } else if (file.endsWith(".gz")) {
            // TODO
        } else if (file.endsWith(".bz2")) {
            // TODO
        }
        return target;
    }

    /**
     * 压缩文件夹
     * @param source 源文件夹路径
     * @param target 目标压缩包路径
     */
    public static void compress(String source, String target) {
        if (target.endsWith(".zip")) {
            ZipUtil.zip(source, target);
        } else if (target.endsWith(".rar")) {
            // TODO
        } else if (target.endsWith(".7z")) {
            // TODO
        } else if (target.endsWith(".tar")) {
            // TODO
        } else if (target.endsWith(".gz")) {
            // TODO
        } else if (target.endsWith(".bz2")) {
            // TODO
        }
    }

    /**
     * 判断是否为文件夹
     * @param path 文件路径
     */
    public static Boolean isDirectory(String path) {
        return FileUtil.isDirectory(path);
    }

    /**
     * 判断是否为文件夹
     * @param file 文件
     */
    public static Boolean isDirectory(File file) {
        return FileUtil.isDirectory(file);
    }

}
