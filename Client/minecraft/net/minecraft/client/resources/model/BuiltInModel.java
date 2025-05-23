package net.minecraft.client.resources.model;

import java.util.ArrayList;
import java.util.List;

import ipana.utils.gl.GList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class BuiltInModel implements IBakedModel {
    private ItemCameraTransforms cameraTransforms;
    private List<BakedData> data;

    public BuiltInModel(ItemCameraTransforms p_i46086_1_) {
        this.cameraTransforms = p_i46086_1_;
        this.data = new ArrayList<>();
    }

    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
        return null;
    }

    public List<BakedQuad> getGeneralQuads() {
        return null;
    }

    public boolean isAmbientOcclusion() {
        return false;
    }

    public boolean isGui3d() {
        return true;
    }

    public boolean isBuiltInRenderer() {
        return true;
    }

    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    public ItemCameraTransforms getItemCameraTransforms() {
        return this.cameraTransforms;
    }

    @Override
    public List<BakedData> bakedLists() {
        return this.data;
    }
}
