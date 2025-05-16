package net.minecraft.util.matrix;

import net.minecraft.util.MathHelper;

public final class MatrixVec3f {
    public static MatrixVec3f XN = new MatrixVec3f(-1.0F, 0.0F, 0.0F);
    public static MatrixVec3f XP = new MatrixVec3f(1.0F, 0.0F, 0.0F);
    public static MatrixVec3f YN = new MatrixVec3f(0.0F, -1.0F, 0.0F);
    public static MatrixVec3f YP = new MatrixVec3f(0.0F, 1.0F, 0.0F);
    public static MatrixVec3f ZN = new MatrixVec3f(0.0F, 0.0F, -1.0F);
    public static MatrixVec3f ZP = new MatrixVec3f(0.0F, 0.0F, 1.0F);
    private float x;
    private float y;
    private float z;

    public MatrixVec3f()
    {
    }

    public MatrixVec3f(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MatrixVec3f(MatrixVec3f vecIn)
    {
        this((float)vecIn.x, (float)vecIn.y, (float)vecIn.z);
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass())
        {
            MatrixVec3f vector3f = (MatrixVec3f)p_equals_1_;

            if (Float.compare(vector3f.x, this.x) != 0)
            {
                return false;
            }
            else if (Float.compare(vector3f.y, this.y) != 0)
            {
                return false;
            }
            else
            {
                return Float.compare(vector3f.z, this.z) == 0;
            }
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int i = Float.floatToIntBits(this.x);
        i = 31 * i + Float.floatToIntBits(this.y);
        return 31 * i + Float.floatToIntBits(this.z);
    }

    public float getX()
    {
        return this.x;
    }

    public float getY()
    {
        return this.y;
    }

    public float getZ()
    {
        return this.z;
    }

    public void mul(float multiplier)
    {
        this.x *= multiplier;
        this.y *= multiplier;
        this.z *= multiplier;
    }

    public void mul(float mx, float my, float mz)
    {
        this.x *= mx;
        this.y *= my;
        this.z *= mz;
    }

    public void clamp(float min, float max)
    {
        this.x = MathHelper.clamp_float(this.x, min, max);
        this.y = MathHelper.clamp_float(this.y, min, max);
        this.z = MathHelper.clamp_float(this.z, min, max);
    }

    public void set(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(float x, float y, float z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void add(MatrixVec3f vectorIn)
    {
        this.x += vectorIn.x;
        this.y += vectorIn.y;
        this.z += vectorIn.z;
    }

    public void sub(MatrixVec3f vec)
    {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
    }

    public float dot(MatrixVec3f vec)
    {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public boolean normalize()
    {
        float f = this.x * this.x + this.y * this.y + this.z * this.z;

        if ((double)f < 1.0E-5D)
        {
            return false;
        }
        else
        {
            float f1 = MathHelper.fastInvSqrt(f);
            this.x *= f1;
            this.y *= f1;
            this.z *= f1;
            return true;
        }
    }

    public void cross(MatrixVec3f vec)
    {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        float f3 = vec.getX();
        float f4 = vec.getY();
        float f5 = vec.getZ();
        this.x = f1 * f5 - f2 * f4;
        this.y = f2 * f3 - f * f5;
        this.z = f * f4 - f1 * f3;
    }

    public void transform(Matrix3f matrixIn)
    {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        this.x = matrixIn.m00 * f + matrixIn.m01 * f1 + matrixIn.m02 * f2;
        this.y = matrixIn.m10 * f + matrixIn.m11 * f1 + matrixIn.m12 * f2;
        this.z = matrixIn.m20 * f + matrixIn.m21 * f1 + matrixIn.m22 * f2;
    }

    public void transform(Quaternion quaternionIn)
    {
        Quaternion quaternion = new Quaternion(quaternionIn);
        quaternion.multiply(new Quaternion(this.getX(), this.getY(), this.getZ(), 0.0F));
        Quaternion quaternion1 = new Quaternion(quaternionIn);
        quaternion1.conjugate();
        quaternion.multiply(quaternion1);
        this.set(quaternion.getX(), quaternion.getY(), quaternion.getZ());
    }

    public void lerp(MatrixVec3f vectorIn, float pctIn)
    {
        float f = 1.0F - pctIn;
        this.x = this.x * f + vectorIn.x * pctIn;
        this.y = this.y * f + vectorIn.y * pctIn;
        this.z = this.z * f + vectorIn.z * pctIn;
    }

    public Quaternion rotation(float valueIn)
    {
        return new Quaternion(this, valueIn, false);
    }

    public Quaternion rotationDegrees(float valueIn)
    {
        return new Quaternion(this, valueIn, true);
    }

    public MatrixVec3f copy()
    {
        return new MatrixVec3f(this.x, this.y, this.z);
    }

    public String toString()
    {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
