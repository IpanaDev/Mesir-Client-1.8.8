package net.minecraft.client.renderer.culling;

public class ClippingHelper {
    public float[][] frustum = new float[6][4];
    public float[] projectionMatrix = new float[16];
    public float[] modelviewMatrix = new float[16];
    public float[] clippingMatrix = new float[16];

    /**
     * Returns true if the box is inside all 6 clipping planes, otherwise returns false.
     */
    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float fMinX = (float) minX;
        float fMinY = (float) minY;
        float fMinZ = (float) minZ;
        float fMaxX = (float) maxX;
        float fMaxY = (float) maxY;
        float fMaxZ = (float) maxZ;

        for (int i = 0; i < 6; ++i) {
            float[] floats = this.frustum[i];
            float float0 = floats[0];
            float float1 = floats[1];
            float float2 = floats[2];
            float float3 = floats[3];
            float neg0 = -(fMinZ*float2 + float3);
            float cMinMin = fMinX*float0 + fMinY*float1;
            if (cMinMin > neg0) {
                continue;
            }
            float cMaxMin = fMaxX*float0 + fMinY*float1;
            if (cMaxMin > neg0) {
                continue;
            }
            float cMinMax = fMinX*float0 + fMaxY*float1;
            if (cMinMax > neg0) {
                continue;
            }
            float cMaxMax = fMaxX*float0 + fMaxY*float1;
            if (cMaxMax > neg0) {
                continue;
            }
            float neg1 = -(fMaxZ*float2 + float3);
            if (cMinMin <= neg1 && cMaxMin <= neg1 && cMinMax <= neg1 && cMaxMax <= neg1) {
                return false;
            }
        }
        return true;
    }

    public boolean isBoxInFrustumOld(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float fMinX = (float) minX;
        float fMinY = (float) minY;
        float fMinZ = (float) minZ;
        float fMaxX = (float) maxX;
        float fMaxY = (float) maxY;
        float fMaxZ = (float) maxZ;

        for (int i = 0; i < 6; ++i) {
            float[] afloat = this.frustum[i];

            if (
                    this.dot(afloat, fMinX, fMinY, fMinZ) <= 0.0F &&
                    this.dot(afloat, fMaxX, fMinY, fMinZ) <= 0.0F &&
                    this.dot(afloat, fMinX, fMaxY, fMinZ) <= 0.0F &&
                    this.dot(afloat, fMaxX, fMaxY, fMinZ) <= 0.0F &&
                    this.dot(afloat, fMinX, fMinY, fMaxZ) <= 0.0F &&
                    this.dot(afloat, fMaxX, fMinY, fMaxZ) <= 0.0F &&
                    this.dot(afloat, fMinX, fMaxY, fMaxZ) <= 0.0F &&
                    this.dot(afloat, fMaxX, fMaxY, fMaxZ) <= 0.0F) {
                return false;
            }
        }

        return true;
    }

    private float dot(float[] p_dot_1_, float p_dot_2_, float p_dot_3_, float p_dot_4_) {
        return p_dot_1_[0] * p_dot_2_ + p_dot_1_[1] * p_dot_3_ + p_dot_1_[2] * p_dot_4_ + p_dot_1_[3];
    }

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float f = (float) minX;
        float f1 = (float) minY;
        float f2 = (float) minZ;
        float f3 = (float) maxX;
        float f4 = (float) maxY;
        float f5 = (float) maxZ;

        for (int i = 0; i < 6; ++i) {
            float[] afloat = this.frustum[i];
            float f6 = afloat[0];
            float f7 = afloat[1];
            float f8 = afloat[2];
            float f9 = afloat[3];

            boolean b = f6 * f + f7 * f1 + f8 * f2 + f9 <= 0.0F;
            boolean b1 = f6 * f3 + f7 * f1 + f8 * f2 + f9 <= 0.0F;
            boolean b2 = f6 * f + f7 * f4 + f8 * f2 + f9 <= 0.0F;
            boolean b3 = f6 * f3 + f7 * f4 + f8 * f2 + f9 <= 0.0F;
            boolean b4 = f6 * f + f7 * f1 + f8 * f5 + f9 <= 0.0F;
            if (i < 4) {
                if (b || b1 || b2 || b3 || b4 || f6 * f3 + f7 * f1 + f8 * f5 + f9 <= 0.0F || f6 * f + f7 * f4 + f8 * f5 + f9 <= 0.0F || f6 * f3 + f7 * f4 + f8 * f5 + f9 <= 0.0F) {
                    return false;
                }
            } else if (b && b1 && b2 && b3 && b4 && f6 * f3 + f7 * f1 + f8 * f5 + f9 <= 0.0F && f6 * f + f7 * f4 + f8 * f5 + f9 <= 0.0F && f6 * f3 + f7 * f4 + f8 * f5 + f9 <= 0.0F) {
                return false;
            }
        }

        return true;
    }
}
