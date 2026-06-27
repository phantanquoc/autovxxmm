/*
 * Decompiled with CFR 0.152.
 */
package service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import service.entity.Blocker;
import utils.FileUtils;

public class BlockService {
    private static final String PATH = "records/blockers.txt";
    private static final BlockService instance = new BlockService();
    private final List<Blocker> blockers = new ArrayList<Blocker>();

    private BlockService() {
        this.load();
    }

    private void load() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(FileUtils.read(PATH));
            DataInputStream dis = new DataInputStream(bais);
            int size = dis.readInt();
            for (int i = 0; i < size; ++i) {
                Blocker blocker = new Blocker();
                blocker.read(dis);
                this.blockers.add(blocker);
            }
            dis.close();
            bais.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void add(Blocker blocker) {
        this.blockers.add(blocker);
        this.save();
    }

    public void remove(int index) {
        this.blockers.remove(index);
        this.save();
    }

    public boolean isContains(int serverId, String name) {
        return this.blockers.stream().anyMatch(blocker -> blocker.getServerId() == serverId && blocker.getName().equals(name));
    }

    public void save() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(this.blockers.size());
            for (int i = 0; i < this.blockers.size(); ++i) {
                this.blockers.get(i).write(dos);
            }
            FileUtils.save(PATH, baos.toByteArray());
            dos.close();
            baos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Blocker> getBlockers() {
        return this.blockers;
    }

    public static BlockService getInstance() {
        return instance;
    }
}

