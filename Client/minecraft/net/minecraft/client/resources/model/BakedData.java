package net.minecraft.client.resources.model;

import ipana.utils.gl.GList;
import ipana.utils.gl.VList;

/*
 VList was never and never will be faster for item rendering keep cry ipana
 */
public class BakedData {
    IBakedModel model;
    int metadata;
    int color;
    GList<IBakedModel> gList;
    //VList<IBakedModel> vList;


    public BakedData(IBakedModel model, int metadata, int color, GList<IBakedModel> gList /*VList<IBakedModel> vList*/) {
        this.model = model;
        this.metadata = metadata;
        this.color = color;
        this.gList = gList;
        //this.vList = vList;
    }

    public IBakedModel model() {
        return model;
    }

    public int metadata() {
        return metadata;
    }

    public int color() {
        return color;
    }

    /*
    public VList<IBakedModel> vList() {
        return vList;
    }

     */

    public GList<IBakedModel> gList() {
        return gList;
    }
}
