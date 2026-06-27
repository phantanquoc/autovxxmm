/*
 * Decompiled with CFR 0.152.
 */
package core.cache;

import core.cache.Skills;
import core.model.Effect;
import core.model.Item;
import core.model.Mob;
import core.model.NClass;
import core.model.Npc;
import core.model.Skill;
import core.model.SkillOption;
import core.model.TileMap;
import core.template.EffectTemplate;
import core.template.ItemOptionTemplate;
import core.template.ItemTemplate;
import core.template.MobTemplate;
import core.template.NpcTemplate;
import core.template.SkillOptionTemplate;
import core.template.SkillTemplate;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class DataStream {
    public static boolean isLoadData;
    public static boolean isLoadMap;
    public static boolean isLoadSkill;
    public static boolean isLoadItem;
    public static long[] arrExp;
    public static SkillOptionTemplate[] sOptionTemplates;
    public static NClass[] nClasss;
    public static ItemOptionTemplate[] iOptionTemplates;

    public static synchronized void loadData(byte[] ba) {
        try {
            int l;
            int i;
            if (isLoadData) {
                return;
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
            byte vsData = dis.readByte();
            for (int i2 = 0; i2 < 5; ++i2) {
                int lengh = dis.readInt();
                byte[] data = new byte[lengh];
                dis.read(data);
            }
            int size = dis.readByte();
            for (i = 0; i < size; ++i) {
                int size2 = dis.readByte();
                for (int j = 0; j < size2; ++j) {
                    dis.readByte();
                    dis.readByte();
                }
            }
            arrExp = new long[dis.readUnsignedByte()];
            for (i = 0; i < arrExp.length; ++i) {
                DataStream.arrExp[i] = dis.readLong();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            for (l = 0; l < dis.readByte(); ++l) {
                dis.readInt();
            }
            Effect.effTemplates = new EffectTemplate[dis.readByte()];
            for (int num = 0; num < Effect.effTemplates.length; ++num) {
                Effect.effTemplates[num] = new EffectTemplate();
                Effect.effTemplates[num].id = dis.readByte();
                Effect.effTemplates[num].type = dis.readByte();
                Effect.effTemplates[num].name = dis.readUTF();
                Effect.effTemplates[num].iconId = dis.readShort();
            }
            isLoadData = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static synchronized void loadMap(byte[] ba) {
        try {
            int i;
            if (isLoadMap) {
                return;
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
            dis.readByte();
            TileMap.mapNames = new String[dis.readUnsignedByte()];
            for (i = 0; i < TileMap.mapNames.length; ++i) {
                TileMap.mapNames[i] = dis.readUTF();
            }
            Npc.arrNpcTemplate = new NpcTemplate[dis.readByte()];
            for (i = 0; i < Npc.arrNpcTemplate.length; i = (int)((byte)(i + 1))) {
                Npc.arrNpcTemplate[i] = new NpcTemplate();
                Npc.arrNpcTemplate[i].npcId = i;
                Npc.arrNpcTemplate[i].name = dis.readUTF();
                Npc.arrNpcTemplate[i].headId = dis.readShort();
                Npc.arrNpcTemplate[i].bodyId = dis.readShort();
                Npc.arrNpcTemplate[i].legId = dis.readShort();
                Npc.arrNpcTemplate[i].menu = new String[dis.readByte()][];
                for (int j = 0; j < Npc.arrNpcTemplate[i].menu.length; ++j) {
                    Npc.arrNpcTemplate[i].menu[j] = new String[dis.readByte()];
                    for (int j2 = 0; j2 < Npc.arrNpcTemplate[i].menu[j].length; ++j2) {
                        Npc.arrNpcTemplate[i].menu[j][j2] = dis.readUTF();
                    }
                }
            }
            int size = dis.readUnsignedByte();
            Mob.arrMobTemplate = new MobTemplate[size];
            for (int i2 = 0; i2 < size; ++i2) {
                Mob.arrMobTemplate[i2] = new MobTemplate();
                Mob.arrMobTemplate[i2].mobTemplateId = (short)i2;
                Mob.arrMobTemplate[i2].type = dis.readByte();
                Mob.arrMobTemplate[i2].name = dis.readUTF();
                Mob.arrMobTemplate[i2].hp = dis.readInt();
                Mob.arrMobTemplate[i2].rangeMove = dis.readByte();
                Mob.arrMobTemplate[i2].speed = dis.readByte();
            }
            isLoadMap = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static synchronized void loadSkill(byte[] ba) {
        try {
            if (isLoadSkill) {
                return;
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
            byte vcSkill = dis.readByte();
            int size = dis.readByte();
            sOptionTemplates = new SkillOptionTemplate[size];
            for (int i = 0; i < size; ++i) {
                DataStream.sOptionTemplates[i] = new SkillOptionTemplate();
                DataStream.sOptionTemplates[i].id = i;
                DataStream.sOptionTemplates[i].name = dis.readUTF();
            }
            nClasss = new NClass[dis.readUnsignedByte()];
            for (int j = 0; j < nClasss.length; ++j) {
                DataStream.nClasss[j] = new NClass();
                DataStream.nClasss[j].classId = j;
                DataStream.nClasss[j].name = dis.readUTF();
                DataStream.nClasss[j].skillTemplates = new SkillTemplate[dis.readByte()];
                for (int k = 0; k < DataStream.nClasss[j].skillTemplates.length; ++k) {
                    DataStream.nClasss[j].skillTemplates[k] = new SkillTemplate();
                    DataStream.nClasss[j].skillTemplates[k].id = dis.readByte();
                    DataStream.nClasss[j].skillTemplates[k].name = dis.readUTF();
                    DataStream.nClasss[j].skillTemplates[k].maxPoint = dis.readByte();
                    DataStream.nClasss[j].skillTemplates[k].type = dis.readByte();
                    DataStream.nClasss[j].skillTemplates[k].iconId = dis.readShort();
                    String description = dis.readUTF();
                    DataStream.nClasss[j].skillTemplates[k].description = new String[0];
                    DataStream.nClasss[j].skillTemplates[k].skills = new Skill[dis.readByte()];
                    for (int l = 0; l < DataStream.nClasss[j].skillTemplates[k].skills.length; ++l) {
                        DataStream.nClasss[j].skillTemplates[k].skills[l] = new Skill();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].skillId = dis.readShort();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].template = DataStream.nClasss[j].skillTemplates[k];
                        DataStream.nClasss[j].skillTemplates[k].skills[l].point = dis.readByte();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].level = dis.readByte();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].manaUse = dis.readShort();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].coolDown = dis.readInt();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].dx = dis.readShort();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].dy = dis.readShort();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].maxFight = dis.readByte();
                        DataStream.nClasss[j].skillTemplates[k].skills[l].options = new SkillOption[dis.readByte()];
                        for (int m = 0; m < DataStream.nClasss[j].skillTemplates[k].skills[l].options.length; ++m) {
                            DataStream.nClasss[j].skillTemplates[k].skills[l].options[m] = new SkillOption();
                            DataStream.nClasss[j].skillTemplates[k].skills[l].options[m].param = dis.readShort();
                            DataStream.nClasss[j].skillTemplates[k].skills[l].options[m].optionTemplate = sOptionTemplates[dis.readByte()];
                        }
                        Skills.add(DataStream.nClasss[j].skillTemplates[k].skills[l]);
                    }
                }
            }
            isLoadSkill = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static synchronized void loadItem(byte[] ba) {
        try {
            int i;
            if (isLoadItem) {
                return;
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
            byte vcItem = dis.readByte();
            int size = dis.readUnsignedByte();
            iOptionTemplates = new ItemOptionTemplate[size];
            for (i = 0; i < size; ++i) {
                DataStream.iOptionTemplates[i] = new ItemOptionTemplate();
                DataStream.iOptionTemplates[i].id = i;
                DataStream.iOptionTemplates[i].name = dis.readUTF();
                DataStream.iOptionTemplates[i].type = dis.readByte();
            }
            size = dis.readShort();
            for (i = 0; i < size; ++i) {
                ItemTemplate itemTemplate = new ItemTemplate((short)i, dis.readByte(), dis.readByte(), dis.readUTF(), dis.readUTF(), dis.readByte(), dis.readShort(), dis.readShort(), dis.readBoolean());
                Item.add(itemTemplate);
            }
            isLoadItem = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

