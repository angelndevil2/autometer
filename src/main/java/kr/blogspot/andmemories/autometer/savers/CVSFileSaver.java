package kr.blogspot.andmemories.autometer.savers;

import kr.blogspot.andmemories.autometer.common.IResultSaver;
import kr.blogspot.andmemories.autometer.common.AutoMeterException;
import kr.blogspot.andmemories.autometer.common.StatisticSample;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author k, Created on 16. 2. 2.
 */
@Data
@Slf4j
public class CVSFileSaver implements IResultSaver {

    private final String filename;
    @Getter(AccessLevel.NONE)
    private final PrintWriter out;

    public CVSFileSaver(final String filename) throws FileNotFoundException, UnsupportedEncodingException {
        this.filename = filename;
        out = getFileWriter(filename);
    }

    @Override
    public void save(@NonNull final StatisticSample r) {
        out.println(r.toCVSString());
    }

    @Override
    public void close() throws AutoMeterException {
        if (!out.checkError()) out.close();
        else throw new AutoMeterException(filename+" has error.");
    }

    private PrintWriter getFileWriter(String filename) throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = null;
        File pdir = (new File(filename)).getParentFile();
        if(pdir != null) {
            if(pdir.mkdirs()) {
                log.info("Folder " + pdir.getAbsolutePath() + " was created");
            }

            if(!pdir.exists()) {
                log.warn("Error creating directories for " + pdir.toString());
            }
        }

        writer = new PrintWriter(
                new OutputStreamWriter(
                        new BufferedOutputStream(
                                new FileOutputStream(filename, false)), "UTF-8"), true);
        log.debug("Opened file: " + filename);

        return writer;
    }
}
