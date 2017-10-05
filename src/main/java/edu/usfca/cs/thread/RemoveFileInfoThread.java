package edu.usfca.cs.thread;

import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.route.ReplicaMaintainer;

/**
 * Created by bingkunyang on 10/4/17.
 */
public class RemoveFileInfoThread extends Thread {
    private ReplicaMaintainer maintainer;
    private StorageMessages.FixInfoMsg fixInfoMsg;

    public RemoveFileInfoThread(ReplicaMaintainer maintainer, StorageMessages.FixInfoMsg fixInfoMsg){
        this.maintainer = maintainer;
        this.fixInfoMsg = fixInfoMsg;
    }

    @Override
    public void run() {
        maintainer.removeCorruptedFileInfo(fixInfoMsg);
    }
}
