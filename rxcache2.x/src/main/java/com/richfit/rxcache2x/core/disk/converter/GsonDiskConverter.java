package com.richfit.rxcache2x.core.disk.converter;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.richfit.rxcache2x.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;



/**
 * GSON-数据转换器
 * @version monday 2016-07
 */
public class GsonDiskConverter implements IDiskConverter {
    private Gson gson;

    public GsonDiskConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object load(InputStream source, Type type) {
        Object value = null;
        try {
            value = gson.fromJson(new InputStreamReader(source), type);
        } catch (JsonIOException e) {
        } catch (JsonSyntaxException e) {
        } finally {
            FileUtil.close(source);
        }
        return value;
    }

    @Override
    public boolean writer(OutputStream sink, Object data) {
        try {
            String json = gson.toJson(data);
            byte[] bytes = json.getBytes();
            sink.write(bytes, 0, bytes.length);
            sink.flush();
            return true;
        } catch (JsonIOException e) {
        } catch (JsonSyntaxException e) {
        } catch (IOException e) {
        }
        return false;
    }

}
