package ipana.modules.render;

import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.Value;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class XRay extends Module {
    public XRay() {
        super("XRay", Keyboard.KEY_NONE,Category.Render,"XRay the blocks.");
        addData(diamond, Blocks.diamond_ore, Blocks.diamond_block);
        addData(gold, Blocks.gold_ore, Blocks.gold_block);
        addData(iron, Blocks.iron_ore, Blocks.iron_block);
        addData(redStone, Blocks.redstone_ore, Blocks.redstone_block);
        addData(lapis, Blocks.lapis_ore, Blocks.lapis_block);
        addData(coal, Blocks.coal_ore, Blocks.coal_block);
        addData(obsidian, Blocks.obsidian);
        addData(emerald, Blocks.emerald_ore, Blocks.emerald_block);
        addData(glowStone, Blocks.glowstone);
        addData(lava, Blocks.lava, Blocks.flowing_lava);
        addData(water, Blocks.water, Blocks.flowing_water);
    }
    public NumberValue<Float> brightness = new NumberValue<>("Brightness", this, 0.2f, 0.0f, 1.0f, 0.1f, "Brightness of the blocks.");
    public BoolValue diamond = new BoolValue("Diamond",this,true,"Diamond");
    public BoolValue gold = new BoolValue("Gold",this,true,"Gold");
    public BoolValue iron = new BoolValue("Iron",this,true,"Iron");
    public BoolValue redStone = new BoolValue("RedStone",this,true,"RedStone");
    public BoolValue lapis = new BoolValue("Lapis",this,true,"Lapis");
    public BoolValue coal = new BoolValue("Coal",this,true,"Coal");
    public BoolValue obsidian = new BoolValue("Obsidian",this,true,"Obsidian");
    public BoolValue emerald = new BoolValue("Emerald",this,true,"Emerald");
    public BoolValue glowStone = new BoolValue("GlowStone",this,true,"GlowStone");
    public BoolValue lava = new BoolValue("Lava",this,true,"Lava");
    public BoolValue water = new BoolValue("Water",this,true,"Water");
    public ArrayList<BlockData> blockDatas = new ArrayList<>();

    @Override
    public void onEnable() {
        mc.renderGlobal.loadRenderers();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.renderGlobal.loadRenderers();
        super.onDisable();
    }

    public void addData(BoolValue value, Block... blocks) {
        this.blockDatas.add(new BlockData(value, blocks));
    }

    public boolean checkAllValidity(Block block) {
        for (BlockData data : blockDatas) {
            if (data.checkValidity(block)) {
                return true;
            }
        }
        return false;
    }

    class BlockData {
        public BlockData(BoolValue value, Block... blocks) {
            this.value = value;
            this.blocks = blocks;
        }

        BoolValue value;
        Block[] blocks;

        public boolean checkValidity(Block block) {
            if (value.getValue()) {
                for (Block xrayBlock : blocks) {
                    if (xrayBlock == block) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
