package net.minecraft.client.model;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

public class ModelSign extends ModelBase
{
    /** The board on a sign that has the writing on it. */
    public ModelRenderer signBoard;

    /** The stick a sign stands on. */
    public ModelRenderer signStick;
    private int combinedList;

    public ModelSign() {
        this.signBoard = new ModelRenderer(this, 0, 0);
        this.signBoard.addBox(-12.0F, -14.0F, -1.0F, 24, 12, 2, 0.0F);
        this.signStick = new ModelRenderer(this, 0, 14);
        this.signStick.addBox(-1.0F, -2.0F, -1.0F, 2, 14, 2, 0.0F);
        combinedList = GLAllocation.generateDisplayLists(1);
        setupCombinedList(combinedList, 0.0625f, signBoard, signStick);
    }

    /**
     * Renders the sign model through TileEntitySignRenderer
     */
    public void renderSign()
    {
        GL11.glCallList(combinedList);
        //this.signBoard.render(0.0625F);
        //this.signStick.render(0.0625F);
    }
}
