package com.example.projecttemplate.exception.file;


import com.example.projecttemplate.exception.base.BaseException;

/**
 * 文件信息异常类
 * 
 * @author 7bin
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
