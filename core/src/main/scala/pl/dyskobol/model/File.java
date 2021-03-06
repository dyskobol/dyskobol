package pl.dyskobol.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import org.apache.tika.Tika;
import scala.Int;
import scala.concurrent.Promise;

public class File {
    final public String name;
    public String path;
    final public int type;
    final public long addr;
    final public int permissions;
    final public long size;
    final public int uid;
    final public int gid;
    final public long mtime;
    final public long mtime_nano;
    final public long atime;
    final public long atime_nano;
    final public long ctime;
    final public long ctime_nano;
    final public long crtime;
    final public long crtime_nano;
    final public long filesystem;

    public String mime;
    public int id;

    public static final int REGULAR_FILE = 5;
    public static final int DIRECTORY = 3;

    public File(String name,
                String path,
                int type,
                long addr,
                int permissions,
                long size,
                int uid,
                int gid,
                long mtime,
                long mtime_nano,
                long atime,
                long atime_nano,
                long ctime,
                long ctime_nano,
                long crtime,
                long crtime_nano,
                long filesystem) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.addr = addr;
        this.permissions = permissions;
        this.size = size;
        this.uid = uid;
        this.gid = gid;
        this.mtime = mtime;
        this.mtime_nano = mtime_nano;
        this.atime = atime;
        this.atime_nano = atime_nano;
        this.ctime = ctime;
        this.ctime_nano = ctime_nano;
        this.crtime = crtime;
        this.crtime_nano = crtime_nano;
        this.filesystem = filesystem;
    }

    public InputStream createStream() {
        return new FileStream(filesystem, this);
    }

    public String toString() {
        return name + " " + id;
    }
}
