package tv.game88.common.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.exception.ExcelException;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Log4j2
public class ExportExcelUtil {
    public static void exportBigExcel( List<?> list, String title, String sheetName, Class<?> pojoClass,
                                       HttpServletResponse response ) {
        ExportParams exportParams = new ExportParams( title, sheetName );
        exportParams.setMaxNum( 1000000 );
        Workbook workbook = ExcelExportUtil.exportBigExcel( exportParams, pojoClass, ( queryParamsNum, num ) -> {
            // 只导出一次，第二次返回null终止循环
            if ( ( ( int ) queryParamsNum ) == num ) {
                return null;
            }
            log.warn( "正在进行大数据量导出，条数: " + list.size() );
            return Arrays.asList( list.toArray() );
        }, 2 );
        downLoadExcel( response, workbook, title );
    }

    private static void downLoadExcel( HttpServletResponse response, Workbook workbook, String filename ) {
        try {
            response.setCharacterEncoding( "UTF-8" );
            response.setHeader( "Content-Disposition", "attachment;filename=a.xls" );
            response.setContentType( "application/vnd.ms-excel" );
            response.setHeader( "filename", URLEncoder.encode( filename, StandardCharsets.UTF_8 ) + ".xls" );
            workbook.write( response.getOutputStream() );
        } catch ( IOException e ) {
            log.error( e.getMessage(), e );
            throw new ExcelException( e.getMessage() );
        } finally {
            try {
                workbook.close();
            } catch ( IOException e ) {
                log.error( e.getMessage() );
            }
        }
    }

    public static <T> List<T> importExcel( String filePath, Integer titleRows, Integer headerRows, Class<T> pojoClass ) {
        if ( StringUtils.isBlank( filePath ) ) {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows( titleRows );
        params.setHeadRows( headerRows );
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel( new File( filePath ), pojoClass, params );
        } catch ( NoSuchElementException e ) {
            throw new ExcelException( "模板不能为空" );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new ExcelException( e.getMessage() );
        }
        return list;
    }

    public static <T> List<T> importExcel( MultipartFile file, Integer titleRows, Integer headerRows, Class<T> pojoClass ) {
        if ( file == null ) {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows( titleRows );
        params.setHeadRows( headerRows );
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel( file.getInputStream(), pojoClass, params );
        } catch ( NoSuchElementException e ) {
            throw new ExcelException( "excel文件不能为空" );
        } catch ( Exception e ) {
            throw new ExcelException( e.getMessage() );
        }
        return list;
    }
}
