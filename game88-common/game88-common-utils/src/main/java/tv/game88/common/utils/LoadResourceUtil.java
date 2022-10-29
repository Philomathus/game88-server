package tv.game88.common.utils;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

public class LoadResourceUtil {
    public static String getSecurityKeyStr(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        String tempPath = System.getProperty("java.io.tmpdir") + path.split("/")[1];
        File file = new File(tempPath);
        if (resource.exists()) {
            IOUtils.copy(resource.getInputStream(), new FileOutputStream(file));
            return readUsingBufferedReader(file);
        }
        return null;
    }

    private static String readUsingBufferedReader(File file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        StringBuilder sb = new StringBuilder();
        while (StringUtils.isNotBlank(line = br.readLine())) {
            sb.append(line);
        }
        br.close();
        fr.close();
        return sb.toString();
    }
}
